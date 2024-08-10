#!/bin/bash
set -eux
# Assumes you are in the root of the repo and you have cloned the Cody repo in a
# sibling directory.
pushd ../cody
pnpm dlx pnpm@8.6.7 install
pnpm dlx pnpm@8.6.7 -C agent build
popd
mkdir -p plugins/cody-chat/resources/cody-agent
cp ../cody/agent/dist/index.js plugins/cody-chat/resources/cody-agent
cp ../cody/agent/dist/*.wasm plugins/cody-chat/resources/cody-agent
cp ../cody/agent/dist/win-ca-roots.exe plugins/cody-chat/resources/cody-agent
cp -r ../cody/agent/dist/webview plugins/cody-chat/resources/cody-agent
pushd plugins/cody-chat/resources/cody-agent
rm -f assets.txt
find . -type f >assets.txt
popd
