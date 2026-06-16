# WorldGuard Architectural Analysis & Lessons Learned

Analyzing WorldGuard (specifically version 7.0.x) provides valuable architectural inspiration for designing a modded-first protection system. Below are the key insights and design lessons that we can generalize for `worldProtect`.

## Protection Model Lessons from WorldGuard

### 1. Granular Flag Evaluation
* **Beyond Build/Break**: WorldGuard moves far beyond simple "build/break" binary checks. It separates concerns into specific action flags (e.g., `block-break`, `block-place`, `chest-access`, `vehicle-place`, `pvp`, `sleep`).
* **Fallback Mechanisms**: Specific flags (like `block-break`) are evaluated in tandem with a broader `build` flag. If the specific flag is unset, the system falls back to the general `build` rule. This hierarchy allows precise control without duplicating configuration.

### 2. Multi-Entity Cause Attribution
* **Indirect Causes**: Actions in Minecraft rarely have a simple 1:1 mapping from player to action. WorldGuard handles diverse causes, such as:
  * **Explosions**: Differentiating block destruction, entity damage, and item drops resulting from TNT, Creepers, Ghasts, or Withers.
  * **Automated systems**: Pistons moving blocks across region boundaries, or hoppers pulling items from containers.
* **Generalizing for Modded**: In modded environments, we must support even more indirect actors (e.g., mechanical wrenches, quarry blocks, custom pipes, auto-crafting terminals). Treating the cause of an action as a structured chain (e.g., Player -> Wrench Item -> Block Modify) is critical to determine the correct permission.

### 3. Separation of Concerns for Destructive Events
* **Explosions and Drops**: When a block explodes, the destruction of the block (`EXPLOSION_BLOCK_DAMAGE`), damage to nearby entities (`EXPLOSION_ENTITY_DAMAGE`), and the resulting item drops (`EXPLOSION_ITEM_DROP` / `BLOCK_DROP`) are distinct logical occurrences. WorldGuard provides separate flags for TNT vs. Creeper vs. Block Drops. 
* **Independent Controls**: A server administrator might want TNT to break blocks but not destroy items, or prevent entity damage while allowing environmental changes. A modded protection system must handle drops independently from the action that caused the block/entity destruction.

### 4. Abstraction Layer for Events
* **Platform Event Normalization**: WorldGuard uses a massive `EventAbstractionListener` to translate standard Bukkit events (which are platform-dependent, highly redundant, and messy) into cleaner, standardized, protection-specific events.
* **Lessons for worldProtect**: For our multi-platform NeoForge and Fabric model, this pattern is essential. Rather than spreading protection checks directly across various loader-specific event hooks, we should normalize incoming events into structured `ProtectionQuery` objects (containing action, cause, target, etc.) and let a centralized resolver make the decision.
