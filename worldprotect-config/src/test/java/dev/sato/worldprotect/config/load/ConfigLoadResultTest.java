package dev.sato.worldprotect.config.load;

import dev.sato.worldprotect.protection.config.ConfigValidationMessage;
import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.region.RegionSet;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public final class ConfigLoadResultTest {

    @Test
    public void testSuccessResult() {
        WorldProtectConfig rawConfig = WorldProtectConfig.of(List.of());
        RegionSet regionSet = RegionSet.of(List.of());
        ConfigValidationResult diagnostics = ConfigValidationResult.ok();
        LoadedWorldProtectConfig loaded = LoadedWorldProtectConfig.of(rawConfig, regionSet, diagnostics);

        ConfigLoadResult result = ConfigLoadResult.success(loaded);
        assertTrue(result.isSuccess());
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        assertEquals(loaded, result.loadedConfig().orElse(null));
        assertEquals(diagnostics, result.diagnostics());
    }

    @Test
    public void testFailureResult() {
        ConfigValidationResult diagnostics = ConfigValidationResult.ok()
                .add(ConfigValidationMessage.error("test", "Fatal error"));

        ConfigLoadResult result = ConfigLoadResult.failure(diagnostics);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertFalse(result.hasWarnings());
        assertFalse(result.loadedConfig().isPresent());
        assertEquals(diagnostics, result.diagnostics());
    }

    @Test
    public void testNullChecks() {
        assertThrows(NullPointerException.class, () -> ConfigLoadResult.success(null));
        assertThrows(NullPointerException.class, () -> ConfigLoadResult.failure(null));
    }
}
