package dev.sato.worldprotect.audit;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Representation of a rollback operation to be executed on the server main thread.
 */
public final class RollbackPlan {
    private final UUID planId;
    private final List<AuditEvent> eventsToRevert;
    private boolean executed;

    public RollbackPlan(UUID planId, List<AuditEvent> eventsToRevert) {
        this.planId = Objects.requireNonNull(planId, "planId must not be null");
        this.eventsToRevert = Collections.unmodifiableList(Objects.requireNonNull(eventsToRevert, "eventsToRevert must not be null"));
        this.executed = false;
    }

    public UUID getPlanId() {
        return planId;
    }

    public List<AuditEvent> getEventsToRevert() {
        return eventsToRevert;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void markExecuted() {
        this.executed = true;
    }

    @Override
    public String toString() {
        return "RollbackPlan{planId=" + planId + ", events=" + eventsToRevert.size() + ", executed=" + executed + "}";
    }
}
