---
name: create-style-gui
description: "Use when implementing or changing this mod's Create-style GUI screens, config screens, textures, icon buttons, toggle buttons, or tooltip-rendered values."
---

# Create-Style GUI

Create 的 GUI 观感来自一套现成积木，别自己手绘像素。先在 `D:\aMyMCMods\api\ponder-neoforge-1.0.82+mc1.21.1-sources\net\createmod\catnip\gui\` 和 Create 的 `foundation\gui\` 里找可复用的东西：

| 需求 | 用什么 |
|---|---|
| 面板/背景/格子贴图 | `catnip` 的 `AllGuiTextures`（Create 侧同名类 `com.simibubi.create.foundation.gui.AllGuiTextures`） |
| 图标按钮 | `AllIcons` + `IconButton`，或自己继承 `AbstractSimiWidget` |
| 屏幕基类 | `AbstractSimiContainerScreen<T>`（自动居中、带 Create 风格阴影与标题） |
| 滚动/分组区域 | `ScrollInput`、`BoxWidget`、`ScrollValueBehaviour` 类控件 |
| 数值框 / 提示行 | `ValueBox` + `ValueSettingsBoard` 系列（Create 里大量使用） |
| 颜色 | `catnip` 的 `theme/`（`Theme` / `Color`）而不是硬编码 `0xFF...` |

## Required practices

- Create widgets in `init()`, never in `render()`.
- Draw custom background and text before `super.render()` so widgets render on top.
- Override the vanilla background when it would cover custom artwork.
- Reset `GuiGraphics.setColor()` to white after tinted drawing (`setColor(-1)` / `setColor(1,1,1,1)`).
- Keep layout as named pixel constants relative to the window origin, not scattered magic numbers.
- Screen 类只能在客户端加载；服务端共享的逻辑放 `AbstractContainerMenu` 一侧，别在 `*Screen` 里放会在服务端被加载的代码。
- 传送/修改服务端状态一律走 payload 或菜单 `clickMenuButton`，不要在屏幕里直接改世界。

## Config screens

- NeoForge 默认配置界面已由 `MyWrenchClient` 注册：`container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new)`。
- 配置项在 `Config.java` 里定义，**翻译键必须**补到 `assets/mywrench/lang/en_us.json`（`mywrench.configuration.<key>` 与 `.tooltip`），否则界面显示原始键名。
- 想换成 Create 风格界面时，自己实现 `IConfigScreenFactory` 并复用上表的 Catnip 控件；不要为了改外观而改动 `Config.java` 的语义。

## Icons / textures ownership

- 通用控件用 Create 的 `AllIcons` / `AllGuiTextures`；只有本 mod 独有的图标才放进 `assets/mywrench/textures/gui/`。
- 最后一个 `blit` 参数是**源贴图**尺寸，不是目标尺寸；`blit` 的宽高写反是「图标被拉伸/裁切」的常见原因。

## Check

- Open the target screen, hover and click each control, then close and reopen to verify persistence.
- 分辨率变化（GUI Scale 2/3/4）下再看一遍：坐标写死相对值才会对齐。
- 服务端也启动一次，确认没有把客户端类加载到服务端（`runServer` 不崩）。
