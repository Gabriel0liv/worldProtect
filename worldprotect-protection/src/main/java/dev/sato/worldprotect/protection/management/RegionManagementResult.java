package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import java.util.Objects;
import java.util.Optional;

/**
 * Generic result wrapper for immutable region management operations.
 */
public final class RegionManagementResult<T> {
    private final RegionManagementStatus status;
    private final T value;
    private final ConfigValidationResult diagnostics;
    private final String message;
    private final Optional<RegionMutationPlan> mutationPlan;

    private RegionManagementResult(
            RegionManagementStatus status,
            T value,
            ConfigValidationResult diagnostics,
            String message,
            Optional<RegionMutationPlan> mutationPlan
    ) {
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
        this.mutationPlan = Objects.requireNonNull(mutationPlan, "mutationPlan must not be null");
        this.value = value;
    }

    public static <T> RegionManagementResult<T> success(T value, ConfigValidationResult diagnostics, String message) {
        return new RegionManagementResult<>(RegionManagementStatus.SUCCESS, value, diagnostics, message, Optional.empty());
    }

    public static <T> RegionManagementResult<T> success(
            T value,
            ConfigValidationResult diagnostics,
            String message,
            RegionMutationPlan mutationPlan
    ) {
        return new RegionManagementResult<>(
                RegionManagementStatus.SUCCESS,
                value,
                diagnostics,
                message,
                Optional.of(Objects.requireNonNull(mutationPlan, "mutationPlan must not be null"))
        );
    }

    public static <T> RegionManagementResult<T> noChange(T value, String message) {
        return new RegionManagementResult<>(RegionManagementStatus.NO_CHANGE, value, ConfigValidationResult.ok(), message, Optional.empty());
    }

    public static <T> RegionManagementResult<T> failure(
            RegionManagementStatus status,
            ConfigValidationResult diagnostics,
            String message
    ) {
        if (status == RegionManagementStatus.SUCCESS) {
            throw new IllegalArgumentException("failure result must not use SUCCESS status");
        }
        return new RegionManagementResult<>(status, null, diagnostics, message, Optional.empty());
    }

    public boolean isSuccess() {
        return status == RegionManagementStatus.SUCCESS || status == RegionManagementStatus.NO_CHANGE;
    }

    public boolean isFailure() {
        return !isSuccess();
    }

    public RegionManagementStatus status() {
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

    public RegionManagementStatus getStatus() {
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
}
