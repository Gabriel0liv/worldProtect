package dev.sato.worldprotect.config.toml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public final class TomlConfigParserFileTest {

    @TempDir
    public Path tempDir;

    @Test
    public void testParseValidTempTomlFile() throws IOException {
        Path tomlFile = tempDir.resolve("config.toml");
        String content =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n";
        
        Files.writeString(tomlFile, content, StandardCharsets.UTF_8);

        TomlConfigParser parser = new TomlConfigParser();
        TomlConfigParseResult result = parser.parseFile(tomlFile);
        
        assertTrue(result.isSuccess());
        assertTrue(result.config().isPresent());
        assertEquals(1, result.config().get().regions().size());
    }

    @Test
    public void testMissingFileReturnsFailureWithPathFile() {
        Path missingFile = tempDir.resolve("non_existent_file.toml");
        
        TomlConfigParser parser = new TomlConfigParser();
        TomlConfigParseResult result = parser.parseFile(missingFile);
        
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals(1, result.diagnostics().errors().size());
        assertEquals("file", result.diagnostics().errors().get(0).path());
    }
}
