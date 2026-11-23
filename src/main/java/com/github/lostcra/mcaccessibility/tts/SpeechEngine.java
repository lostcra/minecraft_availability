package com.github.lostcra.mcaccessibility.tts;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.config.CustomSynthesizerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpeechEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger("MinecraftAvailability/TTS");
    private static SpeechEngine instance;

    private final MojangNarratorBackend mojangBackend;
    private final WindowsSapiBackend sapiBackend;
    private final Map<String, CustomCommandBackend> customBackends = new LinkedHashMap<>();

    private SpeechEngine() {
        this.mojangBackend = new MojangNarratorBackend();
        this.sapiBackend = new WindowsSapiBackend(mojangBackend);
        reloadCustomBackends();
        LOGGER.info("SpeechEngine initialized. SAPI: {}, Mojang: {}",
                sapiBackend.isAvailable(), mojangBackend.isAvailable());
    }

    public static synchronized SpeechEngine getInstance() {
        if (instance == null) {
            instance = new SpeechEngine();
        }
        return instance;
    }

    public synchronized void reloadCustomBackends() {
        customBackends.clear();
        AccessibilityConfig config = AccessibilityConfig.get();
        for (CustomSynthesizerConfig cfg : config.getCustomSynthesizers()) {
            if (cfg != null && cfg.getId() != null) {
                customBackends.put(cfg.getId().toUpperCase(), new CustomCommandBackend(cfg));
            }
        }
    }

    public synchronized List<SpeechBackend> getAvailableBackends() {
        List<SpeechBackend> list = new ArrayList<>();
        if (sapiBackend.isAvailable()) {
            list.add(sapiBackend);
        }
        for (CustomCommandBackend cb : customBackends.values()) {
            if (cb.isAvailable()) {
                list.add(cb);
            }
        }
        if (mojangBackend.isAvailable() || list.isEmpty()) {
            list.add(mojangBackend);
        }
        return list;
    }

    public synchronized SpeechBackend getBackend(String id) {
        if (id == null) return null;
        if ("WINDOWS_SAPI".equalsIgnoreCase(id)) return sapiBackend;
        if ("MOJANG".equalsIgnoreCase(id)) return mojangBackend;
        if (id.toUpperCase().startsWith("CUSTOM_")) {
            String subId = id.substring(7).toUpperCase();
            return customBackends.get(subId);
        }
        return customBackends.get(id.toUpperCase());
    }

    public SpeechBackend getActiveBackend(AccessibilityConfig config) {
        String engine = config.getTtsEngine();
        SpeechBackend backend = getBackend(engine);
        if (backend != null && backend.isAvailable()) {
            return backend;
        }
        if (sapiBackend.isAvailable()) {
            return sapiBackend;
        }
        return mojangBackend;
    }

    public void speak(String text) {
        speak(text, true);
    }

    public void speak(String text, boolean interrupt) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        AccessibilityConfig config = AccessibilityConfig.get();
        if (!config.isMasterEnabled()) {
            return;
        }

        String processed = text.trim();
        if (config.isReadEmojis()) {
            processed = SpeechTextProcessor.translateEmojis(processed);
        }

        SpeechBackend backend = getActiveBackend(config);
        boolean isEspeak = backend instanceof CustomCommandBackend && ((CustomCommandBackend) backend).isEspeak();
        if (!isEspeak) {
            processed = SpeechTextProcessor.processPunctuation(processed, config.getPunctuationMode());
        }

        backend.speak(
                processed.trim(),
                interrupt,
                config.getSpeechVolume(),
                config.getSpeechRate(),
                config.getSelectedVoice()
        );
    }

    public synchronized void stop() {
        for (CustomCommandBackend cb : customBackends.values()) {
            cb.stop();
        }
        sapiBackend.stop();
        mojangBackend.stop();
    }

    public List<String> getAvailableVoices() {
        AccessibilityConfig config = AccessibilityConfig.get();
        return getActiveBackend(config).getAvailableVoices();
    }

    public void testSpeech() {
        AccessibilityConfig config = AccessibilityConfig.get();
        SpeechBackend active = getActiveBackend(config);
        String msg = "Тест синтезатора " + active.getName() + " завершён успешно. Synthesizer test completed.";
        speak(msg, true);
    }
}
