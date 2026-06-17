package dev.sato.worldprotect.protection.subject;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionSubjectsTest {

    @Test
    public void testEmptySubjects() {
        RegionSubjects subjects = RegionSubjects.empty();
        assertTrue(subjects.isEmpty());
        assertTrue(subjects.owners().isEmpty());
        assertTrue(subjects.members().isEmpty());
    }

    @Test
    public void testOwnerWinsOverMember() {
        SubjectRef p1 = SubjectRef.player(UUID.randomUUID());
        SubjectRef p2 = SubjectRef.player(UUID.randomUUID());

        // p1 is both in owners and members
        RegionSubjects subjects = RegionSubjects.of(Set.of(p1), Set.of(p1, p2));
        assertFalse(subjects.isEmpty());
        assertTrue(subjects.owners().contains(p1));
        assertFalse(subjects.members().contains(p1)); // Removed from members because owner wins
        assertTrue(subjects.members().contains(p2));

        assertEquals(RegionRole.OWNER, subjects.roleOf(p1));
        assertEquals(RegionRole.MEMBER, subjects.roleOf(p2));
    }

    @Test
    public void testWithOwnerAndMemberTransitions() {
        SubjectRef p1 = SubjectRef.player(UUID.randomUUID());
        SubjectRef p2 = SubjectRef.player(UUID.randomUUID());

        RegionSubjects subjects = RegionSubjects.empty()
                .withOwner(p1)
                .withMember(p2);

        assertTrue(subjects.isOwner(p1));
        assertTrue(subjects.isMember(p2));
        assertEquals(RegionRole.OWNER, subjects.roleOf(p1));
        assertEquals(RegionRole.MEMBER, subjects.roleOf(p2));

        // Upgrading p2 to owner
        RegionSubjects upgraded = subjects.withOwner(p2);
        assertTrue(upgraded.isOwner(p2));
        assertFalse(upgraded.isMember(p2)); // Removed from members

        // Adding p1 to members (should do nothing since p1 is already owner)
        RegionSubjects same = upgraded.withMember(p1);
        assertTrue(same.isOwner(p1));
        assertFalse(same.isMember(p1));

        // Removing
        RegionSubjects removed = upgraded.withoutOwner(p1).withoutMember(p2);
        assertFalse(removed.isOwner(p1));
        assertTrue(removed.isOwner(p2)); // p2 was owner, not member, so withoutMember didn't touch it
    }

    @Test
    public void testImmutabilityAndDefensiveCopies() {
        SubjectRef p1 = SubjectRef.player(UUID.randomUUID());
        Set<SubjectRef> owners = new HashSet<>();
        owners.add(p1);

        RegionSubjects subjects = RegionSubjects.of(owners, Set.of());
        owners.clear(); // Mutate original set

        // Subjects set must remain unchanged
        assertEquals(1, subjects.owners().size());
        assertTrue(subjects.owners().contains(p1));

        // Sets must be unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> subjects.owners().clear());
        assertThrows(UnsupportedOperationException.class, () -> subjects.members().clear());
    }
}
