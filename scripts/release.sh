#!/bin/bash
set -eux

VERSION=$1
TAG="v$VERSION"

git tag -af $TAG -m "New Cody Eclipse release"
git push -f origin $TAG

git checkout releases
git rebase main

cp -r releng/cody-update-site/* docs
git add --force docs

git add .
git commit -am "Add jars from local build"
git push origin releases
