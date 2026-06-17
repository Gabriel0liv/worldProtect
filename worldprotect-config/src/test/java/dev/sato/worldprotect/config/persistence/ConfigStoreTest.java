package dev.sato.worldprotect.config.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public final class ConfigStoreTest {

    @TempDir
    Path tempDir;

    @Test
    public void testInMemoryConfigStoreReadWrite() {
        InMemoryConfigStore store = InMemoryConfigStore.ofToml("a");
        assertEquals("a", store.read().content().orElseThrow());
        assertTrue(store.write("b").isSuccess());
        assertEquals("b", store.read().content().orElseThrow());
    }

    @Test
    public void testFileConfigStoreReadAndWriteCreatesFileAndParents() throws Exception {
        Path target = tempDir.resolve("nested").resolve("worldprotect.toml");
        FileConfigStore store = FileConfigStore.of(target);

        assertTrue(store.write("[regions]\n").isSuccess());
        assertTrue(Files.exists(target));
        assertEquals("[regions]\n", Files.readString(target));
        assertEquals("[regions]\n", store.read().content().orElseThrow());
    }

    @Test
    public void testFileConfigStoreReplacesExistingFile() throws Exception {
        Path target = tempDir.resolve("worldprotect.toml");
        Files.writeString(target, "old");
        FileConfigStore store = FileConfigStore.of(target);

        assertTrue(store.write("new").isSuccess());
        assertEquals("new", Files.readString(target));
    }

    @Test
    public void testFileConfigStoreWriteFailureOnDirectoryTarget() throws Exception {
        Path directoryTarget = tempDir.resolve("as-directory");
        Files.createDirectories(directoryTarget);
        FileConfigStore store = FileConfigStore.of(directoryTarget);

        ConfigStoreWriteResult result = store.write("x");

        assertFalse(result.isSuccess());
    }
}
