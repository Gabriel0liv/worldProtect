package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.core.actor.ActorType;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.config.BoundsConfig;
import dev.sato.worldprotect.protection.config.ConfigToDomainMapper;
import dev.sato.worldprotect.protection.config.FlagRuleConfig;
import dev.sato.worldprotect.protection.config.RegionAccessPolicyConfig;
import dev.sato.worldprotect.protection.config.RegionConfig;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.query.CauseChain;
import dev.sato.worldprotect.protection.query.ProtectionAction;
import dev.sato.worldprotect.protection.query.ProtectionCause;
import dev.sato.worldprotect.protection.query.ProtectionQuery;
import dev.sato.worldprotect.protection.query.ProtectionTarget;
import dev.sato.worldprotect.protection.region.RegionId;
import dev.sato.worldprotect.protection.resolver.ProtectionResolver;
import dev.sato.worldprotect.protection.subject.RegionGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionManagementServiceFlagTest {

    private RegionManagementService service;
    private ConfigToDomainMapper mapper;
    private ProtectionResolver resolver;
    private DimensionRef overworld;
    private BoundsConfig bounds;

    @BeforeEach
    public void setUp() {
        service = RegionManagementService.withBuiltIns();
        mapper = new ConfigToDomainMapper();
        resolver = new ProtectionResolver();
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        bounds = BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10));
    }

    @Test
    public void testSetSimpleFlagSucceedsAndMapsToResolver() {
        WorldProtectConfig config = baseConfig();

        RegionManagementResult<WorldProtectConfig> result = service.setFlag(
                config,
                SetRegionFlagRequest.of(RegionId.of("spawn"), BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY))
        );

        assertEquals(RegionManagementStatus.SUCCESS, result.status());
        ProtectionQuery query = new ProtectionQuery(
                new Actor("player", ActorType.PLAYER),
                ProtectionAction.BLOCK_BREAK,
                CauseChain.of(ProtectionCause.player()),
                ProtectionTarget.unknown(),
                overworld,
                new BlockPosRef(5, 5, 5)
        );
        assertTrue(resolver.resolve(query, mapper.toRegionSet(result.value())).isDenied());
    }

    @Test
    public void testSetConditionalFlagSucceeds() {
        WorldProtectConfig config = baseConfig();

        RegionManagementResult<WorldProtectConfig> result = service.setFlag(
                config,
                SetRegionFlagRequest.of(
                        RegionId.of("spawn"),
                        BuiltInFlags.USE_ITEM_KEY,
                        FlagRuleConfig.conditional(FlagState.DENY, List.of("create:wrench"), List.of())
                )
        );

        assertEquals(RegionManagementStatus.SUCCESS, result.status());
        assertEquals(1, result.value().regions().get(0).flags().size());
    }

    @Test
    public void testSetGroupScopedFlagSucceeds() {
        WorldProtectConfig config = baseConfig();

        RegionManagementResult<WorldProtectConfig> result = service.setFlag(
                config,
                SetRegionFlagRequest.of(
                        RegionId.of("spawn"),
                        BuiltInFlags.OPEN_CONTAINER_KEY,
                        FlagRuleConfig.simple(FlagState.DENY, RegionGroup.MEMBERS)
                )
        );

        assertEquals(RegionManagementStatus.SUCCESS, result.status());
        assertEquals(RegionGroup.MEMBERS, result.value().regions().get(0).flags().get(BuiltInFlags.OPEN_CONTAINER_KEY).group());
    }

    @Test
    public void testSetUnknownFlagFails() {
        RegionManagementResult<WorldProtectConfig> result = service.setFlag(
                baseConfig(),
                SetRegionFlagRequest.of(
                        RegionId.of("spawn"),
                        FlagKey.of("unknown-flag"),
                        FlagRuleConfig.simple(FlagState.DENY)
                )
        );

        assertEquals(RegionManagementStatus.VALIDATION_FAILED, result.status());
    }

    @Test
    public void testClearExistingFlagSucceedsAndMissingReturnsNoChange() {
        RegionConfig region = RegionConfig.of(
                RegionId.of("spawn"), overworld, 100, bounds,
                Map.of(BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY))
        );
        WorldProtectConfig config = WorldProtectConfig.of(List.of(region));

        RegionManagementResult<WorldProtectConfig> cleared = service.clearFlag(
                config,
                ClearRegionFlagRequest.of(RegionId.of("spawn"), BuiltInFlags.BREAK_BLOCK_KEY)
        );
        RegionManagementResult<WorldProtectConfig> missing = service.clearFlag(
                config,
                ClearRegionFlagRequest.of(RegionId.of("spawn"), BuiltInFlags.PLACE_BLOCK_KEY)
        );

        assertEquals(RegionManagementStatus.SUCCESS, cleared.status());
        assertTrue(cleared.value().regions().get(0).flags().isEmpty());
        assertEquals(RegionManagementStatus.NO_CHANGE, missing.status());
    }

    @Test
    public void testSetFlagPreservesAccessPolicyAndSetAccessPolicyPreservesFlags() {
        RegionConfig region = RegionConfig.of(
                RegionId.of("spawn"),
                overworld,
                100,
                bounds,
                Map.of(BuiltInFlags.BUILD_KEY, FlagRuleConfig.simple(FlagState.DENY)),
                dev.sato.worldprotect.protection.config.RegionSubjectsConfig.empty(),
                RegionAccessPolicyConfig.of(true, false, List.of("build"), List.of())
        );
        WorldProtectConfig config = WorldProtectConfig.of(List.of(region));

        RegionManagementResult<WorldProtectConfig> flagResult = service.setFlag(
                config,
                SetRegionFlagRequest.of(RegionId.of("spawn"), BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.ALLOW))
        );
        RegionManagementResult<WorldProtectConfig> accessResult = service.setAccessPolicy(
                config,
                SetRegionAccessPolicyRequest.of(RegionId.of("spawn"), RegionAccessPolicyConfig.of(false, true, List.of("build"), List.of("break-block")))
        );

        assertEquals(RegionManagementStatus.SUCCESS, flagResult.status());
        assertEquals(RegionManagementStatus.SUCCESS, accessResult.status());
        assertEquals(Boolean.TRUE, flagResult.value().regions().get(0).accessPolicyConfig().ownersBypass());
        assertTrue(accessResult.value().regions().get(0).flags().containsKey(BuiltInFlags.BUILD_KEY));
    }

    @Test
    public void testInvalidAccessPolicyFlagFails() {
        RegionManagementResult<WorldProtectConfig> result = service.setAccessPolicy(
                baseConfig(),
                SetRegionAccessPolicyRequest.of(
                        RegionId.of("spawn"),
                        RegionAccessPolicyConfig.of(true, false, List.of("not-a-real-flag"), List.of())
                )
        );

        assertEquals(RegionManagementStatus.VALIDATION_FAILED, result.status());
    }

    private WorldProtectConfig baseConfig() {
        return WorldProtectConfig.of(List.of(
                RegionConfig.of(RegionId.of("spawn"), overworld, 100, bounds, Map.of())
        ));
    }
}
