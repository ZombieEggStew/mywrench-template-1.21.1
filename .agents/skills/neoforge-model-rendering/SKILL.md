---
name: neoforge-model-rendering
description: "Use when creating or debugging NeoForge OBJ models, MTL textures, standalone BakedModel loading, BlockEntityRenderer model rendering, or Flywheel visual-driven rendering."
---

# NeoForge Model Rendering

## OBJ resources

- Use a JSON model with `"loader": "neoforge:obj"`, `"flip_v": true`, and a `model` path under `models/`.
- Map MTL materials through JSON texture slots: MTL `map_Kd #0` maps to JSON texture key `"0"`.
- For reusable geometry with different skins, use a base OBJ JSON and small parent JSON texture variants.
- Blender exports need UVs, materials, triangulated faces, and a tested axis convention. Treat visual verification in-game as authoritative.
- Keep OBJ/MTL under the mod namespace (`assets/mywrench/models/...`) so resource-pack paths stay predictable.

## Standalone BER models

Models not referenced by a blockstate must be registered with `ModelEvent.RegisterAdditional` using `ModelResourceLocation.standalone(location)`, then captured from `ModelEvent.BakingCompleted`.

```java
event.register(ModelResourceLocation.standalone(location));
BakedModel model = event.getModels().get(ModelResourceLocation.standalone(location));
```

Render them through the block-model renderer using `Sheets.solidBlockSheet()`, not `RenderType.solid()`. A missing registration or wrong buffer commonly presents as a purple-black model.

## Flywheel visuals (Create dynamic blocks)

- Prefer Flywheel (`dev.engine_room.flywheel.api.*` + `lib.*`) over a hand-written BER for anything animated or instanced; Create's own dynamic blocks all do this.
- Typical path: register a `Visual` (often extending `lib`'s `SimpleVisual`/`AbstractVisual`) on `VisualizerRegistry`, build the model through `InstanceTypes`, then let the instancing backend render it.
- API sources: `D:\aMyMCMods\api\flywheel-neoforge-1.21.1-1.0.6-sources\dev\engine_room\flywheel\{api,lib}`; Create-side examples: `...\create-1.21.1-6.0.10-280-sources\com\simibubi\create\content\kinetics`.
- Do not mix a BER and a Visual for the same rendered object — you get double rendering or z-fighting.

## Buffers and transforms

- Catnip `SuperByteBuffer` is the usual workhorse for block-ish geometry: `translate`, `rotateCentered`, `unCentre`, `shiftUV`, `light`, `renderInto`. Rotation sign conventions are covered by the `neoforge-create-rotation` skill.
- For item/hand rendering, remember `ItemDisplayContext` (`gui`, `firstperson_*`, `thirdperson_*`) usually needs separate transforms.

## Check

- Confirm resource paths match the namespace and model layout exactly.
- Run `./gradlew.bat classes`; launch the client for texture, lighting, and orientation verification.
