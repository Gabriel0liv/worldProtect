package dev.sato.worldprotect.protection.resolver;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.region.CuboidRegion;
import dev.sato.worldprotect.protection.region.Region;
import dev.sato.worldprotect.protection.region.RegionFlags;
import dev.sato.worldprotect.protection.region.RegionId;
import dev.sato.worldprotect.protection.region.RegionSet;
import dev.sato.worldprotect.protection.rule.FlagRule;
import dev.sato.worldprotect.protection.subject.RegionAccessPolicy;
import dev.sato.worldprotect.protection.subject.RegionSubjects;
import dev.sato.worldprotect.protection.subject.SubjectRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionInheritanceResolverTest {

    private DimensionRef overworld;
    private BlockPosRef min;
    private BlockPosRef max;

    @BeforeEach
    public void setUp() {
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        min = new BlockPosRef(0, 0, 0);
        max = new BlockPosRef(10, 10, 10);
    }

    @Test
    public void testLineageOrder() {
        Region rRoot = new CuboidRegion(RegionId.of("root"), overworld, min, max, 0, RegionFlags.empty(), RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty());
        Region rParent = new CuboidRegion(RegionId.of("parent"), overworld, min, max, 0, RegionFlags.empty(), RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.of(RegionId.of("root")));
        Region rChild = new CuboidRegion(RegionId.of("child"), overworld, min, max, 0, RegionFlags.empty(), RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.of(RegionId.of("parent")));

        RegionSet set = RegionSet.of(List.of(rRoot, rParent, rChild));
        RegionInheritanceResolver resolver = new RegionInheritanceResolver(set);

        List<Region> lineage = resolver.lineage(rChild);
        assertEquals(3, lineage.size());
        assertEquals(rChild, lineage.get(0));
        assertEquals(rParent, lineage.get(1));
        assertEquals(rRoot, lineage.get(2));
    }

    @Test
    public void testEffectiveFlagRulePrecedence() {
        FlagRule rootRule = FlagRule.simple(FlagState.DENY);
        FlagRule parentRule = FlagRule.simple(FlagState.ALLOW);
        
        Region rRoot = new CuboidRegion(RegionId.of("root"), overworld, min, max, 0, RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, rootRule)), RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.empty());
        Region rParent = new CuboidRegion(RegionId.of("parent"), overworld, min, max, 0, RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, parentRule)), RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.of(RegionId.of("root")));
        Region rChild = new CuboidRegion(RegionId.of("child"), overworld, min, max, 0, RegionFlags.empty(), RegionSubjects.empty(), RegionAccessPolicy.defaults(), Optional.of(RegionId.of("parent")));

        RegionSet set = RegionSet.of(List.of(rRoot, rParent, rChild));
        RegionInheritanceResolver resolver = new RegionInheritanceResolver(set);

        // Child inherits from parent (ALLOW) which overrides root (DENY)
        Optional<FlagRule> childRule = resolver.effectiveFlagRule(rChild, BuiltInFlags.BREAK_BLOCK_KEY);
        assertTrue(childRule.isPresent());
        assertEquals(parentRule, childRule.get());

        // Parent inherits/defines parentRule (ALLOW)
        Optional<FlagRule> parentRuleResolved = resolver.effectiveFlagRule(rParent, BuiltInFlags.BREAK_BLOCK_KEY);
        assertTrue(parentRuleResolved.isPresent());
        assertEquals(parentRule, parentRuleResolved.get());

        // Root defines rootRule (DENY)
        Optional<FlagRule> rootRuleResolved = resolver.effectiveFlagRule(rRoot, BuiltInFlags.BREAK_BLOCK_KEY);
        assertTrue(rootRuleResolved.isPresent());
        assertEquals(rootRule, rootRuleResolved.get());
    }

    @Test
    public void testEffectiveSubjectsMerging() {
        SubjectRef p1 = SubjectRef.player(java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"));
        SubjectRef p2 = SubjectRef.player(java.util.UUID.fromString("22222222-2222-2222-2222-222222222222"));
        SubjectRef p3 = SubjectRef.player(java.util.UUID.fromString("33333333-3333-3333-3333-333333333333"));

        RegionSubjects rootSubjects = RegionSubjects.of(Set.of(p1), Set.of()); // root owner: p1
        RegionSubjects parentSubjects = RegionSubjects.of(Set.of(p2), Set.of(p1)); // parent owner: p2, parent member: p1
        RegionSubjects childSubjects = RegionSubjects.of(Set.of(), Set.of(p3)); // child member: p3

        Region rRoot = new CuboidRegion(RegionId.of("root"), overworld, min, max, 0, RegionFlags.empty(), rootSubjects, RegionAccessPolicy.defaults(), Optional.empty());
        Region rParent = new CuboidRegion(RegionId.of("parent"), overworld, min, max, 0, RegionFlags.empty(), parentSubjects, RegionAccessPolicy.defaults(), Optional.of(RegionId.of("root")));
        Region rChild = new CuboidRegion(RegionId.of("child"), overworld, min, max, 0, RegionFlags.empty(), childSubjects, RegionAccessPolicy.defaults(), Optional.of(RegionId.of("parent")));

        RegionSet set = RegionSet.of(List.of(rRoot, rParent, rChild));
        RegionInheritanceResolver resolver = new RegionInheritanceResolver(set);

        RegionSubjects effective = resolver.effectiveSubjects(rChild);

        // Owners combined: p1 (from root), p2 (from parent)
        // Members combined: p1 (from parent), p3 (from child)
        // But owner status overrides member status globally, so p1 is owner, p2 is owner, p3 is member
        assertTrue(effective.owners().contains(p1));
        assertTrue(effective.owners().contains(p2));
        assertTrue(effective.members().contains(p3));
        assertFalse(effective.members().contains(p1));
    }
}
