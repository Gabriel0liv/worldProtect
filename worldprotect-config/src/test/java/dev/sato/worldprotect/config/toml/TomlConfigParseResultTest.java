package dev.sato.worldprotect.config.toml;

import dev.sato.worldprotect.protection.config.ConfigValidationMessage;
import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public final class TomlConfigParseResultTest {

    @Test
    public void testSuccessOutcome() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of());
        ConfigValidationResult diagnostics = ConfigValidationResult.ok();
        
        TomlConfigParseResult result = TomlConfigParseResult.success(config, diagnostics);
        
        assertTrue(result.isSuccess());
        assertTrue(result.hasConfig());
        assertEquals(config, result.config().orElse(null));
        assertFalse(result.hasErrors());
        assertEquals(diagnostics, result.diagnostics());
    }

    @Test
    public void testFailureOutcome() {
        ConfigValidationResult diagnostics = ConfigValidationResult.of(List.of(
                ConfigValidationMessage.error("toml", "Syntax error")
        ));
        
        TomlConfigParseResult result = TomlConfigParseResult.failure(diagnostics);
        
        assertFalse(result.isSuccess());
        assertFalse(result.hasConfig());
        assertFalse(result.config().isPresent());
        assertTrue(result.hasErrors());
        assertEquals(diagnostics, result.diagnostics());
    }

    @Test
    public void testDiagnosticsImmutability() {
        ConfigValidationResult diagnostics = ConfigValidationResult.ok();
        TomlConfigParseResult result = TomlConfigParseResult.failure(diagnostics);
        
        // Ensure returning diagnostic list is read-only
        assertThrows(UnsupportedOperationException.class, () -> {
            result.diagnostics().messages().clear();
        });
    }
}
