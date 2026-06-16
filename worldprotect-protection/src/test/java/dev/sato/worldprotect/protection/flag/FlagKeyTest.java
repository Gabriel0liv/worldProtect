package dev.sato.worldprotect.protection.flag;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class FlagKeyTest {

    @Test
    public void testValidFlagKeys() {
        assertDoesNotThrow(() -> new FlagKey("build"));
        assertDoesNotThrow(() -> new FlagKey("chest-access"));
        
        // Capitalization should be normalized to lowercase
        FlagKey uppercase = new FlagKey("BUILD");
        assertEquals("build", uppercase.getName());
    }

    @Test
    public void testInvalidFlagKeys() {
        // Spaces not allowed
        assertThrows(IllegalArgumentException.class, () -> new FlagKey("chest access"));
        // Underscores not allowed (only dashes)
        assertThrows(IllegalArgumentException.class, () -> new FlagKey("chest_access"));
        // Empty not allowed
        assertThrows(IllegalArgumentException.class, () -> new FlagKey(""));
        // Length > 64 not allowed
        String longFlagName = "a".repeat(65);
        assertThrows(IllegalArgumentException.class, () -> new FlagKey(longFlagName));
    }
}
