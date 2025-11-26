grindstone: true
enchant: true
deenchantment 缩写 de、den 权限:deenchantment.
# DeEnchantment

<!--<h4 align="center">☄️ A fully open-source cursed-enchantment plugin</h4>
<p align="center">
    <a href="https://www.oscs1024.com/project/oscs/Iseason2000/DeEnchantment?ref=badge_small" aria-label="OSCS Status"><img src="https://www.oscs1024.com/platform/badge/Iseason2000/DeEnchantment.svg?size=small" alt="OSCS"/></a>
    <a href="https://www.codefactor.io/repository/github/iseason2000/deenchantment" aria-label="CodeFactor"><img src="https://www.codefactor.io/repository/github/iseason2000/deenchantment/badge" alt="CodeFactor"/></a>
    <a href="https://sonarcloud.io/summary/new_code?id=Iseason2000_DeEnchantment" aria-label="Security rating"><img src="https://sonarcloud.io/api/project_badges/measure?project=Iseason2000_DeEnchantment&metric=security_rating" alt="Security"/></a>
    <a href="https://sonarcloud.io/summary/new_code?id=Iseason2000_DeEnchantment" aria-label="Lines of code"><img src="https://sonarcloud.io/api/project_badges/measure?project=Iseason2000_DeEnchantment&metric=ncloc" alt="LoC"/></a>
    <a href="https://sonarcloud.io/summary/new_code?id=Iseason2000_DeEnchantment" aria-label="Maintainability"><img src="https://sonarcloud.io/api/project_badges/measure?project=Iseason2000_DeEnchantment&metric=sqale_rating" alt="Maintainability"/></a>
    <a href="https://bstats.org/plugin/bukkit/DeEnchantment/13440" aria-label="bStats servers"><img src="https://img.shields.io/bstats/servers/13440?color=brightgreen" alt="bStats servers"/></a>
    <a href="https://bstats.org/plugin/bukkit/DeEnchantment/13440" aria-label="bStats players"><img src="https://img.shields.io/bstats/players/13440?color=brightgreen" alt="bStats players"/></a>
</p>-->

> English | [中文](README.md)

DeEnchantment injects more than forty custom "anti-enchantments" into Paper servers. Items can mutate into curses while enchanting, looting, bartering, fishing, trading, or interacting with Slimefun/EcoEnchants machines. Version 2 rewrites the majority of the original project in modern Java with clearer architecture, hot reload flows, and compatibility hooks.

## ✨ Key features

- 40+ configurable curses with custom lore, descriptions, and behaviors
- Hot reload support for both global config and individual curse definitions
- Built-in migration from legacy V1 installations
- Optional hooks for EcoEnchants (lore sync) and Slimefun auto machines
- Per-controller toggles for loot, trades, fishing, natural mobs, rewards, anvils, and grindstones
- Rich command suite for admins (add, give, randomize, purify, migrate, reload)
- Metrics via bStats plus structured logging with on-demand debug output

## 📦 Requirements

- Java 17 runtime (matching the Paper 1.20.x toolchain)
- Paper / Folia 1.20+ (or any fork exposing the Paper API)
- Optional dependencies:
  - [EcoEnchants](https://modrinth.com/plugin/ecoenchants) 9.15.3+ for lore synchronization
  - [Slimefun4](https://github.com/Slimefun/Slimefun4) RC-35+ for auto enchanter/disenchanter support
  - PlaceholderAPI for message placeholders (already compileOnly)

## 🚀 Installation

1. Download the latest release from GitHub Actions or build it yourself following the instructions below.
2. Copy `DeEnchantment-<version>.jar` into your server's `plugins/` directory.
3. (Optional) Install EcoEnchants and/or Slimefun4 if you want their integrations. The plugin auto-detects them at runtime through the new hook manager.
4. Start the server once to generate configuration files under `plugins/DeEnchantment/`.
5. Edit `config.yml`, `messages.yml`, and each entry in `DeEnchantments.yml` to fit your economy/balance goals, then run `/deenchantment reload`.

## 🧩 Optional integrations

| Hook | Purpose | Notes |
| --- | --- | --- |
| EcoEnchantsHook | Writes curse names/descriptions into EcoEnchants' `vanillaenchants.yml` and refreshes its display cache. | Automatically retries when Eco's display cache is busy and falls back to legacy file names. |
| SlimefunHook | Refreshes lore on Auto Enchanter / Auto Disenchanter output slots. | Runs asynchronously to avoid blocking Slimefun machines. |
| PlaceholderApiHook | Expands PlaceholderAPI tokens in every plugin message before they're sent. | Requires PlaceholderAPI to be installed; otherwise messages behave normally. |

Hooks are enabled on demand via the runtime `HookManager`, so missing dependencies never break startup.

## 🧙 Command suite

| Command | Description | Permission |
| --- | --- | --- |
| `/deenchantment add <curse> [level]` | Applies a specific curse to the item in hand. | `deenchantment.add` |
| `/deenchantment give <player> <curse> [level]` | Gives a player a curse book. | `deenchantment.give` |
| `/deenchantment random <type> <player> [level]` | Rolls random curses for the target type (armor, weapon, etc.). | `deenchantment.random` |
| `/deenchantment update` | Refreshes lore on the held item to match current definitions. | `deenchantment.update` |
| `/deenchantment pur [player]` | Purifies the held item, swapping curses back into normal enchants. | `deenchantment.purification` |
| `/deenchantment reload` | Reloads configs, curse registry, controllers, and hooks. | `deenchantment.reload` |
| `/deenchantment migrate` | Converts V1 configuration/data into the V2 layout. | `deenchantment.migrate` |

All commands default to OP-only but can be delegated through standard Bukkit permission nodes.

## ⚙️ Configuration overview

Global toggles live inside `config.yml` and mirror the original plugin's behavior:

```yml
anvil: true              # Allow curses when combining items in anvils
grindstone: true         # Apply curse logic to grindstone disenchanting
enchant: true            # Enable curse mutations in enchanting tables
chestLoot: true          # Inject curses into generated loot tables
spawn: true              # Give naturally spawned mobs cursed gear
trade: true              # Let villager trades roll curses
fishing: true            # Curse fishing loot
reward: true             # Piglin barters / raid rewards can contain curses
levelUnlimited: false    # Enforce vanilla enchant level caps
tooExpensive: false      # Bypass "Too Expensive" anvil limit
cleanConsole: false      # Trim verbose debug output
allowDescription: true   # Write curse descriptions into lore
lorePosition: 0          # Inject lore at a specific index
enchantsPermission: false # Require custom permission per curse
debug: false             # Enable verbose logger output
```

Every entry in `DeEnchantments.yml` (or split files under `curses/`) exposes deeper tuning:

```yml
DE_SOUL_BOUND:
  enabled: true
  translate-name: "§aSoul Bound"
  description: "§8- This item is linked to its first wearer"
  target: BREAKABLE
  chance: 0.2
  max-level: 1
  conflicts:
    - DE_VANISHING_CURSE
  bind-message: "&aYour soul now binds this gear"
  owner-message: "&cBound gear rejects other players!"
  deny-anvil: true
  deny-grindstone: true
  deny-pickup: true
  placeholder: player
```

Consult `docs/ARCHITECTURE.md` for a deeper overview of services, schedulers, and controller lifecycles.

## 🛠️ Building from source

```bash
./gradlew :plugin:shadowJar
```

The shaded artifact will appear in `plugin/build/libs/`. Run `./gradlew build` from the repository root if you want to compile every module.

## 📊 Telemetry

The plugin ships with [bStats](https://bstats.org/plugin/bukkit/DeEnchantment/13440). Metrics are anonymous and can be disabled by editing `plugins/bStats/config.yml`.

## 📄 License

DeEnchantment is distributed under the terms of the [GNU General Public License v3](LICENSE).

---

![bStats banner](https://bstats.org/signatures/bukkit/DeEnchantment.svg)
