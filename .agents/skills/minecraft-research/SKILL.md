---
name: minecraft-research
description: 'Minecraft 相关调研：当需要确认 Minecraft 本体、或依赖 mod（Create、Ponder、Flywheel、Catnip、Registrate）的源码、API 与实现方式时使用。API/接口查询用共享的 api/（干净源码，位置 D:\aMyMCMods\api），案例参考用共享的 references/（D:\aMyMCMods\references），个人调研源码放项目内 .research/；用于确认 API 方法签名、追踪调用链、调试兼容性或理解注册与渲染机制。'
---

# Minecraft 相关调研

> 调研 Minecraft 本体与 mod 源码：定位 API、参考案例实现、追踪跨 mod 调用链。

> 本项目的依赖源码**不在仓库内**，而是共享在两个固定位置（只读）：
> `D:\aMyMCMods\api`（依赖 API 源码）与 `D:\aMyMCMods\references`（案例 mod 完整源码）。
> 项目内的 `.research/` 只放个人临时调研产物。

## 何时使用

- 需要查看 Minecraft 本体（`net.minecraft.*`）或依赖 mod 的类、方法、接口源码
- 查询 API 方法签名、接口定义 → `api/`（干净源码）；参考整体实现、设计模式 → `references/`
- 需要理解 Create、Ponder、Flywheel、Catnip、Registrate 的行为
- 调试与依赖 mod 的兼容性问题、追踪跨 mod 调用链或 mixin 目标
- 了解某个 mod 的网络包、事件、注册机制

不要为了了解项目概况或做常规源码修改而读取参考源码。只有当前代码的 import、编译错误或明确问题指向外部 API / 参考实现时才使用本 skill。

## 源码目录（全在工作区之外，用绝对路径访问）

| 目录 | 用途 |
|------|------|
| `D:\aMyMCMods\api\` | 依赖 mod 的 **API / 接口源码**（`-sources` 目录，可直接 `read`/`grep`）；查签名首选 |
| `D:\aMyMCMods\references\` | 案例项目完整源码（看整体架构、实现细节、设计模式） |
| `.\\.research\`（项目根） | 个人调研源码：Minecraft 本体 `mc-src/`、反编译 jar、临时下载；新调研的东西一律放这里 |
| `libs\`（项目根） | 本地 maven 依赖仓库（jar 本体，不是源码） |

### api/ 现有内容（本 mod 的依赖生态）

| 目录 | 内容 |
|------|------|
| `D:\aMyMCMods\api\create-1.21.1-6.0.10-280-sources\` | ⭐ Create 6.0.10 完整源码（`com.simibubi.create.*`，**不含 catnip**） |
| `D:\aMyMCMods\api\ponder-neoforge-1.0.82+mc1.21.1-sources\` | ⭐ Ponder（`net.createmod.ponder.*`）+ **内嵌 catnip（编译匹配版）**：`net/createmod/catnip/` |
| `D:\aMyMCMods\api\flywheel-neoforge-1.21.1-1.0.6-sources\` | ⭐ Flywheel（`dev.engine_room.flywheel.*`：api/ lib/ backend/ impl/） |
| `D:\aMyMCMods\api\Registrate-MC1.21-1.3.0+67-sources\` | ⭐ Registrate 1.3.0（`com.tterrag.registrate.{builders,providers,util}`；非独立 mod，只作编译期库 + 被 Create jarJar） |

> **JEI（`mezz.jei`）不在 api/ 中**（本 mod 未依赖）。需要时用 `web_search`，或从 maven 拉 sources 放到 `D:\aMyMCMods\api\`。

> **Catnip 只有一份**：ponder jar 内嵌的 shaded 副本，包结构是顶层 `net.createmod.catnip.{outliner,math,render,theme,data,gui,animation}`，与本项目编译用的 classpath 完全一致。**不要去引用独立 Catnip 的 `net.createmod.catnip.utility.*` 布局**（那是 0.8.x 独立版，类名同、包路径不同）。

### references/ 案例项目

`references/` 目前为空（新项目，尚未导入案例）。需要案例时，把目标 mod 仓库克隆/反编译到 `D:\aMyMCMods\references\<mod>-<branch-or-version>\`，并在下面维护索引：

| 目录 | 是什么 | 可参考点 |
|------|--------|----------|
| （待补充） | | |

挑选案例的优先级：
1. Create 本体（已在 `api/` 有完整源码，`references/` 不必重复放）
2. 其它 Create 附属 mod 中实现同类功能的（扳手/工具交互、方块旋转、Ponder 场景、GUI）
3. 报错或 API 疑点直接指向谁，就只看谁

## 查找步骤

### 1. 确认目标

根据用户问题或当前代码中的 import 语句确定要看哪份源码。按意图分档：

- **查 API / 接口**（方法签名、接口定义、返回类型）→ `D:\aMyMCMods\api\` 对应目录
- **参考案例实现**（某功能整体怎么做、设计模式）→ `D:\aMyMCMods\references\` 对应项目
- **查 Minecraft 本体 / 旧版本 Create** → `.\\.research\`

常见 import 前缀与源码位置：

| Import 前缀 | API 查询（`D:\aMyMCMods\api\`） | 案例参考（`D:\aMyMCMods\references\`） |
|-------------|--------------------------|---------------------------|
| `com.simibubi.create` | `create-1.21.1-6.0.10-280-sources\` | 同上完整源码已含实现，无需另找 |
| `net.createmod.ponder` | `ponder-neoforge-1.0.82+mc1.21.1-sources\` | — |
| `net.createmod.catnip` | `ponder-neoforge-1.0.82+mc1.21.1-sources\net\createmod\catnip\` | — |
| `dev.engine_room.flywheel` | `flywheel-neoforge-1.21.1-1.0.6-sources\`（`api.*` 接口 + `lib.*` 现成实现） | Create 源码内的 Visual 实现 |
| `com.tterrag.registrate` | `Registrate-MC1.21-1.3.0+67-sources\`（`builders/` 链式注册、`providers/` datagen） | — |
| `net.minecraft.*` | — | `.research\mc-src\`（需要时自建） |

### 2. 在对应目录中限定搜索

用 `grep` / `glob` 并把 `path` 指向绝对目录，**不要扫描整个工作区**（`api\` 有 30MB 源码，全量扫会拖慢）：

```
grep: path = "D:\aMyMCMods\api\create-1.21.1-6.0.10-280-sources", pattern = "class WrenchItem"
grep: path = "D:\aMyMCMods\api\ponder-neoforge-1.0.82+mc1.21.1-sources\net\createmod\catnip", pattern = "class SuperByteBuffer"
grep: path = "D:\aMyMCMods\api\flywheel-neoforge-1.21.1-1.0.6-sources", pattern = "interface Visual"
```

### 3. 阅读源码文件

找到目标文件后用 `read` 先读局部范围（目标方法/调用点附近）；只有局部无法确定 API 合约时才扩大读取范围。标准包结构即目录结构。

### 4. 必要时使用网络

源码不在本地时用 `web_search`。常见仓库：

- Create: `Creators-of-Create/Create`（分支 `mc1.21.1/dev`）
- Ponder: `Creators-of-Create/Ponder`
- Flywheel: `Engine-Room/Flywheel`（分支 `1.21.1/dev`）

## 注意事项

- `D:\aMyMCMods\api`、`D:\aMyMCMods\references`、`libs\`、`build\`、`run\` 均为**只读**参考，不要修改；改源码请直接改 `src\main\java`。
- 引用到的结论要记录来源（哪个目录哪个文件），按 `AGENTS.md` 要求写进回答或 `memo/`。
- 依赖版本与 maven 坐标的对应关系见 `gradle.properties`；`api/` 里的源码版本必须与**编译实际使用的版本**一致（Create 6.0.10 / Ponder 1.0.82 / Flywheel 1.0.6 / Registrate `MC1.21-1.3.0+67`），不要拿别的版本源码下结论。
- Registrate 有源码就直接读 `api/Registrate-MC1.21-1.3.0+67-sources`，**不要**再去反编译 `libs/` 里的 jar，也不要以为它是独立 mod。
- 查不到的东西不要猜：先 `web_search`，再向用户确认，禁止凭印象写 API 调用。
