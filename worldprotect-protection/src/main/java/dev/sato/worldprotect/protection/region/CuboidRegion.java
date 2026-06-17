package dev.sato.worldprotect.protection.region;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import java.util.Objects;
import java.util.Optional;

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
    private final dev.sato.worldprotect.protection.subject.RegionSubjects subjects;
    private final dev.sato.worldprotect.protection.subject.RegionAccessPolicy accessPolicy;
    private final Optional<RegionId> parentId;

    public CuboidRegion(RegionId id, DimensionRef dimension, BlockPosRef pos1, BlockPosRef pos2, int priority) {
        this(id, dimension, pos1, pos2, priority, RegionFlags.empty());
    }

    public CuboidRegion(RegionId id, DimensionRef dimension, BlockPosRef pos1, BlockPosRef pos2, int priority, RegionFlags flags) {
        this(id, dimension, pos1, pos2, priority, flags, dev.sato.worldprotect.protection.subject.RegionSubjects.empty(), dev.sato.worldprotect.protection.subject.RegionAccessPolicy.defaults());
    }

    public CuboidRegion(
            RegionId id,
            DimensionRef dimension,
            BlockPosRef pos1,
            BlockPosRef pos2,
            int priority,
            RegionFlags flags,
            dev.sato.worldprotect.protection.subject.RegionSubjects subjects,
            dev.sato.worldprotect.protection.subject.RegionAccessPolicy accessPolicy
    ) {
        this(id, dimension, pos1, pos2, priority, flags, subjects, accessPolicy, Optional.empty());
    }

    public CuboidRegion(
            RegionId id,
            DimensionRef dimension,
            BlockPosRef pos1,
            BlockPosRef pos2,
            int priority,
            RegionFlags flags,
            dev.sato.worldprotect.protection.subject.RegionSubjects subjects,
            dev.sato.worldprotect.protection.subject.RegionAccessPolicy accessPolicy,
            Optional<RegionId> parentId
    ) {
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
        this.subjects = Objects.requireNonNull(subjects, "subjects must not be null");
        this.accessPolicy = Objects.requireNonNull(accessPolicy, "accessPolicy must not be null");
        this.parentId = Objects.requireNonNull(parentId, "parentId must not be null");
        if (parentId.isPresent() && parentId.get().equals(id)) {
            throw new IllegalArgumentException("Parent ID cannot be equal to region ID");
        }
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
    public dev.sato.worldprotect.protection.subject.RegionSubjects subjects() {
        return subjects;
    }

    @Override
    public dev.sato.worldprotect.protection.subject.RegionAccessPolicy accessPolicy() {
        return accessPolicy;
    }

    @Override
    public Optional<RegionId> parentId() {
        return parentId;
    }

    @Override
    public Optional<RegionId> getParentId() {
        return parentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CuboidRegion that = (CuboidRegion) o;
        return minX == that.minX && minY == that.minY && minZ == that.minZ &&
               maxX == that.maxX && maxY == that.maxY && maxZ == that.maxZ &&
               priority == that.priority &&
               id.equals(that.id) &&
               dimension.equals(that.dimension) &&
               flags.equals(that.flags) &&
               subjects.equals(that.subjects) &&
               accessPolicy.equals(that.accessPolicy) &&
               parentId.equals(that.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dimension, minX, minY, minZ, maxX, maxY, maxZ, priority, flags, subjects, accessPolicy, parentId);
    }

    @Override
    public String toString() {
        return "CuboidRegion{id=" + id + ", dimension=" + dimension +
               ", min=(" + minX + "," + minY + "," + minZ + ")" +
               ", max=(" + maxX + "," + maxY + "," + maxZ + ")" +
               ", priority=" + priority + ", flags=" + flags +
               ", subjects=" + subjects + ", accessPolicy=" + accessPolicy +
               ", parentId=" + parentId + "}";
    }
}
