# GUI long-text verification / 界面长文本验证

## English

2026-10-04, Asia/Shanghai. Worktree: .worktrees/gui-long-text-20261004; base 8b5979d. The original checkout and Core sources were not edited.

### Changes

- Seventeen GUI files use Core scrolling text with layout-derived bounds. Titles, labels, names, prices and instructions preserve styled Components and stop before neighboring controls.
- Shop item buttons reserve icon space; product cells clip their contents; reward/quest pickers render above detail text. Choice-overlay titles stop before Cancel/Confirm.
- Reward-pool instructions and actions occupy separate rows. FTB headers reserve Search, Save and item space. Tip selectors separate title, Search and Cancel.
- Tip labels cannot consume the controls' space. Core's scroll controller keeps the condition list above Save/Back; deletion and hover follow the visible rows.
- Fixtures are opt-in, under src/test and packaged separately. Release JARs contain zero validation classes. No source translation or language key was changed. Core 26.10.3 already exposes the required APIs; minimum dependency unchanged.

### Build

Offline buildAll plus :1.21.1-neoforge:guiValidationJar passed in 25 seconds, with -Pkineticcore_version=26.10.4 and -Poutput_mods_dir=build/libs. An ignored init script selects local Core JARs for the nested worktree; production resolution is unchanged.

- Class major versions: Forge 1.20.1 / NeoForge 1.21.1 = 65; NeoForge 26.1.2 = 69. Release class counts: 189 / 192 / 192. Forge contains its refmap; NeoForge has no Forge refmap.
- Source and all packaged EN/CN sets have identical 757 keys; 1.21.1 overrides have 6 matching keys. Formatting regressions exercise 933 templates, styles, nesting, clipping, serialization and language switching on 1.20.1 and 1.21.1.
- Eleven JUnit tests pass on 1.21.1. Native FTB item-component and tips-component checks pass. Forge has no discovered JUnit tests; its dedicated formatting check passes.
- Six Mixin classes per node. NeoForge: zero problems/notes. Forge: zero problems, three preexisting notes: MerchantMenu clickMenuButton target and two references from ValidItemsScreenSubmitLimitMixin$1 to its Mixin-package outer class. No gameplay changes were mixed into this task.
- 26.1.2 compileTestJava/test and three stand-in regression tasks remain skipped by the migration build. This GUI work was not runtime-tested on 26.1.2 or 1.20.1.

### Runtime matrix

Existing 1.21.1-NeoForge_21.1.252 installation, unchanged 8 GiB settings, copied world, automatic GUI scale. No game download or desktop input.

| Area | Captured states |
| --- | --- |
| Shop | Empty, buy, sell, locked, gacha, choice overlay, quest picker, reward picker |
| Entry editor | Buy, sell, gacha, choice |
| Wallet authoring | Empty/populated commands, command picker, currencies, wallet, empty/populated quests, quest picker |
| Rewards | Empty, gacha, choice, sell |
| FTB | Empty/populated bindings, submit counts, quest selection, item/task blacklists |
| Tips | Empty/populated editor, timing, populated biome registry |

Initial: 34 states in both languages at 854×480 and 1536×864 = 136 captures; extended translations add 68 start/later-scroll captures. Findings were fixed and 18 affected states rechecked in both languages/sizes (72 captures), plus 36 final stress captures. Total 312 successful captures, all visually reviewed. Wheel validation reaches lower tip conditions. Unsaved drafts only; no trade, reward execution or persistent Save.

### Limits and restoration

Fixture quest ids are synthetic; absent configured currency ids show existing Barrier placeholders. This is layout validation, not integration/gameplay acceptance. HUD tips were unchanged. Core 26.10.4 tall-tooltip height overflow remains documented in the CombatSystems handoff; no addon tooltip algorithm was introduced.

Ignored .gradle/gui-long-text-20261004 holds build/client logs and images. Private launch arguments are not committed. Original addon JAR, options and changed configs are restored byte-for-byte; original world level.dat hash is unchanged. Own fixture and copied world are archived in the profile task backup after clients exit.

## 简体中文

2026-10-04，上海时区；基于 8b5979d 的独立工作树。原检出目录与核心源码未改动。

17 个界面按布局边界使用核心滚动文字，保留 Component 样式并避开相邻控件。物品按钮预留图标，商品格裁剪内容，奖励/任务选择面板置于详情文字上层；自选标题避开取消/确定。奖励池说明与按钮分行，FTB 顶部预留搜索、保存和物品区域；提示选择器分开标题、搜索与取消。提示条件区使用核心滚动控制器限制在保存/返回上方，删除与悬浮按可见位置响应。验证代码只在 src/test 并独立打包；语言文本/键名与最低核心依赖不变。

三版本离线 buildAll 通过（25 秒）。类版本依次 65、65、69，类数 189、192、192；Forge refmap 保留。源码和产物中英文各 757 键完全一致，1.21.1 覆盖资源各 6 键一致。旧两版本格式检查覆盖 933 个模板及颜色、嵌套、裁剪、序列化、语言切换；1.21.1 的 11 项 JUnit、原生 FTB/提示组件检查通过。Forge 未发现 JUnit 测试，独立格式检查通过。

各节点检查 6 个 Mixin，NeoForge 零问题、零备注，Forge 零问题，有 3 条既有备注：MerchantMenu 的 clickMenuButton 目标及 ValidItemsScreenSubmitLimitMixin$1 对 Mixin 外层类的两次引用。没有顺带修改玩法。26.1.2 的替身测试仍按既有设置跳过，本次没有宣称该版本或 1.20.1 界面实测通过。

使用已有 1.21.1 安装和原 8 GiB 内存配置，复制测试世界并命令启动，不下载游戏、不操作桌面输入。上表 34 种状态在中英文、854×480/1536×864 下共 136 张截图，加长翻译初检 68 张；修复后 18 种状态双语/双窗口复检 72 张，最终加长翻译 36 张，共 312 张成功截图，全部核看。额外断言滚轮能到达底部条件。未保存草稿或执行交易/奖励。

合成任务 ID 和原配置缺失币种的屏障占位只用于布局验证，不能代替联动/玩法验收。HUD 提示未改动。核心 26.10.4 的超高悬浮提示越界仍在 CombatSystems 独立交接中，不在附属另写算法。

证据在忽略的 .gradle/gui-long-text-20261004 内；私密启动参数不提交。完成后恢复原 JAR、options 和变化配置并校验字节一致，原世界 level.dat 哈希不变，自己的验证 JAR 与复制世界归档到版本任务备份。
