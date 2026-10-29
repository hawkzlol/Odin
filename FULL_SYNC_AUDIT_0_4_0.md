# Odin 0.4.0 full synchronization audit

Date: 2026-10-29. Official release tag `0.4.0`, commit
`16e31d0b234d59dfaefcc68381ab4411095cc78a` in `odtheking/Odin`.
The previous port baseline was `f2840a0`. Upstream targets Minecraft 26.3;
this port remains on Minecraft 26.2, Java 25 and its existing Fabric/Loom toolchain.

## Scope

Applied the complete 33-path release delta, then compared the entire 271-file upstream Git
archive, including unchanged production code, resources, build configuration and metadata.
All 71 upstream module declarations/paths match. Every remaining difference is classified below.

Included changes:
- Etherwarp collision/standing-position estimation fixes, server-position option, and restored
  dungeon etherwarp-waypoint triggering.
- Immediate Blaze nameplate scanning, optional blaze/nameplate/smoke hiding, puzzle resets and cleanup.
- Particle event producer and subscribers for Gyro cooldown detection and targeted explosion hiding.
- Fire Freeze timer update, Paul on/off/auto command, puzzle map checkmarks and special-column discovery fixes.
- Map head/background/pointer scale settings and current render submission behavior.
- Name replacement and event dispatch performance updates, updated block-update hook timing,
  reduced redundant movement events and asynchronous WebSocket sending.
- Faster UUID lookup with official fallback and current Hypixel API endpoint.

## Preserved behavior and adaptations

Our ClickGUI, HUD editor, panels, controls, font renderer, existing setting adapters and Numbers
Customizer remain. Of the 73 protected UI/support paths, 72 match the backup byte-for-byte.
ClickGUIModule changes only its API configuration imports/default/getter (plus root blank-line
whitespace); its visual behavior is unchanged. The existing `API URL` key is retained. Saved
values using the former default endpoint resolve to the new upstream URL; custom endpoints
remain unchanged. This avoids silently retaining a retired default or resetting user overrides.

The managed 26.2 extraction/immutable-frame/submission architecture and native item renderer
remain. The upstream text-submission simplification is applied through that architecture.
Minecraft 26.2 movement signatures, particle-speed fields, shader ABI and authlib classes remain.
GUI Mixin names retain the Windows-safe split between GameGuiMixin and GuiMixin. Privacy/update
routing differences and startup telemetry exclusion remain. The local forcedungeons override
and its existing boss-state masking limitation remain unchanged.

## Verification

- Core build and all 62 tests pass; public build and all 68 tests pass, with no skips/errors/failures.
- Clean builds were exercised; final test/build passes include the new regression tests.
- Added endpoint migration/custom-endpoint preservation, whole-name replacement/formatting and
  malformed-input tests, plus event priority/cancellation/subscribe/unsubscribe regression coverage.
- New and changed Mixin targets were checked against the actual 26.2 source: ClientLevel particle
  signature; LevelChunk section-write invocation and BlockPos argument; EntityRenderer return hook
  without the 26.3 partialTicks parameter; Font prepareText/width signatures; Entity interpolation.
- LocalPlayer xLast/yLast/zLast fields exist in 26.2 and are exposed through the access widener.
- ParticleEvent, ClientLevelMixin and all declared Mixins are packaged. Source JARs match final sources.
- Public binary/source artifacts contain no private add-ons, user diagnostics or runtime harnesses.
- Merge-conflict and whitespace checks pass.

The name codec test requires Minecraft registry bootstrap in the test process; this was added
after the initial test failure. No game client was launched. Live Mixin application, rendering,
Hypixel behavior and network services remain unverified. No installation, push or release was performed.

## Artifact

`Odin-0.4.0+mc26.2.port.1.jar` (6978192 bytes).

SHA-256: `1305b1f69094f2e0966af43801f71f03a35146227da6364a84446fc081496453`.

## Complete upstream inventory

271 files: 4 absent-by-design, 140 adapted, 127 equal.

| Path | Comparison | Resolution |
| --- | --- | --- |
| `.github/workflows/build.yml` | adapted | 26.2 toolchain/build/CI configuration retained |
| `.gitignore` | adapted | 26.2 toolchain/build/CI configuration retained |
| `build.gradle.kts` | adapted | 26.2 toolchain/build/CI configuration retained |
| `gradle/wrapper/gradle-wrapper.jar` | equal | Exact after newline normalization |
| `gradle/wrapper/gradle-wrapper.properties` | adapted | 26.2 toolchain/build/CI configuration retained |
| `gradle.properties` | adapted | 26.2 toolchain/build/CI configuration retained |
| `gradlew` | equal | Exact after newline normalization |
| `gradlew.bat` | equal | Exact after newline normalization |
| `jitpack.yml` | equal | Exact after newline normalization |
| `LICENSE` | equal | Exact after newline normalization |
| `README.md` | adapted | 26.2 toolchain/build/CI configuration retained |
| `settings.gradle.kts` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/AbstractContainerMenuMixin.java` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/AbstractContainerScreenMixin.java` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/AvatarRendererMixin.java` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/java/com/odtheking/mixin/mixins/BossBarHealthOverlayMixin.java` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/ChatScreenMixin.java` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/ClientLevelMixin.java` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/ClientPacketListenerMixin.java` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/ConnectionMixin.java` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/java/com/odtheking/mixin/mixins/EntityMixin.java` | adapted | 26.2 movement/interpolation injection signature |
| `src/main/java/com/odtheking/mixin/mixins/EntityRendererMixin.java` | adapted | 26.2 shouldRender injection signature |
| `src/main/java/com/odtheking/mixin/mixins/ExperienceBarRendererMixin.java` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/FontMixin.java` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/java/com/odtheking/mixin/mixins/GUIMixin.java` | adapted | Upstream GUI lifecycle lives in GameGuiMixin; filesystem case collision with local HUD GuiMixin |
| `src/main/java/com/odtheking/mixin/mixins/HUDMixin.java` | absent-by-design | Equivalent implementation in GuiMixin.java |
| `src/main/java/com/odtheking/mixin/mixins/KeyBindingMixin.java` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/LevelChunkMixin.java` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/java/com/odtheking/mixin/mixins/LivingEntityMixin.java` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/LocalPlayerMixin.java` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/MinecraftMixin.java` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/MouseHandlerMixin.java` | adapted | 26.2 cursor-release API and retained tail injection |
| `src/main/java/com/odtheking/mixin/mixins/MultiPlayerGameModeMixin.java` | adapted | Private visibility for otherwise identical callback |
| `src/main/java/com/odtheking/mixin/mixins/ScreenEffectRendererMixin.java` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/ScreenMixin.java` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/SynchedEntityDataMixin.java` | equal | Exact after newline normalization |
| `src/main/java/com/odtheking/mixin/mixins/YggdrasilSignatureIgnoreMixin.java` | adapted | 26.2 authlib service class name |
| `src/main/kotlin/com/odtheking/odin/clickgui/ClickGUI.kt` | adapted | Preserved ClickGUI/support or isolated Compose dialog adapter |
| `src/main/kotlin/com/odtheking/odin/clickgui/GuiTheme.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/clickgui/HudLayer.kt` | absent-by-design | Excluded upstream ClickGUI replacement |
| `src/main/kotlin/com/odtheking/odin/clickgui/HudManager.kt` | adapted | Preserved ClickGUI/support or isolated Compose dialog adapter |
| `src/main/kotlin/com/odtheking/odin/clickgui/ModMenuIntegration.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/clickgui/OdinScreen.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/AlwaysActive.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/DevModule.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/ActionSetting.kt` | adapted | Preserved ClickGUI/support or isolated Compose dialog adapter |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/BooleanSetting.kt` | adapted | Preserved ClickGUI/support or isolated Compose dialog adapter |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/ColorSetting.kt` | adapted | Preserved ClickGUI/support or isolated Compose dialog adapter |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/DropdownSetting.kt` | adapted | Preserved ClickGUI/support or isolated Compose dialog adapter |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/HUDElement.kt` | adapted | Preserved ClickGUI/support or isolated Compose dialog adapter |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/HUDSetting.kt` | adapted | Preserved ClickGUI/support or isolated Compose dialog adapter |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/KeybindSetting.kt` | adapted | Preserved ClickGUI/support or isolated Compose dialog adapter |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/ListSetting.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/MapSetting.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/NumberSetting.kt` | adapted | Preserved ClickGUI/support or isolated Compose dialog adapter |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/SelectorSetting.kt` | adapted | Preserved ClickGUI/support or isolated Compose dialog adapter |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/StringSetting.kt` | adapted | Preserved ClickGUI/support or isolated Compose dialog adapter |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/Saving.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/clickgui/settings/Setting.kt` | adapted | Preserved ClickGUI/support or isolated Compose dialog adapter |
| `src/main/kotlin/com/odtheking/odin/clickgui/ui/Animation.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/clickgui/ui/Components.kt` | adapted | Preserved ClickGUI/support or isolated Compose dialog adapter |
| `src/main/kotlin/com/odtheking/odin/clickgui/ui/ModuleView.kt` | absent-by-design | Excluded upstream ClickGUI replacement |
| `src/main/kotlin/com/odtheking/odin/clickgui/ui/Panel.kt` | absent-by-design | Excluded upstream ClickGUI replacement |
| `src/main/kotlin/com/odtheking/odin/commands/CataCommand.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/commands/DevCommand.kt` | adapted | Equivalent tested room-coordinate helper |
| `src/main/kotlin/com/odtheking/odin/commands/DungeonWaypointCommand.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/commands/MainCommand.kt` | adapted | Retained local forcedungeons command |
| `src/main/kotlin/com/odtheking/odin/commands/SoopyCommand.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/commands/TermsimCommand.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/commands/WaypointCommand.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/config/DungeonWaypointConfig.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/config/ModuleConfig.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/config/WaypointPackFileUtils.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/config/WaypointPackStateFile.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/events/core/CancellableEvent.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/events/core/Event.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/events/core/EventBus.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/events/core/EventPriority.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/events/CustomEvents.kt` | adapted | 26.2 extraction context and immutable render consumer |
| `src/main/kotlin/com/odtheking/odin/events/EventDispatcher.kt` | adapted | 26.2 extraction followed by frame-data submission |
| `src/main/kotlin/com/odtheking/odin/events/GuiEvent.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/events/ScreenEvent.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/Category.kt` | adapted | Retained ClickGUI category layout |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/ArrowAlign.kt` | adapted | 26.2 BlockPos center API |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/ArrowsDevice.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/DragonCheck.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/ExtraStats.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/KingRelics.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/LividSolver.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/MelodyMessage.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/SimonSays.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/SpiritBear.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/termGUI/CustomTermGui.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/termGUI/MelodyGui.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/TerminalSimulator.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/TerminalSolver.kt` | adapted | User-requested Numbers Customizer and existing control adapters |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/TerminalSounds.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/TerminalsStatus.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/TerminalTimes.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/TerminalTitles.kt` | adapted | Old string control lacks placeholder hint |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/termsim/MelodySim.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/termsim/NumbersSim.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/termsim/PanesSim.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/termsim/RubixSim.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/termsim/SelectAllSim.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/termsim/StartGUI.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/termsim/StartsWithSim.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/termsim/TermSimGUI.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/TerracottaTimer.kt` | adapted | 26.2 BlockPos center API |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/TickTimers.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/WitherDragons.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/boss/WitherDragonsEnum.kt` | adapted | 26.2 scalar particle maxSpeed replaces 26.3 per-axis fields |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/BetterPartyFinder.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/BlessingDisplay.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/BloodCamp.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/BreakerDisplay.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/Croesus.kt` | adapted | Disambiguates rounded-outline overload while retaining shape |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/DoorHighlight.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/DungeonQueue.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/dungeonwaypoints/DialogComponents.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/dungeonwaypoints/DungeonWaypointEditor.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/dungeonwaypoints/DungeonWaypointHud.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/dungeonwaypoints/DungeonWaypointPacks.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/dungeonwaypoints/DungeonWaypoints.kt` | adapted | Old StringSetting lacks placeholder hint; latest command-parsable enum retained |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/dungeonwaypoints/SecretWaypoints.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/dungeonwaypoints/TextPromtScreen.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/dungeonwaypoints/WaypointPackSelectorScreen.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/Highlight.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/InvincibilityTimer.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/LeapMenu.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/MageBeam.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/map/DungeonMap.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/map/DungeonScan.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/map/MapRenderer.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/map/MapScan.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/map/SpecialColumn.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/map/tile/DungeonDoor.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/map/tile/DungeonRoom.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/map/tile/RoomData.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/map/WorldScan.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/MapInfo.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/Mimic.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/PuzzleHud.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/puzzlesolvers/BeamsSolver.kt` | adapted | 26.2 BlockPos center API |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/puzzlesolvers/BlazeSolver.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/puzzlesolvers/BoulderSolver.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/puzzlesolvers/IceFillSolver.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/puzzlesolvers/PuzzleSolvers.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/puzzlesolvers/QuizSolver.kt` | adapted | Equivalent tested quiz timer helpers |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/puzzlesolvers/TPMazeSolver.kt` | adapted | 26.2 BlockPos center API |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/puzzlesolvers/WaterSolver.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/puzzlesolvers/WeirdosSolver.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/RoomClear.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/SecretClicked.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/SecretsCounter.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/nether/BuildHelper.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/nether/FreshTools.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/nether/KuudraInfo.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/nether/KuudraTracker.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/nether/Misc.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/nether/NoPre.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/nether/PearlWaypoints.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/nether/RemovePerks.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/nether/SupplyHelper.kt` | adapted | 26.2 BlockPos center API |
| `src/main/kotlin/com/odtheking/odin/features/impl/nether/Vesuvius.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/Camera.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/ClickGUIModule.kt` | adapted | Retained GUI and privacy/update routing; old API URL key retained with default endpoint migration |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/CustomNameReplacer.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/Etherwarp.kt` | adapted | Existing enum control adapter; otherwise release logic |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/GyroWand.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/HidePlayers.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/PerformanceHUD.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/PlayerSize.kt` | adapted | Equivalent tested current-case request payload helper |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/RenderOptimizer.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/RenderTest.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/Shenanigans.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/TemporaryWaypoints.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/waypoints/WaypointAreas.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/waypoints/Waypoints.kt` | adapted | Favorite colors persisted outside retained ClickGUI; old numeric adapter |
| `src/main/kotlin/com/odtheking/odin/features/impl/render/waypoints/WaypointScreen.kt` | adapted | Isolated Compose dialog controls instead of legacy ClickGUI controls |
| `src/main/kotlin/com/odtheking/odin/features/impl/skyblock/AutoSprint.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/skyblock/ChatCommands.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/skyblock/CommandKeybinds.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/skyblock/LoadoutKeybinds.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/skyblock/NoCursorReset.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/skyblock/PetKeybinds.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/skyblock/PlayerDisplay.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/skyblock/QuiverDisplay.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/skyblock/Ragnarock.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/skyblock/SlotBinds.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/skyblock/Splits.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/impl/skyblock/SpringBoots.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/features/impl/skyblock/WardrobeKeybinds.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/features/Module.kt` | adapted | Retained HUD coordinate and setting contract for existing ClickGUI |
| `src/main/kotlin/com/odtheking/odin/features/ModuleManager.kt` | adapted | Legacy keybind value accessor and HUD rendering contract; upstream key identity matching applied |
| `src/main/kotlin/com/odtheking/odin/OdinMod.kt` | adapted | Managed glyph lifecycle and extraction manager; native item rendering replaces custom PIP |
| `src/main/kotlin/com/odtheking/odin/utils/AutoSessionID.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/ChatUtils.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/utils/Color.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/handlers/TickTasks.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/IrisCompat.kt` | adapted | 26.2 pipeline API and additional port pipeline classification |
| `src/main/kotlin/com/odtheking/odin/utils/ItemUtils.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/IVec2.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/utils/JsonResourceLoader.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/network/hypixelapi/HypixelData.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/network/hypixelapi/RequestUtils.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/network/WebSocketConnection.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/utils/network/WebUtils.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/utils/PersonalBest.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/utils/PlayerUtils.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/utils/render/CustomRenderPipelines.kt` | adapted | 26.2 managed rendering/API adaptation; full draw API retained |
| `src/main/kotlin/com/odtheking/odin/utils/render/CustomRenderType.kt` | adapted | 26.2 managed rendering/API adaptation; full draw API retained |
| `src/main/kotlin/com/odtheking/odin/utils/render/DrawContextUtils.kt` | adapted | 26.2 managed rendering/API adaptation; full draw API retained |
| `src/main/kotlin/com/odtheking/odin/utils/render/ItemRenderer.kt` | adapted | 26.2 managed rendering/API adaptation; full draw API retained |
| `src/main/kotlin/com/odtheking/odin/utils/render/RenderUtils.kt` | adapted | 26.2 managed rendering/API adaptation; full draw API retained |
| `src/main/kotlin/com/odtheking/odin/utils/render/RoundedRect.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/render/RoundedRectRenderer.kt` | adapted | 26.2 managed rendering/API adaptation; full draw API retained |
| `src/main/kotlin/com/odtheking/odin/utils/render/Scissor.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/ServerUtils.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/ActionBarListener.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/dungeon/DungeonEnums.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/dungeon/DungeonListener.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/dungeon/DungeonUtils.kt` | adapted | Equivalent tested score helper and retained forcedungeons override |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/dungeon/terminals/terminalhandler/MelodyHandler.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/dungeon/terminals/terminalhandler/NumbersHandler.kt` | adapted | User-requested independent In Order Numbers toggle |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/dungeon/terminals/terminalhandler/PanesHandler.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/dungeon/terminals/terminalhandler/RubixHandler.kt` | adapted | User-requested independent In Rubix Numbers toggle |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/dungeon/terminals/terminalhandler/SelectAllHandler.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/dungeon/terminals/terminalhandler/StartsWithHandler.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/dungeon/terminals/terminalhandler/TerminalHandler.kt` | adapted | Equivalent tested protection helper; new input mapping retained |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/dungeon/terminals/TerminalTypes.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/dungeon/terminals/TerminalUtils.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/Island.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/KuudraUtils.kt` | adapted | Equivalent code; imports/control adapters or whitespace only |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/LocationUtils.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/PartyUtils.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/SplitsManager.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/skyblock/Supply.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/ui/animations/Easing.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/ui/compose/Layouts.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/ui/compose/Motion.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/ui/compose/ScrollState.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/ui/compose/UiHost.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/ui/compose/UiNode.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/ui/widget/CustomGUIImpl.kt` | equal | Exact after newline normalization |
| `src/main/kotlin/com/odtheking/odin/utils/Utils.kt` | adapted | Existing control adapters and no placeholder hint |
| `src/main/kotlin/com/odtheking/odin/utils/VecUtils.kt` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/icon.png` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/map/cross.png` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/map/green_check.png` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/map/question.png` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/map/white_check.png` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/puzzles/boulder-solutions.json` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/puzzles/creeper-beams-solutions.json` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/puzzles/ice-fill-floors.json` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/puzzles/quiz-answers.json` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/puzzles/water-solutions.json` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/rooms.json` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/shaders/core/round_rect.fsh` | adapted | 26.2 shader ABI retained; 26.3 changes use renamed renderpearl GLSL imports |
| `src/main/resources/assets/odin/shaders/core/round_rect.vsh` | adapted | 26.2 shader ABI retained; 26.3 changes use renamed renderpearl GLSL imports |
| `src/main/resources/assets/odin/shaders/core/round_rect_shadow.fsh` | adapted | 26.2 shader ABI retained; 26.3 changes use renamed renderpearl GLSL imports |
| `src/main/resources/assets/odin/shaders/core/round_rect_textured.fsh` | adapted | 26.2 shader ABI retained; 26.3 changes use renamed renderpearl GLSL imports |
| `src/main/resources/assets/odin/shaders/include/round_rect.glsl` | adapted | 26.2 shader ABI retained; 26.3 changes use renamed renderpearl GLSL imports |
| `src/main/resources/assets/odin/textures/chevron.png` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/textures/huegradient.png` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/textures/job.png` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/textures/logo.png` | equal | Exact after newline normalization |
| `src/main/resources/assets/odin/textures/movementicon.png` | equal | Exact after newline normalization |
| `src/main/resources/fabric.mod.json` | adapted | 26.2 metadata, managed renderer, public identity and mixin names |
| `src/main/resources/odin.accesswidener` | adapted | 26.2 metadata, managed renderer, public identity and mixin names |
| `src/main/resources/odin.mixins.json` | adapted | 26.2 metadata, managed renderer, public identity and mixin names |

## Port-only production files

- `src/main/java/com/odtheking/mixin/mixins/GameGuiMixin.java`
- `src/main/kotlin/com/odtheking/odin/clickgui/Panel.kt`
- `src/main/kotlin/com/odtheking/odin/clickgui/SearchBar.kt`
- `src/main/kotlin/com/odtheking/odin/clickgui/settings/ModuleButton.kt`
- `src/main/kotlin/com/odtheking/odin/clickgui/settings/RenderableSetting.kt`
- `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/EnumSelectorSetting.kt`
- `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/HudDrawing.kt`
- `src/main/kotlin/com/odtheking/odin/clickgui/settings/impl/RangeNumberSetting.kt`
- `src/main/kotlin/com/odtheking/odin/features/impl/dungeon/puzzlesolvers/QuizTimerLogic.kt`
- `src/main/kotlin/com/odtheking/odin/utils/PortVersion.kt`
- `src/main/kotlin/com/odtheking/odin/utils/network/hypixelapi/ApiEndpoint.kt`
- `src/main/kotlin/com/odtheking/odin/utils/render/DrawContextRenderer.kt`
- `src/main/kotlin/com/odtheking/odin/utils/render/OdinWorldRenderData.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/HoverHandler.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/MouseUtils.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/TextInputHandler.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/animations/Animation.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/animations/ColorAnimation.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/animations/EaseInOutAnimation.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/animations/EaseOutAnimation.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/animations/LinearAnimation.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/compose/settings/ColorSetting.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/compose/settings/DialogSetting.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/compose/settings/NumberSetting.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/rendering/Font.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/rendering/Gradient.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/rendering/Image.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/rendering/NVGRenderer.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/rendering/OdinGlyphAtlas.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/rendering/SmoothGlyphRenderState.kt`
- `src/main/kotlin/com/odtheking/odin/utils/ui/rendering/UiGeometryRenderState.kt`
- `src/main/resources/assets/odin/chevron.png`
- `src/main/resources/assets/odin/chevron.png.mcmeta`
- `src/main/resources/assets/odin/hue_gradient.png`
- `src/main/resources/assets/odin/hue_gradient.png.mcmeta`
- `src/main/resources/assets/odin/movement_icon.png`
- `src/main/resources/assets/odin/movement_icon.png.mcmeta`
- `src/main/resources/assets/odin/font/default.json`
- `src/main/resources/assets/odin/font/default.ttf`
