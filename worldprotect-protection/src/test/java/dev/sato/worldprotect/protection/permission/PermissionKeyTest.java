package dev.sato.worldprotect.protection.permission;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class PermissionKeyTest {

    @Test
    public void testValidKeys() {
        PermissionKey key = PermissionKey.of("worldprotect.bypass.flag.build");
        assertEquals("worldprotect.bypass.flag.build", key.value());
    }

    @Test
    public void testInvalidKeys() {
        // Uppercase
        assertThrows(IllegalArgumentException.class, () -> PermissionKey.of("Worldprotect.bypass"));
        // Spaces
        assertThrows(IllegalArgumentException.class, () -> PermissionKey.of("worldprotect bypass"));
        // Empty segments
        assertThrows(IllegalArgumentException.class, () -> PermissionKey.of("worldprotect..bypass"));
        assertThrows(IllegalArgumentException.class, () -> PermissionKey.of(".worldprotect"));
        assertThrows(IllegalArgumentException.class, () -> PermissionKey.of("worldprotect."));
        // Invalid chars
        assertThrows(IllegalArgumentException.class, () -> PermissionKey.of("worldprotect.bypass#"));
        // Length boundary
        assertThrows(IllegalArgumentException.class, () -> PermissionKey.of("a".repeat(129)));
        assertThrows(IllegalArgumentException.class, () -> PermissionKey.of(""));
    }

    @Test
    public void testStartsWith() {
        PermissionKey p1 = PermissionKey.of("worldprotect.bypass.flag.build");
        PermissionKey prefix1 = PermissionKey.of("worldprotect.bypass");
        PermissionKey prefix2 = PermissionKey.of("worldprotect.bypass.flag.build");

        assertTrue(p1.startsWith(prefix1));
        assertTrue(p1.startsWith("worldprotect.bypass"));
        assertTrue(p1.startsWith(prefix2));

        // Non-segment boundary match
        assertFalse(p1.startsWith("worldprotect.byp"));
        assertFalse(p1.startsWith("worldprotect.bypass.flag.b"));
    }
}
