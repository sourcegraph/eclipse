#!/bin/bash
set -eux
INDEXER_DIR=${SCIP_TYPESCRIPT_DIR:-../scip-typescript-cody-bindings}
CODY_DIR=${CODY_DIR:-../cody}
ECLIPSE_DIR=$PWD
# Assumes you are in the root of the repo and you have cloned the Cody repo in a
# sibling directory.
if [ ! -d $INDEXER_DIR ]; then
  git clone https://github.com/sourcegraph/scip-typescript.git $INDEXER_DIR
fi


pushd $INDEXER_DIR
git checkout olafurpg/signatures-rebase1
git pull origin olafurpg/signatures-rebase1
yarn install
popd

pushd $CODY_DIR
pnpm install
pnpm build
pnpm dlx ts-node $INDEXER_DIR/src/main.ts index --emit-signatures --emit-external-symbols
pnpm dlx ts-node agent/src/cli/scip-codegen/command.ts --language java --output $ECLIPSE_DIR/plugins/cody-chat/src/com/sourcegraph/cody/protocol_generated
popd
