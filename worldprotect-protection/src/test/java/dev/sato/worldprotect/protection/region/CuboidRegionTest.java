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

    @Test
    public void testParentIdDefaultsToEmpty() {
        RegionId id = new RegionId("test-region");
        DimensionRef dim = new DimensionRef(ResourceRef.parse("minecraft:overworld"));
        CuboidRegion region = new CuboidRegion(id, dim, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 100);
        assertFalse(region.parentId().isPresent());
        assertFalse(region.getParentId().isPresent());
    }

    @Test
    public void testParentIdPreserved() {
        RegionId id = new RegionId("test-region");
        RegionId parent = new RegionId("parent-region");
        DimensionRef dim = new DimensionRef(ResourceRef.parse("minecraft:overworld"));
        CuboidRegion region = new CuboidRegion(
                id, dim, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 100,
                RegionFlags.empty(), dev.sato.worldprotect.protection.subject.RegionSubjects.empty(),
                dev.sato.worldprotect.protection.subject.RegionAccessPolicy.defaults(), java.util.Optional.of(parent)
        );
        assertTrue(region.parentId().isPresent());
        assertEquals(parent, region.parentId().get());
    }

    @Test
    public void testSelfParentIdThrows() {
        RegionId id = new RegionId("test-region");
        DimensionRef dim = new DimensionRef(ResourceRef.parse("minecraft:overworld"));
        assertThrows(IllegalArgumentException.class, () -> {
            new CuboidRegion(
                    id, dim, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 100,
                    RegionFlags.empty(), dev.sato.worldprotect.protection.subject.RegionSubjects.empty(),
                    dev.sato.worldprotect.protection.subject.RegionAccessPolicy.defaults(), java.util.Optional.of(id)
            );
        });
    }

    @Test
    public void testEqualsAndHashCodeIncludeParentId() {
        RegionId id = new RegionId("test-region");
        DimensionRef dim = new DimensionRef(ResourceRef.parse("minecraft:overworld"));
        CuboidRegion r1 = new CuboidRegion(
                id, dim, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 100,
                RegionFlags.empty(), dev.sato.worldprotect.protection.subject.RegionSubjects.empty(),
                dev.sato.worldprotect.protection.subject.RegionAccessPolicy.defaults(), java.util.Optional.of(RegionId.of("parent1"))
        );
        CuboidRegion r2 = new CuboidRegion(
                id, dim, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 100,
                RegionFlags.empty(), dev.sato.worldprotect.protection.subject.RegionSubjects.empty(),
                dev.sato.worldprotect.protection.subject.RegionAccessPolicy.defaults(), java.util.Optional.of(RegionId.of("parent1"))
        );
        CuboidRegion r3 = new CuboidRegion(
                id, dim, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 100,
                RegionFlags.empty(), dev.sato.worldprotect.protection.subject.RegionSubjects.empty(),
                dev.sato.worldprotect.protection.subject.RegionAccessPolicy.defaults(), java.util.Optional.of(RegionId.of("parent2"))
        );

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }
}
