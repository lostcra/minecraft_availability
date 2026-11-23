package com.github.lostcra.mcaccessibility.tts;

import com.github.lostcra.mcaccessibility.config.AccessibilityConfig;
import com.github.lostcra.mcaccessibility.config.CustomSynthesizerConfig;
import com.github.lostcra.mcaccessibility.config.SynthesizerFinder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomCommandBackend implements SpeechBackend {
    private final CustomSynthesizerConfig config;
    private Process currentProcess;
    private List<String> cachedVoices = null;

    public CustomCommandBackend(CustomSynthesizerConfig config) {
        this.config = config;
    }

    public static boolean isInstaller(String path) {
        if (path == null || path.trim().isEmpty()) return false;
        String lower = new File(path).getName().toLowerCase();
        return lower.contains("setup")
                || lower.contains("install")
                || lower.contains("wizard")
                || lower.contains("unins");
    }

    public boolean isEspeak() {
        if (config == null || config.getExecutablePath() == null) return false;
        return config.getExecutablePath().toLowerCase().contains("espeak");
    }

    @Override
    public String getId() {
        return "CUSTOM_" + config.getId();
    }

    @Override
    public String getName() {
        return config.getName() != null ? config.getName() : "Custom (" + config.getId() + ")";
    }

    public CustomSynthesizerConfig getConfig() {
        return config;
    }

    @Override
    public synchronized void speak(String text, boolean interrupt, int volume, int rate, String voice) {
        if (!isAvailable()) return;

        if (interrupt) {
            stop();
        }

        try {
            File exe = new File(config.getExecutablePath());
            if (exe.isDirectory()) {
                File resolved = SynthesizerFinder.resolveExecutable(exe);
                if (resolved != null) {
                    exe = resolved;
                }
            }
            List<String> cmd = new ArrayList<>();
            cmd.add(exe.getAbsolutePath());

            AccessibilityConfig accConfig = AccessibilityConfig.get();
            boolean isEspeak = isEspeak();

            String rawArgs = config.getArgsPattern();
            if (rawArgs != null && !rawArgs.trim().isEmpty()) {
                String punctArg = "";
                if ("ALL".equalsIgnoreCase(accConfig.getPunctuationMode())) {
                    punctArg = "--punct";
                } else if ("SOME".equalsIgnoreCase(accConfig.getPunctuationMode())) {
                    punctArg = "--punct=\".,?!:;\"";
                }

                String capArg = "";
                if ("WORD".equalsIgnoreCase(accConfig.getCapitalMode())) {
                    capArg = "-k 2";
                } else if ("PITCH".equalsIgnoreCase(accConfig.getCapitalMode())) {
                    capArg = "-k 20";
                } else if ("BEEP".equalsIgnoreCase(accConfig.getCapitalMode())) {
                    capArg = "-k 1";
                }

                String populated = rawArgs
                        .replace("{text}", text)
                        .replace("{rate}", String.valueOf(rate))
                        .replace("{volume}", String.valueOf(volume))
                        .replace("{voice}", voice != null && !voice.equalsIgnoreCase("Default") ? voice : (isEspeak ? "ru" : ""))
                        .replace("{punct}", punctArg)
                        .replace("{capitals}", capArg);

                for (String token : populated.split("\\s+")) {
                    if (!token.isEmpty()) {
                        cmd.add(token);
                    }
                }
            } else if (isEspeak) {
                // Optimal default configuration for eSpeak
                cmd.add("-v");
                cmd.add(voice != null && !voice.equalsIgnoreCase("Default") ? voice : "ru");
                cmd.add("-b");
                cmd.add("1"); // UTF-8 input

                int wpm = 175 + (rate * 15);
                cmd.add("-s");
                cmd.add(String.valueOf(Math.max(80, Math.min(450, wpm))));

                int amp = Math.max(0, Math.min(200, volume * 2));
                cmd.add("-a");
                cmd.add(String.valueOf(amp));

                if ("ALL".equalsIgnoreCase(accConfig.getPunctuationMode())) {
                    cmd.add("--punct");
                } else if ("SOME".equalsIgnoreCase(accConfig.getPunctuationMode())) {
                    cmd.add("--punct=.,?!:;");
                }

                if ("WORD".equalsIgnoreCase(accConfig.getCapitalMode())) {
                    cmd.add("-k");
                    cmd.add("2");
                } else if ("PITCH".equalsIgnoreCase(accConfig.getCapitalMode())) {
                    cmd.add("-k");
                    cmd.add("20");
                } else if ("BEEP".equalsIgnoreCase(accConfig.getCapitalMode())) {
                    cmd.add("-k");
                    cmd.add("1");
                }

                if (!config.isUseStdin()) {
                    cmd.add(text);
                }
            } else if (!config.isUseStdin()) {
                cmd.add(text);
            }

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(exe.getParentFile());

            Process p = pb.start();
            this.currentProcess = p;

            if (config.isUseStdin()) {
                try (OutputStream os = p.getOutputStream()) {
                    os.write(text.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }
            }

            Thread waitThread = new Thread(() -> {
                try {
                    p.waitFor();
                } catch (InterruptedException ignored) {}
                synchronized (CustomCommandBackend.this) {
                    if (currentProcess == p) {
                        currentProcess = null;
                    }
                }
            });
            waitThread.setDaemon(true);
            waitThread.start();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public synchronized void stop() {
        if (currentProcess != null && currentProcess.isAlive()) {
            try {
                currentProcess.destroyForcibly();
            } catch (Throwable ignored) {}
            currentProcess = null;
        }
    }

    @Override
    public synchronized List<String> getAvailableVoices() {
        if (cachedVoices != null) {
            return cachedVoices;
        }
        List<String> list = new ArrayList<>();
        list.add("Default");

        if (isEspeak() && isAvailable()) {
            try {
                File exe = new File(config.getExecutablePath());
                ProcessBuilder pb = new ProcessBuilder(exe.getAbsolutePath(), "--voices");
                pb.redirectErrorStream(true);
                Process p = pb.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    boolean headerPassed = false;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("Pty") || line.contains("VoiceName")) {
                            headerPassed = true;
                            continue;
                        }
                        if (headerPassed && !line.isEmpty()) {
                            String[] parts = line.split("\\s+");
                            if (parts.length >= 4) {
                                String lang = parts[1];
                                if (!list.contains(lang)) {
                                    list.add(lang);
                                }
                            }
                        }
                    }
                }
                p.waitFor();
            } catch (Throwable ignored) {}
        }

        cachedVoices = list;
        return cachedVoices;
    }

    @Override
    public boolean isAvailable() {
        if (config == null || config.getExecutablePath() == null) return false;
        if (isInstaller(config.getExecutablePath())) return false;
        File f = new File(config.getExecutablePath());
        if (f.isDirectory()) {
            File resolved = SynthesizerFinder.resolveExecutable(f);
            return resolved != null && resolved.exists() && resolved.isFile();
        }
        return f.exists() && f.isFile();
    }
}
