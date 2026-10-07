# Changelog / 更新日志

## 2026-10-08 — Item preview backgrounds / 物品预览背景

### English

- Shop balances, the small currency icons beside product prices, and the currency picker now share the same item-slot backgrounds.
- FTB item previews keep clear spacing between slots, and item icons stay inside their preview backgrounds.

### 简体中文

- 商店余额、商品计价的小货币图标与货币选择器统一使用物品格背景。
- FTB 物品预览格之间保持清晰间距，物品图标留在预览格内，不再贴边。

---

## 2026-10-08 — Shop reward probabilities / 商店奖励概率

### English

- Shop reward probabilities scroll within their original item cell instead of covering neighboring items.

### 简体中文

- 商店奖励概率在原有物品格内滚动显示，不再覆盖相邻物品。

---

## 26.10.6 — 2026-10-06 07:12 +08:00

### English

- Heart of Steel and Paradise Lost tooltips show the server's numbers. The server writes them onto the accessory itself (refreshed every second while you carry or wear it), so a client never shows its own local settings and nothing is sent to other players when an admin changes them. Until an item has values from the server, its tooltip says so instead of showing numbers. The equip conflict check on the client uses the same server values.
- Editing the shop refreshes only the editor; other players see the change when they open the shop. Saving tips updates only the editor; other players receive them when they log in.
- The shop entry editor's page, buy/sell and content mode menus mark their current option yellow as single choices.
- Shop, wallet, FTB and tips editor text scrolls inside its own region instead of being cut off or running into neighbors.
- The tips structure, biome, advancement and dimension pickers list their entries as plain text rows instead of a column of buttons.
- On 26.1.2 the shop entry editor's item, currency and barter item buttons were blank; they show their text and item icon as on the other versions. The editor's fields and labels keep 3 px clear of the section frames.
- Hovering a currency in the wallet shows where that money is: the wallet, the inventory (with each stack's size), Sophisticated Backpacks, the bound RS network (or why it is unavailable) and the total. The server counts them only for the player looking: when the wallet opens, after a wallet action, and every 2 seconds while the wallet stays open. The regular wallet sync does not carry them and nothing is sent to other players.
- The shop entry editor's "Price & Limits" title stops before the command rewards column, and the reward pool hides its weight field outside gacha pools, where it had no label and could not be edited.
- The wallet, the shop currency picker, the command reward picker and the quest requirement list are only as tall as their content needs (up to a limit, then they scroll) and sit in the middle of the window, instead of a fixed full-height frame with a large empty area below a few rows. The wallet rows' text keeps 2 px from the row frame. The command picker's title stops before its Cancel button, and its hint no longer touches the button.
- The shop entry editor's item, currency and barter buttons show only their text; the squeezed-in item icon is gone, since the display icon slot already shows the item. Their tooltips name the chosen item and its ID.
- In the shop, the reward choice dialog and the quest list hide the controls underneath while they are open and have an opaque background, so page buttons and text no longer show through them. Product tiles are taller, so their status line and price keep 2 to 3 px from the tile and badge frames; the trade amount slider keeps 2 px above the quick amount buttons, and the Buy button 2 px above the panel's bottom line.
- The FTB item binding editor's task rows are taller, so both text lines keep clear of the row frame instead of the second line sitting on its bottom line.
- Every screen keeps the same layout in every language: the shop balance label, page tabs, menu buttons, trade amount label and the editor's Save/Cancel buttons have fixed widths, the price and trade cost icons stay at fixed spots, the FTB item label and favourite column and the tips editor label column have fixed widths, and the currency picker's Cancel button no longer grows with its text. Longer text scrolls. The shop entry editor's item, currency and barter buttons draw their own labels, centred like every other button instead of sitting low.
- Requires KineticCore 26.10.5+.

### 简体中文

- 钢铁之心与失乐园的提示显示服务端的数值。服务端把数值写在饰品本身上（携带或佩戴时每秒刷新），客户端从不显示本地设置；管理员修改设置时也不会向其他玩家发送任何内容。物品还没有服务端数值时，提示会说明这一点而不显示数字。客户端的装备排斥检查同样使用服务端数值。
- 编辑商店只刷新编辑者；其他玩家在打开商店时看到变更。保存提示只更新编辑者；其他玩家在登录时获得。
- 商店条目编辑器的分页、买卖与内容模式菜单作为单选，用黄色标出当前选项。
- 商店、钱包、FTB 与提示编辑器的文字在各自区域内滚动，不再被截断或压到相邻元素。
- 提示的结构、生物群系、进度和维度选择器改用纯文字行列出条目，不再是一列按钮。
- 26.1.2 上商店条目编辑器的物品、货币与以物易物按钮显示为空白；现在与其他版本一样显示文字和物品图标。编辑器的输入框和标签与分区边框保持 3 像素间距。
- 在钱包中悬停某种货币，会显示这些钱都在哪里：钱包、背包（含每一组的数量）、精妙背包、已绑定的 RS 网络（或不可用的原因）以及总计。服务端只为查看的玩家统计：打开钱包时、钱包操作之后，以及钱包保持打开时每 2 秒一次。常规的钱包同步不携带这些数据，也不会发送给其他玩家。
- 商店条目编辑器的"价格与限购"标题在指令奖励一栏之前结束；奖励池在非抽奖池中隐藏权重输入框，那里它既没有标签也无法编辑。
- 钱包、商店货币选择、指令奖励选择和任务需求列表的高度按内容决定（超过上限后滚动），并位于窗口中央，不再是固定的整高边框、几行内容下面留下一大片空白。钱包行内文字与行边框保持 2 像素。指令奖励选择的标题在取消按钮之前结束，提示文字也不再紧贴该按钮。
- 商店条目编辑器的物品、货币与以物易物按钮只显示文字，去掉了挤在按钮里的物品图标，因为右侧显示图标格已经显示该物品。按钮的悬浮提示会列出所选物品的名称与 ID。
- 商店中的奖励选择对话框和任务列表打开时会隐藏其下方的控件，并使用不透明背景，页面按钮和文字不再透出来。商品格子加高，状态文字和价格与格子边框、价格框保持 2 至 3 像素；交易数量滑块与快捷数量按钮之间保持 2 像素，购买按钮与面板底边线保持 2 像素。
- FTB 物品绑定编辑器的任务行加高，两行文字都与行边框保持距离，第二行不再压在底边线上。
- 所有语言下界面排版相同：商店的余额标签、分页标签、菜单按钮、交易数量标签以及条目编辑器的保存/取消按钮使用固定宽度，价格与交易花费的图标位置固定，FTB 物品标签与收藏列以及提示编辑器的标签列宽度固定，货币选择的取消按钮也不再随文字变宽。过长的文字滚动显示。商店条目编辑器的物品、货币与以物易物按钮由按钮自己绘制文字，与其他按钮一样垂直居中，不再偏下。
- 需要 KineticCore 26.10.5+。

---

## 26.10.4 — 2026-10-04 20:42 +08:00

### English

- Bound and scroll long GUI labels, titles, names and instructions in shop, wallet, FTB and tips editors while preserving colors and clearing neighboring controls.
- Reserve item-button icon space, raise shop pickers above detail text, separate reward instructions from actions and keep tip conditions scrollable above Save/Back in small windows.

### 简体中文

- 商店、钱包、FTB 与提示编辑器的长标签、标题、名称和说明在自己的区域内滚动，保留颜色并避开相邻控件。
- 物品按钮预留图标，商店选择面板置于详情文字上层，奖励池说明与按钮分行；小窗口的提示条件列表可在保存/返回上方滚动。

## 26.10.4 — 2026-10-04

### English

- Added six useful general tips to the English/Chinese default configurations for timed rotation even without matching conditions. Removed the redundant instruction to press Esc. Existing custom files remain intact.
- Added support for Forge 1.20.1, NeoForge 1.21.1 and NeoForge 26.1.2 (Java 25).
- On 26.1.2, the wallet, shop, Heart of Steel, Paradise Lost, Levitation Backpack, FTB quest submission limits and tips work on servers and clients. Compatible dependency versions are Curios 15, FTB Library / Quests / Teams 26.1.2, JEI 29.43, Sophisticated Backpacks 3.26, Refined Storage 3.2 and Architectury 20.1.
- Fixed on 1.21.1: the Levitation Backpack recipe and the Curios slot item tags were packaged in 1.20.1 folders (recipes/, tags/items/) that 1.21 no longer reads; each version now ships its own data folder and recipe format.
- Renamed four item textures to lowercase (invicon_iron_*.png); resource paths must be lowercase, so the game skipped them on every version with an "Invalid path" warning.
- Release names include loader and Minecraft version: adventuresystems-<loader>-<minecraft>-<version>.jar.
- Forge retains its original NBT workflow; NeoForge uses native data components for items, component-aware wallet/shop records, quest matches and tip conditions. No old item-NBT converter is provided.
- Fixed component delimiters in shop records. RS 2 item extraction respects current permissions, dynamic tips wait until world data is available, and client-side tip conditions correctly match enchanted items.

### 简体中文

- 中英文默认配置增加六条实用通用提示，无条件匹配时也可定时轮换；删除多余的“按 Esc”操作提示，保留已有自定义文件。
- 新增 Forge 1.20.1、NeoForge 1.21.1 与 NeoForge 26.1.2 支持（Java 25）。
- 26.1.2 的钱包、商店、钢铁之心、失乐园、悬浮背包、FTB 任务提交限制与提示均可在服务端和客户端使用。兼容的依赖版本为 Curios 15、FTB Library / Quests / Teams 26.1.2、JEI 29.43、精妙背包 3.26、Refined Storage 3.2 与 Architectury 20.1。
- 修复 1.21.1：悬浮背包配方与 Curios 槽位物品标签此前打包在 1.20.1 的目录（recipes/、tags/items/），1.21 不再读取；现在各版本使用各自的数据目录与配方格式。
- 将四张物品贴图改为小写文件名（invicon_iron_*.png）；资源路径必须小写，此前各版本都会以“Invalid path”警告跳过它们。
- 发布文件名包含加载器与 Minecraft 版本：adventuresystems-<加载器>-<Minecraft版本>-<模组版本>.jar。
- Forge 保留原有 NBT 流程；NeoForge 的物品、钱包和商店记录、任务匹配及提示条件使用原生数据组件，不提供旧物品 NBT 转换器。
- 修正商店记录中的组件分隔符。RS 2 物品提取遵循当前权限，动态提示等待世界数据就绪后解析，客户端提示条件正确匹配附魔物品。

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
