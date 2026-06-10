# The Hungering Portal

A Fabric mod that gates the End Portal behind a ritual: even when the frame is lit with eyes of ender, the portal won't take anyone until specific offerings are thrown in and a minimum number of witnesses are present.

## How it works

1. Players light an End Portal frame normally with 12 eyes of ender — the portal blocks appear lit.
2. Anyone who tries to walk in is **launched away** and shown the required offerings on screen.
3. Players must throw ten specific items into the lit portal:
   - **A Trident from the bubbling undead** — Trident
   - **A block of Nether & Gold forged steel** — Netherite Block
   - **An egg of a beast long past** — Sniffer Egg
   - **An Apple glistening with power** — Enchanted Golden Apple
   - **A hand held savior** — Totem of Undying
   - **A Nether Star's gilded prison** — Beacon
   - **A mace of crushing weight** — Mace
   - **A compass for the fallen** — Recovery Compass
   - **A disc of porcine percussion** — Pigstep Music Disc
   - **A skull from the restless dead** — Zombie Head
4. By default at least **3 players** must have participated (thrown an offering or stood within 16 blocks of the portal during an offering).
5. If 60 seconds pass between offerings, the portal "loses patience" and returns all thrown items to whoever threw them.
6. Once activated, the change is **world-wide** — every End Portal works normally from then on.
7. State is **persistent** across server restarts.

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/thp portalreq` | Anyone | Show the portal requirements on screen and in chat. |
| `/thp setendplayercount <n>` | Op (2+) | Change the minimum participant count. Use `1` for singleplayer. |
| `/thp reset` | Op (2+) | Re-lock the End Portal so the full ritual is required again. Clears the activated state and any in-progress offerings. |

> `/endbeast` still works as an alias for `/thp` (backwards compatibility).

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api).
2. Drop the The Hungering Portal (`thp`) JAR into your `mods` folder.
3. Start the server. State is saved in `<world>/endbeast.json`.

## License

All Rights Reserved. See the [LICENSE](LICENSE) file — these mods are proprietary.
