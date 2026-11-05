# Changelog / 更新日志
- Tip editor: a tip that breaks the limits (16 characters per line, 3 lines) is shown in red in the list, its tooltip says what is wrong, and the content field has a red border while it is over. Saving with such a tip selects it and names the problem (for example "line 1 has 23 characters; at most 16 per line"); before, the same general limit message appeared whatever the tip being edited looked like, because the old long demo tips in the list blocked saving.
- 提示编辑器：超出限制（每行 16 个字、最多 3 行）的提示在列表中显示为红色，悬浮提示说明原因，内容输入框在超限时显示红色边框。存在这类提示时点击保存，会自动选中它并说明具体问题（例如“第 1 行有 23 个字，每行最多 16 个”）；此前无论当前提示多长都会弹出同一条限制说明，原因是列表里旧的长演示提示阻止了保存。
- Tip files that contain only the old demo tips, even when an older editor saved them again or some were deleted, are replaced by the current short tips. Those old tips are all over the limits, so they were never shown and could not be saved.
- 只包含旧演示提示的提示文件（即使被旧版编辑器重新保存过或删掉了其中几条）会替换为当前的简短提示。这些旧提示全部超出限制，既不会显示，也无法保存。
- New back accessory, the Wind-Thunder Wings, after the roc wings of the anime: a modelled pair of solid teal wings with three-jointed bones, ten two-part primaries that bend under the air and spread into fingers at the tip, ten broad secondaries, tertials, two rows of coverts and an alula, all overlapping, lit by the world and trembling in the wind, with fine blue-white lightning crawling along the bones and feathers. Worn, they grant KineticCore super flight (double-jump to take off): the wings grow out of the back and beat like a great bird's, a quick downstroke and a slow upstroke with the hand folding in, about once every 1.4 seconds hovering and 2.2 seconds in level flight, where they are held up in a soaring dihedral. Moving takes the flight pose; letting go glides on and slows to an upright hover. Normal flight is twice creative speed. Light Escape (R by default) readies the light: standing still the wearer stays as they are, and once moving they flash into a streak of green light, the body and wings gone, at six times creative speed (three times normal flight) — a white-hot core in a stretched body of light, a soft trail that lingers and fades, and motes of light shed behind, with no lightning. Stopping brakes the light, gathers it back in a closing ring and a soft flash, and the wearer reappears with motes rising. Blood Burn (H) is the blood-shadow escape: blood red, wrapped in a dark red mist, twenty times creative speed (ten times normal flight) for ten seconds; it takes 30% of max health, needs at least 30% health and cools down for 15 seconds after it ends. Landing ends the light. Wing beats, the light breaking away and gathering back, its rush and the blood burn's heartbeat and roar are mixed from public-domain (CC0) recordings and can be replaced by resource packs (see sounds/wings/CREDITS.txt). Speeds, burn duration, health cost, minimum health and cooldown are on the mechanics page in F6; colours, trail length and width, wings, sparks and sounds on the Wind-Thunder Wings Look page. Crafted from lightning rods, a nether star, feathers, an elytra, phantom membranes and a diamond block. Requires KineticCore 26.10.31.
- 新增背部饰品“风雷翅”，按动画里雷鹏骨翅的样子制作：建模的一对青色实心羽翼——三段翼骨，十根分两节的初级飞羽（梢部受风后弯、末端分开如指），十根宽阔的次级飞羽，三级飞羽，两层覆羽与小翼羽，彼此交叠，受世界光照并随风轻颤；细细的青白电弧沿翼骨与羽毛游走。佩戴后获得 KineticCore 的超人飞行（双击跳跃起飞）：双翼从背后生长展开，像大鸟一样扇动——下扑快、上抬慢，上抬时翼尖收拢；悬停时约 1.4 秒一拍，平飞时约 2.2 秒一拍并上扬滑翔。移动时进入飞行姿势，松开按键会继续滑行、减速后直立悬停。普通飞行为创造飞行的 2 倍速。遁光（默认 R）先蓄势：静止时身形如常，一飞动便光华一闪化作青绿遁光，身形与双翼隐去，速度为创造飞行的 6 倍（普通飞行的 3 倍）——白热的光核、拉长的光身、短暂停留后渐散的柔和光带、不断脱落的灵光碎点，没有闪电；停下时遁光刹住、光环收拢、光华一闪，现出身形，光点上浮飘散。燃血（默认 H）为血影遁：血红遁光外裹暗红血雾，10 秒内达到创造飞行的 20 倍（普通飞行的 10 倍），直接扣除 30% 最大生命，生命低于 30% 时无法发动，结束后冷却 15 秒。落地即收起遁光。振翅、化光与收光、遁光呼啸、燃血的心跳与轰鸣由公有领域（CC0）录音混制，可用资源包替换（见 sounds/wings/CREDITS.txt）。速度、燃血时长、扣血比例、最低生命与冷却在 F6 的机制页设置；颜色、尾迹长度与宽度、翅膀、火花与音效在“风雷翅外观”页设置。合成材料为避雷针、下界之星、羽毛、鞘翅、幻翼膜与钻石块。需要 KineticCore 26.10.31。
- Paradise Lost and Heart of Steel have new 32×32 textures drawn after what they do.
- 失乐园与心之钢换上了按其功能重新绘制的 32×32 贴图。
- The wallet's depositable coin items are edited as item slots with their names and values (click a slot to change the item, Add picks a new one) instead of text lines.
- 钱包“可存入的货币物品”改为物品格编辑：显示物品图标、名称与面值，点击格子更换物品，“添加”直接选择新物品，不再是文本行。
- Shop entry editor rebuilt on one page: the goods, payment and display icon are item slots with their names in colour, prices and limits sit in short fields, the content mode shows its reward items as icons, and the command rewards and required quests are listed in full on the right (click to edit). Back and Save are at the top.
- 商店条目编辑页重新排版：商品、支付物与显示图标都以物品格显示并配彩色名称，价格与限购用短输入框，内容模式下直接以图标列出奖励物品，右侧完整列出指令奖励与任务要求（点击即编辑），返回与保存在顶栏。
- Required quests are picked on one page: all quests on the left (live search, chosen ones in green), the chosen quests on the right; the separate quest picker is gone.
- 任务要求合并为一页：左侧为全部任务（输入即搜，已选为绿色），右侧为已选任务，不再另开任务选择窗口。
- FTB quest binding editor: short header buttons with Save at the top right, title centred, one-line quest rows, the current item with its name in colour, items with bindings as a compact icon grid; the empty panel and nested frames are gone.
- FTB 任务绑定编辑器：顶栏按钮缩短、保存在右上角、标题居中；任务改为单行显示，当前物品显示彩色名称，已有绑定的物品以紧凑图标网格显示，去掉空白面板与多层边框。
- The duration and exchange-count dialogs are compact, with their buttons in the header; the tip editor's condition buttons fit on one row.
- 时长与兑换次数对话框改为紧凑布局，按钮放在顶栏；提示编辑器的条件按钮排成一行。
- Reward pools: the add and clear buttons are as wide as their labels, and the count and weight fields sit on the same row at the right instead of at the bottom, so the list reaches the bottom of the page. FTB blacklists: the Add button is shorter.
- 奖励池：添加与清空按钮缩短为文字所需宽度，数量与权重输入框移到同一行右侧，不再放在底部，列表一直延伸到页面底部。FTB 黑名单：“添加”按钮缩短。
- Shop product cards are wider, so price, quest requirement and limit lines read without scrolling; their item icons use the standard slot size. Wallet coin rows, the currency picker and the command/reward/FTB blacklist grids use the standard 22 px item slots too.
- 商店商品卡片加宽，价格、任务要求与限购文字无需滚动即可读完，商品图标使用标准物品格；钱包货币行、货币选择、指令/奖励选择与 FTB 黑名单网格也统一为 22 像素物品格。
- Shop tooltips open beside the product card or reward slot they describe instead of covering it.
- 商店的悬浮提示显示在对应商品卡片或奖励格旁边，不再盖住它。
- The wallet holds one currency, Adventure Coins (冒险币). Coin items (gold, diamond and netherite coins by default) are no longer currencies in their own right: the wallet's new Quick deposit button turns every coin in the inventory into Adventure Coins at its configured value (gold 1, diamond 100, netherite 1000). The wallet screen shows the balance, what a quick deposit would add, and each coin item with its value and how many you carry.
- 钱包只保存一种货币：冒险币。金币、钻石币、合金币等物品不再各自作为钱包货币，钱包界面新增“快速存入”，按配置面值（金币 1、钻石币 100、合金币 1000）把背包里的货币物品换成冒险币。钱包界面显示余额、快速存入可得数量，以及每种货币物品的面值和背包中的数量。
- Exchange between currencies, withdrawing, the coin magnet and automatic pick-up into the wallet are removed, with their settings (maxCurrencyTypes, magnetRange, magnetIntervalTicks, exchangeRules, manualExchangeOnlyCurrencies). currencyDefinitions now lists the coin items the wallet takes in and their value. Balances stored per coin by earlier versions are not carried over.
- 移除货币兑换、取出、磁吸和拾取自动存入，以及对应设置（maxCurrencyTypes、magnetRange、magnetIntervalTicks、exchangeRules、manualExchangeOnlyCurrencies）。currencyDefinitions 改为“可存入的货币物品及面值”。旧版本按货币分别保存的余额不会自动转换。
- Shop prices and sales use adventuresystems:adventure_coin; coin items are sold in the shop like other goods. Villager trades and FTB item tasks that ask for a coin item still pay from the wallet, at the coin's value in Adventure Coins.
- 商店价格与出售收入使用 adventuresystems:adventure_coin；货币物品可以像普通商品一样在商店买卖。村民交易与 FTB 物品任务需要货币物品时，仍按面值从钱包的冒险币中扣除。
- New /kt wallet give|take <players> <amount> [coin item] and /kt wallet balance <player> (operators), and WalletApi for KubeJS (Wallet.give(player, 25)).
- 新增 /kt wallet give|take <玩家> <数量> [货币物品] 与 /kt wallet balance <玩家>（管理员），以及供 KubeJS 调用的 WalletApi（Wallet.give(player, 25)）。
- Shop cards get a red frame only when a product cannot be had (quest missing, sold out); a product the wallet cannot pay for yet shows its price in red. The FTB submit-count window says Back instead of Cancel.
- 商店卡片只有在商品无法获得时（缺任务、售罄）才显示红框；钱包暂时付不起的商品只把价格标红。FTB 提交数量窗口的“取消”改为“返回”。
- The wallet shop is redesigned as a three-column storefront. Pages are a vertical list with entry counts and the wallet balances sit under them; products are cards with their name, status and price that fill the width in whole columns; the selected product shows a large icon, its page, price, quest requirement, payment sources, limits, reward chances and its description, with the amount and the trade button at the bottom. Editor mode shows in the title. The three columns can be resized with the core layout editor (/ktlayout).
- 钱包商店重新设计为三栏：左侧是竖排分页列表（带商品数量），下方是钱包余额；中间是商品卡片（名称、状态、价格），按整列铺满宽度；右侧是所选商品的大图标、分页、价格、任务要求、支付来源、限购、奖励概率和商品描述，底部是数量与交易按钮。编辑模式显示在标题中。三栏宽度可用核心布局编辑器（/ktlayout）拖动调整。
- The reward choice dialog dims the shop behind it; a product with several quests shows ▾ on its quest button. The amount slider is gone: −1, +1, ×10, Max and the number field set the amount.
- 自选奖励弹窗打开时背后的商店变暗；需要多个任务的商品，其任务按钮带 ▾。去掉了数量滑块，数量用 −1、+1、×10、最大和输入框设置。
- Screens that closed with Done or Cancel at the top-left now show Back there (quest, command, reward and FTB editors, tip pickers, shop editors); the screen the wallet opens is unchanged.
- 左上角原为“完成”或“取消”的界面（任务、命令、奖励与 FTB 编辑器、提示选择、商店编辑）统一改为“返回”；钱包打开的界面保持不变。
- Chinese text on 1.21.1 and 26.1.2 no longer puts spaces around 数据组件 (for example 编辑数据组件 instead of 编辑 数据组件).
- 1.21.1 与 26.1.2 的中文不再在“数据组件”两侧留空格（例如“编辑数据组件”而不是“编辑 数据组件”）。
- The shop's reward selection window uses the same 20-pixel slots and item size as the product grid, with 2-pixel gaps and 14 slots per row, instead of enlarged slots with wide gaps.
- 商店“自选奖励”窗口的物品格改为与商品列表相同的 20 像素格子和物品大小，间距 2 像素、每行 14 格，不再放大物品、留大间距。
- The shop entry editor uses the full canvas as its frame, so Cancel and Save Entry keep the four-pixel margin instead of sitting on the border.
- 商店条目编辑器以完整画布为外框，取消与保存按钮保留 4 像素边距，不再压在边框上。
- Wallet, currency, command, shop and FTB item tooltips retain native item details and append the existing action hints.
- 钱包、货币、命令、商店及 FTB 物品提示保留完整原生详情，并附加原有操作说明。
- Reward picker titles and borders no longer index rewards or throw during hover; drawing, selection and native item tooltips share the same clipped rows. Saving a tip duration now returns to the editor, and registry selectors use the full canvas outer frame.
- 商店奖励选择器的标题及边缘不再触发越界悬停；绘制、点击与原生物品提示共用同一裁剪范围。提示时长保存后返回编辑器，注册表选择器外框覆盖完整画布。

## 2026-10-20 — Editor frames / 编辑器边框

- Editing canvases use one white outer frame. Shop, FTB binding and tip content regions use gray-white dividers without nested rectangular borders; interactive rows, item cards and popups keep their outlines.
- 编辑画布统一使用白色外框；商店、FTB 绑定与提示内容区域改用灰白分隔线，移除内部矩形边框，保留交互行、物品卡片与弹窗边框。

## 2026-10-16 — Wallet scrolling and shop navigation / 钱包滚动与商店入口

- Wallet currency and conversion rows share the core's precise scrollbar, allowing the final row and its buttons to be shown fully without scrolling into blank space.
- Currency updates rebuild row buttons together with the displayed conversion rows.
- The shop editor is the first entry in the adventure server configuration.
- Large wallet, quest, FTB and tip editors share a 640×360 canvas with 4 px spacing. Search fields are wider, with adjacent controls repositioned to fit.
- Existing delete and remove actions support the core Delete shortcut while text fields retain normal editing; small currency, time and quantity dialogs retain their compact layout.
- 钱包货币与兑换列表使用核心精确滚动条，滚轮和拖动滑块均可完整显示最后一行及其按钮，末尾不再多出空白。
- 货币更新时同步重建兑换行与对应按钮。
- 商店编辑入口移至冒险系统服务端配置的第一项。
- 大型钱包、任务、FTB与提示编辑页统一640×360画布和4点边距；搜索框加宽，并同步调整相邻控件避免重叠。
- 既有删除与移除操作接入核心Delete快捷键，文本输入保留正常编辑；货币、时间与数量等小型对话框保持紧凑布局。

## 2026-10-15 — Tips editor and shop tooltips / 提示编辑器与商店提示

### English

- The tips editor lists tips as text rows instead of buttons.
- Back and Esc in the tips editor return to the screen it was opened from instead of closing the menu.
- The shop's FTB quest button shows its tooltip when the mouse is on the button; it showed only just below it.

### 简体中文

- 提示编辑器左侧以文本行列出提示，不再使用按钮。
- 提示编辑器的返回按钮与 Esc 会回到打开它的界面，不再直接关闭菜单。
- 商店的 FTB 任务按钮在鼠标悬停在按钮上时显示提示；此前只在按钮下方才会出现。

## 2026-10-13 — Shop and picker layout fixes / 商店与选择窗口排版修复

### English

- The shop balance strip is only as tall as the rows its enabled currencies fill, leaving more room for the product list.
- The reward picker in the shop details covers the text beneath it instead of letting it show through.
- Quest pickers and bindings show FTB quest and chapter titles as FTB displays them, without raw `&` color codes, and unnamed quests use the current language.
- The currency picker has no empty band below its grid; the command reward picker, quest requirement list and tip duration dialog align their titles and controls on shared lines.
- The English cost preview reads "Requires 1K+" with a space.

### 简体中文

- 商店顶部余额栏只按已启用货币实际占用的行数显示，商品列表获得更多空间。
- 商店详情中的奖励选择列表会完整遮住下方文字，不再透出。
- 任务选择与绑定界面按 FTB 的显示方式展示任务和章节标题，不再出现 `&` 颜色代码原文；未命名任务随当前语言显示。
- 货币选择窗口去掉网格下方的空白；指令奖励选择、任务需求列表和提示时长窗口的标题与按钮对齐在同一行。
- 英文价格预览改为带空格的 “Requires 1K+”。

## 2026-10-11 — Short contextual tips / 简短情境提示

### English

- Replaced the default tips with 12 lesser-known vanilla mechanics and four configuration guides, including crop crowding, nearby copper oxidation, bee crop growth, mud-to-clay conversion, sponge water buckets, composter signals and stored furnace experience.
- Added environment-specific tips for sponge water buckets in the Nether, snowy cauldrons, village hero gifts and swamp slimes. Item-condition guidance differs between NBT and component versions.
- Removed the separate Tip heading. Short text stays on one line; longer tips use only the lines they need, with no extra title row.
- Tips now allow up to 16 visible characters per line and three lines. Enter \n in the editor to insert a new line; color codes do not count. Invalid text cannot be saved.
- Untouched earlier defaults migrate automatically. Custom files are preserved, including old long entries that can be shortened in the editor before saving.

### 简体中文

- 默认提示改为 12 条原版机制冷知识与 4 条配置教程，包含作物密植减速、邻近铜块影响氧化、蜜蜂催熟作物、泥巴转黏土、湿海绵取得水桶、堆肥桶信号和熔炉累积经验。
- 下界湿海绵取得水桶、下雪收集细雪、村庄英雄赠礼和沼泽史莱姆提示按环境筛选；物品条件教程按 NBT 与数据组件版本区分。
- 移除单独的“提示”标题行，短文本保持一行，较长内容仅按需要换行，不再额外占用一行标题。
- 每行最多 16 个可见字符，每条最多 3 行。编辑器输入 \n 换行，颜色代码不计入字数；超限内容无法保存。
- 未修改的旧默认提示自动更新。自定义文件保持原样，旧的长文本可在编辑器中缩短后保存。

## 2026-10-10 — Editor navigation / 编辑器返回位置

### English

- Back, Cancel and Done controls that return to the previous editor now sit at the upper left in shops, shop entry and reward editors, quest and currency pickers, FTB binding/submission dialogs, and tip selectors. The tip editor uses the lower-left corner because its header already contains search and editing controls.
- Header titles scroll within the space beside navigation buttons. Editor lists, item grids, fields, and save behavior retain their existing layout and actions.

### 简体中文

- 商店、商品与奖励编辑器、任务与货币选择器、FTB 关联与提交界面，以及提示选择器中用于回到上一界面的返回、取消和完成按钮，统一放在左上角。提示编辑器顶部已被搜索与编辑控件占用，因此使用左下角。
- 标题在返回按钮旁的区域内滚动；编辑列表、物品网格、输入框以及保存操作保留既有布局与行为。

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
