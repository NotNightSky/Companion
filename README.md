# Companion

A lightweight, client-side Fabric mod that brings animated 2D companions to your screen — on the HUD and through every menu.

![Companion in action](https://cdn.modrinth.com/data/WUZ1RDId/images/9c85975fe0d9e7b21b173e21a8331f78898f655d.gif)

## Features

- Supports GIFs and PNGs.
- Movable and resizable companion.

## Requirements

Minecraft 26.3 · Fabric Loader 0.20.1+ · Fabric API · ModMenu 21.0.0+ · Java 25

## Installation

1. Install Fabric Loader, [Fabric API](https://modrinth.com/mod/fabric-api), and [ModMenu](https://modrinth.com/mod/modmenu).
2. Drop the Companion jar into your `mods` folder.
3. Launch the game and open **ModMenu → Companion**.

## Texture sets

See the [wiki](https://github.com/NotNightsky/companion/wiki) for the texture set format and configuration. Example sets are available in [`public/companion`](public/companion).

## Building from source

```sh
./gradlew build        # jar in build/libs
./gradlew runClient    # launch a test client
```

Requires JDK 25.

## License

MIT — see [LICENSE](LICENSE).
