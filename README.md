# Adventure Systems

[English](#english) | [简体中文](#chinese) | [CurseForge](https://www.curseforge.com/minecraft/mc-mods/adventuresystems)

<a id="english"></a>

## English

Adventure Systems connects equipment progression, a configurable currency economy, FTB Quests shortcuts, and contextual guidance. Pack authors can build shops and quest-linked unlocks in-game, while players use Curios accessories, a wallet, and item-to-quest navigation.

### Requirements

| Component | Requirement |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 47.4.2+ for Minecraft 1.20.1 |
| KineticCore | 26.9.24+ |
| Curios API | 5.10.0+ |
| FTB Library, FTB Quests, FTB Teams | Required by this branch's mod metadata |
| Refined Storage, Sophisticated Backpacks, JEI | Optional integrations |

Install the mod and its required dependencies on both client and server. The starter currency list points to KubeJS items, but KubeJS itself is optional; replace those IDs with items already present in your pack if you do not use KubeJS.

### Accessories and progression

| Accessory | Function | Configuration |
| --- | --- | --- |
| **Heart of Steel** | Charged combat actions accumulate stacks, provide additional maximum health, and apply configured healing and bonus damage. | Cooldown, stack gains, stacks per health point, base health, growth cap, healing multiplier, and damage cap. |
| **Paradise Lost** | Records distinct foods eaten per player and turns their nutrition-based score into a damage bonus while the accessory is active. | Minimum nutrition, score multiplier, curve points, and interpolation mode. Eating the same food ID again does not repeatedly add score. |
| **Levitation Backpack** | Grants a flight source while equipped in Curios. | Optional knockback immunity while flying. It is a flight accessory, not an item-storage backpack. |
| **Currency Wallet** | Stores configured currencies, provides a wallet/shop interface, and can collect nearby dropped currency. | Currency definitions, exchange directions, type limit, pickup range/interval, extra belt slots, and HUD placement. |

Accessory enable switches and conflict lists let the pack author control availability and incompatible Curios combinations. Heart of Steel and Paradise Lost also use ownership/progression checks; transferring an accessory is not equivalent to transferring another player's entire progression.

### Wallet and currency exchange

- Equip the wallet in Curios and press **U** to open it. Keeping the item only in the ordinary inventory is not the same as equipping it.
- Deposit and withdraw configured currency items, inspect balances, and exchange along the allowed conversion routes.
- Right-click while holding the wallet **without sneaking** to toggle its currency magnet. The magnet collects configured currencies, not arbitrary dropped items.
- Adjust the balance HUD's position and scale through the wallet HUD page.
- Eligible villager trade payment slots can be replenished from the equipped wallet; currencies marked manual-exchange-only still require the appropriate manual conversion.

Currency definitions use `item_id|value`, and exchange rules use `source_id->target_id`. The shipped definitions are:

```text
kubejs:gold_coin|1
kubejs:diamond_coin|100
kubejs:netherite_coin|1000
kubejs:monster_coin|10000
```

These IDs do not create the currency items. Supply them in the pack or replace them with registered items, then configure permitted exchange directions. The default monster coin is marked for manual exchange only.

### Shop authoring

The shop supports buying items and selling materials, with visual editors for entries, currencies, quest conditions, and reward pools.

- Set display items, names, descriptions, pages, prices, and item quantities.
- Charge a configured wallet currency or an ordinary payment item.
- Gate entries behind selected FTB quests and a required number of completed prerequisites.
- Apply per-player purchase limits and timed cooldowns.
- Offer fixed items, weighted random rewards (including empty outcomes), selectable rewards, and administrator-authored command rewards.
- Preserve item data for NBT-bearing entries rather than reducing every stack to its item ID.
- Where the optional integrations and access conditions are satisfied, include supported Sophisticated Backpack or Refined Storage materials in shop operations. The interface shows these sources separately.

New configurations start with empty buy/sell lists. A blank shop therefore normally needs authored entries. Shop editing requires **permission level 2**; command rewards are maintained by administrators because they execute with server authority.

A practical setup is to define valid currencies first, create a simple fixed-price entry, test a purchase and material sale, and then add quest gates or reward pools.

### FTB Quests navigation and submission

| Default control | Action |
| --- | --- |
| **G** over an item in a supported GUI | Jump to an associated quest. |
| **Alt + G** over an item | Open the multi-quest selection flow. |
| **F6 → Adventure Systems → FTB page** | Enable/disable task jumping and open the binding editor. |

Bindings support item/tag targets and optional NBT matching. Local favorites and item blacklists help keep the quest picker useful. JEI support supplies hovered items when that integration is present. These bindings navigate existing quests; they do not create an FTB quest book.

Batch submission has deliberate eligibility checks: the quest must be repeatable, contain a single supported resource-consuming item task with one accepted display item, and have a supported single reward. Crafting-only and task-screen-only tasks are excluded, and the player's team must be able to start the quest. It is not a universal bulk-complete button for every task type.

### Contextual tips

Tips can be authored per language for loading and in-world/pause contexts. Each entry has text, display time, and a stage, with optional conditions for:

- Dimension, biome, or structure.
- Advancement progress.
- Carried items or equipped Curios, with optional NBT matching.

The editor includes condition selectors and timing controls. While connected, clients request the server's tips for their selected language; local files provide fallback behavior. Editing server-managed tip content requires permission level 2.

### Configuration and data

All paths below are relative to the game/server directory:

| Path | Purpose |
| --- | --- |
| `config/kineticcore/curios.toml` | Accessory mechanics, currencies, exchange rules, and wallet settings. |
| `config/kineticcore/currency_wallet_shop.toml` | Shared buy/sell definitions. |
| `config/kineticcore/currency_wallet_shop_client.toml` | Local shop interface preferences. |
| `config/kineticcore/ftb_item_client.toml` | Local task-jump option. |
| `config/kineticcore/ftb_item_quest_bindings.json` | Local item-to-quest bindings. |
| `config/kineticcore/ftb_quest_favorites.json` | Local quest favorites. |
| `config/kineticcore/ftb_item_blacklist.json` | Local item filtering for quest navigation. |
| `config/kineticcore/tips_settings.toml` | General tip settings. |
| `config/kineticcore/tips/tips_<language>.json` | Language-specific tip content, including `en_us` and `zh_cn`. |

Open the shared configuration center with **F6**. Client pages such as HUD/navigation preferences are separate from server-authoritative mechanics and content. Accessory module switches require restarting; follow each page's apply notice for other settings. A client-side file edit does not override a remote server's rules. Player progression and wallet state are also runtime/save data, so copying the configuration directory alone is not a complete player-data backup.


---

<a id="chinese"></a>

## 简体中文

Adventure Systems 将装备成长、货币经济、FTB 任务快捷交互和情境提示连接起来。整合包作者可以在游戏中维护商店与任务解锁条件，玩家则通过 Curios 饰品、钱包和物品关联任务界面使用这些内容。

### 运行环境

| 组件 | 要求 |
| --- | --- |
| Minecraft | 1.20.1 |
| Forge | 对应 1.20.1 分支的 47.4.2+ |
| KineticCore | 26.9.24+ |
| Curios API | 5.10.0+ |
| FTB Library、FTB Quests、FTB Teams | 当前分支元数据声明为必需 |
| Refined Storage、Sophisticated Backpacks、JEI | 可选联动 |

客户端和服务端均应安装本模组及必要前置。初始货币列表引用 KubeJS 物品，但 KubeJS 本身不是必须安装的前置；不使用 KubeJS 时，可将货币 ID 改为整合包中已有的物品。

### 饰品与成长

| 饰品 | 实际功能 | 可调整内容 |
| --- | --- | --- |
| **心之钢** | 通过充能战斗动作积累层数，增加最大生命，并按配置提供治疗和额外伤害。 | 冷却、层数获取量、每点生命所需层数、基础生命、成长上限、治疗倍率与伤害上限。 |
| **失乐园** | 按玩家记录吃过的不同食物，以营养值计算分数，并在饰品生效时转换为伤害加成。 | 最低营养门槛、分数倍率、曲线节点及插值方式；重复吃同一物品 ID 不会反复加分。 |
| **悬浮背包** | 放入 Curios 槽位后提供飞行来源。 | 可设置飞行时免疫击退；它是飞行饰品，不是储物背包。 |
| **货币钱包** | 保存配置中的货币，提供钱包与商店界面，并可吸取附近掉落货币。 | 货币定义、兑换方向、种类上限、磁吸范围与间隔、额外腰带槽、HUD 位置。 |

饰品开关和冲突名单用于控制可用性及不兼容的 Curios 搭配。心之钢与失乐园还有归属和成长校验，转交饰品不等于转移另一名玩家的全部成长记录。

### 钱包与兑换

- 将钱包**装备到 Curios 槽位**后，默认按 **U** 打开。仅放在普通背包内不等于装备。
- 存取配置中的货币物品，查看余额，并沿允许的路线兑换。
- 手持钱包、**不潜行右键**切换货币磁吸；磁吸针对已定义的货币，不会吸取所有掉落物。
- 在钱包 HUD 页面调整余额显示的位置和缩放。
- 村民交易中的适用支付槽可从已装备的钱包补充货币；被标记为仅手动兑换的货币仍需先完成相应转换。

货币定义格式为 `物品ID|价值`，兑换路线格式为 `来源ID->目标ID`。默认定义如下：

```text
kubejs:gold_coin|1
kubejs:diamond_coin|100
kubejs:netherite_coin|1000
kubejs:monster_coin|10000
```

填写 ID 不会自动创建物品。整合包必须提供这些物品，或将定义替换为真实存在的物品，再设置允许的兑换方向。默认怪物币被列为仅手动兑换。

### 商店编辑

商店支持商品购买与材料出售，并提供商品、货币、任务条件和奖励池的可视化编辑界面。

- 设置展示物品、名称、描述、分页、价格与数量。
- 使用钱包货币或普通物品作为购买支付材料。
- 选择前置 FTB 任务，并设置需要完成的前置数量。
- 设置玩家购买总限额与限时冷却。
- 提供固定物品、带权重的随机奖励（含空奖励）、自选奖励及管理员编写的命令奖励。
- 保存带 NBT 物品的完整数据，不把所有商品都简化为物品 ID。
- 可选联动及访问条件满足时，可使用受支持的精妙背包或 Refined Storage 材料参与商店操作，界面分别显示这些材料来源。

首次生成的购买、出售列表为空，因此空白商店通常需要先添加内容。编辑商店需要**权限等级 2**；命令奖励由管理员维护，因为执行时具有服务器权限。

推荐先定义可用货币，创建简单的固定价格商品，验证购买和出售，再添加任务门槛或随机奖励池。

### FTB 任务关联与提交

| 默认操作 | 功能 |
| --- | --- |
| 在受支持界面中悬停物品，按 **G** | 跳转关联任务。 |
| 悬停物品，按 **Alt + G** | 打开多任务选择流程。 |
| **F6 → Adventure Systems → FTB 页面** | 开关任务跳转，打开绑定编辑器。 |

绑定支持物品或标签目标，以及可选的 NBT 匹配。本地收藏与物品黑名单用于整理任务选择结果；安装 JEI 后可从其界面获取悬停物品。这些绑定用于导航已有任务，不会替代 FTB Quests 创建任务书。

批量提交有明确限制：任务必须可重复，只有一个受支持、消耗物品的任务目标，接受单一展示物品，并且只有一个受支持的奖励。不适用于仅限合成或仅限任务屏幕的任务；玩家所属队伍还必须满足任务开始条件，并非所有任务都能一键批量完成。

### 情境提示

提示可按语言维护，在加载和世界内／暂停情境中使用。每条可设置文本、显示时长和阶段，并按以下条件筛选：

- 维度、生物群系、结构。
- 进度条件。
- 携带物品、已装备 Curios，以及可选的 NBT 匹配。

编辑器提供条件选择与时间设置。连接服务器时，客户端按当前语言请求服务器提示，本地文件用于回退。编辑服务器管理的提示内容需要权限等级 2。

### 配置与数据位置

下列路径均相对于游戏或服务器目录：

| 路径 | 用途 |
| --- | --- |
| `config/kineticcore/curios.toml` | 饰品机制、货币、兑换规则和钱包设置。 |
| `config/kineticcore/currency_wallet_shop.toml` | 公共购买与出售定义。 |
| `config/kineticcore/currency_wallet_shop_client.toml` | 本地商店界面偏好。 |
| `config/kineticcore/ftb_item_client.toml` | 本地任务跳转开关。 |
| `config/kineticcore/ftb_item_quest_bindings.json` | 本地物品与任务绑定。 |
| `config/kineticcore/ftb_quest_favorites.json` | 本地任务收藏。 |
| `config/kineticcore/ftb_item_blacklist.json` | 本地任务导航物品过滤。 |
| `config/kineticcore/tips_settings.toml` | 提示常规设置。 |
| `config/kineticcore/tips/tips_<语言>.json` | 各语言提示内容，包括 `en_us`、`zh_cn`。 |

默认按 **F6** 打开统一配置中心。HUD、任务导航等客户端页面，与服务器控制的机制和内容分开管理。饰品模块开关需要重启，其他设置应遵守页面的生效提示；修改客户端文件不会覆盖远程服务器规则。玩家成长和钱包状态还涉及运行时及存档数据，仅复制配置目录不等于完整备份玩家数据。
