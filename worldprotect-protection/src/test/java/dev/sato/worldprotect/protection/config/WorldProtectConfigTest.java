package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagRegistry;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.region.RegionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public final class WorldProtectConfigTest {

    private FlagRegistry registry;
    private DimensionRef overworld;
    private BoundsConfig bounds;

    @BeforeEach
    public void setUp() {
        registry = FlagRegistry.withBuiltIns();
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        bounds = BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10));
    }

    @Test
    public void testValidConfigPasses() {
        RegionConfig r1 = RegionConfig.of(RegionId.of("spawn"), overworld, 10, bounds, Map.of(
                BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY)
        ));
        RegionConfig r2 = RegionConfig.of(RegionId.of("wild"), overworld, 0, bounds, Map.of(
                BuiltInFlags.BUILD_KEY, FlagRuleConfig.simple(FlagState.ALLOW)
        ));

        WorldProtectConfig config = WorldProtectConfig.of(List.of(r1, r2));

        ConfigValidationResult result = config.validate(registry);
        assertTrue(result.isValid());
        assertEquals(0, result.messages().size());
    }

    @Test
    public void testDuplicateRegionIdCreatesError() {
        RegionConfig r1 = RegionConfig.of(RegionId.of("spawn"), overworld, 10, bounds, Map.of(
                BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY)
        ));
        RegionConfig r2 = RegionConfig.of(RegionId.of("spawn"), overworld, 0, bounds, Map.of(
                BuiltInFlags.BUILD_KEY, FlagRuleConfig.simple(FlagState.ALLOW)
        ));

        WorldProtectConfig config = WorldProtectConfig.of(List.of(r1, r2));

        ConfigValidationResult result = config.validate(registry);
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("regions.spawn", result.errors().get(0).path());
    }

    @Test
    public void testEmptyRegionsCreatesWarning() {
        WorldProtectConfig config = WorldProtectConfig.of(List.of());

        ConfigValidationResult result = config.validate(registry);
        assertTrue(result.isValid());
        assertTrue(result.hasWarnings());
        assertEquals(1, result.warnings().size());
        assertEquals("regions", result.warnings().get(0).path());
        assertTrue(result.warnings().get(0).message().contains("No regions configured"));
    }

    @Test
    public void testRegionsListIsImmutableAndDefensivelyCopied() {
        RegionConfig r1 = RegionConfig.of(RegionId.of("spawn"), overworld, 10, bounds, Map.of(
                BuiltInFlags.BREAK_BLOCK_KEY, FlagRuleConfig.simple(FlagState.DENY)
        ));
        List<RegionConfig> list = new ArrayList<>();
        list.add(r1);

        WorldProtectConfig config = WorldProtectConfig.of(list);

        // Modify input list
        list.clear();

        // Config should remain unaffected
        assertEquals(1, config.regions().size());
        assertEquals(r1, config.regions().get(0));

        // Attempting to modify output list must throw UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> {
            config.regions().clear();
        });
    }
}
