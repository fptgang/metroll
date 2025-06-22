#!/usr/bin/env bash
set -euo pipefail
MODULE_NAME="$1"
shift || true
JAR="${MODULE_NAME}/build/libs/${MODULE_NAME}-1.0-SNAPSHOT.jar"
if [[ ! -f "$JAR" ]]; then
  echo "ERROR: $JAR not found. Make sure Gradle built it." >&2
  exit 1
fi
exec java "$@" -jar "$JAR" 