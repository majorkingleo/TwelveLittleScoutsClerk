#!/bin/bash
# Installs all local JARs for TwelveLittleScoutsClerk into the local Maven repository (~/.m2).
# Run this script once after cloning the repo, or whenever FrameWork is rebuilt.

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
EXT="$PROJECT_DIR/src/at/redeye/twelvelittlescoutsclerk/ext_resources/framework"
FRAMEWORK_DIST="$PROJECT_DIR/../FrameWork/dist/FrameWork.jar"

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

# FrameWork: prefer the current build from the sibling project, fall back to the bundled copy
if [ -f "$FRAMEWORK_DIST" ]; then
    install "$FRAMEWORK_DIST"           at.redeye FrameWork 1.0
else
    echo "WARNING: ../FrameWork/dist/FrameWork.jar not found, using bundled copy"
    install "$EXT/FrameWork.jar"        at.redeye FrameWork 1.0
fi

install "$EXT/JDatePicker.jar"          net.sourceforge.jdatepicker jdatepicker        1.0-local
install "$EXT/JDatePickerPlugin.jar"    net.sourceforge.jdatepicker jdatepicker-plugin 1.0-local
install "$EXT/opencsv-1.0.jar"          net.sf.opencsv              opencsv            1.0

echo ""
echo "All local JARs installed successfully."
