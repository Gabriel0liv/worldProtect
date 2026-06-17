package dev.sato.worldprotect.config.persistence;

public enum ConfigRepositoryStatus {
    SUCCESS,
    NO_CHANGE,
    LOAD_FAILED,
    MUTATION_FAILED,
    VALIDATION_FAILED,
    WRITE_FAILED,
    SERIALIZATION_FAILED
}
