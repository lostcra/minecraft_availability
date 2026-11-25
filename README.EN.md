# Minecraft Availability

**Minecraft Availability** is an accessibility mod for Minecraft (Fabric Loader, version **26.3**) designed specifically to empower blind and visually impaired players with comprehensive voiceover, keyboard navigation, and audio accessibility.

It transforms Minecraft into a fully narrated and intuitive gaming environment supporting built-in and external speech synthesizers, character-by-character text field navigation, audio cues, and seamless keyboard controls.

---

## 🌟 Key Features

### 1. Global Fast-Toggle Hotkeys
* **`Ctrl + Alt + A`** — Global toggle for game and UI voiceover with spoken audio feedback.
* **`Ctrl + Alt + C`** — Independent toggle for in-game chat reading.
* **`Ctrl + Alt + S`** — Open the Accessibility Settings GUI.
* **`Ctrl + Alt + F`** (and **`F3`** key) — Announce coordinates, facing direction, biome, light level, targeted block/entity, and day.
* **`Control` key** (or **any key** when *Speech Interruption* is enabled) — **Instant speech silence/stop** (universal screen reader behavior like NVDA/JAWS: immediately silences ongoing speech).

---

### 2. Character-by-Character Navigation in Text Fields (`EditBox`)
Input fields in Minecraft now work just like accessible desktop text editors:
* **`Left` / `Right` Arrow keys**: Move the cursor with clear speech announcement for each character. Punctuation marks and path symbols (`\`, `/`, `:`, `.`, `-`, `_`, space) are spoken with human-readable names (*"backslash"*, *"colon"*, *"dot"*), preventing path input mistakes.
* **Line Boundaries**: Announces *"Start of line"* or *"End of line"*.
* **`Ctrl + Left` / `Ctrl + Right`**: Move by word with spoken word feedback.
* **`Up` / `Down` Arrow keys**: Announce the entire line.
* **`Home` / `End`**: Jump to the start or end of the line.
* **`Backspace` / `Delete`**: Announce the deleted character (*"Deleted: [char]"*).
* **Text Typing**: Instantly announces each typed character or symbol.
* Eliminates repetitive re-reading of the entire text box from the beginning on every cursor movement.

---

### 3. Voice Selection Menu (`VoiceSelectionScreen`)
The Voice button opens a dedicated voice picker menu:
* **Smart Sorting**: Active voice is listed first (marked with `✓`), Russian voices (`ru`, `russian`, `Aleksandr`, `Elena`, `Irina`) are placed at the very top, followed by English voices, then all other languages alphabetically.
* **Search Field**: Instantly filter available voices by name or language code (e.g. typing `ru`, `en`, `Irina`, `David`).
* **Pagination**: `<< Prev` and `Next >>` buttons for navigating large voice libraries (such as eSpeak's 80+ voices).
* **Test Voice**: A *"Test Voice"* button to sample any voice immediately.

---

### 4. Speech Synthesizers (TTS) & Smart Auto-Discovery
Supports three speech synthesizer backends:
1. **Windows SAPI (System)** — uses Windows system voices (Microsoft Irina, Zira, David, plus any installed SAPI 5 voices).
2. **Mojang Narrator** — Minecraft's built-in cross-platform narrator.
3. **Custom Synthesizers** (eSpeak, eSpeak NG, RHVoice, Piper, etc.):
   * **"Auto-Detect" Button**: Scans the Windows Registry (`HKLM/HKCU ... CurrentVersion\Uninstall`) and standard directories to find installed synthesizers in 1 click.
   * **Folder Support**: Point to a program folder (e.g. `C:\Program Files (x86)\eSpeak`); the mod automatically locates the executable inside subfolders.
   * **Installation Wizard**: Select an installer (`setup.exe`) and click *"Install..."*; the mod launches the installer, waits for completion, and automatically retrieves the newly registered install path from the Windows Registry.
   * **"Browse..." Button**: Opens standard Windows Explorer file chooser dialog.
   * **Installer Protection**: Blocks accidental execution of `setup.exe` as a TTS speaking process.

---

### 5. Advanced Speech Settings
* **Speech Interruption on Key Press**: Toggle instant silencing of ongoing speech whenever any key is pressed.
* **Punctuation Mode**:
  * *Off* — punctuation marks are skipped.
  * *Basic* — announces periods, commas, question marks, exclamation marks, and colons.
  * *All* — announces all special characters (brackets, slashes, hashes, asterisks, etc.).
* **Emoji Reading**: Automatically translates text emoticons (`:)`, `:D`, `XD`, `<3`, `¯\_(ツ)_/¯`) and Unicode emojis (😀, 😂, 🔥, ❤️, 👍) into descriptive words.
* **Capital Letters**:
  * *Off* — standard pitch without emphasis.
  * *Word* — speaks *"Capital [Letter]"*.
  * *Pitch* — raises voice pitch for capitals (native `-k 20` in eSpeak).
  * *Beep* — plays an audio click/beep on capital letters.
* **Speech Rate and Volume**: Smooth sliders from -10 to +10 and 0% to 100%.

---

### 6. Inventory & Menu Accessibility
* **Inventory**: Arrow keys (`Left/Right` for slots, `Up/Down` for rows of 9), `Space/Enter` to pick up/place items, `Shift + Space` for quick move, `R` to repeat slot description.
* **Advancements**: `Tab / Shift+Tab` for switching tabs, Arrow keys for tree navigation (`Right` child, `Left` parent, `Up/Down` siblings), status and progress narration.
* **F3 Debug Menu**: Announces X Y Z coordinates, facing direction, biome, light level, targeted block/entity, and world day.

---

### 7. In-Game Chat Commands
* `/accessibility` or `/mcaccess` — Open accessibility settings menu.
* `/accessibility toggle` — Toggle voiceover on/off.
* `/accessibility chat` — Toggle chat narration on/off.
* `/accessibility rate <-10..10>` — Set speech rate.
* `/accessibility volume <0..100>` — Set speech volume.
* `/accessibility test` — Speak a test sentence.

---

## 💻 Developer Guide (Architecture & Building)

### Codebase Organization
Source code is located under `src/main/java/com/github/lostcra/mcaccessibility`:
* **`config/`**:
  * `AccessibilityConfig.java` — configuration model (saved to `.minecraft/config/minecraft_availability.json`).
  * `AccessibilitySettingsScreen.java` — main GUI settings screen.
  * `VoiceSelectionScreen.java` — voice browser with search and pagination.
  * `AddSynthesizerScreen.java` — external synthesizer setup screen.
  * `SynthesizerFinder.java` — registry query (`reg query`), folder scanner, and installer capture.
* **`mixin/`**:
  * `MixinKeyboardHandler.java` — hotkey handling and key press speech interruption.
  * `MixinScreen.java` — screen narration and suppression of redundant speech.
  * `MixinEditBox.java` — intercept text input, cursor movement, and deletion.
  * `MixinAbstractContainerScreen.java` — inventory slot narration and keyboard navigation.
  * `MixinAdvancementsScreen.java` — advancements tree navigation.
  * `MixinGameNarrator.java` — integration with Minecraft's narrator subsystem.
* **`navigation/`**:
  * `EditBoxNarrationHelper.java` — character-by-character, word, and line narration.
  * `InventoryNavigator.java` — inventory cursor and slot traversal logic.
  * `AdvancementNavigator.java` — advancement tree traversal logic.
  * `F3NarrationHelper.java` — F3 debug data extraction and formatting.
* **`tts/`**:
  * `SpeechEngine.java` — speech dispatcher and backend manager.
  * `SpeechTextProcessor.java` — emoji, emoticon, and punctuation translations.
  * `WindowsSapiBackend.java` — Windows SAPI through background PowerShell/System.Speech.
  * `CustomCommandBackend.java` — CLI synthesizers (eSpeak, RHVoice, Piper).
  * `MojangNarratorBackend.java` — fallback narrator backend.

### Building
The repository includes cross-platform build scripts with automatic discovery of Java 25 and `.minecraft` libraries:

**On Windows (PowerShell):**
```powershell
# Build mod JAR (minecraft_availability-0.2.0-beta.jar):
.\compile.ps1

# Build and copy directly into .minecraft/mods:
.\compile.ps1 -InstallToMods
```

**On Linux / macOS (Bash):**
```bash
# Build JAR file:
chmod +x ./compile.sh
./compile.sh

# Build and install to mods folder:
./compile.sh --install
```
