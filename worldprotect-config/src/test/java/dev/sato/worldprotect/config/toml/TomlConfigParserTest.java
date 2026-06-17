package dev.sato.worldprotect.config.toml;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.core.actor.ActorType;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.config.ConfigToDomainMapper;
import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagRegistry;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.query.CauseChain;
import dev.sato.worldprotect.protection.query.ProtectionAction;
import dev.sato.worldprotect.protection.query.ProtectionCause;
import dev.sato.worldprotect.protection.query.ProtectionQuery;
import dev.sato.worldprotect.protection.query.ProtectionTarget;
import dev.sato.worldprotect.protection.region.RegionSet;
import dev.sato.worldprotect.protection.resolver.ProtectionResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public final class TomlConfigParserTest {

    private TomlConfigParser parser;
    private FlagRegistry registry;

    @BeforeEach
    public void setUp() {
        parser = new TomlConfigParser();
        registry = FlagRegistry.withBuiltIns();
    }

    // 1. parses minimal valid region with simple flags
    @Test
    public void testParsesMinimalValidRegionWithSimpleFlags() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.flags]\n" +
                "break-block = \"deny\"\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertTrue(result.isSuccess());
        assertTrue(result.config().isPresent());

        WorldProtectConfig config = result.config().get();
        assertEquals(1, config.regions().size());
        assertEquals("spawn", config.regions().get(0).id().getValue());
        assertEquals(FlagState.DENY, config.regions().get(0).flags().get(BuiltInFlags.BREAK_BLOCK_KEY).defaultState());
    }

    // 2. parses conditional flag rule with allow/deny lists
    @Test
    public void testParsesConditionalFlagRuleWithAllowDenyLists() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.flags.use-item]\n" +
                "default = \"deny\"\n" +
                "allow = [\"minecraft:bucket\", \"create:wrench\"]\n" +
                "deny = [\"botania:twig_wand\"]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertTrue(result.isSuccess());
        
        WorldProtectConfig config = result.config().get();
        assertEquals(1, config.regions().size());
        
        var rule = config.regions().get(0).flags().get(BuiltInFlags.USE_ITEM_KEY);
        assertNotNull(rule);
        assertFalse(rule.isSimple());
        assertEquals(FlagState.DENY, rule.defaultState());
        assertEquals(List.of("minecraft:bucket", "create:wrench"), rule.allowSelectors());
        assertEquals(List.of("botania:twig_wand"), rule.denySelectors());
    }

    // 3. parses multiple regions
    @Test
    public void testParsesMultipleRegions() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "\n" +
                "[regions.market]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 50\n" +
                "[regions.market.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [-50, 0, -50]\n" +
                "max = [50, 100, 50]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertTrue(result.isSuccess());
        
        WorldProtectConfig config = result.config().get();
        assertEquals(2, config.regions().size());
        assertEquals("spawn", config.regions().get(0).id().getValue());
        assertEquals("market", config.regions().get(1).id().getValue());
    }

    // 4. missing regions table returns parse failure/error
    @Test
    public void testMissingRegionsTableReturnsFailure() {
        String toml = "title = \"Nothing here\"\n";
        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions", result.diagnostics().errors().get(0).path());
    }

    // 5. invalid region ID produces error and skips region
    @Test
    public void testInvalidRegionIdProducesErrorAndSkipsRegion() {
        // region ID contains spaces, which fails RegionId validation
        String toml =
                "[regions.\"spawn zone\"]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.\"spawn zone\".bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.spawn zone", result.diagnostics().errors().get(0).path());
    }

    // 6. invalid dimension produces error
    @Test
    public void testInvalidDimensionProducesError() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"invalid:dimension:namespace:too_many_colons\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.spawn.dimension", result.diagnostics().errors().get(0).path());
    }

    // 7. missing priority produces error
    @Test
    public void testMissingPriorityProducesError() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.spawn.priority", result.diagnostics().errors().get(0).path());
    }

    // 8. priority wrong type produces error
    @Test
    public void testPriorityWrongTypeProducesError() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = \"hundred\"\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.spawn.priority", result.diagnostics().errors().get(0).path());
    }

    // 9. missing bounds table produces error
    @Test
    public void testMissingBoundsTableProducesError() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.spawn.bounds", result.diagnostics().errors().get(0).path());
    }

    // 10. bounds type not cuboid produces error
    @Test
    public void testBoundsTypeNotCuboidProducesError() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"polygon\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.spawn.bounds", result.diagnostics().errors().get(0).path());
    }

    // 11. min/max arrays must have exactly 3 integers
    @Test
    public void testMinMaxArraysMustHaveExactly3Integers() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0]\n" + // size is 2
                "max = [10, 10, 10]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.spawn.bounds.min", result.diagnostics().errors().get(0).path());
    }

    // 12. float bounds values fail
    @Test
    public void testFloatBoundsValuesFail() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0.5, 0]\n" + // has a float value 0.5
                "max = [10, 10, 10]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.spawn.bounds.min", result.diagnostics().errors().get(0).path());
    }

    // 13. simple flag invalid state fails
    @Test
    public void testSimpleFlagInvalidStateFails() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.flags]\n" +
                "break-block = \"blocked\"\n"; // invalid state name

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.spawn.flags.break-block", result.diagnostics().errors().get(0).path());
    }

    // 14. conditional flag missing default fails
    @Test
    public void testConditionalFlagMissingDefaultFails() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.flags.use-item]\n" +
                "allow = [\"minecraft:bucket\"]\n"; // default state is required

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.spawn.flags.use-item.default", result.diagnostics().errors().get(0).path());
    }

    // 15. conditional allow/deny must be arrays of strings
    @Test
    public void testConditionalAllowDenyMustBeArraysOfStrings() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.flags.use-item]\n" +
                "default = \"deny\"\n" +
                "allow = [100, \"create:wrench\"]\n"; // 100 is not a string

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.spawn.flags.use-item.allow[0]", result.diagnostics().errors().get(0).path());
    }

    // 16. uppercase region ID fails
    @Test
    public void testUppercaseRegionIdFails() {
        String toml =
                "[regions.Spawn]\n" + // capital S
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.Spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.Spawn", result.diagnostics().errors().get(0).path());
    }

    // 17. uppercase dimension namespace fails
    @Test
    public void testUppercaseDimensionNamespaceFails() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"Minecraft:overworld\"\n" + // capital M
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.spawn.dimension", result.diagnostics().errors().get(0).path());
    }

    // 18. uppercase selector is accepted by parser but later fails FlagRuleConfig.validate(...)
    @Test
    public void testUppercaseSelectorIsAcceptedByParserButFailsFlagRuleConfigValidate() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.flags.use-item]\n" +
                "default = \"deny\"\n" +
                "allow = [\"Create:wrench\"]\n"; // capital C is syntactically invalid for ResourceRef/Selector parse

        TomlConfigParseResult result = parser.parseString(toml);
        // Should parse successfully because syntax/type rules pass at TOML level
        assertTrue(result.isSuccess());
        
        WorldProtectConfig config = result.config().get();
        // Validation by ConfigValidation validates the config block structurally
        ConfigValidationResult validation = config.validate(registry);
        assertFalse(validation.isValid());
        assertEquals(1, validation.errors().size());
        assertEquals("regions.spawn.flags.use-item.allow[0]", validation.errors().get(0).path());
    }

    // 19. parsed config can be validated and mapped into RegionSet
    @Test
    public void testParsedConfigCanBeValidatedAndMappedIntoRegionSet() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.flags]\n" +
                "break-block = \"deny\"\n" +
                "[regions.spawn.flags.use-item]\n" +
                "default = \"deny\"\n" +
                "allow = [\"create:wrench\"]\n";

        TomlConfigParseResult parseResult = parser.parseString(toml);
        assertTrue(parseResult.isSuccess());
        
        WorldProtectConfig config = parseResult.config().get();
        ConfigValidationResult validation = config.validate(registry);
        assertTrue(validation.isValid());

        ConfigToDomainMapper mapper = new ConfigToDomainMapper();
        RegionSet regionSet = mapper.toRegionSet(config);
        assertEquals(1, regionSet.regions().size());
    }

    // 20. mapped RegionSet works with ProtectionResolver
    @Test
    public void testMappedRegionSetWorksWithProtectionResolver() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.flags]\n" +
                "break-block = \"deny\"\n" +
                "[regions.spawn.flags.use-item]\n" +
                "default = \"deny\"\n" +
                "allow = [\"create:wrench\"]\n";

        TomlConfigParseResult parseResult = parser.parseString(toml);
        assertTrue(parseResult.isSuccess());
        
        WorldProtectConfig config = parseResult.config().get();
        ConfigToDomainMapper mapper = new ConfigToDomainMapper();
        RegionSet regionSet = mapper.toRegionSet(config);

        ProtectionResolver resolver = new ProtectionResolver();
        Actor actor = new Actor("player1", ActorType.PLAYER);
        DimensionRef overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        BlockPosRef pos = new BlockPosRef(5, 5, 5);

        // break-block is denied
        ProtectionTarget blockTarget = ProtectionTarget.block(ResourceRef.of("minecraft", "stone"), overworld, pos);
        ProtectionQuery breakQuery = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, CauseChain.of(ProtectionCause.player()), blockTarget, overworld, pos);
        assertTrue(resolver.resolve(breakQuery, regionSet).isDenied());

        // use-item: create:wrench is allowed
        CauseChain causeWrench = CauseChain.of(ProtectionCause.item(ResourceRef.of("create", "wrench")));
        ProtectionQuery wrenchQuery = new ProtectionQuery(actor, ProtectionAction.ITEM_USE, causeWrench, ProtectionTarget.unknown(), overworld, pos);
        assertTrue(resolver.resolve(wrenchQuery, regionSet).isAllowed());

        // use-item: minecraft:stick is denied
        CauseChain causeStick = CauseChain.of(ProtectionCause.item(ResourceRef.of("minecraft", "stick")));
        ProtectionQuery stickQuery = new ProtectionQuery(actor, ProtectionAction.ITEM_USE, causeStick, ProtectionTarget.unknown(), overworld, pos);
        assertTrue(resolver.resolve(stickQuery, regionSet).isDenied());
    }

    // Additional test matching requirement 5: simple flags and conditional subtables together
    @Test
    public void testSimpleFlagsAndConditionalSubtablesTogether() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.flags]\n" +
                "break-block = \"deny\"\n" + // Simple inline flag
                "[regions.spawn.flags.use-item]\n" + // Subtable under same flags table
                "default = \"deny\"\n" +
                "allow = [\"create:wrench\"]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertTrue(result.isSuccess());

        WorldProtectConfig config = result.config().get();
        var flags = config.regions().get(0).flags();
        assertEquals(2, flags.size());
        assertTrue(flags.get(BuiltInFlags.BREAK_BLOCK_KEY).isSimple());
        assertFalse(flags.get(BuiltInFlags.USE_ITEM_KEY).isSimple());
    }

    @Test
    public void testParsesSubjectsSuccessfully() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.subjects]\n" +
                "owners = [\"player:00000000-0000-0000-0000-000000000000\"]\n" +
                "members = [\"group:trusted\", \"console\", \"system\"]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertTrue(result.isSuccess());

        WorldProtectConfig config = result.config().get();
        var subjects = config.regions().get(0).subjectsConfig();
        assertEquals(1, subjects.owners().size());
        assertEquals("player:00000000-0000-0000-0000-000000000000", subjects.owners().get(0).rawValue());
        assertEquals(3, subjects.members().size());
        assertEquals("group:trusted", subjects.members().get(0).rawValue());
        assertEquals("console", subjects.members().get(1).rawValue());
        assertEquals("system", subjects.members().get(2).rawValue());
    }

    @Test
    public void testParsesAccessPolicySuccessfully() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.access]\n" +
                "owners-bypass = false\n" +
                "members-bypass = true\n" +
                "owner-bypass-flags = [\"break-block\"]\n" +
                "member-bypass-flags = [\"place-block\"]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertTrue(result.isSuccess());

        WorldProtectConfig config = result.config().get();
        var access = config.regions().get(0).accessPolicyConfig();
        assertEquals(false, access.ownersBypass());
        assertEquals(true, access.membersBypass());
        assertEquals(List.of("break-block"), access.ownerBypassFlags());
        assertEquals(List.of("place-block"), access.memberBypassFlags());
    }

    @Test
    public void testInvalidSubjectsOrAccessPolicyDiagnostics() {
        // Test owners not being an array
        String toml1 =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.subjects]\n" +
                "owners = \"not-an-array\"\n";

        TomlConfigParseResult result1 = parser.parseString(toml1);
        assertFalse(result1.isSuccess());
        assertTrue(result1.hasErrors());
        assertEquals("regions.spawn.subjects.owners", result1.diagnostics().errors().get(0).path());

        // Test owners array contains non-string
        String toml2 =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.subjects]\n" +
                "owners = [123]\n";

        TomlConfigParseResult result2 = parser.parseString(toml2);
        assertFalse(result2.isSuccess());
        assertTrue(result2.hasErrors());
        assertEquals("regions.spawn.subjects.owners[0]", result2.diagnostics().errors().get(0).path());

        // Test owners-bypass wrong type
        String toml3 =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.access]\n" +
                "owners-bypass = \"yes\"\n";

        TomlConfigParseResult result3 = parser.parseString(toml3);
        assertFalse(result3.isSuccess());
        assertTrue(result3.hasErrors());
        assertEquals("regions.spawn.access.owners-bypass", result3.diagnostics().errors().get(0).path());
    }

    @Test
    public void testParsesGlobalBoundsWithoutCoordinates() {
        String toml =
                "[regions.global_overworld]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = -100\n" +
                "[regions.global_overworld.bounds]\n" +
                "type = \"global\"\n" +
                "[regions.global_overworld.flags]\n" +
                "break-block = \"deny\"\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertTrue(result.isSuccess());
        assertFalse(result.diagnostics().hasWarnings());

        WorldProtectConfig config = result.config().get();
        assertEquals(1, config.regions().size());
        assertTrue(config.regions().get(0).bounds().isGlobal());
    }

    @Test
    public void testParsesGlobalBoundsWithCoordinatesGeneratesWarning() {
        String toml =
                "[regions.global_overworld]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = -100\n" +
                "[regions.global_overworld.bounds]\n" +
                "type = \"global\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertTrue(result.isSuccess());
        assertTrue(result.diagnostics().hasWarnings());
        assertEquals(1, result.diagnostics().warnings().size());
        assertEquals("regions.global_overworld.bounds", result.diagnostics().warnings().get(0).path());
        assertTrue(result.diagnostics().warnings().get(0).message().contains("Global bounds ignore min/max"));
    }

    @Test
    public void testParserRejectsUnknownBoundsType() {
        String toml =
                "[regions.global_overworld]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = -100\n" +
                "[regions.global_overworld.bounds]\n" +
                "type = \"sphere\"\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.global_overworld.bounds", result.diagnostics().errors().get(0).path());
    }

    @Test
    public void testParserAllowsBlankSubjectsButFailsValidation() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n" +
                "[regions.spawn.subjects]\n" +
                "owners = [\"\"]\n" +
                "members = [\"   \"]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertTrue(result.isSuccess());

        WorldProtectConfig config = result.config().get();
        ConfigValidationResult validation = config.validate(registry);
        assertFalse(validation.isValid());
        assertEquals(2, validation.errors().size());
        assertTrue(validation.errors().stream().anyMatch(e -> e.path().contains("owners[0]")));
        assertTrue(validation.errors().stream().anyMatch(e -> e.path().contains("members[0]")));
    }

    @Test
    public void testParsesParentKeySuccessfully() {
        String toml =
                "[regions.child]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "parent = \"spawn\"\n" +
                "[regions.child.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertTrue(result.isSuccess());
        WorldProtectConfig config = result.config().get();
        assertEquals(1, config.regions().size());
        assertTrue(config.regions().get(0).parentId().isPresent());
        assertEquals("spawn", config.regions().get(0).parentId().get().getValue());
    }

    @Test
    public void testParserFailsOnParentWrongType() {
        String toml =
                "[regions.child]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "parent = 123\n" +
                "[regions.child.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.child.parent", result.diagnostics().errors().get(0).path());
        assertTrue(result.diagnostics().errors().get(0).message().contains("Parent must be a string"));
    }

    @Test
    public void testParserFailsOnInvalidParentIdSyntax() {
        String toml =
                "[regions.child]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 100\n" +
                "parent = \"Invalid parent with spaces\"\n" +
                "[regions.child.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 0, 0]\n" +
                "max = [10, 10, 10]\n";

        TomlConfigParseResult result = parser.parseString(toml);
        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertEquals("regions.child.parent", result.diagnostics().errors().get(0).path());
        assertTrue(result.diagnostics().errors().get(0).message().contains("Invalid parent region ID"));
    }
}
