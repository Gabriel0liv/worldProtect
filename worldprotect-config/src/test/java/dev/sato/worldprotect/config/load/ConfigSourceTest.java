package dev.sato.worldprotect.config.load;

import dev.sato.worldprotect.config.toml.TomlConfigParseResult;
import dev.sato.worldprotect.config.toml.TomlConfigParser;
import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public final class ConfigSourceTest {

    @Test
    public void testStringTomlConfigSourceSuccess() {
        StringTomlConfigSource source = StringTomlConfigSource.of("test-desc", "regions = {}");
        assertEquals("test-desc", source.description());
        assertEquals("regions = {}", source.toml());

        StringTomlConfigSource inline = StringTomlConfigSource.ofToml("regions = {}");
        assertEquals("inline TOML", inline.description());
        assertEquals("regions = {}", inline.toml());
    }

    @Test
    public void testStringTomlConfigSourceValidation() {
        assertThrows(NullPointerException.class, () -> StringTomlConfigSource.of(null, "regions = {}"));
        assertThrows(IllegalArgumentException.class, () -> StringTomlConfigSource.of("   ", "regions = {}"));
        assertThrows(NullPointerException.class, () -> StringTomlConfigSource.of("test", null));
        assertThrows(NullPointerException.class, () -> StringTomlConfigSource.ofToml(null));
    }

    @Test
    public void testStringTomlConfigSourceParse() {
        StringTomlConfigSource source = StringTomlConfigSource.ofToml("regions = {}");
        TomlConfigParser parser = new TomlConfigParser();
        TomlConfigParseResult result = source.parse(parser);
        assertNotNull(result);
        assertTrue(result.isSuccess() || result.hasErrors()); // Check parse is executed
    }

    @Test
    public void testFileTomlConfigSourceSuccess(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("config.toml");
        Files.writeString(file, "regions = {}");

        FileTomlConfigSource source = FileTomlConfigSource.of(file);
        assertEquals(file, source.path());
        assertEquals("file:" + file.toAbsolutePath(), source.description());

        TomlConfigParser parser = new TomlConfigParser();
        TomlConfigParseResult result = source.parse(parser);
        assertNotNull(result);
    }

    @Test
    public void testFileTomlConfigSourceValidation() {
        assertThrows(NullPointerException.class, () -> FileTomlConfigSource.of(null));
    }
}
