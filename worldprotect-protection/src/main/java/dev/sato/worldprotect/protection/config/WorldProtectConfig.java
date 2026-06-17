package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.protection.flag.FlagRegistry;
import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable configuration representation of the global plugin state.
 */
public final class WorldProtectConfig {
    private final List<RegionConfig> regions;

    private WorldProtectConfig(Collection<RegionConfig> regions) {
        Objects.requireNonNull(regions, "regions must not be null");
        for (RegionConfig rc : regions) {
            Objects.requireNonNull(rc, "region config element must not be null");
        }
        this.regions = List.copyOf(regions);
    }

    public static WorldProtectConfig of(Collection<RegionConfig> regions) {
        return new WorldProtectConfig(regions);
    }

    public List<RegionConfig> regions() {
        return regions;
    }

    public List<RegionConfig> getRegions() {
        return regions;
    }

    public ConfigValidationResult validate(FlagRegistry flagRegistry) {
        Objects.requireNonNull(flagRegistry, "flagRegistry must not be null");
        ConfigValidationResult result = ConfigValidationResult.ok();

        if (regions.isEmpty()) {
            result = result.add(ConfigValidationMessage.warning("regions", "No regions configured"));
            return result;
        }

        Set<RegionId> seenIds = new HashSet<>();
        for (RegionConfig rc : regions) {
            RegionId id = rc.id();
            if (seenIds.contains(id)) {
                result = result.add(ConfigValidationMessage.error("regions." + id.getValue(), "Duplicate region ID found: " + id.getValue()));
            } else {
                seenIds.add(id);
            }
            // Merge all validation results from regions
            result = result.merge(rc.validate(flagRegistry));
        }

        if (!result.hasErrors()) {
            RegionHierarchyValidator hierarchyValidator = new RegionHierarchyValidator();
            result = result.merge(hierarchyValidator.validate(this));
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldProtectConfig that = (WorldProtectConfig) o;
        return regions.equals(that.regions);
    }

    @Override
    public int hashCode() {
        return regions.hashCode();
    }

    @Override
    public String toString() {
        return "WorldProtectConfig{regions=" + regions + "}";
    }
}
