package dev.sato.worldprotect.config.toml;

import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable result of canonical TOML serialization.
 */
public final class TomlConfigWriteResult {
    private final Optional<String> content;
    private final ConfigValidationResult diagnostics;

    private TomlConfigWriteResult(Optional<String> content, ConfigValidationResult diagnostics) {
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics must not be null");
    }

    public static TomlConfigWriteResult success(String content, ConfigValidationResult diagnostics) {
        return new TomlConfigWriteResult(Optional.of(Objects.requireNonNull(content, "content must not be null")), diagnostics);
    }

    public static TomlConfigWriteResult failure(ConfigValidationResult diagnostics) {
        return new TomlConfigWriteResult(Optional.empty(), diagnostics);
    }

    public boolean isSuccess() {
        return content.isPresent() && !diagnostics.hasErrors();
    }

    public Optional<String> content() {
        return content;
    }

    public ConfigValidationResult diagnostics() {
        return diagnostics;
    }

    public Optional<String> getContent() {
        return content;
    }

    public ConfigValidationResult getDiagnostics() {
        return diagnostics;
    }
}
