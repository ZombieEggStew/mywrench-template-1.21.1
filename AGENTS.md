# Agent 工作规则

## 工作原则(最高优先级,必须遵守)

- **实测优先**:证据不足时不要埋头推理,先写最小调试代码,请用户进游戏做针对性测试并反馈结果,再定位问题。
- **少思考多提问**:每轮先问「这步真的必要吗」;缺关键信息或能直接问用户时,立即用 `ask_user_question` 提问(复现步骤、具体表现、期望行为),禁止在证据不足时长时间自主推理或空转。

## 项目事实（动手前先确认这几条）

- Minecraft **1.21.1** + NeoForge **21.1.266**，Java **21**，Gradle **9.2.1**，插件 `net.neoforged.moddev 2.0.148`。
- mod id `mywrench`，显示名 `My Wrench`，Java 包根 `com.zzy205.mywrench`，资源命名空间 `assets/mywrench/`。
- 本项目是 **Create 附属 mod**。硬依赖（`src/main/templates/META-INF/neoforge.mods.toml` 已声明 `type="required"`）：
  - `create` 6.0.10 —— 主依赖
  - `ponder` 1.0.82 —— Create 的教程动画系统，Create 的 API 表面直接引用其类型
  - `flywheel` 1.0.6 —— Create 的实例化渲染引擎
  - `Registrate` 1.3.0 —— **不是独立 mod**（jar 内没有 `neoforge.mods.toml`），只做编译期库，运行时由 Create 的 jarJar 内嵌。不要在 `neoforge.mods.toml` 里声明 `registrate` 依赖。
- 编译校验命令：`./gradlew.bat classes`（存在更窄的相关检查时优先用它）。
- 目录约定（只读参考 vs 可改）：

| 路径 | 说明 | 可写 |
|---|---|---|
| `src/main/java`、`src/main/resources`、`src/main/templates` | 本项目源码/资源/元数据模板 | ✅ |
| `libs/` | 本地 maven 布局依赖仓库（`libs/<group>/<artifact>/<version>/*.jar+.pom`） | 只加 jar，不改内部 |
| `memo/` | 项目备忘：代码地图、踩坑、进度 | ✅ |
| `.research/` | 临时调研源码（反编译产物等） | ✅ |
| `D:\aMyMCMods\api` | **共享**依赖 API 源码（只读） | ❌ |
| `D:\aMyMCMods\references` | **共享**案例 mod 完整源码（只读） | ❌ |
| `build/`、`bin/`、`run/`、`.gradle/` | 构建与运行产物 | ❌ |

## Java 源码规则（仅修改 `src/main/java/**/*.java` 时适用）

- 只改当前请求相关的代码。`build/`、`bin/`、`run/`、`libs/`、`.research/` 以及共享的 `api/`、`references/` 视为只读参考。
- 优先复用项目已有模式，不引入新抽象。
- 先读直接相关的类、最近的调用方或测试；定位文件可先查 `memo/code-map.md`。需要确认 Create / Ponder / Flywheel / Registrate 或 Minecraft 的 API 时，使用 `minecraft-research` 技能。
- Create 系 API 的 import 前缀：`com.simibubi.create.*` / `net.createmod.ponder.*` / `net.createmod.catnip.*`（Catnip 用 ponder 内嵌副本）/ `dev.engine_room.flywheel.*` / `com.tterrag.registrate.*`。
- 用 `./gradlew.bat classes` 验证 Java 改动。

## 资源与数据规则（仅修改 `src/main/resources/**/*.{json,lang,mcmeta,png}` 时适用）

- 只改当前请求直接需要的资源或数据。
- 保持命名空间 `mywrench`、JSON 布局与模型约定；新增内容优先参考 Create 自己的资源组织方式。
- `src/main/resources` 是可读写的；`src/generated/resources` 由 datagen 生成，不要手改。
- 将 `build/`、`bin/`、`run/`、`libs/`、`.research/`、`api/`、`references/` 视为只读参考目录。

## 依赖变更规则（改 `gradle.properties` / `build.gradle` / `neoforge.mods.toml` 时）

三处必须同步，缺一处会出现「编译过但进游戏崩」或「进游戏找不到依赖」：

1. `gradle.properties`：加 `<name>_version = <maven 版本>`，并在需要写进 toml 时加 `<name>_version_range=[<mod 版本>,)`。
   - ⚠️ **maven 版本 ≠ mod 版本**：create 的 maven 坐标是 `6.0.10-280`，但 jar 内 `modId="create"` 的 `version` 是 `6.0.10`；ponder 是 `1.0.82` → `1.0.82+mc1.21.1`。写 `versionRange` 一律用 **jar 内 mod 版本**（可以用 `neoforge.mods.toml` 或 `gradlew dependencies` 核对）。
2. `build.gradle`：`dependencies` 里加 `compileOnly`（API/编译期）与 `localRuntime`（仅开发环境运行期）。Create 生态的 jar 一律 `transitive = false`，否则会去解析它 pom 里的可选集成（FTB/JourneyMap/Curios/sodium…）。
3. `src/main/templates/META-INF/neoforge.mods.toml`：加 `[[dependencies.${mod_id}]]`，`ordering="AFTER"`，`side="BOTH"`。**若该 jar 不是独立 mod（无 `neoforge.mods.toml`，如 Registrate）则不能声明**。

依赖解析顺序：`libs/`（本地 maven 仓库）→ `maven.createmod.net` / `maven.ithundxr.dev`。往 `libs/` 放 jar 时按 `libs/<group 路径>/<artifact>/<version>/<artifact>-<version>.jar` 命名，并同时放 `.pom`（没有 pom 时 Gradle 会退化为无依赖元数据，通常也能用，但建议补上）。

- Create **slim** jar = 不含 jarJar 的 flywheel/ponder/Registrate，用于 `compileOnly`；
- Create **全量** jar = 带 `META-INF/jarjar/`，用于 `localRuntime`，所以开发环境跑起来只需要它一个。
