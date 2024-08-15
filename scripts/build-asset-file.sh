#!/bin/bash
set -eux
# Assumes you are in the root of the repo and have populated the
# plugins/cody-chat/resources directory.
ASSETS_FILE=plugins/cody-chat/resources/cody-agent/assets.txt
rm -f $ASSETS_FILE
for f in plugins/cody-chat/resources/cody-agent/*; do
  echo $(basename "$f") >>$ASSETS_FILE
done
