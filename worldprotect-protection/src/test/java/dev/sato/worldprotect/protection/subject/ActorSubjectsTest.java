package dev.sato.worldprotect.protection.subject;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.core.actor.ActorType;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public final class ActorSubjectsTest {

    @Test
    public void testPlayerActorSubjects() {
        Actor actor = new Actor("player_id", ActorType.PLAYER);
        UUID uuid = UUID.randomUUID();
        ActorSubjects actorSubjects = ActorSubjects.player(actor, uuid, List.of("vip", "admin"));

        assertEquals(actor, actorSubjects.actor());
        assertEquals(3, actorSubjects.subjects().size());
        assertTrue(actorSubjects.contains(SubjectRef.player(uuid)));
        assertTrue(actorSubjects.contains(SubjectRef.group("vip")));
        assertTrue(actorSubjects.contains(SubjectRef.group("admin")));

        assertTrue(actorSubjects.hasAny(List.of(SubjectRef.group("vip"))));
        assertFalse(actorSubjects.hasAny(List.of(SubjectRef.group("mod"))));
    }

    @Test
    public void testConsoleActorSubjects() {
        Actor actor = new Actor("console", ActorType.SYSTEM);
        ActorSubjects actorSubjects = ActorSubjects.console(actor);

        assertEquals(actor, actorSubjects.actor());
        assertEquals(1, actorSubjects.subjects().size());
        assertTrue(actorSubjects.contains(SubjectRef.console()));
    }

    @Test
    public void testSystemActorSubjects() {
        Actor actor = new Actor("system", ActorType.SYSTEM);
        ActorSubjects actorSubjects = ActorSubjects.system(actor);

        assertEquals(actor, actorSubjects.actor());
        assertEquals(1, actorSubjects.subjects().size());
        assertTrue(actorSubjects.contains(SubjectRef.system()));
    }

    @Test
    public void testValidation() {
        assertThrows(NullPointerException.class, () -> ActorSubjects.of(null, Set.of(SubjectRef.console())));
        assertThrows(IllegalArgumentException.class, () -> ActorSubjects.of(new Actor("a", ActorType.UNKNOWN), Set.of()));
    }
}
