package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.protection.config.ConfigValidationMessage;
import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import dev.sato.worldprotect.protection.config.FlagRuleConfig;
import dev.sato.worldprotect.protection.config.RegionAccessPolicyConfig;
import dev.sato.worldprotect.protection.config.RegionConfig;
import dev.sato.worldprotect.protection.config.RegionSubjectsConfig;
import dev.sato.worldprotect.protection.config.SubjectRefConfig;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagRegistry;
import dev.sato.worldprotect.protection.region.RegionId;
import dev.sato.worldprotect.protection.subject.RegionRole;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Platform-independent immutable region management service.
 */
public final class RegionManagementService {
    private final FlagRegistry flagRegistry;

    public RegionManagementService(FlagRegistry flagRegistry) {
        this.flagRegistry = Objects.requireNonNull(flagRegistry, "flagRegistry must not be null");
    }

    public static RegionManagementService withBuiltIns() {
        return new RegionManagementService(FlagRegistry.withBuiltIns());
    }

    public RegionManagementResult<WorldProtectConfig> createRegion(WorldProtectConfig config, CreateRegionRequest request) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(request, "request must not be null");

        if (findRegion(config, request.regionId()).isPresent()) {
            return failure(
                    RegionManagementStatus.ALREADY_EXISTS,
                    "regions." + request.regionId().getValue(),
                    "Region '" + request.regionId().getValue() + "' already exists"
            );
        }
        if (request.parentId().isPresent()) {
            RegionId parentId = request.parentId().get();
            if (parentId.equals(request.regionId())) {
                return failure(
                        RegionManagementStatus.VALIDATION_FAILED,
                        "regions." + request.regionId().getValue() + ".parent",
                        "Region parent must not be itself"
                );
            }
            if (findRegion(config, parentId).isEmpty()) {
                return failure(
                        RegionManagementStatus.NOT_FOUND,
                        "regions." + request.regionId().getValue() + ".parent",
                        "Parent region '" + parentId.getValue() + "' does not exist"
                );
            }
        }

        RegionConfig created = RegionConfig.of(
                request.regionId(),
                request.dimension(),
                request.priority(),
                request.bounds(),
                Map.of(),
                RegionSubjectsConfig.empty(),
                RegionAccessPolicyConfig.defaults(),
                request.parentId()
        );

        List<RegionConfig> updated = new ArrayList<>(config.regions());
        updated.add(created);
        WorldProtectConfig newConfig = config.withRegions(updated);
        RegionMutationPlan plan = RegionMutationPlan.of(
                RegionMutationType.CREATE,
                request.regionId(),
                "Create region '" + request.regionId().getValue() + "'",
                Optional.empty(),
                Optional.of(created)
        );
        return validatedMutationResult(newConfig, "Region created", plan);
    }

    public RegionManagementResult<WorldProtectConfig> deleteRegion(WorldProtectConfig config, DeleteRegionRequest request) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(request, "request must not be null");

        Optional<RegionConfig> targetOpt = findRegion(config, request.regionId());
        if (targetOpt.isEmpty()) {
            return failure(RegionManagementStatus.NOT_FOUND, "regions." + request.regionId().getValue(), "Region not found");
        }

        List<RegionConfig> children = config.regions().stream()
                .filter(region -> region.parentId().isPresent() && region.parentId().get().equals(request.regionId()))
                .collect(Collectors.toList());
        if (!children.isEmpty()) {
            String childIds = children.stream()
                    .map(region -> region.id().getValue())
                    .sorted()
                    .collect(Collectors.joining(", "));
            ConfigValidationResult diagnostics = ConfigValidationResult.ok().add(
                    ConfigValidationMessage.error(
                            "regions." + request.regionId().getValue(),
                            "Cannot delete region '" + request.regionId().getValue() + "' because it is a parent of: " + childIds
                    )
            );
            return RegionManagementResult.failure(RegionManagementStatus.CONFLICT, diagnostics, "Region has dependent children");
        }

        List<RegionConfig> updated = config.regions().stream()
                .filter(region -> !region.id().equals(request.regionId()))
                .collect(Collectors.toList());
        WorldProtectConfig newConfig = config.withRegions(updated);
        RegionMutationPlan plan = RegionMutationPlan.of(
                RegionMutationType.DELETE,
                request.regionId(),
                "Delete region '" + request.regionId().getValue() + "'",
                targetOpt,
                Optional.empty()
        );
        return validatedMutationResult(newConfig, "Region deleted", plan);
    }

    public RegionManagementResult<WorldProtectConfig> setBounds(WorldProtectConfig config, SetRegionBoundsRequest request) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(request, "request must not be null");
        return mutateRegion(config, request.regionId(), RegionMutationType.SET_BOUNDS, "Set region bounds",
                region -> region.withBounds(request.bounds()));
    }

    public RegionManagementResult<WorldProtectConfig> setPriority(WorldProtectConfig config, SetRegionPriorityRequest request) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(request, "request must not be null");
        return mutateRegion(config, request.regionId(), RegionMutationType.SET_PRIORITY, "Set region priority",
                region -> region.withPriority(request.priority()));
    }

    public RegionManagementResult<WorldProtectConfig> setParent(WorldProtectConfig config, SetRegionParentRequest request) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(request, "request must not be null");

        Optional<RegionConfig> regionOpt = findRegion(config, request.regionId());
        if (regionOpt.isEmpty()) {
            return failure(RegionManagementStatus.NOT_FOUND, "regions." + request.regionId().getValue(), "Region not found");
        }
        if (request.regionId().equals(request.parentId())) {
            return failure(RegionManagementStatus.VALIDATION_FAILED, "regions." + request.regionId().getValue() + ".parent", "Region parent must not be itself");
        }
        if (findRegion(config, request.parentId()).isEmpty()) {
            return failure(RegionManagementStatus.NOT_FOUND, "regions." + request.regionId().getValue() + ".parent", "Parent region not found");
        }
        if (regionOpt.get().parentId().isPresent() && regionOpt.get().parentId().get().equals(request.parentId())) {
            return RegionManagementResult.noChange(config, "Parent already set");
        }

        return mutateRegion(config, request.regionId(), RegionMutationType.SET_PARENT, "Set region parent",
                region -> region.withParentId(Optional.of(request.parentId())));
    }

    public RegionManagementResult<WorldProtectConfig> clearParent(WorldProtectConfig config, ClearRegionParentRequest request) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(request, "request must not be null");

        Optional<RegionConfig> regionOpt = findRegion(config, request.regionId());
        if (regionOpt.isEmpty()) {
            return failure(RegionManagementStatus.NOT_FOUND, "regions." + request.regionId().getValue(), "Region not found");
        }
        if (regionOpt.get().parentId().isEmpty()) {
            return RegionManagementResult.noChange(config, "Region has no parent");
        }

        return mutateRegion(config, request.regionId(), RegionMutationType.CLEAR_PARENT, "Clear region parent",
                region -> region.withParentId(Optional.empty()));
    }

    public RegionManagementResult<WorldProtectConfig> setFlag(WorldProtectConfig config, SetRegionFlagRequest request) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(request, "request must not be null");

        if (!flagRegistry.exists(request.flagKey())) {
            return failure(
                    RegionManagementStatus.VALIDATION_FAILED,
                    "regions." + request.regionId().getValue() + ".flags." + request.flagKey().getValue(),
                    "Unknown flag key: " + request.flagKey().getValue()
            );
        }

        return mutateRegion(config, request.regionId(), RegionMutationType.SET_FLAG, "Set region flag", region -> {
            if (request.rule().equals(region.flags().get(request.flagKey()))) {
                return region;
            }
            Map<FlagKey, FlagRuleConfig> updatedFlags = new java.util.LinkedHashMap<>(region.flags());
            updatedFlags.put(request.flagKey(), request.rule());
            return region.withFlags(updatedFlags);
        });
    }

    public RegionManagementResult<WorldProtectConfig> clearFlag(WorldProtectConfig config, ClearRegionFlagRequest request) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(request, "request must not be null");

        Optional<RegionConfig> regionOpt = findRegion(config, request.regionId());
        if (regionOpt.isEmpty()) {
            return failure(RegionManagementStatus.NOT_FOUND, "regions." + request.regionId().getValue(), "Region not found");
        }
        if (!regionOpt.get().flags().containsKey(request.flagKey())) {
            return RegionManagementResult.noChange(config, "Flag not present on region");
        }

        return mutateRegion(config, request.regionId(), RegionMutationType.CLEAR_FLAG, "Clear region flag", region -> {
            Map<FlagKey, FlagRuleConfig> updatedFlags = new java.util.LinkedHashMap<>(region.flags());
            updatedFlags.remove(request.flagKey());
            return region.withFlags(updatedFlags);
        });
    }

    public RegionManagementResult<WorldProtectConfig> addSubject(WorldProtectConfig config, AddRegionSubjectRequest request) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(request, "request must not be null");

        if (!isManageableRole(request.role())) {
            return failure(RegionManagementStatus.VALIDATION_FAILED, "regions." + request.regionId().getValue() + ".subjects", "Subject role must be OWNER or MEMBER");
        }
        ConfigValidationResult subjectValidation = request.subject().validate("regions." + request.regionId().getValue() + ".subjects");
        if (subjectValidation.hasErrors()) {
            return RegionManagementResult.failure(RegionManagementStatus.VALIDATION_FAILED, subjectValidation, "Invalid subject");
        }

        Optional<RegionConfig> regionOpt = findRegion(config, request.regionId());
        if (regionOpt.isEmpty()) {
            return failure(RegionManagementStatus.NOT_FOUND, "regions." + request.regionId().getValue(), "Region not found");
        }

        RegionConfig region = regionOpt.get();
        boolean inOwners = region.subjectsConfig().owners().contains(request.subject());
        boolean inMembers = region.subjectsConfig().members().contains(request.subject());

        if (request.role() == RegionRole.OWNER && inOwners) {
            return RegionManagementResult.noChange(config, "Subject already owner");
        }
        if (request.role() == RegionRole.MEMBER && (inMembers || inOwners)) {
            return RegionManagementResult.noChange(config, inOwners ? "Subject is already owner" : "Subject already member");
        }

        return mutateRegion(config, request.regionId(), RegionMutationType.ADD_SUBJECT, "Add region subject", current -> {
            List<SubjectRefConfig> owners = new ArrayList<>(current.subjectsConfig().owners());
            List<SubjectRefConfig> members = new ArrayList<>(current.subjectsConfig().members());
            if (request.role() == RegionRole.OWNER) {
                members.remove(request.subject());
                owners.add(request.subject());
            } else {
                members.add(request.subject());
            }
            return current.withSubjectsConfig(RegionSubjectsConfig.of(owners, members));
        });
    }

    public RegionManagementResult<WorldProtectConfig> removeSubject(WorldProtectConfig config, RemoveRegionSubjectRequest request) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(request, "request must not be null");

        if (!isManageableRole(request.role())) {
            return failure(RegionManagementStatus.VALIDATION_FAILED, "regions." + request.regionId().getValue() + ".subjects", "Subject role must be OWNER or MEMBER");
        }

        Optional<RegionConfig> regionOpt = findRegion(config, request.regionId());
        if (regionOpt.isEmpty()) {
            return failure(RegionManagementStatus.NOT_FOUND, "regions." + request.regionId().getValue(), "Region not found");
        }

        RegionConfig region = regionOpt.get();
        boolean removed = request.role() == RegionRole.OWNER
                ? region.subjectsConfig().owners().contains(request.subject())
                : region.subjectsConfig().members().contains(request.subject());
        if (!removed) {
            return RegionManagementResult.noChange(config, "Subject not present in role");
        }

        return mutateRegion(config, request.regionId(), RegionMutationType.REMOVE_SUBJECT, "Remove region subject", current -> {
            List<SubjectRefConfig> owners = new ArrayList<>(current.subjectsConfig().owners());
            List<SubjectRefConfig> members = new ArrayList<>(current.subjectsConfig().members());
            if (request.role() == RegionRole.OWNER) {
                owners.remove(request.subject());
            } else {
                members.remove(request.subject());
            }
            return current.withSubjectsConfig(RegionSubjectsConfig.of(owners, members));
        });
    }

    public RegionManagementResult<WorldProtectConfig> setAccessPolicy(WorldProtectConfig config, SetRegionAccessPolicyRequest request) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(request, "request must not be null");
        return mutateRegion(config, request.regionId(), RegionMutationType.SET_ACCESS_POLICY, "Set region access policy",
                region -> region.withAccessPolicyConfig(request.accessPolicy()));
    }

    public RegionManagementResult<RegionInfoView> info(WorldProtectConfig config, RegionInfoRequest request) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(request, "request must not be null");

        Optional<RegionConfig> regionOpt = findRegion(config, request.regionId());
        if (regionOpt.isEmpty()) {
            return failure(RegionManagementStatus.NOT_FOUND, "regions." + request.regionId().getValue(), "Region not found");
        }
        return RegionManagementResult.success(toInfoView(regionOpt.get()), ConfigValidationResult.ok(), "Region info loaded");
    }

    public RegionManagementResult<RegionListView> list(WorldProtectConfig config, RegionListRequest request) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(request, "request must not be null");

        List<RegionInfoView> views = config.regions().stream()
                .filter(region -> request.dimensionFilter().isEmpty() || region.dimension().equals(request.dimensionFilter().get()))
                .map(this::toInfoView)
                .collect(Collectors.toList());
        return RegionManagementResult.success(
                RegionListView.of(request.dimensionFilter(), views),
                ConfigValidationResult.ok(),
                "Region list loaded"
        );
    }

    private RegionManagementResult<WorldProtectConfig> mutateRegion(
            WorldProtectConfig config,
            RegionId regionId,
            RegionMutationType mutationType,
            String summary,
            java.util.function.Function<RegionConfig, RegionConfig> mutator
    ) {
        int index = indexOfRegion(config, regionId);
        if (index < 0) {
            return failure(RegionManagementStatus.NOT_FOUND, "regions." + regionId.getValue(), "Region not found");
        }

        RegionConfig before = config.regions().get(index);
        RegionConfig after = Objects.requireNonNull(mutator.apply(before), "mutator must not return null");
        if (before.equals(after)) {
            return RegionManagementResult.noChange(config, "No changes applied");
        }

        List<RegionConfig> updated = new ArrayList<>(config.regions());
        updated.set(index, after);
        WorldProtectConfig newConfig = config.withRegions(updated);
        RegionMutationPlan plan = RegionMutationPlan.of(
                mutationType,
                regionId,
                summary + " for '" + regionId.getValue() + "'",
                Optional.of(before),
                Optional.of(after)
        );
        return validatedMutationResult(newConfig, summary, plan);
    }

    private RegionManagementResult<WorldProtectConfig> validatedMutationResult(
            WorldProtectConfig config,
            String successMessage,
            RegionMutationPlan mutationPlan
    ) {
        ConfigValidationResult diagnostics = config.validate(flagRegistry);
        if (diagnostics.hasErrors()) {
            return RegionManagementResult.failure(RegionManagementStatus.VALIDATION_FAILED, diagnostics, "Mutation failed validation");
        }
        return RegionManagementResult.success(config, diagnostics, successMessage, mutationPlan);
    }

    private Optional<RegionConfig> findRegion(WorldProtectConfig config, RegionId regionId) {
        return config.regions().stream()
                .filter(region -> region.id().equals(regionId))
                .findFirst();
    }

    private int indexOfRegion(WorldProtectConfig config, RegionId regionId) {
        for (int i = 0; i < config.regions().size(); i++) {
            if (config.regions().get(i).id().equals(regionId)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isManageableRole(RegionRole role) {
        return role == RegionRole.OWNER || role == RegionRole.MEMBER;
    }

    private RegionInfoView toInfoView(RegionConfig region) {
        return RegionInfoView.of(
                region.id(),
                region.dimension(),
                region.priority(),
                region.bounds(),
                region.parentId(),
                region.flags(),
                region.subjectsConfig().owners(),
                region.subjectsConfig().members(),
                region.accessPolicyConfig(),
                summarizeAccessPolicy(region.accessPolicyConfig())
        );
    }

    private String summarizeAccessPolicy(RegionAccessPolicyConfig accessPolicy) {
        String ownersBypass = accessPolicy.ownersBypass() == null ? "default(true)" : accessPolicy.ownersBypass().toString();
        String membersBypass = accessPolicy.membersBypass() == null ? "default(false)" : accessPolicy.membersBypass().toString();
        String ownerFlags = accessPolicy.ownerBypassFlags().stream().sorted().collect(Collectors.joining(", "));
        String memberFlags = accessPolicy.memberBypassFlags().stream().sorted().collect(Collectors.joining(", "));
        return "ownersBypass=" + ownersBypass
                + ", membersBypass=" + membersBypass
                + ", ownerFlagBypasses=[" + ownerFlags + "]"
                + ", memberFlagBypasses=[" + memberFlags + "]";
    }

    private <T> RegionManagementResult<T> failure(RegionManagementStatus status, String path, String message) {
        return RegionManagementResult.failure(status, ConfigValidationResult.ok().add(ConfigValidationMessage.error(path, message)), message);
    }
}
