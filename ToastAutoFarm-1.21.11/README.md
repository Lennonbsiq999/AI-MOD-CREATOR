# Toast AutoFarm Cleaner 1.21.11

Fabric client-side mod for Minecraft 1.21.11.

## Features
- Search every block registered in the current game (including modded blocks).
- Select a block by its localized name or registry ID.
- Search for the nearest visible selected block within 5 blocks.
- Optional Auto-Tool: choose the most suitable and fastest tool from the 9 hotbar slots.
- Compact cleaner GUI.

## Controls
- O: Open the block picker.
- P: Toggle mining.
- Search by a display name (for example "Diamond Ore") or registry ID (for example "minecraft:diamond_ore").
- Use the Auto-Tool button to enable or disable automatic hotbar tool selection.

Auto-Tool only searches the hotbar. Blocks that are unbreakable in the current world cannot be mined. The mod is experimental; test in a private world first. Automated mining may violate multiplayer-server rules.

## Build
Requires JDK 21. Run `gradle build` (or `gradlew.bat build` when using a Gradle wrapper).
The remapped mod JAR is written to `build/libs/`.
