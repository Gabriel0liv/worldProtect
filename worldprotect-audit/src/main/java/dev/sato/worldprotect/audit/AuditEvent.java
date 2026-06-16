package dev.sato.worldprotect.audit;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Record of an action executed in the world.
 */
public final class AuditEvent {
    private final UUID id;
    private final Instant timestamp;
    private final Actor actor;
    private final AuditAction action;
    private final DimensionRef dimension;
    private final BlockPosRef position;
    private final String details; // Extra JSON/metadata serialized for simplicity

    public AuditEvent(UUID id, Instant timestamp, Actor actor, AuditAction action, DimensionRef dimension, BlockPosRef position, String details) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
        this.actor = Objects.requireNonNull(actor, "actor must not be null");
        this.action = Objects.requireNonNull(action, "action must not be null");
        this.dimension = Objects.requireNonNull(dimension, "dimension must not be null");
        this.position = Objects.requireNonNull(position, "position must not be null");
        this.details = Objects.requireNonNull(details, "details must not be null");
    }

    public UUID getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Actor getActor() {
        return actor;
    }

    public AuditAction getAction() {
        return action;
    }

    public DimensionRef getDimension() {
        return dimension;
    }

    public BlockPosRef getPosition() {
        return position;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "AuditEvent{id=" + id + ", timestamp=" + timestamp +
               ", actor=" + actor + ", action=" + action +
               ", position=" + position + "}";
    }
}
