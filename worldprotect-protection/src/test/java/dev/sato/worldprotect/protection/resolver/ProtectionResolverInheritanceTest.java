package dev.sato.worldprotect.protection.resolver;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.core.actor.ActorType;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.permission.PermissionSet;
import dev.sato.worldprotect.protection.permission.ProtectionSubjectContext;
import dev.sato.worldprotect.protection.query.CauseChain;
import dev.sato.worldprotect.protection.query.ProtectionCause;
import dev.sato.worldprotect.protection.query.ProtectionAction;
import dev.sato.worldprotect.protection.query.ProtectionQuery;
import dev.sato.worldprotect.protection.query.ProtectionTarget;
import dev.sato.worldprotect.protection.region.CuboidRegion;
import dev.sato.worldprotect.protection.region.Region;
import dev.sato.worldprotect.protection.region.RegionFlags;
import dev.sato.worldprotect.protection.region.RegionId;
import dev.sato.worldprotect.protection.region.RegionSet;
import dev.sato.worldprotect.protection.result.DecisionState;
import dev.sato.worldprotect.protection.result.ProtectionDecision;
import dev.sato.worldprotect.protection.rule.FlagRule;
import dev.sato.worldprotect.protection.subject.ActorSubjects;
import dev.sato.worldprotect.protection.subject.RegionAccessPolicy;
import dev.sato.worldprotect.protection.subject.RegionSubjects;
import dev.sato.worldprotect.protection.subject.SubjectRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public final class ProtectionResolverInheritanceTest {

    private DimensionRef overworld;
    private BlockPosRef pos;
    private ProtectionQuery query;
    private ProtectionResolver resolver;
    private Actor defaultActor;

    @BeforeEach
    public void setUp() {
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        pos = new BlockPosRef(5, 5, 5);
        defaultActor = new Actor("player1", ActorType.PLAYER);
        query = new ProtectionQuery(
                defaultActor,
                ProtectionAction.BLOCK_BREAK,
                CauseChain.of(ProtectionCause.player()),
                ProtectionTarget.unknown(),
                overworld,
                pos
        );
        resolver = new ProtectionResolver();
    }

    @Test
    public void testInheritedDenyApplies() {
        // Parent defines BLOCK_BREAK to DENY
        Region rParent = new CuboidRegion(
                RegionId.of("parent"), overworld, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 10,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY)))
        );
        // Child is sub-region, has no flags, priority 20
        Region rChild = new CuboidRegion(
                RegionId.of("child"), overworld, new BlockPosRef(2, 2, 2), new BlockPosRef(8, 8, 8), 20,
                RegionFlags.empty(), RegionSubjects.empty(), RegionAccessPolicy.defaults(),
                Optional.of(RegionId.of("parent"))
        );

        RegionSet set = RegionSet.of(List.of(rParent, rChild));

        // Query on child pos
        ProtectionDecision decision = resolver.resolve(query, set);
        assertEquals(DecisionState.DENY, decision.state());
        assertEquals(RegionId.of("child"), decision.regionId().orElse(null));
        assertEquals(BuiltInFlags.BREAK_BLOCK_KEY, decision.flagKey().orElse(null));
    }

    @Test
    public void testChildOverrideParentRule() {
        // Parent defines BLOCK_BREAK to DENY, priority 10
        Region rParent = new CuboidRegion(
                RegionId.of("parent"), overworld, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 10,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY)))
        );
        // Child overrides with ALLOW, priority 20
        Region rChild = new CuboidRegion(
                RegionId.of("child"), overworld, new BlockPosRef(2, 2, 2), new BlockPosRef(8, 8, 8), 20,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.ALLOW))),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(),
                Optional.of(RegionId.of("parent"))
        );

        RegionSet set = RegionSet.of(List.of(rParent, rChild));

        // Query on child pos should be allowed
        ProtectionDecision decision = resolver.resolve(query, set);
        assertEquals(DecisionState.ALLOW, decision.state());
        assertEquals(RegionId.of("child"), decision.regionId().orElse(null));
    }

    @Test
    public void testInheritedOwnerBypassWithChildAccessPolicy() {
        UUID playerUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        SubjectRef ownerRef = SubjectRef.player(playerUuid);
        
        // Parent defines DENY, owner is ownerRef, but parent access policy disallows bypass, priority 10
        Region rParent = new CuboidRegion(
                RegionId.of("parent"), overworld, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 10,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.of(Set.of(ownerRef), Set.of()),
                RegionAccessPolicy.of(false, false, Set.of(), Set.of()) // parent disallows owner bypass
        );

        // Child has no flags, inherits parent rule. Child access policy ALLOWS owner bypass, priority 20
        Region rChild = new CuboidRegion(
                RegionId.of("child"), overworld, new BlockPosRef(2, 2, 2), new BlockPosRef(8, 8, 8), 20,
                RegionFlags.empty(),
                RegionSubjects.empty(),
                RegionAccessPolicy.of(true, false, Set.of(), Set.of()), // child allows owner bypass
                Optional.of(RegionId.of("parent"))
        );

        RegionSet set = RegionSet.of(List.of(rParent, rChild));

        // Actor context for ownerRef
        Actor playerActor = new Actor(playerUuid.toString(), ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.player(playerActor, playerUuid, List.of());
        ProtectionSubjectContext ctx = ProtectionSubjectContext.of(
                actorSubjects,
                PermissionSet.empty()
        );

        // Resolve query for child pos.
        // Role should be owner (inherited from parent).
        // Access policy checked must be child (allows bypass).
        // Therefore, it should be ALLOWED because the child's local access policy is checked!
        ProtectionDecision decision = resolver.resolve(query, set, ctx);
        assertEquals(DecisionState.ALLOW, decision.state());
    }

    @Test
    public void testInheritedOwnerBypassFailsIfChildAccessPolicyDisallows() {
        UUID playerUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        SubjectRef ownerRef = SubjectRef.player(playerUuid);

        // Parent defines DENY, owner is ownerRef. Parent access policy allows owner bypass, priority 10
        Region rParent = new CuboidRegion(
                RegionId.of("parent"), overworld, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 10,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.of(Set.of(ownerRef), Set.of()),
                RegionAccessPolicy.of(true, false, Set.of(), Set.of()) // parent allows
        );

        // Child inherits parent rule. Child access policy disallows owner bypass, priority 20
        Region rChild = new CuboidRegion(
                RegionId.of("child"), overworld, new BlockPosRef(2, 2, 2), new BlockPosRef(8, 8, 8), 20,
                RegionFlags.empty(),
                RegionSubjects.empty(),
                RegionAccessPolicy.of(false, false, Set.of(), Set.of()), // child disallows
                Optional.of(RegionId.of("parent"))
        );

        RegionSet set = RegionSet.of(List.of(rParent, rChild));

        // Actor context for ownerRef
        Actor playerActor = new Actor(playerUuid.toString(), ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.player(playerActor, playerUuid, List.of());
        ProtectionSubjectContext ctx = ProtectionSubjectContext.of(
                actorSubjects,
                PermissionSet.empty()
        );

        // Resolve query. Child disallows owner bypass, so it must return DENY.
        ProtectionDecision decision = resolver.resolve(query, set, ctx);
        assertEquals(DecisionState.DENY, decision.state());
    }

    @Test
    public void testParentPermissionBypassDoesNotApplyToChild() {
        UUID playerUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

        // Parent defines DENY, priority 10
        Region rParent = new CuboidRegion(
                RegionId.of("parent"), overworld, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 10,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY)))
        );

        // Child inherits parent rule. Child access policy allows owner bypass, priority 20.
        Region rChild = new CuboidRegion(
                RegionId.of("child"), overworld, new BlockPosRef(2, 2, 2), new BlockPosRef(8, 8, 8), 20,
                RegionFlags.empty(),
                RegionSubjects.empty(),
                RegionAccessPolicy.of(true, false, Set.of(), Set.of()), // child allows
                Optional.of(RegionId.of("parent"))
        );

        RegionSet set = RegionSet.of(List.of(rParent, rChild));

        // Actor has bypass permission for parent ("worldprotect.region.parent.owner"),
        // but NOT for child ("worldprotect.region.child.owner").
        Actor playerActor = new Actor(playerUuid.toString(), ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.player(playerActor, playerUuid, List.of());
        ProtectionSubjectContext ctx = ProtectionSubjectContext.of(
                actorSubjects,
                PermissionSet.ofStrings(List.of("worldprotect.region.parent.owner"))
        );

        // Resolve query. Parent permission bypass must not bypass child decision. Must return DENY.
        ProtectionDecision decision = resolver.resolve(query, set, ctx);
        assertEquals(DecisionState.DENY, decision.state());
    }

    @Test
    public void testMatchingParentRegionWithHigherPriorityWins() {
        // Parent defines BLOCK_BREAK to ALLOW, priority 20
        Region rParent = new CuboidRegion(
                RegionId.of("parent"), overworld, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 20,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.ALLOW)))
        );
        // Child defines BLOCK_BREAK to DENY, priority 10
        Region rChild = new CuboidRegion(
                RegionId.of("child"), overworld, new BlockPosRef(2, 2, 2), new BlockPosRef(8, 8, 8), 10,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(),
                Optional.of(RegionId.of("parent"))
        );

        RegionSet set = RegionSet.of(List.of(rParent, rChild));

        // Query on child pos. Since parent matches and has higher priority, its ALLOW should win.
        ProtectionDecision decision = resolver.resolve(query, set);
        assertEquals(DecisionState.ALLOW, decision.state());
        assertEquals(RegionId.of("parent"), decision.regionId().orElse(null));
    }

    @Test
    public void testLowerPriorityGlobalParentDoesNotOverrideHigherPriorityChild() {
        // Parent is global region with priority 5, ALLOW
        Region rParent = new dev.sato.worldprotect.protection.region.GlobalRegion(
                RegionId.of("parent"), overworld, 5,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.ALLOW))),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(),
                Optional.empty()
        );
        // Child is cuboid with priority 10, DENY, parent = parent
        Region rChild = new CuboidRegion(
                RegionId.of("child"), overworld, new BlockPosRef(2, 2, 2), new BlockPosRef(8, 8, 8), 10,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY))),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(),
                Optional.of(RegionId.of("parent"))
        );

        RegionSet set = RegionSet.of(List.of(rParent, rChild));

        // Query on child pos. Child's priority 10 DENY must win over parent's priority 5 ALLOW.
        ProtectionDecision decision = resolver.resolve(query, set);
        assertEquals(DecisionState.DENY, decision.state());
        assertEquals(RegionId.of("child"), decision.regionId().orElse(null));
    }

    @Test
    public void testParentCanStillMakeItsOwnDecisionIfMatchesAsNormalRegion() {
        // Parent defines BLOCK_BREAK to DENY
        Region rParent = new CuboidRegion(
                RegionId.of("parent"), overworld, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 10,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.DENY)))
        );
        // Child defines BLOCK_BREAK to ALLOW
        Region rChild = new CuboidRegion(
                RegionId.of("child"), overworld, new BlockPosRef(2, 2, 2), new BlockPosRef(8, 8, 8), 10,
                RegionFlags.ofRules(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.ALLOW))),
                RegionSubjects.empty(), RegionAccessPolicy.defaults(),
                Optional.of(RegionId.of("parent"))
        );

        RegionSet set = RegionSet.of(List.of(rParent, rChild));

        // Query outside child, but inside parent: e.g. pos = (1, 1, 1)
        ProtectionQuery outsideQuery = new ProtectionQuery(
                defaultActor,
                ProtectionAction.BLOCK_BREAK,
                CauseChain.of(ProtectionCause.player()),
                ProtectionTarget.unknown(),
                overworld,
                new BlockPosRef(1, 1, 1)
        );

        // Resolve: parent matches and returns DENY, child does not match.
        ProtectionDecision decision = resolver.resolve(outsideQuery, set);
        assertEquals(DecisionState.DENY, decision.state());
        assertEquals(RegionId.of("parent"), decision.regionId().orElse(null));
    }
}
