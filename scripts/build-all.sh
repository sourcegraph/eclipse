#!/bin/bash
set -eux
./scripts/build-agent.sh
./scripts/build-webview.sh
./scripts/build-java-bindings.sh
python scripts/format.py
