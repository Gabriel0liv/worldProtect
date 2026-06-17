package dev.sato.worldprotect.protection.permission;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.core.actor.ActorType;
import dev.sato.worldprotect.protection.subject.ActorSubjects;
import dev.sato.worldprotect.protection.subject.SubjectRef;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public final class ProtectionSubjectContextTest {

    @Test
    public void testContextDelegation() {
        Actor actor = new Actor("player1", ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.console(actor);
        PermissionSet permissions = PermissionSet.ofStrings(List.of("worldprotect.bypass"));

        ProtectionSubjectContext context = ProtectionSubjectContext.of(actorSubjects, permissions);

        assertEquals(actor, context.actor());
        assertEquals(actorSubjects, context.actorSubjects());
        assertEquals(permissions, context.permissions());

        assertTrue(context.hasPermission("worldprotect.bypass"));
        assertFalse(context.hasPermission("worldprotect.admin"));

        assertTrue(context.hasSubject(SubjectRef.console()));
        assertFalse(context.hasSubject(SubjectRef.system()));
    }

    @Test
    public void testWithoutPermissions() {
        Actor actor = new Actor("player1", ActorType.PLAYER);
        ActorSubjects actorSubjects = ActorSubjects.console(actor);
        ProtectionSubjectContext context = ProtectionSubjectContext.withoutPermissions(actorSubjects);

        assertTrue(context.permissions().permissions().isEmpty());
    }
}
