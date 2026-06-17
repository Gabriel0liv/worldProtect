package dev.sato.worldprotect.protection.subject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionGroupTest {

    @Test
    public void testParseValidKeys() {
        assertEquals(RegionGroup.ALL, RegionGroup.parse("all"));
        assertEquals(RegionGroup.OWNERS, RegionGroup.parse("owners"));
        assertEquals(RegionGroup.MEMBERS, RegionGroup.parse("members"));
        assertEquals(RegionGroup.NONOWNERS, RegionGroup.parse("nonowners"));
        assertEquals(RegionGroup.NONMEMBERS, RegionGroup.parse("nonmembers"));
        
        // Case-insensitivity and trim
        assertEquals(RegionGroup.ALL, RegionGroup.parse("  ALL  "));
        assertEquals(RegionGroup.NONMEMBERS, RegionGroup.parse("NonMembers"));
    }

    @Test
    public void testParseInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> RegionGroup.parse("invalid"));
        assertThrows(NullPointerException.class, () -> RegionGroup.parse(null));
    }

    @Test
    public void testConfigKey() {
        assertEquals("all", RegionGroup.ALL.configKey());
        assertEquals("owners", RegionGroup.OWNERS.configKey());
        assertEquals("members", RegionGroup.MEMBERS.configKey());
        assertEquals("nonowners", RegionGroup.NONOWNERS.configKey());
        assertEquals("nonmembers", RegionGroup.NONMEMBERS.configKey());
    }

    @Test
    public void testMatches() {
        // ALL matches everything
        assertTrue(RegionGroup.ALL.matches(RegionRole.OWNER));
        assertTrue(RegionGroup.ALL.matches(RegionRole.MEMBER));
        assertTrue(RegionGroup.ALL.matches(RegionRole.NONE));

        // OWNERS matches owner only
        assertTrue(RegionGroup.OWNERS.matches(RegionRole.OWNER));
        assertFalse(RegionGroup.OWNERS.matches(RegionRole.MEMBER));
        assertFalse(RegionGroup.OWNERS.matches(RegionRole.NONE));

        // MEMBERS matches owner and member
        assertTrue(RegionGroup.MEMBERS.matches(RegionRole.OWNER));
        assertTrue(RegionGroup.MEMBERS.matches(RegionRole.MEMBER));
        assertFalse(RegionGroup.MEMBERS.matches(RegionRole.NONE));

        // NONOWNERS matches member and none, but not owner
        assertFalse(RegionGroup.NONOWNERS.matches(RegionRole.OWNER));
        assertTrue(RegionGroup.NONOWNERS.matches(RegionRole.MEMBER));
        assertTrue(RegionGroup.NONOWNERS.matches(RegionRole.NONE));

        // NONMEMBERS matches none only, not owner/member
        assertFalse(RegionGroup.NONMEMBERS.matches(RegionRole.OWNER));
        assertFalse(RegionGroup.NONMEMBERS.matches(RegionRole.MEMBER));
        assertTrue(RegionGroup.NONMEMBERS.matches(RegionRole.NONE));
    }
}
