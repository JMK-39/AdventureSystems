# Addon language key audit / 附属语言键检查

2026-10-04. Checks cover authored source files, version-specific overlays and processed resources. Exact key sets are compared; values must be strings. Generated `.formatted`, `.formatted.original` and `.formatted.part.N` keys are forbidden.

检查源码、版本覆盖和最终资源，要求中英文完整键名逐字一致、值为字符串，禁止自动格式辅助键。检查由各项目的 `gradle/kinetic-language-validation.gradle` 接入资源处理和 `check`。KineticCore 的构建文件不在本次修改范围。

| Addon | Source keys per language | Forge packaged | NeoForge packaged | Result |
| --- | ---: | ---: | ---: | --- |
| AdventureSystems | 755 | 755 | 755 | PASS |
| CombatSystems | 2494 | 2494 | — | PASS |
| ContentStudio | 871 | 871 | 889 | PASS |
| EnchantWorks | 105 | 105 | — | PASS |
| EntityControl | 1196 | 1196 | 1201 | PASS |
| ItemControl | 497 | 497 | 502 | PASS |
| KineticArmory | 389 | 389 | 392 | PASS |
| MobAscension | 482 | 482 | — | PASS |
| ModRefinery | 178 | 178 | — | PASS |
| RealmControl | 410 | 410 | 410 | PASS |
| TACZWorkshop | 632 | 632 | — | PASS |
| TextStudio | 471 | 471 | 471 | PASS |

A dash indicates no enabled NeoForge node in that project. MobAscension had no release JAR in NEWMODS; its processed Forge resources were checked directly. AdventureSystems was the sole generator of helper language keys: old release English had 2611 fields (755 authored +1856 generated), Chinese 2631 (755+1876). New releases have 755 each and identical complete keys. KineticArmory's NeoForge overlay is now 11/11 after adding two missing Chinese component labels.

“—”表示该项目未启用 NeoForge 节点。MobAscension 在 NEWMODS 没有发布 JAR，本次直接验证了处理后的 Forge 资源。只有 AdventureSystems 生成了语言辅助键；修复前英文 2611 个字段、中文 2631 个字段，修复后各 755 个且完整键集合一致。KineticArmory 的 NeoForge 覆盖文件补齐两条中文组件文案后为 11/11。

Negative fixtures demonstrate build failure for mismatched keys, generated keys even when present in both locales, and non-string values. The valid fixture and all 12 projects' resource tasks pass offline.

失败用例已验证：漏键、双方都有的派生格式键、非字符串值均阻止构建；合法用例和 12 个项目的资源任务均离线通过。
