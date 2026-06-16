package dev.sato.worldprotect.core.actor;

import java.util.Objects;

/**
 * Represents an entity, player, block or system that triggers actions in the world.
 */
public final class Actor {
    private final String id;
    private final ActorType type;

    public Actor(String id, ActorType type) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public String getId() {
        return id;
    }

    public ActorType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Actor actor = (Actor) o;
        return id.equals(actor.id) && type == actor.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    @Override
    public String toString() {
        return "Actor{id='" + id + "', type=" + type + "}";
    }
}
