package dev.sato.worldprotect.protection.result;

import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable representation of a decision outcome from the protection system.
 */
public final class ProtectionDecision {
    private final DecisionState state;
    private final String reason;
    private final RegionId regionId;
    private final FlagKey flagKey;

    public ProtectionDecision(DecisionState state, String reason, RegionId regionId, FlagKey flagKey) {
        this.state = Objects.requireNonNull(state, "state must not be null");
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("reason must not be null or blank");
        }
        this.reason = reason;
        this.regionId = regionId;
        this.flagKey = flagKey;
    }

    public DecisionState state() {
        return state;
    }

    public String reason() {
        return reason;
    }

    public Optional<RegionId> regionId() {
        return Optional.ofNullable(regionId);
    }

    public Optional<FlagKey> flagKey() {
        return Optional.ofNullable(flagKey);
    }

    public DecisionState getState() {
        return state;
    }

    public String getReason() {
        return reason;
    }

    public RegionId getRegionId() {
        return regionId;
    }

    public FlagKey getFlagKey() {
        return flagKey;
    }

    public boolean isAllowed() {
        return state == DecisionState.ALLOW;
    }

    public boolean isDenied() {
        return state == DecisionState.DENY;
    }

    public boolean isPass() {
        return state == DecisionState.PASS;
    }

    public static ProtectionDecision allow(String reason, RegionId regionId, FlagKey flagKey) {
        return new ProtectionDecision(DecisionState.ALLOW, reason, regionId, flagKey);
    }

    public static ProtectionDecision deny(String reason, RegionId regionId, FlagKey flagKey) {
        return new ProtectionDecision(DecisionState.DENY, reason, regionId, flagKey);
    }

    public static ProtectionDecision pass(String reason) {
        return new ProtectionDecision(DecisionState.PASS, reason, null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtectionDecision that = (ProtectionDecision) o;
        return state == that.state &&
               reason.equals(that.reason) &&
               Objects.equals(regionId, that.regionId) &&
               Objects.equals(flagKey, that.flagKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, reason, regionId, flagKey);
    }

    @Override
    public String toString() {
        return "ProtectionDecision{state=" + state + ", reason='" + reason + '\'' +
               ", regionId=" + regionId + ", flagKey=" + flagKey + '}';
    }
}
