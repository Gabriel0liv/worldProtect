package dev.sato.worldprotect.protection.resolver;

import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.ActionFlagMapper;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.query.ProtectionQuery;
import dev.sato.worldprotect.protection.result.ProtectionDecision;
import dev.sato.worldprotect.protection.region.Region;
import dev.sato.worldprotect.protection.region.RegionSet;
import dev.sato.worldprotect.protection.rule.FlagRule;
import dev.sato.worldprotect.protection.rule.FlagRuleEvaluation;
import dev.sato.worldprotect.protection.rule.FlagRuleMatchSource;
import dev.sato.worldprotect.protection.rule.QueryResourceExtractor;
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
        return resolve(query, regionSet, null);
    }

    /**
     * Resolves the protection check for the given query, region set, and subject context.
     */
    public ProtectionDecision resolve(
            ProtectionQuery query,
            RegionSet regionSet,
            dev.sato.worldprotect.protection.permission.ProtectionSubjectContext subjectContext
    ) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(regionSet, "regionSet must not be null");

        // 1. If subjectContext has global bypass, return ALLOW immediately.
        if (subjectContext != null && dev.sato.worldprotect.protection.subject.SubjectResolver.hasGlobalBypass(subjectContext)) {
            return ProtectionDecision.allow("Action allowed due to global bypass", null, null);
        }

        List<Region> matched = regionSet.matching(query.getDimension(), query.getPosition());
        if (matched.isEmpty()) {
            return ProtectionDecision.pass("No matching regions found for action " + query.getAction());
        }

        RegionInheritanceResolver inheritanceResolver = new RegionInheritanceResolver(regionSet);

        // Filter out parent regions that are inherited by other matched regions to prevent
        // parent access policies or parent-specific bypass permissions from bypassing a child region decision.
        java.util.Set<dev.sato.worldprotect.protection.region.RegionId> inheritedRegionIds = new java.util.HashSet<>();
        for (Region r : matched) {
            List<Region> lineage = inheritanceResolver.lineage(r);
            for (int i = 1; i < lineage.size(); i++) {
                inheritedRegionIds.add(lineage.get(i).getId());
            }
        }

        List<Region> filteredMatched = new ArrayList<>();
        for (Region r : matched) {
            if (!inheritedRegionIds.contains(r.getId())) {
                filteredMatched.add(r);
            }
        }
        matched = filteredMatched;

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
        Optional<ResourceRef> resourceOpt = QueryResourceExtractor.primaryResource(query);
        ResourceRef resource = resourceOpt.orElse(null);

        for (List<Region> group : priorityGroups) {
            int priority = group.get(0).getPriority();

            for (FlagKey flagKey : mappedFlags) {
                boolean hasDeny = false;
                boolean hasAllow = false;
                Region denyRegion = null;
                Region allowRegion = null;
                String denySource = FlagRuleMatchSource.DEFAULT.name();
                String allowSource = FlagRuleMatchSource.DEFAULT.name();

                for (Region region : group) {
                    // 2. Check if subjectContext has flag-specific bypass
                    if (subjectContext != null && dev.sato.worldprotect.protection.subject.SubjectResolver.hasFlagBypass(subjectContext, flagKey)) {
                        hasAllow = true;
                        if (allowRegion == null) {
                            allowRegion = region;
                            allowSource = "flag bypass";
                        }
                        continue;
                    }

                    Optional<FlagRule> ruleOpt = inheritanceResolver.effectiveFlagRule(region, flagKey);
                    if (ruleOpt.isPresent()) {
                        FlagRule rule = ruleOpt.get();
                        FlagRuleEvaluation evaluation = rule.evaluate(resource);
                        FlagState resolvedState = evaluation.state();

                        String source = evaluation.source().name();
                        if (evaluation.matchedSelector().isPresent()) {
                            source += ":" + evaluation.matchedSelector().get();
                        }

                        if (resolvedState == FlagState.DENY) {
                            boolean bypassed = false;
                            if (subjectContext != null) {
                                dev.sato.worldprotect.protection.subject.RegionSubjects effectiveSubjects = inheritanceResolver.effectiveSubjects(region);
                                dev.sato.worldprotect.protection.subject.RegionRole role = dev.sato.worldprotect.protection.subject.SubjectResolver.roleInRegion(subjectContext, effectiveSubjects);
                                boolean isOwnerRoleBypass = role == dev.sato.worldprotect.protection.subject.RegionRole.OWNER && region.accessPolicy().ownerBypasses(flagKey);
                                boolean isMemberRoleBypass = role == dev.sato.worldprotect.protection.subject.RegionRole.MEMBER && region.accessPolicy().memberBypasses(flagKey);

                                boolean isOwnerPermissionBypass = dev.sato.worldprotect.protection.subject.SubjectResolver.hasRegionOwnerBypass(subjectContext, region.getId()) && region.accessPolicy().ownerBypasses(flagKey);
                                boolean isMemberPermissionBypass = dev.sato.worldprotect.protection.subject.SubjectResolver.hasRegionMemberBypass(subjectContext, region.getId()) && region.accessPolicy().memberBypasses(flagKey);

                                if (isOwnerRoleBypass || isOwnerPermissionBypass) {
                                    bypassed = true;
                                    hasAllow = true;
                                    if (allowRegion == null) {
                                        allowRegion = region;
                                        allowSource = isOwnerRoleBypass ? "region owner bypass" : "region owner bypass permission";
                                    }
                                } else if (isMemberRoleBypass || isMemberPermissionBypass) {
                                    bypassed = true;
                                    hasAllow = true;
                                    if (allowRegion == null) {
                                        allowRegion = region;
                                        allowSource = isMemberRoleBypass ? "region member bypass" : "region member bypass permission";
                                    }
                                }
                            }

                            if (!bypassed) {
                                hasDeny = true;
                                if (denyRegion == null) {
                                    denyRegion = region;
                                    denySource = source;
                                }
                            }
                        } else if (resolvedState == FlagState.ALLOW) {
                            hasAllow = true;
                            if (allowRegion == null) {
                                allowRegion = region;
                                allowSource = source;
                            }
                        }
                    }
                }

                // DENY beats ALLOW at the same priority level
                if (hasDeny) {
                    String resourceStr = resource != null ? " for resource " + resource : "";
                    String reason = String.format("Action %s denied by flag %s in region %s (priority %d)%s (via %s)",
                            query.getAction(), flagKey.getValue(), denyRegion.getId().getValue(), priority, resourceStr, denySource);
                    return ProtectionDecision.deny(reason, denyRegion.getId(), flagKey);
                } else if (hasAllow) {
                    String resourceStr = resource != null ? " for resource " + resource : "";
                    String reason = String.format("Action %s allowed by flag %s in region %s (priority %d)%s (via %s)",
                            query.getAction(), flagKey.getValue(), allowRegion.getId().getValue(), priority, resourceStr, allowSource);
                    return ProtectionDecision.allow(reason, allowRegion.getId(), flagKey);
                }
            }
        }

        return ProtectionDecision.pass("No explicit flags matched for action " + query.getAction());
    }
}
