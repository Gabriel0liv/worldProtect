package dev.sato.worldprotect.config.persistence;

import java.util.Objects;
import java.util.Optional;

public final class ConfigStoreReadResult {
    private final Optional<String> content;
    private final String message;

    private ConfigStoreReadResult(Optional<String> content, String message) {
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.message = Objects.requireNonNull(message, "message must not be null");
    }

    public static ConfigStoreReadResult success(String content, String message) {
        return new ConfigStoreReadResult(Optional.of(Objects.requireNonNull(content, "content must not be null")), message);
    }

    public static ConfigStoreReadResult failure(String message) {
        return new ConfigStoreReadResult(Optional.empty(), message);
    }

    public boolean isSuccess() {
        return content.isPresent();
    }

    public Optional<String> content() {
        return content;
    }

    public String message() {
        return message;
    }

    public Optional<String> getContent() {
        return content;
    }

    public String getMessage() {
        return message;
    }
}
