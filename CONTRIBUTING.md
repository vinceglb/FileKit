# Contributing to FileKit

Thanks for helping improve FileKit! Bug reports, documentation fixes, platform support, and new features are all
welcome. This guide explains how the repository is organised and what we expect from a pull request.

- **Questions and ideas** → [GitHub Discussions](https://github.com/vinceglb/FileKit/discussions)
- **Bugs and feature requests** → [open an issue](https://github.com/vinceglb/FileKit/issues/new/choose)
- **Code of conduct** → [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)

For anything larger than a small fix (a new API, a new platform, a behaviour change), please open an issue or a
discussion first so we can agree on the approach before you invest time in it.

## Setup

You need:

- **JDK 21** (the CI uses Temurin 21).
- **Android SDK** for the Android targets and sample. Android Studio or IntelliJ IDEA with the Kotlin Multiplatform
  plugin is recommended.
- **macOS with Xcode** to build and test the Apple targets (iOS, macOS, watchOS). Other targets build on any OS.
- **Linux only:** `libdbus-1-dev` to build the native Linux dialogs (`sudo apt-get install libdbus-1-dev`).

Then clone the repository and open it in your IDE. The Gradle wrapper downloads everything else.

```shell
git clone https://github.com/vinceglb/FileKit.git
cd FileKit
```

## Project layout

| Module                    | What it contains                                                                 |
|---------------------------|----------------------------------------------------------------------------------|
| `filekit-core`            | The `PlatformFile` abstraction and file operations (read, write, copy, list, …)  |
| `filekit-dialogs`         | File, directory, and save pickers, plus other native dialogs                     |
| `filekit-dialogs-compose` | Compose Multiplatform bindings (`rememberFilePickerLauncher`, …)                 |
| `filekit-coil`            | Coil integration for loading images from a `PlatformFile`                        |
| `sample/`                 | Sample app: `shared` code plus one app module per platform                       |
| `docs/`                   | User documentation (Mintlify site published at https://filekit.mintlify.app)     |
| `build-logic/`            | Gradle convention plugins shared by the modules                                  |

`documentation-v0.8.8.md` is a legacy snapshot of the old docs; do not update it.

### Source-set hierarchy

Each library module uses intermediate source sets above the platform ones, for example `nonWebMain`,
`jvmAndNativeMain`, `webMain`, `mobileMain`, and `nonAndroidMain`. **Put code in the widest source set whose
platforms share the implementation**, and only drop down to a platform set (`androidMain`, `iosMain`, `jvmMain`,
`linuxMain`, `wasmJsMain`, …) when the implementation really differs.

- Keep `expect`/`actual` declarations mirrored across targets.
- Group platform-specific helpers under the matching `src/<platform>Main` directory.
- Keep shared constants in `commonMain`.

Tests follow the same layout in sibling `src/<sourceSet>Test` directories. Shared test fixtures live in
`src/nonWebTest/resources/`.

## Building and testing locally

FileKit targets many platforms, so a full build is slow and heavy. **Keep local validation scoped to the module you
changed and one relevant target**, and let CI run the full matrix.

```shell
# Run one test class on the JVM
./gradlew :filekit-core:jvmTest --tests '*PlatformFileDeletionTest*' --max-workers=1

# Other useful targeted test tasks
./gradlew :filekit-core:iosSimulatorArm64Test --max-workers=1
./gradlew :filekit-core:wasmJsBrowserTest --max-workers=1
./gradlew :filekit-dialogs:testAndroidHostTest --max-workers=1
```

Please **do not run repository-wide `./gradlew build`, `./gradlew check`, or `./gradlew assemble`** (or module-level
aggregate `check` tasks) as part of your normal loop. They compile every target and are rarely needed: CI runs them
on every pull request. In your PR description, list the targeted checks you ran and what you left to CI.

### Writing tests

- Add tests in the closest `src/<target>Test` directory. Default to `commonTest` when the behaviour is shared.
- Name tests `Subject_action_expectation`, for example `FilePicker_openDirectory_returnsFolder`.
- New features need regression coverage for at least one non-JVM target.
- When behaviour depends on native APIs that tests cannot drive (system pickers, permissions, sandboxing), describe
  the manual verification steps in your PR.

## Running the sample app

The sample app lives in `sample/`. Run it when your change affects user-facing behaviour, and update it alongside
library changes when that behaviour shifts.

| Platform            | How to run                                                                    |
|---------------------|-------------------------------------------------------------------------------|
| Android             | Run the `sample.androidApp` configuration from Android Studio                 |
| Desktop (JVM)       | `./gradlew :sample:desktopApp:run`                                            |
| Web (Wasm)          | `./gradlew :sample:webApp:wasmJsBrowserDevelopmentRun`                        |
| Web (JS)            | `./gradlew :sample:webApp:jsBrowserDevelopmentRun`                            |
| iOS / macOS native  | Open `sample/iosApp/FileKit Sample.xcodeproj` in Xcode and run                |
| Windows native      | `./gradlew :sample:windowsNativeApp:runDebugExecutableMingwX64` (on Windows)  |

## Code style

FileKit follows the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html): four-space
indentation, trailing commas where helpful, and `UpperCamelCase` for public types. Composable functions are
PascalCase and take a `modifier` parameter when they render UI. Name files after their primary type.

Formatting is enforced by [ktlint](https://pinterest.github.io/ktlint/) with the
[Compose rules](https://github.com/mrmans0n/compose-rules). The CI lint job is the source of truth for the versions:

```shell
# Download the Compose rules once (version used by CI)
curl -sSLO https://github.com/mrmans0n/compose-rules/releases/download/v0.6.6/ktlint-compose-0.6.6-all.jar

# Check
ktlint '**/*.kt' '**/*.kts' '!**/build/**' -R ktlint-compose-0.6.6-all.jar

# Auto-fix
ktlint '**/*.kt' '**/*.kts' '!**/build/**' -R ktlint-compose-0.6.6-all.jar --format
```

Public APIs need KDoc. FileKit uses explicit API mode, so every public declaration needs an explicit visibility
modifier.

## Documentation

User documentation lives in `docs/` as a [Mintlify](https://mintlify.com) site. If your change adds or changes
public behaviour, update the relevant page in the same PR. Do not rename or move existing pages without adding a
redirect in `docs/docs.json`, so published links keep working.

## Commits and pull requests

**Commits** are short, imperative sentences, often prefixed with an emoji that gives the category:

```
✨ Add WASM directory picker
🐛 Fix iOS picker never returning on early dismiss
📝 Document bookmark data on macOS
♻️ Simplify Android URI resolution
✅ Add Linux tests for copyRecursively
```

Keep related changes squashed together; a reviewer should be able to read the history of a PR.

**Pull requests** should:

1. Target `main` and stay focused on one change.
2. Describe what changed and why, and link the related issue or discussion.
3. List the affected platforms.
4. Report the targeted checks you ran locally and confirm CI is green.
5. Call out documentation and sample updates.
6. Include screenshots or screen recordings when UI behaviour changes.
7. Note any follow-up work.

The [pull request template](.github/pull_request_template.md) walks you through these points.

## Reporting bugs

Use the [bug report form](https://github.com/vinceglb/FileKit/issues/new?template=bug_report.yml). The most useful
reports include the FileKit version, the affected platforms and OS versions, and a **minimal reproducer**: a snippet
or small project that only uses FileKit. Issues that need a reproducer to move forward get the `needs repro` label.

## License

By contributing, you agree that your contributions are licensed under the [MIT License](LICENSE).
