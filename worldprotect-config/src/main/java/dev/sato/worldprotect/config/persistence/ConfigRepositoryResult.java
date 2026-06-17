package dev.sato.worldprotect.config.persistence;

import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import dev.sato.worldprotect.protection.management.RegionMutationPlan;
import java.util.Objects;
import java.util.Optional;

public final class ConfigRepositoryResult<T> {
    private final ConfigRepositoryStatus status;
    private final T value;
    private final ConfigValidationResult diagnostics;
    private final String message;
    private final Optional<RegionMutationPlan> mutationPlan;
    private final Optional<String> serializedContent;

    private ConfigRepositoryResult(
            ConfigRepositoryStatus status,
            T value,
            ConfigValidationResult diagnostics,
            String message,
            Optional<RegionMutationPlan> mutationPlan,
            Optional<String> serializedContent
    ) {
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.value = value;
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
        this.mutationPlan = Objects.requireNonNull(mutationPlan, "mutationPlan must not be null");
        this.serializedContent = Objects.requireNonNull(serializedContent, "serializedContent must not be null");
    }

    public static <T> ConfigRepositoryResult<T> success(T value, ConfigValidationResult diagnostics, String message, Optional<String> serializedContent) {
        return new ConfigRepositoryResult<>(ConfigRepositoryStatus.SUCCESS, value, diagnostics, message, Optional.empty(), serializedContent);
    }

    public static <T> ConfigRepositoryResult<T> success(
            T value,
            ConfigValidationResult diagnostics,
            String message,
            Optional<RegionMutationPlan> mutationPlan,
            Optional<String> serializedContent
    ) {
        return new ConfigRepositoryResult<>(ConfigRepositoryStatus.SUCCESS, value, diagnostics, message, mutationPlan, serializedContent);
    }

    public static <T> ConfigRepositoryResult<T> noChange(T value, ConfigValidationResult diagnostics, String message) {
        return new ConfigRepositoryResult<>(ConfigRepositoryStatus.NO_CHANGE, value, diagnostics, message, Optional.empty(), Optional.empty());
    }

    public static <T> ConfigRepositoryResult<T> failure(ConfigRepositoryStatus status, ConfigValidationResult diagnostics, String message) {
        return new ConfigRepositoryResult<>(status, null, diagnostics, message, Optional.empty(), Optional.empty());
    }

    public boolean isSuccess() {
        return status == ConfigRepositoryStatus.SUCCESS || status == ConfigRepositoryStatus.NO_CHANGE;
    }

    public boolean isFailure() {
        return !isSuccess();
    }

    public ConfigRepositoryStatus status() {
        return status;
    }

    public T value() {
        return value;
    }

    public ConfigValidationResult diagnostics() {
        return diagnostics;
    }

    public String message() {
        return message;
    }

    public Optional<RegionMutationPlan> mutationPlan() {
        return mutationPlan;
    }

    public Optional<String> serializedContent() {
        return serializedContent;
    }

    public ConfigRepositoryStatus getStatus() {
        return status;
    }

    public T getValue() {
        return value;
    }

    public ConfigValidationResult getDiagnostics() {
        return diagnostics;
    }

    public String getMessage() {
        return message;
    }

    public Optional<RegionMutationPlan> getMutationPlan() {
        return mutationPlan;
    }

    public Optional<String> getSerializedContent() {
        return serializedContent;
    }
}
