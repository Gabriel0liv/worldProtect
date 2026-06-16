package dev.sato.worldprotect.minecraft.snapshot;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import java.util.Objects;

/**
 * Capture state of a block in the world.
 */
public final class BlockSnapshot {
    private final DimensionRef dimension;
    private final BlockPosRef position;
    private final ResourceRef blockId;
    private final String stateProperties; // e.g. "facing=north,waterlogged=false"
    private final NbtSnapshot tileEntityNbt;

    public BlockSnapshot(DimensionRef dimension, BlockPosRef position, ResourceRef blockId, String stateProperties, NbtSnapshot tileEntityNbt) {
        this.dimension = Objects.requireNonNull(dimension, "dimension must not be null");
        this.position = Objects.requireNonNull(position, "position must not be null");
        this.blockId = Objects.requireNonNull(blockId, "blockId must not be null");
        this.stateProperties = Objects.requireNonNull(stateProperties, "stateProperties must not be null");
        this.tileEntityNbt = Objects.requireNonNull(tileEntityNbt, "tileEntityNbt must not be null");
    }

    public DimensionRef getDimension() {
        return dimension;
    }

    public BlockPosRef getPosition() {
        return position;
    }

    public ResourceRef getBlockId() {
        return blockId;
    }

    public String getStateProperties() {
        return stateProperties;
    }

    public NbtSnapshot getTileEntityNbt() {
        return tileEntityNbt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockSnapshot that = (BlockSnapshot) o;
        return dimension.equals(that.dimension) &&
                position.equals(that.position) &&
                blockId.equals(that.blockId) &&
                stateProperties.equals(that.stateProperties) &&
                tileEntityNbt.equals(that.tileEntityNbt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, position, blockId, stateProperties, tileEntityNbt);
    }

    @Override
    public String toString() {
        return "BlockSnapshot{" + blockId + " at " + position + " in " + dimension + "}";
    }
}
