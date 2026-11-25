package com.github.lostcra.mcaccessibility.config;

import com.github.lostcra.mcaccessibility.tts.SpeechEngine;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class TutorialScreen extends Screen {
    private final Screen parent;
    private int currentChapter = 0;
    private boolean spokenOnOpen = false;

    public static class Chapter {
        private final String title;
        private final String shortDesc;
        private final String fullText;
        private final String narration;

        public Chapter(String title, String shortDesc, String fullText, String narration) {
            this.title = title;
            this.shortDesc = shortDesc;
            this.fullText = fullText;
            this.narration = narration;
        }

        public String getTitle() { return title; }
        public String getShortDesc() { return shortDesc; }
        public String getFullText() { return fullText; }
        public String getNarration() { return narration; }
    }

    private final List<Chapter> chapters = new ArrayList<>();

    public TutorialScreen(Screen parent) {
        super(Component.literal("Обучение и быстрый старт"));
        this.parent = parent;
        initChapters();
    }

    private void initChapters() {
        chapters.add(new Chapter(
                "1. Быстрый старт",
                "Обзор возможностей мода",
                "Добро пожаловать в Minecraft Availability!\n\n" +
                "Этот мод делает Minecraft 26.3 полностью доступным для незрячих и слабовидящих игроков:\n" +
                "• Полное озвучивание всех экранов, кнопок, меню и списков.\n" +
                "• Голосовая навигация по инвентарю, сундукам, верстакам и печам.\n" +
                "• Посимвольное и пословное чтение в текстовых полях и чате.\n" +
                "• Озвучивание координат, направления взгляда и биома (F3).\n" +
                "• Поддержка синтезаторов Windows SAPI, eSpeak и eSpeak NG.\n\n" +
                "Используйте кнопки «Предыдущая» и «Следующая» или клавиши Page Up / Page Down для переключения глав.",
                "Глава 1: Быстрый старт. " +
                "Добро пожаловать в Minecraft Availability! Этот мод делает Minecraft полностью доступным для незрячих и слабовидящих игроков. " +
                "Основные возможности: полное озвучивание меню, голосовая навигация по инвентарю и сундукам стрелками, " +
                "посимвольное и пословное чтение в текстовых полях, озвучивание координат и биома по клавише Ctrl+Alt+F, " +
                "а также поддержка синтезаторов SAPI, eSpeak и eSpeak NG. " +
                "Для перехода к следующей главе используйте кнопку Следующая или клавиши Page Down."
        ));

        chapters.add(new Chapter(
                "2. Горячие клавиши",
                "Основные сочетания клавиш мода",
                "Горячие клавиши мода:\n\n" +
                "• Ctrl + Alt + S — открыть меню настроек синтезатора и доступности.\n" +
                "• Ctrl + Alt + A — быстрое включение / выключение всей озвучки игры.\n" +
                "• Ctrl + Alt + C — включение / выключение озвучки сообщений чата.\n" +
                "• Ctrl + Alt + F — озвучить координаты (X, Y, Z), направление взгляда и биом.\n" +
                "• Ctrl + Alt + R — прочитать весь текст текущего окна (предупреждения, прогресс, описания).\n" +
                "• Клавиша Control (Ctrl) — мгновенно остановить речь синтезатора.\n" +
                "• Команда в чате /accessibility config — открыть меню настроек.",
                "Глава 2: Горячие клавиши мода. " +
                "Ctrl + Alt + S — открыть настройки синтезатора и доступности. " +
                "Ctrl + Alt + A — быстрое включение или выключение всей озвучки игры. " +
                "Ctrl + Alt + C — включение или выключение чтения чата. " +
                "Ctrl + Alt + F — озвучить координаты, сторону света и биом. " +
                "Ctrl + Alt + R — прочитать весь текст текущего экрана. " +
                "Клавиша Control — мгновенно остановить речь синтезатора. " +
                "Команда в чате /accessibility config — открыть меню настроек."
        ));

        chapters.add(new Chapter(
                "3. Инвентарь и контейнеры",
                "Навигация по предметам и слотам",
                "Управление в инвентаре, сундуках и верстаках:\n\n" +
                "• Стрелки Вверх, Вниз, Влево, Вправо — перемещение по сетке слотов.\n" +
                "• Синтезатор озвучивает предмет, количество, зачарования и зону слота.\n" +
                "• Tab и Shift + Tab — быстрый переход между зонами (Хотбар, Инвентарь, Крафт, Броня).\n" +
                "• Пробел или Enter — взять предмет или положить в слот.\n" +
                "• Shift + Пробел — быстрое перемещение стака в другой контейнер.",
                "Глава 3: Инвентарь и контейнеры. " +
                "Стрелки Вверх, Вниз, Влево и Вправо перемещают курсор по сетке слотов. " +
                "Синтезатор сообщает название предмета, количество в стаке и тип слота. " +
                "Клавиши Tab и Shift+Tab переключают смысловые зоны: Хотбар, Основной инвентарь, Сетка крафта, Броня. " +
                "Пробел или Enter поднимает предмет или кладет его в слот. " +
                "Shift + Пробел быстро перемещает стак между контейнерами."
        ));

        chapters.add(new Chapter(
                "4. Текстовые поля и чат",
                "Чтение и редактирование текста",
                "Навигация в текстовых полях и строке чата:\n\n" +
                "• Стрелки Влево и Вправо — чтение по одному символу.\n" +
                "• Ctrl + Влево / Вправо — чтение по целым словам.\n" +
                "• Home и End — перемещение в начало / конец с прочтением всей строки.\n" +
                "• Backspace и Delete — удаление символа с его озвучиванием.\n" +
                "• Заглавные буквы — настраиваемая индикация (словом, повышенным тоном или сигналом).",
                "Глава 4: Текстовые поля и чат. " +
                "Стрелки Влево и Вправо читают строку по одному символу. " +
                "Ctrl плюс Влево или Вправо перемещают курсор по целым словам. " +
                "Клавиши Home и End перемещают курсор в начало или конец и читают всю строку целиком. " +
                "Backspace и Delete удаляют символ и озвучивают удалённый знак. " +
                "Индикация заглавных букв настраивается: словом Заглавная, более высоким тоном или звуковым сигналом."
        ));

        chapters.add(new Chapter(
                "5. Синтезаторы и eSpeak NG",
                "Выбор движков и голосов",
                "Синтезаторы речи и выбор голоса:\n\n" +
                "• Поддержка Windows SAPI (Irina, Pavel) и консольных движков.\n" +
                "• В папку Downloads установлен eSpeak NG с качественной русской речью (как в NVDA).\n" +
                "• Экран «Выбор голоса» позволяет искать голоса по названию или языку (ru, en).\n" +
                "• Кнопка «Добавить...» позволяет подключить любой внешний синтезатор или запустить установщик.",
                "Глава 5: Синтезаторы речи и eSpeak NG. " +
                "Мод поддерживает системные голоса Windows SAPI, а также консольные синтезаторы. " +
                "В вашей папке Загрузки подготовлен современный eSpeak NG с качественной русской речью, аналогичный чтецу NVDA. " +
                "В меню доступен экран Выбор голоса с поиском и перелистыванием страниц. " +
                "Через кнопку Добавить можно подключить любой другой синтезатор речи."
        ));

        chapters.add(new Chapter(
                "6. Остановка речи и режимы",
                "Управление чтением и пунктуация",
                "Управление речью и режимы озвучки:\n\n" +
                "• Остановка речи: прерывание длинной речи при нажатии любой клавиши.\n" +
                "• Клавиша Control (Ctrl): всегда мгновенно глушит синтезатор.\n" +
                "• Пунктуация: Выкл (знаки опускаются), Основная (.,?!:;) или Все знаки.\n" +
                "• Чтение смайликов: озвучивание текстовых смайликов и эмодзи словами.",
                "Глава 6: Остановка речи и режимы. " +
                "Опция Остановка речи прерывает текущую фразу синтезатора при нажатии любой клавиши. " +
                "Клавиша Control всегда работает как мгновенная кнопка глушения речи. " +
                "Доступны три режима пунктуации: Выключено, Основная и Все знаки. " +
                "Функция Чтение смайликов автоматически проговаривает смайлики и эмодзи понятными словами."
        ));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        // Title
        addRenderableOnly(new StringWidget(cx - 155, 6, 310, 16, Component.literal("Обучение и быстрый старт"), this.font));

        // Chapter Indicator
        Chapter chapter = chapters.get(currentChapter);
        String indicatorText = "Глава " + (currentChapter + 1) + " из " + chapters.size() + ": " + chapter.getTitle();
        addRenderableOnly(new StringWidget(cx - 155, 24, 310, 14, Component.literal(indicatorText), this.font));

        // Chapter navigation bar: 6 quick chapter buttons (2 rows of 3 buttons)
        int btnWidth = 100;
        int btnHeight = 18;
        int startX = cx - 155;
        int row1Y = 40;
        int row2Y = 60;

        for (int i = 0; i < chapters.size(); i++) {
            final int idx = i;
            int row = i / 3;
            int col = i % 3;
            int bx = startX + col * 105;
            int by = (row == 0) ? row1Y : row2Y;

            String label = (i + 1) + ". " + getShortChapterName(i);
            Button btn = addRenderableWidget(Button.builder(
                    Component.literal(label),
                    b -> selectChapter(idx, true)
            ).bounds(bx, by, btnWidth, btnHeight).build());

            if (i == currentChapter) {
                btn.active = false;
            }
            btn.setTooltip(Tooltip.create(Component.literal(chapters.get(i).getTitle() + "\n" + chapters.get(i).getShortDesc())));
        }

        // Text area: MultiLineTextWidget
        int textY = 82;
        int textWidth = 310;
        MultiLineTextWidget textWidget = addRenderableOnly(new MultiLineTextWidget(
                cx - 155,
                textY,
                Component.literal(chapter.getFullText()),
                this.font
        ).setMaxWidth(textWidth));

        // Bottom Controls
        int bottomY = this.height - 28;
        int bWidth = 72;

        // Prev Button
        Button prevBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.tutorial.prev"),
                b -> previousChapter()
        ).bounds(cx - 155, bottomY, bWidth, 20).build());
        prevBtn.active = (currentChapter > 0);
        prevBtn.setTooltip(Tooltip.create(Component.literal("Перейти к предыдущей главе (Page Up)")));

        // Speak again Button
        Button speakBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.tutorial.speak_again"),
                b -> speakCurrentChapter()
        ).bounds(cx - 78, bottomY, 78, 20).build());
        speakBtn.setTooltip(Tooltip.create(Component.literal("Прослушать текущую главу заново")));

        // Next Button
        Button nextBtn = addRenderableWidget(Button.builder(
                Component.translatable("mcaccessibility.tutorial.next"),
                b -> nextChapter()
        ).bounds(cx + 5, bottomY, bWidth, 20).build());
        nextBtn.active = (currentChapter < chapters.size() - 1);
        nextBtn.setTooltip(Tooltip.create(Component.literal("Перейти к следующей главе (Page Down)")));

        // Done / Back Button
        Button doneBtn = addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                b -> onClose()
        ).bounds(cx + 82, bottomY, 73, 20).build());
        doneBtn.setTooltip(Tooltip.create(Component.literal("Вернуться в меню настроек")));

        if (!spokenOnOpen) {
            spokenOnOpen = true;
            speakCurrentChapter();
        }
    }

    private String getShortChapterName(int index) {
        switch (index) {
            case 0: return "Быстрый старт";
            case 1: return "Клавиши";
            case 2: return "Инвентарь";
            case 3: return "Текст и чат";
            case 4: return "Синтезаторы";
            case 5: return "Режимы речи";
            default: return "Глава " + (index + 1);
        }
    }

    private void selectChapter(int index, boolean speak) {
        if (index >= 0 && index < chapters.size()) {
            this.currentChapter = index;
            rebuildWidgets();
            if (speak) {
                speakCurrentChapter();
            }
        }
    }

    private void previousChapter() {
        if (currentChapter > 0) {
            selectChapter(currentChapter - 1, true);
        }
    }

    private void nextChapter() {
        if (currentChapter < chapters.size() - 1) {
            selectChapter(currentChapter + 1, true);
        }
    }

    private void speakCurrentChapter() {
        Chapter ch = chapters.get(currentChapter);
        SpeechEngine.getInstance().speak(ch.getNarration(), true);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (key == InputConstants.KEY_PAGEUP) {
            previousChapter();
            return true;
        } else if (key == InputConstants.KEY_PAGEDOWN) {
            nextChapter();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        SpeechEngine.getInstance().stop();
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(parent);
        }
    }
}
