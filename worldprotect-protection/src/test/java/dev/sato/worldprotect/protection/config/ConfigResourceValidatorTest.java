package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.minecraft.registry.ResourceKind;
import dev.sato.worldprotect.minecraft.registry.ResourceRegistryView;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.region.RegionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public final class ConfigResourceValidatorTest {

    private ConfigResourceValidator validator;
    private ResourceRegistryView registryView;
    private DimensionRef overworld;
    private BoundsConfig bounds;

    @BeforeEach
    public void setUp() {
        validator = new ConfigResourceValidator();
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        bounds = BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10));

        // Create a self-contained local stub implementation of ResourceRegistryView
        registryView = new ResourceRegistryView() {
            @Override
            public boolean namespaceLoaded(String namespace) {
                return "minecraft".equals(namespace) || "create".equals(namespace);
            }

            @Override
            public boolean exists(ResourceKind kind, ResourceRef id) {
                // Not used by ConfigResourceValidator under EXACT namespace-only validation
                return false;
            }

            @Override
            public Set<String> loadedNamespaces() {
                return Set.of("minecraft", "create");
            }

            @Override
            public Set<ResourceRef> ids(ResourceKind kind) {
                return Set.of();
            }
        };
    }

    @Test
    public void testExactSelectorWithLoadedNamespacePasses() {
        RegionConfig region = RegionConfig.of(RegionId.of("spawn"), overworld, 10, bounds, Map.of(
                BuiltInFlags.USE_ITEM_KEY, FlagRuleConfig.conditional(FlagState.DENY, List.of("create:wrench"), List.of())
        ));
        WorldProtectConfig config = WorldProtectConfig.of(List.of(region));

        ConfigValidationResult result = validator.validateResources(config, registryView);
        assertTrue(result.isValid());
        assertEquals(0, result.messages().size());
    }

    @Test
    public void testMissingNamespaceExactSelectorFails() {
        RegionConfig region = RegionConfig.of(RegionId.of("spawn"), overworld, 10, bounds, Map.of(
                BuiltInFlags.USE_ITEM_KEY, FlagRuleConfig.conditional(FlagState.DENY, List.of("unknown_mod:wrench"), List.of())
        ));
        WorldProtectConfig config = WorldProtectConfig.of(List.of(region));

        ConfigValidationResult result = validator.validateResources(config, registryView);
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("regions.spawn.flags.use-item.allow[0]", result.errors().get(0).path());
        assertTrue(result.errors().get(0).message().contains("unknown_mod"));
    }

    @Test
    public void testNamespaceWildcardWithMissingNamespaceFails() {
        RegionConfig region = RegionConfig.of(RegionId.of("spawn"), overworld, 10, bounds, Map.of(
                BuiltInFlags.USE_ITEM_KEY, FlagRuleConfig.conditional(FlagState.DENY, List.of("unknown_mod:*"), List.of())
        ));
        WorldProtectConfig config = WorldProtectConfig.of(List.of(region));

        ConfigValidationResult result = validator.validateResources(config, registryView);
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("regions.spawn.flags.use-item.allow[0]", result.errors().get(0).path());
        assertTrue(result.errors().get(0).message().contains("unknown_mod"));
    }

    @Test
    public void testGlobalWildcardPasses() {
        RegionConfig region = RegionConfig.of(RegionId.of("spawn"), overworld, 10, bounds, Map.of(
                BuiltInFlags.USE_ITEM_KEY, FlagRuleConfig.conditional(FlagState.DENY, List.of("*"), List.of())
        ));
        WorldProtectConfig config = WorldProtectConfig.of(List.of(region));

        ConfigValidationResult result = validator.validateResources(config, registryView);
        assertTrue(result.isValid());
        assertEquals(0, result.messages().size());
    }

    @Test
    public void testTagSelectorReturnsWarningNotError() {
        RegionConfig region = RegionConfig.of(RegionId.of("spawn"), overworld, 10, bounds, Map.of(
                BuiltInFlags.USE_ITEM_KEY, FlagRuleConfig.conditional(FlagState.DENY, List.of("#forge:ores"), List.of())
        ));
        WorldProtectConfig config = WorldProtectConfig.of(List.of(region));

        ConfigValidationResult result = validator.validateResources(config, registryView);
        assertTrue(result.isValid());
        assertTrue(result.hasWarnings());
        assertEquals(1, result.warnings().size());
        assertEquals("regions.spawn.flags.use-item.allow[0]", result.warnings().get(0).path());
        assertTrue(result.warnings().get(0).message().contains("Tag selector parsed but tag membership validation is not implemented yet"));
    }
}
