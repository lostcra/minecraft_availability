package com.github.lostcra.mcaccessibility.config;

public class CustomSynthesizerConfig {
    private String id;
    private String name;
    private String executablePath;
    private String argsPattern; // e.g. "-v {voice} -s {rate} {text}"
    private boolean useStdin = true;
    private String selectedVoice = "Default";

    public CustomSynthesizerConfig() {}

    public CustomSynthesizerConfig(String id, String name, String executablePath, String argsPattern, boolean useStdin) {
        this.id = id;
        this.name = name;
        this.executablePath = executablePath;
        this.argsPattern = argsPattern;
        this.useStdin = useStdin;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getExecutablePath() { return executablePath; }
    public void setExecutablePath(String executablePath) { this.executablePath = executablePath; }

    public String getArgsPattern() { return argsPattern; }
    public void setArgsPattern(String argsPattern) { this.argsPattern = argsPattern; }

    public boolean isUseStdin() { return useStdin; }
    public void setUseStdin(boolean useStdin) { this.useStdin = useStdin; }

    public String getSelectedVoice() { return selectedVoice; }
    public void setSelectedVoice(String selectedVoice) { this.selectedVoice = selectedVoice; }
}
