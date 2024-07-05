#!/bin/bash
set -eux

VERSION=$1
TAG="v$VERSION"

git tag -af $TAG -m "New Cody Eclipse release"
git push -f origin $TAG
