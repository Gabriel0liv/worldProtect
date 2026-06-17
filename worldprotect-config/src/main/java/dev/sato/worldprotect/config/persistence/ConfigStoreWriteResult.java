package dev.sato.worldprotect.config.persistence;

import java.util.Objects;

public final class ConfigStoreWriteResult {
    private final boolean success;
    private final String message;

    private ConfigStoreWriteResult(boolean success, String message) {
        this.success = success;
        this.message = Objects.requireNonNull(message, "message must not be null");
    }

    public static ConfigStoreWriteResult success(String message) {
        return new ConfigStoreWriteResult(true, message);
    }

    public static ConfigStoreWriteResult failure(String message) {
        return new ConfigStoreWriteResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String message() {
        return message;
    }

    public String getMessage() {
        return message;
    }
}
