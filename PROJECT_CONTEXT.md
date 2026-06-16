# Project Context

## Motivation

Traditional servers running modded Minecraft (e.g. Forge, NeoForge, Fabric) often rely on hybrid servers (like Arclight, Mohist, or Cardboard) to run Bukkit/Spigot/Paper plugins such as WorldGuard and CoreProtect. However, this hybrid approach introduces severe limitations:

1. **Limited CoreProtect Modded Inventory Visibility**: CoreProtect running on Bukkit/Paper lacks visibility into custom modded inventories, workstations, automatic pipes, item transfer nodes, and energy-based networks.
2. **Limited WorldGuard Event Coverage**: WorldGuard relies on standard Bukkit events. It fails to intercept block placements, block modifications, or interactions triggered by custom modded items, spell systems, machinery, and energy tools.
3. **Suboptimal WorldEdit Integration**: Depending on the Bukkit version of WorldEdit on a hybrid server causes translation issues and compatibility lag. A native, loader-independent API is superior.
4. **Native Modded Solution**: `worldProtect` is designed to run natively within the modded game loop. It accesses native Minecraft interfaces, Fabric Transfer API, and NeoForge capabilities (`IItemHandler`), ensuring 100% accuracy in block modification checks and inventory logging.

## Workflow Conventions

To maintain architectural integrity and code quality, the team follows this development workflow:

1. **Human Design & Architectural Discussions**: The human developer designs the high-level architecture, outlines the modular boundaries, and reviews major architectural decisions.
2. **Scoped Agent Tasks**: AI agents implement scoped, granular tasks using modular layers. We avoid giant, monolithic changes to keep review overhead low.
3. **Code Review & Incremental Commits**: Every change is thoroughly verified with unit and integration tests. Changes must be reviewed and approved by the developer before moving to the next roadmap milestone.
