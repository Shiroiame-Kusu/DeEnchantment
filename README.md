# 负魔书 DeEnchantment

> 中文 | [English](README.en.md)

DeEnchantment 是一款面向 Paper/Folia 服务器的全功能负魔插件。超过四十种「反向附魔」可以在附魔台、战利品、交易、钓鱼、怪物掉落等环节随机生成，也可以通过管理员指令直接控制。V2 版本使用现代化 Java 架构重新实现，大幅提升性能、可维护性与可配置度，并内置多款常见插件兼容。

## ✨ 主要特性

- 40+ 款可自定义的负魔，支持独立名称、描述与行为定义
- 全局配置与负魔定义支持热重载
- 内置 V1 -> V2 数据迁移
- 自动检测并对接 EcoEnchants（同步描述）与 Slimefun 自动附魔/祛魔机
- 精细的玩法开关：附魔、战利品、钓鱼、交易、自然生成、奖励、铁砧、砂轮等
- 完整的管理指令集：添加、赠送、随机、净化、迁移、重载
- bStats 指标 + 结构化日志，可随时打开调试模式

## 📦 环境要求

- Java 17 运行时（与 Paper 1.20.x 工具链一致）
- Paper / Folia 1.20+ 或兼容分支
- 可选依赖：
  - [EcoEnchants](https://modrinth.com/plugin/ecoenchants) 9.15.3+（同步负魔描述）
  - [Slimefun4](https://github.com/Slimefun/Slimefun4) RC-35+（自动附魔/祛魔机输出同步）
  - PlaceholderAPI（用于消息占位符，已作为 compileOnly 依赖）

## 🚀 安装步骤

1. 从 Release 或自行构建获取最新版 `DeEnchantment-<version>.jar`。
2. 将 Jar 拷贝到服务器 `plugins/` 目录。
3. （可选）安装 EcoEnchants / Slimefun4，插件会在启动时通过 HookManager 自动检测并启用兼容。
4. 启动服务器以生成 `plugins/DeEnchantment/` 下的配置文件。
5. 根据需要修改 `config.yml`、`messages.yml` 与 `DeEnchantments.yml`（或 `curses/` 子文件），再执行 `/deenchantment reload` 应用变更。

## 🧩 可选兼容

| Hook | 作用 | 说明 |
| --- | --- | --- |
| EcoEnchantsHook | 将负魔名称与描述写入 Eco 的 `vanillaenchants.yml` 并刷新 DisplayCache | 支持新老配置路径，失败时后台自动重试 |
| SlimefunHook | 在 Auto Enchanter / Auto Disenchanter 产出的物品上刷新负魔 Lore | 在异步线程中执行，避免阻塞机器运作 |
| PlaceholderApiHook | 在发送前解析所有消息中的 PlaceholderAPI 占位符 | 仅在服务器安装 PlaceholderAPI 时启用 |

## 🧙 指令列表

| 指令 | 功能 | 权限 |
| --- | --- | --- |
| `/deenchantment add <curse> [level]` | 将指定负魔附加到手持物品 | `deenchantment.add` |
| `/deenchantment give <player> <curse> [level]` | 给玩家一本负魔书 | `deenchantment.give` |
| `/deenchantment random <type> <player> [level]` | 为指定类型随机生成负魔（如武器/护甲） | `deenchantment.random` |
| `/deenchantment update` | 更新手持物品的 Lore 与负魔展示 | `deenchantment.update` |
| `/deenchantment pur [player]` | 将手持物品的负魔净化为普通附魔 | `deenchantment.purification` |
| `/deenchantment reload` | 重载配置、诅咒注册、控制器与 Hook | `deenchantment.reload` |
| `/deenchantment migrate` | 将 V1 配置迁移至 V2 | `deenchantment.migrate` |

默认仅 OP 拥有所有权限，可以通过权限插件分配。

## ⚙️ 配置概览

`config.yml` 中的核心开关：

```yml
anvil: true              # 是否在铁砧合成时处理负魔
grindstone: true         # 是否在砂轮祛魔时处理
enchant: true            # 是否在附魔台生成负魔
chestLoot: true          # 是否注入战利品表
spawn: true              # 自然生成的怪物是否携带负魔装备
trade: true              # 是否影响村民交易
fishing: true            # 钓鱼战利品
reward: true             # 猪灵交易 / 村庄英雄掉落
levelUnlimited: false    # 是否突破附魔等级上限
tooExpensive: false      # 是否忽略「需要过多经验」限制
cleanConsole: false      # 精简控制台输出
allowDescription: true   # 在 Lore 中显示负魔描述
lorePosition: 0          # Lore 插入位置
enchantsPermission: false # 是否需要单独权限控制负魔
debug: false             # 打开调试日志
```

`DeEnchantments.yml` 或单独的 `curses/*.yml` 用于定义每个负魔：

```yml
DE_SOUL_BOUND:
  enabled: true
  translate-name: "§a灵魂绑定"
  description: "§8 - 绑定玩家灵魂，仅限本人使用"
  target: BREAKABLE
  chance: 0.2
  max-level: 1
  conflicts:
    - DE_VANISHING_CURSE
  bind-message: "&a装备已与你的灵魂绑定"
  owner-message: "&c你无法使用他人的灵魂装备"
  deny-anvil: true
  deny-grindstone: true
  deny-pickup: true
  placeholder: 玩家
```

更多服务与控制器架构详见 [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)。

## 🛠️ 源码构建

```bash
./gradlew :plugin:shadowJar
```

产物位于 `plugin/build/libs/`。如果希望一次构建全部模块，可以在仓库根目录运行 `./gradlew build`。

## 📊 遥测

插件内置 [bStats](https://bstats.org/plugin/bukkit/DeEnchantment/13440) 统计，可在 `plugins/bStats/config.yml` 关闭。

## 📄 协议

项目采用 [GNU General Public License v3](LICENSE) 许可。
