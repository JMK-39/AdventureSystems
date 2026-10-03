# AdventureSystems Multiversion Migration Implementation Plan

> **For agentic workers:** Use superpowers:executing-plans; independent subsystem ports may use superpowers:dispatching-parallel-agents.

**Goal:** Preserve Forge gameplay and provide a complete NeoForge 1.21.1 port with verified native component data and bilingual documentation.
**Architecture:** Shared Stonecutter source and MDG loader scripts following KineticCore and migrated addons.
**Tech Stack:** Java 21, Gradle 9.8.0, Stonecutter 0.9.8, MDG 2.0.148.
**Spec:** ../specs/2026-10-03-multiversion-migration-design.md

## Global Constraints

Two enabled nodes only; 26.1.2 reserved. Preserve baseline Forge behavior and existing dirty modifications. Native components on NeoForge; no item NBT upgrade path. Existing PCL client, no memory changes or game downloads. Local commits only.

## Review Focus

Wallet counts and item variants must survive saves, network, purchases and optional storage extraction. Accessories retain ownership and progression without shared mutable CustomData. FTB submission keeps permission, repeatability, consumption and reward checks. Text styles and tooltip values survive registry-aware component serialization. Optional integrations do not crash absent-mod clients.

### Task 1: Forge build architecture

Files: settings.gradle, stonecutter.gradle, build.forge.gradle, build.neoforge.gradle, gradle/*, versions/*/gradle.properties, CI workflows.
Interfaces: release tasks and shared component source available for Task 2.
- [x] Save preexisting patch/untracked files and original successful build JAR.
- [x] Port verified common Gradle logic; retain standalone formatting checks; replace generated language helpers with runtime templates as subsequently required.
- [x] Run :1.20.1-forge:build --offline; expect zero unregistered checker issues, explicitly recorded Forge baseline notes and text formatting pass.
- [x] Compare released Forge class instructions and resources to baseline; inspect bytecode 65, refmap, JAVA_17 and manifest.

### Task 2: NeoForge native API port

Files: src/main/java/dev/xyat/adventuresystems/{curios,ftb,tips,text}, AdventureSystems.java, node metadata/dependencies and targeted tests.
Interfaces: uses Task 1 shared build; supplies two buildable nodes and registry-aware data serialization.
- [x] Obtain matching third-party mod artifacts from local pools first, official pinned sources when absent.
- [x] Add version branches for Curios/accessories, wallet native item components/network, FTB APIs/submission and tips/rendering.
- [x] Add and run focused regression checks before final implementation for component roundtrip, permissions and mutation hazards.
- [x] Enable NeoForge node; buildAll --offline must pass both nodes and mixin/ref checks.
- [x] Verify NeoForge with temporary runtime fixture in existing client profile, launched directly by commands; inspect logs and close normally.

### Task 3: Documentation, review and delivery

Files: README.md, CHANGELOG.md, docs/multiversion-migration-report.md, external AdventureSystems.md and AdventureSystems-wiki pages.
Interfaces: documents Task 2 exact verified behavior.
- [x] Preserve all old changelog entries, newest entry first; shorten mod description.
- [x] Separate English/Chinese Wiki topic pages matching Core with reciprocal links.
- [x] Request fresh whole-change review; fix material findings and verify tests.
- [x] Run final offline build and JAR/Wiki checks, local commits only.
