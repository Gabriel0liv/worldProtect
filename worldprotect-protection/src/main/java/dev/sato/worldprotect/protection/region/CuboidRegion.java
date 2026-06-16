package dev.sato.worldprotect.protection.region;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import java.util.Objects;

/**
 * A cuboid region bounded by minimum and maximum x, y, z points.
 */
public final class CuboidRegion implements Region {
    private final RegionId id;
    private final DimensionRef dimension;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;
    private final int priority;
    private final RegionFlags flags;

    public CuboidRegion(RegionId id, DimensionRef dimension, BlockPosRef pos1, BlockPosRef pos2, int priority) {
        this(id, dimension, pos1, pos2, priority, RegionFlags.empty());
    }

    public CuboidRegion(RegionId id, DimensionRef dimension, BlockPosRef pos1, BlockPosRef pos2, int priority, RegionFlags flags) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.dimension = Objects.requireNonNull(dimension, "dimension must not be null");
        Objects.requireNonNull(pos1, "pos1 must not be null");
        Objects.requireNonNull(pos2, "pos2 must not be null");
        
        this.minX = Math.min(pos1.getX(), pos2.getX());
        this.minY = Math.min(pos1.getY(), pos2.getY());
        this.minZ = Math.min(pos1.getZ(), pos2.getZ());
        
        this.maxX = Math.max(pos1.getX(), pos2.getX());
        this.maxY = Math.max(pos1.getY(), pos2.getY());
        this.maxZ = Math.max(pos1.getZ(), pos2.getZ());
        
        this.priority = priority;
        this.flags = Objects.requireNonNull(flags, "flags must not be null");
    }

    @Override
    public RegionId getId() {
        return id;
    }

    @Override
    public DimensionRef getDimension() {
        return dimension;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public RegionFlags flags() {
        return flags;
    }

    public RegionFlags getFlags() {
        return flags;
    }

    @Override
    public boolean contains(BlockPosRef pos) {
        Objects.requireNonNull(pos, "pos must not be null");
        return pos.getX() >= minX && pos.getX() <= maxX &&
               pos.getY() >= minY && pos.getY() <= maxY &&
               pos.getZ() >= minZ && pos.getZ() <= maxZ;
    }

    @Override
    public String toString() {
        return "CuboidRegion{id=" + id + ", dimension=" + dimension +
               ", min=(" + minX + "," + minY + "," + minZ + ")" +
               ", max=(" + maxX + "," + maxY + "," + maxZ + ")" +
               ", priority=" + priority + ", flags=" + flags + "}";
    }
}
