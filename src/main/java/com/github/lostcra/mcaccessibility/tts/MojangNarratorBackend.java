package com.github.lostcra.mcaccessibility.tts;

import com.mojang.text2speech.Narrator;
import java.util.Collections;
import java.util.List;

public class MojangNarratorBackend implements SpeechBackend {
    private final Narrator narrator;

    public MojangNarratorBackend() {
        Narrator n = null;
        try {
            n = Narrator.getNarrator();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        this.narrator = (n != null) ? n : Narrator.EMPTY;
    }

    @Override
    public String getId() {
        return "MOJANG";
    }

    @Override
    public String getName() {
        return "Mojang (Встроенный)";
    }

    @Override
    public void speak(String text, boolean interrupt, int volume, int rate, String voice) {
        if (narrator == null || !narrator.active()) {
            return;
        }
        float vol = Math.max(0.0f, Math.min(1.0f, volume / 100.0f));
        narrator.say(text, interrupt, vol);
    }

    @Override
    public void stop() {
        if (narrator != null && narrator.active()) {
            narrator.clear();
        }
    }

    @Override
    public List<String> getAvailableVoices() {
        return Collections.singletonList("Default");
    }

    @Override
    public boolean isAvailable() {
        return narrator != null && narrator.active();
    }
}
