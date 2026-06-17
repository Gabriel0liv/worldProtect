package dev.sato.worldprotect.config.load;

import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.region.RegionSet;
import java.util.Objects;

/**
 * Immutable container representing successfully parsed and mapped configurations.
 */
public final class LoadedWorldProtectConfig {
    private final WorldProtectConfig rawConfig;
    private final RegionSet regionSet;
    private final ConfigValidationResult diagnostics;

    private LoadedWorldProtectConfig(
            WorldProtectConfig rawConfig,
            RegionSet regionSet,
            ConfigValidationResult diagnostics
    ) {
        this.rawConfig = Objects.requireNonNull(rawConfig, "rawConfig must not be null");
        this.regionSet = Objects.requireNonNull(regionSet, "regionSet must not be null");
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics must not be null");
        if (diagnostics.hasErrors()) {
            throw new IllegalArgumentException("Diagnostics contain errors: " + diagnostics.errors());
        }
    }

    public static LoadedWorldProtectConfig of(
            WorldProtectConfig rawConfig,
            RegionSet regionSet,
            ConfigValidationResult diagnostics
    ) {
        return new LoadedWorldProtectConfig(rawConfig, regionSet, diagnostics);
    }

    public WorldProtectConfig rawConfig() {
        return rawConfig;
    }

    public RegionSet regionSet() {
        return regionSet;
    }

    public ConfigValidationResult diagnostics() {
        return diagnostics;
    }
}
