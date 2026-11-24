# Animations in Action: Hypixel

**Animations in Action: Hypixel** is a client-side Fabric mod for **Minecraft 26.3** that turns Hypixel SkyBlock rare drops into on-screen spinning item reveal animations with rarity-colored light rays.

---

## Project Overview

In Hypixel SkyBlock, rare drops often appear as a single line of chat text that is easy to miss during gameplay. 
**Animations in Action: Hypixel** plays a customizable visual reveal whenever a supported rare drop occurs.

* **Target Version & Loader:** Minecraft 26.3 on Fabric (`>=0.19.5`)
* **Environment:** Client-side only (purely visual overlay; sends no extra packets and performs no gameplay automation)
* **Source Repository:** https://github.com/imCalculus/Animations-in-Action-Hypixel

---

## Installation Instructions

### Prerequisites
* **Minecraft:** 26.3
* **Java:** Java 25 or newer
* **Fabric Loader:** `>=0.19.5` (https://fabricmc.net/use/installer/)
* **Fabric API:** Compatible 26.3 release (https://modrinth.com/mod/fabric-api)

### Install via Launcher
1. Install **Fabric Loader** for **Minecraft 26.3** in your launcher (Vanilla Launcher, Modrinth App, CurseForge App, or Prism Launcher).
2. Download **Fabric API** (26.3) and place it in your `.minecraft/mods` directory.
3. Place the compiled `animationinaction` `.jar` file into the same `.minecraft/mods` directory.
4. Launch the **Fabric 26.3** profile and connect to `mc.hypixel.net`.

### Build from Source
git clone https://github.com/imCalculus/Animations-in-Action-Hypixel.git
cd Animations-in-Action-Hypixel
./gradlew build

*(On Windows, run `gradlew.bat build` instead. The compiled `.jar` is generated in `build/libs/`.)*

---

## Usage and Features

### Key Features
* **Rare-Drop Reveal Overlay:** Displays a spinning 3D item preview surrounded by rarity-colored rays and a animation when a supported rare drop triggers.
* **Per-Drop Toggles:** Enable or disable animations individually for specific Hypixel SkyBlock drops.
* **Live Preview & Config GUI:** Adjust animation behavior and preview drop reveals directly in-game without restarting Minecraft.

### Quick Start
1. Log into **Hypixel SkyBlock** (`mc.hypixel.net`).
2. Run `/aia` in chat to open the configuration menu.
3. Toggle your preferred drops on or off, tweak animation settings, and trigger a preview inside the GUI to test the reveal overlay immediately.

---

## Modpack Specifics

* **Client-Only Integration:** **Animations in Action: Hypixel** is a standalone client-side mod. It does not need to be installed on a server and requires no server startup configuration.
* **Modpack Inclusion Policy:** Modpack creators on **Modrinth** and **CurseForge** are permitted to include this mod in public or private modpacks, provided the modpack references or links directly to the official project page without redistributing the `.jar` file separately.
* **Bug Reports & Support:** Report issues or suggest new SkyBlock drops at https://github.com/imCalculus/Animations-in-Action-Hypixel/issues

---

## License and Credits

### License
**All Rights Reserved** — Copyright © 2026 **Artic** & **Calculus**.

See the `LICENSE` file in this repository for full terms. No part of this software or source code may be copied, modified, distributed, or reuploaded without explicit written permission from the authors, except for inclusion in official Modrinth or CurseForge modpacks as outlined above.

### Authors & Acknowledgments
* **Artic** — Co-Author
* **Calculus** — Co-Author
* Built using Fabric (https://fabricmc.net/) and Fabric API (https://github.com/FabricMC/fabric).
