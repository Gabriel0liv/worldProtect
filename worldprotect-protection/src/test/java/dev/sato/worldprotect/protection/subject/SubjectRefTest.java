package dev.sato.worldprotect.protection.subject;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public final class SubjectRefTest {

    @Test
    public void testPlayerSerialization() {
        UUID uuid = UUID.randomUUID();
        SubjectRef ref = SubjectRef.player(uuid);
        assertEquals(SubjectType.PLAYER, ref.type());
        assertEquals(uuid.toString().toLowerCase(), ref.id());
        assertTrue(ref.isPlayer());
        assertFalse(ref.isGroup());
        assertFalse(ref.isConsole());
        assertFalse(ref.isSystem());
    }

    @Test
    public void testGroupValidId() {
        SubjectRef ref = SubjectRef.group("admin_team-1.2");
        assertEquals(SubjectType.GROUP, ref.type());
        assertEquals("admin_team-1.2", ref.id());
        assertTrue(ref.isGroup());
    }

    @Test
    public void testGroupInvalidIds() {
        // Uppercase
        assertThrows(IllegalArgumentException.class, () -> SubjectRef.group("Admin"));
        // Spaces
        assertThrows(IllegalArgumentException.class, () -> SubjectRef.group("admin team"));
        // Invalid chars
        assertThrows(IllegalArgumentException.class, () -> SubjectRef.group("admin#team"));
        // Blank
        assertThrows(IllegalArgumentException.class, () -> SubjectRef.group("   "));
        // Too long
        String longName = "a".repeat(65);
        assertThrows(IllegalArgumentException.class, () -> SubjectRef.group(longName));
    }

    @Test
    public void testConsoleAndSystem() {
        SubjectRef console = SubjectRef.console();
        assertEquals(SubjectType.CONSOLE, console.type());
        assertEquals("console", console.id());
        assertTrue(console.isConsole());

        SubjectRef system = SubjectRef.system();
        assertEquals(SubjectType.SYSTEM, system.type());
        assertEquals("system", system.id());
        assertTrue(system.isSystem());
    }

    @Test
    public void testOfValidation() {
        assertThrows(NullPointerException.class, () -> SubjectRef.of(null, "id"));
        assertThrows(NullPointerException.class, () -> SubjectRef.of(SubjectType.PLAYER, null));
        assertThrows(IllegalArgumentException.class, () -> SubjectRef.of(SubjectType.PLAYER, "not-a-uuid"));
        assertThrows(IllegalArgumentException.class, () -> SubjectRef.of(SubjectType.CONSOLE, "not-console"));
        assertThrows(IllegalArgumentException.class, () -> SubjectRef.of(SubjectType.SYSTEM, "not-system"));
    }

    @Test
    public void testEqualsAndHashCode() {
        UUID uuid = UUID.randomUUID();
        SubjectRef ref1 = SubjectRef.player(uuid);
        SubjectRef ref2 = SubjectRef.of(SubjectType.PLAYER, uuid.toString().toUpperCase());
        SubjectRef ref3 = SubjectRef.group("admin");

        assertEquals(ref1, ref2);
        assertNotEquals(ref1, ref3);
        assertEquals(ref1.hashCode(), ref2.hashCode());
        assertNotEquals(ref1.hashCode(), ref3.hashCode());
        assertEquals("PLAYER:" + uuid.toString().toLowerCase(), ref1.toString());
    }
}
