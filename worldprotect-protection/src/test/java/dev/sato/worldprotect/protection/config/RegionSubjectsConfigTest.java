package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.protection.subject.RegionSubjects;
import dev.sato.worldprotect.protection.subject.SubjectRef;
import dev.sato.worldprotect.protection.subject.SubjectType;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionSubjectsConfigTest {

    @Test
    public void testEmptySubjectsConfig() {
        RegionSubjectsConfig config = RegionSubjectsConfig.empty();
        assertTrue(config.isEmpty());
        assertTrue(config.owners().isEmpty());
        assertTrue(config.members().isEmpty());

        ConfigValidationResult result = config.validate("test.path");
        assertTrue(result.isValid());

        RegionSubjects domain = config.toDomain();
        assertTrue(domain.isEmpty());
    }

    @Test
    public void testValidOwnersAndMembersMapping() {
        UUID ownerUuid = UUID.randomUUID();
        SubjectRefConfig ownerConf = SubjectRefConfig.of("player:" + ownerUuid);
        SubjectRefConfig memberConf = SubjectRefConfig.of("group:trusted");

        RegionSubjectsConfig config = RegionSubjectsConfig.of(List.of(ownerConf), List.of(memberConf));
        assertFalse(config.isEmpty());

        ConfigValidationResult result = config.validate("test.path");
        assertTrue(result.isValid());

        RegionSubjects domain = config.toDomain();
        assertFalse(domain.isEmpty());
        assertTrue(domain.isOwner(SubjectRef.player(ownerUuid)));
        assertTrue(domain.isMember(SubjectRef.group("trusted")));
    }

    @Test
    public void testOwnerWinsOverMember() {
        SubjectRefConfig subjectConf = SubjectRefConfig.of("group:trusted");
        RegionSubjectsConfig config = RegionSubjectsConfig.of(List.of(subjectConf), List.of(subjectConf));

        ConfigValidationResult result = config.validate("test.path");
        // Warning should be present for intersection
        assertTrue(result.isValid()); // warnings are not errors
        assertTrue(result.hasWarnings());
        assertEquals(1, result.warnings().size());
        assertTrue(result.warnings().get(0).message().contains("configured as both owner and member"));

        RegionSubjects domain = config.toDomain();
        assertTrue(domain.isOwner(SubjectRef.group("trusted")));
        // Owner wins: it must NOT be a member in the final domain container because RegionSubjects constructor filters it out.
        assertFalse(domain.members().contains(SubjectRef.group("trusted")));
    }

    @Test
    public void testDuplicateOwnersWarning() {
        SubjectRefConfig o1 = SubjectRefConfig.of("console");
        RegionSubjectsConfig config = RegionSubjectsConfig.of(List.of(o1, o1), List.of());

        ConfigValidationResult result = config.validate("test.path");
        assertTrue(result.isValid());
        assertTrue(result.hasWarnings());
        assertEquals(1, result.warnings().size());
        assertEquals("test.path.owners", result.warnings().get(0).path());
        assertTrue(result.warnings().get(0).message().contains("Duplicate owner configured"));
    }

    @Test
    public void testDuplicateMembersWarning() {
        SubjectRefConfig m1 = SubjectRefConfig.of("system");
        RegionSubjectsConfig config = RegionSubjectsConfig.of(List.of(), List.of(m1, m1));

        ConfigValidationResult result = config.validate("test.path");
        assertTrue(result.isValid());
        assertTrue(result.hasWarnings());
        assertEquals(1, result.warnings().size());
        assertEquals("test.path.members", result.warnings().get(0).path());
        assertTrue(result.warnings().get(0).message().contains("Duplicate member configured"));
    }

    @Test
    public void testListImmutability() {
        RegionSubjectsConfig config = RegionSubjectsConfig.empty();
        assertThrows(UnsupportedOperationException.class, () -> config.owners().add(SubjectRefConfig.of("console")));
        assertThrows(UnsupportedOperationException.class, () -> config.members().add(SubjectRefConfig.of("system")));
    }
}
