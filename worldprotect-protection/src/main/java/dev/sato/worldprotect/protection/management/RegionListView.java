package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.minecraft.DimensionRef;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable list view for region info queries.
 */
public final class RegionListView {
    private final Optional<DimensionRef> dimensionFilter;
    private final List<RegionInfoView> regions;

    private RegionListView(Optional<DimensionRef> dimensionFilter, List<RegionInfoView> regions) {
        this.dimensionFilter = Objects.requireNonNull(dimensionFilter, "dimensionFilter must not be null");
        this.regions = List.copyOf(Objects.requireNonNull(regions, "regions must not be null"));
    }

    public static RegionListView of(Optional<DimensionRef> dimensionFilter, List<RegionInfoView> regions) {
        return new RegionListView(dimensionFilter, regions);
    }

    public Optional<DimensionRef> dimensionFilter() {
        return dimensionFilter;
    }

    public List<RegionInfoView> regions() {
        return regions;
    }

    public int size() {
        return regions.size();
    }

    public Optional<DimensionRef> getDimensionFilter() {
        return dimensionFilter;
    }

    public List<RegionInfoView> getRegions() {
        return regions;
    }
}
