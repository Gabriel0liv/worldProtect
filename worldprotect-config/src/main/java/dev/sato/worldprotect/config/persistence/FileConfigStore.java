package dev.sato.worldprotect.config.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Filesystem-backed config store using safe temp-file replacement writes.
 */
public final class FileConfigStore implements ConfigStore {
    private final Path path;

    private FileConfigStore(Path path) {
        this.path = Objects.requireNonNull(path, "path must not be null");
    }

    public static FileConfigStore of(Path path) {
        return new FileConfigStore(path);
    }

    @Override
    public ConfigStoreReadResult read() {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return ConfigStoreReadResult.success(content, "Read config from " + path.toAbsolutePath());
        } catch (IOException e) {
            return ConfigStoreReadResult.failure("Failed to read config from " + path.toAbsolutePath() + ": " + e.getMessage());
        }
    }

    @Override
    public ConfigStoreWriteResult write(String content) {
        Objects.requireNonNull(content, "content must not be null");
        Path parent = path.toAbsolutePath().getParent();
        Path tempFile = null;
        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tempDir = parent != null ? parent : path.toAbsolutePath().getParent();
            tempFile = Files.createTempFile(tempDir, path.getFileName().toString(), ".tmp");
            Files.writeString(tempFile, content, StandardCharsets.UTF_8);
            try {
                Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
            }
            return ConfigStoreWriteResult.success("Wrote config to " + path.toAbsolutePath());
        } catch (IOException e) {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // Best-effort cleanup only.
                }
            }
            return ConfigStoreWriteResult.failure("Failed to write config to " + path.toAbsolutePath() + ": " + e.getMessage());
        }
    }

    @Override
    public String description() {
        return "file:" + path.toAbsolutePath();
    }

    public Path path() {
        return path;
    }
}
