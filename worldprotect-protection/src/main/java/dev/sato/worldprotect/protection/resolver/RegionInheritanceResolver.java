package dev.sato.worldprotect.protection.resolver;

import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.rule.FlagRule;
import dev.sato.worldprotect.protection.region.Region;
import dev.sato.worldprotect.protection.region.RegionId;
import dev.sato.worldprotect.protection.region.RegionSet;
import dev.sato.worldprotect.protection.subject.RegionSubjects;
import dev.sato.worldprotect.protection.subject.SubjectRef;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Resolver for region inheritance lineage, effective flag rules, and effective subjects.
 */
public final class RegionInheritanceResolver {
    private final RegionSet regionSet;

    public RegionInheritanceResolver(RegionSet regionSet) {
        this.regionSet = Objects.requireNonNull(regionSet, "regionSet must not be null");
    }

    /**
     * Resolves the lineage of a region, from child to root parent.
     */
    public List<Region> lineage(Region region) {
        Objects.requireNonNull(region, "region must not be null");
        List<Region> path = new ArrayList<>();
        Set<RegionId> visited = new HashSet<>();
        Region current = region;

        while (current != null) {
            if (!visited.add(current.getId())) {
                throw new IllegalStateException("Circular inheritance detected in region: " + current.getId().getValue());
            }
            path.add(current);
            Optional<RegionId> parentIdOpt = current.parentId();
            if (parentIdOpt.isPresent()) {
                RegionId parentId = parentIdOpt.get();
                current = regionSet.findById(parentId).orElseThrow(() ->
                        new IllegalStateException("Parent region '" + parentId.getValue() + "' not found for region '" + region.getId().getValue() + "'")
                );
            } else {
                current = null;
            }
        }
        return path;
    }

    /**
     * Finds the effective flag rule for the given region and flag key in the lineage.
     */
    public Optional<FlagRule> effectiveFlagRule(Region region, FlagKey flagKey) {
        Objects.requireNonNull(region, "region must not be null");
        Objects.requireNonNull(flagKey, "flagKey must not be null");

        for (Region r : lineage(region)) {
            Optional<FlagRule> ruleOpt = r.flags().rule(flagKey);
            if (ruleOpt.isPresent()) {
                return ruleOpt;
            }
        }
        return Optional.empty();
    }

    /**
     * Finds the effective flag rule for the given region, flag key, and role in the lineage.
     */
    public Optional<FlagRule> effectiveFlagRule(Region region, FlagKey flagKey, dev.sato.worldprotect.protection.subject.RegionRole role) {
        Objects.requireNonNull(region, "region must not be null");
        Objects.requireNonNull(flagKey, "flagKey must not be null");
        Objects.requireNonNull(role, "role must not be null");

        for (Region r : lineage(region)) {
            Optional<FlagRule> ruleOpt = r.flags().rule(flagKey);
            if (ruleOpt.isPresent()) {
                FlagRule rule = ruleOpt.get();
                if (rule.group().matches(role)) {
                    return ruleOpt;
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Combines subjects across the region's lineage (parent owners/members are inherited).
     * Child owners take precedence over member roles.
     */
    public RegionSubjects effectiveSubjects(Region region) {
        Objects.requireNonNull(region, "region must not be null");
        List<Region> path = lineage(region);

        Set<SubjectRef> owners = new HashSet<>();
        Set<SubjectRef> members = new HashSet<>();

        for (Region r : path) {
            owners.addAll(r.subjects().owners());
            members.addAll(r.subjects().members());
        }

        // Owners override member status globally
        members.removeAll(owners);

        return RegionSubjects.of(owners, members);
    }

    public boolean hasParent(Region region) {
        Objects.requireNonNull(region, "region must not be null");
        return region.parentId().isPresent();
    }

    public Optional<Region> parentOf(Region region) {
        Objects.requireNonNull(region, "region must not be null");
        return region.parentId().flatMap(regionSet::findById);
    }
}
