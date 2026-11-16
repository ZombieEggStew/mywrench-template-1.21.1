# My Wrench 项目总览（功能 / 设计 / 数据）

> 本文记录**功能设计、数据约定、配置与踩坑**。定位某个 Java 文件该改哪里请看 `memo/code-map.md`；查 Create / Ponder / Flywheel / Registrate API 位置请看 `memo/api-code-map.md`；项目硬事实（版本、目录约定）见 `AGENTS.md`。

## 1. 这是什么

Create 附属 mod：**不新增物品**，通过 NeoForge 事件给原版 `create:wrench` 增加「武器化」和「工具化」两条锻造升级路径。

- mod id：`mywrench`，显示名 `My Wrench`
- 包根：`com.zzy205.mywrench`，资源命名空间 `assets/mywrench/`
- 环境：Minecraft 1.21.1 + NeoForge 21.1.266 + Java 21 + Gradle 9.2.1（`net.neoforged.moddev 2.0.148`）
- 硬依赖：`create` 6.0.10、`ponder` 1.0.82、`flywheel` 1.0.6；`Registrate` 1.3.0 只做编译期库（**不要**写进 `neoforge.mods.toml`）

## 2. 为什么强化原版扳手而不是新物品（核心约束）

Create 里约 **30 处**用 `AllItems.WRENCH.isIn(stack)` 判断扳手，这是 Registrate `ItemProviderEntry.isIn` 的**严格身份判断**（`stack.getItem() == create:wrench`），只有约 8 处走 `#c:tools/wrench` 标签。

→ 换成新物品会让皮带切割、钢梁、部署器、转向架、半径扳手菜单、蓝图、底盘范围提示等大量交互**静默失效**。

→ 所以强化对象必须是原 `create:wrench`，用**数据组件**打标记，用**事件**改行为。

## 3. 两条锻造升级路径

| 路径 | 模板物品（创造页 Ingredients） | 贴图 | 组件标记 | 效果 |
|---|---|---|---|---|
| 攻击升级 | `mywrench:wrench_attack_upgrade_smithing_template` | `wrench_upgrade_smithing_template_1.png` | `mywrench:wrench_attack_upgrade` | 高额攻击力 + 剑级攻速 |
| 工具升级 | `mywrench:wrench_tool_upgrade_smithing_template` | `wrench_upgrade_smithing_template_2.png` | `mywrench:wrench_tool_upgrade` | 钻石级破坏速度（**所有方块**）+ 全方块掉落 |

锻造配方（`data/mywrench/recipe/`）：`模板 + create:wrench + minecraft:netherite_ingot` → 仍是 `create:wrench`，只额外带上自己那条组件 + `max_damage` + `unbreakable`。

- 两个组件**可以叠加**：`SmithingTransformRecipe#assemble` 是 `transmuteCopy` + `applyComponents`，会保留 base 已有组件，所以先打攻击模板再打工具模板 → 两种能力都在。
- 升级判定在 `wrench/WrenchUpgrades.java`：`hasAttackUpgrade(...)` / `hasToolUpgrade(...)`；`enableUpgradeGate = false`（测试用）时任何扳手都算已升级。

### 模板 tooltip

- 升级标题（`Attack Upgrade` / `Tool Upgrade`）= 灰 `#a8a8a8`
- `Create Wrench`（applies_to）/ `Netherite Ingot`（ingredients）= 蓝紫 `#5454fc`
- 颜色写在 `MyWrench.java` 的 `TEMPLATE_TITLE_COLOR` / `TEMPLATE_DESCRIPTION_COLOR`（lang 不支持给单条文字上色，必须 `withStyle(style -> style.withColor(...))`）
- 空槽图标：攻击模板用原版 `item/empty_slot_sword`，工具模板用 `item/empty_slot_pickaxe`，材料槽用 `item/empty_slot_ingot`

## 4. 当前数值（都可配置）

| 配置键（`run/config/mywrench-common.toml`） | 默认 | 含义 |
|---|---|---|
| `enableWrenchCombat` | true | 攻击升级总开关 |
| `wrenchAttackDamage` | 8.0 | **总攻击伤害**（修饰符量 = 该值 − 1.0，即 +7；tooltip 显示 `8 [1 + 7]`） |
| `wrenchAttackSpeed` | 1.6 | **最终攻速**（修饰符量 = 该值 − 4.0，即 −2.4） |
| `enableWrenchTool` | true | 工具升级总开关 |
| `wrenchToolSpeedMultiplier` | 8.0 | 破坏速度倍率（1.0 基础 × 8 ≈ 钻石工具） |
| `enableWrenchToolDrops` | true | 让「需要正确工具」的方块也掉落 |
| `enableUpgradeGate` | true | 必须锻造升级后才有加成；设 false 只用于测试 |

> 8 伤害 / 1.6 攻速 = 下界合金剑同等手感；工具模式 ×8 = 钻石工具速度。

## 5. 实现要点（事件注入点 + 原版门槛）

| 能力 | 注入点 | 关键所在 |
|---|---|---|
| 攻击伤害/攻速 | `ItemAttributeModifierEvent` | 每次 `ItemStack#getAttributeModifiers()` 都会触发（NeoForge `IItemStackExtension`），所以不用换物品；修饰符 id 必须用 `Item.BASE_ATTACK_DAMAGE_ID` / `BASE_ATTACK_SPEED_ID`，只有 id 等于 `Attribute#getBaseId()` 才会按「基础值+加成」合并显示 |
| 破坏速度 | `PlayerEvent.BreakSpeed` | `Player#getDigSpeed` 算完药水/水下/悬空倍率后触发；扳手无 `TOOL` 组件 → 基础速度 1.0，×8 后≈钻石工具，且保留原版惩罚 |
| 方块掉落 | `PlayerEvent.HarvestCheck` | `ServerPlayerGameMode#destroyBlock` → `BlockState#canHarvestBlock` → NeoForge `IBlockExtension` → 触发本事件；`setCanHarvest(true)` 即可让石头/矿石等掉落 |
| 附魔台可用性 | —— | `Item#isEnchantable` 要求 `getMaxStackSize()==1 && has(DataComponents.MAX_DAMAGE)`，所以锻造结果必须写 `minecraft:max_damage: 4096`；再配 `minecraft:unbreakable: {}` 保证永不磨损、不显示耐久条 |

### 效率附魔为什么要特殊处理

原版 `Player#getDigSpeed`：

```java
float f = this.inventory.getDestroySpeed(state);   // 扳手 = 1.0（没有 TOOL 组件）
if (f > 1.0F) {                                    // ← 1.0 不大于 1.0，进不去
    f += (float)this.getAttributeValue(Attributes.MINING_EFFICIENCY);  // 效率附魔在这里
}
```

效率附魔的实际数据是属性 `minecraft:player.mining_efficiency`（`add_value`，量 = 等级²+1）。扳手基础速度恒为 1.0 → 效率永远加不进去。

→ `WrenchToolHandler#onBreakSpeed` 里按原版语义重算：

```
新速度 = (配置倍率 + 效率属性值) × originalSpeed
```

（`originalSpeed` 已乘完药水/水下/悬空等倍率，所以等价于把扳手当成「破坏速度 = 配置倍率」的工具再吃效率附魔。）效率 V 时 = 8 + 26 = 34，与钻石镐效率 V 一致。

## 6. 物品标签与外部 mod 兼容

| 文件 | 作用 |
|---|---|
| `data/minecraft/tags/item/swords.json` | 追加 `create:wrench` 到 `#minecraft:swords`：① Punchy 等动画 mod 会把扳手当剑播动作；② 派生 `enchantable/sharp_weapon` / `sword` / `fire_aspect` / `durability` → 锋利、横扫之刃、火焰附加、耐久、经验修补都能附 |
| `data/minecraft/tags/item/enchantable/mining.json` | 效率附魔的 `supported_items` |
| `data/minecraft/tags/item/enchantable/mining_loot.json` | 时运 / 精准采集的 `supported_items` |
| `assets/minecraft/punchy/compat/combat/wrench_compat.json` | Punchy 兼容数据：`itemSpecific."create:wrench".kind = "SWORD"` |

- 1.21 起标签目录是**单数** `tags/item`（不是 `tags/items`）。
- Punchy 是闭源 ARR 模组，只走数据层挂接，不反编译、不写 mixin。
- 标签只认物品 id，不认组件 → 未升级的扳手也在这些标签里，加成与否靠 `WrenchUpgrades` 的组件判断区分。

## 7. 文件清单

```
src/main/java/com/zzy205/mywrench/
├─ MyWrench.java                # 主入口：注册 BLOCKS/ITEMS/数据组件、两个模板物品、tooltip 颜色、创造页挂载
├─ MyWrenchDataComponents.java  # mywrench:wrench_attack_upgrade / mywrench:wrench_tool_upgrade
├─ Config.java                  # 所有配置项（SPEC 已 public，供 handler 读取）
├─ MyWrenchClient.java          # 客户端初始化 + 配置界面工厂
└─ wrench/
   ├─ WrenchUpgrades.java        # hasAttackUpgrade / hasToolUpgrade
   ├─ WrenchCombatHandler.java   # ItemAttributeModifierEvent → 攻伤/攻速
   ├─ WrenchToolHandler.java     # BreakSpeed + HarvestCheck（对所有方块）
   └─ WrenchTooltipHandler.java  # ItemTooltipEvent → 两条金色说明行

src/main/resources/
├─ assets/mywrench/lang/{en_us,zh_cn}.json
├─ assets/mywrench/models/item/{wrench_attack,wrench_tool}_upgrade_smithing_template.json
├─ assets/mywrench/textures/item/wrench_upgrade_smithing_template_{1,2}.png
├─ assets/minecraft/punchy/compat/combat/wrench_compat.json
├─ data/minecraft/tags/item/swords.json
├─ data/minecraft/tags/item/enchantable/{mining,mining_loot}.json
└─ data/mywrench/recipe/
   ├─ wrench_attack_upgrade.json / wrench_attack_upgrade_smithing_template.json
   └─ wrench_tool_upgrade.json   / wrench_tool_upgrade_smithing_template.json
```

四个配方：前两个是模板自身的占位合成（攻击 = 4 钻石 + 1 下界岩；工具 = 4 铁锭 + 1 下界岩），后两个是锻造配方。

## 8. 创造模式与残留模板内容

- 自定义标签页（`mywrench:example_tab`）**已删除**；两个模板直接挂到原版 `CreativeModeTabs.INGREDIENTS`
- `example_item` 已删除（Java 注册、创造页条目、lang 键）
- `example_block` / `EXAMPLE_BLOCK_ITEM` **仍保留**（会出现在原版 Building Blocks 页），且没有模型/贴图资源
- `Config.java` 里 `logDirtBlock` / `magicNumber` / `magicNumberIntroduction` / `items` 以及 `commonSetup` 的日志都是 NeoForge 模板遗留，可随时清掉

## 9. 测试方法

```powershell
.\gradlew.bat classes        # 编译校验
.\gradlew.bat processResources
.\gradlew.bat runClient      # 进游戏实测
```

- 快速拿成品（跳过锻造）：
```
/give @s create:wrench[mywrench:wrench_attack_upgrade=true,minecraft:max_damage=4096,minecraft:unbreakable={}]
/give @s create:wrench[mywrench:wrench_tool_upgrade=true,minecraft:max_damage=4096,minecraft:unbreakable={}]
/give @s create:wrench[mywrench:wrench_attack_upgrade=true,mywrench:wrench_tool_upgrade=true,minecraft:max_damage=4096,minecraft:unbreakable={}]
```
- 锻造流程：锻造台 → 模板槽放对应模板、base 放 `create:wrench`、材料槽放下界合金锭
- 回归项：Shift+右键拾取方块、右键旋转/调整、半径扳手菜单、皮带切割等 Create 扳手交互；Punchy 剑动画；附魔台能出效率/时运/精准采集；扳手无耐久条

## 10. 踩坑与已解决的坑

1. **不能换物品**（见 §2）。
2. **`AllItems.WRENCH.isIn` 是严格身份**，不能用标签替代。
3. **加标签不够**：附魔台还看 `Item#isEnchantable` → 必须有 `MAX_DAMAGE` 组件（用锻造结果的 `components` 写进去，配 `unbreakable`）。
4. **效率附魔对非工具物品无效**（`f > 1.0F` 门槛），必须在 `BreakSpeed` 里自己补。
5. **Punchy 分类靠标签 + compat 数据**；写进 `#minecraft:swords` 与 `punchy/compat` 两条都做了。副作用：动画 mod 会把扳手当剑（挖掘动作也变成剑的挥砍），且可附剑类附魔。
6. **锻造结果支持组件**：`SmithingTransformRecipe` 用 `ItemStack.STRICT_CODEC` 解析 `result`，`assemble` 用 `transmuteCopy` + `applyComponents`。
7. **组件改名会让旧物品失效**：`wrench_upgraded`（初版）→ `wrench_attack_upgrade` + `wrench_mining_upgrade` → 现行 `wrench_attack_upgrade` + `wrench_tool_upgrade`。每次改名后老存档里已升级的扳手都会退回普通扳手，需要重新锻造或 `/give`。
8. **配置键改名会让旧配置文件被丢弃**（`enableWrenchMining*` → `enableWrenchTool*`），新键按默认值重建。

## 11. 可选的后续方向

- 两个模板的**获取配方**目前是占位材料，待定正式配方（或做成复制配方 / 掉落 / 交易）
- 是否给「工具升级」也加一点点攻击力（当前是纯工具，攻击力仍为 1）
- 精准采集与时运互斥（原版规则），需要切换时用砂轮洗掉
- 若想让「攻击用剑动作、挖掘保持镐动作」，需要改 Punchy 的 tuning/pack（单 kind 限制）
- 清理模板遗留：`example_block`、`Config` 里的示例键、`commonSetup` 的日志
- 模板贴图目前是用户自绘资源；如要做多语言版本/材质变体可再加工

## 12. 变更记录

| 阶段 | 内容 |
|---|---|
| 1 | 纯事件强化原版扳手（单一 `wrench_upgraded` 组件）：攻伤、挖掘、掉落、效率 |
| 2 | 数值定为 8 伤害 / 1.6 攻速（`minecraft:base_attack_damage` −2.4/ +7 的显示格式） |
| 3 | 加锻造门槛：自制模板物品 + `smithing_transform` 配方 + `max_damage`/`unbreakable` + 升级说明 tooltip |
| 4 | Punchy 剑动画（`#minecraft:swords` + compat json）；附魔台可用（`enchantable/mining` + `mining_loot`）；效率附魔补算 |
| 5 | 拆成两张模板（攻击 / 工具）+ 两个组件；删自定义标签页、模板进 Ingredients 页；删 `example_item`；tooltip 颜色 `#a8a8a8` / `#5454fc` |
| 6 | 「挖掘」全面正名为「工具」（id / 组件 / 配置键 / 文案 / 文件），并清理废弃文件 |
