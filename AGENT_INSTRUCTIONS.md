# Agent Instructions

This document lists strict instructions that all future AI agents working on this project must follow.

## Rules for Code Safety and Reference Materials

> [!CRITICAL]
> - **Read-Only Reference Sources**: The `ref_source/` directory is for reading and architectural analysis only. 
> - **No Copying**: Do not copy any implementation code, comments, or structures directly from `ref_source/` into the active modules.
> - **No Imports**: Do not import packages or classes from the reference code directories.
> - **No Gradle Includes**: Never add any subdirectory of `ref_source/` to `settings.gradle.kts` or `build.gradle.kts`.

## Architectural Boundaries

- **No Bukkit/Spigot/Paper APIs**: Do not add Bukkit, Spigot, Paper, or Purpur dependencies, classes, or imports. `worldProtect` is a native mod.
- **Thin Platform Adapters**: `worldprotect-fabric` and `worldprotect-neoforge` must remain thin wrapper modules. They must contain only bootstrapping, event listening, mixins, commands, and dependency injections.
- **Strict Separation of Concerns**: All core rules, region math, logic checks, database queries, and audit models must reside in the core modules (`worldprotect-core`, `worldprotect-protection`, `worldprotect-audit`).

## Implementation Best Practices

- **Incremental Tasks**: Avoid trying to implement multiple system features in one task. Keep edits focused and small.
- **Prefer Generic Approaches First**: Do not add custom compatibility modules for individual mods (like Applied Energistics 2, Create, or Refined Storage) until the generic inventory logging system has been fully implemented, tested, and proved insufficient for that specific mod.
- **Architectural Documentation**: If a design choice requires deviating from the initial plan, or introduces a major design decision, document it with an Architecture Decision Record (ADR) under `docs/decisions/`.
- **Reviewable Commits**: Keep pull requests and changes small, highly cohesive, and easy to review. Ensure unit tests are written for any new logic.

## Resource Validation Rules

- **No Hardcoded Mod IDs**: Do not hardcode lists of valid mod IDs inside code.
- **No Path/Folder Checking**: Never validate modded resource IDs by checking folder names, file existences, or static files.
- **Enforce Registry Views**: Any runtime resource ID semantic validation must go through the `ResourceRegistryView` abstraction interface.
- **Boundary Separation**: Never add Fabric, NeoForge, or Minecraft registry code directly to `worldprotect-minecraft`. Keep it purely abstract.
- **No Silent Normalization**: Never silently normalize user-supplied IDs to lowercase. Throw `IllegalArgumentException` on uppercase or invalid symbols immediately.

## Protection Query and Cause Validation Rules

- **Do Not Collapse Protection to Build/Break/Place**: Always map logical actions precisely (e.g. use `CONTAINER_OPEN`, `FLUID_SPREAD`, `INVENTORY_INSERT`, `WORLD_MODIFY` where appropriate) instead of treating everything as a generic build/break action.
- **Model Indirect Modifications with Cause Chains**: For automation, machines, or projectiles, always utilize a structured `CauseChain` (e.g., player -> item -> projectile -> explosion) rather than assuming players are the direct cause.
- **Independent Explosion and Drop Control**: Keep block damage (`EXPLOSION_BLOCK_DAMAGE`), entity damage (`EXPLOSION_ENTITY_DAMAGE`), and item drops (`EXPLOSION_ITEM_DROP` / `BLOCK_DROP` / `ENTITY_DROP`) as distinct, independently controllable check actions.
- **Treat Modded Item Use as First-Class**: Ensure wrench actions and modded block entities are resolved using appropriate cause type and target identifiers.


