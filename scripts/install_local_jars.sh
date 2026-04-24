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

echo ""
echo "All local JARs installed successfully."
