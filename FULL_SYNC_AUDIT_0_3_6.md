# Odin 0.3.6 synchronization — Minecraft 26.2

Verified 2026-10-07 against official `odtheking/Odin` main and tag `0.3.6`, both
`833e0533ef9c47529b790612627a65618ebd5a58`. The final remote check returned the same revision.
Upstream now targets Minecraft 26.2. This update ports the full 0.3.3-to-0.3.6
feature delta while retaining this port's existing ClickGUI as requested.

## Scope and compatibility

- All 71 upstream module declarations and paths are present. New waypoint management,
  terminal status and prediction, dungeon/boss timers, map synchronization, event hooks,
  resources, and other gameplay fixes follow the pinned release, including upstream removals.
- Existing ClickGUI panels, controls, search, HUD editor, font renderer, module HUD contract,
  category layout, and legacy assets are preserved. All 55 protected source/asset files in
  each tree match the pre-update backup byte-for-byte (41 source files plus 14 assets).
- New enum/range declarations use adapters around the existing selector and number controls.
  Enum settings read legacy display labels and current enum names. Existing saved HUD
  positions retain the port's physical-pixel coordinate system.
- New standalone waypoint screens use isolated Compose support and dialog controls. These
  do not replace or render the existing ClickGUI. Favorite colors belong to Waypoints.
- ClickGUIModule changes are limited to updated map sync messages and socket shutdown.
  Existing GUI settings and startup telemetry exclusion remain intact.
- The managed 26.2 extraction/submission renderer remains, with the updated upstream event
  name, render types, text phase, beacon timing, and box styles. Existing platform hook names
  remain where Windows case-insensitive paths prevent adopting GUIMixin/HUDMixin names.
- Tested helper seams remain equivalent to upstream; dungeon bonus score now counts every
  bat. The local force-dungeon command remains, including its existing boss-state masking.
- Java 25, current existing Fabric/Loom/Kotlin dependencies and optional-mod pins remain.
  Compose runtime 1.10.0 and its required AndroidX dependencies are bundled for new dialogs.

## Verification

- Core: build and 54 tests pass.
- Public port: clean build and 60 tests pass.
- Regression checks cover legacy selector loading, enum persistence, numeric bounds/steps,
  waypoint geometry and deserialization, plus the retained port regressions.
- Public binary/source JAR inspection found no private add-on or diagnostic harness entries.
  All declared Mixin classes are packaged. Bundled libraries contain no duplicate Kotlin or
  coroutine runtimes and no NanoVG native dependency.
- Source whitespace and merge-conflict checks pass. Public and private source boundaries remain.

At completion of the local port, no game launch, live Hypixel test, installation, deployment,
push, or release had been performed. Subsequent publication uses the audited artifact below. Earlier 0.3.3 runtime smoke is historical evidence only; new screens and live behavior
still need runtime verification. Build success does not establish Mixin application or visual health.

## Artifact

`Odin-0.3.6+mc26.2.port.1.jar` (6947338 bytes).
SHA-256: `90379481e238f3d6d8fbc210b981f4b6c161901c9b238e9b88ce8053b9458053`.
