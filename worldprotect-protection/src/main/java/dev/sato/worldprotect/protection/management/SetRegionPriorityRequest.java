package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Objects;

public final class SetRegionPriorityRequest {
    private final RegionId regionId;
    private final int priority;

    private SetRegionPriorityRequest(RegionId regionId, int priority) {
        this.regionId = Objects.requireNonNull(regionId, "regionId must not be null");
        this.priority = priority;
    }

    public static SetRegionPriorityRequest of(RegionId regionId, int priority) {
        return new SetRegionPriorityRequest(regionId, priority);
    }

    public RegionId regionId() { return regionId; }
    public int priority() { return priority; }
    public RegionId getRegionId() { return regionId; }
    public int getPriority() { return priority; }
}
