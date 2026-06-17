package dev.sato.worldprotect.protection.permission;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public final class PermissionSetTest {

    @Test
    public void testEmptyPermissionSet() {
        PermissionSet set = PermissionSet.empty();
        assertTrue(set.permissions().isEmpty());
    }

    @Test
    public void testOfStrings() {
        PermissionSet set = PermissionSet.ofStrings(List.of("worldprotect.bypass", "worldprotect.admin"));
        assertEquals(2, set.permissions().size());
        assertTrue(set.has("worldprotect.bypass"));
        assertTrue(set.has(PermissionKey.of("worldprotect.admin")));
        assertFalse(set.has("worldprotect.other"));
    }

    @Test
    public void testHasAny() {
        PermissionSet set = PermissionSet.ofStrings(List.of("worldprotect.bypass"));
        assertTrue(set.hasAny(List.of(PermissionKey.of("worldprotect.bypass"), PermissionKey.of("worldprotect.admin"))));
        assertFalse(set.hasAny(List.of(PermissionKey.of("worldprotect.admin"))));
    }

    @Test
    public void testWithAndWithout() {
        PermissionKey bypass = PermissionKey.of("worldprotect.bypass");
        PermissionKey admin = PermissionKey.of("worldprotect.admin");

        PermissionSet set = PermissionSet.empty()
                .with(bypass)
                .with(admin);

        assertTrue(set.has(bypass));
        assertTrue(set.has(admin));

        PermissionSet modified = set.without(bypass);
        assertFalse(modified.has(bypass));
        assertTrue(modified.has(admin));

        // Original is unchanged
        assertTrue(set.has(bypass));
    }
}
