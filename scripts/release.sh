#!/bin/bash
set -eux

VERSION=$1
TAG="v$VERSION"


# Assert we are on the main branch
current_branch=$(git rev-parse --abbrev-ref HEAD)
if [ "$current_branch" != "main" ]; then
    echo "Error: Not on main branch. Please switch to main branch before releasing."
    exit 1
fi

# Check for dirty state
if ! git diff-index --quiet HEAD --; then
    echo "Error: There are uncommitted changes. Please commit or stash them before releasing."
    exit 1
fi


echo "$VERSION" > plugins/cody-chat/version.txt
git add .
git commit -am "Release $TAG"
git push origin main
git tag -af "$TAG" -m "New Cody Eclipse release"
git push -f origin "$TAG"

