# Changelog / 更新日志

## 26.10.6 — 2026-10-06 07:12 +08:00

### English

- Heart of Steel and Paradise Lost tooltips show the server's numbers. The server writes them onto the accessory itself (refreshed every second while you carry or wear it), so a client never shows its own local settings and nothing is sent to other players when an admin changes them. Until an item has values from the server, its tooltip says so instead of showing numbers. The equip conflict check on the client uses the same server values.
- Editing the shop refreshes only the editor; other players see the change when they open the shop. Saving tips updates only the editor; other players receive them when they log in.
- The shop entry editor's page, buy/sell and content mode menus mark their current option yellow as single choices.
- Shop, wallet, FTB and tips editor text scrolls inside its own region instead of being cut off or running into neighbors.
- The tips structure, biome, advancement and dimension pickers list their entries as plain text rows instead of a column of buttons.
- On 26.1.2 the shop entry editor's item, currency and barter item buttons were blank; they show their text and item icon as on the other versions. The editor's fields and labels keep 3 px clear of the section frames.
- Requires KineticCore 26.10.5+.

### 简体中文

- 钢铁之心与失乐园的提示显示服务端的数值。服务端把数值写在饰品本身上（携带或佩戴时每秒刷新），客户端从不显示本地设置；管理员修改设置时也不会向其他玩家发送任何内容。物品还没有服务端数值时，提示会说明这一点而不显示数字。客户端的装备排斥检查同样使用服务端数值。
- 编辑商店只刷新编辑者；其他玩家在打开商店时看到变更。保存提示只更新编辑者；其他玩家在登录时获得。
- 商店条目编辑器的分页、买卖与内容模式菜单作为单选，用黄色标出当前选项。
- 商店、钱包、FTB 与提示编辑器的文字在各自区域内滚动，不再被截断或压到相邻元素。
- 提示的结构、生物群系、进度和维度选择器改用纯文字行列出条目，不再是一列按钮。
- 26.1.2 上商店条目编辑器的物品、货币与以物易物按钮显示为空白；现在与其他版本一样显示文字和物品图标。编辑器的输入框和标签与分区边框保持 3 像素间距。
- 需要 KineticCore 26.10.5+。

---

## 26.10.4 — 2026-10-04 20:42 +08:00

### English

- Bound and scroll long GUI labels, titles, names and instructions in shop, wallet, FTB and tips editors while preserving colors and clearing neighboring controls.
- Reserve item-button icon space, raise shop pickers above detail text, separate reward instructions from actions and keep tip conditions scrollable above Save/Back in small windows.
- All three release builds and exact bilingual key parity pass. Real 1.21.1 GUI checks cover both languages, two window sizes and extended translations; evidence and limits: docs/gui-long-text-verification.md.

### 简体中文

- 商店、钱包、FTB 与提示编辑器的长标签、标题、名称和说明在自己的区域内滚动，保留颜色并避开相邻控件。
- 物品按钮预留图标，商店选择面板置于详情文字上层，奖励池说明与按钮分行；小窗口的提示条件列表可在保存/返回上方滚动。
- 三版本发布构建与中英文键名一致性通过；真实 1.21.1 界面检查覆盖中英文、两种窗口与加长翻译，证据与验证范围见 docs/gui-long-text-verification.md。

## 26.10.4 — 2026-10-04

### English

- Verified native item-component and village tip conditions in the real 1.21.1 client: standalone sword, village without items, both matching tips rotating, damage mismatch and leaving the structure; 19 checks passed, with test inventory/position restored.
- Added six useful general tips to the English/Chinese default configurations for timed rotation even without matching conditions. Removed the redundant instruction to press Esc. Existing custom files remain intact.
- Migrated build architecture to Java 21, Gradle 9.8.0, Stonecutter and ModDevGradle; enabled Forge 1.20.1, NeoForge 1.21.1 and NeoForge 26.1.2 (Java 25).
- 26.1.2 builds against Curios 15, FTB Library / Quests / Teams 26.1.2, JEI 29.43, Sophisticated Backpacks 3.26, Refined Storage 3.2 and Architectury 20.1: wallet, shop, Heart of Steel, Paradise Lost, Levitation Backpack, FTB quest submission limits and tips start on a 26.1.2 server and client. Slot conflict checks follow Curios 15's item-change event; items carry their registry id and 26.1 item model definitions; saved data, tooltips, cooldowns, notify sounds and backpack extraction use the 26.1 APIs.
- Fixed on 1.21.1: the Levitation Backpack recipe and the Curios slot item tags were packaged in 1.20.1 folders (recipes/, tags/items/) that 1.21 no longer reads; each version now ships its own data folder and recipe format.
- Renamed four item textures to lowercase (invicon_iron_*.png); resource paths must be lowercase, so the game skipped them on every version with an "Invalid path" warning.
- The JUnit/regression suite runs on 1.20.1 and 1.21.1; it boots Minecraft through a stand-in mod list that FancyModLoader 11 (26.1) does not support, so 26.1.2 was checked in a running server and client.
- Release names include loader and Minecraft version: adventuresystems-<loader>-<minecraft>-<version>.jar.
- Forge retains its original NBT workflow; NeoForge uses native data components for items, component-aware wallet/shop records, quest matches and tip conditions. No old item-NBT converter is provided.
- Kept only authored language keys in release JSON: English and Chinese now contain the same 755 public keys. Runtime formatting preserves colors, arguments, resource-pack overrides and language switching without generated `.formatted*` entries.
- Added mandatory source/override/package language-key parity and string-value validation; generated formatting keys fail the build.
- Fixed component delimiters in shop records, checked current RS 2 extraction permissions, deferred dynamic tip components until world registries exist, and used client registries for client-side enchanted-item matching.
- Moved detailed tutorials into separate English and Chinese local Wiki pages with reciprocal language links. Wiki publication is pending.

### 简体中文

- 在真实 1.21.1 客户端验证物品组件与村庄提示：单独持剑、村庄内空手、两种匹配提示轮换、耐久不符与离开结构均通过；共 19 项检查，测试背包和位置已恢复。
- 中英文默认配置增加六条实用通用提示，无条件匹配时也可定时轮换；删除多余的“按 Esc”操作提示，保留已有自定义文件。
- 构建迁移到 Java 21、Gradle 9.8.0、Stonecutter 与 ModDevGradle，启用 Forge 1.20.1、NeoForge 1.21.1 与 NeoForge 26.1.2（Java 25）。
- 26.1.2 基于 Curios 15、FTB Library / Quests / Teams 26.1.2、JEI 29.43、精妙背包 3.26、Refined Storage 3.2 与 Architectury 20.1 构建：钱包、商店、钢铁之心、失乐园、悬浮背包、FTB 任务提交限制与提示均可在 26.1.2 服务端和客户端启动。饰品冲突检查跟随 Curios 15 的物品变更事件；物品带有注册 ID 与 26.1 物品模型定义；存档数据、提示、冷却、提示音与背包提取使用 26.1 的接口。
- 修复 1.21.1：悬浮背包配方与 Curios 槽位物品标签此前打包在 1.20.1 的目录（recipes/、tags/items/），1.21 不再读取；现在各版本使用各自的数据目录与配方格式。
- 将四张物品贴图改为小写文件名（invicon_iron_*.png）；资源路径必须小写，此前各版本都会以“Invalid path”警告跳过它们。
- JUnit/回归测试在 1.20.1 与 1.21.1 上运行；它们通过替身模组列表启动 Minecraft，FancyModLoader 11（26.1）不支持这种方式，因此 26.1.2 在实际运行的服务端与客户端中检查。
- 发布文件名包含加载器与 Minecraft 版本：adventuresystems-<加载器>-<Minecraft版本>-<模组版本>.jar。
- Forge 保留原有 NBT 流程；NeoForge 的物品、钱包和商店记录、任务匹配及提示条件使用原生数据组件，不提供旧物品 NBT 转换器。
- 发布语言 JSON 只保留人工编写的语言键，中英文均为同一套 755 个完整公开键。运行时格式处理保留颜色、参数、资源包覆盖和语言切换，不再生成 `.formatted*` 派生键。
- 构建强制检查源码、版本覆盖和打包资源的中英文键名一致、值为字符串；存在派生格式键时构建失败。
- 修正商店记录中的组件分隔符，校验 RS 2 当前提取权限，等待世界注册表就绪后解析动态提示组件，并为客户端附魔物品匹配使用客户端注册表。
- 详细教程移至中英文独立的本地 Wiki 页面，页首提供语言互链。Wiki 待上线。

---

2026年10月02日 13时53分

- Enabled KineticCore addon architecture validation during compilation.
- Verified the full build, final-JAR API references, and real development-client startup. No source-level warning suppressions were added.

- 在编译阶段接入 KineticCore 附属架构验证。
- 完整构建、最终 JAR API 引用检查及真实开发客户端启动验证通过，未添加源码级警告抑制。


---

历史记录（原记录未标注时间）

- New and edited shop entries now show up immediately after saving, without reopening the shop; the shop updates locally first and then syncs with the server.
- Saving an entry now returns to the shop and selects the saved entry.
- The page field in the entry editor is now a selector: choose an existing page or the default page, or pick "New Page…" to type a new one. New entries default to the page you are browsing.
- Move to Front / Up / Down / Move to End and drag sorting now work within the current page instead of the whole list.
- Fixed an entry switched between Buy and Sell overwriting another entry in the other list.
- The shop toolbar is more compact: the edit options and the item source switches (Sophisticated Backpack / RS Disk) are now drop-down menus.
- Entry type and reward content type are now chosen from drop-down menus instead of clicking through them.
- On/off switches in the shop menus now show their state as green "On" or red "Off" text.
- Gacha and choice entries now cycle their reward icon once per second, and the "+" marker is no longer hidden behind the item.
- Each entry can now have its own icon in the shop list: left-click the Icon slot in the editor to choose one, right-click to go back to the entry item itself.
- The currency picker now shows currencies as compact boxes; click a box to choose it.
- In the command reward manager, the icon is a plain item slot that you click to change; the Done button now matches the other buttons in size.
- Probabilities no longer show trailing zeros: whole numbers have no decimals, and up to 5 decimal places are shown (e.g. 1.005% instead of 1.00500%).
- The page for entries without a page is now called "Default" instead of a second "All".
- Text colours follow the KineticCore rules: no grey text.
- Requires KineticCore 26.10.1 or newer.

- 新增或修改商品保存后立即显示，无需重新打开商店；先在本地更新，再与服务器同步。
- 保存商品后自动返回商店并选中该商品。
- 商品编辑器的分页名改为选择器：可选择已有分页或默认分页，也可选“新建分页…”输入新分页名；新建商品默认放在当前浏览的分页。
- 移到最前 / 上移 / 下移 / 移到最后以及拖拽排序改为在当前分页内进行，不再以全部商品为准。
- 修复商品在购买和出售之间切换后，会覆盖另一列表中商品的问题。
- 商店顶部栏更紧凑：编辑选项和物品来源开关（精妙背包 / RS 磁盘）改为下拉菜单。
- 条目类型和奖励内容类型改为下拉选择，不再需要反复点击切换。
- 商店菜单中的开关改为用绿色“开”、红色“关”文字显示状态。
- 抽奖和自选商品的奖励图标改为每秒切换一次，“+”标记不再被物品挡住。
- 每个商品可以单独设置商店列表中的显示图标：在编辑器中左键点击“显示图标”格选择，右键恢复为商品本身的图标。
- 货币选择改为紧凑的方框，点击方框即可选择。
- 指令奖励管理中的图标只显示物品格，点击格子即可更换；“完成”按钮与其它按钮大小一致。
- 概率不再显示多余的 0：整数不带小数点，最多显示 5 位小数（例如显示 1.005%，而不是 1.00500%）。
- 未设置分页的商品所在分页改名为“默认”，不再与“全部”重名。
- 文字颜色遵循 KineticCore 规范，不再使用灰色文字。
- 需要 KineticCore 26.10.1 或更高版本。
