package dev.sato.worldprotect.protection.resolver;

import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.permission.ProtectionSubjectContext;
import dev.sato.worldprotect.protection.query.ProtectionQuery;
import dev.sato.worldprotect.protection.region.GlobalRegion;
import dev.sato.worldprotect.protection.region.Region;
import dev.sato.worldprotect.protection.region.RegionSet;
import dev.sato.worldprotect.protection.result.ProtectionDecision;
import dev.sato.worldprotect.protection.rule.FlagRule;
import dev.sato.worldprotect.protection.rule.FlagRuleEvaluation;
import dev.sato.worldprotect.protection.rule.FlagRuleMatchSource;
import dev.sato.worldprotect.protection.rule.QueryResourceExtractor;
import dev.sato.worldprotect.protection.subject.RegionRole;
import dev.sato.worldprotect.protection.subject.RegionSubjects;
import dev.sato.worldprotect.protection.subject.SubjectResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Resolves protection decisions for build-related actions using the three-step resolution order:
 *
 * <ol>
 *   <li><strong>Specific flags</strong>: Action-specific flags (e.g. {@code break-block} for {@code BLOCK_BREAK}).
 *       Evaluated normally. Passthrough does NOT skip specific flags.</li>
 *   <li><strong>Explicit build fallback</strong>: The generic {@code build} flag.
 *       Skipped for a region if {@code passthrough = ALLOW} on that region.</li>
 *   <li><strong>Implicit membership build</strong>: Membership-based implicit protection.
 *       Skipped for a region if {@code passthrough = ALLOW} on that region.
 *       A region is "protection-active" for implicit build if it is a spatial region with
 *       effective subjects and no passthrough=allow. OWNER/MEMBER get implicit ALLOW;
 *       NONE gets implicit DENY (with access policy bypass checks).</li>
 * </ol>
 *
 * <p>At each step, priority grouping is applied: same-priority DENY beats same-priority ALLOW.
 * A higher-priority decision overrides all lower-priority decisions.</p>
 */
public final class BuildDecisionResolver {

    private BuildDecisionResolver() {}

    /**
     * Resolves a build-related action query. Caller must verify the action is build-related
     * before calling this method.
     */
    public static ProtectionDecision resolve(
            ProtectionQuery query,
            RegionSet regionSet,
            ProtectionSubjectContext subjectContext,
            List<Region> matched,
            RegionInheritanceResolver inheritanceResolver
    ) {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(regionSet, "regionSet must not be null");
        Objects.requireNonNull(matched, "matched must not be null");
        Objects.requireNonNull(inheritanceResolver, "inheritanceResolver must not be null");

        Optional<ResourceRef> resourceOpt = QueryResourceExtractor.primaryResource(query);
        ResourceRef resource = resourceOpt.orElse(null);

        // Group matched regions by priority descending
        List<List<Region>> priorityGroups = groupByPriority(matched);

        List<FlagKey> specificFlags = BuildSemantics.specificFlagsFor(query.getAction());

        // === STEP 1: Evaluate specific flags (passthrough does NOT skip these) ===
        ProtectionDecision specificResult = evaluateFlags(
                query, priorityGroups, specificFlags, resource, subjectContext, inheritanceResolver, false
        );
        if (!specificResult.isPass()) {
            return specificResult;
        }

        // === STEP 2: Evaluate explicit build fallback (passthrough=ALLOW skips) ===
        ProtectionDecision buildResult = evaluateFlags(
                query, priorityGroups, List.of(BuiltInFlags.BUILD_KEY), resource, subjectContext, inheritanceResolver, true
        );
        if (!buildResult.isPass()) {
            return buildResult;
        }

        // === STEP 3: Implicit membership-based build protection ===
        ProtectionDecision implicitResult = evaluateImplicitMembership(
                query, priorityGroups, subjectContext, inheritanceResolver
        );
        if (!implicitResult.isPass()) {
            return implicitResult;
        }

        return ProtectionDecision.pass("No build protection matched for action " + query.getAction());
    }

    /**
     * Evaluates flag-based protection across priority groups.
     *
     * @param passthroughAware if true, regions with passthrough=ALLOW are skipped
     */
    private static ProtectionDecision evaluateFlags(
            ProtectionQuery query,
            List<List<Region>> priorityGroups,
            List<FlagKey> flagKeys,
            ResourceRef resource,
            ProtectionSubjectContext subjectContext,
            RegionInheritanceResolver inheritanceResolver,
            boolean passthroughAware
    ) {
        if (flagKeys.isEmpty()) {
            return ProtectionDecision.pass("No specific flags to evaluate");
        }

        for (List<Region> group : priorityGroups) {
            int priority = group.get(0).getPriority();

            for (FlagKey flagKey : flagKeys) {
                boolean hasDeny = false;
                boolean hasAllow = false;
                Region denyRegion = null;
                Region allowRegion = null;
                String denySource = FlagRuleMatchSource.DEFAULT.name();
                String allowSource = FlagRuleMatchSource.DEFAULT.name();

                for (Region region : group) {
                    // Check flag-specific bypass
                    if (subjectContext != null && SubjectResolver.hasFlagBypass(subjectContext, flagKey)) {
                        hasAllow = true;
                        if (allowRegion == null) {
                            allowRegion = region;
                            allowSource = "flag bypass";
                        }
                        continue;
                    }

                    // Compute role
                    RegionRole role = RegionRole.NONE;
                    if (subjectContext != null) {
                        RegionSubjects effectiveSubjects = inheritanceResolver.effectiveSubjects(region);
                        role = SubjectResolver.roleInRegion(subjectContext, effectiveSubjects);
                    }

                    // If passthrough-aware, check passthrough flag
                    if (passthroughAware) {
                        Optional<FlagRule> passthroughOpt = inheritanceResolver.effectiveFlagRule(region, BuiltInFlags.PASSTHROUGH_KEY, role);
                        if (passthroughOpt.isPresent()) {
                            FlagState ptState = passthroughOpt.get().evaluate(null).state();
                            if (ptState == FlagState.ALLOW) {
                                continue; // passthrough=allow -> skip this region for build fallback
                            }
                        }
                    }

                    Optional<FlagRule> ruleOpt = inheritanceResolver.effectiveFlagRule(region, flagKey, role);
                    if (ruleOpt.isPresent()) {
                        FlagRule rule = ruleOpt.get();
                        FlagRuleEvaluation evaluation = rule.evaluate(resource);
                        FlagState resolvedState = evaluation.state();

                        String source = evaluation.source().name();
                        if (evaluation.matchedSelector().isPresent()) {
                            source += ":" + evaluation.matchedSelector().get();
                        }

                        if (resolvedState == FlagState.DENY) {
                            Optional<String> bypassSource = resolveBypassSource(subjectContext, region, role, flagKey);
                            if (bypassSource.isPresent()) {
                                hasAllow = true;
                                if (allowRegion == null) {
                                    allowRegion = region;
                                    allowSource = bypassSource.get();
                                }
                            } else {
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

        return ProtectionDecision.pass("No explicit flags matched for build action " + query.getAction());
    }

    /**
     * Evaluates implicit membership-based build protection.
     * A region is "protection-active" for implicit build if:
     * - It is a spatial (non-global) region
     * - It has effective subjects
     * - passthrough is NOT allow
     *
     * For protection-active regions:
     * - OWNER/MEMBER -> implicit ALLOW
     * - NONE -> implicit DENY (with access policy build bypass check)
     */
    private static ProtectionDecision evaluateImplicitMembership(
            ProtectionQuery query,
            List<List<Region>> priorityGroups,
            ProtectionSubjectContext subjectContext,
            RegionInheritanceResolver inheritanceResolver
    ) {
        if (subjectContext == null) {
            return ProtectionDecision.pass("No subject context for implicit membership check");
        }

        for (List<Region> group : priorityGroups) {
            int priority = group.get(0).getPriority();
            boolean hasDeny = false;
            boolean hasAllow = false;
            Region denyRegion = null;
            Region allowRegion = null;

            for (Region region : group) {
                // Global regions do not participate in implicit build protection
                if (region instanceof GlobalRegion) {
                    continue;
                }

                // Compute role and effective subjects
                RegionSubjects effectiveSubjects = inheritanceResolver.effectiveSubjects(region);
                RegionRole role = SubjectResolver.roleInRegion(subjectContext, effectiveSubjects);

                // Check passthrough - if passthrough=allow, skip implicit membership
                Optional<FlagRule> passthroughOpt = inheritanceResolver.effectiveFlagRule(region, BuiltInFlags.PASSTHROUGH_KEY, role);
                if (passthroughOpt.isPresent()) {
                    FlagState ptState = passthroughOpt.get().evaluate(null).state();
                    if (ptState == FlagState.ALLOW) {
                        continue; // passthrough=allow -> skip implicit membership for this region
                    }
                }

                // A region is protection-active for implicit build only if it has effective subjects
                if (effectiveSubjects.isEmpty()) {
                    continue;
                }

                // Implicit membership resolution
                if (role == RegionRole.OWNER || role == RegionRole.MEMBER) {
                    hasAllow = true;
                    if (allowRegion == null) {
                        allowRegion = region;
                    }
                } else {
                    // NONE - implicit deny, but check access policy build bypass
                    boolean bypassed = false;

                    // Check flag-specific bypass for build
                    if (SubjectResolver.hasFlagBypass(subjectContext, BuiltInFlags.BUILD_KEY)) {
                        bypassed = true;
                    }

                    // Check region-specific owner/member bypass permissions
                    if (!bypassed) {
                        bypassed = resolveBypassSource(subjectContext, region, role, BuiltInFlags.BUILD_KEY).isPresent();
                    }

                    if (bypassed) {
                        hasAllow = true;
                        if (allowRegion == null) {
                            allowRegion = region;
                        }
                    } else {
                        hasDeny = true;
                        if (denyRegion == null) {
                            denyRegion = region;
                        }
                    }
                }
            }

            // DENY beats ALLOW at the same priority level
            if (hasDeny) {
                String reason = String.format("Action %s denied by implicit membership build in region %s (priority %d)",
                        query.getAction(), denyRegion.getId().getValue(), priority);
                return ProtectionDecision.deny(reason, denyRegion.getId(), BuiltInFlags.BUILD_KEY);
            } else if (hasAllow) {
                String reason = String.format("Action %s allowed by implicit membership build in region %s (priority %d)",
                        query.getAction(), allowRegion.getId().getValue(), priority);
                return ProtectionDecision.allow(reason, allowRegion.getId(), BuiltInFlags.BUILD_KEY);
            }
        }

        return ProtectionDecision.pass("No implicit membership build protection for action " + query.getAction());
    }

    /**
     * Resolves the bypass source string for role-based bypasses.
     * Returns the bypass source description if bypassed, or empty if not.
     */
    private static Optional<String> resolveBypassSource(
            ProtectionSubjectContext subjectContext,
            Region region,
            RegionRole role,
            FlagKey flagKey
    ) {
        if (subjectContext == null) {
            return Optional.empty();
        }

        boolean isOwnerRoleBypass = role == RegionRole.OWNER && region.accessPolicy().ownerBypasses(flagKey);
        boolean isMemberRoleBypass = role == RegionRole.MEMBER && region.accessPolicy().memberBypasses(flagKey);

        boolean isOwnerPermissionBypass = SubjectResolver.hasRegionOwnerBypass(subjectContext, region.getId())
                && region.accessPolicy().ownerBypasses(flagKey);
        boolean isMemberPermissionBypass = SubjectResolver.hasRegionMemberBypass(subjectContext, region.getId())
                && region.accessPolicy().memberBypasses(flagKey);

        if (isOwnerRoleBypass || isOwnerPermissionBypass) {
            return Optional.of(isOwnerRoleBypass ? "region owner bypass" : "region owner bypass permission");
        } else if (isMemberRoleBypass || isMemberPermissionBypass) {
            return Optional.of(isMemberRoleBypass ? "region member bypass" : "region member bypass permission");
        }
        return Optional.empty();
    }

    /**
     * Groups a list of regions (already sorted by priority descending) into sublists,
     * where each sublist contains regions with the same priority.
     */
    private static List<List<Region>> groupByPriority(List<Region> matched) {
        List<List<Region>> groups = new ArrayList<>();
        if (matched.isEmpty()) {
            return groups;
        }
        List<Region> currentGroup = new ArrayList<>();
        int currentPriority = matched.get(0).getPriority();
        currentGroup.add(matched.get(0));

        for (int i = 1; i < matched.size(); i++) {
            Region r = matched.get(i);
            if (r.getPriority() == currentPriority) {
                currentGroup.add(r);
            } else {
                groups.add(currentGroup);
                currentGroup = new ArrayList<>();
                currentPriority = r.getPriority();
                currentGroup.add(r);
            }
        }
        groups.add(currentGroup);
        return groups;
    }
}
