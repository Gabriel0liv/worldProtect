package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.config.BoundsConfig;
import dev.sato.worldprotect.protection.config.RegionConfig;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.region.RegionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionManagementServiceCreateDeleteTest {

    private RegionManagementService service;
    private DimensionRef overworld;
    private DimensionRef nether;
    private BoundsConfig bounds;

    @BeforeEach
    public void setUp() {
        service = RegionManagementService.withBuiltIns();
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        nether = new DimensionRef(ResourceRef.of("minecraft", "the_nether"));
        bounds = BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10));
    }

    @Test
    public void testCreateRegionSucceeds() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of());

        RegionManagementResult<WorldProtectConfig> result = service.createRegion(
                config,
                CreateRegionRequest.of(RegionId.of("spawn"), overworld, 100, bounds)
        );

        assertEquals(RegionManagementStatus.SUCCESS, result.status());
        assertNotNull(result.value());
        assertEquals(1, result.value().regions().size());
        assertTrue(config.regions().isEmpty(), "Original config must remain unchanged");
    }

    @Test
    public void testCreateDuplicateRegionReturnsAlreadyExists() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of(RegionConfig.of(
                RegionId.of("spawn"), overworld, 100, bounds, java.util.Map.of()
        )));

        RegionManagementResult<WorldProtectConfig> result = service.createRegion(
                config,
                CreateRegionRequest.of(RegionId.of("spawn"), overworld, 50, bounds)
        );

        assertEquals(RegionManagementStatus.ALREADY_EXISTS, result.status());
        assertTrue(result.isFailure());
    }

    @Test
    public void testCreateWithInvalidParentReturnsNotFound() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of());

        RegionManagementResult<WorldProtectConfig> result = service.createRegion(
                config,
                CreateRegionRequest.of(RegionId.of("child"), overworld, 0, bounds, Optional.of(RegionId.of("missing")))
        );

        assertEquals(RegionManagementStatus.NOT_FOUND, result.status());
    }

    @Test
    public void testCreateWithParentSameDimensionSucceeds() {
        RegionConfig parent = RegionConfig.of(RegionId.of("parent"), overworld, 10, bounds, java.util.Map.of());
        WorldProtectConfig config = WorldProtectConfig.of(List.of(parent));

        RegionManagementResult<WorldProtectConfig> result = service.createRegion(
                config,
                CreateRegionRequest.of(RegionId.of("child"), overworld, 20, bounds, Optional.of(RegionId.of("parent")))
        );

        assertEquals(RegionManagementStatus.SUCCESS, result.status());
        assertEquals(Optional.of(RegionId.of("parent")), result.value().regions().get(1).parentId());
    }

    @Test
    public void testCreateWithParentDifferentDimensionFails() {
        RegionConfig parent = RegionConfig.of(RegionId.of("parent"), nether, 10, bounds, java.util.Map.of());
        WorldProtectConfig config = WorldProtectConfig.of(List.of(parent));

        RegionManagementResult<WorldProtectConfig> result = service.createRegion(
                config,
                CreateRegionRequest.of(RegionId.of("child"), overworld, 20, bounds, Optional.of(RegionId.of("parent")))
        );

        assertEquals(RegionManagementStatus.VALIDATION_FAILED, result.status());
    }

    @Test
    public void testDeleteExistingRegionSucceeds() {
        RegionConfig region = RegionConfig.of(RegionId.of("spawn"), overworld, 100, bounds, java.util.Map.of());
        WorldProtectConfig config = WorldProtectConfig.of(List.of(region));

        RegionManagementResult<WorldProtectConfig> result = service.deleteRegion(config, DeleteRegionRequest.of(RegionId.of("spawn")));

        assertEquals(RegionManagementStatus.SUCCESS, result.status());
        assertTrue(result.value().regions().isEmpty());
        assertEquals(1, config.regions().size(), "Original config must remain unchanged");
    }

    @Test
    public void testDeleteMissingRegionReturnsNotFound() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of());

        RegionManagementResult<WorldProtectConfig> result = service.deleteRegion(config, DeleteRegionRequest.of(RegionId.of("missing")));

        assertEquals(RegionManagementStatus.NOT_FOUND, result.status());
    }

    @Test
    public void testDeleteParentRegionWithChildrenReturnsConflict() {
        RegionConfig parent = RegionConfig.of(RegionId.of("parent"), overworld, 10, bounds, java.util.Map.of());
        RegionConfig child = RegionConfig.of(
                RegionId.of("child"), overworld, 20, bounds, java.util.Map.of(),
                dev.sato.worldprotect.protection.config.RegionSubjectsConfig.empty(),
                dev.sato.worldprotect.protection.config.RegionAccessPolicyConfig.defaults(),
                Optional.of(RegionId.of("parent"))
        );
        WorldProtectConfig config = WorldProtectConfig.of(List.of(parent, child));

        RegionManagementResult<WorldProtectConfig> result = service.deleteRegion(config, DeleteRegionRequest.of(RegionId.of("parent")));

        assertEquals(RegionManagementStatus.CONFLICT, result.status());
        assertTrue(result.diagnostics().errors().get(0).message().contains("child"));
    }

    @Test
    public void testSetBoundsAndPriorityPreserveRegionMetadata() {
        RegionConfig region = RegionConfig.of(RegionId.of("spawn"), overworld, 100, bounds, java.util.Map.of());
        WorldProtectConfig config = WorldProtectConfig.of(List.of(region));
        BoundsConfig newBounds = BoundsConfig.cuboid(new BlockPosRef(-5, 0, -5), new BlockPosRef(5, 10, 5));

        RegionManagementResult<WorldProtectConfig> boundsResult = service.setBounds(
                config,
                SetRegionBoundsRequest.of(RegionId.of("spawn"), newBounds)
        );
        RegionManagementResult<WorldProtectConfig> priorityResult = service.setPriority(
                config,
                SetRegionPriorityRequest.of(RegionId.of("spawn"), 250)
        );

        assertEquals(RegionManagementStatus.SUCCESS, boundsResult.status());
        assertEquals(newBounds, boundsResult.value().regions().get(0).bounds());
        assertEquals(100, boundsResult.value().regions().get(0).priority());
        assertEquals(RegionManagementStatus.SUCCESS, priorityResult.status());
        assertEquals(250, priorityResult.value().regions().get(0).priority());
        assertEquals(bounds, priorityResult.value().regions().get(0).bounds());
    }

    @Test
    public void testInvalidBoundsFails() {
        RegionConfig region = RegionConfig.of(RegionId.of("spawn"), overworld, 100, bounds, java.util.Map.of());
        WorldProtectConfig config = WorldProtectConfig.of(List.of(region));
        BoundsConfig invalidBounds = BoundsConfig.cuboid(new BlockPosRef(10, 0, 0), new BlockPosRef(0, 10, 10));

        RegionManagementResult<WorldProtectConfig> result = service.setBounds(
                config,
                SetRegionBoundsRequest.of(RegionId.of("spawn"), invalidBounds)
        );

        assertEquals(RegionManagementStatus.VALIDATION_FAILED, result.status());
    }

    @Test
    public void testMissingRegionOnUpdateReturnsNotFound() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of());

        RegionManagementResult<WorldProtectConfig> result = service.setPriority(
                config,
                SetRegionPriorityRequest.of(RegionId.of("missing"), 1)
        );

        assertEquals(RegionManagementStatus.NOT_FOUND, result.status());
    }
}
