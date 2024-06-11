#!/bin/bash
# Run this script to generate a scala-cli project for the Eclipse plugin
# More information about scala-cli https://scala-cli.virtuslab.org/
# The purpose of this script:
# - You can import the project into IntelliJ IDEA by using the BSP support from the Scala plugin https://www.jetbrains.com/help/idea/bsp-support.html
#   However, note that error highlighting doesn't seem to work for Java files.
# - You can compile the codebase super quickly locally with `scala-cli compile build.scala`



set -eu
rm -f build.scala

echo "//> using files plugins/cody-chat/src" >> build.scala

append_to_build_scala() {
  if [[ $1 == *-sources.jar ]]; then
    echo "//> using sourceJar \"$1\"" >> build.scala
  else
    echo "//> using jar \"$1\"" >> build.scala    
  fi
}

DIR=$(realpath ${ECLIPSE_PLUGINS:-/Applications/Eclipse.app/Contents/Eclipse/plugins/})

for file in $DIR/*; do
  if [[ $file == *.source_* ]]; then
    without_source="${file/.source/}"
    new_path="${without_source/.jar/}-sources.jar"
    echo $new_path
    ln -svf "$file" "$new_path"
    append_to_build_scala "$new_path"
  else
    append_to_build_scala "$file"
  fi
done

for file in $(cs fetch org.eclipse.lsp4j:org.eclipse.lsp4j:0.23.1 --classifier sources); do
  append_to_build_scala "$file"
done

for file in $(cs fetch org.eclipse.lsp4j:org.eclipse.lsp4j:0.23.1); do
  append_to_build_scala "$file"
done

scala-cli setup-ide build.scala
echo "Done. Import the project in IntelliJ IDEA. Make sure you have installed the Scala plugin."
