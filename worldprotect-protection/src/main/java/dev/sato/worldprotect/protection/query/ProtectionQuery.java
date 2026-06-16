package dev.sato.worldprotect.protection.query;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.protection.flag.FlagKey;
import java.util.Objects;

/**
 * Encapsulates parameters needed to evaluate if an action is allowed.
 */
public final class ProtectionQuery {
    private final Actor actor;
    private final DimensionRef dimension;
    private final BlockPosRef position;
    private final FlagKey flagKey;

    public ProtectionQuery(Actor actor, DimensionRef dimension, BlockPosRef position, FlagKey flagKey) {
        this.actor = Objects.requireNonNull(actor, "actor must not be null");
        this.dimension = Objects.requireNonNull(dimension, "dimension must not be null");
        this.position = Objects.requireNonNull(position, "position must not be null");
        this.flagKey = Objects.requireNonNull(flagKey, "flagKey must not be null");
    }

    public Actor getActor() {
        return actor;
    }

    public DimensionRef getDimension() {
        return dimension;
    }

    public BlockPosRef getPosition() {
        return position;
    }

    public FlagKey getFlagKey() {
        return flagKey;
    }

    @Override
    public String toString() {
        return "ProtectionQuery{actor=" + actor + ", dimension=" + dimension +
               ", position=" + position + ", flagKey=" + flagKey + "}";
    }
}
