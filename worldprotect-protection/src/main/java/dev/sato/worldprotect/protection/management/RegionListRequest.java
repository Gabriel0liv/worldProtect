package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.minecraft.DimensionRef;
import java.util.Objects;
import java.util.Optional;

public final class RegionListRequest {
    private final Optional<DimensionRef> dimensionFilter;

    private RegionListRequest(Optional<DimensionRef> dimensionFilter) {
        this.dimensionFilter = Objects.requireNonNull(dimensionFilter, "dimensionFilter must not be null");
    }

    public static RegionListRequest all() {
        return new RegionListRequest(Optional.empty());
    }

    public static RegionListRequest of(Optional<DimensionRef> dimensionFilter) {
        return new RegionListRequest(dimensionFilter);
    }

    public Optional<DimensionRef> dimensionFilter() { return dimensionFilter; }
    public Optional<DimensionRef> getDimensionFilter() { return dimensionFilter; }
}
