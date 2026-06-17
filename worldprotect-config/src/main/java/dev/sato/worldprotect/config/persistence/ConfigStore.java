package dev.sato.worldprotect.config.persistence;

/**
 * Abstract read/write storage for canonical TOML config text.
 */
public interface ConfigStore {
    ConfigStoreReadResult read();
    ConfigStoreWriteResult write(String content);
    String description();
}
