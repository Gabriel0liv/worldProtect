package dev.sato.worldprotect.protection.region;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * An immutable set of regions, allowing queries to find matching regions for a coordinate.
 */
public final class RegionSet {
    private final List<Region> regions;

    private RegionSet(List<Region> regions) {
        this.regions = List.copyOf(regions);
    }

    public static RegionSet of(Collection<Region> regions) {
        Objects.requireNonNull(regions, "regions must not be null");
        for (Region region : regions) {
            Objects.requireNonNull(region, "region element must not be null");
        }
        return new RegionSet(List.copyOf(regions));
    }

    /**
     * Finds matching regions that contain the position in the specified dimension.
     * Sorted by priority (descending) then Region ID (ascending).
     */
    public List<Region> matching(DimensionRef dimension, BlockPosRef position) {
        Objects.requireNonNull(dimension, "dimension must not be null");
        Objects.requireNonNull(position, "position must not be null");

        List<Region> matched = new ArrayList<>();
        for (Region region : regions) {
            if (dimension.equals(region.getDimension()) && region.contains(position)) {
                matched.add(region);
            }
        }

        matched.sort(new Comparator<Region>() {
            @Override
            public int compare(Region r1, Region r2) {
                int priorityCompare = Integer.compare(r2.getPriority(), r1.getPriority());
                if (priorityCompare != 0) {
                    return priorityCompare;
                }
                return r1.getId().getValue().compareTo(r2.getId().getValue());
            }
        });

        return matched;
    }

    public List<Region> regions() {
        return regions;
    }
}
