# My Wrench

Minecraft **1.21.1** + NeoForge **21.1.266** 的 Create 附属 mod（`mod id: mywrench`）。

## 环境要求

- JDK **21**（Gradle toolchain 已锁定 21）
- Gradle Wrapper 9.2.1（无需手装）
- 依赖由 `net.neoforged.moddev 2.0.148` 提供

## 依赖

| mod | 版本 | 角色 |
|---|---|---|
| Create | 6.0.10（maven `6.0.10-280`） | 硬依赖 |
| Ponder | 1.0.82（`1.0.82+mc1.21.1`） | 硬依赖（Create API 引用其类型） |
| Flywheel | 1.0.6 | 硬依赖（Create 渲染引擎） |
| Registrate | `MC1.21-1.3.0+67` | 仅编译期库（不是独立 mod，运行时由 Create jarJar 提供） |

依赖解析顺序：本地 `libs/`（maven 目录布局，见 `libs/README.md`）→ `maven.createmod.net` / `maven.ithundxr.dev`。
版本号统一在 `gradle.properties`；依赖声明在 `build.gradle` 与 `src/main/templates/META-INF/neoforge.mods.toml`（改依赖时三处要同步，规则见 `AGENTS.md`）。

## 常用命令

```bat
gradlew.bat classes          :: 编译校验
gradlew.bat runClient        :: 启动开发客户端
gradlew.bat runServer        :: 启动开发服务端
gradlew.bat runData          :: 数据生成（输出 src/generated/resources）
gradlew.bat build            :: 打包
```

> 开发环境运行时用的是 **Create 全量 jar**（`libs/com/simibubi/create/create-1.21.1/6.0.10-280/create-1.21.1-6.0.10-280.jar`，自带 jarJar 的 flywheel/ponder/Registrate），所以 `runClient` 不需要额外把 jar 丢进 `run/mods`。

## 项目结构

```
src/main/java/com/zzy205/mywrench/      源码（MyWrench 主类 / Config / MyWrenchClient）
src/main/resources/assets/mywrench/     资源（lang、models、blockstates、textures）
src/main/templates/META-INF/            模组元数据模板（gradle 展开占位符）
libs/                                   本地 maven 依赖仓库（不进版本库）
memo/                                   项目备忘：代码地图、踩坑、进度
.research/                              临时调研源码（不进版本库）
```

外部依赖源码（只读，跨项目共享）：`D:\aMyMCMods\api`（API 源码）、`D:\aMyMCMods\references`（案例 mod 源码）。

## 文档

- `AGENTS.md` —— Agent 工作规则、依赖变更规则、目录约定
- `memo/code-map.md` —— 定位/修改 Java 源码的首要入口
- `memo/api-code-map.md` —— 查 Create / Ponder / Flywheel / Catnip API 的首要入口
- `.agents/skills/` —— 任务型技能（minecraft-research、memo-lookup、neoforge-create-rotation、neoforge-model-rendering、create-style-gui）
