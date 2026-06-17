package dev.sato.worldprotect.config.load;

import dev.sato.worldprotect.config.toml.TomlConfigParseResult;
import dev.sato.worldprotect.config.toml.TomlConfigParser;
import java.nio.file.Path;
import java.util.Objects;

/**
 * File-system representation of TOML configuration source.
 */
public final class FileTomlConfigSource implements ConfigSource {
    private final Path path;

    private FileTomlConfigSource(Path path) {
        this.path = Objects.requireNonNull(path, "path must not be null");
    }

    public static FileTomlConfigSource of(Path path) {
        return new FileTomlConfigSource(path);
    }

    @Override
    public String description() {
        return "file:" + path.toAbsolutePath().toString();
    }

    @Override
    public TomlConfigParseResult parse(TomlConfigParser parser) {
        Objects.requireNonNull(parser, "parser must not be null");
        return parser.parseFile(path);
    }

    public Path path() {
        return path;
    }
}
