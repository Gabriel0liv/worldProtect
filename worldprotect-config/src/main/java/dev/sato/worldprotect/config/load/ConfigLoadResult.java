package dev.sato.worldprotect.config.load;

import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable container holding the outcome of a configuration loading attempt.
 */
public final class ConfigLoadResult {
    private final Optional<LoadedWorldProtectConfig> loadedConfig;
    private final ConfigValidationResult diagnostics;

    private ConfigLoadResult(
            Optional<LoadedWorldProtectConfig> loadedConfig,
            ConfigValidationResult diagnostics
    ) {
        this.loadedConfig = Objects.requireNonNull(loadedConfig, "loadedConfig must not be null");
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics must not be null");
    }

    public static ConfigLoadResult success(LoadedWorldProtectConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        return new ConfigLoadResult(Optional.of(config), config.diagnostics());
    }

    public static ConfigLoadResult failure(ConfigValidationResult diagnostics) {
        Objects.requireNonNull(diagnostics, "diagnostics must not be null");
        return new ConfigLoadResult(Optional.empty(), diagnostics);
    }

    public Optional<LoadedWorldProtectConfig> loadedConfig() {
        return loadedConfig;
    }

    public ConfigValidationResult diagnostics() {
        return diagnostics;
    }

    public boolean isSuccess() {
        return loadedConfig.isPresent();
    }

    public boolean hasErrors() {
        return diagnostics.hasErrors();
    }

    public boolean hasWarnings() {
        return diagnostics.hasWarnings();
    }
}
