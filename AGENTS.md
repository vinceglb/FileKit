# Repository Guidelines

## Project Structure & Module Organization
FileKit is split across multiplatform modules: `filekit-core` contains platform-agnostic APIs, while dialogs, Compose bindings, and Coil integration live in `filekit-dialogs`, `filekit-dialogs-compose`, and `filekit-coil`. Shared source sets live under `src/*Main`, with platform tests in sibling `src/*Test` directories. Sample apps under `samples/` (`sample-core`, `sample-compose`, `sample-file-explorer`) demonstrate integration patterns; update them alongside library changes when user-facing behaviour shifts. API docs and release notes are tracked in `docs/` and `documentation-v0.8.8.md`.

## Build, Test, and Development Commands
Keep local validation scoped to the changed module and one relevant target, with `--max-workers=1`; run checks sequentially. For example, use `./gradlew :filekit-core:jvmTest --tests '*PlatformFileDeletionTest*' --max-workers=1` for a deletion regression.

**Never run repository-wide `./gradlew assemble`, `./gradlew check`, or `./gradlew build` locally**, alone or combined. They overload the maintainer's Mac. Leave broad builds and multiplatform test matrices, including module-level aggregate `check` tasks, to CI; do not use them as a fallback when a targeted check fails. Report the targeted checks run and any validation left to CI.

Exercise sample apps only when needed for the change. For local publishing smoke tests, scope publishing to the required module and platform instead of publishing every artifact.

For Kotlin formatting/linting, run `ktlint '**/*.kt' '**/*.kts' '!**/build/**' -R ktlint-compose-0.4.28-all.jar`. To auto-fix issues, add `--format` to that command.

## Coding Style & Naming Conventions
Follow Kotlin official style: four-space indentation, trailing commas where helpful, and `UpperCamelCase` for public APIs. Keep expect/actual implementations mirrored across targets and group platform-specific helpers under the corresponding `src/<platform>Main` directory. Compose functions remain PascalCase and should take a `modifier` parameter when rendering UI. Prefer descriptive file names that match the primary type, and keep shared constants in `commonMain` to minimise duplication.

## Testing Guidelines
Add unit tests in the closest `src/<target>Test` directory; default to `commonTest` when behaviour is shared and mirror target-specific coverage otherwise. Test names follow the `Subject_action_expectation` convention (e.g., `FilePicker_openDirectory_returnsFolder`). Before pushing, run the relevant targeted checks under the local validation limits above, and ensure new features include regression coverage for at least one non-JVM target. When behaviour depends on native APIs, document manual verification steps in the PR description.

## Commit & Pull Request Guidelines
Commits are short, imperative statements and often begin with an emoji category (e.g., `✨ Add WASM picker`); keep related changes squashed together. Each PR should describe the change, note affected platforms, call out doc updates, and link issues or discussions when relevant. Attach screenshots or screen recordings when UI behaviour changes. Before requesting review, report targeted validation and CI status, update sample apps if behaviour shifts, and note any follow-up work in the description.

## Agent skills

### Issue tracker

Private agent work is tracked as local Markdown under `.scratch/`. See `specs/agents/issue-tracker.md`.

### Triage labels

Local issues use the canonical triage statuses. See `specs/agents/triage-labels.md`.

### Domain docs

FileKit uses a single-context domain model under `specs/`. See `specs/agents/domain.md`.
