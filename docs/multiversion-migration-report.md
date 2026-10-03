# Multiversion migration report / 多版本迁移报告

2026-10-04. Local delivery only; nothing was pushed or published. KineticCore is maintained separately by Claude and was not modified by this work.

本次仅本地交付，未推送、未发布。KineticCore 由 Claude 单独维护，本次未修改核心，也未给核心添加语言约束。

## Scope / 范围

AdventureSystems now builds Forge 1.20.1 and NeoForge 1.21.1 together using Java 21, Gradle 9.8.0, Stonecutter 0.9.8 and ModDevGradle 2.0.148. The 26.1.2 node is reserved and disabled. Forge items retain NBT; NeoForge items use native data components without converting old item NBT. World/player persistence still uses the game's NBT APIs.

AdventureSystems 使用 Java 21、Gradle 9.8.0、Stonecutter 0.9.8、ModDevGradle 2.0.148，一次构建 Forge 1.20.1 和 NeoForge 1.21.1。26.1.2 仅预留、未启用。Forge 物品保留 NBT，NeoForge 物品使用原生数据组件，不转换旧物品 NBT。世界与玩家持久化继续使用游戏的 NBT API。

## Verification / 验证

| Check / 检查 | Result / 结果 |
| --- | --- |
| Offline buildAll / 双节点离线构建 | PASS; tip rotation full build 19 seconds |
| Authored, overlay and processed language keys / 源码、覆盖与处理后的语言键 | PASS; English/Chinese each 755 complete keys; no generated formatting keys |
| Text regression / 文本回归 | PASS on both nodes; 933 colored entries across both languages, styles, nested/repeated arguments, clipping, serialization and language switching |
| NeoForge JUnit | 11 tests; zero failures/errors |
| Native FTB/tips component checks / 原生任务、提示组件检查 | PASS |
| Architecture and Core reference checks / 架构与核心引用检查 | PASS; final Core references: 0 problems per node |
| Mixin targets / Mixin 目标 | Forge: 6 mixins, 0 problems, 3 registered notes; NeoForge: 6 mixins, 0 problems, 0 notes |
| Original migration baseline comparison / 原迁移提交基线对比 | 180 non-text classes, 73,752 ordered javap lines and 30 non-text resources identical |
| Release JAR inspection / 发布 JAR 检查 | Forge 184 / NeoForge 187 classes; class version 65; 3 Mixin configs each; correct loader metadata; no validation fixture/test agent |
| Existing 1.21.1 client / 现有 1.21.1 客户端 | 17 runtime checks, 0 failures; actual pause screen and screenshot verified; world saved and client exited normally |
| Native item/structure condition runtime / 物品与结构条件运行检查 | 19 checks, 0 failures; six scenarios and real two-tip rotation; inventory and position restored |
| All 12 addon resource tasks / 全部 12 个附属资源任务 | PASS offline; exact English/Chinese key parity enforced |
| Negative language fixtures / 语言失败用例 | Mismatched keys, generated keys and non-string values each fail; valid pair passes |

The Forge comparison intentionally excludes the text subsystem and language resources changed at the user's request. Before that language change, the pure build migration preserved all 183 classes / 74,719 ordered instruction lines and 33 resources. Forge gameplay was not launched again, as requested.

Forge 对比排除了用户明确要求修改的文本子系统和语言资源；仅构建迁移阶段曾验证全部 183 个类、74,719 行有序指令和 33 项资源一致。按用户要求，本次没有重新启动 1.20.1 游戏。

Runtime testing used the existing profile at F:/game/异界战斗幻想/.minecraft/versions/1.21.1-NeoForge_21.1.252, its existing Java and 8 GB arguments. The client was launched directly from commands without desktop input, without changing memory settings or downloading a game version. A temporary validation mod ran 13 server-side checks and 4 client-side checks, then requested normal client exit; the fixture was removed afterward. The installed release JAR matches the hash below.

运行测试直接通过命令启动现有 1.21.1 实例，沿用现有 Java 和 8 GB 参数，不操作鼠标、不改内存、不下载游戏版本。临时验证模组执行 13 项服务端与 4 项客户端检查后正常退出，随后移出 mods。实例内安装的正式 JAR 与下列哈希一致。

## Artifacts / 产物

Output directory / 输出目录: D:/NEWMODS.

| JAR | SHA-256 |
| --- | --- |
| adventuresystems-forge-1.20.1-26.10.4.jar | BBE1814E8F2703A409110AE6FB1807A330E2B64027ED7DF02C0E9235D220C648 |
| adventuresystems-neoforge-1.21.1-26.10.4.jar | 61C6BC94087260E20733D35874A85DBF9A8E56C75C49AA12B1D43B401DAEDE37 |

Forge contains the refmap, JAVA_17 Mixin compatibility and MixinConfigs manifest. Its KineticCore dependency range remains [26.10.3,).

Forge 包含 refmap，Mixin 兼容级别保持 JAVA_17，清单包含 MixinConfigs；KineticCore 依赖范围保持 [26.10.3,)。

## Language cleanup / 语言清理

Release JSON contains authored keys only. Formatting templates are derived from the active language at runtime and cached outside the public key namespace, preserving resource-pack overrides and vanilla base-key network serialization. All 12 addons have gradle/kinetic-language-validation.gradle wired into resource processing and check. Two missing Chinese NeoForge component labels were added to KineticArmory. See [the audit](language-key-audit.md) for counts and scope.

发布语言 JSON 仅含人工语言键。格式模板从当前语言在运行时解析并缓存，不占用公开语言键，保留资源包覆盖和原版基础键网络序列化。全部 12 个附属都将语言校验接入资源处理和 check；KineticArmory 补齐两条中文 NeoForge 组件文案。详细数量和范围见[检查报告](language-key-audit.md)。

## Pause tips / 暂停菜单提示

The two profiles used different tip configurations. Forge had many unconditional any/game entries; NeoForge had only one loading entry and six conditional game entries, none matching the tested location. This caused the empty pause menu.

两个实例的提示配置不同：Forge 整合包有许多无条件 any/game 条目，NeoForge 只有一条加载提示和六条带条件的游戏提示，测试位置无匹配条目，因此暂停菜单没有内容。

Both default language templates now contain six useful unconditional any-stage tips at six seconds per entry; the redundant Esc instruction was removed. The existing timed/random selection algorithm is unchanged. Existing customized files are not rewritten by the mod. The user's existing NeoForge English/Chinese files were backed up under codex-migration-backup/pause-tips-20261004 and pause-tip-rotation-20261004. Only our single Esc entry was removed and replaced by six general entries; original configured entries were retained. Forge pack configuration was not changed.

中英文默认模板已加入六条实用的无条件 any 阶段提示，每条六秒，删除多余的 Esc 操作提示；原有计时与随机选择算法保持不变。模组不会覆盖已有自定义文件。用户现有 NeoForge 中英文配置已备份到 codex-migration-backup/pause-tips-20261004 与 pause-tip-rotation-20261004；仅将我们此前补充的单条 Esc 提示替换为六条通用提示，原条目保留。Forge 整合包配置未改。

A test first reproduced missing unconditional default pause tips; a follow-up regression reproduced the single-entry pool and now verifies multiple distinct defaults and absence of Esc instructions. Runtime validation opened the actual PauseScreen through the game API without keyboard or mouse input, confirmed the integrated server was paused, received 2388 real screen-render callbacks over 20 seconds, observed three different selected tips, and captured the framebuffer. Visual inspection confirmed the colored tip panel in the lower-left corner. The client passed 17 checks and saved/exited normally. Evidence: .gradle/migration/pause-tip-rotation.png and tip-rotation-final-client.log.

回归测试先复现默认暂停提示缺失，再补充验证提示池包含多条不同内容且不含多余 Esc 指令。运行验证通过游戏 API 打开实际 PauseScreen，不发送键鼠输入；确认单人游戏已经暂停，持续 20 秒，收到 2388 次真实界面绘制回调，观察到 3 条不同提示并保存画面。截图确认左下角彩色提示面板显示。客户端共 17 项检查通过，正常保存并退出。证据位于 .gradle/migration/pause-tip-rotation.png 与 tip-rotation-final-client.log。

The original Forge baseline comparison above was recorded at migration commit bc72eb0 before this follow-up. This fix intentionally adds default configuration entries; current release hashes are listed above.

上表中的 Forge 基线对比来自本次后续修复之前的迁移提交 bc72eb0。本次有意增加默认配置条目；上表产物哈希已更新为当前版本。

## Known baseline limitations / 已知基线限制

- Forge MerchantMenuMixin injection targets an inherited clickMenuButton method that is absent from the target class itself. This preexisting issue is registered, not silently suppressed.
- The Forge FTB anonymous button references its outer Mixin class, risking IllegalClassLoadError. Both resulting references are registered notes. NeoForge uses a helper outside the Mixin package.
- Four existing uppercase armor texture paths produce warnings. Their resources were preserved.
- No 26.1.2 build, full multiplayer session or optional-mod-absent client matrix was claimed.

- Forge MerchantMenuMixin 注入的 clickMenuButton 是继承方法，在目标类自身中不存在；该原有问题已登记。
- Forge FTB 匿名按钮引用外层 Mixin 类，存在 IllegalClassLoadError 风险；对应两条引用已登记。NeoForge 使用 Mixin 包外辅助类。
- 四个原有含大写字符的护甲纹理路径会产生警告，资源保持原样。
- 本次未声称完成 26.1.2 构建、完整多人会话或缺少可选模组的客户端组合测试。

## Documentation and source control / 文档与版本控制

CHANGELOG.md keeps newest entries above preserved history. README and the external AdventureSystems.md are concise descriptions. Detailed tutorials are in D:/IDEAWork/AdventureSystems-wiki: 10 separate English/Chinese page pairs plus Home and sidebar, with reciprocal links. The Wiki is a local repository pending publication.

CHANGELOG.md 新日志在上，保留旧历史；README 与外部 AdventureSystems.md 保持简明介绍。详细教程位于 D:/IDEAWork/AdventureSystems-wiki，共 10 组中英文独立页面，加 Home 和侧栏，提供语言互链；Wiki 为待发布的本地仓库。

Preexisting AdventureSystems edits were backed up before migration and retained in the final implementation. Sibling guard commits use a separate Git index to avoid including unrelated working or staged changes, including CombatSystems' staged IDE files. No pushes or releases were performed.

迁移前已备份 AdventureSystems 原有修改，并保留在最终实现中。其他附属的语言约束提交使用独立 Git 索引，避免混入已有工作区或暂存区修改，包含 CombatSystems 已暂存的 IDE 文件。没有推送或发布。

## Conditional tip follow-up / 条件提示补充验证

2026-10-04. The existing 1.21.1 profile was launched by command. No game downloads, memory changes or desktop input were used. The test located a genuine minecraft:village_plains structure, tested a standable position inside a structure piece (301, 70, -284) and a nearby position outside all detected structure pieces (306, 71, -250). Item/village examples came from the user's existing configuration. The combined AND entry was temporary in-memory test data; configuration files were not changed.

使用现有 1.21.1 实例命令启动，不下载游戏、不改内存、不发送桌面输入。测试定位真实 minecraft:village_plains 结构，使用结构片段内可站立位置 (301, 70, -284) 与附近不属于已检测结构片段的位置 (306, 71, -250)。物品和村庄条目来自用户现有配置；组合 AND 条件条目仅为内存中的临时测试数据，没有改写提示文件。

| Scenario / 场景 | Undamaged sword / 未损坏剑 | Village / 村庄 | Combined AND / 两者同时 | Actual screen / 实际界面 |
| --- | --- | --- | --- | --- |
| Empty outside / 结构外空手 | false | false | false | No conditional tip / 不显示条件提示 |
| Sword only outside / 结构外仅持合金剑 | true | false | false | Component tip visible / 组件提示显示 |
| Damaged sword outside / 结构外损坏合金剑 | false | false | false | No conditional tip / 不显示条件提示 |
| Village empty / 村庄结构内空手 | false | true | false | Village tip visible / 村庄提示显示 |
| Village and sword / 村庄结构内持剑 | true | true | true | Both original tips rotated in the same actual pause menu / 两条原有提示在真实暂停菜单中轮换 |
| Leave village empty / 离开村庄结构后空手 | false | false | false | Cached village match cleared / 旧村庄匹配清除 |

The item example requires the native component predicate damage=0 in WEAK mode. Therefore a sword with damage=1 intentionally does not match. Structure matching currently requires being within a real structure piece bounding box; being visually near a village does not by itself satisfy this predicate.

物品示例使用原生组件 damage=0 与 WEAK 匹配，因此 damage=1 的剑按配置应不匹配。当前结构检测要求玩家位于实际结构片段包围盒中，仅在视觉上靠近村庄并不自动满足条件。

18 positive/negative condition assertions plus one actual two-tip rotation assertion passed: ADVENTURE_CONDITIONS_PASS checks=19 failures=0 restored=true. Village information came through the real request/response channel when opening PauseScreen; the test did not manually assign the structure cache. The user inventory was saved with native registry-aware inventory serialization, restored exactly (inventory=true), and original dimension/position/rotation/selected slot restored before normal world save and exit. Existing 17 general runtime checks also passed.

18 项条件正反断言与 1 项实际双提示轮换断言通过。村庄信息来自打开 PauseScreen 后的真实网络请求／响应，测试未直接改写结构缓存。背包使用原生注册表相关存储保存并恢复，确认 inventory=true；原维度、位置、视角与选中槽位均恢复后正常保存退出。此前 17 项通用运行检查亦通过。

Evidence / 证据：

- .gradle/migration/tips-conditions-final-client.log
- .gradle/migration/tips-condition-sword-only-outside.png
- .gradle/migration/tips-condition-village-empty.png
- .gradle/migration/tips-condition-combined-sword.png
- .gradle/migration/tips-condition-combined-village.png

Only test fixtures and this verification record changed. Production release hashes remain unchanged. The temporary validation JAR was removed from mods after the client exited.

本次仅修改测试与验证记录，正式 JAR 哈希不变；客户端退出后已将临时验证 JAR 移出 mods。
