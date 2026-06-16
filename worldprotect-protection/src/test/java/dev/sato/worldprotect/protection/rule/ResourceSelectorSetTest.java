package dev.sato.worldprotect.protection.rule;

import dev.sato.worldprotect.minecraft.ResourceRef;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public final class ResourceSelectorSetTest {

    @Test
    public void testEmptySetMatchesNothing() {
        ResourceSelectorSet set = ResourceSelectorSet.empty();
        assertTrue(set.isEmpty());
        assertFalse(set.matches(ResourceRef.of("minecraft", "stone")));
    }

    @Test
    public void testAnyMatchingSelectorReturnsTrue() {
        ResourceSelector exact = ResourceSelector.parse("minecraft:stone");
        ResourceSelector ns = ResourceSelector.parse("create:*");
        ResourceSelectorSet set = ResourceSelectorSet.of(List.of(exact, ns));

        assertFalse(set.isEmpty());
        assertTrue(set.matches(ResourceRef.of("minecraft", "stone")));
        assertTrue(set.matches(ResourceRef.of("create", "wrench")));
        assertFalse(set.matches(ResourceRef.of("minecraft", "dirt")));
    }

    @Test
    public void testImmutableSelectorList() {
        ResourceSelector selector = ResourceSelector.parse("*");
        ResourceSelectorSet set = ResourceSelectorSet.of(List.of(selector));

        assertThrows(UnsupportedOperationException.class, () -> set.selectors().add(ResourceSelector.parse("stone")));
    }

    @Test
    public void testRejectsNullElements() {
        List<ResourceSelector> list = new ArrayList<>();
        list.add(null);
        assertThrows(NullPointerException.class, () -> ResourceSelectorSet.of(list));
    }
}
