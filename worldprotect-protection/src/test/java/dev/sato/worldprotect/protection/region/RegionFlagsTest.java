package dev.sato.worldprotect.protection.region;

import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagState;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionFlagsTest {

    @Test
    public void testEmptyFlags() {
        RegionFlags empty = RegionFlags.empty();
        assertTrue(empty.asMap().isEmpty());
        assertFalse(empty.contains(FlagKey.of("build")));
        assertTrue(empty.get(FlagKey.of("build")).isEmpty());
    }

    @Test
    public void testDefensiveCopy() {
        Map<FlagKey, FlagState> map = new HashMap<>();
        FlagKey key = FlagKey.of("build");
        map.put(key, FlagState.ALLOW);

        RegionFlags flags = RegionFlags.of(map);
        assertTrue(flags.contains(key));
        assertEquals(FlagState.ALLOW, flags.get(key).orElse(null));

        // Mutating source map should not affect RegionFlags
        map.put(key, FlagState.DENY);
        assertEquals(FlagState.ALLOW, flags.get(key).orElse(null));
    }

    @Test
    public void testAsMapIsImmutable() {
        Map<FlagKey, FlagState> map = Map.of(FlagKey.of("build"), FlagState.ALLOW);
        RegionFlags flags = RegionFlags.of(map);

        assertThrows(UnsupportedOperationException.class, () -> flags.asMap().put(FlagKey.of("break-block"), FlagState.DENY));
    }
}
