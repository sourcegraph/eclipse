#!/bin/bash
set -eux
# Assumes you are in the root of the repo and you have cloned the Cody repo in a
# sibling directory.
pushd ../cody
pnpm dlx pnpm@8.6.7 install
pnpm dlx pnpm@8.6.7 build
pnpm dlx pnpm@8.6.7 -C vscode build:prod:webviews
popd
WEBVIEW_DIR=plugins/cody-chat/resources/dist/webviews
mkdir -p $WEBVIEW_DIR
cp ../cody/vscode/dist/webviews/* $WEBVIEW_DIR
cp plugins/cody-chat/resources/favicon.ico $WEBVIEW_DIR
pushd $WEBVIEW_DIR
find . -type f >assets.txt
popd
