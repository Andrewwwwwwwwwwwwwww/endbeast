# EndBeast Changelog

## [1.3.0] - 2026-06-04

### Added
- `/endbeast reset` (ops only) — re-locks the End Portal so the full ritual is required again.
  Clears the activated state and any in-progress ritual progress, and announces "The End Portal
  hungers once more." (In-progress offerings are not returned.)

---

## [1.2.3] - 2026-05-29

### Changed
- Chat requirement list now appends the actual item name in parentheses after each poetic clue, e.g. `A hand held savior (Totem of Undying)`, making it immediately clear what to collect.

---

## [1.2.2] - 2026-05-28

### Fixed
- Mod Menu config screen and README referenced the command as `/EndBeast`; corrected to `/endbeast` to match the case-sensitive Brigadier registration.

---

## [1.2.1] - 2026-05-28

### Added
- Mod Menu integration — players can configure the required participant count via the in-game mod list without using commands. Multiplayer clients see a read-only screen with the relevant commands instead.
- Icon updated to 400×400 RGBA PNG for CurseForge logo compatibility.

---

## [1.2.0] - 2026-05-27

### Added
- Five new required offerings: Beacon, Mace, Recovery Compass, Pigstep Music Disc, Zombie Head — bringing the total to ten items.
- Player-count requirement line added to the chat backup that fires after the on-screen slideshow, so witnesses needed is always visible in the chat history.
- Title/subtitle display now adapts to GUI scale — large titles use a subtitle line instead of cramming text into the title, preventing overflow at GUI scale 3+.

---

## [1.1.1] - 2026-05-15

### Fixed
- Command `/EndBeast` renamed to `/endbeast` — Brigadier is case-sensitive; the old name was awkward and non-standard.
- Mixin `compatibilityLevel` corrected from `JAVA_21` to `JAVA_25` to match the project's compile target.
- Memory leak: `pendingChats`, `pendingTitles`, and `lastMessageTick` are now cleared when the server reloads, preventing stale UI state from carrying over in development environments.

### Changed
- Fabric Loom pinned to `1.16.2` (stable) — was previously on `1.16-SNAPSHOT`.
- Fabric API dependency tightened to `>=0.148.2` instead of the wildcard `*`.

### Added
- MIT `LICENSE` file added to the repository and packaged in the JAR.
- Updated mod icon (full end portal frame structure with stars).

---

## [1.1.0] - 2026-05-15

### Added
- `/endbeast portalreq` command — shows the portal requirements on screen and in chat without needing to interact with the portal.
- Title/subtitle slideshow for requirements display — each item scrolls through sequentially on-screen, with a chat backup after the sequence ends.
- Repackaged to `io.github.andrewwwwwwwwwwwwwww.endbeast` namespace.
- SLF4J logger replaces raw stderr output.
- `fabric.mod.json` contact metadata (homepage, sources, issues).
- README.md with full feature and install documentation.

### Fixed
- Mixin descriptor corrected to include the missing `boolean` parameter in `entityInside` — the mixin previously failed to inject.
- LP-box title rendering replaced with a proper title slideshow sequence that doesn't interfere with other UI.

---

## [1.0.4] - 2026-05-15

### Added
- Chat message queued after the title/subtitle sequence fades so players have a text reference after the on-screen display is gone.

### Changed
- Portal requirements now displayed on-screen via title/subtitle packets instead of only in chat.

---

## [1.0.1] - 2026-05-15

### Added
- Initial public release (renamed from `enterend`).
- End Portal ritual gate: 12 eyes of ender light the portal, but players are repelled until five specific offerings are thrown in and a minimum number of witnesses are present.
- Required offerings: Trident, Netherite Block, Sniffer Egg, Enchanted Golden Apple, Totem of Undying.
- 60-second offering timeout — if players take too long between offerings the portal returns all items.
- Configurable minimum participant count via `/EndBeast setendplayercount <n>`.
- Persistent world state saved to `<world>/endbeast.json`.
- Activation is world-wide and permanent once triggered.
