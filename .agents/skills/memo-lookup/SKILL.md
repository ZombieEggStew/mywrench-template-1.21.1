---
name: memo-lookup
description: '查阅项目 memo 文档。memo/code-map.md 是定位 Java 源码的首要入口，memo/api-code-map.md 是查外部依赖 API 的首要入口。当需要定位应修改的 Java 文件、了解项目当前状态与进度、或查阅 Create/Ponder/Flywheel/Registrate 的 API 位置、踩坑记录时使用此技能。'
---

# 项目 Memo 查阅

> memo 全部位于项目根 `memo/`。当前项目处于骨架阶段，memo 文件会随开发逐步丰富；**没有对应主题文件就说明还没写过，不要凭空创建大量文档**。

## 两个首要入口

### `memo/code-map.md` —— 定位 Java 源码

> 需要定位或修改 `src/main/java/com/zzy205/mywrench` 下的代码时，**第一步先读它，不要直接搜索源码**。

包含：

- **按修改目标定位表**：把需求（新增方块 / 配置项 / 启动逻辑 / 依赖升级 …）直接映射到「首先查看」和「通常还要检查」的文件。
- **模块职责表**：启动与注册、配置、资源各自负责什么，什么场景要改。
- **当前已知边界**：如「`neoforge.mods.toml` 里不能声明 registrate 依赖」「versionRange 用 mod 版本而不是 maven 版本」，改相关代码前必须核对。
- **维护约定**：新增/删除/重命名 Java 文件时要回来同步更新。

### `memo/api-code-map.md` —— 查外部依赖 API

> 需要确认 Create / Ponder / Flywheel / Catnip / Registrate 的包路径、类位置、方法签名时，**先读它，再去 `D:\aMyMCMods\api\` 里 grep**。

包含包级地图（Create 的 `api/ foundation/ content/` 分工、Ponder 的 `api/ foundation/`、Catnip 的 `outliner/ render/ math/ gui/`、Flywheel 的 `api/visual` 等）、import 前缀 → 绝对路径对照表、以及「不在 api/ 里的依赖怎么取源码」。

## 何时使用

- 需要定位或修改 Java 源码 → 先读 `code-map.md`
- 需要查外部依赖 API 的包路径/类位置/签名 → 先读 `api-code-map.md`，再进 `D:\aMyMCMods\api\` 搜索（细则见 `minecraft-research` 技能）
- 想知道项目当前做到哪一步、有哪些已知约束 → 读 `code-map.md` 的「现状总览」与「当前已知边界」
- 遇到以前踩过的坑 → 读 `memo/ATTENTION.md`（**首次出现踩坑记录时才创建**）

## Memo 文件索引

| 文件 | 用途 | 何时查阅 |
|------|------|---------|
| `code-map.md` | Java 源码职责、修改入口、现状与已知边界 | 定位/修改 Java 文件时（首要入口） |
| `api-code-map.md` | 外部依赖源码包级地图与路径对照 | 查依赖 API 时（首要入口） |
| `ATTENTION.md` | 踩坑与注意事项合集（**尚未创建**） | 第一次踩坑后创建并在其中累积；改代码前若存在则先核对对应小节 |

> 新主题文档（如 `registrate-datagen.md`、`ponder-scene.md`、`flywheel-visual.md`）**在真正落地对应功能后再建**，并在下面记录。

## 最小上下文原则

- 不要在每次任务开始时读取全部 memo。
- 只读取与当前请求直接对应的一份；仅当它引用了另一个必须的文档时再读下一份。
- 与项目状态或设计取舍无关的任务，不加载 memo。
- 定位 Java 文件查 `code-map.md`；定位外部 API 查 `api-code-map.md`。

## 查阅步骤

1. **确定文档**：
   - 定位/修改 Java 源码 → `code-map.md`
   - 查外部依赖 API → `api-code-map.md`
   - 旋转/朝向 → `neoforge-create-rotation` 技能
   - OBJ / 独立模型烘焙 → `neoforge-model-rendering` 技能
   - Create 风格 GUI → `create-style-gui` 技能
   - 依赖坐标/版本问题 → `AGENTS.md`「依赖变更规则」
2. **读取相关段落**（`read` 指定 offset/limit，不必整篇读完）。
3. **结合源码实施**：memo 给结论与路径，具体 API 仍要回源码核对。
4. **回写**：任务改变了代码结构、模块边界或踩到新坑时，同步更新 `code-map.md`（及 `ATTENTION.md`）。

## 注意事项

- memo 只保存**当前项目的状态、约束和结论**；通用的技术任务流放在 `.agents/skills/`，不要重复抄。
- memo 里的路径都是相对项目根；外部依赖源码是绝对路径 `D:\aMyMCMods\api\`。
- memo 与 skill 都回答不了时，回退到 `minecraft-research` 技能。
