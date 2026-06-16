package dev.sato.worldprotect.protection.query;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import java.util.Objects;

/**
 * Encapsulates parameters needed to evaluate if an action is allowed.
 */
public final class ProtectionQuery {
    private final Actor actor;
    private final ProtectionAction action;
    private final CauseChain causeChain;
    private final ProtectionTarget target;
    private final DimensionRef dimension;
    private final BlockPosRef position;

    public ProtectionQuery(Actor actor, ProtectionAction action, CauseChain causeChain, ProtectionTarget target, DimensionRef dimension, BlockPosRef position) {
        this.actor = Objects.requireNonNull(actor, "actor must not be null");
        this.action = Objects.requireNonNull(action, "action must not be null");
        this.causeChain = Objects.requireNonNull(causeChain, "causeChain must not be null");
        this.target = Objects.requireNonNull(target, "target must not be null");
        this.dimension = Objects.requireNonNull(dimension, "dimension must not be null");
        this.position = Objects.requireNonNull(position, "position must not be null");
    }

    public Actor getActor() {
        return actor;
    }

    public ProtectionAction getAction() {
        return action;
    }

    public CauseChain getCauseChain() {
        return causeChain;
    }

    public ProtectionTarget getTarget() {
        return target;
    }

    public DimensionRef getDimension() {
        return dimension;
    }

    public BlockPosRef getPosition() {
        return position;
    }

    public Actor actor() {
        return actor;
    }

    public ProtectionAction action() {
        return action;
    }

    public CauseChain causeChain() {
        return causeChain;
    }

    public ProtectionTarget target() {
        return target;
    }

    public DimensionRef dimension() {
        return dimension;
    }

    public BlockPosRef position() {
        return position;
    }

    @Override
    public String toString() {
        return "ProtectionQuery{" +
               "actor=" + actor +
               ", action=" + action +
               ", causeChain=" + causeChain +
               ", target=" + target +
               ", dimension=" + dimension +
               ", position=" + position +
               '}';
    }
}
