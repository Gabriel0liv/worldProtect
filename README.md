# worldProtect

`worldProtect` is a native Minecraft mod designed to provide advanced world protection, event logging, and rollback features directly within modded Minecraft environments.

## What is worldProtect?

Unlike traditional Bukkit, Spigot, or Paper plugins, `worldProtect` is **not** a Bukkit/Paper plugin. It is built from the ground up as a native Minecraft mod designed to run on modded servers. 

The core goal of `worldProtect` is to combine the capabilities of:
- **WorldGuard-style region protection**: Restricting player interactions, building, and modification rights based on configurable areas and flags.
- **CoreProtect-style audit logging, lookup, and rollback**: Tracking block edits, container transactions, and entity interactions with the ability to query and undo changes.
- **Optional WorldEdit integration**: Exposing APIs and selection listeners for region creation and operational logging.
- **Deep modded compatibility**: Providing first-class support for modded blocks, block entities, custom inventories, items, and entities.

## Target Platforms

`worldProtect` targets Minecraft version **1.20.1** and is designed from the beginning with a loader-independent architecture to support:
- **Fabric**
- **NeoForge**

## Repository Structure & Reference Sources

- `/worldprotect-core`: Pure domain logic (Actor, Region, Decision, etc.), independent of Minecraft.
- `/worldprotect-minecraft`: Loader-independent Minecraft wrappers (BlockPosRef, BlockSnapshot, etc.).
- `/worldprotect-protection`: Region models, flag management, and protection checks.
- `/worldprotect-audit`: Logging, transaction models, and rollback logic.
- `/worldprotect-compat-api`: Custom compatibility APIs for complex modded interactions.
- `/worldprotect-worldedit-bridge`: Selection import and logging integration.
- `/worldprotect-fabric`: Fabric loader entrypoints and event adapters.
- `/worldprotect-neoforge`: NeoForge loader entrypoints and event adapters.
- `/ref_source`: Contains reference source code (CoreProtect, WorldGuard, and WorldEdit) for analysis **only**.

### Reference Source Rules
> [!CRITICAL]
> - Do **not** copy implementation code from `ref_source/` into this project.
> - Do **not** add `ref_source/` to Gradle includes.
> - Do **not** import any classes from `ref_source/` libraries.
> - `ref_source/` is strictly read-only reference material.

## License

This project is licensed under TBD. See [LICENSE.md](LICENSE.md) for details.
