package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.protection.config.SubjectRefConfig;
import dev.sato.worldprotect.protection.region.RegionId;
import dev.sato.worldprotect.protection.subject.RegionRole;
import java.util.Objects;

public final class RemoveRegionSubjectRequest {
    private final RegionId regionId;
    private final RegionRole role;
    private final SubjectRefConfig subject;

    private RemoveRegionSubjectRequest(RegionId regionId, RegionRole role, SubjectRefConfig subject) {
        this.regionId = Objects.requireNonNull(regionId, "regionId must not be null");
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.subject = Objects.requireNonNull(subject, "subject must not be null");
    }

    public static RemoveRegionSubjectRequest of(RegionId regionId, RegionRole role, SubjectRefConfig subject) {
        return new RemoveRegionSubjectRequest(regionId, role, subject);
    }

    public RegionId regionId() { return regionId; }
    public RegionRole role() { return role; }
    public SubjectRefConfig subject() { return subject; }
    public RegionId getRegionId() { return regionId; }
    public RegionRole getRole() { return role; }
    public SubjectRefConfig getSubject() { return subject; }
}
