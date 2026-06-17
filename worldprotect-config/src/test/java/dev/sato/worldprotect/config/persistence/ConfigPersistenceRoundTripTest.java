package dev.sato.worldprotect.config.persistence;

import dev.sato.worldprotect.config.toml.TomlConfigParser;
import dev.sato.worldprotect.config.toml.TomlConfigWriter;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.config.BoundsConfig;
import dev.sato.worldprotect.protection.config.FlagRuleConfig;
import dev.sato.worldprotect.protection.config.RegionAccessPolicyConfig;
import dev.sato.worldprotect.protection.config.RegionConfig;
import dev.sato.worldprotect.protection.config.RegionSubjectsConfig;
import dev.sato.worldprotect.protection.config.SubjectRefConfig;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.management.RegionManagementService;
import dev.sato.worldprotect.protection.management.SetRegionFlagRequest;
import dev.sato.worldprotect.protection.region.RegionId;
import dev.sato.worldprotect.protection.subject.RegionGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public final class ConfigPersistenceRoundTripTest {

    private TomlConfigWriter writer;
    private TomlConfigParser parser;

    @BeforeEach
    public void setUp() {
        writer = new TomlConfigWriter();
        parser = new TomlConfigParser();
    }

    @Test
    public void testCanonicalSampleConfigRoundTrips() {
        WorldProtectConfig config = complexConfig();
        String toml = writer.write(config).content().orElseThrow();

        assertTrue(parser.parseString(toml).isSuccess());
        WorldProtectConfig reparsed = parser.parseString(toml).config().orElseThrow();
        assertEquals(config, reparsed);
    }

    @Test
    public void testBuildPassthroughAndRegionGroupsRoundTrip() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of(
                RegionConfig.of(
                        RegionId.of("spawn"),
                        new DimensionRef(ResourceRef.of("minecraft", "overworld")),
                        100,
                        BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(5, 5, 5)),
                        Map.of(
                                BuiltInFlags.BUILD_KEY, FlagRuleConfig.simple(FlagState.DENY),
                                BuiltInFlags.PASSTHROUGH_KEY, FlagRuleConfig.simple(FlagState.ALLOW),
                                BuiltInFlags.INTERACT_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY, RegionGroup.NONMEMBERS)
                        )
                )
        ));

        WorldProtectConfig reparsed = parser.parseString(writer.write(config).content().orElseThrow()).config().orElseThrow();
        assertEquals(config, reparsed);
    }

    @Test
    public void testRepositorySaveAndReloadWithManagementMutation() {
        WorldProtectConfig original = complexConfig();
        InMemoryConfigStore store = InMemoryConfigStore.ofToml(writer.write(original).content().orElseThrow());
        WorldProtectConfigRepository repository = WorldProtectConfigRepository.withBuiltIns(store);
        RegionManagementService managementService = RegionManagementService.withBuiltIns();

        ConfigRepositoryResult<WorldProtectConfig> update = repository.update(config ->
                managementService.setFlag(
                        config,
                        SetRegionFlagRequest.of(RegionId.of("spawn"), BuiltInFlags.FLUID_SPREAD_KEY, FlagRuleConfig.simple(FlagState.DENY))
                )
        );
        ConfigRepositoryResult<WorldProtectConfig> reload = repository.load();

        assertEquals(ConfigRepositoryStatus.SUCCESS, update.status());
        assertEquals(ConfigRepositoryStatus.SUCCESS, reload.status());
        assertTrue(reload.value().regions().get(0).flags().containsKey(BuiltInFlags.FLUID_SPREAD_KEY));
    }

    private WorldProtectConfig complexConfig() {
        DimensionRef overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        RegionConfig parent = RegionConfig.of(
                RegionId.of("spawn"),
                overworld,
                100,
                BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10)),
                Map.of(
                        BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY),
                        BuiltInFlags.PASSTHROUGH_KEY, FlagRuleConfig.simple(FlagState.ALLOW),
                        BuiltInFlags.USE_ITEM_ON_BLOCK_KEY, FlagRuleConfig.conditional(FlagState.DENY, List.of("minecraft:oak_door"), List.of("create:wrench"), RegionGroup.MEMBERS)
                ),
                RegionSubjectsConfig.of(
                        List.of(SubjectRefConfig.of("player:00000000-0000-0000-0000-000000000000")),
                        List.of(SubjectRefConfig.of("group:trusted"))
                ),
                RegionAccessPolicyConfig.of(false, true, List.of("build"), List.of("break-block"))
        );
        RegionConfig child = RegionConfig.of(
                RegionId.of("shop"),
                overworld,
                150,
                BoundsConfig.global(),
                Map.of(BuiltInFlags.BUILD_KEY, FlagRuleConfig.simple(FlagState.ALLOW)),
                RegionSubjectsConfig.empty(),
                RegionAccessPolicyConfig.defaults(),
                Optional.of(RegionId.of("spawn"))
        );
        return WorldProtectConfig.of(List.of(parent, child));
    }
}
