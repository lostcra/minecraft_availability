#!/usr/bin/env bash
set -e

INSTALL_TO_MODS=false
MINECRAFT_DIR=""
MINECRAFT_VERSION="26.3"
JAVA_BIN=""

for arg in "$@"; do
    case $arg in
        --install|-i)
            INSTALL_TO_MODS=true
            shift
            ;;
        --minecraft-dir=*)
            MINECRAFT_DIR="${arg#*=}"
            shift
            ;;
        --java-bin=*)
            JAVA_BIN="${arg#*=}"
            shift
            ;;
        --version=*)
            MINECRAFT_VERSION="${arg#*=}"
            shift
            ;;
    esac
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 1. Detect Minecraft directory
if [ -z "$MINECRAFT_DIR" ]; then
    if [ -n "$MINECRAFT_HOME" ] && [ -d "$MINECRAFT_HOME" ]; then
        MINECRAFT_DIR="$MINECRAFT_HOME"
    elif [ "$(uname)" = "Darwin" ]; then
        MINECRAFT_DIR="$HOME/Library/Application Support/minecraft"
    else
        MINECRAFT_DIR="$HOME/.minecraft"
    fi
fi

if [ ! -d "$MINECRAFT_DIR" ]; then
    echo "Error: Minecraft directory not found at $MINECRAFT_DIR" >&2
    exit 1
fi

LIBS_DIR="$MINECRAFT_DIR/libraries"
MODS_DIR="$MINECRAFT_DIR/mods"
MC_JAR="$MINECRAFT_DIR/versions/$MINECRAFT_VERSION/$MINECRAFT_VERSION.jar"

if [ ! -f "$MC_JAR" ]; then
    MC_JAR=$(find "$MINECRAFT_DIR/versions" -name "*.jar" 2>/dev/null | head -n 1)
fi

# 2. Detect Java
JAVAC="javac"
JAR="jar"

if [ -n "$JAVA_BIN" ] && [ -x "$JAVA_BIN/javac" ]; then
    JAVAC="$JAVA_BIN/javac"
    JAR="$JAVA_BIN/jar"
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/javac" ]; then
    JAVAC="$JAVA_HOME/bin/javac"
    JAR="$JAVA_HOME/bin/jar"
fi

BUILD_DIR="$SCRIPT_DIR/build"
CLASSES_DIR="$BUILD_DIR/classes/java/main"
OUTPUT_JAR="$BUILD_DIR/libs/minecraft_availability-0.2.0-beta.jar"
TEMP_EXTRACTED="$BUILD_DIR/temp_api_jars"

rm -rf "$CLASSES_DIR" "$TEMP_EXTRACTED"
mkdir -p "$CLASSES_DIR" "$BUILD_DIR/libs" "$TEMP_EXTRACTED"

# Extract Fabric API nested jars
FABRIC_API_JAR=$(find "$MODS_DIR" -name "*fabric-api*.jar" 2>/dev/null | head -n 1)
if [ -n "$FABRIC_API_JAR" ]; then
    "$JAR" xf "$FABRIC_API_JAR" META-INF/jars 2>/dev/null || true
    if [ -d "META-INF/jars" ]; then
        cp META-INF/jars/*.jar "$TEMP_EXTRACTED/" 2>/dev/null || true
        rm -rf META-INF
    fi
fi

# Build classpath
CP="$MC_JAR"
for j in $(find "$LIBS_DIR" -name "*.jar" 2>/dev/null); do
    CP="$CP:$j"
done
for j in $(find "$MODS_DIR" -name "cloth-config*.jar" 2>/dev/null); do
    CP="$CP:$j"
done
for j in $(find "$TEMP_EXTRACTED" -name "*.jar" 2>/dev/null); do
    CP="$CP:$j"
done

echo "Compiling Java sources..."
SOURCES=$(find "$SCRIPT_DIR/src/main/java" -name "*.java")
"$JAVAC" -encoding UTF-8 -cp "$CP" -d "$CLASSES_DIR" $SOURCES

echo "Copying resources..."
cp -r "$SCRIPT_DIR/src/main/resources/"* "$CLASSES_DIR/"

echo "Packaging JAR: $OUTPUT_JAR"
(cd "$CLASSES_DIR" && "$JAR" cf "$OUTPUT_JAR" *)

rm -rf "$TEMP_EXTRACTED"
echo "Build SUCCESSFUL: $OUTPUT_JAR"

if [ "$INSTALL_TO_MODS" = true ]; then
    echo "Installing to mods folder: $MODS_DIR"
    rm -f "$MODS_DIR"/minecraft_availability-*.jar 2>/dev/null || true
    cp "$OUTPUT_JAR" "$MODS_DIR/minecraft_availability-0.2.0-beta.jar"
    echo "Installed successfully!"
fi