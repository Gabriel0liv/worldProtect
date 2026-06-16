package dev.sato.worldprotect.protection.region;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class CuboidRegionTest {

    @Test
    public void testContains() {
        RegionId id = new RegionId("test-region");
        DimensionRef dim = new DimensionRef(ResourceRef.parse("minecraft:overworld"));
        BlockPosRef pos1 = new BlockPosRef(-10, 60, -10);
        BlockPosRef pos2 = new BlockPosRef(10, 80, 10);
        
        CuboidRegion region = new CuboidRegion(id, dim, pos1, pos2, 100);

        // Center point
        assertTrue(region.contains(new BlockPosRef(0, 70, 0)));

        // Corner boundaries
        assertTrue(region.contains(new BlockPosRef(-10, 60, -10)));
        assertTrue(region.contains(new BlockPosRef(10, 80, 10)));

        // Edge boundaries
        assertTrue(region.contains(new BlockPosRef(10, 70, 5)));

        // Outside points
        assertFalse(region.contains(new BlockPosRef(-11, 70, 0)));
        assertFalse(region.contains(new BlockPosRef(0, 59, 0)));
        assertFalse(region.contains(new BlockPosRef(0, 70, 11)));
    }
}
