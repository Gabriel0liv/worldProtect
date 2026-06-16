package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class BoundsConfigTest {

    @Test
    public void testValidCuboidPasses() {
        BlockPosRef min = new BlockPosRef(0, 0, 0);
        BlockPosRef max = new BlockPosRef(10, 10, 10);
        BoundsConfig bounds = BoundsConfig.cuboid(min, max);

        ConfigValidationResult result = bounds.validate();
        assertTrue(result.isValid());
    }

    @Test
    public void testMinXGreaterThanMaxXFails() {
        BlockPosRef min = new BlockPosRef(11, 0, 0);
        BlockPosRef max = new BlockPosRef(10, 10, 10);
        BoundsConfig bounds = BoundsConfig.cuboid(min, max);

        ConfigValidationResult result = bounds.validate();
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("bounds.min", result.errors().get(0).path());
        assertTrue(result.errors().get(0).message().contains("min.x"));
    }

    @Test
    public void testMinYGreaterThanMaxYFails() {
        BlockPosRef min = new BlockPosRef(0, 11, 0);
        BlockPosRef max = new BlockPosRef(10, 10, 10);
        BoundsConfig bounds = BoundsConfig.cuboid(min, max);

        ConfigValidationResult result = bounds.validate();
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("bounds.min", result.errors().get(0).path());
        assertTrue(result.errors().get(0).message().contains("min.y"));
    }

    @Test
    public void testMinZGreaterThanMaxZFails() {
        BlockPosRef min = new BlockPosRef(0, 0, 11);
        BlockPosRef max = new BlockPosRef(10, 10, 10);
        BoundsConfig bounds = BoundsConfig.cuboid(min, max);

        ConfigValidationResult result = bounds.validate();
        assertFalse(result.isValid());
        assertEquals(1, result.errors().size());
        assertEquals("bounds.min", result.errors().get(0).path());
        assertTrue(result.errors().get(0).message().contains("min.z"));
    }

    @Test
    public void testNoHeightValidationIsPerformed() {
        // Build boundary exceeding standard height limits (e.g. -2000 to 5000)
        BlockPosRef min = new BlockPosRef(0, -2000, 0);
        BlockPosRef max = new BlockPosRef(10, 5000, 10);
        BoundsConfig bounds = BoundsConfig.cuboid(min, max);

        ConfigValidationResult result = bounds.validate();
        assertTrue(result.isValid());
    }

    @Test
    public void testValidateWithPathRouting() {
        BlockPosRef min = new BlockPosRef(5, 0, 0);
        BlockPosRef max = new BlockPosRef(2, 10, 10);
        BoundsConfig bounds = BoundsConfig.cuboid(min, max);

        ConfigValidationResult result = bounds.validate("regions.spawn.bounds");
        assertFalse(result.isValid());
        assertEquals("regions.spawn.bounds.min", result.errors().get(0).path());
    }
}
