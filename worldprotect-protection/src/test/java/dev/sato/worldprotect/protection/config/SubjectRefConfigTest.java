package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.protection.subject.SubjectRef;
import dev.sato.worldprotect.protection.subject.SubjectType;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public final class SubjectRefConfigTest {

    @Test
    public void testValidPlayerSubject() {
        UUID uuid = UUID.randomUUID();
        SubjectRefConfig config = SubjectRefConfig.of("player:" + uuid);
        assertEquals("player:" + uuid, config.rawValue());

        ConfigValidationResult result = config.validate("test.path");
        assertTrue(result.isValid());

        SubjectRef domain = config.toDomain();
        assertEquals(SubjectType.PLAYER, domain.type());
        assertEquals(uuid.toString(), domain.id());
    }

    @Test
    public void testValidGroupSubject() {
        SubjectRefConfig config = SubjectRefConfig.of("group:trusted-members");
        assertEquals("group:trusted-members", config.rawValue());

        ConfigValidationResult result = config.validate("test.path");
        assertTrue(result.isValid());

        SubjectRef domain = config.toDomain();
        assertEquals(SubjectType.GROUP, domain.type());
        assertEquals("trusted-members", domain.id());
    }

    @Test
    public void testConsoleSubject() {
        SubjectRefConfig config = SubjectRefConfig.of("console");
        assertEquals("console", config.rawValue());

        ConfigValidationResult result = config.validate("test.path");
        assertTrue(result.isValid());

        SubjectRef domain = config.toDomain();
        assertEquals(SubjectType.CONSOLE, domain.type());
        assertEquals("console", domain.id());
    }

    @Test
    public void testSystemSubject() {
        SubjectRefConfig config = SubjectRefConfig.of("system");
        assertEquals("system", config.rawValue());

        ConfigValidationResult result = config.validate("test.path");
        assertTrue(result.isValid());

        SubjectRef domain = config.toDomain();
        assertEquals(SubjectType.SYSTEM, domain.type());
        assertEquals("system", domain.id());
    }

    @Test
    public void testUnknownPrefixFailsValidation() {
        SubjectRefConfig config = SubjectRefConfig.of("unknown:value");
        ConfigValidationResult result = config.validate("test.path");
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("test.path", result.errors().get(0).path());
        assertTrue(result.errors().get(0).message().contains("Unknown or invalid prefix"));
    }

    @Test
    public void testInvalidUuidFailsValidation() {
        SubjectRefConfig config = SubjectRefConfig.of("player:not-a-uuid");
        ConfigValidationResult result = config.validate("test.path");
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("test.path", result.errors().get(0).path());
        assertTrue(result.errors().get(0).message().contains("Invalid UUID format"));
    }

    @Test
    public void testUppercaseGroupFailsValidation() {
        SubjectRefConfig config = SubjectRefConfig.of("group:Trusted");
        ConfigValidationResult result = config.validate("test.path");
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("test.path", result.errors().get(0).path());
        assertTrue(result.errors().get(0).message().contains("must be lowercase"));
    }

    @Test
    public void testConsoleWithParametersFailsValidation() {
        SubjectRefConfig config = SubjectRefConfig.of("console:extra");
        ConfigValidationResult result = config.validate("test.path");
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("test.path", result.errors().get(0).path());
        assertTrue(result.errors().get(0).message().contains("must not contain parameters"));
    }

    @Test
    public void testUppercaseConsoleFailsValidation() {
        SubjectRefConfig config = SubjectRefConfig.of("Console");
        ConfigValidationResult result = config.validate("test.path");
        assertFalse(result.isValid());
        assertEquals("test.path", result.errors().get(0).path());
        assertTrue(result.errors().get(0).message().contains("Console subject must be exactly 'console'"));
    }

    @Test
    public void testBlankStringRejectedOnCreation() {
        assertThrows(NullPointerException.class, () -> SubjectRefConfig.of(null));
        assertThrows(IllegalArgumentException.class, () -> SubjectRefConfig.of(""));
        assertThrows(IllegalArgumentException.class, () -> SubjectRefConfig.of("   "));
    }
}
