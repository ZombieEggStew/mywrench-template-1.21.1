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
| 扳手攻击伤害 / 攻击速度 | `wrench/WrenchCombatHandler.java`（`ItemAttributeModifierEvent`） | `Config.java` 的 `enableWrenchCombat` 等项 |
| 扳手挖掘速度 / 掉落 | `wrench/WrenchMiningHandler.java`（`PlayerEvent.BreakSpeed` / `HarvestCheck`） | `Config.java` 的 `enableWrenchMining` 等项 |
| 依赖版本升级 | `gradle.properties` → `build.gradle` → `neoforge.mods.toml` | 见 `AGENTS.md`「依赖变更规则」 |
| 本地依赖 jar 缺失 | `libs/<group>/<artifact>/<version>/` | `build.gradle` 的 `repositories`（LocalLibs 与在线回退） |

> 添加 Create 系功能（BlockEntity、Flywheel Visual、Ponder 场景、Registrate builder 链）时，先按 `minecraft-research` 技能在共享 `api/` 里确认 API，再落地代码，并回来更新本表的行。

## 3. 模块职责表

### 启动与注册

| 文件 | 职责 | 修改场景 |
|---|---|---|
| `MyWrench.java` | `@Mod(MyWrench.MODID)` 主入口；持有 `LOGGER`、`BLOCKS` / `ITEMS` / `CREATIVE_MODE_TABS` 三个 DeferredRegister；构造器里注册到 mod 事件总线（含 `MyWrenchDataComponents.DATA_COMPONENT_TYPES`）、注册 `Config.SPEC`、挂 `BuildCreativeModeTabContentsEvent`；保留 `example_block`（示例方块），`example_item` 已移除；`WRENCH_UPGRADE_SMITHING_TEMPLATE` 模板物品与两个 tooltip 颜色常量（`TEMPLATE_TITLE_COLOR` = `#a8a8a8`、`TEMPLATE_DESCRIPTION_COLOR` = `#5454fc`）在此声明；`EXAMPLE_TAB` 只放模板 | 任何「新增注册对象」「改启动顺序」「改 config 注册类型」都要动它 |
| `MyWrenchDataComponents.java` | 注册数据组件 `mywrench:wrench_upgraded`（`DataComponentType<Boolean>`，`Codec.BOOL` + `ByteBufCodecs.BOOL`）；锻造机的配方靠它给 `create:wrench` 打「已加固」标记 | 新增组件、改组件类型/同步方式 |
| `MyWrenchClient.java` | `@Mod(value = MyWrench.MODID, dist = Dist.CLIENT)` + `@EventBusSubscriber(Dist.CLIENT)`；注册 `IConfigScreenFactory`，示例客户端 setup 日志 | 客户端初始化、屏幕/渲染器注册、客户端事件 |

### 配置

| 文件 | 职责 | 修改场景 |
|---|---|---|
| `Config.java` | `ModConfigSpec` 示例：布尔 / 整数 / 字符串 / 字符串列表四种写法 + 校验器；另有扳手强化配置（`enableWrenchCombat` / `wrenchAttackDamage` / `wrenchAttackSpeed` / `enableWrenchMining` / `wrenchMiningSpeedMultiplier` / `enableWrenchMiningDrops`），`SPEC` 已改为 `public` | 加配置项、改默认值、加校验 |
| `lang/en_us.json` | `itemGroup.mywrench` 与配置项翻译 | 新增可翻译文本 |
| `lang/zh_cn.json` | 与 en_us 同键的中文翻译 | 同上（两个语言文件同步加键） |

### 扳手强化（关键设计）

| 文件 | 职责 | 修改场景 |
|---|---|---|
| `wrench/WrenchCombatHandler.java` | `@EventBusSubscriber(modid = MyWrench.MODID)`；监听 `ItemAttributeModifierEvent`，对 `WrenchUpgrades.isUpgradedWrench(stack)` 成立的主手加修饰符：id 用 `Item.BASE_ATTACK_DAMAGE_ID` / `Item.BASE_ATTACK_SPEED_ID`（只有 id 等于 `Attribute#getBaseId()` 才会按「基础值+加成」合并显示），量 = `wrenchAttackDamage - 1.0`、`wrenchAttackSpeed - 4.0`（默认 8 伤害 / 1.6 攻速，tooltip 显示 `8 [1 + 7]`） | 调整武器属性、加击退/横扫、换触发条件 |
| `wrench/WrenchMiningHandler.java` | 同一事件总线；`PlayerEvent.HarvestCheck` 里 `setCanHarvest(true)`（需 `enableWrenchMiningDrops`）；`PlayerEvent.BreakSpeed` 里按「(配置倍率 + `Attributes.MINING_EFFICIENCY`) × originalSpeed」重算——原版 `Player#getDigSpeed` 只在基础速度 `> 1.0F` 时才加效率属性，扳手无 `TOOL` 组件（基础 1.0）永远进不去该分支，所以必须在这里补 | 挖掘速度、掉落白/黑名单、按方块标签限速、效率/急迫等附魔与药水协同 |
| `wrench/WrenchUpgrades.java` | 升级状态判定入口：`create:wrench` 身份 + `mywrench:wrench_upgraded` 组件；`enableUpgradeGate = false` 时退化成「任何扳手都算」 | 改门槛逻辑、加更多升级状态 |
| `wrench/WrenchTooltipHandler.java` | `ItemTooltipEvent`（在 `ItemStack#getTooltipLines` 末尾触发）给已加固扳手追加一行 `item.mywrench.wrench_upgraded.desc` | 改说明文案、加耐久/模式显示 |

> **锻造门槛**：`data/mywrench/recipe/wrench_upgrade.json` 是 `smithing_transform`，`base` 为 `create:wrench`、`template` 为本模组模板、`addition` 为下界合金锭，`result` 仍是 `create:wrench` 但带 `"components": {"mywrench:wrench_upgraded": true}`（`SmithingTransformRecipe` 用 `ItemStack.STRICT_CODEC` 解析结果，并用 `transmuteCopy` + `applyComponents` 组装，所以同一个物品可以被「贴组件」升级）。模板自身的获取配方是 `wrench_upgrade_smithing_template.json`（占位：4 钻石 + 1 下界岩，用户之后自己改）。

> 事件方案**不新建物品**：Create 里约 30 处用 `AllItems.WRENCH.isIn(stack)`（严格 `getItem() ==` 身份）判断，只有约 8 处走 `#c:tools/wrench` 标签。换新物品会让皮带切割、钢梁、部署器、半径扳手菜单等大量交互失效，所以强化对象必须是原 `create:wrench`。

### 资源

| 路径 | 职责 |
|---|---|
| `src/main/resources/assets/mywrench/lang/en_us.json` | 语言文件：标签页键、配置项、锻造模板 5 个描述键、已加固说明行 |
| `src/main/resources/assets/mywrench/lang/zh_cn.json` | 同键中文翻译（两个文件要同步加键） |
| `assets/mywrench/textures/item/wrench_upgrade_smithing_template.png` | 模板贴图（16×16，当前是原版下界合金模板底图的复制，用于改色占位） |
| `assets/mywrench/models/item/wrench_upgrade_smithing_template.json` | 模板物品模型（`item/generated`，和原版模板结构一致） |
| `data/mywrench/recipe/wrench_upgrade.json` | 锻造配方：模板 + `create:wrench` + 下界合金锭 → 带 `mywrench:wrench_upgraded` 组件的 `create:wrench`；结果同时写 `minecraft:max_damage: 4096` + `minecraft:unbreakable: {}`——因为 `Item#isEnchantable` 要求 `getMaxStackSize()==1 && has(MAX_DAMAGE)`，没有 `MAX_DAMAGE` 的扳手在附魔台上会被直接跳过；`unbreakable` 保证它仍然永不磨损 |
| `data/mywrench/recipe/wrench_upgrade_smithing_template.json` | 模板获取配方（占位，待用户改材料） |
| `data/minecraft/tags/item/swords.json` | 把 `create:wrench` 追加进原版 `#minecraft:swords` 物品标签（1.21 起标签目录是单数 `tags/item`），让 Punchy 之类的动画 mod 把扳手识别成剑；副作用：扳手可被附剑类附魔、动画类 mod 会用剑的挖掘动作 |
| `data/minecraft/tags/item/enchantable/mining.json` | 追加 `create:wrench` 进 `#minecraft:enchantable/mining`，效率附魔（`efficiency` 的 `supported_items`）才能出现在附魔台上 |
| `data/minecraft/tags/item/enchantable/mining_loot.json` | 追加 `create:wrench` 进 `#minecraft:enchantable/mining_loot`，时运 / 精准采集（`fortune` / `silk_touch` 的 `supported_items`）才能附上；注意真正生效还要求栈上有 `MAX_DAMAGE`（见配方那一行） |
| `assets/minecraft/punchy/compat/combat/wrench_compat.json` | Punchy 兼容数据：`itemSpecific.create:wrench.kind = "SWORD"`，直接指定 Punchy 的物品 kind（Punchy 会合并资源包/模组里的 `punchy/compat/**`） |
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
