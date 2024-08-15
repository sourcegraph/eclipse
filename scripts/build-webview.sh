#!/bin/bash
set -eux
# Assumes you are in the root of the repo and you have cloned the Cody repo in a
# sibling directory.
pushd ../cody
pnpm dlx pnpm@8.6.7 install
pnpm dlx pnpm@8.6.7 build
pnpm dlx pnpm@8.6.7 -C vscode build:prod:webviews
popd
mkdir -p plugins/cody-chat/resources/webviews
cp ../cody/vscode/dist/webviews/* plugins/cody-chat/resources/cody-webviews
cp plugins/cody-chat/resources/favicon.ico plugins/cody-chat/resources/cody-webviews
