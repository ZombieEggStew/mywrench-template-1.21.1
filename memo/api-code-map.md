# api/ 依赖源码代码地图（api-code-map）

> 查外部依赖 API 的**首要入口**。源码在仓库之外：`D:\aMyMCMods\api\`（只读）。
> grep/read 时把 path 指到下面这些绝对目录，不要扫整个工作区（Create 源码约 30MB）。

## 0. 版本对齐（必须与编译期一致）

| mod | 编译期 maven 坐标（`gradle.properties`） | jar 内 mod 版本（`neoforge.mods.toml` 依赖用） | api/ 源码目录 |
|---|---|---|---|
| Create | `com.simibubi.create:create-1.21.1:6.0.10-280:slim` | `6.0.10` | `create-1.21.1-6.0.10-280-sources` |
| Ponder | `net.createmod.ponder:ponder-neoforge:1.0.82+mc1.21.1` | `1.0.82+mc1.21.1` | `ponder-neoforge-1.0.82+mc1.21.1-sources` |
| Flywheel | `dev.engine-room.flywheel:flywheel-neoforge-api-1.21.1:1.0.6` | `1.0.6` | `flywheel-neoforge-1.21.1-1.0.6-sources` |
| Registrate | `com.tterrag.registrate:Registrate:MC1.21-1.3.0+67` | —（非独立 mod） | `Registrate-MC1.21-1.3.0+67-sources` |
| Catnip | 无独立坐标（见下） | — | 在 ponder 源码目录内的 `net/createmod/catnip/` |

> ⚠️ **Catnip 只有 ponder 内嵌的 shaded 副本**，包结构是顶层 `net.createmod.catnip.{animation,data,gui,math,outliner,render,theme,...}`。独立发布版的 `net.createmod.catnip.utility.*` 布局与编译期 classpath **不一致**，照它写 import 会编译失败。

## 1. import 前缀 → 路径对照

| import 前缀 | 源码路径（绝对） |
|---|---|
| `com.simibubi.create.api.*` | `D:\aMyMCMods\api\create-1.21.1-6.0.10-280-sources\com\simibubi\create\api\` |
| `com.simibubi.create.foundation.*` | `...\create-1.21.1-6.0.10-280-sources\com\simibubi\create\foundation\` |
| `com.simibubi.create.content.*` | `...\create-1.21.1-6.0.10-280-sources\com\simibubi\create\content\` |
| `com.simibubi.create.infrastructure.*` / `impl.*` | `...\create-1.21.1-6.0.10-280-sources\com\simibubi\create\{infrastructure,impl}\` |
| `com.simibubi.create.All*`（`AllBlocks` / `AllItems` / `AllShapes` …） | `...\create-1.21.1-6.0.10-280-sources\com\simibubi\create\`（顶层） |
| `net.createmod.ponder.*` | `D:\aMyMCMods\api\ponder-neoforge-1.0.82+mc1.21.1-sources\net\createmod\ponder\` |
| `net.createmod.catnip.*` | `...\ponder-neoforge-1.0.82+mc1.21.1-sources\net\createmod\catnip\` |
| `dev.engine_room.flywheel.api.*` | `D:\aMyMCMods\api\flywheel-neoforge-1.21.1-1.0.6-sources\dev\engine_room\flywheel\api\` |
| `dev.engine_room.flywheel.lib.*` | `...\flywheel-neoforge-1.21.1-1.0.6-sources\dev\engine_room\flywheel\lib\` |
| `com.tterrag.registrate.*` | `D:\aMyMCMods\api\Registrate-MC1.21-1.3.0+67-sources\com\tterrag\registrate\` |
| `net.minecraft.*` | **不在 api/**（需要时把 MC 源码放到项目 `.research\mc-src\`） |

## 2. Create 包级地图（`com/simibubi/create/`）

| 包 | 内容 | 常用场景 |
|---|---|---|
| `api/` | 对外 API：`behaviour`、`contraption`、`data`、`equipment`、`event`、`packager`、`registry`、`schematic`、`stress`、`connectivity`、`effect`、`boiler`、`registrate` | 想「被 Create 识别」（方块交互行为、阀门、包装、应力）时先查这里 |
| `foundation/` | 基础设施：`block`、`blockEntity`、`gui`、`item`、`render`、`ponder`、`networking`、`model`、`placement`、`utility`、`events`、`packet`、`mixin` | 写 BlockEntity / GUI / 渲染 / 网络的现成基类（`SmartBlockEntity`、`AbstractSimiContainerScreen`、`AllGuiTextures`…） |
| `content/` | 具体内容：`kinetics`、`logistics`、`processing`、`trains`、`decoration`、`equipment`、`schematics`、`fluids`、`materials`、`contraptions`、`redstone`、`legacy` | 参考同类方块的完整实现（扳手类交互先看 `content/equipment` 与 `content/kinetics`） |
| `infrastructure/` | 启动、配置、事件装配（`Create.java` 之外的 glue） | 理解 Create 如何注册它的全部内容 |
| `impl/` | 内部实现细节 | 只在追踪调用链时看，不要依赖 |
| 顶层 `All*.java` | 全部注册表常量（`AllBlocks`、`AllItems`、`AllBlockEntityTypes`、`AllShapes`、`AllPartialModels`、`AllSoundEvents`、`AllDataComponents`…） | 找 Create 已有的方块/物品/模型/音效常量的第一站 |

`content/` 子包：`contraptions`、`decoration`、`equipment`、`fluids`、`kinetics`、`legacy`、`logistics`、`materials`、`processing`、`redstone`、`schematics`、`trains`。

## 3. Ponder / Catnip / Flywheel 包级地图

### Ponder（`net/createmod/ponder/`）

| 包 | 内容 |
|---|---|
| `api/` | `PonderSceneBuilder`/`PonderScene`/`SceneBuilder`、`PonderRegistrationHelper`、`PonderTag`、`PonderStoryBoard`、`PonderSceneBuilder` 等（写场景入门先看它） |
| `foundation/` | 场景运行时、元素、部分（`PonderScene`、`PonderElement`、`PonderUI` 相关实现） |
| `render/` | 场景专用渲染工具 |
| `enums/` `config/` `command/` | 枚举、配置、`/ponder` 命令 |
| 顶层 | `Ponder.java`、`PonderClient.java`、`NeoForgePonder*.java`（入口） |

### Catnip（`net/createmod/catnip/`）

| 包 | 内容 | 常用类 |
|---|---|---|
| `outliner/` | 3D 线框/高亮 | `Outliner`、`AABBOutline`、`LineOutline`、`ColoredOutline`、`Outliner.Params` |
| `render/` | 批次渲染与缓冲 | `SuperByteBuffer`、`ShadowOnlySuperByteBuffer`、`RenderTypes`、`CachedRenderTypes` |
| `math/` | 数学/形状 | `VecMath`、`AngleHelper`、`VoxelShaper`、`MatrixMath`、`AnimationTickHolder` |
| `gui/` | Create 风格控件 | `AbstractSimiWidget`、`AbstractSimiContainerScreen`、`AllGuiTextures`、`AllIcons` |
| `data/` | 数据结构 | `Couple`（Create API 签名会用到）、`IntAttached`、`DirectionalExtender` |
| `theme/` `layout/` `animation/` `components/` `placement/` `net/` `config/` `client/` | 主题色、布局、缓动动画、UI 组件、放置辅助、网络、配置、客户端工具 | — |

### Flywheel（`dev/engine_room/flywheel/`）

| 包 | 内容 | 常用类 |
|---|---|---|
| `api/visual/` | Visual 与可视化注册 | `Visual`、`VisualManager`、`SimpleVisual`、`AbstractVisual` |
| `api/instance/` | 实例类型 | `InstanceType`、`AbstractInstance`、`Instancer` |
| `api/model/` | 模型 | `DynamicModel`、`BlockModel`、`Model` |
| `api/material/` | 材质 | `MaterialManager`、`InstanceMaterial`、`Material` |
| `api/layout/` | 属性布局 | `InstanceLayout`、`Layout` |
| `api/vertex/` | 顶点格式 | `VertexList`、`VertexConsumer` |
| `api/task/` | 渲染任务 | `VisualizationManager`、`RenderStage`、`TaskExecutor` |
| `api/registry/` | 注册表 | `VisualizerRegistry`、`InstanceTypeRegistry` 等 |
| `lib/` | 现成实现工具（`Simple*` 系列、`InstanceTypes`、`RenderStage`） | 写 Visual 时优先用 `lib/` 的现成实现 |
| `backend/` `impl/` | 后端（GL/instancing）与内部实现 | 只读参考 |

### Registrate（`com/tterrag/registrate/`）

> 1.3.0（`Registrate-MC1.21-1.3.0+67-sources`）。**不是独立 mod**：不写进 `neoforge.mods.toml`，运行时由 Create jarJar 提供。

| 包 / 文件 | 内容 | 常用类 |
|---|---|---|
| `AbstractRegistrate.java` | 注册中枢：`object(name)` 开新 builder、`defaultCreativeTab(...)`、`modifyCreativeModeTab`、`addLang/addRawLang`、`addDataGenerator`、`registerEventListeners(IEventBus)`、`get/getAll/getOptional` | 自定义子类时才需要继承它 |
| `Registrate.java` | 默认实现 + `Registrate.create(modid)` 入口。**`create()` 会自己从 `ModList` 拿本 mod 的 `IEventBus` 并 `registerEventListeners`**，所以要在 **mod 构造器里**调用（那时容器已存在），别再手动注册一遍 | `public static final Registrate REGISTRATE = Registrate.create(MODID);` |
| `builders/` | 链式 builder：`BlockBuilder`、`ItemBuilder`、`BlockEntityBuilder`、`EntityBuilder`、`FluidBuilder`、`MenuBuilder`、`BiomeBuilder`、`AbstractBuilder`（共用 API：`.properties()` `.lang()` `.blockstate()` `.item()` `.recipe()` `.tag()` `.register()` …） | 注册方块/物品优先用它，datagen 会跟着自动生成 |
| `providers/` | datagen provider：`RegistrateDataProvider`（入口）、`RegistrateLangProvider`、`RegistrateBlockstateProvider`、`RegistrateItemModelProvider`、`RegistrateRecipeProvider`、`RegistrateTagsProvider`、`RegistrateAdvancementProvider`、`RegistrateDatapackProvider`、`RegistrateDataMapProvider`、`RegistrateGenericProvider`、`loot/` | 写 datagen 时看这里，**不要手写 lang/json** |
| `util/` | `DataIngredient`、`CreativeModeTabModifier`、`RegistrateDistExecutor`、`Sequence`、`entry/`、`nullness/` | 配合 builder 的杂项 |

写 Registrate 代码的三个已知坑（参考项目踩过）：

1. **datagen 会覆盖手写 lang**：`RegistrateDataProvider` 生成 `en_us.json` 时整文件重写，手写内容会被清掉 → 要么全部走 `.lang()`，要么别让 Registrate 的 lang provider 跑。
2. **builder 自动 `.tab(SEARCH)`** 可能让服务端启动崩溃（创造标签内容依赖客户端类）→ 显式 `defaultCreativeTab(...)` 或去掉自动 tab。
3. **datagen 环境要最小化 mod**：`runData` 时同样会加载注册，任何客户端类被静态引用都会炸。

## 4. 不在 api/ 里的依赖

- **JEI**：`mezz.jei.*`，本 mod 未依赖；需要时再加坐标并把 sources 放到 `D:\aMyMCMods\api\`。
- **Minecraft 本体**：`net.minecraft.*`。需要时把官方/反编译源码解到项目 `.research\mc-src\`。
- Registrate **已提取**在 `api/Registrate-MC1.21-1.3.0+67-sources`（见 §3），不要再反编译 `libs/` 里的 jar。

## 5. 使用建议

- 查签名 → 直接 `grep` 目标包（例如 `pattern = "class WrenchItem|interface Wrenchable"`，`path = "...\create\api\behaviour"`）。
- 查「Create 自己怎么做的」→ `content\` 下找同类方块，比 `impl\` 更可靠。
- 写渲染 → 先在 `catnip\render\` 与 `flywheel\api\` 找现成基类，别自己造轮子；旋转相关先加载 `neoforge-create-rotation` 技能。
- 写 GUI → 先在 `catnip\gui\` 找 `AbstractSimiWidget` / `AllGuiTextures` / `AllIcons`；风格约定见 `create-style-gui` 技能。
- 结论要记来源（目录 + 文件），必要时补进 `memo/`。
