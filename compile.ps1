param(
    [switch]$InstallToMods = $false
)

$ErrorActionPreference = "Stop"

$javaBin = "C:\Users\lostcra\AppData\Local\Packages\Microsoft.4297127D64EC6_8wekyb3d8bbwe\LocalCache\Local\runtime\java-runtime-epsilon\windows-x64\java-runtime-epsilon\bin"
$javac = "$javaBin\javac.exe"
$jar = "$javaBin\jar.exe"

$libs = "C:\Users\lostcra\AppData\Roaming\.minecraft\libraries"
$mods = "C:\Users\lostcra\AppData\Roaming\.minecraft\mods"
$mcJar = "C:\Users\lostcra\AppData\Roaming\.minecraft\versions\26.3\26.3.jar"

$buildDir = "$PSScriptRoot\build"
$classesDir = "$buildDir\classes\java\main"
$outputJar = "$buildDir\libs\minecraft_availability-1.0.0.jar"
$tempExtracted = "$buildDir\temp_api_jars"

Write-Host "Creating build directories..."
if (Test-Path $classesDir) { Remove-Item -Recurse -Force $classesDir }
New-Item -ItemType Directory -Force -Path $classesDir | Out-Null
New-Item -ItemType Directory -Force -Path "$buildDir\libs" | Out-Null
New-Item -ItemType Directory -Force -Path $tempExtracted | Out-Null

# Extract nested jars from fabric-api
Write-Host "Unpacking Fabric API nested modules..."
& $jar xf "$mods\fabric-api.jar" META-INF/jars
Get-ChildItem -Path "META-INF/jars" -Filter "*.jar" | ForEach-Object {
    Copy-Item $_.FullName $tempExtracted -Force
}
Remove-Item -Recurse -Force META-INF

$nestedJars = (Get-ChildItem -Path $tempExtracted -Filter "*.jar").FullName

$cpItems = @(
    $mcJar,
    "$libs\net\fabricmc\fabric-loader\0.19.5\fabric-loader-0.19.5.jar",
    "$libs\net\fabricmc\sponge-mixin\0.17.4+mixin.0.8.7\sponge-mixin-0.17.4+mixin.0.8.7.jar",
    "$libs\org\ow2\asm\asm\9.10.1\asm-9.10.1.jar",
    "$libs\org\slf4j\slf4j-api\2.0.17\slf4j-api-2.0.17.jar",
    "$libs\com\google\code\gson\gson\2.14.0\gson-2.14.0.jar",
    "$libs\com\google\guava\guava\33.6.0-jre\guava-33.6.0-jre.jar",
    "$libs\com\mojang\text2speech\1.19.12\text2speech-1.19.12.jar",
    "$libs\com\mojang\brigadier\1.3.11\brigadier-1.3.11.jar",
    "$libs\net\java\dev\jna\jna\5.17.0\jna-5.17.0.jar",
    "$libs\net\java\dev\jna\jna-platform\5.17.0\jna-platform-5.17.0.jar",
    "$mods\cloth-config-fabric-26.3.159.jar"
) + $nestedJars

$cp = $cpItems -join ";"

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
Remove-Item -Recurse -Force $tempExtracted

Write-Host "Build SUCCESSFUL: $outputJar"

if ($InstallToMods) {
    Write-Host "Installing to Minecraft mods folder: $mods"
    Copy-Item $outputJar "$mods\minecraft_availability-1.0.0.jar" -Force
    Write-Host "Installed successfully!"
}
