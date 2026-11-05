#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
APP="$SCRIPT_DIR/target/ElectricalNetwork/bin/ElectricalNetwork"

if [ ! -x "$APP" ]; then
    echo "Error: bundled application not found at \"$APP\"" >&2
    echo 'Run "mvn clean package" from the project root first.' >&2
    exit 1
fi

exec "$APP" "$@"
