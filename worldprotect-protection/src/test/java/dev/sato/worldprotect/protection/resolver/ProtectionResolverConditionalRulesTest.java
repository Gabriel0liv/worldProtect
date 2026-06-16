package dev.sato.worldprotect.protection.resolver;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.core.actor.ActorType;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.query.CauseChain;
import dev.sato.worldprotect.protection.query.ProtectionAction;
import dev.sato.worldprotect.protection.query.ProtectionCause;
import dev.sato.worldprotect.protection.query.ProtectionQuery;
import dev.sato.worldprotect.protection.query.ProtectionTarget;
import dev.sato.worldprotect.protection.region.CuboidRegion;
import dev.sato.worldprotect.protection.region.Region;
import dev.sato.worldprotect.protection.region.RegionFlags;
import dev.sato.worldprotect.protection.region.RegionId;
import dev.sato.worldprotect.protection.region.RegionSet;
import dev.sato.worldprotect.protection.rule.FlagRule;
import dev.sato.worldprotect.protection.rule.ResourceSelector;
import dev.sato.worldprotect.protection.rule.ResourceSelectorSet;
import dev.sato.worldprotect.protection.result.ProtectionDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public final class ProtectionResolverConditionalRulesTest {

    private DimensionRef overworld;
    private BlockPosRef posInside;
    private BlockPosRef min;
    private BlockPosRef max;
    private Actor actor;
    private ProtectionResolver resolver;

    @BeforeEach
    public void setUp() {
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        posInside = new BlockPosRef(5, 5, 5);
        min = new BlockPosRef(0, 0, 0);
        max = new BlockPosRef(10, 10, 10);
        actor = new Actor("player1", ActorType.PLAYER);
        resolver = new ProtectionResolver();
    }

    // 1. use-item default DENY with allow create:wrench allows create:wrench
    @Test
    public void testUseItemDefaultDenyWithAllowWrench() {
        ResourceSelectorSet allow = ResourceSelectorSet.of(List.of(ResourceSelector.parse("create:wrench")));
        FlagRule rule = FlagRule.conditional(FlagState.DENY, allow, ResourceSelectorSet.empty());

        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0,
                RegionFlags.ofRules(Map.of(BuiltInFlags.USE_ITEM_KEY, rule)));
        RegionSet set = RegionSet.of(List.of(region));

        CauseChain causes = CauseChain.of(ProtectionCause.item(ResourceRef.of("create", "wrench")));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.ITEM_USE, causes, ProtectionTarget.unknown(), overworld, posInside);

        ProtectionDecision decision = resolver.resolve(query, set);
        assertTrue(decision.isAllowed());
        assertTrue(decision.reason().contains("allow selector"));
    }

    // 2. use-item default DENY denies non-allowed item
    @Test
    public void testUseItemDefaultDenyDeniesStick() {
        ResourceSelectorSet allow = ResourceSelectorSet.of(List.of(ResourceSelector.parse("create:wrench")));
        FlagRule rule = FlagRule.conditional(FlagState.DENY, allow, ResourceSelectorSet.empty());

        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0,
                RegionFlags.ofRules(Map.of(BuiltInFlags.USE_ITEM_KEY, rule)));
        RegionSet set = RegionSet.of(List.of(region));

        CauseChain causes = CauseChain.of(ProtectionCause.item(ResourceRef.of("minecraft", "stick")));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.ITEM_USE, causes, ProtectionTarget.unknown(), overworld, posInside);

        ProtectionDecision decision = resolver.resolve(query, set);
        assertTrue(decision.isDenied());
        assertTrue(decision.reason().contains("default"));
    }

    // 3. use-item default ALLOW with deny botania:twig_wand denies that item
    @Test
    public void testUseItemDefaultAllowDeniesWand() {
        ResourceSelectorSet deny = ResourceSelectorSet.of(List.of(ResourceSelector.parse("botania:twig_wand")));
        FlagRule rule = FlagRule.conditional(FlagState.ALLOW, ResourceSelectorSet.empty(), deny);

        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0,
                RegionFlags.ofRules(Map.of(BuiltInFlags.USE_ITEM_KEY, rule)));
        RegionSet set = RegionSet.of(List.of(region));

        CauseChain causes = CauseChain.of(ProtectionCause.item(ResourceRef.of("botania", "twig_wand")));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.ITEM_USE, causes, ProtectionTarget.unknown(), overworld, posInside);

        ProtectionDecision decision = resolver.resolve(query, set);
        assertTrue(decision.isDenied());
        assertTrue(decision.reason().contains("deny selector"));
    }

    // 4. namespace wildcard create:* allows create:wrench
    @Test
    public void testNamespaceWildcardAllowsWrench() {
        ResourceSelectorSet allow = ResourceSelectorSet.of(List.of(ResourceSelector.parse("create:*")));
        FlagRule rule = FlagRule.conditional(FlagState.DENY, allow, ResourceSelectorSet.empty());

        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0,
                RegionFlags.ofRules(Map.of(BuiltInFlags.USE_ITEM_KEY, rule)));
        RegionSet set = RegionSet.of(List.of(region));

        CauseChain causes = CauseChain.of(ProtectionCause.item(ResourceRef.of("create", "wrench")));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.ITEM_USE, causes, ProtectionTarget.unknown(), overworld, posInside);

        ProtectionDecision decision = resolver.resolve(query, set);
        assertTrue(decision.isAllowed());
        assertTrue(decision.reason().contains("allow selector"));
    }

    // 5. global wildcard allow * allows any item
    @Test
    public void testGlobalWildcardAllowsAnyItem() {
        ResourceSelectorSet allow = ResourceSelectorSet.of(List.of(ResourceSelector.parse("*")));
        FlagRule rule = FlagRule.conditional(FlagState.DENY, allow, ResourceSelectorSet.empty());

        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0,
                RegionFlags.ofRules(Map.of(BuiltInFlags.USE_ITEM_KEY, rule)));
        RegionSet set = RegionSet.of(List.of(region));

        CauseChain causes = CauseChain.of(ProtectionCause.item(ResourceRef.of("minecraft", "stick")));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.ITEM_USE, causes, ProtectionTarget.unknown(), overworld, posInside);

        ProtectionDecision decision = resolver.resolve(query, set);
        assertTrue(decision.isAllowed());
    }

    // 6. deny selector beats allow selector
    @Test
    public void testDenySelectorBeatsAllowSelector() {
        ResourceSelector allowSel = ResourceSelector.parse("create:*");
        ResourceSelector denySel = ResourceSelector.parse("create:creative_motor");
        ResourceSelectorSet allow = ResourceSelectorSet.of(List.of(allowSel));
        ResourceSelectorSet deny = ResourceSelectorSet.of(List.of(denySel));
        FlagRule rule = FlagRule.conditional(FlagState.DENY, allow, deny);

        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0,
                RegionFlags.ofRules(Map.of(BuiltInFlags.USE_ITEM_KEY, rule)));
        RegionSet set = RegionSet.of(List.of(region));

        // wrench is allowed
        CauseChain causesWrench = CauseChain.of(ProtectionCause.item(ResourceRef.of("create", "wrench")));
        ProtectionQuery queryWrench = new ProtectionQuery(actor, ProtectionAction.ITEM_USE, causesWrench, ProtectionTarget.unknown(), overworld, posInside);
        assertTrue(resolver.resolve(queryWrench, set).isAllowed());

        // creative motor is denied
        CauseChain causesMotor = CauseChain.of(ProtectionCause.item(ResourceRef.of("create", "creative_motor")));
        ProtectionQuery queryMotor = new ProtectionQuery(actor, ProtectionAction.ITEM_USE, causesMotor, ProtectionTarget.unknown(), overworld, posInside);
        assertTrue(resolver.resolve(queryMotor, set).isDenied());
    }

    // 7. interact-block default DENY with allow minecraft:oak_door allows oak door
    @Test
    public void testInteractBlockAllowsOakDoor() {
        ResourceSelectorSet allow = ResourceSelectorSet.of(List.of(ResourceSelector.parse("minecraft:oak_door")));
        FlagRule rule = FlagRule.conditional(FlagState.DENY, allow, ResourceSelectorSet.empty());

        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0,
                RegionFlags.ofRules(Map.of(BuiltInFlags.INTERACT_BLOCK_KEY, rule)));
        RegionSet set = RegionSet.of(List.of(region));

        ProtectionTarget target = ProtectionTarget.block(ResourceRef.of("minecraft", "oak_door"), overworld, posInside);
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BLOCK_INTERACT, CauseChain.of(ProtectionCause.player()), target, overworld, posInside);

        ProtectionDecision decision = resolver.resolve(query, set);
        assertTrue(decision.isAllowed());
    }

    // 8. explosion-drop-items default DENY denies diamond drop
    @Test
    public void testExplosionDropItemsDefaultDeny() {
        FlagRule rule = FlagRule.simple(FlagState.DENY);

        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0,
                RegionFlags.ofRules(Map.of(BuiltInFlags.EXPLOSION_DROP_ITEMS_KEY, rule)));
        RegionSet set = RegionSet.of(List.of(region));

        ProtectionTarget target = ProtectionTarget.drop(ResourceRef.of("minecraft", "diamond"), overworld, posInside);
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.EXPLOSION_ITEM_DROP, CauseChain.of(ProtectionCause.explosion(null)), target, overworld, posInside);

        ProtectionDecision decision = resolver.resolve(query, set);
        assertTrue(decision.isDenied());
    }

    // 9. block-drops deny is independent from break-block allow
    @Test
    public void testBlockDropsIndependentFromBreakBlock() {
        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0,
                RegionFlags.ofRules(Map.of(
                        BuiltInFlags.BREAK_BLOCK_KEY, FlagRule.simple(FlagState.ALLOW),
                        BuiltInFlags.BLOCK_DROPS_KEY, FlagRule.simple(FlagState.DENY)
                )));
        RegionSet set = RegionSet.of(List.of(region));

        // break-block query is allowed
        ProtectionTarget blockTarget = ProtectionTarget.block(ResourceRef.of("minecraft", "stone"), overworld, posInside);
        ProtectionQuery queryBreak = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, CauseChain.of(ProtectionCause.player()), blockTarget, overworld, posInside);
        assertTrue(resolver.resolve(queryBreak, set).isAllowed());

        // block-drop query is denied
        ProtectionTarget dropTarget = ProtectionTarget.drop(ResourceRef.of("minecraft", "stone"), overworld, posInside);
        ProtectionQuery queryDrop = new ProtectionQuery(actor, ProtectionAction.BLOCK_DROP, CauseChain.of(ProtectionCause.player()), dropTarget, overworld, posInside);
        assertTrue(resolver.resolve(queryDrop, set).isDenied());
    }

    // 10. specific flag still overrides generic fallback
    @Test
    public void testSpecificFlagOverridesGenericFallback() {
        ResourceSelectorSet allowWrench = ResourceSelectorSet.of(List.of(ResourceSelector.parse("create:wrench")));
        FlagRule specificRule = FlagRule.conditional(FlagState.DENY, allowWrench, ResourceSelectorSet.empty());
        FlagRule genericRule = FlagRule.simple(FlagState.DENY);

        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0,
                RegionFlags.ofRules(Map.of(
                        BuiltInFlags.USE_ITEM_ON_BLOCK_KEY, specificRule,
                        BuiltInFlags.USE_ITEM_KEY, genericRule
                )));
        RegionSet set = RegionSet.of(List.of(region));

        CauseChain causes = CauseChain.of(ProtectionCause.item(ResourceRef.of("create", "wrench")));
        ProtectionTarget target = ProtectionTarget.block(ResourceRef.of("minecraft", "furnace"), overworld, posInside);
        
        // Query ITEM_USE_ON_BLOCK using create:wrench.
        // It checks use-item-on-block first, resolving to ALLOW, bypassing use-item.
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.ITEM_USE_ON_BLOCK, causes, target, overworld, posInside);

        ProtectionDecision decision = resolver.resolve(query, set);
        assertTrue(decision.isAllowed());
        assertEquals(BuiltInFlags.USE_ITEM_ON_BLOCK_KEY, decision.flagKey().orElse(null));
    }
}
