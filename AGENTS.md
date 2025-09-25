# Repository Guidelines

## Project Structure & Module Organization
FileKit is split across multiplatform modules: `filekit-core` contains platform-agnostic APIs, while dialogs, Compose bindings, and Coil integration live in `filekit-dialogs`, `filekit-dialogs-compose`, and `filekit-coil`. Shared source sets live under `src/*Main`, with platform tests in sibling `src/*Test` directories. Sample apps under `samples/` (`sample-core`, `sample-compose`, `sample-file-explorer`) demonstrate integration patterns; update them alongside library changes when user-facing behaviour shifts. API docs and release notes are tracked in `docs/` and `documentation-v0.8.8.md`.

## Build, Test, and Development Commands
Use `./gradlew assemble` to ensure all published artifacts compile before raising a PR. Run `./gradlew :filekit-core:check :filekit-dialogs:check` to execute the multiplatform test matrix for the primary modules. Sample apps can be exercised with `./gradlew :samples:sample-compose:composeApp:run` (desktop) or by opening the Gradle targets in Android Studio for mobile builds. For smoke testing local publishing, run `./gradlew publishToMavenLocal` and consume the artifacts from a sample project.

## Coding Style & Naming Conventions
Follow Kotlin official style: four-space indentation, trailing commas where helpful, and `UpperCamelCase` for public APIs. Keep expect/actual implementations mirrored across targets and group platform-specific helpers under the corresponding `src/<platform>Main` directory. Compose functions remain PascalCase and should take a `modifier` parameter when rendering UI. Prefer descriptive file names that match the primary type, and keep shared constants in `commonMain` to minimise duplication.

## Testing Guidelines
Add unit tests in the closest `src/<target>Test` directory; default to `commonTest` when behaviour is shared and mirror target-specific coverage otherwise. Test names follow the `Subject_action_expectation` convention (e.g., `FilePicker_openDirectory_returnsFolder`). Run `./gradlew check` locally before every push and ensure new features include regression coverage for at least one non-JVM target. When behaviour depends on native APIs, document manual verification steps in the PR description.

## Commit & Pull Request Guidelines
Commits are short, imperative statements and often begin with an emoji category (e.g., `✨ Add WASM picker`); keep related changes squashed together. Each PR should describe the change, note affected platforms, call out doc updates, and link issues or discussions when relevant. Attach screenshots or screen recordings when UI behaviour changes. Before requesting review, verify CI-critical tasks (`assemble`, `check`), update sample apps if behaviour shifts, and note any follow-up work in the description.
