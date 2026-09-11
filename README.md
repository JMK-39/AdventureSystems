# Adventure Systems

[English](#english) | [简体中文](#简体中文)

## English

### Overview

A player-progression and adventure utility suite centered on Curios equipment, wallet/currency and shop mechanics, FTB Quests navigation and submission helpers, and context-sensitive tutorial or story tips.

The project is designed around in-game administration. Where a feature changes shared gameplay data or server rules, the server remains authoritative; client-only presentation features stay local to the client. Configuration screens use KineticCore's UI and configuration infrastructure.

### Key Features

- Curios equipment utilities and configurable accessory behaviors.
- Wallet, currency and shop systems with server-authoritative rules.
- FTB Quests item/tag/NBT bindings, quick navigation and repeated submission helpers.
- Contextual tips filtered by dimension, biome, structure, advancement, inventory and Curios equipment.
- Language-aware tip data and in-game visual editing.
- Optional integrations with Refined Storage, Sophisticated Backpacks and JEI.

### Requirements and Compatibility

| Type | Dependency |
|---|---|
| Required | Minecraft 1.20.1 |
| Required | Minecraft Forge 47+ |
| Required | KineticCore 26.9.8+ |
| Required | Curios 5.10+ |
| Required | FTB Library |
| Required | FTB Quests |
| Required | FTB Teams |
| Optional | Refined Storage |
| Optional | Sophisticated Backpacks |
| Optional | JEI |

### Access and Configuration

- Open the KineticCore configuration center with its configured F6 entry and select **Adventure Systems**.
- Server-owned settings are saved by the server and synchronized where the feature requires client awareness.
- Client-only presentation settings remain local.
- Individual feature areas document their own data/configuration paths below.
- Search, list selection, item/entity inspection, tooltips and return/navigation controls reuse KineticCore UI components where available.

## Detailed Feature Reference

### Curios, Wallet & Shop

#### Overview

**Curios and Wallet Systems** is an accessory, wallet, and wallet-shop mod. It provides configurable accessory mechanics, currency storage and exchange, shop transactions, and optional integrations with quests, villager trading, Sophisticated Backpacks, and Refined Storage.

#### Key Features

- Currency wallet with deposit, withdrawal, exchange, magnet pickup and HUD.
- Protected/manual-only currencies.
- Configurable wallet shop with buy/sell entries, limits, quest requirements and weighted reward pools.
- Material consumption from player inventory, Sophisticated Backpacks and bound Refined Storage networks.
- Heart of Steel accessory with combat stacking and configurable health/damage behavior.
- Paradise Lost accessory with food-variety based mechanics.
- Levitation Backpack accessory and knockback-related behavior.
- Curios slot/conflict handling and tooltip integration.

#### Configuration

```text
config/kineticcore/curios.toml
config/kineticcore/currency_wallet_shop.toml
config/kineticcore/currency_wallet_shop_client.toml
```

### Feature Reference
#### Config Details
| Item | Description |
|---|---|
| **Wallet HUD** | Adjust the wallet balance overlay for this client. Changes apply immediately and do not modify server settings. |
| **Edit Position and Scale** | Opens a live preview editor. Drag the overlay to move it, scroll while hovering to scale it, or use arrow keys for precise movement. |
| **Curio and Wallet Mechanics** | Server-authoritative accessory mechanics and currency rules. Both singleplayer and multiplayer read and save through the active server; editing requires permission level 2. |
| **Wallet Shop Editor** | Edit the wallet shop through a server snapshot. Remote editing requires permission level 2, and the server validates and saves every change. |
| **Open Shop Editor** | Requests the shop snapshot from the current server and enters edit mode directly. No equipped wallet is required; the server rejects insufficient permissions. |
| **Enable Heart of Steel** | Controls whether Heart of Steel is registered as a functional accessory. A game or server restart is required. |
| **Enable Paradise Lost** | Controls whether Paradise Lost is registered as a functional accessory. A game or server restart is required. |
| **Enable Levitation Backpack** | Controls whether the Levitation Backpack is registered as a functional accessory. A game or server restart is required. |
| **Enable Currency Wallet** | Controls whether the Currency Wallet and its currency features are enabled. A game or server restart is required. |
| **Charge Cooldown (Seconds)** | Cooldown after a charged attack successfully grants stacks. Default: 3 seconds. |
| **Bonus Damage Cap** | Absolute cap on final bonus damage converted from maximum health. Default: 3. |
| **Stacked Health Cap** | Maximum bonus health obtainable from stacks, excluding the base health bonus. Default: 200. |
| **Stacks per Health Point** | Number of stacks converted into 1 maximum health point. Must be at least 1. Default: 100. |
| **Base Health Bonus** | Maximum health granted immediately while Heart of Steel is equipped, without stacks. Default: 10. |
| **Healing Multiplier** | Base multiplier used when a charged attack heals from damage dealt. Default: 2. |
| **Damage Window (Ticks)** | Duration during which bonus damage can apply after charging. 20 ticks equal 1 second. Default: 5 ticks. |
| **Minimum Stacks per Hit** | Minimum random stacks from one successful charged attack. It should not exceed the maximum. Default: 2. |
| **Maximum Stacks per Hit** | Maximum random stacks from one successful charged attack. It should not be below the minimum. Default: 5. |
| **Damage Coefficient per Health** | Ratio of maximum health converted to bonus damage. 0.005 means 0.5%, still limited by the damage cap. |
| **Conflicting Accessory IDs** | Heart of Steel is disabled while any listed accessory is equipped. Enter one full item ID per line, such as enigmaticlegacy:cursed_ring. |
| **Minimum Nutrition** | Foods with nutrition at or below this value do not count toward variety. A value of 0 ignores foods that restore no hunger. |
| **Conflicting Accessory IDs** | Paradise Lost is disabled while any listed accessory is equipped. Enter one full item ID per line. |
| **Extra Belt Slots** | Additional Curios belt slots granted while the wallet is equipped. Re-equipping may be required after a change. |
| **Maximum Currency Types** | Maximum currency types read by the UI and currency system. The runtime hard cap is 10. |
| **Currency Magnet Radius** | Radius in blocks for collecting currency drops while the wallet is equipped. Values at or below 0 disable the magnet. |
| **Magnet Check Interval (Ticks)** | Interval between nearby-currency scans. Higher values reduce server work but increase pickup delay; 20 ticks equal 1 second. |
| **Currency Definitions** | One item_id\|base_value entry per line; value must be positive. Example: kubejs:gold_coin\|1. |
| **One-Way Exchange Rules** | One source_item_id->target_item_id entry per line for change-making and upgrades. Add the reverse direction separately when needed. |
| **Manual-Exchange-Only Currencies** | These currencies are never auto-consumed or split by villager trades, the wallet shop, or FTB virtual payments. Enter one item ID per line. |

#### Item and HUD
| Item | Description |
|---|---|
| **Gourmet's Law (Infinite Growth):** | Devour various foods to permanently gain final damage bonus. |

#### Editable Options
- Balance Overlay
- Module Toggles
- Heart of Steel
- Paradise Lost
- Currency Wallet

#### Config Defaults
| Key | Default |
|---|---|
| `currency_wallet.extraBeltSlots` | `1` |
| `currency_wallet.hudOffsetX` | `0` |
| `currency_wallet.hudOffsetY` | `0` |
| `currency_wallet.hudScale` | `0.75` |
| `currency_wallet.magnetIntervalTicks` | `10` |
| `currency_wallet.magnetRange` | `6.0` |
| `currency_wallet.maxCurrencyTypes` | `10` |
| `general.enable_currency_wallet` | `true` |
| `general.enable_heart_of_steel` | `true` |
| `general.enable_levitation_backpack` | `true` |
| `general.enable_paradise_lost` | `true` |
| `heart_of_steel.baseHealth` | `10.0` |
| `heart_of_steel.damageCap` | `3.0` |
| `heart_of_steel.damagePerHp` | `0.005` |
| `heart_of_steel.damageWindowTicks` | `5` |
| `heart_of_steel.growthInterval` | `3` |
| `heart_of_steel.healMultiplier` | `2.0` |
| `heart_of_steel.maxGain` | `5` |
| `heart_of_steel.maxHealthCap` | `200` |
| `heart_of_steel.minGain` | `2` |
| `heart_of_steel.stacksPerHp` | `100` |
| `paradise_lost.minNutrition` | `0` |

#### Data Paths
Primary configuration/data paths:

- `config/kineticcore/curios.toml`
- `config/kineticcore/currency_wallet_shop.toml`
- `config/kineticcore/currency_wallet_shop_client.toml`

### FTB Quests Integration

#### Overview

**FTB Quests Integration** integrates with **FTB Quests**. It binds items, item tags and NBT variants to quest pages, provides direct quest navigation from item tooltips, and improves repeated submission for supported FTB item tasks.

#### Key Features

- Quest-jump tooltip for bound items.
- `G` opens the default linked quest.
- `Alt + G` opens the multi-quest selector.
- Per-item favorite/default quest selection.
- Item, tag and fuzzy-NBT binding rules.
- Item/tag/NBT blacklist support.
- Visual binding editor in the F6 configuration center.
- Multi-submit support for eligible repeatable FTB ItemTasks.
- Server-side task submission remains authoritative.
- Local client switch for tooltip and hotkey navigation.
- Optional JEI integration.

#### Configuration

```text
config/kineticcore/ftb_item_client.toml
config/kineticcore/ftb_item_quest_bindings.json
config/kineticcore/ftb_quest_favorites.json
config/kineticcore/ftb_item_blacklist.json
```

### Feature Reference
#### Config Details
| Item | Description |
|---|---|
| **Enable Quest Task Jump** | Shows quest-task links and enables the task jump shortcut for matching items. |
| **Open Item Binding Editor** | Edit FTB task item bindings, favorites, and blacklist entries. |

#### GUI and Editors
| Item | Description |
|---|---|
| **submit** | Enter how many exchanges to submit. |

#### Item and HUD
| Item | Description |
|---|---|
| **Favorite Quest** | Right-click to set this task as the default open target |
| **Search Quest** | Search quests linked to the current item. |

#### Editable Options
- [Mod Global]
- [Item Tag]

#### Config Defaults
| Key | Default |
|---|---|
| `enableTaskJump` | `true` |

#### Data Paths
Primary configuration/data paths:

- `config/kineticcore/ftb_item_blacklist.json`
- `config/kineticcore/ftb_item_client.toml`
- `config/kineticcore/ftb_item_quest_bindings.json`
- `config/kineticcore/ftb_quest_favorites.json`

### Contextual Tips

#### Overview

**Contextual Tips** is a contextual tip system for modpacks, servers, tutorials, and story-driven gameplay. Tips can appear during loading, in-game, or in any stage, and can be filtered by the player's current dimension, biome, structure, advancements, inventory items, and Curios equipment.

Tip data is server-authoritative and language-aware. Administrators can maintain it through an in-game visual editor instead of manually editing complex JSON.

#### Main Features

##### Multi-Stage Tips

Each tip can target:

- `loading`
- `game`
- `any`

Each entry also has its own display duration.

##### Context Conditions

Tips may require one or more of the following:

- Dimension
- Biome
- Structure
- Advancement
- Inventory item
- Curios item

Only tips whose conditions match the current player are eligible for display.

##### NBT Matching
Item-based conditions support:

- `NONE`: item type only
- `WEAK`: weak NBT matching
- `STRONG`: strict NBT matching

This is useful for quest items, custom equipment, guns, accessories, and other NBT-heavy items.

##### Per-Language Tip Files

Typical files include:

- `tips_zh_cn.json`
- `tips_en_us.json`

Clients report their selected language and the server sends the matching runtime tip snapshot. English is used as a fallback when the requested language file is unavailable.

##### Visual Editor

Administrators can:

- Add, edit, delete, and search tips.
- Edit formatted text with Minecraft `§` formatting codes.
- Set stage and duration.
- Select dimension, biome, structure, and advancement conditions.
- Add inventory-item and Curios requirements through selectors.
- Cycle NBT match modes.
- Remove conditions with right-click actions.

The server validates resource IDs, text length, NBT syntax, duration limits, and entry counts before accepting data.

##### Server Sync
- Tip data is loaded from the server.
- Editing requires administrator permission.
- The server validates every save request again.
- Updated language JSON is written server-side.
- Large JSON payloads use KineticCore's compressed network transport with size limits.
- Updated snapshots are broadcast only to clients using the relevant language.
- Integrated singleplayer servers use the same save path.

#### Configuration

Base settings:

- `config/kineticcore/tips_settings.toml`

Tip directory:

- `config/kineticcore/tips/`

Typical language files:

- `config/kineticcore/tips/tips_zh_cn.json`
- `config/kineticcore/tips/tips_en_us.json`

### Feature Reference
#### Config Details
| Item | Description |
|---|---|
| **Screen Tip Settings** | Controls custom tips on loading, world-transition, and pause screens. Edit the basic toggle here and use the dedicated editor for tip content and conditions. |
| **Enable Screen Tips** | Disabling this hides custom loading, world-transition, and pause tips without deleting tip data. |
| **Open Tip Editor** | Edit tip text, stage, duration, biome/structure/dimension/advancement conditions, and item/Curios NBT conditions. Opening the editor on a multiplayer server requires permission level 2. |

#### GUI and Editors
| Item | Description |
|---|---|
| **[Tip]** | Enter seconds (e.g., 5.0) |

#### Config Defaults
| Key | Default |
|---|---|
| `enableTips` | `true` |

#### Data Paths
Primary configuration/data paths:

- `config/kineticcore/tips`
- `config/kineticcore/tips/`
- `config/kineticcore/tips/tips_en_us.json`
- `config/kineticcore/tips/tips_zh_cn.json`
- `config/kineticcore/tips_settings.toml`

### Building from Source

- Minecraft: `1.20.1`
- Java: `17`
- ForgeGradle: `6.0.24`
- Gradle: the project is pinned to the `8.1.1` Wrapper; do not import it with Gradle 9 directly.
- Local development JARs are controlled by `local_libs_dir` and can be overridden in `gradle.properties` or with a project property.
- Typical build command: `gradlew.bat build` on Windows or `./gradlew build` on Linux/macOS.
- Development and release artifacts use `adventuresystems` as the current project identifier.

## 简体中文

### 模组定位

围绕玩家成长与冒险流程构建的系统工具，包含 Curios 饰品、钱包/货币/商店、FTB Quests 导航与提交辅助，以及按场景触发的教程/剧情提示。

本项目以游戏内管理为核心。涉及共享玩法数据、世界规则或服务器规则的功能由服务端权威处理；仅影响显示的客户端功能保持本地生效。配置界面统一使用 KineticCore 提供的 GUI 与配置基础设施。

### 主要功能

- 提供 Curios 饰品与可配置的装备行为。
- 提供钱包、货币与商店系统，并由服务端权威处理规则。
- 支持 FTB Quests 的物品/标签/NBT 绑定、快捷跳转与重复提交辅助。
- 提示可按维度、生物群系、结构、进度、背包物品和 Curios 装备筛选。
- 提示数据支持多语言并可在游戏内可视化编辑。
- 可选兼容 Refined Storage、Sophisticated Backpacks 与 JEI。

### 运行环境与兼容

| 类型 | 依赖 |
|---|---|
| 必需 | Minecraft 1.20.1 |
| 必需 | Minecraft Forge 47+ |
| 必需 | KineticCore 26.9.8+ |
| 必需 | Curios 5.10+ |
| 必需 | FTB Library |
| 必需 | FTB Quests |
| 必需 | FTB Teams |
| 可选 | Refined Storage |
| 可选 | Sophisticated Backpacks |
| 可选 | JEI |

### 打开方式与配置

- 使用 KineticCore 配置中心对应的 F6 入口，选择 **Adventure Systems**。
- 服务端规则由服务端保存，并在需要时同步给客户端。
- 纯显示类客户端设置只在本地生效。
- 各功能自己的配置/数据路径在下方详细功能说明中列出。
- 搜索、列表选择、物品/实体信息读取、悬浮提示、返回与导航等操作尽可能复用 KineticCore GUI 组件。

## 完整功能参考

### Curios、钱包与商店

#### 模组定位

**Curios and Wallet Systems** 是 本项目中的饰品、钱包与商店模组，为整合包提供可配置饰品能力、货币钱包、货币兑换、钱包商店与外部仓储联动。它可与 FTB Quests、村民交易、Sophisticated Backpacks 和 Refined Storage 等系统协同工作。

#### 主要功能

##### 钱包袋子

- 将指定物品定义为货币，并把货币余额存入钱包。
- 支持存入、取出、货币兑换和受保护货币。
- 支持附近货币掉落物自动磁吸。
- 提供可编辑位置和缩放的钱包 HUD。
- 佩戴钱包可额外提供 Curios Belt 槽位。
- 支持潜行右键绑定 Refined Storage 控制器。

##### 钱包商店

- 支持购买和出售商品。
- 支持价格、单次数量、限时购买、总限购与页面分类。
- 支持 FTB 任务前置。
- 支持奖励池、空奖励、权重和抽卡型商品。
- 支持命令奖励与自定义显示信息。
- 出售材料可从玩家物品栏、精妙背包和已绑定的 Refined Storage 网络中扣除。
- 材料不足时支持部分提交并保存进度。

##### 饰品

- **心之钢**：通过战斗叠层、提升生命上限并提供额外战斗收益，可配置叠层、治疗、伤害与冲突饰品。
- **失乐园**：围绕食物多样性与长期状态记录提供饰品效果，并支持冲突配置。
- **悬浮背包**：提供对应饰品效果与击退免疫相关能力。
- **钱包袋子**：既是货币容器，也是商店、HUD 与外部仓库联动入口。

##### 联动

- FTB Quests Integration：钱包货币可参与任务或任务前置相关逻辑。
- 村民交易：钱包货币可以辅助村民交易支付与回收。
- Sophisticated Backpacks：出售时可扫描玩家携带的精妙背包材料。
- Refined Storage：绑定控制器后可从 RS 网络读取和扣除出售材料。

#### 配置文件

```text
config/kineticcore/curios.toml
config/kineticcore/currency_wallet_shop.toml
config/kineticcore/currency_wallet_shop_client.toml
```

`curios.toml` 主要控制饰品开关、心之钢、失乐园、钱包货币、兑换与 HUD 等基础规则；商店内容保存在独立商店配置中。

### 完整功能参考

#### 配置项详细说明

| 项目 | 说明 |
|---|---|
| **钱包 HUD** | 调整当前客户端的钱包余额浮层。保存后立即生效，不会修改服务器设置。 |
| **可视化编辑位置与缩放** | 打开实时预览编辑器。拖动浮层改变位置，悬停后滚动鼠标滚轮改变缩放，方向键可精细移动。 |
| **饰品与钱包机制** | 服务器权威的饰品机制与货币规则。单人和多人都通过当前服务器读取和保存；需要 OP 2 级权限才能编辑。 |
| **钱包商店编辑器** | 通过服务器快照编辑钱包商店。远程编辑需要服务器权限等级 2，并由服务器验证和保存每次修改。 |
| **打开商店编辑器** | 向当前服务器请求商店快照并直接进入编辑模式。无需装备钱包；权限不足时服务器会拒绝。 |
| **启用心之钢** | 决定心之钢物品是否注册为可用饰品。修改后需要重启游戏或服务器。 |
| **启用失乐园** | 决定失乐园物品是否注册为可用饰品。修改后需要重启游戏或服务器。 |
| **启用悬浮背包** | 决定悬浮背包是否注册为可用饰品。修改后需要重启游戏或服务器。 |
| **启用钱包袋子** | 决定钱包袋子及其货币功能是否启用。修改后需要重启游戏或服务器。 |
| **充能冷却（秒）** | 成功触发充能攻击并获取层数后的冷却时间。默认 3 秒。 |
| **额外伤害上限** | 心之钢按最大生命值换算后最多能增加的最终伤害绝对值。默认 3。 |
| **叠层生命上限** | 通过积累层数最多获得的额外最大生命值，不包含基础生命加成。默认 200。 |
| **每点生命所需层数** | 多少层数转化为 1 点最大生命值。必须至少为 1，默认 100。 |
| **基础生命加成** | 佩戴心之钢后无需叠层即可获得的最大生命值。默认 10。 |
| **治疗倍率** | 充能攻击基于造成伤害恢复生命值时使用的基础倍率。默认 2。 |
| **增伤窗口（Tick）** | 触发充能后允许额外伤害生效的持续时间。20 Tick 等于 1 秒，默认 5 Tick。 |
| **单次最小层数** | 一次成功充能攻击随机获得的最小层数，不应大于最大层数。默认 2。 |
| **单次最大层数** | 一次成功充能攻击随机获得的最大层数，不应小于最小层数。默认 5。 |
| **每点生命伤害系数** | 玩家最大生命值转化为额外伤害的比例。0.005 表示 0.5%，最终仍受伤害上限约束。 |
| **排斥饰品 ID** | 佩戴列表中的任意饰品时心之钢会失效。每行填写一个完整物品 ID，例如 enigmaticlegacy:cursed_ring。 |
| **最低营养值** | 营养值小于或等于此值的食物不会计入多样性。0 会忽略不恢复饱食度的食物。 |
| **排斥饰品 ID** | 佩戴列表中的任意饰品时失乐园会失效。每行填写一个完整物品 ID。 |
| **额外腰带栏位** | 装备钱包时额外提供的 Curios 腰带栏位数量。变更后可能需要重新装备钱包。 |
| **最大货币种类** | 界面和货币系统最多读取的货币种类数。运行时硬上限为 10。 |
| **货币磁吸半径** | 装备钱包时自动收集货币掉落物的半径，单位为方块；小于或等于 0 会禁用磁吸。 |
| **磁吸检查间隔（Tick）** | 检查附近货币的间隔。数值越大越节省服务器性能，但收集延迟更高；20 Tick 等于 1 秒。 |
| **货币定义** | 每行格式为 物品ID\|基础价值，价值必须大于 0。例如 kubejs:gold_coin\|1。 |
| **单向兑换规则** | 每行格式为 来源物品ID->目标物品ID，用于找零和自动升级。反向兑换需要单独添加规则。 |
| **仅手动兑换货币** | 这些货币不会被村民交易、钱包商店或 FTB 虚拟支付自动消耗或拆分，每行填写一个物品 ID。 |

#### 物品、HUD、Jade 与状态提示说明

| 项目 | 说明 |
|---|---|
| **美食家法则 (无限成长):** | 吞噬各种食物来永久获得最终伤害加成。 |

#### 可编辑字段、模式与分类索引

- 余额浮层
- 模块总开关
- 心之钢
- 失乐园
- 钱包袋子

#### 配置键与默认值

| 配置键 | 默认值 |
|---|---|
| `currency_wallet.extraBeltSlots` | `1` |
| `currency_wallet.hudOffsetX` | `0` |
| `currency_wallet.hudOffsetY` | `0` |
| `currency_wallet.hudScale` | `0.75` |
| `currency_wallet.magnetIntervalTicks` | `10` |
| `currency_wallet.magnetRange` | `6.0` |
| `currency_wallet.maxCurrencyTypes` | `10` |
| `general.enable_currency_wallet` | `true` |
| `general.enable_heart_of_steel` | `true` |
| `general.enable_levitation_backpack` | `true` |
| `general.enable_paradise_lost` | `true` |
| `heart_of_steel.baseHealth` | `10.0` |
| `heart_of_steel.damageCap` | `3.0` |
| `heart_of_steel.damagePerHp` | `0.005` |
| `heart_of_steel.damageWindowTicks` | `5` |
| `heart_of_steel.growthInterval` | `3` |
| `heart_of_steel.healMultiplier` | `2.0` |
| `heart_of_steel.maxGain` | `5` |
| `heart_of_steel.maxHealthCap` | `200` |
| `heart_of_steel.minGain` | `2` |
| `heart_of_steel.stacksPerHp` | `100` |
| `paradise_lost.minNutrition` | `0` |

#### 配置与数据路径

主要配置/数据路径：

- `config/kineticcore/curios.toml`
- `config/kineticcore/currency_wallet_shop.toml`
- `config/kineticcore/currency_wallet_shop_client.toml`

### FTB Quests 集成

#### 模组定位

**FTB Quests Integration** 是 本项目专门面向 **FTB Quests** 的任务物品联动模块。它把物品、标签和 NBT 变种与 FTB 任务绑定起来，让玩家从物品 Tooltip 直接跳转到对应任务，同时扩展可重复物品任务的提交体验。

#### 主要功能

- **物品直达任务**：鼠标悬停在已绑定物品上时显示任务跳转提示。
- **默认快捷键 G**：在支持的 GUI 中按 `G` 直接打开该物品默认绑定的 FTB 任务。
- **Alt + G 多任务列表**：一个物品关联多个任务时，可以打开任务选择界面。
- **任务收藏**：可把某个任务设为该物品的默认星标任务，以后普通打开键优先跳转到它。
- **物品 / 标签绑定**：既能绑定具体物品，也能按物品标签批量关联任务。
- **NBT 模糊匹配**：支持针对带 NBT 的物品变种进行任务绑定，适合附魔书、枪械、饰品等动态物品。
- **黑名单系统**：可排除不希望出现任务跳转提示的物品、标签或 NBT 变种。
- **可视化绑定编辑器**：通过 F6 配置中心打开绑定管理界面，不需要手写完整 JSON。
- **可重复 ItemTask 多次提交**：对允许批量提交的 FTB 物品任务提供次数输入与重复提交逻辑。
- **服务端提交校验**：多次提交最终仍由服务端 FTB Quest 数据和 TeamData 规则决定，不会只靠客户端修改任务进度。
- **独立客户端总开关**：可仅关闭本机的 Tooltip 跳转提示和快捷键跳转，不影响服务器任务数据。
- **JEI 可选兼容**：安装 JEI 时继续提供相关客户端联动。

#### 配置与数据文件

```text
config/kineticcore/ftb_item_client.toml
config/kineticcore/ftb_item_quest_bindings.json
config/kineticcore/ftb_quest_favorites.json
config/kineticcore/ftb_item_blacklist.json
```

- `ftb_item_client.toml`：纯客户端任务跳转开关。
- `ftb_item_quest_bindings.json`：物品与任务绑定数据。
- `ftb_quest_favorites.json`：多任务物品的默认收藏记录。
- `ftb_item_blacklist.json`：任务跳转黑名单。

#### 使用建议

先通过 F6 打开 FTB Quests Integration 页面并进入绑定编辑器。普通玩家只需要记住 `G` 和 `Alt + G`；整合包作者则可以使用绑定、收藏和黑名单把物品说明与任务书直接连接起来。

### 完整功能参考

#### 配置项详细说明

| 项目 | 说明 |
|---|---|
| **启用任务物品跳转** | 为匹配物品显示任务关联，并启用任务跳转快捷操作。 |
| **打开物品绑定编辑器** | 编辑 FTB 任务物品绑定、收藏与黑名单。 |

#### 界面操作与编辑器说明

| 项目 | 说明 |
|---|---|
| **submit** | 请输入这次要兑换几次。 |

#### 物品、HUD、Jade 与状态提示说明

| 项目 | 说明 |
|---|---|
| **收藏任务** | 右键将该任务设为默认打开项 |
| **搜索任务** | 搜索当前物品关联到的任务。 |

#### 可编辑字段、模式与分类索引

- [模组全局]
- [物品标签]

#### 配置键与默认值

| 配置键 | 默认值 |
|---|---|
| `enableTaskJump` | `true` |

#### 配置与数据路径

主要配置/数据路径：

- `config/kineticcore/ftb_item_blacklist.json`
- `config/kineticcore/ftb_item_client.toml`
- `config/kineticcore/ftb_item_quest_bindings.json`
- `config/kineticcore/ftb_quest_favorites.json`

### 情境提示

#### 模组定位

**Contextual Tips** 是面向整合包、服务器与剧情玩法的上下文提示系统。它可以在加载阶段、游戏阶段或任意阶段显示提示，并根据玩家当前维度、群系、结构、进度、物品和 Curios 饰品条件自动筛选真正适合当前玩家的内容。

提示内容由服务器统一维护和同步，支持按客户端语言下发不同语言版本。管理员可以直接在游戏内使用可视化编辑器维护提示，无需手写复杂 JSON。

#### 主要功能

##### 多阶段提示

每条提示都可以指定显示阶段：

- `loading`：加载 / 世界切换阶段。
- `game`：游戏内阶段。
- `any`：任意阶段均可参与抽取。

每条提示还可以单独设置显示时间，从短提示到长说明都能独立控制。

##### 上下文条件筛选

一条提示可以同时附带多种条件：

- 指定维度。
- 指定群系。
- 指定结构。
- 指定进度（Advancement）。
- 要求玩家持有指定物品。
- 要求玩家佩戴指定 Curios 饰品。

只有当前玩家满足条件时，这条提示才会进入可用提示池。因此可以制作例如“进入末地后才出现”“到达某结构后才提示”“获得某件关键物品后才教学”的定向说明。

##### 物品与 Curios NBT 匹配

物品条件与 Curios 条件支持三种 NBT 模式：

- `NONE`：只判断物品类型。
- `WEAK`：弱 NBT 匹配。
- `STRONG`：强 NBT 匹配。

适合区分普通物品、带特殊数据的任务物品、定制装备、枪械、饰品等不同变种。

##### 多语言提示文件

提示文件按语言独立保存：

- `tips_zh_cn.json`
- `tips_en_us.json`
- 也可以继续增加其他 Minecraft 语言代码对应的文件。

客户端进入服务器后会报告自己的语言，服务器根据该语言发送对应提示快照；找不到对应语言文件时会使用英文提示作为回退。

##### 游戏内可视化编辑器

管理员可以在编辑器中直接完成：

- 新建、修改、删除提示。
- 搜索已有提示。
- 修改提示文本。
- 通过颜色面板插入 Minecraft `§` 格式代码。
- 设置显示阶段和单条显示时间。
- 选择维度、群系、结构和进度条件。
- 使用物品选择器添加背包物品条件。
- 使用 Curios 选择条件。
- 点击物品 / Curios 条件循环切换 NBT 匹配模式。
- 右键移除条件。

编辑器会对非法资源 ID、空文本、超长文本、非法 NBT、超范围显示时间和过量条目进行校验，避免错误配置直接写入服务器。

##### 服务端权威保存与压缩同步

- 提示编辑数据由服务器读取。
- 只有管理员权限玩家可以提交修改。
- 保存时服务器再次校验数据，不依赖客户端按钮权限。
- 保存后服务器写入对应语言 JSON。
- 使用 KineticCore 的网络压缩工具同步大型提示 JSON，并限制压缩包与解压数据大小。
- 只向使用对应语言的在线客户端广播更新后的提示快照。
- 单人游戏同样走集成服务器保存路线。

#### 配置文件

基础开关：

- `config/kineticcore/tips_settings.toml`

提示文件目录：

- `config/kineticcore/tips/`

常见文件：

- `config/kineticcore/tips/tips_zh_cn.json`
- `config/kineticcore/tips/tips_en_us.json`

`tips_settings.toml` 中可以总开关提示系统；具体提示正文和条件保存在语言 JSON 中。

#### 使用建议

- 普通通用知识使用 `any`。
- 只希望游戏内出现的操作教学使用 `game`。
- 世界载入提示使用 `loading`。
- 剧情或阶段式教学尽量搭配维度、结构、进度、物品条件，避免无关提示污染提示池。
- 对带复杂 NBT 的任务物品，优先使用弱 / 强 NBT 匹配，而不是只按物品 ID 判断。

### 完整功能参考

#### 配置项详细说明

| 项目 | 说明 |
|---|---|
| **屏幕提示设置** | 控制加载界面、世界切换和暂停界面的自定义提示。基础开关可直接修改，提示内容和条件使用专用编辑器。 |
| **启用屏幕提示** | 关闭时不显示自定义加载、世界切换和暂停提示；提示数据保持不变。 |
| **打开提示编辑器** | 编辑提示文本、显示阶段、持续时间、群系/结构/维度/进度条件以及物品/饰品 NBT 条件。联机服务器中打开编辑器需要等级 2 管理权限。 |

#### 界面操作与编辑器说明

| 项目 | 说明 |
|---|---|
| **[提示]** | 输入秒数 (例如: 5.0) |

#### 配置键与默认值

| 配置键 | 默认值 |
|---|---|
| `enableTips` | `true` |

#### 配置与数据路径

主要配置/数据路径：

- `config/kineticcore/tips`
- `config/kineticcore/tips/`
- `config/kineticcore/tips/tips_en_us.json`
- `config/kineticcore/tips/tips_zh_cn.json`
- `config/kineticcore/tips_settings.toml`

### 从源码构建

- Minecraft：`1.20.1`
- Java：`17`
- ForgeGradle：`6.0.24`
- Gradle：项目固定使用 `8.1.1` Wrapper，请不要使用 Gradle 9 直接导入。
- 默认本地依赖目录由 `local_libs_dir` 控制，可在 `gradle.properties` 或命令行参数中覆盖。
- 常用构建命令：`gradlew.bat build`（Windows）或 `./gradlew build`（Linux/macOS）。
- 生成的开发/发布文件以 `adventuresystems` 作为当前工程标识。
