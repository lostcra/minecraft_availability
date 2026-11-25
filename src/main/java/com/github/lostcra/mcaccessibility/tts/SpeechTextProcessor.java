package com.github.lostcra.mcaccessibility.tts;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SpeechTextProcessor {

    private static final Map<String, String> ASCII_EMOTICONS;
    private static final Map<String, String> UNICODE_EMOJIS;
    private static final Map<String, String> EMOJIS;

    static {
        Map<String, String> ascii = new LinkedHashMap<>();
        ascii.put(":-)", "смайлик улыбка");
        ascii.put(":)", "смайлик улыбка");
        ascii.put("=)", "смайлик улыбка");
        ascii.put(":-(", "смайлик грусть");
        ascii.put(":(", "смайлик грусть");
        ascii.put("=(", "смайлик грусть");
        ascii.put(":-D", "смайлик смех");
        ascii.put(":D", "смайлик смех");
        ascii.put("=D", "смайлик смех");
        ascii.put("xD", "смайлик громкий смех");
        ascii.put("XD", "смайлик громкий смех");
        ascii.put(";-)", "смайлик подмигивание");
        ascii.put(";)", "смайлик подмигивание");
        ascii.put(":-P", "смайлик показывает язык");
        ascii.put(":P", "смайлик показывает язык");
        ascii.put(":-p", "смайлик показывает язык");
        ascii.put(":p", "смайлик показывает язык");
        ascii.put(":-O", "смайлик удивление");
        ascii.put(":O", "смайлик удивление");
        ascii.put(":-o", "смайлик удивление");
        ascii.put(":o", "смайлик удивление");
        ascii.put(":-/", "смайлик сомнение");
        ascii.put(":/", "смайлик сомнение");
        ascii.put("<3", "сердечко");
        ascii.put("¯\\_(ツ)_/¯", "пожимает плечами");
        ASCII_EMOTICONS = Collections.unmodifiableMap(ascii);

        Map<String, String> map = new LinkedHashMap<>();

        // Faces & Emotions
        map.put("😀", "улыбающееся лицо");
        map.put("😃", "радостное лицо");
        map.put("😄", "весёлое лицо");
        map.put("😁", "сияющее лицо");
        map.put("😆", "смех");
        map.put("😅", "неловкий смех");
        map.put("😂", "слёзы радости");
        map.put("🤣", "катается со смеху");
        map.put("😊", "довольное лицо");
        map.put("🙂", "лёгкая улыбка");
        map.put("🙃", "перевёрнутое лицо");
        map.put("😉", "подмигивающее лицо");
        map.put("😍", "влюблённое лицо");
        map.put("🥰", "лицо с сердечками");
        map.put("😘", "воздушный поцелуй");
        map.put("😋", "лицо с языком");
        map.put("😎", "крутое лицо в очках");
        map.put("🤔", "задумчивое лицо");
        map.put("😐", "нейтральное лицо");
        map.put("😑", "безразличное лицо");
        map.put("🙄", "закатывает глаза");
        map.put("😏", "ухмылка");
        map.put("😣", "страдающее лицо");
        map.put("😥", "облегчение со слезой");
        map.put("😮", "открытый рот");
        map.put("🤐", "рот на замке");
        map.put("😯", "изумление");
        map.put("😪", "сонное лицо");
        map.put("😫", "усталое лицо");
        map.put("🥱", "зевающее лицо");
        map.put("😴", "спящее лицо");
        map.put("😌", "умиротворённое лицо");
        map.put("😛", "показывает язык");
        map.put("😜", "подмигивает с языком");
        map.put("😝", "зажмурился с языком");
        map.put("🤤", "пускает слюни");
        map.put("😒", "недовольное лицо");
        map.put("😓", "пот на лбу");
        map.put("😔", "задумчивая грусть");
        map.put("😕", "озадаченное лицо");
        map.put("🤑", "лицо с деньгами");
        map.put("😲", "сильное удивление");
        map.put("☹️", "хмурое лицо");
        map.put("🙁", "грустное лицо");
        map.put("😖", "растерянное лицо");
        map.put("😞", "разочарованное лицо");
        map.put("😤", "триумф или пар из носа");
        map.put("😢", "плачущее лицо");
        map.put("😭", "громко плачет");
        map.put("😦", "испуганное лицо");
        map.put("😧", "мучительное лицо");
        map.put("😨", "страх");
        map.put("😩", "измученное лицо");
        map.put("🤯", "взрыв мозга");
        map.put("😬", "скривился");
        map.put("😰", "тревога с потом");
        map.put("😱", "крик ужаса");
        map.put("🥵", "жарко");
        map.put("🥶", "холодно");
        map.put("😳", "покраснел");
        map.put("🤪", "дурашливое лицо");
        map.put("😵", "головокружение");
        map.put("😡", "сердитое лицо");
        map.put("😠", "злое лицо");
        map.put("🤬", "ругается");
        map.put("😷", "лицо в маске");
        map.put("🤒", "с градусником");
        map.put("🤕", "с повязкой");
        map.put("🤢", "тошнота");
        map.put("🤮", "рвота");
        map.put("🤧", "чихает");
        map.put("😇", "нимб");
        map.put("🤠", "ковбой");
        map.put("🥳", "праздник");
        map.put("🥺", "умоляющие глаза");
        map.put("🤫", "тссс");
        map.put("🤭", "хихикает в кулак");
        map.put("🧐", "лицо с моноклем");
        map.put("🤓", "ботаник");
        map.put("😈", "чертёнок");
        map.put("👿", "злой демон");
        map.put("🤡", "клоун");
        map.put("💩", "какашка");
        map.put("👻", "привидение");
        map.put("💀", "череп");
        map.put("☠️", "череп и кости");
        map.put("👽", "пришелец");
        map.put("👾", "монстрик");
        map.put("🤖", "робот");
        map.put("🎃", "тыква хэллоуин");

        // Gestures & Body
        map.put("👍", "палец вверх");
        map.put("👎", "палец вниз");
        map.put("👌", "окей");
        map.put("✌️", "знак победы");
        map.put("🤞", "скрещенные пальцы");
        map.put("🤟", "люблю тебя");
        map.put("🤘", "рокерская коза");
        map.put("🤙", "позвони мне");
        map.put("👈", "указывает влево");
        map.put("👉", "указывает вправо");
        map.put("👆", "указывает вверх");
        map.put("👇", "указывает вниз");
        map.put("☝️", "внимание вверх");
        map.put("✋", "поднятая ладонь");
        map.put("🤚", "тыльная сторона ладони");
        map.put("🖐️", "ладонь с пальцами");
        map.put("🖖", "вулканский салют");
        map.put("👋", "машет рукой");
        map.put("👏", "аплодисменты");
        map.put("🙌", "руки вверх от радости");
        map.put("👐", "открытые ладони");
        map.put("🤲", "ладони вместе");
        map.put("🤝", "рукопожатие");
        map.put("🙏", "сложенные руки");
        map.put("✍️", "пишущая рука");
        map.put("💪", "бицепс");
        map.put("👀", "глаза");

        // Hearts & Symbols
        map.put("❤️", "красное сердце");
        map.put("🧡", "оранжевое сердце");
        map.put("💛", "жёлтое сердце");
        map.put("💚", "зелёное сердце");
        map.put("💙", "синее сердце");
        map.put("💜", "фиолетовое сердце");
        map.put("🖤", "чёрное сердце");
        map.put("🤍", "белое сердце");
        map.put("🤎", "коричневое сердце");
        map.put("💔", "разбитое сердце");
        map.put("❣️", "сердце восклицание");
        map.put("💕", "два сердца");
        map.put("💞", "кружащиеся сердца");
        map.put("💓", "бьющееся сердце");
        map.put("💗", "растущее сердце");
        map.put("💖", "сверкающее сердце");
        map.put("💘", "сердце со стрелой");
        map.put("💝", "сердце с лентой");
        map.put("🔥", "огонь");
        map.put("✨", "искры");
        map.put("🎉", "хлопушка");
        map.put("🎊", "конфетти");
        map.put("🎁", "подарок");
        map.put("🏆", "кубок");
        map.put("💎", "алмаз");
        map.put("⚡", "молния");
        map.put("⭐", "звезда");
        map.put("🌟", "сияющая звезда");
        map.put("💥", "взрыв");
        map.put("🔔", "колокольчик");
        map.put("🎵", "нота");
        map.put("🎶", "ноты");
        map.put("🎮", "геймпад");
        map.put("💯", "сто очков");
        map.put("⚠️", "предупреждение");
        map.put("⛔", "вход запрещен");
        map.put("🚫", "запрещено");
        map.put("✅", "галочка");
        map.put("❌", "крестик");
        map.put("❓", "вопросительный знак");
        map.put("❗", "восклицательный знак");

        UNICODE_EMOJIS = Collections.unmodifiableMap(map);
        Map<String, String> combined = new LinkedHashMap<>(ASCII_EMOTICONS);
        combined.putAll(UNICODE_EMOJIS);
        EMOJIS = Collections.unmodifiableMap(combined);
    }

    public static String translateEmojis(String text) {
        if (text == null || text.isEmpty()) return text;

        boolean hasAsciiTrigger = false;
        boolean hasUnicodeTrigger = false;
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (c >= 0x2000 || Character.isHighSurrogate(c)) {
                hasUnicodeTrigger = true;
                if (c == '¯') hasAsciiTrigger = true;
            } else if ((c == ':' || c == ';' || c == '=' || c == '<' || c == 'x' || c == 'X')
                    && i + 1 < len) {
                char next = text.charAt(i + 1);
                if (next != ' ' && next != '\t' && next != '\n') {
                    hasAsciiTrigger = true;
                }
            }
            if (hasAsciiTrigger && hasUnicodeTrigger) break;
        }

        if (!hasAsciiTrigger && !hasUnicodeTrigger) {
            return text;
        }

        String result = text;
        if (hasAsciiTrigger) {
            for (Map.Entry<String, String> entry : ASCII_EMOTICONS.entrySet()) {
                if (result.contains(entry.getKey())) {
                    result = result.replace(entry.getKey(), " " + entry.getValue() + " ");
                }
            }
        }
        if (hasUnicodeTrigger) {
            for (Map.Entry<String, String> entry : UNICODE_EMOJIS.entrySet()) {
                if (result.contains(entry.getKey())) {
                    result = result.replace(entry.getKey(), " " + entry.getValue() + " ");
                }
            }
        }
        return result;
    }

    public static String processPunctuation(String text, String mode) {
        if (text == null || text.isEmpty() || mode == null || "NONE".equalsIgnoreCase(mode)) {
            return text;
        }

        StringBuilder sb = new StringBuilder(text.length() + 16);
        boolean all = "ALL".equalsIgnoreCase(mode);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String word = getPunctuationWord(c, all);
            if (word != null) {
                sb.append(' ').append(word).append(' ');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String getPunctuationWord(char c, boolean all) {
        switch (c) {
            case '.': return "точка";
            case ',': return "запятая";
            case '!': return "восклицательный знак";
            case '?': return "вопросительный знак";
            case ':': return "двоеточие";
            case ';': return "точка с запятой";
            case '-': return "дефис";
            default:
                if (!all) return null;
                switch (c) {
                    case '_': return "подчёркивание";
                    case '/': return "слэш";
                    case '\\': return "обратный слэш";
                    case '\"': return "кавычка";
                    case '\'': return "апостроф";
                    case '(': return "открывающая скобка";
                    case ')': return "закрывающая скобка";
                    case '[': return "открывающая квадратная скобка";
                    case ']': return "закрывающая квадратная скобка";
                    case '{': return "открывающая фигурная скобка";
                    case '}': return "закрывающая фигурная скобка";
                    case '<': return "меньше";
                    case '>': return "больше";
                    case '=': return "равно";
                    case '+': return "плюс";
                    case '*': return "звёздочка";
                    case '%': return "процент";
                    case '$': return "доллар";
                    case '#': return "решётка";
                    case '@': return "собака";
                    case '&': return "амперсанд";
                    case '^': return "крышка";
                    case '|': return "вертикальная черта";
                    case '~': return "тильда";
                    case '`': return "обратный апостроф";
                    default: return null;
                }
        }
    }

    public static String narrateCharacter(char c, String capitalMode) {
        switch (c) {
            case ' ': return "Пробел";
            case '\t': return "Табуляция";
            case '\n': return "Новая строка";
            case '.': return "Точка";
            case ',': return "Запятая";
            case '!': return "Восклицательный знак";
            case '?': return "Вопросительный знак";
            case ':': return "Двоеточие";
            case ';': return "Точка с запятой";
            case '-': return "Дефис";
            case '_': return "Подчёркивание";
            case '/': return "Слэш";
            case '\\': return "Обратный слэш";
            case '\"': return "Кавычка";
            case '\'': return "Апостроф";
            case '(': return "Открывающая скобка";
            case ')': return "Закрывающая скобка";
            case '[': return "Открывающая квадратная скобка";
            case ']': return "Закрывающая квадратная скобка";
            case '{': return "Открывающая фигурная скобка";
            case '}': return "Закрывающая фигурная скобка";
            case '<': return "Меньше";
            case '>': return "Больше";
            case '=': return "Равно";
            case '+': return "Плюс";
            case '*': return "Звёздочка";
            case '%': return "Процент";
            case '$': return "Доллар";
            case '#': return "Решётка";
            case '@': return "Собака";
            case '&': return "Амперсанд";
            case '^': return "Крышка";
            case '|': return "Вертикальная черта";
            case '~': return "Тильда";
            case '`': return "Обратный апостроф";
            default:
                if (Character.isLetter(c) && Character.isUpperCase(c)) {
                    if ("WORD".equalsIgnoreCase(capitalMode)) {
                        return "Заглавная " + c;
                    }
                }
                return String.valueOf(c);
        }
    }
}
