package dev.sato.worldprotect.protection.region;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.subject.RegionAccessPolicy;
import dev.sato.worldprotect.protection.subject.RegionSubjects;
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

    @Test
    public void testGlobalRegionMatchingAndSorting() {
        DimensionRef overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        DimensionRef nether = new DimensionRef(ResourceRef.of("minecraft", "the_nether"));

        BlockPosRef pos1 = new BlockPosRef(5, 5, 5);
        BlockPosRef pos2 = new BlockPosRef(1000, 1000, 1000);

        // Global Region A: Overworld, priority 0
        Region rA = new GlobalRegion(
                RegionId.of("global-overworld"),
                overworld,
                0,
                RegionFlags.empty(),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );
        // Cuboid Region B: Overworld, priority 10
        Region rB = new CuboidRegion(
                RegionId.of("cuboid-overworld"),
                overworld,
                new BlockPosRef(0, 0, 0),
                new BlockPosRef(10, 10, 10),
                10
        );
        // Global Region C: Nether, priority 100
        Region rC = new GlobalRegion(
                RegionId.of("global-nether"),
                nether,
                100,
                RegionFlags.empty(),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        RegionSet set = RegionSet.of(List.of(rA, rB, rC));

        // Query Overworld inside cuboid: both rB (priority 10) and rA (priority 0) match
        List<Region> matchedInside = set.matching(overworld, pos1);
        assertEquals(2, matchedInside.size());
        assertEquals("cuboid-overworld", matchedInside.get(0).getId().getValue());
        assertEquals("global-overworld", matchedInside.get(1).getId().getValue());

        // Query Overworld outside cuboid: only rA (priority 0) matches
        List<Region> matchedOutside = set.matching(overworld, pos2);
        assertEquals(1, matchedOutside.size());
        assertEquals("global-overworld", matchedOutside.get(0).getId().getValue());

        // Query Nether: only rC matches
        List<Region> matchedNether = set.matching(nether, pos2);
        assertEquals(1, matchedNether.size());
        assertEquals("global-nether", matchedNether.get(0).getId().getValue());
    }

    @Test
    public void testDuplicateRegionIdsThrow() {
        DimensionRef overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        Region r1 = new CuboidRegion(RegionId.of("spawn"), overworld, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 10);
        Region r2 = new CuboidRegion(RegionId.of("spawn"), overworld, new BlockPosRef(20, 0, 20), new BlockPosRef(30, 10, 30), 20);

        assertThrows(IllegalArgumentException.class, () -> RegionSet.of(List.of(r1, r2)));
    }

    @Test
    public void testFindByIdAndContainsId() {
        DimensionRef overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        Region r1 = new CuboidRegion(RegionId.of("spawn"), overworld, new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10), 10);
        RegionSet set = RegionSet.of(List.of(r1));

        assertTrue(set.containsId(RegionId.of("spawn")));
        assertFalse(set.containsId(RegionId.of("other")));

        assertTrue(set.findById(RegionId.of("spawn")).isPresent());
        assertEquals(r1, set.findById(RegionId.of("spawn")).get());
        assertFalse(set.findById(RegionId.of("other")).isPresent());
    }
}
