#!/usr/bin/env bash
set -e

COUNT="${1:-1}"
CORE_DIRS=("core/src" "assets")
CHECKSUM_FILE=".core_checksum"
JAR_PATH="lwjgl3/build/libs/MicroPatosMania-1.0.0.jar"

CURRENT_SUM=$(find "${CORE_DIRS[@]}" -type f -exec sha1sum {} + | sort | sha1sum | awk '{print $1}')

if [[ ! -f "$CHECKSUM_FILE" || "$(cat "$CHECKSUM_FILE")" != "$CURRENT_SUM" ]]; then
  echo "Source or assets changed - rebuilding..."
  ./gradlew lwjgl3:build
  echo "$CURRENT_SUM" > "$CHECKSUM_FILE"
else
  echo "No changes in source/assets - skipping rebuild."
fi

for _ in $(seq 1 "$COUNT"); do
  # Running in background with combined output
  java -jar "$JAR_PATH" > "lastrun-$_.log" 2>&1 &
done