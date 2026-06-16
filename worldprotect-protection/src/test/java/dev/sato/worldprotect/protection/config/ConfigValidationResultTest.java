package dev.sato.worldprotect.protection.config;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public final class ConfigValidationResultTest {

    @Test
    public void testOkResultIsValid() {
        ConfigValidationResult result = ConfigValidationResult.ok();
        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        assertTrue(result.messages().isEmpty());
    }

    @Test
    public void testResultWithErrorIsInvalid() {
        ConfigValidationMessage error = ConfigValidationMessage.error("path.to.field", "Invalid format");
        ConfigValidationResult result = ConfigValidationResult.of(List.of(error));
        assertFalse(result.isValid());
        assertTrue(result.hasErrors());
        assertFalse(result.hasWarnings());
        assertEquals(1, result.errors().size());
        assertEquals(0, result.warnings().size());
    }

    @Test
    public void testResultWithWarningIsValidButHasWarnings() {
        ConfigValidationMessage warning = ConfigValidationMessage.warning("path.to.field", "Check this setting");
        ConfigValidationResult result = ConfigValidationResult.of(List.of(warning));
        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
        assertTrue(result.hasWarnings());
        assertEquals(0, result.errors().size());
        assertEquals(1, result.warnings().size());
    }

    @Test
    public void testMergeCombinesMessages() {
        ConfigValidationResult res1 = ConfigValidationResult.of(List.of(
                ConfigValidationMessage.error("path1", "Error 1")
        ));
        ConfigValidationResult res2 = ConfigValidationResult.of(List.of(
                ConfigValidationMessage.warning("path2", "Warning 2")
        ));

        ConfigValidationResult merged = res1.merge(res2);
        assertFalse(merged.isValid());
        assertTrue(merged.hasErrors());
        assertTrue(merged.hasWarnings());
        assertEquals(2, merged.messages().size());
        assertEquals("path1", merged.messages().get(0).path());
        assertEquals("path2", merged.messages().get(1).path());
    }

    @Test
    public void testAddReturnsNewResultWithoutMutatingOriginal() {
        ConfigValidationResult original = ConfigValidationResult.ok();
        ConfigValidationMessage msg = ConfigValidationMessage.error("path", "Something failed");
        
        ConfigValidationResult updated = original.add(msg);
        
        // Assertions for updated
        assertFalse(updated.isValid());
        assertEquals(1, updated.messages().size());
        
        // Assertions for original (must remain empty and valid)
        assertTrue(original.isValid());
        assertTrue(original.messages().isEmpty());
    }

    @Test
    public void testMessagesListIsImmutable() {
        ConfigValidationMessage msg = ConfigValidationMessage.error("path", "Error");
        List<ConfigValidationMessage> list = new ArrayList<>();
        list.add(msg);
        
        ConfigValidationResult result = ConfigValidationResult.of(list);
        
        // Attempt to modify input list - should not affect the result if defensive copy is made
        list.clear();
        assertEquals(1, result.messages().size());
        
        // Attempt to modify output list - should throw UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> {
            result.messages().clear();
        });
    }
}
