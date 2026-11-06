# Toast AutoFarm 1.21.11

Fabric client-side mod for Minecraft 1.21.11.

## Controls
- O: Open the target block menu.
- P: Toggle mining helper.
- Select one of the supported ore blocks in the menu.

The helper searches for the selected block within a 5-block radius, checks line of sight, aims at the nearest match, and uses the normal client block-destruction controller. The mod is experimental. Test in a private world first. Automated mining may violate multiplayer-server rules.

## Build
Requires JDK 21. Run `gradle build` (or `gradlew.bat build` when using a Gradle wrapper).
The remapped mod JAR is written to `build/libs/`.
