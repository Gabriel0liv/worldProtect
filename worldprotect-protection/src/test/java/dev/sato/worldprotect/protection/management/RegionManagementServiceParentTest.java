package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.config.BoundsConfig;
import dev.sato.worldprotect.protection.config.RegionAccessPolicyConfig;
import dev.sato.worldprotect.protection.config.RegionConfig;
import dev.sato.worldprotect.protection.config.RegionSubjectsConfig;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.region.RegionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionManagementServiceParentTest {

    private RegionManagementService service;
    private DimensionRef overworld;
    private DimensionRef end;
    private BoundsConfig bounds;

    @BeforeEach
    public void setUp() {
        service = RegionManagementService.withBuiltIns();
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        end = new DimensionRef(ResourceRef.of("minecraft", "the_end"));
        bounds = BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(5, 5, 5));
    }

    @Test
    public void testSetParentSucceeds() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of(
                RegionConfig.of(RegionId.of("parent"), overworld, 10, bounds, Map.of()),
                RegionConfig.of(RegionId.of("child"), overworld, 20, bounds, Map.of())
        ));

        RegionManagementResult<WorldProtectConfig> result = service.setParent(
                config,
                SetRegionParentRequest.of(RegionId.of("child"), RegionId.of("parent"))
        );

        assertEquals(RegionManagementStatus.SUCCESS, result.status());
        assertEquals(Optional.of(RegionId.of("parent")), result.value().regions().get(1).parentId());
        assertTrue(config.regions().get(1).parentId().isEmpty(), "Original config must remain unchanged");
    }

    @Test
    public void testSetParentToMissingRegionFails() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of(
                RegionConfig.of(RegionId.of("child"), overworld, 20, bounds, Map.of())
        ));

        RegionManagementResult<WorldProtectConfig> result = service.setParent(
                config,
                SetRegionParentRequest.of(RegionId.of("child"), RegionId.of("missing"))
        );

        assertEquals(RegionManagementStatus.NOT_FOUND, result.status());
    }

    @Test
    public void testSetParentToSelfFails() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of(
                RegionConfig.of(RegionId.of("child"), overworld, 20, bounds, Map.of())
        ));

        RegionManagementResult<WorldProtectConfig> result = service.setParent(
                config,
                SetRegionParentRequest.of(RegionId.of("child"), RegionId.of("child"))
        );

        assertEquals(RegionManagementStatus.VALIDATION_FAILED, result.status());
    }

    @Test
    public void testSetParentDifferentDimensionFails() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of(
                RegionConfig.of(RegionId.of("parent"), end, 10, bounds, Map.of()),
                RegionConfig.of(RegionId.of("child"), overworld, 20, bounds, Map.of())
        ));

        RegionManagementResult<WorldProtectConfig> result = service.setParent(
                config,
                SetRegionParentRequest.of(RegionId.of("child"), RegionId.of("parent"))
        );

        assertEquals(RegionManagementStatus.VALIDATION_FAILED, result.status());
    }

    @Test
    public void testSetParentCycleFails() {
        RegionConfig parent = RegionConfig.of(
                RegionId.of("parent"), overworld, 10, bounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(), Optional.of(RegionId.of("child"))
        );
        RegionConfig child = RegionConfig.of(RegionId.of("child"), overworld, 20, bounds, Map.of());
        WorldProtectConfig config = WorldProtectConfig.of(List.of(parent, child));

        RegionManagementResult<WorldProtectConfig> result = service.setParent(
                config,
                SetRegionParentRequest.of(RegionId.of("child"), RegionId.of("parent"))
        );

        assertEquals(RegionManagementStatus.VALIDATION_FAILED, result.status());
    }

    @Test
    public void testClearParentSucceeds() {
        RegionConfig child = RegionConfig.of(
                RegionId.of("child"), overworld, 20, bounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(), Optional.of(RegionId.of("parent"))
        );
        WorldProtectConfig config = WorldProtectConfig.of(List.of(
                RegionConfig.of(RegionId.of("parent"), overworld, 10, bounds, Map.of()),
                child
        ));

        RegionManagementResult<WorldProtectConfig> result = service.clearParent(
                config,
                ClearRegionParentRequest.of(RegionId.of("child"))
        );

        assertEquals(RegionManagementStatus.SUCCESS, result.status());
        assertTrue(result.value().regions().get(1).parentId().isEmpty());
    }

    @Test
    public void testClearParentWhenAbsentReturnsNoChange() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of(
                RegionConfig.of(RegionId.of("child"), overworld, 20, bounds, Map.of())
        ));

        RegionManagementResult<WorldProtectConfig> result = service.clearParent(
                config,
                ClearRegionParentRequest.of(RegionId.of("child"))
        );

        assertEquals(RegionManagementStatus.NO_CHANGE, result.status());
        assertSame(config, result.value());
    }
}
