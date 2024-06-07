#!/bin/bash
set -eux
pushd cody
pnpm install
pnpm build
pnpm -C vscode build
popd
mkdir -p plugins/cody-chat/resources/cody-webviews
cp cody/vscode/dist/webviews/* plugins/cody-chat/resources/cody-webviews
