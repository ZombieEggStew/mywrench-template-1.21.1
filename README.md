# My Wrench

<h1 align="center">My Wrench</h1>

<h2 align="center">A Create Addon for Minecraft 1.21.1</h2>

<h3 align="center">
	<a href="https://www.curseforge.com/minecraft/mc-mods/my-wrench">
    <img src="https://img.shields.io/curseforge/dt/1809803?logo=curseforge&label=&suffix=%20&style=flat&color=242629&labelColor=F16436&logoColor=1C1C1C" alt="CurseForge"></a>
    <a href="https://modrinth.com/mod/my-wrench">
    <img src="https://img.shields.io/modrinth/dt/my-wrench?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5CA424&logoColor=1C1C1C" alt="Modrinth"></a>
</h1>

A Create addon for Minecraft **1.21.1** + NeoForge **21.1.228** (`mod id: mywrench`).

## Requirements

- JDK **21** (the Gradle toolchain is pinned to 21)
- Gradle Wrapper 9.2.1 (no manual install needed)
- Dependencies are provided by `net.neoforged.moddev 2.0.148`

## Dependencies

| mod | version | role |
|---|---|---|
| Create | 6.0.10 (maven `6.0.10-280`) | hard dependency |
| Ponder | 1.0.82 (`1.0.82+mc1.21.1`) | hard dependency (Create's API references its types) |
| Flywheel | 1.0.6 | hard dependency (Create's rendering engine) |
| Registrate | `MC1.21-1.3.0+67` | compile-time library only (not a standalone mod; supplied at runtime by Create's jarJar) |

Dependency resolution order: local `libs/` (maven directory layout, see `libs/README.md`) → `maven.createmod.net` / `maven.ithundxr.dev`.
Versions are centralized in `gradle.properties`; dependency declarations live in `build.gradle` and `src/main/templates/META-INF/neoforge.mods.toml` (when changing dependencies these three places must stay in sync — see `AGENTS.md` for the rules).

## Common Commands

```bat
gradlew.bat classes          :: compile check
gradlew.bat runClient        :: launch the development client
gradlew.bat runServer        :: launch the development server
gradlew.bat runData          :: data generation (writes to src/generated/resources)
gradlew.bat build            :: build the jar
```

> The development runtime uses the **full Create jar** (`libs/com/simibubi/create/create-1.21.1/6.0.10-280/create-1.21.1-6.0.10-280.jar`, which bundles flywheel/ponder/Registrate via jarJar), so `runClient` needs no extra jars dropped into `run/mods`.

## Project Layout

```
src/main/java/com/zzy205/mywrench/      source code (MyWrench main class / Config / MyWrenchClient)
src/main/resources/assets/mywrench/     resources (lang, models, blockstates, textures)
src/main/templates/META-INF/            mod metadata templates (gradle expands the placeholders)
libs/                                   local maven dependency repository (not committed)
memo/                                   project notes: code map, pitfalls, progress
.research/                              scratch research sources (not committed)
```

External dependency sources (read-only, shared across projects): `D:\aMyMCMods\api` (API sources), `D:\aMyMCMods\references` (example mod sources).

## Documentation

- `AGENTS.md` — agent working rules, dependency-change rules, directory conventions
- `memo/code-map.md` — first stop for locating/editing Java sources
- `memo/api-code-map.md` — first stop for looking up Create / Ponder / Flywheel / Catnip APIs
- `.agents/skills/` — task-oriented skills (minecraft-research, memo-lookup, neoforge-create-rotation, neoforge-model-rendering, create-style-gui)
