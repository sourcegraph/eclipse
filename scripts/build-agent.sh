#!/bin/bash
set -eux
# Assumes you are in the root of the repo and you have cloned the Cody repo in a
# sibling directory.
pushd ../cody
pnpm dlx pnpm@8.6.7 install
pnpm dlx pnpm@8.6.7 -C agent build:agent
popd
CODY_AGENT_DIR=plugins/cody-chat/resources/dist/cody-agent
mkdir -p $CODY_AGENT_DIR 
cp ../cody/agent/dist/index.js $CODY_AGENT_DIR
cp ../cody/agent/dist/*.wasm $CODY_AGENT_DIR
cp ../cody/agent/dist/win-ca-roots.exe $CODY_AGENT_DIR
pushd $CODY_AGENT_DIR
find . -type f >assets.txt
popd
