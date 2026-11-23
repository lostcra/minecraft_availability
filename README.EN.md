# Minecraft Availability

An accessibility mod for Minecraft (Fabric Loader, version 26.3) created specifically to empower blind and visually impaired players with full voiceover, keyboard navigation, and audio assistance.

---

## 🌟 Key Features

### 1. Global Fast-Toggle Hotkeys
* **`Ctrl + Alt + A`** — Global toggle for game and UI voiceover with spoken audio feedback ("Accessibility voiceover enabled" / "Accessibility voiceover disabled").
* **`Ctrl + Alt + C`** — Independent toggle for chat reading ("Chat narration enabled" / "Chat narration disabled").
* **`Ctrl + Alt + S`** — Open the Accessibility Settings GUI.

---

### 2. Comprehensive UI Voiceover
* **Screen and Menu Narration**: Whenever any screen opens (Title Screen, Pause Menu, Options, Inventories, etc.), the mod automatically announces the screen title.
* **Focused Widget Narration**: Buttons, checkboxes, sliders, and text fields are narrated as you focus them with keyboard navigation or hover over them.

---

### 3. Inventory and Container Accessibility
* **Slot Announcements**: Speaks slot index, item name, stack count, and carried item on cursor.
* **Detailed Tooltips**: Reads enchantments, durability/damage values, potion effects, and lore.
* **Keyboard Slot Navigation**:
  * **`Left` / `Right` Arrow**: Move to previous / next slot.
  * **`Up` / `Down` Arrow**: Move across inventory rows (9 slots per row).
  * **`Tab` / `Shift + Tab`**: Cycle through slots sequentially.
  * **`Space` / `Enter`**: Pick up or place items with voice confirmation ("Picked up: [item]", "Placed: [item]").
  * **`Shift + Space`**: Quick Move item to/from container.
  * **Key `R`**: Repeat announcement of the currently selected slot.

---

### 4. Advancements Screen Accessibility
* **Tab Navigation**:
  * **`Tab`** / **`Shift + Tab`** switches between advancement tabs with spoken tab titles.
* **Keyboard Tree Navigation**:
  * **`Right` Arrow**: Navigate to child advancement.
  * **`Left` Arrow**: Navigate back to parent advancement.
  * **`Up` / `Down` Arrow**: Navigate between sibling nodes in the same branch.
* **Detailed Advancement Information**:
  * Title.
  * Category/Type: Task, Goal, or Challenge.
  * Completion status: Completed or Incomplete (with progress percentage if applicable).
  * Description.
  * **`Space` / `Enter`**: Repeat announcement for the active advancement.

---

### 5. Customizable Speech Synthesizer & Settings GUI
* **TTS Engine Selection**:
  * `MOJANG`: Built-in Minecraft narrator (cross-platform).
  * `WINDOWS_SAPI`: System Windows SAPI 5 / OneCore speech synthesizer supporting all installed Windows voices (e.g., Microsoft Irina, Microsoft Zira, RHVoice).
* **Voice Selection**: Dropdown list of system-installed voices.
* **Speech Rate**: Configurable from -10 to +10.
* **Speech Volume**: Configurable from 0 to 100%.
* **Test Voice**: Button to preview speech synthesis.
* Settings are saved to `config/minecraft_availability.json`.

---

### 6. Client Commands
* `/accessibility` or `/mcaccess` — Open the settings GUI.
* `/accessibility toggle` — Toggle master voiceover.
* `/accessibility chat` — Toggle chat reader.
* `/accessibility rate <-10..10>` — Set speech rate.
* `/accessibility volume <0..100>` — Set speech volume.
* `/accessibility test` — Speak a test sentence.

---

## 🛠️ Installation & Building

### Installation
1. Install [Fabric Loader](https://fabricmc.net/) for Minecraft **26.3**.
2. Place the following files into `.minecraft/mods`:
   * `fabric-api.jar`
   * `cloth-config-fabric-26.3.159.jar` (for configuration GUI)
   * `minecraft_availability-1.0.0.jar`
3. Launch Minecraft.

### Building from Source
Run the compilation script in PowerShell:
```powershell
.\compile.ps1
```
To compile and automatically copy the resulting jar into `.minecraft/mods`:
```powershell
.\compile.ps1 -InstallToMods
```
The compiled mod JAR will be placed at `build/libs/minecraft_availability-1.0.0.jar`.

---

## 📄 License
This project is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**. See the `LICENSE` file for details.
