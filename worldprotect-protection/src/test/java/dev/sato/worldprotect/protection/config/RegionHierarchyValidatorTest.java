package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.region.RegionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionHierarchyValidatorTest {

    private DimensionRef overworld;
    private DimensionRef nether;
    private BoundsConfig validBounds;

    @BeforeEach
    public void setUp() {
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        nether = new DimensionRef(ResourceRef.of("minecraft", "the_nether"));
        validBounds = BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10));
    }

    @Test
    public void testValidHierarchyPasses() {
        RegionConfig parent = RegionConfig.of(RegionId.of("parent"), overworld, 0, validBounds, Map.of());
        RegionConfig child = RegionConfig.of(
                RegionId.of("child"), overworld, 10, validBounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(),
                Optional.of(RegionId.of("parent"))
        );

        WorldProtectConfig config = WorldProtectConfig.of(List.of(parent, child));
        RegionHierarchyValidator validator = new RegionHierarchyValidator();
        ConfigValidationResult result = validator.validate(config);

        assertTrue(result.isValid());
        assertEquals(0, result.errors().size());
    }

    @Test
    public void testMissingParentFails() {
        RegionConfig child = RegionConfig.of(
                RegionId.of("child"), overworld, 10, validBounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(),
                Optional.of(RegionId.of("missing-parent"))
        );

        WorldProtectConfig config = WorldProtectConfig.of(List.of(child));
        RegionHierarchyValidator validator = new RegionHierarchyValidator();
        ConfigValidationResult result = validator.validate(config);

        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("regions.child.parent", result.errors().get(0).path());
        assertTrue(result.errors().get(0).message().contains("does not exist"));
    }

    @Test
    public void testCrossDimensionParentFails() {
        RegionConfig parent = RegionConfig.of(RegionId.of("parent"), nether, 0, validBounds, Map.of());
        RegionConfig child = RegionConfig.of(
                RegionId.of("child"), overworld, 10, validBounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(),
                Optional.of(RegionId.of("parent"))
        );

        WorldProtectConfig config = WorldProtectConfig.of(List.of(parent, child));
        RegionHierarchyValidator validator = new RegionHierarchyValidator();
        ConfigValidationResult result = validator.validate(config);

        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("regions.child.parent", result.errors().get(0).path());
        assertTrue(result.errors().get(0).message().contains("dimension"));
    }

    @Test
    public void testCircularDependencyDirectFails() {
        RegionConfig rA = RegionConfig.of(
                RegionId.of("a"), overworld, 0, validBounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(),
                Optional.of(RegionId.of("b"))
        );
        RegionConfig rB = RegionConfig.of(
                RegionId.of("b"), overworld, 0, validBounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(),
                Optional.of(RegionId.of("a"))
        );

        WorldProtectConfig config = WorldProtectConfig.of(List.of(rA, rB));
        RegionHierarchyValidator validator = new RegionHierarchyValidator();
        ConfigValidationResult result = validator.validate(config);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(m -> m.path().startsWith("regions.") && m.message().contains("Circular inheritance")));
    }

    @Test
    public void testCircularDependencyIndirectFails() {
        RegionConfig rA = RegionConfig.of(
                RegionId.of("a"), overworld, 0, validBounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(),
                Optional.of(RegionId.of("b"))
        );
        RegionConfig rB = RegionConfig.of(
                RegionId.of("b"), overworld, 0, validBounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(),
                Optional.of(RegionId.of("c"))
        );
        RegionConfig rC = RegionConfig.of(
                RegionId.of("c"), overworld, 0, validBounds, Map.of(),
                RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(),
                Optional.of(RegionId.of("a"))
        );

        WorldProtectConfig config = WorldProtectConfig.of(List.of(rA, rB, rC));
        RegionHierarchyValidator validator = new RegionHierarchyValidator();
        ConfigValidationResult result = validator.validate(config);

        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(m -> m.path().startsWith("regions.") && m.message().contains("Circular inheritance")));
    }
}
