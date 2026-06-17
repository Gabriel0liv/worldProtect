package dev.sato.worldprotect.protection.permission;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.protection.subject.ActorSubjects;
import dev.sato.worldprotect.protection.subject.SubjectRef;
import java.util.Objects;

/**
 * Combines actor subjects and permissions for resolver use.
 */
public final class ProtectionSubjectContext {
    private final ActorSubjects actorSubjects;
    private final PermissionSet permissions;

    private ProtectionSubjectContext(ActorSubjects actorSubjects, PermissionSet permissions) {
        this.actorSubjects = Objects.requireNonNull(actorSubjects, "actorSubjects must not be null");
        this.permissions = Objects.requireNonNull(permissions, "permissions must not be null");
    }

    public static ProtectionSubjectContext of(ActorSubjects actorSubjects, PermissionSet permissions) {
        return new ProtectionSubjectContext(actorSubjects, permissions);
    }

    public static ProtectionSubjectContext withoutPermissions(ActorSubjects actorSubjects) {
        return new ProtectionSubjectContext(actorSubjects, PermissionSet.empty());
    }

    public Actor actor() {
        return actorSubjects.actor();
    }

    public ActorSubjects actorSubjects() {
        return actorSubjects;
    }

    public PermissionSet permissions() {
        return permissions;
    }

    public boolean hasPermission(String permission) {
        return permissions.has(permission);
    }

    public boolean hasSubject(SubjectRef subject) {
        return actorSubjects.contains(subject);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtectionSubjectContext that = (ProtectionSubjectContext) o;
        return actorSubjects.equals(that.actorSubjects) && permissions.equals(that.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actorSubjects, permissions);
    }

    @Override
    public String toString() {
        return "ProtectionSubjectContext{actorSubjects=" + actorSubjects + ", permissions=" + permissions + "}";
    }
}
