#!/bin/bash
set -eux
# Assumes you are in the root of the repo and you have cloned the Cody repo in a
# sibling directory.
pushd ../cody
pnpm dlx pnpm@8.6.7 install
pnpm dlx pnpm@8.6.7 -C agent build:agent
popd
mkdir -p plugins/cody-chat/resources/cody-agent
cp ../cody/agent/dist/index.js plugins/cody-chat/resources/cody-agent
cp ../cody/agent/dist/*.wasm   plugins/cody-chat/resources/cody-agent
cp ../cody/agent/dist/win-ca-roots.exe   plugins/cody-chat/resources/cody-agent
ASSETS_FILE=plugins/cody-chat/resources/cody-agent/assets.txt
rm -f $ASSETS_FILE
for f in plugins/cody-chat/resources/cody-agent/*; do
  echo $(basename $f) >> $ASSETS_FILE
done
