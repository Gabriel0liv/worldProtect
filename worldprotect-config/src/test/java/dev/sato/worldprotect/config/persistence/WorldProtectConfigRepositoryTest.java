package dev.sato.worldprotect.config.persistence;

import dev.sato.worldprotect.config.toml.TomlConfigWriter;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.config.BoundsConfig;
import dev.sato.worldprotect.protection.config.FlagRuleConfig;
import dev.sato.worldprotect.protection.config.RegionAccessPolicyConfig;
import dev.sato.worldprotect.protection.config.RegionConfig;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagRegistry;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.management.RegionManagementResult;
import dev.sato.worldprotect.protection.management.RegionManagementService;
import dev.sato.worldprotect.protection.management.SetRegionFlagRequest;
import dev.sato.worldprotect.protection.region.RegionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class WorldProtectConfigRepositoryTest {

    private WorldProtectConfigRepository repository;
    private InMemoryConfigStore store;
    private RegionManagementService managementService;
    private TomlConfigWriter writer;

    @BeforeEach
    public void setUp() {
        writer = new TomlConfigWriter();
        store = InMemoryConfigStore.ofToml(writer.write(validConfig()).content().orElseThrow());
        repository = WorldProtectConfigRepository.withBuiltIns(store);
        managementService = RegionManagementService.withBuiltIns();
    }

    @Test
    public void testLoadSuccess() {
        ConfigRepositoryResult<WorldProtectConfig> result = repository.load();
        assertEquals(ConfigRepositoryStatus.SUCCESS, result.status());
        assertNotNull(result.value());
    }

    @Test
    public void testLoadParseFailureReturnsLoadFailed() {
        WorldProtectConfigRepository failingRepo = WorldProtectConfigRepository.withBuiltIns(InMemoryConfigStore.ofToml("not toml"));
        ConfigRepositoryResult<WorldProtectConfig> result = failingRepo.load();
        assertEquals(ConfigRepositoryStatus.LOAD_FAILED, result.status());
    }

    @Test
    public void testSaveSuccessWritesToml() {
        ConfigRepositoryResult<WorldProtectConfig> result = repository.save(validConfig());
        assertEquals(ConfigRepositoryStatus.SUCCESS, result.status());
        assertTrue(store.read().content().orElseThrow().contains("[regions.spawn]"));
    }

    @Test
    public void testSaveValidationFailureDoesNotWrite() {
        WorldProtectConfig invalid = WorldProtectConfig.of(java.util.List.of(
                RegionConfig.of(
                        RegionId.of("spawn"),
                        new DimensionRef(ResourceRef.of("minecraft", "overworld")),
                        0,
                        BoundsConfig.cuboid(new BlockPosRef(10, 0, 0), new BlockPosRef(0, 0, 0)),
                        java.util.Map.of()
                )
        ));
        String before = store.read().content().orElseThrow();

        ConfigRepositoryResult<WorldProtectConfig> result = repository.save(invalid);

        assertEquals(ConfigRepositoryStatus.VALIDATION_FAILED, result.status());
        assertEquals(before, store.read().content().orElseThrow());
    }

    @Test
    public void testUpdateSuccessWritesMutatedConfigAndPropagatesMutationPlan() {
        ConfigRepositoryResult<WorldProtectConfig> result = repository.update(config ->
                managementService.setFlag(
                        config,
                        SetRegionFlagRequest.of(RegionId.of("spawn"), BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY))
                )
        );

        assertEquals(ConfigRepositoryStatus.SUCCESS, result.status());
        assertTrue(result.getMutationPlan().isPresent());
        assertTrue(store.read().content().orElseThrow().contains("break-block = \"deny\""));
    }

    @Test
    public void testUpdateMutationFailureDoesNotWrite() {
        String before = store.read().content().orElseThrow();
        ConfigRepositoryResult<WorldProtectConfig> result = repository.update(config ->
                RegionManagementResult.failure(
                        dev.sato.worldprotect.protection.management.RegionManagementStatus.VALIDATION_FAILED,
                        dev.sato.worldprotect.protection.config.ConfigValidationResult.ok().add(
                                dev.sato.worldprotect.protection.config.ConfigValidationMessage.error("x", "bad")
                        ),
                        "bad"
                )
        );

        assertEquals(ConfigRepositoryStatus.MUTATION_FAILED, result.status());
        assertEquals(before, store.read().content().orElseThrow());
    }

    @Test
    public void testUpdateNoChangeDoesNotWrite() {
        String before = store.read().content().orElseThrow();
        ConfigRepositoryResult<WorldProtectConfig> result = repository.update(config ->
                RegionManagementResult.noChange(config, "nothing")
        );

        assertEquals(ConfigRepositoryStatus.NO_CHANGE, result.status());
        assertEquals(before, store.read().content().orElseThrow());
    }

    @Test
    public void testUpdateWriteFailureReturnsWriteFailed() {
        ConfigStore badStore = new ConfigStore() {
            @Override
            public ConfigStoreReadResult read() {
                return ConfigStoreReadResult.success(writer.write(validConfig()).content().orElseThrow(), "ok");
            }

            @Override
            public ConfigStoreWriteResult write(String content) {
                return ConfigStoreWriteResult.failure("cannot write");
            }

            @Override
            public String description() {
                return "bad";
            }
        };
        WorldProtectConfigRepository badRepo = WorldProtectConfigRepository.withBuiltIns(badStore);

        ConfigRepositoryResult<WorldProtectConfig> result = badRepo.update(config ->
                managementService.setFlag(
                        config,
                        SetRegionFlagRequest.of(RegionId.of("spawn"), BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY))
                )
        );

        assertEquals(ConfigRepositoryStatus.WRITE_FAILED, result.status());
    }

    private WorldProtectConfig validConfig() {
        return WorldProtectConfig.of(java.util.List.of(
                RegionConfig.of(
                        RegionId.of("spawn"),
                        new DimensionRef(ResourceRef.of("minecraft", "overworld")),
                        100,
                        BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10)),
                        java.util.Map.of(BuiltInFlags.BUILD_KEY, FlagRuleConfig.simple(FlagState.DENY)),
                        dev.sato.worldprotect.protection.config.RegionSubjectsConfig.empty(),
                        RegionAccessPolicyConfig.defaults()
                )
        ));
    }
}
