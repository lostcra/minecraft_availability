package com.github.lostcra.mcaccessibility.config;

import com.github.lostcra.mcaccessibility.tts.SpeechBackend;
import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VoiceSelectionScreen extends Screen {
    private final Screen parent;
    private final List<String> allVoices = new ArrayList<>();
    private final List<String> filteredVoices = new ArrayList<>();

    private EditBox searchBox;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 8;
    private final List<Button> voiceButtons = new ArrayList<>();
    private final String[] slotVoices = new String[ITEMS_PER_PAGE];
    private Button prevPageBtn;
    private Button nextPageBtn;
    private StringWidget pageIndicator;

    public VoiceSelectionScreen(Screen parent) {
        super(Component.literal("Выбор голоса"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        allVoices.clear();
        AccessibilityConfig config = AccessibilityConfig.get();
        List<String> voices = SpeechEngine.getInstance().getAvailableVoices();
        if (voices != null) {
            allVoices.addAll(voices);
        }
        if (allVoices.isEmpty()) {
            allVoices.add("Default");
        }

        String currentVoice = config.getSelectedVoice();
        allVoices.sort((v1, v2) -> {
            if (v1.equalsIgnoreCase(currentVoice)) return -1;
            if (v2.equalsIgnoreCase(currentVoice)) return 1;

            boolean ru1 = isRussianVoice(v1);
            boolean ru2 = isRussianVoice(v2);
            if (ru1 && !ru2) return -1;
            if (!ru1 && ru2) return 1;

            boolean en1 = isEnglishVoice(v1);
            boolean en2 = isEnglishVoice(v2);
            if (en1 && !en2) return -1;
            if (!en1 && en2) return 1;

            return v1.compareToIgnoreCase(v2);
        });

        int cx = this.width / 2;
        int y = 6;

        SpeechBackend backend = SpeechEngine.getInstance().getActiveBackend(config);
        String backendName = backend != null ? backend.getName() : "Синтезатор";

        addRenderableOnly(new StringWidget(cx - 155, y, 310, 16, Component.literal("Выбор голоса: " + backendName), this.font));
        y += 18;

        addRenderableOnly(new StringWidget(cx - 155, y, 310, 14, Component.literal("Поиск по названию или языку:"), this.font));
        y += 14;
        this.searchBox = addRenderableWidget(new EditBox(this.font, cx - 155, y, 310, 20, Component.literal("Поиск")));
        this.searchBox.setHint(Component.literal("Например: ru, en, Irina, Aleksandr..."));
        this.searchBox.setTooltip(Tooltip.create(Component.literal("Фильтрация списка голосов по названию или языку.")));
        this.searchBox.setResponder(this::onSearchChanged);
        y += 24;

        voiceButtons.clear();
        int startY = y;
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int row = i / 2;
            int col = i % 2;
            int bx = (col == 0) ? (cx - 155) : (cx + 5);
            int by = startY + row * 22;

            final int slot = i;
            Button btn = Button.builder(Component.empty(), b -> {
                String v = slotVoices[slot];
                if (v != null) {
                    selectVoice(v);
                }
            }).bounds(bx, by, 150, 20).build();
            btn.visible = false;
            voiceButtons.add(addRenderableWidget(btn));
        }
        y = startY + 4 * 22 + 4;

        this.prevPageBtn = addRenderableWidget(Button.builder(
                Component.literal("<< Пред."),
                btn -> changePage(-1)
        ).bounds(cx - 155, y, 90, 20).build());
        this.prevPageBtn.setTooltip(Tooltip.create(Component.literal("Предыдущая страница списка голосов.")));

        this.pageIndicator = addRenderableOnly(new StringWidget(cx - 60, y, 120, 20, Component.literal("1 / 1"), this.font));

        this.nextPageBtn = addRenderableWidget(Button.builder(
                Component.literal("След. >>"),
                btn -> changePage(1)
        ).bounds(cx + 65, y, 90, 20).build());
        this.nextPageBtn.setTooltip(Tooltip.create(Component.literal("Следующая страница списка голосов.")));
        y += 24;

        addRenderableWidget(Button.builder(
                Component.literal("Проверить голос"),
                btn -> SpeechEngine.getInstance().testSpeech()
        ).bounds(cx - 155, y, 150, 20).build());

        addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                btn -> onClose()
        ).bounds(cx + 5, y, 150, 20).build());

        updateFilteredList("");
    }

    private void onSearchChanged(String query) {
        this.currentPage = 0;
        updateFilteredList(query);
    }

    private void updateFilteredList(String query) {
        filteredVoices.clear();
        String q = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        for (String v : allVoices) {
            if (q.isEmpty() || v.toLowerCase(Locale.ROOT).contains(q) || getVoiceDescription(v).toLowerCase(Locale.ROOT).contains(q)) {
                filteredVoices.add(v);
            }
        }
        refreshPage();
    }

    private void changePage(int delta) {
        int maxPage = Math.max(0, (filteredVoices.size() - 1) / ITEMS_PER_PAGE);
        int newPage = currentPage + delta;
        if (newPage >= 0 && newPage <= maxPage) {
            currentPage = newPage;
            refreshPage();
            SpeechEngine.getInstance().speak("Страница " + (currentPage + 1) + " из " + (maxPage + 1), true);
        }
    }

    private void refreshPage() {
        int total = filteredVoices.size();
        int maxPage = Math.max(0, (total - 1) / ITEMS_PER_PAGE);
        if (currentPage > maxPage) currentPage = maxPage;

        this.pageIndicator.setMessage(Component.literal((currentPage + 1) + " / " + Math.max(1, maxPage + 1) + " (" + total + ")"));
        this.prevPageBtn.active = currentPage > 0;
        this.nextPageBtn.active = currentPage < maxPage;

        AccessibilityConfig config = AccessibilityConfig.get();
        String activeVoice = config.getSelectedVoice();

        int startIndex = currentPage * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            Button btn = voiceButtons.get(i);
            int idx = startIndex + i;
            if (idx < total) {
                String voice = filteredVoices.get(idx);
                slotVoices[i] = voice;
                boolean isSelected = voice.equalsIgnoreCase(activeVoice);
                String prefix = isSelected ? "✓ " : "";
                btn.setMessage(Component.literal(prefix + truncate(voice, 15)));
                btn.visible = true;
                btn.active = true;
                btn.setTooltip(Tooltip.create(Component.literal("Голос: " + voice + " (" + getVoiceDescription(voice) + ")" + (isSelected ? " [Выбран]" : ""))));
            } else {
                slotVoices[i] = null;
                btn.visible = false;
            }
        }
    }

    private void selectVoice(String voice) {
        AccessibilityConfig config = AccessibilityConfig.get();
        config.setSelectedVoice(voice);
        config.save();
        refreshPage();
        SpeechEngine.getInstance().speak("Выбран голос: " + voice + ". " + getVoiceDescription(voice), true);
    }

    private static boolean isRussianVoice(String v) {
        String s = v.toLowerCase(Locale.ROOT);
        return s.equals("ru") || s.contains("russian") || s.contains("rus") || s.contains("irina") || s.contains("aleksandr") || s.contains("elena") || s.contains("anna") || s.contains("pavel");
    }

    private static boolean isEnglishVoice(String v) {
        String s = v.toLowerCase(Locale.ROOT);
        return s.startsWith("en") || s.contains("english") || s.contains("david") || s.contains("zira") || s.contains("mark");
    }

    private static String getVoiceDescription(String v) {
        if (isRussianVoice(v)) return "Русский";
        if (isEnglishVoice(v)) return "Английский";
        if (v.startsWith("de") || v.contains("german")) return "Немецкий";
        if (v.startsWith("fr") || v.contains("french")) return "Французский";
        if (v.startsWith("es") || v.contains("spanish")) return "Испанский";
        if (v.startsWith("it") || v.contains("italian")) return "Итальянский";
        if (v.startsWith("uk") || v.contains("ukrainian")) return "Украинский";
        if (v.startsWith("pl") || v.contains("polish")) return "Польский";
        if (v.equalsIgnoreCase("Default")) return "По умолчанию";
        return "Голос";
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max - 1) + "…";
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(parent);
        }
    }
}
