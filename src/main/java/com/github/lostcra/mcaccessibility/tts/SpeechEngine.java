package com.github.lostcra.mcaccessibility.tts;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SpeechEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger("MinecraftAvailability/TTS");
    private static SpeechEngine instance;

    private final MojangNarratorBackend mojangBackend;
    private final WindowsSapiBackend sapiBackend;

    private SpeechEngine() {
        this.mojangBackend = new MojangNarratorBackend();
        this.sapiBackend = new WindowsSapiBackend(mojangBackend);
        LOGGER.info("SpeechEngine initialized. SAPI available: {}", sapiBackend.isAvailable());
    }

    public static synchronized SpeechEngine getInstance() {
        if (instance == null) {
            instance = new SpeechEngine();
        }
        return instance;
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

        SpeechBackend backend = getActiveBackend(config);
        backend.speak(
                text.trim(),
                interrupt,
                config.getSpeechVolume(),
                config.getSpeechRate(),
                config.getSelectedVoice()
        );
    }

    public void stop() {
        AccessibilityConfig config = AccessibilityConfig.get();
        getActiveBackend(config).stop();
    }

    private SpeechBackend getActiveBackend(AccessibilityConfig config) {
        String engine = config.getTtsEngine();
        if ("WINDOWS_SAPI".equalsIgnoreCase(engine) && sapiBackend.isAvailable()) {
            return sapiBackend;
        }
        return mojangBackend;
    }

    public List<String> getAvailableVoices() {
        AccessibilityConfig config = AccessibilityConfig.get();
        if ("WINDOWS_SAPI".equalsIgnoreCase(config.getTtsEngine()) && sapiBackend.isAvailable()) {
            return sapiBackend.getAvailableVoices();
        }
        return mojangBackend.getAvailableVoices();
    }

    public void testSpeech() {
        speak("Тест синтезатора речи завершён успешно. Speech synthesizer test completed.", true);
    }
}
