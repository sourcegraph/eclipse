#!/bin/bash

# Determine the operating system
OS=$(uname -s)

clean_windows() {
    rm -rf ~/AppData/Roaming/Sourcegraph/CodyEclipse/
    rm -rf ~/AppData/Roaming/Cody-nodejs/Config/dist/webview
}

clean_macos() {
    rm -rf ~/Library/Application\ Support/com.sourcegraph.Sourcegraph.CodyEclipse
    rm -rf ~/Library/Preferences/Cody-nodejs/dist/webviews/
}

clean_common() {
    rm -rf plugins/cody-chat/resources/dist/
}

clean_common

case "$OS" in
    "Windows_NT")
        clean_windows
        ;;
    "Darwin")
        clean_macos
        ;;
    *)
        echo "Unsupported OS: $OS"
        ;;
esac

