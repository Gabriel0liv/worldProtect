# Architecture

`worldProtect` uses a highly decoupled multi-module architecture designed to split the domain logic, region protection, audit logs, and modloader-specific integration layers.

## High-Level Design: Core vs. Platform (WorldEdit Inspiration)

Following the proven architecture of WorldEdit, `worldProtect` separates its design into core business logic and loader-specific platform adapters:
- **Core Modules (`worldprotect-core`, `worldprotect-protection`, `worldprotect-audit`)**: House pure Java code, domain models, and logic rules. They do not depend on loader APIs or Minecraft classes.
- **Platform Adapters (`worldprotect-fabric`, `worldprotect-neoforge`)**: Bind the core logic to the game loop. They intercept events (e.g. block placement, right-clicks) and forward them to the core services.

This ensures that the business logic can be tested entirely using lightweight JUnit tests without spinning up a heavy Minecraft server instance.

```mermaid
graph TD
    subgraph Core Logic
        core[worldprotect-core]
        protection[worldprotect-protection] --> core
        audit[worldprotect-audit] --> core
        compat[worldprotect-compat-api] --> core
    end
    
    subgraph Minecraft Abstraction
        minecraft[worldprotect-minecraft] --> core
        protection --> minecraft
        audit --> minecraft
        compat --> minecraft
    end

    subgraph Platform Adapters
        neoforge[worldprotect-neoforge] --> protection
        neoforge --> audit
        neoforge --> compat
        
        fabric[worldprotect-fabric] --> protection
        fabric --> audit
        fabric --> compat
    end
```

## Generic Inventory Strategy

Generic inventory access should start from platform-independent snapshots and later be backed by vanilla menus/block entities, NeoForge IItemHandler and Fabric Transfer API.

## Protection Query and Resolution Model

We model all world protection checks using a clean, platform-independent representation of actions, causes, and targets:
- **Action/Cause/Target Queries**: Every protection request is wrapped in a `ProtectionQuery` containing:
  - An explicit `Actor` responsible for the action.
  - A logical `ProtectionAction` representing the activity (e.g., `BUILD`, `BLOCK_BREAK`, `CONTAINER_OPEN`, `WORLD_MODIFY`). Modded interactions are normalized into these actions.
  - A `CauseChain` detailing how the action occurred (e.g., player -> wrench item -> block modify).
  - A `ProtectionTarget` detailing what is being acted upon (e.g., block, item, entity, container, fluid, or position).
- **Explosion & Drop Separation**: Explosions evaluate separate decisions for block damage (`EXPLOSION_BLOCK_DAMAGE`), entity damage (`EXPLOSION_ENTITY_DAMAGE`), and item drops (`EXPLOSION_ITEM_DROP`). Additionally, block/entity drops are protected independently (`BLOCK_DROP`, `ENTITY_DROP`) from the action that caused the destruction.
- **Loader Decoupling**: Fabric and NeoForge adapter modules will later translate physical game events into structured `ProtectionQuery` objects and delegate decision-making to the `ProtectionResolver`.

## Resource ID Validation Strategy

We employ a strict two-layered validation process for all Minecraft identifiers and resource keys:

1. **Syntactic Validation**: Checked immediately upon parsing inside `ResourceRef`. This verifies that the namespace and path conform to strict Minecraft character sets (only lowercase, digits, dots, dashes, and underscores/slashes) and correct formats, preventing config errors from executing.
2. **Semantic Validation**: Evaluated against the `ResourceRegistryView` using a `ResourceValidator`. This checks whether a syntactically correct identifier is actually registered on the current modpack/server runtime (e.g., verifying if a custom block or mod exists). Fabric and NeoForge adapter modules will later supply concrete implementations of this registry view from live game registries.

## Threading Rules

To ensure server performance and database integrity, we adhere to the following concurrency guidelines:

- **Asynchronous Database Log Queue**: All audit logging, database writes, and lookup queries must run asynchronously off the main server thread. Database operations must never block the main game loop.
- **Synchronous World Modifications**: Any operations modifying blocks, entities, or inventories (such as executing a rollback) MUST occur on the Minecraft server main thread to prevent race conditions and chunk corruption.
- **Rollback Planning Separation**: When performing a rollback, the system must generate a read-only `RollbackPlan` first (asynchronously). Once finalized, the plan is scheduled to apply its operations on the main thread incrementally.

## Loader Boundary Rules

- **Platform Modules are Adapters Only**: `worldprotect-fabric` and `worldprotect-neoforge` are adapters that translate platform-specific events (e.g., Fabric events or NeoForge event bus events) to `worldProtect` core API calls.
- **No Domain Logic in Loader Modules**: Code inside loader modules must only handle bootstrap, event registration, injection (mixins), configuration mapping, and command registration. Actual logic remains in `worldprotect-core`, `worldprotect-protection`, and `worldprotect-audit`.
