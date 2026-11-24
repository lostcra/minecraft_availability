param(
    [switch]$InstallToMods = $false,
    [string]$JavaBin = "",
    [string]$MinecraftDir = "",
    [string]$MinecraftVersion = "26.3"
)

$ErrorActionPreference = "Stop"

# 1. Determine .minecraft directory
if (-not $MinecraftDir) {
    if ($env:MINECRAFT_HOME -and (Test-Path $env:MINECRAFT_HOME)) {
        $MinecraftDir = $env:MINECRAFT_HOME
    } elseif ($IsWindows -or ($env:OS -like "*Windows*")) {
        $MinecraftDir = "$env:APPDATA\.minecraft"
        if (-not (Test-Path $MinecraftDir)) {
            $MinecraftDir = "$HOME\.minecraft"
        }
    } elseif ($IsMacOS) {
        $MinecraftDir = "$HOME/Library/Application Support/minecraft"
    } else {
        $MinecraftDir = "$HOME/.minecraft"
    }
}

if (-not (Test-Path $MinecraftDir)) {
    Write-Error "Could not find .minecraft directory at '$MinecraftDir'. Please specify via -MinecraftDir parameter."
    exit 1
}

$libs = Join-Path $MinecraftDir "libraries"
$mods = Join-Path $MinecraftDir "mods"
$mcJar = Join-Path $MinecraftDir "versions\$MinecraftVersion\$MinecraftVersion.jar"
if (-not (Test-Path $mcJar)) {
    $anyJar = Get-ChildItem -Path (Join-Path $MinecraftDir "versions") -Recurse -Filter "*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($anyJar) {
        $mcJar = $anyJar.FullName
    }
}

# 2. Determine Java (javac and jar) binaries
$javac = $null
$jar = $null

if ($JavaBin -and (Test-Path $JavaBin)) {
    $javacCandidate = Join-Path $JavaBin "javac.exe"
    if (-not (Test-Path $javacCandidate)) { $javacCandidate = Join-Path $JavaBin "javac" }
    $jarCandidate = Join-Path $JavaBin "jar.exe"
    if (-not (Test-Path $jarCandidate)) { $jarCandidate = Join-Path $JavaBin "jar" }
    if ((Test-Path $javacCandidate) -and (Test-Path $jarCandidate)) {
        $javac = $javacCandidate
        $jar = $jarCandidate
    }
}

if (-not $javac) {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME "bin"))) {
        $jHomeBin = Join-Path $env:JAVA_HOME "bin"
        $javacCandidate = Join-Path $jHomeBin "javac.exe"
        if (-not (Test-Path $javacCandidate)) { $javacCandidate = Join-Path $jHomeBin "javac" }
        $jarCandidate = Join-Path $jHomeBin "jar.exe"
        if (-not (Test-Path $jarCandidate)) { $jarCandidate = Join-Path $jHomeBin "jar" }
        if ((Test-Path $javacCandidate) -and (Test-Path $jarCandidate)) {
            $javac = $javacCandidate
            $jar = $jarCandidate
        }
    }
}

if (-not $javac) {
    $cmdJavac = Get-Command javac -ErrorAction SilentlyContinue
    $cmdJar = Get-Command jar -ErrorAction SilentlyContinue
    if ($cmdJavac -and $cmdJar) {
        $javac = $cmdJavac.Source
        $jar = $cmdJar.Source
    }
}

if (-not $javac -and ($env:LOCALAPPDATA)) {
    # Check Minecraft Launcher bundled runtimes on Windows
    $runtimes = @(
        "$env:LOCALAPPDATA\Packages\Microsoft.4297127D64EC6_8wekyb3d8bbwe\LocalCache\Local\runtime\java-runtime-epsilon\windows-x64\java-runtime-epsilon\bin",
        "$env:LOCALAPPDATA\Packages\Microsoft.4297127D64EC6_8wekyb3d8bbwe\LocalCache\Local\runtime\java-runtime-gamma\windows-x64\java-runtime-gamma\bin",
        "$env:LOCALAPPDATA\Packages\Microsoft.4297127D64EC6_8wekyb3d8bbwe\LocalCache\Local\runtime\java-runtime-delta\windows-x64\java-runtime-delta\bin"
    )
    foreach ($rt in $runtimes) {
        if (Test-Path (Join-Path $rt "javac.exe")) {
            $javac = Join-Path $rt "javac.exe"
            $jar = Join-Path $rt "jar.exe"
            break
        }
    }
}

if (-not $javac) {
    Write-Error "Could not find javac and jar executables. Please install JDK 25 (or higher) or specify -JavaBin path."
    exit 1
}

Write-Host "Using Java compiler: $javac"
Write-Host "Using Minecraft dir: $MinecraftDir"

$buildDir = "$PSScriptRoot\build"
$classesDir = "$buildDir\classes\java\main"
$outputJar = "$buildDir\libs\minecraft_availability-0.1.0-beta.jar"
$tempExtracted = "$buildDir\temp_api_jars"

Write-Host "Creating build directories..."
if (Test-Path $classesDir) { Remove-Item -Recurse -Force $classesDir }
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null
New-Item -ItemType Directory -Force -Path "$buildDir\libs" | Out-Null
New-Item -ItemType Directory -Force -Path $tempExtracted | Out-Null

# Extract nested jars from fabric-api
$fabricApiJar = Get-ChildItem -Path $mods -Filter "*fabric-api*.jar" -ErrorAction SilentlyContinue | Select-Object -First 1
if ($fabricApiJar) {
    Write-Host "Unpacking Fabric API nested modules from $($fabricApiJar.Name)..."
    & $jar xf $fabricApiJar.FullName META-INF/jars
    if (Test-Path "META-INF/jars") {
        Get-ChildItem -Path "META-INF/jars" -Filter "*.jar" | ForEach-Object {
            Copy-Item $_.FullName $tempExtracted -Force
        }
        Remove-Item -Recurse -Force META-INF
    }
}

$nestedJars = (Get-ChildItem -Path $tempExtracted -Filter "*.jar" -ErrorAction SilentlyContinue).FullName

function Resolve-LibJar($dir, $relativePath, $searchPattern) {
    $exact = Join-Path $dir $relativePath
    if (Test-Path $exact) { return $exact }
    $found = Get-ChildItem -Path $dir -Recurse -Filter $searchPattern -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) { return $found.FullName }
    return $null
}

$clothJar = Resolve-LibJar $mods "cloth-config-fabric-26.3.159.jar" "*cloth-config*.jar"

$cpItems = @(
    $mcJar,
    (Resolve-LibJar $libs "net\fabricmc\fabric-loader\0.19.5\fabric-loader-0.19.5.jar" "fabric-loader-*.jar"),
    (Resolve-LibJar $libs "net\fabricmc\sponge-mixin\0.17.4+mixin.0.8.7\sponge-mixin-0.17.4+mixin.0.8.7.jar" "*sponge-mixin*.jar"),
    (Resolve-LibJar $libs "org\ow2\asm\asm\9.10.1\asm-9.10.1.jar" "asm-9*.jar"),
    (Resolve-LibJar $libs "org\slf4j\slf4j-api\2.0.17\slf4j-api-2.0.17.jar" "slf4j-api-*.jar"),
    (Resolve-LibJar $libs "com\google\code\gson\gson\2.14.0\gson-2.14.0.jar" "gson-*.jar"),
    (Resolve-LibJar $libs "com\google\guava\guava\33.6.0-jre\guava-33.6.0-jre.jar" "guava-*.jar"),
    (Resolve-LibJar $libs "com\mojang\text2speech\1.19.12\text2speech-1.19.12.jar" "text2speech-*.jar"),
    (Resolve-LibJar $libs "com\mojang\brigadier\1.3.11\brigadier-1.3.11.jar" "brigadier-*.jar"),
    (Resolve-LibJar $libs "net\java\dev\jna\jna\5.17.0\jna-5.17.0.jar" "jna-5*.jar"),
    (Resolve-LibJar $libs "net\java\dev\jna\jna-platform\5.17.0\jna-platform-5.17.0.jar" "jna-platform-*.jar"),
    (Resolve-LibJar $libs "it\unimi\dsi\fastutil\8.5.18\fastutil-8.5.18.jar" "fastutil-*.jar"),
    (Resolve-LibJar $libs "org\jspecify\jspecify\1.0.0\jspecify-1.0.0.jar" "jspecify-*.jar"),
    (Resolve-LibJar $libs "org\joml\joml\1.10.9\joml-1.10.9.jar" "joml-*.jar")
)

if ($clothJar) { $cpItems += $clothJar }
if ($nestedJars) { $cpItems += $nestedJars }

$pathSep = [System.IO.Path]::PathSeparator
$cp = ($cpItems | Where-Object { $_ -and (Test-Path $_) }) -join $pathSep

Write-Host "Finding source files..."
$sourceFiles = (Get-ChildItem -Path "$PSScriptRoot\src\main\java" -Recurse -Filter "*.java").FullName
Write-Host "Found $($sourceFiles.Count) Java source files."

Write-Host "Compiling Java sources..."
& $javac -encoding UTF-8 -cp $cp -d $classesDir $sourceFiles
if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}

Write-Host "Copying resources..."
Copy-Item -Path "$PSScriptRoot\src\main\resources\*" -Destination $classesDir -Recurse -Force

Write-Host "Packaging JAR: $outputJar..."
Push-Location $classesDir
& $jar cf $outputJar *
Pop-Location

# Clean up temporary extracted api jars
if (Test-Path $tempExtracted) {
    Remove-Item -Recurse -Force $tempExtracted
}

Write-Host "Build SUCCESSFUL: $outputJar"

if ($InstallToMods) {
    Write-Host "Installing to Minecraft mods folder: $mods"
    Copy-Item $outputJar (Join-Path $mods "minecraft_availability-0.1.0-beta.jar") -Force
    Write-Host "Installed successfully!"
}