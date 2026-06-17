package dev.sato.worldprotect.protection.subject;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Immutable container for owners and members of a region.
 */
public final class RegionSubjects {
    private static final RegionSubjects EMPTY = new RegionSubjects(Set.of(), Set.of());

    private final Set<SubjectRef> owners;
    private final Set<SubjectRef> members;

    private RegionSubjects(Set<SubjectRef> owners, Set<SubjectRef> members) {
        Objects.requireNonNull(owners, "owners must not be null");
        Objects.requireNonNull(members, "members must not be null");

        for (SubjectRef o : owners) {
            Objects.requireNonNull(o, "owner element must not be null");
        }
        for (SubjectRef m : members) {
            Objects.requireNonNull(m, "member element must not be null");
        }

        this.owners = Set.copyOf(owners);
        this.members = members.stream()
                .filter(m -> !this.owners.contains(m))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static RegionSubjects empty() {
        return EMPTY;
    }

    public static RegionSubjects of(Set<SubjectRef> owners, Set<SubjectRef> members) {
        return new RegionSubjects(owners, members);
    }

    public Set<SubjectRef> owners() {
        return owners;
    }

    public Set<SubjectRef> members() {
        return members;
    }

    public boolean isOwner(SubjectRef subject) {
        Objects.requireNonNull(subject, "subject must not be null");
        return owners.contains(subject);
    }

    public boolean isMember(SubjectRef subject) {
        Objects.requireNonNull(subject, "subject must not be null");
        return members.contains(subject);
    }

    public RegionRole roleOf(SubjectRef subject) {
        Objects.requireNonNull(subject, "subject must not be null");
        if (isOwner(subject)) {
            return RegionRole.OWNER;
        }
        if (isMember(subject)) {
            return RegionRole.MEMBER;
        }
        return RegionRole.NONE;
    }

    public boolean isEmpty() {
        return owners.isEmpty() && members.isEmpty();
    }

    public RegionSubjects withOwner(SubjectRef subject) {
        Objects.requireNonNull(subject, "subject must not be null");
        Set<SubjectRef> newOwners = new HashSet<>(owners);
        newOwners.add(subject);
        Set<SubjectRef> newMembers = new HashSet<>(members);
        newMembers.remove(subject);
        return new RegionSubjects(newOwners, newMembers);
    }

    public RegionSubjects withMember(SubjectRef subject) {
        Objects.requireNonNull(subject, "subject must not be null");
        if (isOwner(subject)) {
            return this;
        }
        Set<SubjectRef> newMembers = new HashSet<>(members);
        newMembers.add(subject);
        return new RegionSubjects(owners, newMembers);
    }

    public RegionSubjects withoutOwner(SubjectRef subject) {
        Objects.requireNonNull(subject, "subject must not be null");
        if (!isOwner(subject)) {
            return this;
        }
        Set<SubjectRef> newOwners = new HashSet<>(owners);
        newOwners.remove(subject);
        return new RegionSubjects(newOwners, members);
    }

    public RegionSubjects withoutMember(SubjectRef subject) {
        Objects.requireNonNull(subject, "subject must not be null");
        if (!isMember(subject)) {
            return this;
        }
        Set<SubjectRef> newMembers = new HashSet<>(members);
        newMembers.remove(subject);
        return new RegionSubjects(owners, newMembers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionSubjects that = (RegionSubjects) o;
        return owners.equals(that.owners) && members.equals(that.members);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owners, members);
    }

    @Override
    public String toString() {
        return "RegionSubjects{owners=" + owners + ", members=" + members + "}";
    }
}
