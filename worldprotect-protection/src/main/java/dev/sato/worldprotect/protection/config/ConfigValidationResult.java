package dev.sato.worldprotect.protection.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Immutable container for configuration validation results.
 */
public final class ConfigValidationResult {
    private static final ConfigValidationResult OK = new ConfigValidationResult(List.of());

    private final List<ConfigValidationMessage> messages;

    private ConfigValidationResult(List<ConfigValidationMessage> messages) {
        this.messages = List.copyOf(messages);
    }

    public static ConfigValidationResult ok() {
        return OK;
    }

    public static ConfigValidationResult of(List<ConfigValidationMessage> messages) {
        Objects.requireNonNull(messages, "messages must not be null");
        for (ConfigValidationMessage msg : messages) {
            Objects.requireNonNull(msg, "message element must not be null");
        }
        return new ConfigValidationResult(messages);
    }

    public List<ConfigValidationMessage> messages() {
        return messages;
    }

    public List<ConfigValidationMessage> getMessages() {
        return messages;
    }

    public List<ConfigValidationMessage> errors() {
        return messages.stream()
                .filter(m -> m.severity() == ConfigSeverity.ERROR)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<ConfigValidationMessage> warnings() {
        return messages.stream()
                .filter(m -> m.severity() == ConfigSeverity.WARNING)
                .collect(Collectors.toUnmodifiableList());
    }

    public boolean hasErrors() {
        return messages.stream().anyMatch(m -> m.severity() == ConfigSeverity.ERROR);
    }

    public boolean hasWarnings() {
        return messages.stream().anyMatch(m -> m.severity() == ConfigSeverity.WARNING);
    }

    public boolean isValid() {
        return !hasErrors();
    }

    public ConfigValidationResult merge(ConfigValidationResult other) {
        Objects.requireNonNull(other, "other must not be null");
        if (this.messages.isEmpty()) {
            return other;
        }
        if (other.messages.isEmpty()) {
            return this;
        }
        List<ConfigValidationMessage> merged = new ArrayList<>(this.messages);
        merged.addAll(other.messages);
        return new ConfigValidationResult(merged);
    }

    public ConfigValidationResult add(ConfigValidationMessage message) {
        Objects.requireNonNull(message, "message must not be null");
        List<ConfigValidationMessage> updated = new ArrayList<>(this.messages);
        updated.add(message);
        return new ConfigValidationResult(updated);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigValidationResult that = (ConfigValidationResult) o;
        return messages.equals(that.messages);
    }

    @Override
    public int hashCode() {
        return messages.hashCode();
    }

    @Override
    public String toString() {
        return "ConfigValidationResult{isValid=" + isValid() + ", messages=" + messages + "}";
    }
}
