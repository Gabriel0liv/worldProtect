package dev.sato.worldprotect.protection.rule;

import dev.sato.worldprotect.minecraft.ResourceRef;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class ResourceSelectorTest {

    @Test
    public void testValidParses() {
        // Global wildcard
        ResourceSelector global = ResourceSelector.parse("*");
        assertEquals(ResourceSelectorKind.GLOBAL_WILDCARD, global.kind());

        // Namespace wildcard
        ResourceSelector createWild = ResourceSelector.parse("create:*");
        assertEquals(ResourceSelectorKind.NAMESPACE_WILDCARD, createWild.kind());
        assertEquals("create", createWild.namespace().orElse(null));

        // Tag selector
        ResourceSelector tag = ResourceSelector.parse("#forge:chests");
        assertEquals(ResourceSelectorKind.TAG, tag.kind());
        assertEquals(ResourceRef.of("forge", "chests"), tag.id().orElse(null));

        // Another Tag selector
        ResourceSelector tagAxes = ResourceSelector.parse("#c:tools/axes");
        assertEquals(ResourceSelectorKind.TAG, tagAxes.kind());
        assertEquals(ResourceRef.of("c", "tools/axes"), tagAxes.id().orElse(null));

        // Exact selector
        ResourceSelector exact = ResourceSelector.parse("minecraft:stone");
        assertEquals(ResourceSelectorKind.EXACT, exact.kind());
        assertEquals(ResourceRef.of("minecraft", "stone"), exact.id().orElse(null));

        // Exact selector defaulting namespace
        ResourceSelector exactDefault = ResourceSelector.parse("stone");
        assertEquals(ResourceSelectorKind.EXACT, exactDefault.kind());
        assertEquals(ResourceRef.of("minecraft", "stone"), exactDefault.id().orElse(null));
    }

    @Test
    public void testInvalidParses() {
        assertThrows(IllegalArgumentException.class, () -> ResourceSelector.parse(""));
        assertThrows(IllegalArgumentException.class, () -> ResourceSelector.parse("#"));
        assertThrows(IllegalArgumentException.class, () -> ResourceSelector.parse("#:"));
        assertThrows(IllegalArgumentException.class, () -> ResourceSelector.parse(":*"));
        assertThrows(IllegalArgumentException.class, () -> ResourceSelector.parse("Minecraft:*"));
        assertThrows(IllegalArgumentException.class, () -> ResourceSelector.parse("create: *"));
        assertThrows(IllegalArgumentException.class, () -> ResourceSelector.parse("create:*:extra"));
        assertThrows(IllegalArgumentException.class, () -> ResourceSelector.parse("bad namespace:*"));
        assertThrows(IllegalArgumentException.class, () -> ResourceSelector.parse("minecraft:Stone"));
        assertThrows(IllegalArgumentException.class, () -> ResourceSelector.parse("minecraft:bad path"));
    }

    @Test
    public void testSelectorMatching() {
        ResourceRef stone = ResourceRef.of("minecraft", "stone");
        ResourceRef dirt = ResourceRef.of("minecraft", "dirt");
        ResourceRef press = ResourceRef.of("create", "mechanical_press");

        // Exact
        ResourceSelector exactStone = ResourceSelector.parse("minecraft:stone");
        assertTrue(exactStone.matches(stone));
        assertFalse(exactStone.matches(dirt));
        assertFalse(exactStone.matches(press));

        // Namespace wildcard
        ResourceSelector createWild = ResourceSelector.parse("create:*");
        assertTrue(createWild.matches(press));
        assertFalse(createWild.matches(stone));

        // Global wildcard
        ResourceSelector global = ResourceSelector.parse("*");
        assertTrue(global.matches(stone));
        assertTrue(global.matches(press));

        // Tag (always returns false for raw matching until TagRegistryView is implemented)
        ResourceSelector tag = ResourceSelector.parse("#forge:chests");
        assertFalse(tag.matches(stone));
        assertFalse(tag.matches(press));
    }
}
