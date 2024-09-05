#!/bin/bash
set -eux

NODE_WINDOWS_BINARY_URL="https://github.com/sourcegraph/node-binaries/raw/main/v20.12.2/node-win-x64.exe"
NODE_MACOS_BINARY_URL="https://github.com/sourcegraph/node-binaries/raw/main/v20.12.2/node-macos-arm64"
LINUX_BINARY_URL="https://github.com/sourcegraph/node-binaries/raw/main/v20.12.2/node-linux-x64"

# We only support Windows at this time
mkdir -p plugins/cody-chat/resources/node-binaries
curl -Lo plugins/cody-chat/resources/node-binaries/node-win-x64.exe "$NODE_WINDOWS_BINARY_URL"
curl -Lo plugins/cody-chat/resources/node-binaries/node-macos-arm64 "$NODE_MACOS_BINARY_URL"
curl -Lo plugins/cody-chat/resources/node-binaries/node-linux-x64 "$LINUX_BINARY_URL"
