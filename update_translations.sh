#!/usr/bin/env bash

pkg_to_filename() {
    FILE=$(basename "$1")
    echo "$FILE" | sed 's/.*\.\([A-Z].*\)/\1/'
}

for trans in ~/.TwelveLittleScoutsClerk/translations/*_de.properties; do
    FILENAME=$(pkg_to_filename "$trans")
    cp "$trans" src/at/redeye/twelvelittlescoutsclerk/resources/translations/"$FILENAME"
done
