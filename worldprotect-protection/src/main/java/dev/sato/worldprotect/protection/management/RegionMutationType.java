package dev.sato.worldprotect.protection.management;

/**
 * Describes a region mutation kind for previews and future audit/save-back workflows.
 */
public enum RegionMutationType {
    CREATE,
    DELETE,
    SET_BOUNDS,
    SET_PRIORITY,
    SET_PARENT,
    CLEAR_PARENT,
    SET_FLAG,
    CLEAR_FLAG,
    ADD_SUBJECT,
    REMOVE_SUBJECT,
    SET_ACCESS_POLICY
}
