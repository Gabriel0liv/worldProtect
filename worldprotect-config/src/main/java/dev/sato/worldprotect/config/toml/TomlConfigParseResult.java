package dev.sato.worldprotect.config.toml;

import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable result of a TOML configuration parsing execution.
 */
public final class TomlConfigParseResult {
    private final WorldProtectConfig config;
    private final ConfigValidationResult diagnostics;

    private TomlConfigParseResult(WorldProtectConfig config, ConfigValidationResult diagnostics) {
        this.config = config;
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics must not be null");
    }

    public static TomlConfigParseResult success(WorldProtectConfig config, ConfigValidationResult diagnostics) {
        Objects.requireNonNull(config, "config must not be null");
        return new TomlConfigParseResult(config, diagnostics);
    }

    public static TomlConfigParseResult failure(ConfigValidationResult diagnostics) {
        return new TomlConfigParseResult(null, diagnostics);
    }

    public Optional<WorldProtectConfig> config() {
        return Optional.ofNullable(config);
    }

    public ConfigValidationResult diagnostics() {
        return diagnostics;
    }

    public boolean hasConfig() {
        return config != null;
    }

    public boolean hasErrors() {
        return diagnostics.hasErrors();
    }

    public boolean isSuccess() {
        return config != null && !diagnostics.hasErrors();
    }
}
