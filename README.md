# Odin for Minecraft 26.2

This branch is an **unofficial Minecraft 26.2 port** of
[odtheking/Odin](https://github.com/odtheking/Odin). It tracks upstream main `f2840a0` (0.3.7 development snapshot) while retaining
this port's existing ClickGUI, controls and HUD editor. It is not affiliated with or endorsed by odtheking.

The current refresh includes the upstream gameplay updates and new standalone waypoint screens.
Rendering retains the port's backend-neutral extraction/render-state architecture. The public
build passes 63 tests; this version has not yet undergone game-launch or live Hypixel validation.
See [the update audit](FULL_SYNC_AUDIT_2026_10_17.md) for details.

## Installation

Install the release JAR with:

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer compatible 0.19.x release
- Fabric API 0.154.2+26.2 or newer compatible 26.2 build
- Fabric Language Kotlin 1.13.12+kotlin.2.4.0 or newer compatible build
- Java 25

Mod Menu and Iris are optional. The target Iris build used for compatibility checks is
1.11.2+26.2-fabric.

## Building

On Windows:

```powershell
.\gradlew.bat clean build
```

On Unix-like systems:

```sh
./gradlew clean build
```

The production JAR is written to `build/libs/`.

## Upstream synchronization

Current baseline: upstream main **0.3.7 development snapshot** (`f2840a0`), port version **0.3.7+mc26.2.port.1**.
Upstream now targets 26.3; this branch continues to target 26.2.
The existing port ClickGUI and HUD editor are retained.

The `mc-26.2` branch is based on upstream Git history. Upstream changes are reviewed and ported by
behavior rather than copied blindly: Minecraft-facing calls are resolved against 26.2, while
ordinary feature logic remains aligned with upstream. See [PORTING_26_2.md](PORTING_26_2.md) for
the current baseline, architecture, validation, and known runtime limits.

## Scope and privacy

This repository contains Odin only. Private add-ons, local game instances, configurations, logs,
screenshots, accounts, and development runtime data are intentionally excluded.

## License and attribution

Copyright remains with the original Odin contributors. Redistribution and modification follow the
repository's [BSD 3-Clause license](LICENSE). The Odin name and contributor names do not imply
endorsement of this unofficial port.
