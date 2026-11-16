# mywrench 代码地图（code-map）

> 定位/修改 Java 源码时的**首要入口**。当前项目处于模板骨架阶段，文件极少，本文件随功能落地同步更新。
> 项目事实见 `AGENTS.md`；外部依赖 API 位置见 `memo/api-code-map.md`。

## 1. 现状总览

- mod id：`mywrench`；Java 根包：`src/main/java/com/zzy205/mywrench`
- 资源命名空间：`src/main/resources/assets/mywrench/`（当前只有 `lang/en_us.json`）
- 元数据模板：`src/main/templates/META-INF/neoforge.mods.toml`（由 `generateModMetadata` 展开到 `build/generated/sources/modMetadata`）
- 依赖：Create 6.0.10 / Ponder 1.0.82 / Flywheel 1.0.6（硬依赖）+ Registrate 1.3.0（仅编译期库）

## 2. 按修改目标定位（需求 → 先看哪个文件）

| 修改目标 | 首先查看 | 通常还要检查 |
|---|---|---|
| 新增方块 / 方块物品 | `MyWrench.java`（`BLOCKS` / `ITEMS` DeferredRegister） | `assets/mywrench/lang/en_us.json`、`assets/mywrench/models`、`blockstates` |
| 新增普通物品 | `MyWrench.java`（`ITEMS`） | `lang/en_us.json`、`models/item` |
| 改用 Registrate 注册（可选路线） | `MyWrench.java`（构造函数内 `Registrate.create(MODID)`） | `memo/api-code-map.md` §3 Registrate、`D:\aMyMCMods\api\Registrate-MC1.21-1.3.0+67-sources` |
| 创造模式标签页内容 | `MyWrench.java`（`CREATIVE_MODE_TABS`、`addCreative`） | `lang/en_us.json` 中 `itemGroup.mywrench` |
| 配置项 | `Config.java`（`ModConfigSpec.BUILDER`） | `lang/en_us.json` 中的配置翻译、`MyWrenchClient.java`（配置界面扩展点） |
| 启动期初始化逻辑 | `MyWrench.java` 构造器 / `commonSetup` | — |
| 客户端专属初始化、客户端事件 | `MyWrenchClient.java` | `MyWrench.java`（共享逻辑放哪边） |
| 依赖版本升级 | `gradle.properties` → `build.gradle` → `neoforge.mods.toml` | 见 `AGENTS.md`「依赖变更规则」 |
| 本地依赖 jar 缺失 | `libs/<group>/<artifact>/<version>/` | `build.gradle` 的 `repositories`（LocalLibs 与在线回退） |

> 添加 Create 系功能（BlockEntity、Flywheel Visual、Ponder 场景、Registrate builder 链）时，先按 `minecraft-research` 技能在共享 `api/` 里确认 API，再落地代码，并回来更新本表的行。

## 3. 模块职责表

### 启动与注册

| 文件 | 职责 | 修改场景 |
|---|---|---|
| `MyWrench.java` | `@Mod(MyWrench.MODID)` 主入口；持有 `LOGGER`、`BLOCKS` / `ITEMS` / `CREATIVE_MODE_TABS` 三个 DeferredRegister；构造器里注册到 mod 事件总线、注册 `Config.SPEC`、挂 `BuildCreativeModeTabContentsEvent`；示例方块/物品/标签页在此声明 | 任何「新增注册对象」「改启动顺序」「改 config 注册类型」都要动它 |
| `MyWrenchClient.java` | `@Mod(value = MyWrench.MODID, dist = Dist.CLIENT)` + `@EventBusSubscriber(Dist.CLIENT)`；注册 `IConfigScreenFactory`，示例客户端 setup 日志 | 客户端初始化、屏幕/渲染器注册、客户端事件 |

### 配置

| 文件 | 职责 | 修改场景 |
|---|---|---|
| `Config.java` | `ModConfigSpec` 示例：布尔 / 整数 / 字符串 / 字符串列表四种写法 + 校验器 | 加配置项、改默认值、加校验 |
| `lang/en_us.json` | `itemGroup.mywrench` 与配置项翻译 | 新增可翻译文本 |

### 资源

| 路径 | 职责 |
|---|---|
| `src/main/resources/assets/mywrench/lang/en_us.json` | 语言文件（当前仅标签页键） |
| `src/main/templates/META-INF/neoforge.mods.toml` | 模组元数据模板，依赖声明写在这里（占位符由 gradle 展开） |

## 4. 当前已知边界（改动前核对）

- `neoforge.mods.toml` **不要**声明 `registrate` 依赖：Registrate 不是独立 mod（jar 内无 `neoforge.mods.toml`），运行时由 Create 的 jarJar 提供，声明了会启动失败。
- toml 里的 `versionRange` 用 **mod 自身版本**（`create` = `6.0.10`，`ponder` = `1.0.82+mc1.21.1`，`flywheel` = `1.0.6`），不是 maven 坐标版本（`create` = `6.0.10-280`）。
- 编译期用 Create **slim** jar；开发环境运行时用 Create **全量** jar（自带 jarJar）。两者都放在 `libs/com/simibubi/create/create-1.21.1/6.0.10-280/`，靠 classifier 区分。
- Create 生态依赖一律 `transitive = false`，否则 Gradle 会去解析 FTB / JourneyMap / Curios / sodium 等可选集成。
- `src/generated/resources` 由 datagen 生成，不要手改。
- `MyWrenchClient` 里的客户端代码只能由内置了客户端的环境加载；服务端会因 `Dest.CLIENT` 而不实例化该类，但**不要**把客户端类从 `MyWrench`（common）里直接引用。

## 5. 维护约定

- 新增/删除 Java 文件、重命名类、改变模块边界时，同步更新本文件第 2、3 节的表。
- 踩过的坑写进 `memo/ATTENTION.md`（第一次遇到时创建），不要全堆在这里。
