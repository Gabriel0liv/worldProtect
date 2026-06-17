package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.protection.subject.SubjectRef;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable configuration representation of a subject reference before domain mapping.
 */
public final class SubjectRefConfig {
    private final String rawValue;

    private SubjectRefConfig(String rawValue) {
        this.rawValue = Objects.requireNonNull(rawValue, "rawValue must not be null");
    }

    public static SubjectRefConfig of(String rawValue) {
        return new SubjectRefConfig(rawValue);
    }

    public String rawValue() {
        return rawValue;
    }

    public String asString() {
        return rawValue;
    }

    public ConfigValidationResult validate(String path) {
        Objects.requireNonNull(path, "path must not be null");
        ConfigValidationResult result = ConfigValidationResult.ok();

        if (rawValue.trim().isEmpty()) {
            return result.add(ConfigValidationMessage.error(path, "Subject reference must not be empty or blank"));
        }

        if (rawValue.equalsIgnoreCase("console")) {
            if (!rawValue.equals("console")) {
                return result.add(ConfigValidationMessage.error(path, "Console subject must be exactly 'console' (lowercase)"));
            }
            return result;
        }
        if (rawValue.equalsIgnoreCase("system")) {
            if (!rawValue.equals("system")) {
                return result.add(ConfigValidationMessage.error(path, "System subject must be exactly 'system' (lowercase)"));
            }
            return result;
        }

        int colonIndex = rawValue.indexOf(':');
        if (colonIndex == -1) {
            return result.add(ConfigValidationMessage.error(path, "Invalid subject format: " + rawValue + ". Missing prefix. Expected 'player:<uuid>', 'group:<name>', 'console', or 'system'"));
        }

        String prefix = rawValue.substring(0, colonIndex);
        String value = rawValue.substring(colonIndex + 1);

        if (prefix.equals("console") || prefix.equals("system")) {
            return result.add(ConfigValidationMessage.error(path, prefix + " subject must not contain parameters"));
        }

        if (prefix.equalsIgnoreCase("console") || prefix.equalsIgnoreCase("system")) {
            return result.add(ConfigValidationMessage.error(path, "Unknown or invalid prefix: " + prefix));
        }

        if (prefix.equals("player")) {
            if (value.trim().isEmpty()) {
                return result.add(ConfigValidationMessage.error(path, "Player UUID must not be empty"));
            }
            try {
                UUID.fromString(value);
            } catch (IllegalArgumentException e) {
                return result.add(ConfigValidationMessage.error(path, "Invalid UUID format for player subject: " + value));
            }
        } else if (prefix.equals("group")) {
            if (value.trim().isEmpty()) {
                return result.add(ConfigValidationMessage.error(path, "Group ID must not be empty"));
            }
            try {
                SubjectRef.group(value);
            } catch (IllegalArgumentException e) {
                return result.add(ConfigValidationMessage.error(path, "Invalid group ID: " + e.getMessage()));
            }
        } else {
            return result.add(ConfigValidationMessage.error(path, "Unknown or invalid prefix: " + prefix));
        }

        return result;
    }

    public SubjectRef toDomain() {
        if (rawValue.equals("console")) {
            return SubjectRef.console();
        }
        if (rawValue.equals("system")) {
            return SubjectRef.system();
        }
        int colonIndex = rawValue.indexOf(':');
        if (colonIndex == -1) {
            throw new IllegalStateException("SubjectRefConfig has not been validated: " + rawValue);
        }
        String prefix = rawValue.substring(0, colonIndex);
        String value = rawValue.substring(colonIndex + 1);
        if (prefix.equals("player")) {
            return SubjectRef.player(UUID.fromString(value));
        } else if (prefix.equals("group")) {
            return SubjectRef.group(value);
        } else {
            throw new IllegalStateException("Unknown prefix in SubjectRefConfig: " + prefix);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubjectRefConfig that = (SubjectRefConfig) o;
        return rawValue.equals(that.rawValue);
    }

    @Override
    public int hashCode() {
        return rawValue.hashCode();
    }

    @Override
    public String toString() {
        return rawValue;
    }
}
