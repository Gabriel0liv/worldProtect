package dev.sato.worldprotect.protection.resolver;

import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.query.ProtectionAction;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public final class BuildSemanticsTest {

    @Test
    public void testBlockBreakIsBuildRelated() {
        assertTrue(BuildSemantics.isBuildRelated(ProtectionAction.BLOCK_BREAK));
    }

    @Test
    public void testBlockPlaceIsBuildRelated() {
        assertTrue(BuildSemantics.isBuildRelated(ProtectionAction.BLOCK_PLACE));
    }

    @Test
    public void testBlockModifyIsBuildRelated() {
        assertTrue(BuildSemantics.isBuildRelated(ProtectionAction.BLOCK_MODIFY));
    }

    @Test
    public void testBuildIsBuildRelated() {
        assertTrue(BuildSemantics.isBuildRelated(ProtectionAction.BUILD));
    }

    @Test
    public void testBlockInteractIsBuildRelated() {
        assertTrue(BuildSemantics.isBuildRelated(ProtectionAction.BLOCK_INTERACT));
    }

    @Test
    public void testItemUseOnBlockIsBuildRelated() {
        assertTrue(BuildSemantics.isBuildRelated(ProtectionAction.ITEM_USE_ON_BLOCK));
    }

    @Test
    public void testPistonMoveIsBuildRelated() {
        assertTrue(BuildSemantics.isBuildRelated(ProtectionAction.PISTON_MOVE));
    }

    @Test
    public void testFluidSpreadIsBuildRelated() {
        assertTrue(BuildSemantics.isBuildRelated(ProtectionAction.FLUID_SPREAD));
    }

    @Test
    public void testExplosionBlockDamageIsBuildRelated() {
        assertTrue(BuildSemantics.isBuildRelated(ProtectionAction.EXPLOSION_BLOCK_DAMAGE));
    }

    @Test
    public void testItemUseNotBuildRelated() {
        assertFalse(BuildSemantics.isBuildRelated(ProtectionAction.ITEM_USE));
    }

    @Test
    public void testContainerOpenNotBuildRelated() {
        assertFalse(BuildSemantics.isBuildRelated(ProtectionAction.CONTAINER_OPEN));
    }

    @Test
    public void testEntityDamageNotBuildRelated() {
        assertFalse(BuildSemantics.isBuildRelated(ProtectionAction.ENTITY_DAMAGE));
    }

    @Test
    public void testSpecificFlagsForBlockBreak() {
        List<FlagKey> flags = BuildSemantics.specificFlagsFor(ProtectionAction.BLOCK_BREAK);
        assertEquals(List.of(BuiltInFlags.BREAK_BLOCK_KEY), flags);
    }

    @Test
    public void testSpecificFlagsForBlockPlace() {
        List<FlagKey> flags = BuildSemantics.specificFlagsFor(ProtectionAction.BLOCK_PLACE);
        assertEquals(List.of(BuiltInFlags.PLACE_BLOCK_KEY), flags);
    }

    @Test
    public void testSpecificFlagsForBlockModify() {
        List<FlagKey> flags = BuildSemantics.specificFlagsFor(ProtectionAction.BLOCK_MODIFY);
        assertEquals(List.of(BuiltInFlags.MODIFY_BLOCK_KEY), flags);
    }

    @Test
    public void testSpecificFlagsForBlockInteract() {
        List<FlagKey> flags = BuildSemantics.specificFlagsFor(ProtectionAction.BLOCK_INTERACT);
        assertEquals(List.of(BuiltInFlags.INTERACT_BLOCK_KEY), flags);
    }

    @Test
    public void testSpecificFlagsForItemUseOnBlock() {
        List<FlagKey> flags = BuildSemantics.specificFlagsFor(ProtectionAction.ITEM_USE_ON_BLOCK);
        assertEquals(List.of(BuiltInFlags.USE_ITEM_ON_BLOCK_KEY, BuiltInFlags.USE_ITEM_KEY), flags);
    }

    @Test
    public void testSpecificFlagsForPistonMove() {
        List<FlagKey> flags = BuildSemantics.specificFlagsFor(ProtectionAction.PISTON_MOVE);
        assertEquals(List.of(BuiltInFlags.PISTON_MOVE_KEY), flags);
    }

    @Test
    public void testSpecificFlagsForFluidSpread() {
        List<FlagKey> flags = BuildSemantics.specificFlagsFor(ProtectionAction.FLUID_SPREAD);
        assertEquals(List.of(BuiltInFlags.FLUID_SPREAD_KEY), flags);
    }

    @Test
    public void testSpecificFlagsForExplosionBlockDamage() {
        List<FlagKey> flags = BuildSemantics.specificFlagsFor(ProtectionAction.EXPLOSION_BLOCK_DAMAGE);
        assertEquals(List.of(BuiltInFlags.EXPLOSION_BREAK_BLOCKS_KEY), flags);
    }

    @Test
    public void testSpecificFlagsForBuildIsEmpty() {
        List<FlagKey> flags = BuildSemantics.specificFlagsFor(ProtectionAction.BUILD);
        assertTrue(flags.isEmpty());
    }

    @Test
    public void testSpecificFlagsForNonBuildThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> BuildSemantics.specificFlagsFor(ProtectionAction.CONTAINER_OPEN));
    }

    @Test
    public void testUsesBuildFallback() {
        assertTrue(BuildSemantics.usesBuildFallback(ProtectionAction.BLOCK_BREAK));
        assertTrue(BuildSemantics.usesBuildFallback(ProtectionAction.BLOCK_PLACE));
        assertTrue(BuildSemantics.usesBuildFallback(ProtectionAction.BLOCK_MODIFY));
        assertTrue(BuildSemantics.usesBuildFallback(ProtectionAction.BLOCK_INTERACT));
        assertTrue(BuildSemantics.usesBuildFallback(ProtectionAction.ITEM_USE_ON_BLOCK));
        assertTrue(BuildSemantics.usesBuildFallback(ProtectionAction.PISTON_MOVE));
        assertTrue(BuildSemantics.usesBuildFallback(ProtectionAction.FLUID_SPREAD));
        assertTrue(BuildSemantics.usesBuildFallback(ProtectionAction.EXPLOSION_BLOCK_DAMAGE));
        assertTrue(BuildSemantics.usesBuildFallback(ProtectionAction.BUILD));
    }

    @Test
    public void testUsesBuildFallbackForNonBuildThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> BuildSemantics.usesBuildFallback(ProtectionAction.CONTAINER_OPEN));
    }

    @Test
    public void testAllSpecificFlags() {
        List<FlagKey> all = BuildSemantics.allSpecificFlags();
        assertEquals(9, all.size());
        assertTrue(all.contains(BuiltInFlags.BREAK_BLOCK_KEY));
        assertTrue(all.contains(BuiltInFlags.PLACE_BLOCK_KEY));
        assertTrue(all.contains(BuiltInFlags.MODIFY_BLOCK_KEY));
        assertTrue(all.contains(BuiltInFlags.INTERACT_BLOCK_KEY));
        assertTrue(all.contains(BuiltInFlags.USE_ITEM_ON_BLOCK_KEY));
        assertTrue(all.contains(BuiltInFlags.USE_ITEM_KEY));
        assertTrue(all.contains(BuiltInFlags.PISTON_MOVE_KEY));
        assertTrue(all.contains(BuiltInFlags.FLUID_SPREAD_KEY));
        assertTrue(all.contains(BuiltInFlags.EXPLOSION_BREAK_BLOCKS_KEY));
    }

    @Test
    public void testNullActionThrows() {
        assertThrows(NullPointerException.class, () -> BuildSemantics.isBuildRelated(null));
        assertThrows(NullPointerException.class, () -> BuildSemantics.specificFlagsFor(null));
        assertThrows(NullPointerException.class, () -> BuildSemantics.usesBuildFallback(null));
    }
}
