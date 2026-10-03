# AdventureSystems 多版本迁移设计

沿用用户已确认的核心及前序附属方案。启用 Forge 1.20.1 与 NeoForge 1.21.1，26.1.2 仅预留。Java 21、Gradle 9.8.0、Stonecutter 0.9.8、MDG 2.0.148。产物 adventuresystems-<loader>-<mc>-<version>.jar，输出 D:/NEWMODS。

保留已有未提交的文本、界面修改；旧构建及产物先备份。Forge 使用 NBT，原有行为以构建、文本验证及产物指令对比验证，不启动 1.20.1。NeoForge 使用原生 Data Components，网络、Curios、FTB、可选存储联动按各版本实际 API 移植，不添加旧物品 NBT 转换。世界/玩家持久化仍使用原生 NBT。

使用现有 PCL2 1.21.1 客户端，不修改内存、不下载游戏版本、不修改核心。CHANGELOG 新版本在上，简介精简，详细教程迁往 GitHub Wiki 的中英文独立页面及语言互链。仅本地提交，不推送发布。
