# Yar

Yar is an offline Android toolkit for tabletop games. Its first tool is a two-team Hokm scorekeeper for games where each team announces how many tricks it expects to take.

Yar supports Persian, English, and Arabic-script Balochi. Persian and Balochi use right-to-left layouts. Open **Settings** to choose a language, select Forest, Ocean, or Plum, and choose Light, Dark, or Follow device.

## Playing a match

1. Open the Hokm tool and select **Game settings**. Set the team and player names, tricks per hand, and the minimum bid per team. Names are optional. The default is 13 tricks.
2. For a regular hand, enter both bids. Each bid must be between the game minimum and the number of tricks. The combined bids must cover at least all the available tricks. A higher total is valid. Yar points out that at least one team will miss its bid.
3. Set the forced hand interval and the bid for each team in **Game settings**. New games default to 7 and 7 on every fifth hand. An interval of 7 uses hands 7, 14, and 21. Set it to 0 to turn off forced hands. The countdown identifies the next forced hand, and Yar fills its two configured bids when it starts.
4. Enter the actual tricks won by **both teams**. The total must equal the number of tricks configured for the hand. A team that meets or exceeds its bid gets exactly the bid in points. A team that falls short gets zero.
5. Tap the eye to hide or show earned points during a game. **Game stats** compares each team's bids with its results. **Finish game** saves both totals and shows the winner or tie. **Share score table** opens Android sharing with the final result and a CSV of every recorded hand.

| Announced | Taken | Points |
| --- | --- | --- |
| 7 / 6 | 8 / 5 | 7 / 0 |
| 7 / 6 | 7 / 6 | 7 / 6 |
| 10 / 4 | 9 / 4 | 0 / 4 |
| 8 / 7 | 7 / 6 | 0 / 0 |

Settings apply to one game. A change to the forced schedule or minimum bid applies to hands that have yet to start. A hand in progress keeps the bids and minimum it started with. For a forced hand, each configured bid follows the per-team range and the pair may total less than the available tricks.

Tap a recorded hand to correct it or delete it during play. Starting a new game saves the current one for resuming later. Finishing a game saves the recorded hands as a final result. If a hand is in progress, the finish confirmation explains that the unscored hand will be discarded. Games from version 1.0 keep their scores and start with forced hands disabled.

## Build and test

Yar runs on Android 8.0 (API 26) and newer. Install JDK 17 and Android Studio or the Android SDK command-line tools. Set `JAVA_HOME` to the JDK and `ANDROID_HOME` to the SDK folder. Android Studio can also locate the SDK through `local.properties`.

Clone the repository and install the required SDK packages.

```sh
git clone https://github.com/Vahidpro/yar-android.git
cd yar-android
sdkmanager --licenses
sdkmanager 'platforms;android-36' 'build-tools;35.0.0' 'platform-tools'
```

Use the checked-in Gradle wrapper to run the checks and build the test APK. On Windows use `gradlew.bat`.

```sh
./gradlew :feature:hokm:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The test APK is at `app/build/outputs/apk/debug/app-debug.apk`. Open it on your Android device or install it from a computer with ADB. Run the database and screen tests with a device or emulator attached.

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
./gradlew :feature:hokm:connectedDebugAndroidTest :app:connectedDebugAndroidTest
```

### Download a test APK from GitHub

1. Open the repository's **Actions** tab and choose **Android**.
2. Open the latest successful run for your branch. To build again, select **Run workflow**, choose the branch, and start the run.
3. In the run summary, download **Yar-debug-apk** under **Artifacts**, extract the ZIP, and open `app-debug.apk` on Android.

The test APK uses the debug package ID and the builder's debug key. Android requires the same signing key to update an installed package. If you have scores in a previous test installation, keep it while you resolve a signing mismatch.

Select **Build a preview that installs alongside the debug app** when running the Android workflow, or build it locally with the following command. The preview uses `io.github.vahidpro.yar.preview` and starts with its own game data.

```sh
./gradlew -PyarPreview=true :app:assembleDebug
```

## Project structure

| Module | Responsibility |
| --- | --- |
| `:app` | Tool catalogue, saved-game entry points, navigation, and persisted language and appearance settings. |
| `:core:designsystem` | Color palettes, bidirectional layout, typography, icons, and shared UI components. |
| `:feature:hokm` | Hokm rules, per-game settings, Room storage, statistics, and score-table export. |

The application supplies the repository at the composition root. The Hokm domain owns score calculations. Room stores the source inputs and changes the draft and completed-round tables in one transaction when a round is submitted. The versioned schema is checked into the feature module.

Add a game as a feature module. Give it its own rules and persistence, then register an entry in the tool catalogue and a navigation destination in `:app`.

## Release signing

The production application ID is `io.github.vahidpro.yar`. The test APK uses `io.github.vahidpro.yar.debug` and has its own local data. Create a release keystore once using Android Studio's signing wizard, or use the key already associated with the production app. Set these environment variables in a trusted build environment. [Android app signing guide](https://developer.android.com/studio/publish/app-signing)

| Variable | Value |
| --- | --- |
| `YAR_SIGNING_STORE_FILE` | Absolute path to the release keystore |
| `YAR_SIGNING_STORE_PASSWORD` | Keystore password |
| `YAR_SIGNING_KEY_ALIAS` | Alias of the release key |
| `YAR_SIGNING_KEY_PASSWORD` | Password of the release key |

```sh
./gradlew :app:assembleRelease :app:bundleRelease
```

The signed APK is at `app/build/outputs/apk/release/app-release.apk` and the bundle is at `app/build/outputs/bundle/release/app-release.aab`. With the signing variables absent, Gradle builds an unsigned APK called `app-release-unsigned.apk`. Verify the signed APK with the SDK's `apksigner`.

```sh
"$ANDROID_HOME/build-tools/35.0.0/apksigner" verify --verbose app/build/outputs/apk/release/app-release.apk
```

## Automated GitHub releases

Open **Actions → Release APK → Run workflow** to build an optimized APK and a SHA-256 checksum without publishing a release. Pushing a version tag publishes the signed APK and checksum to **GitHub Releases** when signing is configured.

Add these repository secrets under **Settings → Secrets and variables → Actions**. Use the same release key for every version.

| Repository secret | Value |
| --- | --- |
| `YAR_SIGNING_KEYSTORE_BASE64` | Base64-encoded release keystore |
| `YAR_SIGNING_STORE_PASSWORD` | Keystore password |
| `YAR_SIGNING_KEY_ALIAS` | Release key alias |
| `YAR_SIGNING_KEY_PASSWORD` | Release key password |

Encode the keystore locally before setting `YAR_SIGNING_KEYSTORE_BASE64`. The encoded value contains the signing key and belongs in the repository secret.

```sh
base64 < /path/to/yar-release.jks | tr -d '\n'
```

Set `versionName` in `app/build.gradle.kts`, increment `versionCode` for each update, and push a matching tag from the commit you want to release.

```sh
git tag v1.1.0
git push origin v1.1.0
```

Download `Yar-1.1.0.apk` and `Yar-1.1.0.apk.sha256` from the release. Run `sha256sum -c Yar-1.1.0.apk.sha256` from the download folder to check the APK.

With all signing secrets absent, **Release APK** saves `Yar-1.1.0-unsigned.apk` under **Actions → Artifacts**. Use the **Android** workflow for an installable test APK. To publish an installable production APK, add all four signing secrets and rerun the tagged workflow.

### Troubleshooting APK builds

| What you see | What to check |
| --- | --- |
| `Failed to find package 'tools'` in `setup-android` | Start a new run on the latest branch. The current workflows use the runner's `sdkmanager` and request Platform 36 and Build Tools 35.0.0 directly. A rerun of an older commit still uses its older workflow. |
| `Queued` or `startup_failure` with no jobs | Open the run summary and check runner availability and repository Actions settings. Start a new run after the runner is available. The local build commands above also produce a test APK. |
| `The tag must match versionName` | Push a tag matching the committed `versionName`, such as `v1.1.0` for `1.1.0`. |
| Signing configuration is incomplete | Set all four repository secrets and verify the alias and both passwords against the same keystore. |
| Android refuses an APK update | Compare the application ID and signing certificate with the installed version. Build the update with the same signing key. |

Open the failed job to see the step logs. GitHub provides the workflow run and runner troubleshooting steps. [GitHub Actions logs](https://docs.github.com/en/actions/how-tos/monitor-workflows/use-workflow-run-logs)

## Data and licensing

Yar stores team names, player names, game rules, scores, language, and appearance preferences on the device. It uses no account, app-level cloud sync, analytics, or network permission. Score exports use the Android share sheet.

The application source is licensed under [MIT](LICENSE). The bundled [Estedad](https://github.com/aminabedi68/Estedad) typeface is distributed under the [SIL Open Font License 1.1](core/designsystem/src/main/assets/licenses/Estedad-OFL.txt).
