package com.github.lostcra.mcaccessibility.tts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WindowsSapiBackend implements SpeechBackend {
    private Process process;
    private BufferedWriter writer;
    private BufferedReader reader;
    private final List<String> cachedVoices = new ArrayList<>();
    private boolean available = false;
    private final MojangNarratorBackend fallbackBackend;

    public WindowsSapiBackend(MojangNarratorBackend fallbackBackend) {
        this.fallbackBackend = fallbackBackend;
        if (isWindows()) {
            initProcess();
        }
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }

    private synchronized void initProcess() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-ExecutionPolicy", "Bypass",
                    "-Command",
                    "[Console]::InputEncoding = [System.Text.Encoding]::UTF8; " +
                    "[Console]::OutputEncoding = [System.Text.Encoding]::UTF8; " +
                    "Add-Type -AssemblyName System.Speech; " +
                    "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$s.GetInstalledVoices() | ForEach-Object { Write-Output ('VOICE_LIST:' + $_.VoiceInfo.Name) }; " +
                    "Write-Output 'READY'; " +
                    "while ($line = [Console]::ReadLine()) { " +
                    "  if ($line.StartsWith('SPEAK:')) { " +
                    "    $parts = $line.Substring(6).Split(':', 4); " +
                    "    $interrupt = [int]$parts[0]; " +
                    "    $vol = [int]$parts[1]; " +
                    "    $rate = [int]$parts[2]; " +
                    "    $text = $parts[3]; " +
                    "    if ($interrupt -eq 1) { $s.SpeakAsyncCancelAll() }; " +
                    "    $s.Volume = [Math]::Max(0, [Math]::Min(100, $vol)); " +
                    "    $s.Rate = [Math]::Max(-10, [Math]::Min(10, $rate)); " +
                    "    $s.SpeakAsync($text) | Out-Null; " +
                    "  } elseif ($line.StartsWith('SET_VOICE:')) { " +
                    "    $vname = $line.Substring(10); " +
                    "    try { $s.SelectVoice($vname) } catch {} " +
                    "  } elseif ($line -eq 'STOP') { " +
                    "    $s.SpeakAsyncCancelAll(); " +
                    "  } elseif ($line -eq 'EXIT') { " +
                    "    break; " +
                    "  } " +
                    "}"
            );
            pb.redirectErrorStream(true);
            process = pb.start();
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
            reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("VOICE_LIST:")) {
                    cachedVoices.add(line.substring(11).trim());
                } else if (line.equals("READY")) {
                    available = true;
                    break;
                }
            }

            Runtime.getRuntime().addShutdownHook(new Thread(this::destroyProcess));
        } catch (Throwable t) {
            t.printStackTrace();
            available = false;
        }
    }

    private synchronized void destroyProcess() {
        try {
            if (writer != null) {
                writer.write("EXIT\n");
                writer.flush();
            }
        } catch (Exception ignored) {}
        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    @Override
    public synchronized void speak(String text, boolean interrupt, int volume, int rate, String voice) {
        if (!available || writer == null || !process.isAlive()) {
            if (fallbackBackend != null) {
                fallbackBackend.speak(text, interrupt, volume, rate, voice);
            }
            return;
        }
        try {
            if (voice != null && !voice.isEmpty() && !"Default".equalsIgnoreCase(voice)) {
                writer.write("SET_VOICE:" + voice.replace("\n", " ").replace("\r", " ") + "\n");
            }
            String cleanText = text.replace("\r", " ").replace("\n", " ");
            int intFlag = interrupt ? 1 : 0;
            writer.write("SPEAK:" + intFlag + ":" + volume + ":" + rate + ":" + cleanText + "\n");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
            if (fallbackBackend != null) {
                fallbackBackend.speak(text, interrupt, volume, rate, voice);
            }
        }
    }

    @Override
    public synchronized void stop() {
        if (available && writer != null && process.isAlive()) {
            try {
                writer.write("STOP\n");
                writer.flush();
            } catch (Exception ignored) {}
        }
        if (fallbackBackend != null) {
            fallbackBackend.stop();
        }
    }

    @Override
    public List<String> getAvailableVoices() {
        if (cachedVoices.isEmpty()) {
            return Collections.singletonList("Default");
        }
        List<String> list = new ArrayList<>();
        list.add("Default");
        list.addAll(cachedVoices);
        return list;
    }

    @Override
    public boolean isAvailable() {
        return available && process != null && process.isAlive();
    }
}
