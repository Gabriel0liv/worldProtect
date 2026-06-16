package dev.sato.worldprotect.protection.region;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionSetTest {

    @Test
    public void testMatchingAndSorting() {
        DimensionRef overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        DimensionRef nether = new DimensionRef(ResourceRef.of("minecraft", "the_nether"));

        BlockPosRef min = new BlockPosRef(0, 0, 0);
        BlockPosRef max = new BlockPosRef(10, 10, 10);
        BlockPosRef inside = new BlockPosRef(5, 5, 5);

        // Region A: Overworld, Priority 10, ID "region-a"
        Region rA = new CuboidRegion(RegionId.of("region-a"), overworld, min, max, 10);
        // Region B: Overworld, Priority 5, ID "region-b"
        Region rB = new CuboidRegion(RegionId.of("region-b"), overworld, min, max, 5);
        // Region C: Overworld, Priority 10, ID "region-c" (same priority as A, tests ID tie-breaker)
        Region rC = new CuboidRegion(RegionId.of("region-c"), overworld, min, max, 10);
        // Region D: Nether, Priority 100, ID "region-d" (different dimension, should be ignored)
        Region rD = new CuboidRegion(RegionId.of("region-d"), nether, min, max, 100);

        RegionSet set = RegionSet.of(List.of(rA, rB, rC, rD));

        List<Region> matched = set.matching(overworld, inside);

        // Should find rA, rB, rC, but not rD
        assertEquals(3, matched.size());

        // Sort order should be:
        // 1. Priority desc: region-a (10) and region-c (10) should come before region-b (5)
        // 2. ID asc: region-a should come before region-c
        assertEquals("region-a", matched.get(0).getId().getValue());
        assertEquals("region-c", matched.get(1).getId().getValue());
        assertEquals("region-b", matched.get(2).getId().getValue());
    }

    @Test
    public void testIgnoresOutofBounds() {
        DimensionRef overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        BlockPosRef min = new BlockPosRef(0, 0, 0);
        BlockPosRef max = new BlockPosRef(10, 10, 10);
        BlockPosRef outside = new BlockPosRef(11, 5, 5);

        Region region = new CuboidRegion(RegionId.of("spawn"), overworld, min, max, 0);
        RegionSet set = RegionSet.of(List.of(region));

        List<Region> matched = set.matching(overworld, outside);
        assertTrue(matched.isEmpty());
    }
}
