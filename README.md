# EndBeast

A Fabric mod that gates the End Portal behind a ritual: even when the frame is lit with eyes of ender, the portal won't take anyone until specific offerings are thrown in and a minimum number of witnesses are present.

## How it works

1. Players light an End Portal frame normally with 12 eyes of ender — the portal blocks appear lit.
2. Anyone who tries to walk in is **launched away** and shown the required offerings on screen.
3. Players must throw five specific items into the lit portal:
   - **A Trident from the bubbling undead** — Trident
   - **A block of Nether & Gold forged steel** — Netherite Block
   - **An egg of a beast long past** — Sniffer Egg
   - **An Apple glistening with power** — Enchanted Golden Apple
   - **A hand held savior** — Totem of Undying
4. By default at least **3 players** must have participated (thrown an offering or stood within 16 blocks of the portal during an offering).
5. If 60 seconds pass between offerings, the portal "loses patience" and returns all thrown items to whoever threw them.
6. Once activated, the change is **world-wide** — every End Portal works normally from then on.
7. State is **persistent** across server restarts.

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/EndBeast portalreq` | Anyone | Show the portal requirements on screen and in chat. |
| `/EndBeast setendplayercount <n>` | Op (2+) | Change the minimum participant count. Use `1` for singleplayer. |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api).
2. Drop the EndBeast JAR into your `mods` folder.
3. Start the server. State is saved in `<world>/endbeast.json`.

## License

MIT
