# Issue 626: Prevent macOS JVM Runnable-Class Collisions

## Status

Ready for independent plan review.

Issue: [#626](https://github.com/vinceglb/FileKit/issues/626)

Baseline: `origin/main` at `80b901d05a260b7449cadba770d3b033dc063cb7`

## Objective

Prevent a macOS/JVM picker from crashing in `objc_registerClassPair` when another dependency has already registered IntelliJ's process-global `IdeaRunnable` class name. Keep FileKit's existing main-thread dispatch behavior and public API intact.

## Verified Problem

The issue is real and remains present on the current baseline:

- `Foundation.initRunnableSupport()` allocates the Objective-C class `IdeaRunnable` and passes the result directly to `objc_registerClassPair`.
- `Foundation.executeOnMainThread()` subsequently retrieves `IdeaRunnable` by its global name rather than retaining the class FileKit created.
- Apple documents that Objective-C class names occupy a process-wide namespace and that `objc_allocateClassPair` returns `Nil` when the desired name is already in use.
- FileKit maps the native return to `ID : NativeLong`. A safe standalone JNA 5.19.1 probe on this macOS host registered a unique class and allocated it again. The second allocation produced a non-null `ID` whose numeric value was `0`:

  ```text
  collision.isNull=false
  collision.value=0
  ```

  Kotlin nullability alone therefore does not detect this failure; `Foundation.isNil` does.
- The implementation is unchanged from FileKit 0.14.2 in the affected area. The issue's stack and zero pointer are consistent with passing this value into `objc_registerClassPair`.

Primary references:

- [Apple: `objc_allocateClassPair`](https://developer.apple.com/documentation/objectivec/objc_allocateclasspair%28_%3A_%3A_%3A%29?language=objc)
- [Apple: class names must be unique across an app and its frameworks](https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/ProgrammingWithObjectiveC/Conventions/Conventions.html)
- [Apple: Objective-C has a flat, process-global namespace](https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/LoadingCode/Tasks/NameConflicts.html)
- [JetBrains commit that introduced the original internal `IdeaRunnable` adapter](https://github.com/JetBrains/intellij-community/commit/37dd1fb1395dd204dd8b0ee52f0fe59cf6a1ce43)
- [Gradle: JVM tests execute in a separate forked JVM](https://docs.gradle.org/current/userguide/java_testing.html)

## Autonomous Grilling Record

The requested grilling session was resolved without user interruption because the issue, repository, and platform contracts provide sufficient evidence.

### 1. Does FileKit own `IdeaRunnable`?

**Recommendation and decision:** No. Treat any class already registered under that name as foreign state. FileKit copied an IntelliJ-internal adapter into a reusable library; the name does not identify FileKit ownership.

Do not look up and reuse an existing `IdeaRunnable`. Its `run:` implementation and JNA callback lifetime can belong to unrelated code.

### 2. How should the adapter be named?

**Recommendation and decision:** Register a FileKit-specific name, `FileKitMainThreadRunnable`.

This follows Apple's framework-prefix guidance and removes the demonstrated collision with IntelliJ/JBR code. Keep an explicit allocation guard even with the stronger name. A second FileKit copy loaded through another class loader may still contend for the fixed process-global name; it must fail with a managed JVM exception rather than attach to another runtime owner's callback.

Do not generate a random class name in this fix. Supporting multiple independently loaded FileKit runtimes would add a new lifecycle contract and is not required by #626.

### 3. What counts as failed allocation?

**Recommendation and decision:** Both Kotlin `null` and a zero-valued `ID` are failure.

Use `Foundation.isNil` (or a testable helper with exactly the same semantics) immediately after allocation. Throw a descriptive `IllegalStateException` before calling `class_addMethod`, `objc_registerClassPair`, or looking up the class by name.

### 4. May FileKit reuse a same-named class?

**Recommendation and decision:** No, including a class named `FileKitMainThreadRunnable`.

Objective-C method shape is insufficient proof of ownership. A class from another FileKit class loader would retain a callback into different JVM state. A collision must be a controlled initialization failure.

### 5. What is the safe initialization order?

**Recommendation and decision:** Construct the callback, allocate the class, reject nil, add `run:`, register the completed class, then publish the owned support state.

Apple's documented sequence is allocation, class customization, then registration. The current register-before-`class_addMethod` order can leave a permanently registered but incomplete class when method installation fails.

If `class_addMethod` returns false, call `objc_disposeClassPair` on the still-unregistered class and throw. Do not publish either the class or callback before registration completes. Add the JNA binding for `objc_disposeClassPair`; do not attempt to dispose a registered class.

### 6. What state proves successful initialization?

**Recommendation and decision:** One private support value containing both the created class `ID` and the strong JNA `Callback` reference.

Return that support value from initialization while holding `RUNNABLE_LOCK`, and use its stored class `ID` when creating each adapter instance. Do not perform a global `getObjcClass(name)` lookup after initialization. This makes ownership explicit and prevents later code from silently selecting foreign state.

### 7. What concurrency guarantee is required?

**Recommendation and decision:** Preserve the existing single initialization and runnable-ticket mutation under `RUNNABLE_LOCK`.

Within one FileKit runtime owner, concurrent callers must observe the same fully initialized support value. The Objective-C namespace is process-global, but a lock in one class loader cannot coordinate unrelated owners; the nil guard is the safe boundary for that case. No process-wide inter-library lock or foreign-class adoption belongs in this issue.

### 8. How should the crash path be tested safely?

**Recommendation and decision:** Combine a platform-independent lifecycle seam with one macOS-gated native regression test.

The pure tests must simulate Kotlin null, `ID(0)`, method-installation failure, and success without invoking native registration. The native test must pre-register `IdeaRunnable`, then run FileKit's main-thread adapter and verify the runnable executes. It must not open a picker.

Gradle executes JVM tests in a forked test JVM, separate from the Gradle daemon. If the native regression is reintroduced, the test worker can terminate abnormally while the daemon and developer session remain isolated. Keep the native scenario in its own test class so it can be run alone while diagnosing a failure.

### 9. How should the native test be gated?

**Recommendation and decision:** Keep it in `jvmTest`, but return immediately unless `PlatformUtil.current == Platform.MacOS`, matching existing repository practice.

No build-script task or new source set is needed. The `test-desktop` CI matrix already runs `./gradlew jvmTest` on `macos-26`, so the regression executes on a real macOS Objective-C runtime while Linux and Windows continue to compile it without loading Foundation.

### 10. Should this replace the adapter with Grand Central Dispatch?

**Recommendation and decision:** No.

GCD could remove dynamic class registration, but it changes the scheduling bridge, callback lifetime, and native bindings. The namespaced adapter plus defensive lifecycle fixes the reported crash with a small, reviewable change. Leave the existing GCD TODO and broader Foundation modernization for separate work.

### 11. Does this need a public ADR or Mintlify update?

**Recommendation and decision:** No.

The change is internal, issue-scoped, and reversible; it does not satisfy the threshold for an architectural decision record. It changes no public API or documented picker behavior. Keep this plan and glossary in `specs/`; do not modify `docs/`.

## Implementation Plan

### 1. Make Objective-C class construction defensive

Update:

- `filekit-dialogs/src/jvmMain/kotlin/io/github/vinceglb/filekit/dialogs/platform/mac/foundation/FoundationLibrary.kt`
- `filekit-dialogs/src/jvmMain/kotlin/io/github/vinceglb/filekit/dialogs/platform/mac/foundation/Foundation.kt`

Changes:

1. Add the `objc_disposeClassPair` JNA declaration and a narrowly scoped wrapper only if the lifecycle helper needs it.
2. Introduce a small internal top-level, platform-independent registration helper in `Foundation.kt` (or a sibling internal file only if that keeps the lifecycle testable). It must accept operations for allocate, add-method, register, and dispose so tests can verify behavior without initializing the native-loading `Foundation` object.
3. Make the helper:
   - reject both `null` and `ID(0)` allocation results;
   - add `run:` before registration;
   - dispose the unregistered class if method addition returns false;
   - never register or dispose a nil class;
   - return the created class only after registration completes.
4. Use the fixed name `FileKitMainThreadRunnable`.
5. Replace `ourRunnableCallback` as the initialization flag with a private support value that retains both the class `ID` and callback.
6. Keep initialization under `RUNNABLE_LOCK`, return the initialized support to `executeOnMainThread`, and allocate the adapter instance from the stored class `ID`.
7. Update comments and local names from IntelliJ/`IdeaRunnable` terminology to the glossary's runnable-adapter language.

Do not change runnable-ticket generation, autorelease-pool behavior, `performSelectorOnMainThread:withObject:waitUntilDone:`, picker code, or any public declaration.

### 2. Add deterministic lifecycle regression tests

Add:

- `filekit-dialogs/src/jvmTest/kotlin/io/github/vinceglb/filekit/dialogs/platform/mac/foundation/ObjcRunnableClassRegistrationTest.kt`

Cover:

1. A Kotlin-null allocation throws a descriptive managed exception and does not add, register, or dispose.
2. A non-null `ID(0)` allocation does the same.
3. A failed `class_addMethod` disposes the allocated, unregistered class exactly once and never registers it.
4. A successful path records the exact order `allocate -> add method -> register`, returns the allocated class, and does not dispose it.

Keep these tests native-free and runnable on every JVM CI host.

### 3. Add the real macOS collision regression

Add:

- `filekit-dialogs/src/jvmTest/kotlin/io/github/vinceglb/filekit/dialogs/platform/mac/foundation/FoundationRunnableIntegrationTest.kt`

On macOS only:

1. Resolve `NSObject`.
2. If `IdeaRunnable` is absent, allocate and register a minimal class with that exact name. If it is already present, leave it untouched.
3. Call `Foundation.executeOnMainThread(withAutoreleasePool = true, waitUntilDone = true)` with a runnable that records execution.
4. Assert the runnable completed.

The test must not call `objc_registerClassPair` with a nil value, reuse the foreign class, show a picker, depend on a UI selection, or attempt to dispose the registered foreign class. On non-macOS hosts, return before touching `Foundation`.

## Acceptance Criteria

- A process containing `IdeaRunnable` can initialize FileKit's adapter and execute a runnable without `SIGSEGV`.
- FileKit registers and uses `FileKitMainThreadRunnable`, not `IdeaRunnable`.
- Kotlin-null and zero-valued JNA allocation failures become descriptive JVM exceptions before any unsafe native call.
- The `run:` method is added before `objc_registerClassPair`.
- Pre-registration failure does not leak an allocated class; a registered class is never disposed.
- Callback and class ownership are published together and retained for the runtime owner's lifetime.
- Concurrent calls within one FileKit runtime remain serialized during initialization and share one support value.
- No public API, picker result, platform other than macOS/JVM, Mintlify page, sample, or dependency changes.
- Focused tests, the module check, the repository-wide `check`, assembly, formatting, and CI all pass.

## Verification

Run in this order:

```bash
./gradlew :filekit-dialogs:jvmTest \
  --tests 'io.github.vinceglb.filekit.dialogs.platform.mac.foundation.ObjcRunnableClassRegistrationTest' \
  --no-daemon

./gradlew :filekit-dialogs:jvmTest \
  --tests 'io.github.vinceglb.filekit.dialogs.platform.mac.foundation.FoundationRunnableIntegrationTest' \
  --no-daemon

./gradlew :filekit-dialogs:check --no-daemon
./gradlew check --no-daemon
./gradlew assemble --no-daemon

KTLINT_DIR="$(mktemp -d)"
curl -sSLo "$KTLINT_DIR/ktlint" \
  https://github.com/pinterest/ktlint/releases/download/1.8.0/ktlint
chmod +x "$KTLINT_DIR/ktlint"
curl -sSLo "$KTLINT_DIR/ktlint-compose-0.6.0-all.jar" \
  https://github.com/mrmans0n/compose-rules/releases/download/v0.6.0/ktlint-compose-0.6.0-all.jar

"$KTLINT_DIR/ktlint" \
  filekit-dialogs/src/jvmMain/kotlin/io/github/vinceglb/filekit/dialogs/platform/mac/foundation/Foundation.kt \
  filekit-dialogs/src/jvmMain/kotlin/io/github/vinceglb/filekit/dialogs/platform/mac/foundation/FoundationLibrary.kt \
  filekit-dialogs/src/jvmTest/kotlin/io/github/vinceglb/filekit/dialogs/platform/mac/foundation/ObjcRunnableClassRegistrationTest.kt \
  filekit-dialogs/src/jvmTest/kotlin/io/github/vinceglb/filekit/dialogs/platform/mac/foundation/FoundationRunnableIntegrationTest.kt \
  -R "$KTLINT_DIR/ktlint-compose-0.6.0-all.jar"

git diff --check
```

The lint setup mirrors `.github/workflows/ci.yml` versions while keeping downloaded tools outside the checkout. The second Gradle command is meaningful only on macOS; record the macOS and JDK versions in the PR. No manual picker interaction is required because the native integration test exercises the failing adapter path directly and deterministically. If a UI smoke test is performed, treat it as supplemental rather than a replacement for the collision regression.

## Pull Request and CI Notes

- Use `Fixes #626`.
- State that the affected target is macOS/JVM only and that the public API is unchanged.
- Explain the non-null zero-valued JNA result and why `isNil` is required.
- Call out that the macOS native regression intentionally pre-registers the foreign legacy name.
- Confirm the `test-desktop` `macos-26` job executed the integration test; a green Linux/Windows skip alone is insufficient evidence.
- Do not add a release note, sample change, or Mintlify documentation for this internal crash fix unless maintainers request one during PR review.

## Out of Scope

- Replacing `performSelectorOnMainThread` with GCD.
- Refactoring unrelated Foundation/JNA bindings.
- Reusing or mutating any pre-existing Objective-C class.
- Supporting multiple isolated FileKit copies or class loaders in one process beyond safe, managed collision failure.
- Changing picker threading, modality, autorelease behavior, or public APIs.
- Updating `docs/`.
