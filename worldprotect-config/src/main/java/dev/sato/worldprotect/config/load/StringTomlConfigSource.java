package dev.sato.worldprotect.config.load;

import dev.sato.worldprotect.config.toml.TomlConfigParseResult;
import dev.sato.worldprotect.config.toml.TomlConfigParser;
import java.util.Objects;

/**
 * In-memory string representation of TOML configuration.
 */
public final class StringTomlConfigSource implements ConfigSource {
    private final String description;
    private final String toml;

    private StringTomlConfigSource(String description, String toml) {
        this.description = Objects.requireNonNull(description, "description must not be null");
        if (description.trim().isEmpty()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        this.toml = Objects.requireNonNull(toml, "toml must not be null");
    }

    public static StringTomlConfigSource of(String description, String toml) {
        return new StringTomlConfigSource(description, toml);
    }

    public static StringTomlConfigSource ofToml(String toml) {
        return new StringTomlConfigSource("inline TOML", toml);
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public TomlConfigParseResult parse(TomlConfigParser parser) {
        Objects.requireNonNull(parser, "parser must not be null");
        return parser.parseString(toml);
    }

    public String toml() {
        return toml;
    }
}
