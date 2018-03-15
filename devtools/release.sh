#!/bin/bash

set -eu

ROOT_REPO="$(cd ./"$(git rev-parse --show-cdup)" || exit; pwd)"
"$ROOT_REPO/mvnw" -pl core clean test
"$ROOT_REPO/mvnw" -pl core clean deploy -DperformRelease=true

