package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagRegistry;
import dev.sato.worldprotect.protection.subject.RegionAccessPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionAccessPolicyConfigTest {

    private FlagRegistry registry;

    @BeforeEach
    public void setUp() {
        registry = FlagRegistry.withBuiltIns();
    }

    @Test
    public void testDefaultsMapping() {
        RegionAccessPolicyConfig config = RegionAccessPolicyConfig.defaults();
        assertNull(config.ownersBypass());
        assertNull(config.membersBypass());
        assertTrue(config.ownerBypassFlags().isEmpty());
        assertTrue(config.memberBypassFlags().isEmpty());

        ConfigValidationResult result = config.validate("test.path", registry);
        assertTrue(result.isValid());

        RegionAccessPolicy domain = config.toDomain();
        assertTrue(domain.ownersBypassFlags()); // defaults to true
        assertFalse(domain.membersBypassFlags()); // defaults to false
        assertTrue(domain.ownerBypassFlags().isEmpty());
        assertTrue(domain.memberBypassFlags().isEmpty());
    }

    @Test
    public void testCustomValuesAreRespected() {
        RegionAccessPolicyConfig config = RegionAccessPolicyConfig.of(
                false,
                true,
                List.of("break-block"),
                List.of("open-container")
        );

        assertEquals(false, config.ownersBypass());
        assertEquals(true, config.membersBypass());
        assertEquals(List.of("break-block"), config.ownerBypassFlags());
        assertEquals(List.of("open-container"), config.memberBypassFlags());

        ConfigValidationResult result = config.validate("test.path", registry);
        assertTrue(result.isValid());

        RegionAccessPolicy domain = config.toDomain();
        assertFalse(domain.ownersBypassFlags());
        assertTrue(domain.membersBypassFlags());
        assertTrue(domain.ownerBypassFlags().contains(BuiltInFlags.BREAK_BLOCK_KEY));
        assertTrue(domain.memberBypassFlags().contains(BuiltInFlags.OPEN_CONTAINER_KEY));
    }

    @Test
    public void testInvalidFlagKeySyntaxFails() {
        RegionAccessPolicyConfig config = RegionAccessPolicyConfig.of(
                true,
                false,
                List.of("Invalid flag name"),
                List.of()
        );

        ConfigValidationResult result = config.validate("test.path", registry);
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("test.path.owner-bypass-flags[0]", result.errors().get(0).path());
        assertTrue(result.errors().get(0).message().contains("Invalid flag syntax"));
    }

    @Test
    public void testUnknownFlagFails() {
        RegionAccessPolicyConfig config = RegionAccessPolicyConfig.of(
                true,
                false,
                List.of(),
                List.of("unknown-flag-key")
        );

        ConfigValidationResult result = config.validate("test.path", registry);
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("test.path.member-bypass-flags[0]", result.errors().get(0).path());
        assertTrue(result.errors().get(0).message().contains("Unknown flag key"));
    }

    @Test
    public void testDuplicateFlagWarnings() {
        RegionAccessPolicyConfig config = RegionAccessPolicyConfig.of(
                true,
                false,
                List.of("break-block", "break-block"),
                List.of()
        );

        ConfigValidationResult result = config.validate("test.path", registry);
        assertTrue(result.isValid());
        assertTrue(result.hasWarnings());
        assertEquals(1, result.warnings().size());
        assertEquals("test.path.owner-bypass-flags", result.warnings().get(0).path());
        assertTrue(result.warnings().get(0).message().contains("Duplicate flag configured"));
    }

    @Test
    public void testImmutabilityOfLists() {
        RegionAccessPolicyConfig config = RegionAccessPolicyConfig.defaults();
        assertThrows(UnsupportedOperationException.class, () -> config.ownerBypassFlags().add("break-block"));
        assertThrows(UnsupportedOperationException.class, () -> config.memberBypassFlags().add("break-block"));
    }
}
