package dev.sato.worldprotect.protection.region;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionIdTest {

    @Test
    public void testValidRegionIds() {
        assertDoesNotThrow(() -> new RegionId("region1"));
        assertDoesNotThrow(() -> new RegionId("my-spawn_zone-1"));
        assertDoesNotThrow(() -> new RegionId("region.1"));
        assertDoesNotThrow(() -> new RegionId("a"));
    }

    @Test
    public void testInvalidRegionIds() {
        // Uppercase not allowed
        assertThrows(IllegalArgumentException.class, () -> new RegionId("A"));
        assertThrows(IllegalArgumentException.class, () -> new RegionId("Spawn-zone"));
        // Special characters not allowed
        assertThrows(IllegalArgumentException.class, () -> new RegionId("region@123"));
        // Spaces not allowed
        assertThrows(IllegalArgumentException.class, () -> new RegionId("spawn zone"));
        // Empty string not allowed
        assertThrows(IllegalArgumentException.class, () -> new RegionId(""));
        // Length > 64 not allowed
        String longId = "a".repeat(65);
        assertThrows(IllegalArgumentException.class, () -> new RegionId(longId));
    }
}
