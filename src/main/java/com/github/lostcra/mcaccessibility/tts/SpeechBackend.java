package com.github.lostcra.mcaccessibility.tts;

import java.util.List;

public interface SpeechBackend {
    void speak(String text, boolean interrupt, int volume, int rate, String voice);
    void stop();
    List<String> getAvailableVoices();
    boolean isAvailable();
}
