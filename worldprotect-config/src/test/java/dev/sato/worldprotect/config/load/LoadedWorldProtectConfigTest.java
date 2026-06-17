package dev.sato.worldprotect.config.load;

import dev.sato.worldprotect.protection.config.ConfigValidationMessage;
import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.region.RegionSet;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public final class LoadedWorldProtectConfigTest {

    @Test
    public void testSuccessfulCreation() {
        WorldProtectConfig rawConfig = WorldProtectConfig.of(List.of());
        RegionSet regionSet = RegionSet.of(List.of());
        ConfigValidationResult diagnostics = ConfigValidationResult.ok();

        LoadedWorldProtectConfig config = LoadedWorldProtectConfig.of(rawConfig, regionSet, diagnostics);
        assertEquals(rawConfig, config.rawConfig());
        assertEquals(regionSet, config.regionSet());
        assertEquals(diagnostics, config.diagnostics());
        assertFalse(config.hasWarnings());
    }

    @Test
    public void testCreationWithWarnings() {
        WorldProtectConfig rawConfig = WorldProtectConfig.of(List.of());
        RegionSet regionSet = RegionSet.of(List.of());
        ConfigValidationResult diagnostics = ConfigValidationResult.ok()
                .add(ConfigValidationMessage.warning("test", "A warning"));

        LoadedWorldProtectConfig config = LoadedWorldProtectConfig.of(rawConfig, regionSet, diagnostics);
        assertTrue(config.diagnostics().hasWarnings());
        assertFalse(config.diagnostics().hasErrors());
        assertTrue(config.hasWarnings());
    }

    @Test
    public void testCreationFailsWithErrors() {
        WorldProtectConfig rawConfig = WorldProtectConfig.of(List.of());
        RegionSet regionSet = RegionSet.of(List.of());
        ConfigValidationResult diagnostics = ConfigValidationResult.ok()
                .add(ConfigValidationMessage.error("test", "An error"));

        assertThrows(IllegalArgumentException.class, () ->
                LoadedWorldProtectConfig.of(rawConfig, regionSet, diagnostics)
        );
    }

    @Test
    public void testNullChecks() {
        WorldProtectConfig rawConfig = WorldProtectConfig.of(List.of());
        RegionSet regionSet = RegionSet.of(List.of());
        ConfigValidationResult diagnostics = ConfigValidationResult.ok();

        assertThrows(NullPointerException.class, () -> LoadedWorldProtectConfig.of(null, regionSet, diagnostics));
        assertThrows(NullPointerException.class, () -> LoadedWorldProtectConfig.of(rawConfig, null, diagnostics));
        assertThrows(NullPointerException.class, () -> LoadedWorldProtectConfig.of(rawConfig, regionSet, null));
    }
}
