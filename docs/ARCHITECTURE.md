# DeEnchantment Java Rewrite – Architecture Overview

## High-level goals

- Fully replace the legacy Kotlin/obfuscated codebase with an idiomatic Java 17 design.
- Keep the plugin modular, testable, and easy to extend for future curse mechanics.
- Support every Minecraft/Paper release from **1.20.1 and below** up to the latest **1.21.x** line by abstracting API differences.
- Remove the dependency on the old `bukkittemplate` runtime and instead provide lightweight internal services for configuration, commands, logging, hooks, and scheduling.

## Module layout

```
root
├── plugin (Paper plugin module)
│   └── src/main/java/icu/nyat/kusunoki/deenchantment
│       ├── bootstrap  – plugin lifecycle + service wiring
│       ├── command    – Brigadier-style command tree powered by cloud-commandframework
│       ├── config     – YAML-backed config/message/curse catalogs
│       ├── curse      – Custom enchant/curse registry + lore utilities
│       ├── curse/handler – One handler per curse, each a listener with focused logic
│       ├── hook       – Optional integrations (EcoEnchants, Slimefun4, PlaceholderAPI)
│       ├── listener   – Global controllers for anvils, enchanting, loot, trades, etc.
│       ├── task       – Scheduler-backed background jobs (equipment scanner, target finder…)
│       ├── util       – Shared helpers (text, items, math, collections)
│       └── version    – Capability detection + safe reflection helpers for registries
└── legacy            – archived Kotlin sources for reference only (not built)
```

## Key services

| Service | Purpose |
| --- | --- |
| `PluginContext` | Central dependency container created on `onLoad` and disposed on `onDisable`. Provides access to configs, registries, hooks, and schedulers. |
| `ConfigService` | Wraps Bukkit’s `FileConfiguration` with strongly typed records for `plugin.yml`, `config.yml`, `messages.yml`, and `DeEnchantments.yml`. Generates defaults from `CurseId` enum. |
| `CurseRegistry` | Manages `CustomCurse` objects (extending `Enchantment`) plus their runtime handlers. Handles lore/rendering, permission gates, lore positions, and metadata storage via `PersistentDataContainer`. |
| `ControllerManager` | Registers listeners that control how curses spread: anvil, grindstone, enchanting table, loot tables, villager trades, fishing, random equipment spawn, etc. Each controller consults `PluginConfig` toggles. |
| `HookManager` | Lazily detects optional plugins (PlaceholderAPI, EcoEnchants, Slimefun4, Eco) and exposes unified features such as placeholder expansion or recipe injection. |
| `CommandService` | Provides the `/deenchantment` root command with subcommands (`/add`, `/give`, `/random`, `/reload`, `/update`, `/migrate`, `/purification`). Uses Cloud Command Framework with Brigadier suggestions on modern Paper and legacy tab completion fallback on 1.20.1 and below. |
| `VersionBridge` | Encapsulates the differences between legacy `Enchantment.registerEnchantment` (≤1.20.1) and the new `Bukkit.getUnsafe().registerEnchantment` / registry freezing logic (≥1.20.2). |
| `TaskScheduler` | Wraps Bukkit scheduler to run repeating jobs asynchronously (equipment scanner / target finder) with automatic shutdown. |

## Curse pipeline

1. **Definition** – Each curse is described by `CurseId` (enum) and a user-editable entry in `DeEnchantments.yml` (enable flag, translation, description, target, level cap, conflicts, chance, lore placement, permissions).
2. **Registration** – During enable, `CurseRegistry` loads config, instantiates `CustomCurse` objects, and registers them using `VersionBridge`. All enabled curses also register their `Listener` handler through `HandlerBinder`.
3. **Lore + Metadata** – `LoreService` injects names/descriptions into item lore using persistent data keys so they can be updated or removed cleanly when curses change.
4. **Controllers** – `ControllerManager` consults `ConfigService` toggles to decide where curses can appear (anvil, enchant, loot, spawn, fishing, trades, rewards). Each controller emits or listens for `CurseEvent` abstractions to keep logic isolated from Bukkit events.
5. **Tasks** – Background scanners (e.g., `EquipmentScannerTask`) enforce binding curses, update lore, and run scheduled gameplay effects.

## Compatibility strategy

- Prefer the **Paper API** (1.21.1) for compileOnly while shading nothing from the server.
- For <1.20.2 servers, fall back to `Enchantment.registerEnchantment` reflection guard.
- For ≥1.20.2, rely on `Bukkit.getUnsafe().registerEnchantment` plus `Registry.ENCHANTMENT`. `VersionBridge` handles unfreezing/freezing and removal during reload.
- Controllers avoid NMS entirely and rely on Bukkit abstractions so they remain stable across releases.

## Observability & metrics

- `bStats` is shaded and started via `MetricsService`.
- Debug logging uses structured `PluginLogger` with rate limiting to avoid floods when background tasks scan frequently.

## Testing hooks

- `plugin/src/test/java` will contain lightweight unit tests for config parsing and curse math (e.g., probability calculations).
- Integration sanity checks can be run on Paper dev servers via provided Gradle tasks (`runServer` optional addition later).

This architecture keeps gameplay code intentionally separated from infrastructure layers so future contributors can drop in new curses, add hooks, or alter controllers without touching the rest of the system.
