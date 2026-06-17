package dev.sato.worldprotect.protection.subject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionRoleTest {

    @Test
    public void testAtLeast() {
        assertTrue(RegionRole.OWNER.atLeast(RegionRole.OWNER));
        assertTrue(RegionRole.OWNER.atLeast(RegionRole.MEMBER));
        assertTrue(RegionRole.OWNER.atLeast(RegionRole.NONE));

        assertFalse(RegionRole.MEMBER.atLeast(RegionRole.OWNER));
        assertTrue(RegionRole.MEMBER.atLeast(RegionRole.MEMBER));
        assertTrue(RegionRole.MEMBER.atLeast(RegionRole.NONE));

        assertFalse(RegionRole.NONE.atLeast(RegionRole.OWNER));
        assertFalse(RegionRole.NONE.atLeast(RegionRole.MEMBER));
        assertTrue(RegionRole.NONE.atLeast(RegionRole.NONE));
    }
}
