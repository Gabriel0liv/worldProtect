package dev.sato.worldprotect.config.load;

import dev.sato.worldprotect.config.toml.TomlConfigParseResult;
import dev.sato.worldprotect.config.toml.TomlConfigParser;

/**
 * Interface representing a source of TOML configuration content.
 */
public interface ConfigSource {

    /**
     * Gets a human-readable description of this source (e.g. file path, inline ID).
     */
    String description();

    /**
     * Parses the TOML content from this source using the provided parser.
     */
    TomlConfigParseResult parse(TomlConfigParser parser);
}
