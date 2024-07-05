#!/bin/bash
set -eux

VERSION=$1

# Check if gsed is installed
if ! command -v gsed &> /dev/null
then
    brew install gnu-sed
fi

gsed -i "s/^Bundle-Version:.*/Bundle-Version: $VERSION.qualifier/" plugins/cody-chat/META-INF/MANIFEST.MF
gsed -i "s/clientInfo\.version = \".*\";/clientInfo.version = \"$VERSION\";/" plugins/cody-chat/src/com/sourcegraph/cody/CodyAgent.java
gsed -i "s/      version=\".*\"/      version=\"$VERSION.qualifier\"/" releng/cody-feature/feature.xml

./scripts/build-all.sh

echo "Done. Please review the diff, git add, commit and open a PR."
