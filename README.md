# Yar

Yar is an offline Android toolkit for tabletop games. Its first tool is a two-team Hokm scorekeeper for games where each team announces how many tricks it expects to take.

The app is in Persian with a right-to-left layout, Persian numerals, the bundled Estedad typeface, and light, dark, and system appearance modes.

## Playing a match

1. Open the Hokm tool. Set the team names, two player names per team, and the number of tricks per round. Names are optional and rounds use 13 tricks by default.
2. Before each round, enter both announcements. Each team can announce from zero to the number of tricks in the round. The announcements together must cover at least that many tricks.
3. At the end of the round, enter the number of tricks taken by either team. Yar fills in the other team's result and previews both scores before saving.
4. A team that meets or exceeds its announcement earns the announced number of points. A team that falls short earns zero. Yar keeps a running total and a scrollable record of every round.

| Announced | Taken | Points |
| --- | --- | --- |
| 7 / 6 | 8 / 5 | 7 / 0 |
| 7 / 6 | 7 / 6 | 7 / 6 |
| 10 / 4 | 9 / 4 | 0 / 4 |
| 8 / 7 | 7 / 6 | 0 / 0 |

Tap a recorded round to correct or delete it. Starting a new match saves the previous match, including any pending round. Saved matches can be resumed. Resetting a match clears its rounds and running totals while retaining its teams and settings.

## Build and test

Install JDK 17 or later and Android SDK Platform 36 with Build Tools 35.0.0. Set `ANDROID_HOME` or open the project in Android Studio so it can locate the SDK. Yar runs on Android 8.0 (API 26) and newer.

```sh
./gradlew :app:assembleDebug
./gradlew :feature:hokm:testDebugUnitTest :app:lintDebug
```

The APK is at `app/build/outputs/apk/debug/app-debug.apk`. Run the database and screen tests with a device or emulator attached.

```sh
./gradlew :feature:hokm:connectedDebugAndroidTest :app:connectedDebugAndroidTest
```

## Project structure

| Module | Responsibility |
| --- | --- |
| `:app` | Tool catalogue, saved-game entry points, navigation, and persisted appearance settings. |
| `:core:designsystem` | Shared RTL theme, typography, icons, and UI components. |
| `:feature:hokm` | Hokm rules, Room storage, view model, and scorekeeping screens. |

The application supplies the repository at the composition root. The Hokm domain owns score calculations. Room stores the source inputs and changes the draft and completed-round tables in one transaction when a round is submitted. The versioned schema is checked into the feature module.

Add a game as a feature module. Give it its own rules and persistence, then register an entry in the tool catalogue and a navigation destination in `:app`.

## Release signing

The production application ID is `io.github.vahidpro.yar`. The test APK uses `io.github.vahidpro.yar.debug` and has its own local data. Set the following environment variables in a trusted build environment.

| Variable | Value |
| --- | --- |
| `YAR_SIGNING_STORE_FILE` | Absolute path to the release keystore |
| `YAR_SIGNING_STORE_PASSWORD` | Keystore password |
| `YAR_SIGNING_KEY_ALIAS` | Alias of the release key |
| `YAR_SIGNING_KEY_PASSWORD` | Password of the release key |

```sh
./gradlew :app:assembleRelease :app:bundleRelease
```

The release APK and AAB appear under `app/build/outputs`. The same tasks can produce unsigned artifacts when the signing variables are absent. Keep the production key for future updates and increment `versionCode` with each store release. See [Android's app signing guide](https://developer.android.com/studio/publish/app-signing).

## Data and licensing

Yar stores team names, player names, scores, and appearance preferences on the device. It uses no account, app-level cloud sync, analytics, or network permission.

The application source is licensed under [MIT](LICENSE). The bundled [Estedad](https://github.com/aminabedi68/Estedad) typeface is distributed under the [SIL Open Font License 1.1](core/designsystem/src/main/assets/licenses/Estedad-OFL.txt).
