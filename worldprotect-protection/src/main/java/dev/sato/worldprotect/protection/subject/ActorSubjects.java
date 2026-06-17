package dev.sato.worldprotect.protection.subject;

import dev.sato.worldprotect.core.actor.Actor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Maps an actor to all subject references it represents.
 */
public final class ActorSubjects {
    private final Actor actor;
    private final Set<SubjectRef> subjects;

    private ActorSubjects(Actor actor, Set<SubjectRef> subjects) {
        this.actor = Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(subjects, "subjects must not be null");
        if (subjects.isEmpty()) {
            throw new IllegalArgumentException("subjects must not be empty");
        }
        for (SubjectRef s : subjects) {
            Objects.requireNonNull(s, "subject element must not be null");
        }
        this.subjects = Set.copyOf(subjects);
    }

    public static ActorSubjects of(Actor actor, Set<SubjectRef> subjects) {
        return new ActorSubjects(actor, subjects);
    }

    public static ActorSubjects player(Actor actor, UUID playerUuid, Collection<String> groups) {
        Objects.requireNonNull(playerUuid, "playerUuid must not be null");
        Objects.requireNonNull(groups, "groups must not be null");

        Set<SubjectRef> set = new HashSet<>();
        set.add(SubjectRef.player(playerUuid));
        for (String group : groups) {
            Objects.requireNonNull(group, "group element must not be null");
            set.add(SubjectRef.group(group));
        }
        return new ActorSubjects(actor, set);
    }

    public static ActorSubjects console(Actor actor) {
        return new ActorSubjects(actor, Set.of(SubjectRef.console()));
    }

    public static ActorSubjects system(Actor actor) {
        return new ActorSubjects(actor, Set.of(SubjectRef.system()));
    }

    public Actor actor() {
        return actor;
    }

    public Set<SubjectRef> subjects() {
        return subjects;
    }

    public boolean contains(SubjectRef subject) {
        Objects.requireNonNull(subject, "subject must not be null");
        return subjects.contains(subject);
    }

    public boolean hasAny(Collection<SubjectRef> required) {
        Objects.requireNonNull(required, "required must not be null");
        for (SubjectRef req : required) {
            if (subjects.contains(req)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActorSubjects that = (ActorSubjects) o;
        return actor.equals(that.actor) && subjects.equals(that.subjects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actor, subjects);
    }

    @Override
    public String toString() {
        return "ActorSubjects{actor=" + actor + ", subjects=" + subjects + "}";
    }
}
