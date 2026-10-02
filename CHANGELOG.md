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

---

2026年10月02日 13时53分

- Enabled KineticCore addon architecture validation during compilation.
- Verified the full build, final-JAR API references, and real development-client startup. No source-level warning suppressions were added.

- 在编译阶段接入 KineticCore 附属架构验证。
- 完整构建、最终 JAR API 引用检查及真实开发客户端启动验证通过，未添加源码级警告抑制。
