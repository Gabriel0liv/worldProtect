package dev.sato.worldprotect.protection.region;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.subject.RegionAccessPolicy;
import dev.sato.worldprotect.protection.subject.RegionSubjects;
import org.junit.jupiter.api.Test;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;

public final class GlobalRegionTest {

    @Test
    public void testContainsAlwaysReturnsTrue() {
        RegionId id = RegionId.of("global_nether");
        DimensionRef nether = new DimensionRef(ResourceRef.of("minecraft", "the_nether"));
        GlobalRegion region = new GlobalRegion(
                id,
                nether,
                -100,
                RegionFlags.empty(),
                RegionSubjects.empty(),
                RegionAccessPolicy.defaults()
        );

        assertTrue(region.contains(new BlockPosRef(0, 0, 0)));
        assertTrue(region.contains(new BlockPosRef(-5000, 256, 12345)));
        assertTrue(region.contains(new BlockPosRef(9999, -64, -9999)));

        assertThrows(NullPointerException.class, () -> region.contains(null));
    }

    @Test
    public void testExposesGetters() {
        RegionId id = RegionId.of("global_end");
        DimensionRef end = new DimensionRef(ResourceRef.of("minecraft", "the_end"));
        RegionFlags flags = RegionFlags.empty();
        RegionSubjects subjects = RegionSubjects.empty();
        RegionAccessPolicy policy = RegionAccessPolicy.defaults();

        GlobalRegion region = new GlobalRegion(id, end, 10, flags, subjects, policy);

        assertEquals(id, region.getId());
        assertEquals(id, region.id());
        assertEquals(end, region.getDimension());
        assertEquals(end, region.dimension());
        assertEquals(10, region.getPriority());
        assertEquals(10, region.priority());
        assertEquals(flags, region.flags());
        assertEquals(flags, region.getFlags());
        assertEquals(subjects, region.subjects());
        assertEquals(subjects, region.getSubjects());
        assertEquals(policy, region.accessPolicy());
        assertEquals(policy, region.getAccessPolicy());
    }

    @Test
    public void testNullChecks() {
        RegionId id = RegionId.of("global");
        DimensionRef dim = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        RegionFlags flags = RegionFlags.empty();
        RegionSubjects subjects = RegionSubjects.empty();
        RegionAccessPolicy policy = RegionAccessPolicy.defaults();

        assertThrows(NullPointerException.class, () -> new GlobalRegion(null, dim, 0, flags, subjects, policy));
        assertThrows(NullPointerException.class, () -> new GlobalRegion(id, null, 0, flags, subjects, policy));
        assertThrows(NullPointerException.class, () -> new GlobalRegion(id, dim, 0, null, subjects, policy));
        assertThrows(NullPointerException.class, () -> new GlobalRegion(id, dim, 0, flags, null, policy));
        assertThrows(NullPointerException.class, () -> new GlobalRegion(id, dim, 0, flags, subjects, null));
    }

    @Test
    public void testEqualsAndHashCode() {
        RegionId id = RegionId.of("global");
        DimensionRef dim = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        RegionFlags flags = RegionFlags.empty();
        RegionSubjects subjects = RegionSubjects.empty();
        RegionAccessPolicy policy = RegionAccessPolicy.defaults();

        GlobalRegion r1 = new GlobalRegion(id, dim, 0, flags, subjects, policy);
        GlobalRegion r2 = new GlobalRegion(id, dim, 0, flags, subjects, policy);
        GlobalRegion r3 = new GlobalRegion(RegionId.of("other"), dim, 0, flags, subjects, policy);

        assertEquals(r1, r2);
        assertNotEquals(r1, r3);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1.hashCode(), r3.hashCode());
        assertNotNull(r1.toString());
    }
}
