#!/bin/bash
set -eux

NODE_BINARY_URL="https://github.com/sourcegraph/node-binaries/raw/main/v20.12.2/node-win-x64.exe"

# We only support Windows at this time
mkdir -p plugins/cody-chat/resources/node-binaries
curl -Lo plugins/cody-chat/resources/node-binaries/node-win-x64.exe "$NODE_BINARY_URL"
