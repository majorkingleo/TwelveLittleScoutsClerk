#!/bin/bash
# Installs local third-party JARs for TwelveLittleScoutsClerk into the local Maven repository (~/.m2).
# FrameWork is referenced directly from ../FrameWork/target/FrameWork.jar and is not installed here.

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
EXT="$PROJECT_DIR/src/at/redeye/twelvelittlescoutsclerk/ext_resources/framework"

install() {
    local file="$1"
    local groupId="$2"
    local artifactId="$3"
    local version="$4"

    if [ ! -f "$file" ]; then
        echo "ERROR: JAR not found: $file"
        exit 1
    fi

    echo "Installing $artifactId-$version ..."
    mvn install:install-file -q \
        -Dfile="$file" \
        -DgroupId="$groupId" \
        -DartifactId="$artifactId" \
        -Dversion="$version" \
        -Dpackaging=jar
}

install "$EXT/JDatePicker.jar"          net.sourceforge.jdatepicker jdatepicker        1.0-local
install "$EXT/JDatePickerPlugin.jar"    net.sourceforge.jdatepicker jdatepicker-plugin 1.0-local
install "$EXT/opencsv-1.0.jar"          net.sf.opencsv              opencsv            1.0

# AI modification start (GitHub Copilot / GPT-5.3-Codex)
find_libreoffice_classes_dir() {
    if [ -n "$LIBREOFFICE_CLASSES_DIR" ] && [ -d "$LIBREOFFICE_CLASSES_DIR" ]; then
        echo "$LIBREOFFICE_CLASSES_DIR"
        return 0
    fi

    local candidates=(
        "/usr/lib/libreoffice/program/classes"
        "/usr/lib64/libreoffice/program/classes"
        "/opt/libreoffice/program/classes"
        "/c/Program Files/LibreOffice/program/classes"
        "/c/Program Files (x86)/LibreOffice/program/classes"
    )

    local dir
    for dir in "${candidates[@]}"; do
        if [ -d "$dir" ]; then
            echo "$dir"
            return 0
        fi
    done

    return 1
}

LO_CLASSES_DIR="$(find_libreoffice_classes_dir || true)"

if [ -n "$LO_CLASSES_DIR" ]; then
    echo "Using LibreOffice classes from: $LO_CLASSES_DIR"
    install "$LO_CLASSES_DIR/libreoffice.jar"  org.libreoffice libreoffice 1.0-local
    install "$LO_CLASSES_DIR/jurt.jar"         org.libreoffice jurt        1.0-local
    install "$LO_CLASSES_DIR/ridl.jar"         org.libreoffice ridl        1.0-local
    install "$LO_CLASSES_DIR/unoil.jar"        org.libreoffice unoil       1.0-local
    install "$LO_CLASSES_DIR/unoloader.jar"    org.libreoffice unoloader   1.0-local
else
    echo "WARNING: LibreOffice classes directory not found."
    echo "Set LIBREOFFICE_CLASSES_DIR and rerun this script to install UNO jars."
fi
# AI modification end

echo ""
echo "All local JARs installed successfully."
