#!/bin/bash
# Ketoy Dev Server — Quick Start Script
#
# Usage:
#   ./ketoy-serve.sh                    # Default: port 8484, watch ./ketoy-screens
#   ./ketoy-serve.sh -p 9090            # Custom port
#   ./ketoy-serve.sh -w ./my-screens    # Custom watch directory
#
# This script starts the Ketoy Dev Server for hot-reload preview.
# Make sure your Android device/emulator is on the same network.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo ""
echo "Starting Ketoy Dev Server..."
echo ""

./gradlew :ketoy-devtools-server:run --args="$*" --console=plain -q
