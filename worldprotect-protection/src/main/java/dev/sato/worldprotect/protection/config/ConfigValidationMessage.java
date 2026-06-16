package dev.sato.worldprotect.protection.config;

import java.util.Objects;

/**
 * An immutable validation message containing severity, configuration path, and description.
 */
public final class ConfigValidationMessage {
    private final ConfigSeverity severity;
    private final String path;
    private final String message;

    public ConfigValidationMessage(ConfigSeverity severity, String path, String message) {
        this.severity = Objects.requireNonNull(severity, "severity must not be null");
        this.path = Objects.requireNonNull(path, "path must not be null");
        if (path.trim().isEmpty()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        this.message = Objects.requireNonNull(message, "message must not be null");
        if (message.trim().isEmpty()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }

    public static ConfigValidationMessage error(String path, String message) {
        return new ConfigValidationMessage(ConfigSeverity.ERROR, path, message);
    }

    public static ConfigValidationMessage warning(String path, String message) {
        return new ConfigValidationMessage(ConfigSeverity.WARNING, path, message);
    }

    public ConfigSeverity severity() {
        return severity;
    }

    public String path() {
        return path;
    }

    public String message() {
        return message;
    }

    public ConfigSeverity getSeverity() {
        return severity;
    }

    public String getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigValidationMessage that = (ConfigValidationMessage) o;
        return severity == that.severity &&
               path.equals(that.path) &&
               message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(severity, path, message);
    }

    @Override
    public String toString() {
        return "[" + severity + "] " + path + ": " + message;
    }
}
