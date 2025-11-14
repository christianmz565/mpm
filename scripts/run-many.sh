#!/usr/bin/env bash
set -e

COUNT="${1:-1}"
CORE_DIR="core"
CHECKSUM_FILE=".core_checksum"
JAR_PATH="lwjgl3/build/libs/MicroPatosMania-1.0.0.jar"

CURRENT_SUM=$(find "$CORE_DIR" -type f -exec sha1sum {} + | sort | sha1sum | awk '{print $1}')

if [[ ! -f "$CHECKSUM_FILE" || "$(cat "$CHECKSUM_FILE")" != "$CURRENT_SUM" ]]; then
  echo "Source changed - rebuilding..."
  ./gradlew lwjgl3:build
  echo "$CURRENT_SUM" > "$CHECKSUM_FILE"
else
  echo "No changes in $CORE_DIR - skipping rebuild."
fi

for _ in $(seq 1 "$COUNT"); do
  java -jar "$JAR_PATH" &
done
