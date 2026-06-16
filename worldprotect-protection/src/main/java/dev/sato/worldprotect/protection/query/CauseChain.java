package dev.sato.worldprotect.protection.query;

import dev.sato.worldprotect.minecraft.ResourceRef;
import java.util.List;
import java.util.Objects;

/**
 * Represents an immutable chronological chain of causes that led to an action.
 * The first cause is considered the root cause, and the last cause is the direct cause.
 */
public final class CauseChain {
    private final List<ProtectionCause> causes;

    public CauseChain(List<ProtectionCause> causes) {
        Objects.requireNonNull(causes, "causes must not be null");
        if (causes.isEmpty()) {
            throw new IllegalArgumentException("causes chain must not be empty");
        }
        for (ProtectionCause cause : causes) {
            Objects.requireNonNull(cause, "cause element must not be null");
        }
        this.causes = List.copyOf(causes);
    }

    public static CauseChain of(ProtectionCause... causes) {
        return new CauseChain(List.of(causes));
    }

    public ProtectionCause root() {
        return causes.get(0);
    }

    public ProtectionCause direct() {
        return causes.get(causes.size() - 1);
    }

    public List<ProtectionCause> causes() {
        return causes;
    }

    public boolean containsType(ProtectionCauseType type) {
        if (type == null) return false;
        for (ProtectionCause cause : causes) {
            if (cause.type() == type) {
                return true;
            }
        }
        return false;
    }

    public boolean containsSource(ResourceRef source) {
        if (source == null) return false;
        for (ProtectionCause cause : causes) {
            if (source.equals(cause.getSourceId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CauseChain that = (CauseChain) o;
        return causes.equals(that.causes);
    }

    @Override
    public int hashCode() {
        return causes.hashCode();
    }

    @Override
    public String toString() {
        return "CauseChain{causes=" + causes + "}";
    }
}
