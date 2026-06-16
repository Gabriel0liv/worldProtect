package dev.sato.worldprotect.protection.resolver;

import dev.sato.worldprotect.protection.flag.ActionFlagMapper;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.query.ProtectionQuery;
import dev.sato.worldprotect.protection.result.ProtectionDecision;
import dev.sato.worldprotect.protection.region.Region;
import dev.sato.worldprotect.protection.region.RegionSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Resolves protection queries against regions using priority and flag mappings.
 */
public final class ProtectionResolver {

    /**
     * Resolves the protection check for the given query and region set.
     */
    public ProtectionDecision resolve(ProtectionQuery query, RegionSet regionSet) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(regionSet, "regionSet must not be null");

        List<Region> matched = regionSet.matching(query.getDimension(), query.getPosition());
        if (matched.isEmpty()) {
            return ProtectionDecision.pass("No matching regions found for action " + query.getAction());
        }

        // Group matched regions by priority descending (since matched is already sorted by priority desc)
        List<List<Region>> priorityGroups = new ArrayList<>();
        List<Region> currentGroup = new ArrayList<>();
        int currentPriority = matched.get(0).getPriority();
        currentGroup.add(matched.get(0));

        for (int i = 1; i < matched.size(); i++) {
            Region r = matched.get(i);
            if (r.getPriority() == currentPriority) {
                currentGroup.add(r);
            } else {
                priorityGroups.add(currentGroup);
                currentGroup = new ArrayList<>();
                currentPriority = r.getPriority();
                currentGroup.add(r);
            }
        }
        priorityGroups.add(currentGroup);

        List<FlagKey> mappedFlags = ActionFlagMapper.mapAction(query.getAction());

        for (List<Region> group : priorityGroups) {
            int priority = group.get(0).getPriority();

            for (FlagKey flagKey : mappedFlags) {
                boolean hasDeny = false;
                boolean hasAllow = false;
                Region denyRegion = null;
                Region allowRegion = null;

                for (Region region : group) {
                    Optional<FlagState> stateOpt = region.flags().get(flagKey);
                    if (stateOpt.isPresent()) {
                        FlagState state = stateOpt.get();
                        if (state == FlagState.DENY) {
                            hasDeny = true;
                            if (denyRegion == null) {
                                denyRegion = region;
                            }
                        } else if (state == FlagState.ALLOW) {
                            hasAllow = true;
                            if (allowRegion == null) {
                                allowRegion = region;
                            }
                        }
                    }
                }

                // DENY beats ALLOW at the same priority level
                if (hasDeny) {
                    String reason = String.format("Action %s denied by flag %s in region %s (priority %d)",
                            query.getAction(), flagKey.getValue(), denyRegion.getId().getValue(), priority);
                    return ProtectionDecision.deny(reason, denyRegion.getId(), flagKey);
                } else if (hasAllow) {
                    String reason = String.format("Action %s allowed by flag %s in region %s (priority %d)",
                            query.getAction(), flagKey.getValue(), allowRegion.getId().getValue(), priority);
                    return ProtectionDecision.allow(reason, allowRegion.getId(), flagKey);
                }
            }
        }

        return ProtectionDecision.pass("No explicit flags matched for action " + query.getAction());
    }
}
