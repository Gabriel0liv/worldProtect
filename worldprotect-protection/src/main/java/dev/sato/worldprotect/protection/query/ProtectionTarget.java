package dev.sato.worldprotect.protection.query;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable representation of the target of a protection action.
 */
public final class ProtectionTarget {
    private final ProtectionTargetKind kind;
    private final ResourceRef id;
    private final DimensionRef dimension;
    private final BlockPosRef position;

    public ProtectionTarget(ProtectionTargetKind kind, ResourceRef id, DimensionRef dimension, BlockPosRef position) {
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
        this.id = id;
        this.dimension = dimension;
        this.position = position;
    }

    public ProtectionTargetKind kind() {
        return kind;
    }

    public Optional<ResourceRef> id() {
        return Optional.ofNullable(id);
    }

    public Optional<DimensionRef> dimension() {
        return Optional.ofNullable(dimension);
    }

    public Optional<BlockPosRef> position() {
        return Optional.ofNullable(position);
    }

    public ProtectionTargetKind getKind() {
        return kind;
    }

    public ResourceRef getId() {
        return id;
    }

    public DimensionRef getDimension() {
        return dimension;
    }

    public BlockPosRef getPosition() {
        return position;
    }

    public static ProtectionTarget block(ResourceRef blockId, DimensionRef dimension, BlockPosRef position) {
        return new ProtectionTarget(ProtectionTargetKind.BLOCK, blockId, dimension, position);
    }

    public static ProtectionTarget item(ResourceRef itemId) {
        return new ProtectionTarget(ProtectionTargetKind.ITEM, itemId, null, null);
    }

    public static ProtectionTarget entity(ResourceRef entityId, DimensionRef dimension, BlockPosRef position) {
        return new ProtectionTarget(ProtectionTargetKind.ENTITY, entityId, dimension, position);
    }

    public static ProtectionTarget container(ResourceRef containerId, DimensionRef dimension, BlockPosRef position) {
        return new ProtectionTarget(ProtectionTargetKind.CONTAINER, containerId, dimension, position);
    }

    public static ProtectionTarget drop(ResourceRef itemId, DimensionRef dimension, BlockPosRef position) {
        return new ProtectionTarget(ProtectionTargetKind.DROP, itemId, dimension, position);
    }

    public static ProtectionTarget position(DimensionRef dimension, BlockPosRef position) {
        return new ProtectionTarget(ProtectionTargetKind.POSITION, null, dimension, position);
    }

    public static ProtectionTarget unknown() {
        return new ProtectionTarget(ProtectionTargetKind.UNKNOWN, null, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtectionTarget that = (ProtectionTarget) o;
        return kind == that.kind &&
               Objects.equals(id, that.id) &&
               Objects.equals(dimension, that.dimension) &&
               Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, id, dimension, position);
    }

    @Override
    public String toString() {
        return "ProtectionTarget{kind=" + kind + ", id=" + id + ", dimension=" + dimension + ", position=" + position + "}";
    }
}
