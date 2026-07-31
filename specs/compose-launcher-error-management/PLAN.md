# Compose Launcher Error Management

## Status

Approved after independent design review. Implementation is complete and awaiting independent code review.

Baseline: `main` at `3380b8c8` (merge of PR #631).

## Objective

Give every callback-style Compose launcher a consistent, explicit failure path without changing the throwing behavior of FileKit's suspend functions, conflating user cancellation with failure, swallowing coroutine cancellation, or breaking applications compiled against existing launcher overloads.

The implementation should make this invariant true across every platform adapter for a supported launch while no earlier launch from the same launcher is pending:

> A launch terminates exactly once as a successful result, user cancellation, coroutine cancellation, or an expected operational failure.

## Verified Current Behavior

- `rememberFilePickerLauncher` now catches its documented `FileKitPickerException` and can route it to `onError`. Its original overloads remain as binary-compatible adapters that ignore that error.
- Non-Android `rememberDirectoryPickerLauncher`, JVM/Native `rememberFileSaverLauncher`, iOS `rememberCameraPickerLauncher`, and mobile `rememberShareFileLauncher` each start a suspend operation in a Compose-owned coroutine without an error channel.
- Those underlying operations have real failure paths. Examples include unsupported or failed desktop native dialogs, iOS file-saver temporary-file preparation, camera destination handling, and share-sheet launch failures.
- Android Compose launchers use Activity Result adapters rather than the suspend implementations. Several launch failures are currently converted to `null`, making them indistinguishable from user cancellation; the Android file saver does not consistently guard synchronous launch failure.
- Several iOS operations present through a nullable view controller from inside a suspended continuation. A missing presenter can currently result in a silent no-op or a continuation that never completes.
- Existing public overload descriptors must remain present. Adding a defaulted `onError` parameter to an existing function is source-compatible after recompilation but is not sufficient for already-compiled JVM or KLIB consumers.

## Design Alternatives

### 1. One ambient Compose error provider

Install an application- or subtree-wide handler through a `CompositionLocal`, then let all launchers report to it implicitly.

This has the smallest public surface and supports centralized logging, but it makes local behavior harder to discover, introduces precedence rules beside the already-published file-picker `onError`, and changes error handling through ambient state.

**Decision:** Reject as the primary interface. It can be reconsidered later as an additive convenience if repeated per-launcher handlers become a demonstrated problem.

### 2. A failure envelope with extensible codes and platform details

Return a non-throwable object containing operation identifiers, stable failure codes, the cause, and optional platform details.

This is flexible for telemetry, but exposes several new concepts before FileKit has evidence that callers need programmatic recovery by failure code. It also creates two error models: exceptions for suspend callers and envelopes for Compose callers.

**Decision:** Reject for now. Preserve the same exception model at both seams.

### 3. Explicit `onError` overloads backed by one internal execution module

Keep default launcher usage unchanged and add overloads with a required `onError` parameter immediately before `onResult`. Normalize only expected dialog-operation failures into one dialog-specific exception hierarchy and route them through a common internal runner.

This follows the interface already established for `rememberFilePickerLauncher`, keeps behavior visible at the call site, and concentrates cancellation and callback-dispatch rules in one module.

**Decision:** Adopt.

## Public Error Contract

### Dialog exception

Add one public, open `FileKitDialogException` extending `FileKitException` as a narrow recoverability marker:

```kotlin
public open class FileKitDialogException : FileKitException {
    public constructor(message: String)
    public constructor(message: String, cause: Throwable)
}
```

Change `FileKitPickerException` to extend `FileKitDialogException`, while preserving both existing constructor descriptors.

The new type intentionally has no operation enum, failure code, or platform details. The launcher call site already identifies the operation. Its purpose is only to distinguish callback-safe operational failures from unrelated `FileKitException` subclasses such as initialization, configuration, bookmark, and filesystem failures.

This gives callers one reusable handler:

```kotlin
fun handleDialogError(error: FileKitDialogException) {
    logger.error(error)
}
```

Because function parameters are contravariant, that handler can also be passed to the existing file-picker overload that expects `(FileKitPickerException) -> Unit`.

Changing the direct superclass is a go/no-go requirement for this interface. Before broad implementation edits, verify that the existing `FileKitPickerException` constructors and public launcher descriptors remain linkable for JVM and KLIB consumers. If that prototype fails, stop, revise this public contract, and repeat plan review; do not ship two unrelated exception families or an unsafe cast.

### Failure classification

`FileKitDialogException` represents an expected operational failure of opening, presenting, resolving, or completing a dialog-backed operation. Platform adapters must preserve the original exception as `cause` when one exists.

The following are not dialog failures and must not be routed to `onError`:

- user cancellation;
- coroutine `CancellationException`;
- invalid arguments and unsupported programmer-selected modes;
- initialization or configuration errors such as `FileKitNotInitializedException`;
- Android `FileProvider` authority/path configuration failures;
- exceptions thrown by the application's `onResult` or `onError` callback;
- VM errors, linkage errors, assertions, and other unexpected defects.

Do not catch every `Throwable` or every `Exception` at the Compose seam. Convert known operational failures at the closest platform adapter, so the common runner only needs to catch `FileKitDialogException`.

## Compose Interface

Keep every existing overload unchanged and add error-aware siblings:

```kotlin
@Composable
public fun rememberDirectoryPickerLauncher(
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): PickerResultLauncher

@Composable
public fun rememberFileSaverLauncher(
    dialogSettings: FileKitDialogSettings,
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): SaverResultLauncher

@Composable
public fun rememberCameraPickerLauncher(
    openCameraSettings: FileKitOpenCameraSettings = FileKitOpenCameraSettings.createDefault(),
    onError: (FileKitDialogException) -> Unit,
    onResult: (PlatformFile?) -> Unit,
): PhotoResultLauncher

@Composable
public fun rememberShareFileLauncher(
    shareSettings: FileKitShareSettings = FileKitShareSettings.createDefault(),
    onError: (FileKitDialogException) -> Unit,
): ShareResultLauncher
```

The error parameter is required on the new overloads. Do not replace the old overload with a defaulted parameter.

Mirror the directory-picker and file-saver overloads on the JVM `WindowScope` adapters. Preserve the current file-picker overloads and their narrower `FileKitPickerException` callback.

## Observable Outcome Rules

For every error-aware launcher:

1. Success invokes `onResult` exactly once.
2. User cancellation invokes `onResult(null)` exactly once for result-bearing launchers.
3. Expected operational failure invokes `onError` exactly once and never invokes `onResult`.
4. Coroutine cancellation propagates and invokes neither callback.
5. An exception thrown by either application callback propagates unchanged and is never fed back into `onError`.
6. Exhausting a platform fallback is an error; choosing to cancel an opened system dialog is cancellation.

For compatibility overloads:

- Keep the merged file-picker behavior: ignore `FileKitPickerException` and do not invoke `onResult` for that failure.
- For directory, saver, and camera launchers, map an expected failure to `onResult(null)`. This preserves Android's existing observable behavior while replacing non-Android coroutine crashes with the only failure representation available to those legacy interfaces.
- For the share launcher, ignore the expected failure because the legacy interface has no callback.

Document this compatibility behavior explicitly. New code that must distinguish cancellation from failure uses the error-aware overload.

For state-tracking file-picker modes, continue delivering selection-processing failures through `FileKitPickerState.Failed`. The launcher's `onError` is for failures that occur before a state flow can be handed to the caller; do not report the same failure through both channels.

## Internal Module and Seam

Add a reusable internal Compose execution module in `commonMain` for directory, saver, camera, and share launchers:

```kotlin
internal suspend fun <Result> runDialogLauncher(
    openDialog: suspend () -> Result,
    onError: (FileKitDialogException) -> Unit,
    onResult: (Result) -> Unit,
)
```

Its implementation must isolate the dialog call from callback invocation:

1. Execute `openDialog` inside the `try` block.
2. Catch only `FileKitDialogException`, invoke `onError`, and return.
3. Invoke `onResult` after the `try` block.

This placement ensures callback exceptions are not reclassified. `CancellationException` propagates naturally because it is not a `FileKitDialogException`; add an explicit regression test so this remains an intentional invariant.

Keep the existing picker-specific `runFilePickerLauncher` adapter because its public error callback is narrower: `(FileKitPickerException) -> Unit`. Do not pass that callback to the broader runner and do not use casts. Every operational failure in the file-picker path, including missing iOS presenters and exhausted Android launch fallbacks, must be normalized to `FileKitPickerException` rather than only the base class.

Keep file-picker mode consumption outside the protected dialog call so exceptions produced by application collection or callbacks are not caught.

All Compose implementations must read the latest callbacks through `rememberUpdatedState`, including `onError`.

## Platform Adapter Changes

### Android

- Replace Boolean-only `launchPickerSafely` and `launchCameraSafely` outcomes with an internal result that distinguishes launched, fallback-required, and failed-with-cause.
- File/gallery picker: attempt the existing fallback first and report a dialog failure only when every viable launcher fails.
- Directory picker: convert `ActivityNotFoundException` into a directory-picker failure.
- File saver: guard synchronous `launcher.launch` failures and report them instead of letting them escape.
- Camera: keep runtime permission denial as cancellation; convert `ActivityNotFoundException`, launch-time `SecurityException`, and genuine destination I/O preparation failures into camera-picker failures. Let `FileProvider.getUriForFile` authority/path configuration errors propagate as configuration errors.
- Share: convert chooser creation or `startActivity` launch failures into share failures.
- Preserve pending-launch cleanup before dispatching either callback so a failed launch cannot later consume a stale Activity Result.

### iOS

- Resolve and require the presenter before entering `suspendCancellableCoroutine`. A missing presenter is a dialog failure; no continuation may be left pending because a nullable presenter was unavailable.
- Directory and document picker: cancellation remains `null`; synchronously detectable setup failures become dialog failures.
- File saver: convert temporary-directory, URL-construction, empty-file-write, and synchronously detectable setup failures into file-saver failures.
- Camera: cancellation and permission/user dismissal remain `null`; destination encoding or write failure becomes a camera-picker failure instead of `null`.
- Share: a missing presenter becomes a share failure rather than a silent return.
- Do not claim generic UIKit presentation-failure detection: `presentViewController` has no failure callback. Only failures FileKit can observe synchronously or through its delegates belong in this contract.
- Keep each delegate/controller strongly owned for the supported single in-flight launch and guard completion so success, dismissal, and coroutine cancellation cannot complete the same continuation twice. Register cancellation cleanup before presentation.

The existing interface does not define concurrent or re-entrant launches from the same launcher. This work must document that only one launch may be pending per launcher and must prevent an overlapping iOS launch from overwriting the active delegate/continuation owner. A second launch should fail immediately as an `IllegalStateException`, which is a programmer error and is not routed to `onError`. Add a deterministic ownership-policy test. General multi-launch support is out of scope.

### JVM, macOS Native, Windows Native, JS, and WASM

- Convert known native dialog initialization, presentation, and result-resolution failures into the appropriate `FileKitDialogException`, preserving the cause.
- Treat genuinely unsupported runtime paths as operational failures when they can occur through a documented launcher configuration; keep invalid caller arguments as programming errors.
- Preserve user-dismissal behavior as `null`.
- Do not change native-dialog threading, parent-window selection, picker modality, browser activation rules, or result ordering as part of this work.

## Implementation Plan

### 1. Establish and verify the exception interface

Prototype and verify the superclass change first. Then add the dialog exception under `filekit-dialogs/src/commonMain` and adapt `FileKitPickerException`.

Add KDoc to every suspend dialog operation documenting its dialog failure and cancellation behavior. Suspend callers continue receiving exceptions; no suspend function gains a `Result` return type.

### 2. Normalize expected failures at platform seams

Update the Android, iOS, JVM, macOS Native, Windows Native, JS, and WASM adapters operation by operation. Replace raw operational `IllegalStateException`, `RuntimeException`, `ActivityNotFoundException`, and relevant `SecurityException` paths with `FileKitDialogException` while preserving their cause.

Do not mechanically replace every raw exception in a platform file. Classify each throw site against the failure rules above.

### 3. Generalize callback execution

Add the common runner and migrate non-Android directory, saver, camera, and share launchers through it. Keep the picker-specific runner and its typed callback. Keep operation-specific result consumption outside the protected call.

### 4. Add error-aware overloads and compatibility adapters

Add the new common/non-Web/mobile declarations, all required actual implementations, and the JVM `WindowScope` forwarding overloads. Keep every existing declaration as a real function with its original descriptor.

Implement the legacy outcome mappings defined above instead of relying on a default parameter.

### 5. Align Android's direct Activity Result adapters

Route Android synchronous launch failures into the same caller-visible contract, preserving fallback order, permission-denial behavior, process-recreation state, and pending-state cleanup.

### 6. Update examples and documentation

- Update the directory picker, file saver, camera picker, and share file documentation with error-aware examples and the cancellation distinction.
- Update representative shared sample screens for each operation to display or log `FileKitDialogException` rather than silently failing.
- Keep the short quick-start examples on the compatibility overload so the default case remains trivial.
- Document that one `(FileKitDialogException) -> Unit` handler can be reused across launchers, including the file picker.

## Test Plan

### Common tests

Test the internal runner through its interface:

1. Success invokes only `onResult`.
2. `FileKitDialogException` invokes only `onError`.
3. `CancellationException` propagates and invokes neither callback.
4. An unexpected exception propagates.
5. An exception from `onResult` propagates and does not invoke `onError`.
6. An exception from `onError` propagates exactly once.
7. The original cause survives normalization.

Add compile-time call-shape coverage for trailing-lambda and named-parameter uses of every legacy and error-aware overload.

### Android host tests

Cover:

- primary picker failure followed by successful fallback;
- exhaustion of both picker launchers reports failure;
- directory and saver Activity Result launch failure;
- camera permission denial versus camera launch failure;
- destination URI failure;
- share launch failure;
- pending state is cleared before failure dispatch;
- legacy overloads retain their documented null/ignore behavior.

### Native and desktop tests

- Add an iOS-testable presenter-resolution seam proving a missing presenter completes with failure instead of hanging.
- Add iOS file-saver preparation and camera-write outcome tests without presenting real UI.
- Add an iOS ownership-policy test proving an overlapping launch cannot overwrite the active continuation owner.
- Add JVM/native tests that map representative dialog initialization/result failures while preserving cancellation.
- Ensure at least one non-JVM target runs the common runner and exception tests.

### Compatibility verification

Before changing implementation behavior:

1. Record the JVM descriptors and KLIB declarations for all existing launchers at baseline `3380b8c8`.
2. After the change, prove those declarations still exist unchanged.
3. Compile a small consumer against the baseline artifacts and run/link it against the new artifacts for JVM; perform the equivalent KLIB compatibility check available in the project toolchain.
4. Cover the exact named-parameter call shape:

   ```kotlin
   rememberFilePickerLauncher(
       type = FileKitType.Image,
       onResult = ::handleResult,
   )
   ```

Do not treat recompiling the samples as proof of binary compatibility.

## Acceptance Criteria

- Every Compose launcher offers an explicit error-aware interface.
- Existing launcher overloads and their compiled descriptors remain available.
- A single `FileKitDialogException` handler can be reused across all launchers.
- User cancellation and expected operational failure are distinguishable through the new overloads.
- Coroutine cancellation and unexpected defects are never swallowed.
- Result and error callbacks are mutually exclusive and invoked at most once per launch.
- Missing iOS presenters cannot leave a suspend call pending indefinitely.
- Android only reports picker launch failure after configured fallbacks are exhausted.
- Suspend APIs continue throwing and document their expected dialog failure.
- Relevant docs and sample screens demonstrate the new contract.
- Focused tests, module checks, every CI target represented in `.github/workflows/ci.yml`, formatting, root `check`, and root `assemble` pass.

## Verification

Run at minimum:

```bash
./gradlew :filekit-dialogs:check :filekit-dialogs-compose:check --no-daemon
./gradlew testAndroidHostTest --no-daemon
./gradlew jvmTest --no-daemon
./gradlew iosSimulatorArm64Test --no-daemon
./gradlew macosArm64Test --no-daemon
./gradlew linuxX64Test --no-daemon
./gradlew check assemble --no-daemon

ktlint '**/*.kt' '**/*.kts' '!**/build/**' \
  -R ktlint-compose-0.6.0-all.jar

git diff --check
```

Use the ktlint and Compose-rule versions pinned by `.github/workflows/ci.yml`.

Perform supplemental manual smoke tests for one launcher on Android and iOS. Manual interaction supplements the deterministic adapter tests; it does not replace them.

## Pull Request Structure

Ship this as one cohesive PR with two reviewable commits:

1. **Dialog failure contract and platform normalization** in `filekit-dialogs`, including suspend KDoc and platform tests.
2. **Compose launcher error callbacks** in `filekit-dialogs-compose`, including compatibility overloads, samples, docs, and callback tests.

Keeping both commits in one PR allows CI and reviewers to validate the complete caller-visible contract while preserving a clear internal seam between suspend error normalization and Compose callback adaptation.

## Out of Scope

- Replacing nullable cancellation results with a new sealed result type.
- Adding retries, fallback UI, logging, crash reporting, or user-facing error messages inside FileKit.
- Adding an ambient Compose error provider.
- Exposing native status codes or platform-specific error-detail types.
- Supporting multiple concurrent launches from the same launcher.
- Changing picker selection modes, ordering, limits, permissions policy, dialog ownership, or lifecycle guidance unrelated to terminal failure.
- Catching callback exceptions or unexpected `Throwable`s merely to keep the application alive.
