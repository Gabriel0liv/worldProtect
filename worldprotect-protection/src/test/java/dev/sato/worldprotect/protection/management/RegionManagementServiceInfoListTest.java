package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.config.BoundsConfig;
import dev.sato.worldprotect.protection.config.RegionAccessPolicyConfig;
import dev.sato.worldprotect.protection.config.RegionConfig;
import dev.sato.worldprotect.protection.config.RegionSubjectsConfig;
import dev.sato.worldprotect.protection.config.SubjectRefConfig;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.region.RegionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionManagementServiceInfoListTest {

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
    public void testInfoExistingRegionSucceedsAndMissingFails() {
        WorldProtectConfig config = config();

        RegionManagementResult<RegionInfoView> info = service.info(config, RegionInfoRequest.of(RegionId.of("spawn")));
        RegionManagementResult<RegionInfoView> missing = service.info(config, RegionInfoRequest.of(RegionId.of("missing")));

        assertEquals(RegionManagementStatus.SUCCESS, info.status());
        assertEquals("spawn", info.value().regionId().getValue());
        assertEquals(1, info.value().flagCount());
        assertEquals(1, info.value().ownerCount());
        assertEquals(1, info.value().memberCount());
        assertTrue(info.value().accessPolicySummary().contains("ownersBypass"));
        assertEquals(RegionManagementStatus.NOT_FOUND, missing.status());
    }

    @Test
    public void testListAllAndByDimensionSucceed() {
        WorldProtectConfig config = config();

        RegionManagementResult<RegionListView> all = service.list(config, RegionListRequest.all());
        RegionManagementResult<RegionListView> filtered = service.list(config, RegionListRequest.of(Optional.of(overworld)));

        assertEquals(RegionManagementStatus.SUCCESS, all.status());
        assertEquals(2, all.value().size());
        assertEquals(1, filtered.value().size());
        assertEquals("spawn", filtered.value().regions().get(0).regionId().getValue());
    }

    @Test
    public void testViewObjectsAreDefensivelyCopied() {
        RegionInfoView info = service.info(config(), RegionInfoRequest.of(RegionId.of("spawn"))).value();
        RegionListView list = service.list(config(), RegionListRequest.all()).value();

        assertThrows(UnsupportedOperationException.class, () -> info.flags().clear());
        assertThrows(UnsupportedOperationException.class, () -> info.owners().add(SubjectRefConfig.of("system")));
        assertThrows(UnsupportedOperationException.class, () -> list.regions().clear());
    }

    private WorldProtectConfig config() {
        RegionConfig spawn = RegionConfig.of(
                RegionId.of("spawn"),
                overworld,
                100,
                bounds,
                Map.of(BuiltInFlags.BUILD_KEY, dev.sato.worldprotect.protection.config.FlagRuleConfig.simple(FlagState.DENY)),
                RegionSubjectsConfig.of(
                        List.of(SubjectRefConfig.of("player:" + UUID.randomUUID())),
                        List.of(SubjectRefConfig.of("player:" + UUID.randomUUID()))
                ),
                RegionAccessPolicyConfig.of(true, false, List.of("build"), List.of())
        );
        RegionConfig netherHub = RegionConfig.of(RegionId.of("nether_hub"), nether, 50, bounds, Map.of());
        return WorldProtectConfig.of(List.of(spawn, netherHub));
    }
}
