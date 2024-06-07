#!/bin/bash
set -eux
pushd cody
pnpm dlx pnpm@8.6.7 install
pnpm dlx pnpm@8.6.7 build
pnpm dlx pnpm@8.6.7 -C vscode build
popd
mkdir -p plugins/cody-chat/resources/cody-webviews
cp cody/vscode/dist/webviews/* plugins/cody-chat/resources/cody-webviews
