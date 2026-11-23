package com.github.lostcra.mcaccessibility.config;

import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class SynthesizerFinder {

    public static class DiscoveredSynthesizer {
        private final String name;
        private final String executablePath;
        private final String argsPattern;
        private final boolean useStdin;

        public DiscoveredSynthesizer(String name, String executablePath, String argsPattern, boolean useStdin) {
            this.name = name;
            this.executablePath = executablePath;
            this.argsPattern = argsPattern;
            this.useStdin = useStdin;
        }

        public String getName() { return name; }
        public String getExecutablePath() { return executablePath; }
        public String getArgsPattern() { return argsPattern; }
        public boolean isUseStdin() { return useStdin; }
    }

    public static boolean isInstaller(File file) {
        if (file == null) return false;
        String name = file.getName().toLowerCase();
        return name.contains("setup")
                || name.contains("install")
                || name.contains("wizard")
                || name.contains("unins");
    }

    public static File resolveExecutable(File target) {
        if (target == null || !target.exists()) return null;

        if (target.isFile()) {
            if (target.getName().toLowerCase().endsWith(".exe") && !isInstaller(target)) {
                return target;
            }
            return null;
        }

        if (target.isDirectory()) {
            return searchExeInDirectory(target, 0, 3);
        }

        return null;
    }

    public static File resolvePathOrFolder(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        String clean = input.trim();
        if ((clean.startsWith("\"") && clean.endsWith("\"")) || (clean.startsWith("'") && clean.endsWith("'"))) {
            clean = clean.substring(1, clean.length() - 1).trim();
        }
        File f = new File(clean);
        return resolveExecutable(f);
    }

    private static File searchExeInDirectory(File dir, int currentDepth, int maxDepth) {
        if (dir == null || !dir.isDirectory() || currentDepth > maxDepth) return null;

        File[] files = dir.listFiles();
        if (files == null) return null;

        // Pass 1: Check root of this directory for known prioritized synthesizer executables
        for (File f : files) {
            if (f.isFile()) {
                String name = f.getName().toLowerCase();
                if (name.equals("espeak.exe") || name.equals("espeak-ng.exe")
                        || name.equals("rhvoice-test.exe") || name.equals("piper.exe")
                        || name.equals("say.exe")) {
                    return f;
                }
            }
        }

        // Pass 2: Search subdirectories for known prioritized synthesizer executables
        for (File f : files) {
            if (f.isDirectory()) {
                String dName = f.getName().toLowerCase();
                if (!dName.contains("doc") && !dName.contains("include") && !dName.contains("dict")) {
                    File found = searchExeInDirectory(f, currentDepth + 1, maxDepth);
                    if (found != null) return found;
                }
            }
        }

        // Pass 3: If no known name was found, look for any executable that is not an uninstaller/installer
        for (File f : files) {
            if (f.isFile() && f.getName().toLowerCase().endsWith(".exe")) {
                String name = f.getName().toLowerCase();
                if (!isInstaller(f) && !name.contains("unins") && !name.contains("update")) {
                    return f;
                }
            }
        }

        return null;
    }

    public static List<DiscoveredSynthesizer> findAll() {
        List<DiscoveredSynthesizer> list = new ArrayList<>();
        Set<String> seenPaths = new HashSet<>();

        // 1. Scan Windows Registry (real installed programs)
        for (DiscoveredSynthesizer ds : scanRegistry()) {
            String norm = normalizePath(ds.getExecutablePath());
            if (norm != null && seenPaths.add(norm)) {
                list.add(ds);
            }
        }

        // 2. Scan standard directories
        for (DiscoveredSynthesizer ds : scanStandardDirectories()) {
            String norm = normalizePath(ds.getExecutablePath());
            if (norm != null && seenPaths.add(norm)) {
                list.add(ds);
            }
        }

        return list;
    }

    private static String normalizePath(String path) {
        if (path == null) return null;
        try {
            return new File(path).getCanonicalPath().toLowerCase();
        } catch (Throwable t) {
            return path.toLowerCase();
        }
    }

    public static List<DiscoveredSynthesizer> scanRegistry() {
        List<DiscoveredSynthesizer> result = new ArrayList<>();
        String[] keys = new String[]{
                "HKLM\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
                "HKLM\\Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
                "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall"
        };

        for (String rootKey : keys) {
            try {
                ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "reg query \"" + rootKey + "\" /s");
                Process p = pb.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    String currentDisplayName = null;
                    String currentInstallLocation = null;
                    String currentDisplayIcon = null;

                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.startsWith("HKEY_")) {
                            checkAndAddRegistryEntry(result, currentDisplayName, currentInstallLocation, currentDisplayIcon);
                            currentDisplayName = null;
                            currentInstallLocation = null;
                            currentDisplayIcon = null;
                            continue;
                        }

                        if (line.contains("DisplayName") && line.contains("REG_SZ")) {
                            currentDisplayName = extractRegValue(line);
                        } else if ((line.contains("InstallLocation") || line.contains("App Path")) && line.contains("REG_SZ")) {
                            currentInstallLocation = extractRegValue(line);
                        } else if (line.contains("DisplayIcon") && line.contains("REG_SZ")) {
                            currentDisplayIcon = extractRegValue(line);
                        }
                    }
                    checkAndAddRegistryEntry(result, currentDisplayName, currentInstallLocation, currentDisplayIcon);
                }
                p.waitFor();
            } catch (Throwable ignored) {}
        }
        return result;
    }

    private static String extractRegValue(String line) {
        int idx = line.indexOf("REG_SZ");
        if (idx != -1) {
            return line.substring(idx + 6).trim();
        }
        return null;
    }

    private static void checkAndAddRegistryEntry(
            List<DiscoveredSynthesizer> result,
            String displayName,
            String installLocation,
            String displayIcon
    ) {
        if (displayName == null) return;
        String lowerName = displayName.toLowerCase();
        if (!lowerName.contains("espeak") && !lowerName.contains("rhvoice")
                && !lowerName.contains("piper") && !lowerName.contains("speech")
                && !lowerName.contains("voice") && !lowerName.contains("synthesizer")) {
            return;
        }

        File exe = null;
        if (installLocation != null && !installLocation.trim().isEmpty()) {
            exe = resolveExecutable(new File(installLocation.trim()));
        }
        if (exe == null && displayIcon != null && !displayIcon.trim().isEmpty()) {
            String iconPath = displayIcon.split(",")[0].replace("\"", "").trim();
            exe = resolveExecutable(new File(iconPath));
        }

        if (exe != null && exe.exists() && exe.isFile()) {
            createAndAddSynthesizer(result, displayName, exe);
        }
    }

    public static List<DiscoveredSynthesizer> scanStandardDirectories() {
        List<DiscoveredSynthesizer> result = new ArrayList<>();
        String progFiles = System.getenv("ProgramFiles");
        String progFilesX86 = System.getenv("ProgramFiles(x86)");
        String userProfile = System.getenv("USERPROFILE");

        String[] folders = new String[]{
                (userProfile != null ? userProfile : "C:\\Users\\Default") + "\\Downloads\\eSpeak NG",
                (userProfile != null ? userProfile : "C:\\Users\\Default") + "\\Downloads\\espeak-ng",
                (userProfile != null ? userProfile : "C:\\Users\\Default") + "\\AppData\\Roaming\\.minecraft\\espeak-ng",
                (userProfile != null ? userProfile : "C:\\Users\\Default") + "\\AppData\\Roaming\\.minecraft\\eSpeak NG",
                (progFiles != null ? progFiles : "C:\\Program Files") + "\\eSpeak NG",
                (progFilesX86 != null ? progFilesX86 : "C:\\Program Files (x86)") + "\\eSpeak NG",
                (progFilesX86 != null ? progFilesX86 : "C:\\Program Files (x86)") + "\\eSpeak",
                (progFiles != null ? progFiles : "C:\\Program Files") + "\\eSpeak",
                (progFiles != null ? progFiles : "C:\\Program Files") + "\\RHVoice",
                (progFilesX86 != null ? progFilesX86 : "C:\\Program Files (x86)") + "\\RHVoice",
                (userProfile != null ? userProfile : "C:\\Users\\Default") + "\\AppData\\Local\\Programs\\espeak",
                (userProfile != null ? userProfile : "C:\\Users\\Default") + "\\AppData\\Local\\Programs\\Piper",
                (userProfile != null ? userProfile : "C:\\Users\\Default") + "\\Desktop\\eSpeak NG"
        };

        for (String folder : folders) {
            File f = new File(folder);
            if (f.exists() && f.isDirectory()) {
                File exe = resolveExecutable(f);
                if (exe != null && exe.exists() && exe.isFile()) {
                    createAndAddSynthesizer(result, f.getName(), exe);
                }
            }
        }
        return result;
    }

    private static void createAndAddSynthesizer(List<DiscoveredSynthesizer> list, String nameHint, File exe) {
        String path = exe.getAbsolutePath();
        String lowerPath = path.toLowerCase();
        String name = nameHint;
        String args = "";
        boolean stdin = true;

        if (lowerPath.contains("espeak-ng")) {
            name = "eSpeak NG";
            args = "-v ru";
        } else if (lowerPath.contains("espeak")) {
            name = "eSpeak";
            args = "-v ru -b 1";
        } else if (lowerPath.contains("rhvoice")) {
            name = "RHVoice";
            args = "-p Aleksandr";
        } else if (lowerPath.contains("piper")) {
            name = "Piper";
            args = "";
        }

        list.add(new DiscoveredSynthesizer(name, path, args, stdin));
    }

    public static void launchInstallerAndCapture(
            File installerFile,
            Consumer<DiscoveredSynthesizer> onSuccess,
            Consumer<String> onStatus
    ) {
        Thread thread = new Thread(() -> {
            try {
                onStatus.accept("Запуск установщика: " + installerFile.getName());
                ProcessBuilder pb = new ProcessBuilder(installerFile.getAbsolutePath());
                if (installerFile.getParentFile() != null) {
                    pb.directory(installerFile.getParentFile());
                }
                Process p = pb.start();

                onStatus.accept("Мастер установки запущен. Завершите установку программы, затем вернитесь в игру...");
                p.waitFor();

                onStatus.accept("Установка завершена! Идёт поиск установленной программы в реестре Windows...");
                Thread.sleep(1200);

                List<DiscoveredSynthesizer> all = findAll();
                DiscoveredSynthesizer bestMatch = null;

                String instName = installerFile.getName().toLowerCase();
                for (DiscoveredSynthesizer ds : all) {
                    String sName = ds.getName().toLowerCase();
                    String sPath = ds.getExecutablePath().toLowerCase();
                    if ((instName.contains("espeak") && (sName.contains("espeak") || sPath.contains("espeak")))
                            || (instName.contains("rhvoice") && (sName.contains("rhvoice") || sPath.contains("rhvoice")))
                            || (instName.contains("piper") && (sName.contains("piper") || sPath.contains("piper")))) {
                        bestMatch = ds;
                        break;
                    }
                }

                if (bestMatch == null && !all.isEmpty()) {
                    bestMatch = all.get(0);
                }

                if (bestMatch != null) {
                    DiscoveredSynthesizer finalMatch = bestMatch;
                    Minecraft.getInstance().execute(() -> onSuccess.accept(finalMatch));
                } else {
                    onStatus.accept("Не удалось автоматически определить путь установки. Укажите папку программы вручную.");
                }
            } catch (Throwable t) {
                t.printStackTrace();
                onStatus.accept("Ошибка при запуске установщика: " + t.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void openBrowseDialog(Consumer<String> onSelected) {
        Thread t = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        "powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass", "-Command",
                        "Add-Type -AssemblyName System.Windows.Forms; " +
                        "$d = New-Object System.Windows.Forms.OpenFileDialog; " +
                        "$d.Title = 'Выберите исполняемый файл (.exe) или установщик'; " +
                        "$d.Filter = 'Программы и установщики (*.exe)|*.exe|Все файлы (*.*)|*.*'; " +
                        "if ($d.ShowDialog() -eq [System.Windows.Forms.DialogResult]::OK) { Write-Output $d.FileName }"
                );
                Process p = pb.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line = reader.readLine();
                    if (line != null && !line.trim().isEmpty()) {
                        String path = line.trim();
                        Minecraft.getInstance().execute(() -> onSelected.accept(path));
                    }
                }
            } catch (Throwable t1) {
                t1.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
