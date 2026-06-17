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
    public void testBlockInteractNotBuildRelated() {
        assertFalse(BuildSemantics.isBuildRelated(ProtectionAction.BLOCK_INTERACT));
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
    public void testFluidSpreadNotBuildRelated() {
        assertFalse(BuildSemantics.isBuildRelated(ProtectionAction.FLUID_SPREAD));
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
    public void testSpecificFlagsForBuildIsEmpty() {
        List<FlagKey> flags = BuildSemantics.specificFlagsFor(ProtectionAction.BUILD);
        assertTrue(flags.isEmpty());
    }

    @Test
    public void testSpecificFlagsForNonBuildThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> BuildSemantics.specificFlagsFor(ProtectionAction.BLOCK_INTERACT));
    }

    @Test
    public void testUsesBuildFallback() {
        assertTrue(BuildSemantics.usesBuildFallback(ProtectionAction.BLOCK_BREAK));
        assertTrue(BuildSemantics.usesBuildFallback(ProtectionAction.BLOCK_PLACE));
        assertTrue(BuildSemantics.usesBuildFallback(ProtectionAction.BLOCK_MODIFY));
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
        assertEquals(3, all.size());
        assertTrue(all.contains(BuiltInFlags.BREAK_BLOCK_KEY));
        assertTrue(all.contains(BuiltInFlags.PLACE_BLOCK_KEY));
        assertTrue(all.contains(BuiltInFlags.MODIFY_BLOCK_KEY));
    }

    @Test
    public void testNullActionThrows() {
        assertThrows(NullPointerException.class, () -> BuildSemantics.isBuildRelated(null));
        assertThrows(NullPointerException.class, () -> BuildSemantics.specificFlagsFor(null));
        assertThrows(NullPointerException.class, () -> BuildSemantics.usesBuildFallback(null));
    }
}
