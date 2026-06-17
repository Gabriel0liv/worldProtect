package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.protection.config.ConfigValidationMessage;
import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import dev.sato.worldprotect.protection.region.RegionId;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionManagementResultTest {

    @Test
    public void testSuccessResultHasSuccessStatusAndValue() {
        RegionMutationPlan plan = RegionMutationPlan.of(
                RegionMutationType.CREATE,
                RegionId.of("spawn"),
                "create",
                Optional.empty(),
                Optional.empty()
        );

        RegionManagementResult<String> result = RegionManagementResult.success("ok", ConfigValidationResult.ok(), "done", plan);

        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals(RegionManagementStatus.SUCCESS, result.status());
        assertEquals("ok", result.value());
        assertTrue(result.mutationPlan().isPresent());
    }

    @Test
    public void testFailureResultHasDiagnosticsAndMessage() {
        ConfigValidationResult diagnostics = ConfigValidationResult.ok().add(
                ConfigValidationMessage.error("regions.spawn", "bad")
        );

        RegionManagementResult<String> result = RegionManagementResult.failure(
                RegionManagementStatus.VALIDATION_FAILED,
                diagnostics,
                "failed"
        );

        assertTrue(result.isFailure());
        assertEquals(RegionManagementStatus.VALIDATION_FAILED, result.status());
        assertNull(result.value());
        assertEquals("failed", result.message());
        assertEquals(1, result.diagnostics().errors().size());
    }

    @Test
    public void testNullArgumentsRejected() {
        assertThrows(NullPointerException.class, () -> RegionManagementResult.success("ok", null, "x"));
        assertThrows(NullPointerException.class, () -> RegionManagementResult.failure(RegionManagementStatus.CONFLICT, null, "x"));
        assertThrows(NullPointerException.class, () -> RegionMutationPlan.of(null, RegionId.of("spawn"), "x", Optional.empty(), Optional.empty()));
        assertThrows(NullPointerException.class, () -> RegionListRequest.of(null));
    }
}
