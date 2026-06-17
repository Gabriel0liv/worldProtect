package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.protection.subject.RegionSubjects;
import dev.sato.worldprotect.protection.subject.SubjectRef;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Immutable configuration representation of region owners and members.
 */
public final class RegionSubjectsConfig {
    private static final RegionSubjectsConfig EMPTY = new RegionSubjectsConfig(List.of(), List.of());

    private final List<SubjectRefConfig> owners;
    private final List<SubjectRefConfig> members;

    private RegionSubjectsConfig(Collection<SubjectRefConfig> owners, Collection<SubjectRefConfig> members) {
        Objects.requireNonNull(owners, "owners must not be null");
        Objects.requireNonNull(members, "members must not be null");
        for (SubjectRefConfig o : owners) {
            Objects.requireNonNull(o, "owner element must not be null");
        }
        for (SubjectRefConfig m : members) {
            Objects.requireNonNull(m, "member element must not be null");
        }
        this.owners = List.copyOf(owners);
        this.members = List.copyOf(members);
    }

    public static RegionSubjectsConfig empty() {
        return EMPTY;
    }

    public static RegionSubjectsConfig of(Collection<SubjectRefConfig> owners, Collection<SubjectRefConfig> members) {
        return new RegionSubjectsConfig(owners, members);
    }

    public List<SubjectRefConfig> owners() {
        return owners;
    }

    public List<SubjectRefConfig> members() {
        return members;
    }

    public boolean isEmpty() {
        return owners.isEmpty() && members.isEmpty();
    }

    public ConfigValidationResult validate(String path) {
        Objects.requireNonNull(path, "path must not be null");
        ConfigValidationResult result = ConfigValidationResult.ok();

        Set<String> seenOwners = new HashSet<>();
        Set<String> duplicateOwners = new HashSet<>();
        for (int i = 0; i < owners.size(); i++) {
            SubjectRefConfig owner = owners.get(i);
            String p = path + ".owners[" + i + "]";
            result = result.merge(owner.validate(p));
            if (!seenOwners.add(owner.rawValue())) {
                duplicateOwners.add(owner.rawValue());
            }
        }

        for (String dup : duplicateOwners) {
            result = result.add(ConfigValidationMessage.warning(path + ".owners", "Duplicate owner configured: " + dup));
        }

        Set<String> seenMembers = new HashSet<>();
        Set<String> duplicateMembers = new HashSet<>();
        for (int i = 0; i < members.size(); i++) {
            SubjectRefConfig member = members.get(i);
            String p = path + ".members[" + i + "]";
            result = result.merge(member.validate(p));
            if (!seenMembers.add(member.rawValue())) {
                duplicateMembers.add(member.rawValue());
            }
        }

        for (String dup : duplicateMembers) {
            result = result.add(ConfigValidationMessage.warning(path + ".members", "Duplicate member configured: " + dup));
        }

        // Warning if same subject in owners and members
        Set<String> intersection = new HashSet<>(seenOwners);
        intersection.retainAll(seenMembers);
        for (String both : intersection) {
            result = result.add(ConfigValidationMessage.warning(path, "Subject '" + both + "' is configured as both owner and member. Owner role wins."));
        }

        return result;
    }

    public RegionSubjects toDomain() {
        Set<SubjectRef> domainOwners = owners.stream()
                .map(SubjectRefConfig::toDomain)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<SubjectRef> domainMembers = members.stream()
                .map(SubjectRefConfig::toDomain)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return RegionSubjects.of(domainOwners, domainMembers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionSubjectsConfig that = (RegionSubjectsConfig) o;
        return owners.equals(that.owners) && members.equals(that.members);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owners, members);
    }

    @Override
    public String toString() {
        return "RegionSubjectsConfig{owners=" + owners + ", members=" + members + "}";
    }
}
