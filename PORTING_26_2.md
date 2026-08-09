# Minecraft 26.2 port status

## Identity and upstream baseline

- Upstream repository: `odtheking/Odin`
- Public port branch: `mc-26.2`
- Current upstream baseline: `6132ac938519cfafa43a06b422c4dace0a3b0297`
- Minecraft target: 26.2 only (`~26.2`)
- Public port version: `0.3.0+mc26.2.port.1`
- Java: 25
- Fabric Loader: 0.19.3
- Fabric API: 0.154.2+26.2
- Fabric Loom: 1.16.3
- Kotlin: 2.4.0
- Fabric Language Kotlin: 1.13.12+kotlin.2.4.0
- Mod Menu: 20.0.1, resolved from its published Modrinth Maven artifact

Upstream remains the authority for ordinary Odin behavior. Minecraft 26.2 generated sources and
Fabric's 26.2 APIs are the authority for target-specific code.

## Port architecture

### World rendering

World overlays use a real extraction-to-submission flow. Features record immutable frame data
during level extraction, attach it to the level render state, and submit geometry through
Minecraft/Fabric collectors. No live world/entity objects or deferred gameplay closures cross into
the drawing phase.

### ClickGUI

The former NanoVG OpenGL renderer is replaced by backend-neutral recorded GUI commands, managed
glyph/image textures, and Minecraft GUI render states. The original bundled Inter font and GUI
assets remain in use. Text, clipping, rounded geometry, gradients, images, controls, search, and
expanded settings preserve the upstream interaction model.

### Picture-in-picture and items

Obsolete PIP constructors and global feature submission were removed. Item previews use the 26.2
item GUI extraction path, while ordinary 2D geometry is submitted directly through managed GUI
render states.

### Backend policy

Production source contains no direct OpenGL calls, raw Vulkan calls, backend/device casts, raw GPU
handles, manual framebuffer binding, or NanoVGGL3 bridge. The same source operates through
Minecraft's selected graphics backend.

## Upstream parity

The first incremental sync reviewed upstream changes from
`affec5b8d5a2cc0c671ea321f9380f63e2b77b28` through
`32222f8ecd2bbe8a6577d5051c3bca588a1f435d`: 14 commits and 33 changed paths.

The synchronized behavior includes current loadout/menu commands and titles, Hypixel action-bar
glyphs, Starts With terminal state, Mort-synchronized secret timing, bat/Mimic bonus-score state,
squared positional-message radius, response-body error reporting, and the current dungeon-map
door discovery, layout, coloring, icons, and own-player-head option.

Fourteen changed Kotlin paths normalize to exact upstream content. Eleven retain only required
26.2 API substitutions, formatting cleanup, or pure helper extraction for tests. No upstream 26.1
rendering implementation or dependency was imported.

The second incremental sync reviewed upstream changes from
`32222f8ecd2bbe8a6577d5051c3bca588a1f435d` through
`a235cafdcd449f6f16813ac7aac9dcecd2be7a88`: three commits and eight net-modified paths.
It adds UUID-keyed developer-player data and the current service schema, corrected `/oddev
roomdata` argument semantics, one-time profile-join messaging, queued client-chat insertion, and
visible first-click protection feedback for valid terminal clicks.

The public port deliberately keeps upstream startup telemetry removed, retains fork-only release
routing, and does not echo the password-bearing developer payload into chat. Minecraft-facing
screen/chat calls retain their verified 26.2 forms. An intermediate upstream Water Solver crouch
guard was reverted again by upstream HEAD, so there is no net Water Solver change to import.

The third incremental sync reviewed all 17 commits and 33 changed paths from
`a235cafdcd449f6f16813ac7aac9dcecd2be7a88` through
`6132ac938519cfafa43a06b422c4dace0a3b0297`. It ports the current Croesus menu and Wither Essence
identifiers, bounded chat-command names, registry-aware Starts With targets, automatic terminal GUI
scale, tick-scheduled positional messages, vitality parsing/HUD controls, successful-interaction
Water Solver handling, detailed TPS statistics, server-lag-aware first-click protection, the
ClickGUI chevron correction, secret-timer coloring, and the 289 Fairy Souls quiz answer. The new
`MultiPlayerGameMode.useItemOn` injection uses the exact 26.2 descriptor, and the renamed action-bar
listener retains the target packet and player APIs. Upstream's version and JitPack Java changes are
represented as `0.3.0+mc26.2.port.1` and Java 25.

## Validation

- Clean Java 25 build: passed.
- Unit tests: thirteen suites, 44 tests, zero failures/errors/skips, including UUID developer-player
  payloads, profile-join gating, room-coordinate semantics, first-click protection boundaries, and
  the public port-release version parser/updater routing.
- Forced Vulkan and OpenGL startup: Minecraft reached the title screen, loaded Odin resources and
  the managed Inter atlas, rendered ClickGUI, force-loaded the new interaction Mixin target, and
  stopped cleanly on both backends.
- Graphics devices used for the validation runs: NVIDIA Vulkan 1.4.341 and OpenGL 3.3.0, driver
  610.88.
- Static audit: no raw OpenGL/Vulkan, NanoVG, backend cast, removed buffer/PIP API, development
  smoke harness, stale Minecraft 26.1 dependency, or obsolete map resource.
- JAR audit: client metadata, Mixins, access widener, fonts, images, map assets, and Commodore are
  present; no native library is bundled.
- Public-distribution privacy: the upstream startup username telemetry is removed, support and
  update links point to this fork, and update candidates must use the compatible
  `+mc26.2.port.N` version suffix.
- Candidate artifact: `build/libs/Odin-0.3.0+mc26.2.port.1.jar`, 4,182,290 bytes, SHA-256
  `5E7EBAF00A183D70D022CB736BCD345B52E7260BFC89E3202BA6F629E78D6C9F`.

## Runtime limits

The port has not been validated against a live Hypixel session for every server-only behavior.
Current Hypixel menu titles, action-bar glyphs, bat messages, secret timing, dungeon map scanning,
and other server-specific paths are source-reviewed and regression-tested where deterministic
boundaries exist, but should not be represented as live server passes.

An active Iris shader pack, every ClickGUI input gesture, repeated reload/resize leak profiling,
and migration using a real user-provided 26.1.2 configuration are also not claimed as fully tested.

## Maintenance procedure

For each upstream update:

1. Record the old and new upstream SHAs and build the untouched upstream tree.
2. Inventory every changed production/resource path.
3. Port feature behavior while preserving verified 26.2 APIs and renderer architecture.
4. Add focused regression coverage for changed pure logic.
5. Run clean tests/builds, forbidden-API scans, JAR inspection, and a Vulkan startup check.
6. Publish a new port revision only after the upstream HEAD is rechecked.
