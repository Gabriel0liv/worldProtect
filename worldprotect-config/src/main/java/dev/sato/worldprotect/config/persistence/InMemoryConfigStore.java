package dev.sato.worldprotect.config.persistence;

import java.util.Objects;

/**
 * In-memory config store for tests and pure Java flows.
 */
public final class InMemoryConfigStore implements ConfigStore {
    private final String description;
    private String content;

    private InMemoryConfigStore(String description, String content) {
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.content = Objects.requireNonNull(content, "content must not be null");
    }

    public static InMemoryConfigStore of(String description, String content) {
        return new InMemoryConfigStore(description, content);
    }

    public static InMemoryConfigStore ofToml(String content) {
        return new InMemoryConfigStore("in-memory TOML", content);
    }

    @Override
    public ConfigStoreReadResult read() {
        return ConfigStoreReadResult.success(content, "Read config from memory");
    }

    @Override
    public ConfigStoreWriteResult write(String content) {
        this.content = Objects.requireNonNull(content, "content must not be null");
        return ConfigStoreWriteResult.success("Wrote config to memory");
    }

    @Override
    public String description() {
        return description;
    }
}
