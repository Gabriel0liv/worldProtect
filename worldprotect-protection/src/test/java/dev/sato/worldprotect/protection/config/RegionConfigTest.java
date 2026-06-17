package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagRegistry;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.region.RegionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionConfigTest {

    private FlagRegistry registry;
    private RegionId regionId;
    private DimensionRef overworld;
    private BoundsConfig validBounds;

    @BeforeEach
    public void setUp() {
        registry = FlagRegistry.withBuiltIns();
        regionId = RegionId.of("spawn");
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        validBounds = BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10));
    }

    @Test
    public void testValidRegionPasses() {
        Map<FlagKey, FlagRuleConfig> flags = Map.of(
                BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY),
                BuiltInFlags.USE_ITEM_KEY, FlagRuleConfig.conditional(FlagState.DENY, List.of("create:wrench"), List.of())
        );

        RegionConfig config = RegionConfig.of(regionId, overworld, 100, validBounds, flags);

        ConfigValidationResult result = config.validate(registry);
        assertTrue(result.isValid());
        assertEquals(0, result.messages().size());
    }

    @Test
    public void testUnknownFlagCreatesError() {
        FlagKey unknownKey = FlagKey.of("fake-flag");
        Map<FlagKey, FlagRuleConfig> flags = Map.of(
                unknownKey, FlagRuleConfig.simple(FlagState.DENY)
        );

        RegionConfig config = RegionConfig.of(regionId, overworld, 100, validBounds, flags);

        ConfigValidationResult result = config.validate(registry);
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("regions.spawn.flags.fake-flag", result.errors().get(0).path());
    }

    @Test
    public void testInvalidBoundsCreatesError() {
        BoundsConfig invalidBounds = BoundsConfig.cuboid(new BlockPosRef(15, 0, 0), new BlockPosRef(10, 10, 10));
        RegionConfig config = RegionConfig.of(regionId, overworld, 100, invalidBounds, Map.of(
                BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY)
        ));

        ConfigValidationResult result = config.validate(registry);
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("regions.spawn.bounds.min", result.errors().get(0).path());
    }

    @Test
    public void testInvalidSelectorCreatesError() {
        Map<FlagKey, FlagRuleConfig> flags = Map.of(
                BuiltInFlags.USE_ITEM_KEY, FlagRuleConfig.conditional(FlagState.DENY, List.of("invalid space"), List.of())
        );

        RegionConfig config = RegionConfig.of(regionId, overworld, 100, validBounds, flags);

        ConfigValidationResult result = config.validate(registry);
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("regions.spawn.flags.use-item.allow[0]", result.errors().get(0).path());
    }

    @Test
    public void testEmptyFlagsCreatesWarning() {
        RegionConfig config = RegionConfig.of(regionId, overworld, 100, validBounds, Map.of());

        ConfigValidationResult result = config.validate(registry);
        assertTrue(result.isValid());
        assertTrue(result.hasWarnings());
        assertEquals(1, result.warnings().size());
        assertEquals("regions.spawn", result.warnings().get(0).path());
        assertTrue(result.warnings().get(0).message().contains("no flags configured"));
    }

    @Test
    public void testFlagsMapIsImmutableAndDefensivelyCopied() {
        Map<FlagKey, FlagRuleConfig> flags = new HashMap<>();
        flags.put(BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY));

        RegionConfig config = RegionConfig.of(regionId, overworld, 100, validBounds, flags);

        // Modify input map
        flags.clear();

        // Config should remain unaffected
        assertEquals(1, config.flags().size());
        assertTrue(config.flags().containsKey(BuiltInFlags.BREAK_BLOCK_KEY));

        // Attempting to modify output flags map must throw UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> {
            config.flags().put(BuiltInFlags.PLACE_BLOCK_KEY, FlagRuleConfig.simple(FlagState.ALLOW));
        });
    }

    @Test
    public void testDefaultsOnOldFactoryMethod() {
        RegionConfig config = RegionConfig.of(regionId, overworld, 100, validBounds, Map.of());
        assertEquals(RegionSubjectsConfig.empty(), config.subjectsConfig());
        assertEquals(RegionAccessPolicyConfig.defaults(), config.accessPolicyConfig());
    }

    @Test
    public void testSubjectsAndAccessValidationMerged() {
        // Subjects config with invalid player UUID
        RegionSubjectsConfig subjects = RegionSubjectsConfig.of(
                List.of(SubjectRefConfig.of("player:invalid-uuid")),
                List.of()
        );
        // Access policy config with unknown flag
        RegionAccessPolicyConfig access = RegionAccessPolicyConfig.of(
                true,
                false,
                List.of("unknown-flag-name"),
                List.of()
        );

        RegionConfig config = RegionConfig.of(regionId, overworld, 100, validBounds, Map.of(), subjects, access);

        ConfigValidationResult result = config.validate(registry);
        assertFalse(result.isValid());
        // Should contain error for invalid UUID and error for unknown flag
        assertTrue(result.errors().stream().anyMatch(m -> m.path().equals("regions.spawn.subjects.owners[0]")));
        assertTrue(result.errors().stream().anyMatch(m -> m.path().equals("regions.spawn.access.owner-bypass-flags[0]")));
    }

    @Test
    public void testParentIdDefaultsToEmpty() {
        RegionConfig config = RegionConfig.of(regionId, overworld, 100, validBounds, Map.of());
        assertFalse(config.parentId().isPresent());
        assertFalse(config.getParentId().isPresent());
    }

    @Test
    public void testParentIdPreserved() {
        RegionId parent = RegionId.of("parent-region");
        RegionConfig config = RegionConfig.of(
                regionId, overworld, 100, validBounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(),
                Optional.of(parent)
        );
        assertTrue(config.parentId().isPresent());
        assertEquals(parent, config.parentId().get());
    }

    @Test
    public void testSelfParentIdFailsValidation() {
        RegionConfig config = RegionConfig.of(
                regionId, overworld, 100, validBounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(),
                Optional.of(regionId)
        );
        ConfigValidationResult result = config.validate(registry);
        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(m -> 
                m.path().equals("regions.spawn.parent") && m.message().contains("parent must not be itself")));
    }

    @Test
    public void testEqualsAndHashCodeIncludeParentId() {
        RegionConfig c1 = RegionConfig.of(
                regionId, overworld, 100, validBounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(),
                Optional.of(RegionId.of("parent1"))
        );
        RegionConfig c2 = RegionConfig.of(
                regionId, overworld, 100, validBounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(),
                Optional.of(RegionId.of("parent1"))
        );
        RegionConfig c3 = RegionConfig.of(
                regionId, overworld, 100, validBounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(),
                Optional.of(RegionId.of("parent2"))
        );

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, c3);
    }
}
