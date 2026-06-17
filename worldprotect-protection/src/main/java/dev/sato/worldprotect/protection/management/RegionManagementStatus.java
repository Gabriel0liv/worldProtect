package dev.sato.worldprotect.protection.management;

/**
 * Status returned by region management operations.
 */
public enum RegionManagementStatus {
    SUCCESS,
    NOT_FOUND,
    ALREADY_EXISTS,
    VALIDATION_FAILED,
    CONFLICT,
    NO_CHANGE
}
