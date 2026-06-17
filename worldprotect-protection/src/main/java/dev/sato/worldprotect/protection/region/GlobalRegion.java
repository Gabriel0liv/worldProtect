package dev.sato.worldprotect.protection.region;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.protection.subject.RegionAccessPolicy;
import dev.sato.worldprotect.protection.subject.RegionSubjects;
import java.util.Objects;
import java.util.Optional;

/**
 * A region covering an entire dimension.
 */
public final class GlobalRegion implements Region {
    private final RegionId id;
    private final DimensionRef dimension;
    private final int priority;
    private final RegionFlags flags;
    private final RegionSubjects subjects;
    private final RegionAccessPolicy accessPolicy;
    private final Optional<RegionId> parentId;

    public GlobalRegion(
            RegionId id,
            DimensionRef dimension,
            int priority,
            RegionFlags flags,
            RegionSubjects subjects,
            RegionAccessPolicy accessPolicy
    ) {
        this(id, dimension, priority, flags, subjects, accessPolicy, Optional.empty());
    }

    public GlobalRegion(
            RegionId id,
            DimensionRef dimension,
            int priority,
            RegionFlags flags,
            RegionSubjects subjects,
            RegionAccessPolicy accessPolicy,
            Optional<RegionId> parentId
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.dimension = Objects.requireNonNull(dimension, "dimension must not be null");
        this.flags = Objects.requireNonNull(flags, "flags must not be null");
        this.subjects = Objects.requireNonNull(subjects, "subjects must not be null");
        this.accessPolicy = Objects.requireNonNull(accessPolicy, "accessPolicy must not be null");
        this.priority = priority;
        this.parentId = Objects.requireNonNull(parentId, "parentId must not be null");
        if (parentId.isPresent() && parentId.get().equals(id)) {
            throw new IllegalArgumentException("Parent ID cannot be equal to region ID");
        }
    }

    @Override
    public RegionId getId() {
        return id;
    }

    public RegionId id() {
        return id;
    }

    @Override
    public DimensionRef getDimension() {
        return dimension;
    }

    public DimensionRef dimension() {
        return dimension;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    public int priority() {
        return priority;
    }

    @Override
    public boolean contains(BlockPosRef pos) {
        Objects.requireNonNull(pos, "pos must not be null");
        return true;
    }

    @Override
    public RegionFlags flags() {
        return flags;
    }

    public RegionFlags getFlags() {
        return flags;
    }

    @Override
    public RegionSubjects subjects() {
        return subjects;
    }

    public RegionSubjects getSubjects() {
        return subjects;
    }

    @Override
    public RegionAccessPolicy accessPolicy() {
        return accessPolicy;
    }

    public RegionAccessPolicy getAccessPolicy() {
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
        GlobalRegion that = (GlobalRegion) o;
        return priority == that.priority &&
               id.equals(that.id) &&
               dimension.equals(that.dimension) &&
               flags.equals(that.flags) &&
               subjects.equals(that.subjects) &&
               accessPolicy.equals(that.accessPolicy) &&
               parentId.equals(that.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dimension, priority, flags, subjects, accessPolicy, parentId);
    }

    @Override
    public String toString() {
        return "GlobalRegion{id=" + id + ", dimension=" + dimension +
               ", priority=" + priority + ", flags=" + flags +
               ", subjects=" + subjects + ", accessPolicy=" + accessPolicy +
               ", parentId=" + parentId + "}";
    }
}
