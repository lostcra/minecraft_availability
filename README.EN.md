# Minecraft Availability (Beta v0.1.0)
**Accessibility mod for blind and visually impaired players in Minecraft**

Welcome! This modification is designed to make Minecraft (Java Edition) fully accessible for blind and visually impaired players. It narrates screens, inventories, chests, advancements, in-game chat, statistics, and coordinates/F3 information using text-to-speech synthesizers.

---

## 📋 Requirements

* **Minecraft Version**: Java Edition 26.3 (or compatible 1.21+)
* **Mod Loader**: [Fabric Loader](https://fabricmc.net/) (version 0.16.0 or newer)
* **Required Dependencies (download into `mods` folder)**:
  1. **Fabric API** — [Download on Modrinth / CurseForge](https://modrinth.com/mod/fabric-api) (for version 26.3)
  2. **Cloth Config** *(recommended for GUI)* — [Download on Modrinth](https://modrinth.com/mod/cloth-config)

---

## 🚀 Installation Guide

### 1. Prerequisite (if Fabric is not installed yet)
1. Download the official **Fabric Installer** from [fabricmc.net](https://fabricmc.net/use/installer/).
2. Run the installer, select Minecraft version `26.3`, and click **Install**.
3. Open your Minecraft Launcher and make sure the **Fabric Loader** profile appears in your installations list.

---

### 2. Installing the Mod

#### 🪟 Windows:
1. Download **`minecraft_availability-0.1.0-beta.jar`** from this release.
2. Press `Win + R` on your keyboard.
3. In the Run dialog, paste the following path and press **Enter**:
   ```
   %appdata%\.minecraft\mods
   ```
   *(If the `mods` folder does not exist yet, create it inside `.minecraft`).*
4. Copy the following files into this folder:
   * `minecraft_availability-0.1.0-beta.jar`
   * `fabric-api-....jar`
   * `cloth-config-....jar`
5. Launch Minecraft using the Fabric profile and click **Play**.

#### 🐧 Linux:
1. Download **`minecraft_availability-0.1.0-beta.jar`**.
2. Open terminal or file manager and navigate to your Minecraft directory:
   ```bash
   mkdir -p ~/.minecraft/mods
   cp minecraft_availability-0.1.0-beta.jar ~/.minecraft/mods/
   ```
3. Make sure `fabric-api` and `cloth-config` are also placed in `~/.minecraft/mods/`.
4. Launch the game using your launcher (Official, Prism Launcher, ATLauncher, etc.).

#### 🍎 macOS:
1. Download **`minecraft_availability-0.1.0-beta.jar`**.
2. In Finder, press `Cmd + Shift + G` (Go to folder).
3. Enter:
   ```
   ~/Library/Application Support/minecraft/mods
   ```
   *(Create the `mods` folder if it doesn't exist).*
4. Copy `minecraft_availability-0.1.0-beta.jar` and `fabric-api` into that folder.
5. Launch the game.

---

## 🎮 Controls & Hotkeys

| Hotkey | Action |
| :--- | :--- |
| **`Ctrl + Alt + S`** | Open Accessibility Settings screen |
| **`Ctrl + Alt + A`** | Master Toggle: Enable / Disable all speech narration |
| **`Ctrl + Alt + C`** | Toggle in-game chat narration |
| **`F3 + Space`** | Speak F3 summary (X, Y, Z coordinates, facing direction, biome, light) |
| **Any key** *(or Ctrl)* | Immediately silence current speech (when interruption option is enabled) |

---

## 🎙️ Speech Synthesizer Setup

The mod works smoothly on all major operating systems:

1. **Windows**:
   * Uses **Windows SAPI** by default (supporting built-in voices like Microsoft Irina, Zira, David, plus installed SAPI 5 voices).
   * External CLI synthesizers (**eSpeak NG**, **RHVoice**, **Piper**) can be detected automatically via the **Auto Detect** button.
2. **Linux**:
   * Uses the built-in **Mojang Narrator** out of the box.
   * You can install `espeak-ng` (`sudo apt install espeak-ng` or distribution equivalent) and click **Auto Detect** or specify `/usr/bin/espeak-ng`.
3. **macOS**:
   * Supports built-in **Mojang Narrator** and system speech command `say` (`/usr/bin/say`).

---

## ❓ Troubleshooting (FAQ)

* **Game crashes on startup**:
  * Verify that Java 25 is installed and selected in your launcher.
  * Ensure compatible `fabric-api` version is present in `.minecraft/mods`.
* **No audio / narration**:
  * Press `Ctrl + Alt + A` to ensure master narration is enabled.
  * Press `Ctrl + Alt + S`, go to the "Synthesizer" tab and press **Test**.
  * If needed, switch the engine to **Mojang Narrator**.
* **Speech rate is too fast / slow**:
  * Open `Ctrl + Alt + S`, switch to the "Speech" tab, and adjust the Rate slider (-10 to +10).

---

## 📄 License

This modification is distributed under the **GNU Affero General Public License v3.0 (AGPLv3)**. See [LICENSE](LICENSE) for details.
