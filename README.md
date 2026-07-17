# Odin for Minecraft 26.2

This branch is an **unofficial Minecraft 26.2 port** of
[odtheking/Odin](https://github.com/odtheking/Odin). It is maintained separately because upstream
currently targets Minecraft 26.1.2. It is not affiliated with or endorsed by odtheking.

The port keeps Odin's gameplay behavior and configuration format while adapting the client to
Minecraft 26.2's extraction/render-state architecture. Rendering is backend-neutral and is tested
with Minecraft's Vulkan backend; no OpenGL compatibility bridge or raw Vulkan renderer is used.

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
