#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)

cd "$ROOT_DIR/frontend"

npm ci
npm run build

cd "$ROOT_DIR"

STATIC_DIR="$ROOT_DIR/src/main/resources/static"
rm -rf "$STATIC_DIR"
mkdir -p "$STATIC_DIR"

cp -R "$ROOT_DIR/frontend/dist/." "$STATIC_DIR/"

mvn -DskipTests package
