package dev.sato.worldprotect.protection.resolver;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.core.actor.ActorType;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
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
import dev.sato.worldprotect.protection.result.ProtectionDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public final class ProtectionResolverTest {

    private DimensionRef overworld;
    private BlockPosRef posInside;
    private BlockPosRef min;
    private BlockPosRef max;
    private Actor actor;
    private CauseChain causeChain;
    private ProtectionTarget target;
    private ProtectionResolver resolver;

    @BeforeEach
    public void setUp() {
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        posInside = new BlockPosRef(5, 5, 5);
        min = new BlockPosRef(0, 0, 0);
        max = new BlockPosRef(10, 10, 10);
        actor = new Actor("player1", ActorType.PLAYER);
        causeChain = CauseChain.of(ProtectionCause.player());
        target = ProtectionTarget.unknown();
        resolver = new ProtectionResolver();
    }

    // 1. no matching region returns PASS
    @Test
    public void testNoMatchingRegionReturnsPass() {
        RegionSet set = RegionSet.of(List.of());
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, posInside);
        ProtectionDecision decision = resolver.resolve(query, set);

        assertTrue(decision.isPass());
        assertTrue(decision.reason().contains("No matching regions found"));
    }

    // 2. matching region with no relevant flags returns PASS
    @Test
    public void testMatchingRegionWithNoRelevantFlagsReturnsPass() {
        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0, RegionFlags.empty());
        RegionSet set = RegionSet.of(List.of(region));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, posInside);
        ProtectionDecision decision = resolver.resolve(query, set);

        assertTrue(decision.isPass());
        assertTrue(decision.reason().contains("No build protection matched") || decision.reason().contains("No explicit flags matched"));
    }

    // 3. break block denied by break-block
    @Test
    public void testBreakBlockDeniedByBreakBlock() {
        RegionFlags flags = RegionFlags.of(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagState.DENY));
        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0, flags);
        RegionSet set = RegionSet.of(List.of(region));
        
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, posInside);
        ProtectionDecision decision = resolver.resolve(query, set);

        assertTrue(decision.isDenied());
        assertEquals(region.getId(), decision.regionId().orElse(null));
        assertEquals(BuiltInFlags.BREAK_BLOCK_KEY, decision.flagKey().orElse(null));
        assertTrue(decision.reason().contains("denied by flag break-block"));
    }

    // 4. place block denied by build fallback
    @Test
    public void testPlaceBlockDeniedByBuildFallback() {
        RegionFlags flags = RegionFlags.of(Map.of(BuiltInFlags.BUILD_KEY, FlagState.DENY));
        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0, flags);
        RegionSet set = RegionSet.of(List.of(region));
        
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BLOCK_PLACE, causeChain, target, overworld, posInside);
        ProtectionDecision decision = resolver.resolve(query, set);

        assertTrue(decision.isDenied());
        assertEquals(region.getId(), decision.regionId().orElse(null));
        assertEquals(BuiltInFlags.BUILD_KEY, decision.flagKey().orElse(null));
        assertTrue(decision.reason().contains("denied by flag build"));
    }

    // 5. higher priority ALLOW overrides lower priority DENY
    @Test
    public void testHigherPriorityAllowOverridesLowerPriorityDeny() {
        // Lower priority region (0) denies break-block
        Region low = new CuboidRegion(RegionId.of("wild"), overworld, min, max, 0,
                RegionFlags.of(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagState.DENY)));
        // Higher priority region (10) allows break-block
        Region high = new CuboidRegion(RegionId.of("plot"), overworld, min, max, 10,
                RegionFlags.of(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagState.ALLOW)));

        RegionSet set = RegionSet.of(List.of(low, high));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, posInside);
        ProtectionDecision decision = resolver.resolve(query, set);

        assertTrue(decision.isAllowed());
        assertEquals(high.getId(), decision.regionId().orElse(null));
        assertEquals(BuiltInFlags.BREAK_BLOCK_KEY, decision.flagKey().orElse(null));
    }

    // 6. higher priority DENY overrides lower priority ALLOW
    @Test
    public void testHigherPriorityDenyOverridesLowerPriorityAllow() {
        // Lower priority region (0) allows break-block
        Region low = new CuboidRegion(RegionId.of("wild"), overworld, min, max, 0,
                RegionFlags.of(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagState.ALLOW)));
        // Higher priority region (10) denies break-block
        Region high = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 10,
                RegionFlags.of(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagState.DENY)));

        RegionSet set = RegionSet.of(List.of(low, high));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, posInside);
        ProtectionDecision decision = resolver.resolve(query, set);

        assertTrue(decision.isDenied());
        assertEquals(high.getId(), decision.regionId().orElse(null));
    }

    // 7. equal priority DENY beats ALLOW
    @Test
    public void testEqualPriorityDenyBeatsAllow() {
        Region rA = new CuboidRegion(RegionId.of("region-a"), overworld, min, max, 10,
                RegionFlags.of(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagState.ALLOW)));
        Region rB = new CuboidRegion(RegionId.of("region-b"), overworld, min, max, 10,
                RegionFlags.of(Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagState.DENY)));

        RegionSet set = RegionSet.of(List.of(rA, rB));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, causeChain, target, overworld, posInside);
        ProtectionDecision decision = resolver.resolve(query, set);

        assertTrue(decision.isDenied());
        // Since DENY beats ALLOW, it should return deny from regionB
        assertEquals(rB.getId(), decision.regionId().orElse(null));
    }

    // 8. ITEM_USE_ON_BLOCK checks specific flag before generic use-item
    @Test
    public void testItemUseOnBlockChecksSpecificFlagBeforeGenericUseItem() {
        // Region denies use-item but allows use-item-on-block
        Region region = new CuboidRegion(RegionId.of("plot"), overworld, min, max, 0,
                RegionFlags.of(Map.of(
                        BuiltInFlags.USE_ITEM_ON_BLOCK_KEY, FlagState.ALLOW,
                        BuiltInFlags.USE_ITEM_KEY, FlagState.DENY
                )));

        RegionSet set = RegionSet.of(List.of(region));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.ITEM_USE_ON_BLOCK, causeChain, target, overworld, posInside);
        ProtectionDecision decision = resolver.resolve(query, set);

        // Since use-item-on-block is mapped before use-item, it should evaluate ALLOW first and win
        assertTrue(decision.isAllowed());
        assertEquals(BuiltInFlags.USE_ITEM_ON_BLOCK_KEY, decision.flagKey().orElse(null));
    }

    // 9. EXPLOSION_BLOCK_DAMAGE denied by explosion-break-blocks
    @Test
    public void testExplosionBlockDamageDeniedByExplosionBreakBlocks() {
        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0,
                RegionFlags.of(Map.of(BuiltInFlags.EXPLOSION_BREAK_BLOCKS_KEY, FlagState.DENY)));
        RegionSet set = RegionSet.of(List.of(region));

        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.EXPLOSION_BLOCK_DAMAGE, causeChain, target, overworld, posInside);
        ProtectionDecision decision = resolver.resolve(query, set);

        assertTrue(decision.isDenied());
        assertEquals(BuiltInFlags.EXPLOSION_BREAK_BLOCKS_KEY, decision.flagKey().orElse(null));
    }

    // 10. EXPLOSION_ITEM_DROP denied independently by explosion-drop-items
    @Test
    public void testExplosionItemDropDeniedIndependentlyByExplosionDropItems() {
        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0,
                RegionFlags.of(Map.of(
                        BuiltInFlags.EXPLOSION_BREAK_BLOCKS_KEY, FlagState.ALLOW,
                        BuiltInFlags.EXPLOSION_DROP_ITEMS_KEY, FlagState.DENY
                )));
        RegionSet set = RegionSet.of(List.of(region));

        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.EXPLOSION_ITEM_DROP, causeChain, target, overworld, posInside);
        ProtectionDecision decision = resolver.resolve(query, set);

        assertTrue(decision.isDenied());
        assertEquals(BuiltInFlags.EXPLOSION_DROP_ITEMS_KEY, decision.flagKey().orElse(null));
    }

    // 11. BLOCK_DROP denied independently by block-drops
    @Test
    public void testBlockDropDeniedIndependentlyByBlockDrops() {
        Region region = new CuboidRegion(RegionId.of("plot"), overworld, min, max, 0,
                RegionFlags.of(Map.of(
                        BuiltInFlags.BREAK_BLOCK_KEY, FlagState.ALLOW,
                        BuiltInFlags.BLOCK_DROPS_KEY, FlagState.DENY
                )));
        RegionSet set = RegionSet.of(List.of(region));

        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BLOCK_DROP, causeChain, target, overworld, posInside);
        ProtectionDecision decision = resolver.resolve(query, set);

        assertTrue(decision.isDenied());
        assertEquals(BuiltInFlags.BLOCK_DROPS_KEY, decision.flagKey().orElse(null));
    }

    // 12. WORLD_MODIFY denied by world-modify
    @Test
    public void testWorldModifyDeniedByWorldModify() {
        Region region = new CuboidRegion(RegionId.of("plot"), overworld, min, max, 0,
                RegionFlags.of(Map.of(BuiltInFlags.WORLD_MODIFY_KEY, FlagState.DENY)));
        RegionSet set = RegionSet.of(List.of(region));

        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.WORLD_MODIFY, causeChain, target, overworld, posInside);
        ProtectionDecision decision = resolver.resolve(query, set);

        assertTrue(decision.isDenied());
        assertEquals(BuiltInFlags.WORLD_MODIFY_KEY, decision.flagKey().orElse(null));
    }
}
