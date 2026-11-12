#!/usr/bin/env bash
set -euo pipefail

: "${ANDROID_HOME:?The runner needs an Android SDK installation.}"

if [[ -f "$ANDROID_HOME/platforms/android-36/android.jar" &&
      -x "$ANDROID_HOME/build-tools/35.0.0/aapt2" ]]; then
    exit 0
fi

sdkmanager="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
if [[ ! -x "$sdkmanager" ]]; then
    sdkmanager="$(command -v sdkmanager)" || {
        echo "Android SDK command-line tools are missing." >&2
        exit 1
    }
fi

"$sdkmanager" --sdk_root="$ANDROID_HOME" --licenses < <(yes) > /dev/null
"$sdkmanager" --sdk_root="$ANDROID_HOME" --install 'platforms;android-36' 'build-tools;35.0.0'
