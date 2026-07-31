# Compose Launcher ABI Verification

Baseline: `3380b8c8`

The baseline and current artifacts were built separately. Rebuilding samples is not treated as binary-compatibility evidence.

```bash
git worktree add /tmp/filekit-abi-3380b8c8 3380b8c8
(cd /tmp/filekit-abi-3380b8c8 && ./gradlew assemble)
./gradlew assemble

javap -classpath filekit-dialogs-compose/build/libs/filekit-dialogs-compose-jvm.jar \
  -s -p io.github.vinceglb.filekit.dialogs.compose.FileKitComposeKt

$KONAN_HOME/bin/klib dump-metadata \
  filekit-dialogs-compose/build/libs/filekit-dialogs-compose-wasm-js.klib
$KONAN_HOME/bin/klib dump-metadata \
  filekit-dialogs-compose/build/classes/kotlin/iosSimulatorArm64/main/klib/filekit-dialogs-compose
```

The same `javap` and `klib dump-metadata` commands were run under `/tmp/filekit-abi-3380b8c8`. Every baseline line below was then matched byte-for-byte in the current output. Current output may contain additive declarations.

## JVM baseline descriptors

```text
FileKitPickerException|(Ljava/lang/String;)V
FileKitPickerException|(Ljava/lang/String;Ljava/lang/Throwable;)V
FileKitComposeKt|(Lio/github/vinceglb/filekit/dialogs/FileKitType;Lio/github/vinceglb/filekit/dialogs/FileKitMode;Lio/github/vinceglb/filekit/PlatformFile;Lio/github/vinceglb/filekit/dialogs/FileKitDialogSettings;Lkotlin/jvm/functions/Function1;Landroidx/compose/runtime/Composer;II)Lio/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher;
FileKitComposeKt|(Lio/github/vinceglb/filekit/dialogs/FileKitType;Lio/github/vinceglb/filekit/dialogs/FileKitMode;Lio/github/vinceglb/filekit/PlatformFile;Lio/github/vinceglb/filekit/dialogs/FileKitDialogSettings;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Landroidx/compose/runtime/Composer;II)Lio/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher;
FileKitComposeKt|(Lio/github/vinceglb/filekit/dialogs/FileKitType;Lio/github/vinceglb/filekit/PlatformFile;Lio/github/vinceglb/filekit/dialogs/FileKitDialogSettings;Lkotlin/jvm/functions/Function1;Landroidx/compose/runtime/Composer;II)Lio/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher;
FileKitComposeKt|(Lio/github/vinceglb/filekit/dialogs/FileKitType;Lio/github/vinceglb/filekit/PlatformFile;Lio/github/vinceglb/filekit/dialogs/FileKitDialogSettings;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Landroidx/compose/runtime/Composer;II)Lio/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher;
FileKitCompose_nonAndroidKt|(Lio/github/vinceglb/filekit/PlatformFile;Lio/github/vinceglb/filekit/dialogs/FileKitDialogSettings;Lkotlin/jvm/functions/Function1;Landroidx/compose/runtime/Composer;II)Lio/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher;
FileKitCompose_nonWebKt|(Lio/github/vinceglb/filekit/dialogs/FileKitDialogSettings;Lkotlin/jvm/functions/Function1;Landroidx/compose/runtime/Composer;I)Lio/github/vinceglb/filekit/dialogs/compose/SaverResultLauncher;
FileKitCompose_jvmKt|(Landroidx/compose/ui/window/WindowScope;Lio/github/vinceglb/filekit/dialogs/FileKitType;Lio/github/vinceglb/filekit/dialogs/FileKitMode;Lio/github/vinceglb/filekit/PlatformFile;Lio/github/vinceglb/filekit/dialogs/FileKitDialogSettings;Lkotlin/jvm/functions/Function1;Landroidx/compose/runtime/Composer;II)Lio/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher;
FileKitCompose_jvmKt|(Landroidx/compose/ui/window/WindowScope;Lio/github/vinceglb/filekit/dialogs/FileKitType;Lio/github/vinceglb/filekit/dialogs/FileKitMode;Lio/github/vinceglb/filekit/PlatformFile;Lio/github/vinceglb/filekit/dialogs/FileKitDialogSettings;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Landroidx/compose/runtime/Composer;II)Lio/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher;
FileKitCompose_jvmKt|(Landroidx/compose/ui/window/WindowScope;Lio/github/vinceglb/filekit/dialogs/FileKitType;Lio/github/vinceglb/filekit/PlatformFile;Lio/github/vinceglb/filekit/dialogs/FileKitDialogSettings;Lkotlin/jvm/functions/Function1;Landroidx/compose/runtime/Composer;II)Lio/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher;
FileKitCompose_jvmKt|(Landroidx/compose/ui/window/WindowScope;Lio/github/vinceglb/filekit/dialogs/FileKitType;Lio/github/vinceglb/filekit/PlatformFile;Lio/github/vinceglb/filekit/dialogs/FileKitDialogSettings;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Landroidx/compose/runtime/Composer;II)Lio/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher;
FileKitCompose_jvmKt|(Landroidx/compose/ui/window/WindowScope;Lio/github/vinceglb/filekit/PlatformFile;Lio/github/vinceglb/filekit/dialogs/FileKitDialogSettings;Lkotlin/jvm/functions/Function1;Landroidx/compose/runtime/Composer;II)Lio/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher;
FileKitCompose_jvmKt|(Landroidx/compose/ui/window/WindowScope;Lio/github/vinceglb/filekit/dialogs/FileKitDialogSettings;Lkotlin/jvm/functions/Function1;Landroidx/compose/runtime/Composer;II)Lio/github/vinceglb/filekit/dialogs/compose/SaverResultLauncher;
```

## KLIB baseline declarations

Wasm baseline (5 declarations):

```text
public final fun <T#0 /* PickerResult */, T#1 /* ConsumedResult */> rememberFilePickerLauncher(type: io/github/vinceglb/filekit/dialogs/FileKitType /* = ... */, mode: io/github/vinceglb/filekit/dialogs/FileKitMode<T#0, T#1>, directory: io/github/vinceglb/filekit/PlatformFile? /* = ... */, dialogSettings: io/github/vinceglb/filekit/dialogs/FileKitDialogSettings /* = ... */, onResult: kotlin/Function1<T#1, kotlin/Unit>): io/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher
public final fun <T#0 /* PickerResult */, T#1 /* ConsumedResult */> rememberFilePickerLauncher(type: io/github/vinceglb/filekit/dialogs/FileKitType /* = ... */, mode: io/github/vinceglb/filekit/dialogs/FileKitMode<T#0, T#1>, directory: io/github/vinceglb/filekit/PlatformFile? /* = ... */, dialogSettings: io/github/vinceglb/filekit/dialogs/FileKitDialogSettings /* = ... */, onError: kotlin/Function1<io/github/vinceglb/filekit/dialogs/FileKitPickerException, kotlin/Unit>, onResult: kotlin/Function1<T#1, kotlin/Unit>): io/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher
public final fun rememberFilePickerLauncher(type: io/github/vinceglb/filekit/dialogs/FileKitType /* = ... */, directory: io/github/vinceglb/filekit/PlatformFile? /* = ... */, dialogSettings: io/github/vinceglb/filekit/dialogs/FileKitDialogSettings /* = ... */, onResult: kotlin/Function1<io/github/vinceglb/filekit/PlatformFile?, kotlin/Unit>): io/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher
public final fun rememberFilePickerLauncher(type: io/github/vinceglb/filekit/dialogs/FileKitType /* = ... */, directory: io/github/vinceglb/filekit/PlatformFile? /* = ... */, dialogSettings: io/github/vinceglb/filekit/dialogs/FileKitDialogSettings /* = ... */, onError: kotlin/Function1<io/github/vinceglb/filekit/dialogs/FileKitPickerException, kotlin/Unit>, onResult: kotlin/Function1<io/github/vinceglb/filekit/PlatformFile?, kotlin/Unit>): io/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher
public final fun rememberDirectoryPickerLauncher(directory: io/github/vinceglb/filekit/PlatformFile? /* = ... */, dialogSettings: io/github/vinceglb/filekit/dialogs/FileKitDialogSettings /* = ... */, onResult: kotlin/Function1<io/github/vinceglb/filekit/PlatformFile?, kotlin/Unit>): io/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher
```

iOS simulator ARM64 baseline (8 declarations):

```text
public final fun <T#0 /* PickerResult */, T#1 /* ConsumedResult */> rememberFilePickerLauncher(type: io/github/vinceglb/filekit/dialogs/FileKitType /* = ... */, mode: io/github/vinceglb/filekit/dialogs/FileKitMode<T#0, T#1>, directory: io/github/vinceglb/filekit/PlatformFile? /* = ... */, dialogSettings: io/github/vinceglb/filekit/dialogs/FileKitDialogSettings /* = ... */, onResult: kotlin/Function1<T#1, kotlin/Unit>): io/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher
public final fun <T#0 /* PickerResult */, T#1 /* ConsumedResult */> rememberFilePickerLauncher(type: io/github/vinceglb/filekit/dialogs/FileKitType /* = ... */, mode: io/github/vinceglb/filekit/dialogs/FileKitMode<T#0, T#1>, directory: io/github/vinceglb/filekit/PlatformFile? /* = ... */, dialogSettings: io/github/vinceglb/filekit/dialogs/FileKitDialogSettings /* = ... */, onError: kotlin/Function1<io/github/vinceglb/filekit/dialogs/FileKitPickerException, kotlin/Unit>, onResult: kotlin/Function1<T#1, kotlin/Unit>): io/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher
public final fun rememberFilePickerLauncher(type: io/github/vinceglb/filekit/dialogs/FileKitType /* = ... */, directory: io/github/vinceglb/filekit/PlatformFile? /* = ... */, dialogSettings: io/github/vinceglb/filekit/dialogs/FileKitDialogSettings /* = ... */, onResult: kotlin/Function1<io/github/vinceglb/filekit/PlatformFile?, kotlin/Unit>): io/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher
public final fun rememberFilePickerLauncher(type: io/github/vinceglb/filekit/dialogs/FileKitType /* = ... */, directory: io/github/vinceglb/filekit/PlatformFile? /* = ... */, dialogSettings: io/github/vinceglb/filekit/dialogs/FileKitDialogSettings /* = ... */, onError: kotlin/Function1<io/github/vinceglb/filekit/dialogs/FileKitPickerException, kotlin/Unit>, onResult: kotlin/Function1<io/github/vinceglb/filekit/PlatformFile?, kotlin/Unit>): io/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher
public final fun rememberCameraPickerLauncher(openCameraSettings: io/github/vinceglb/filekit/dialogs/FileKitOpenCameraSettings /* = ... */, onResult: kotlin/Function1<io/github/vinceglb/filekit/PlatformFile?, kotlin/Unit>): io/github/vinceglb/filekit/dialogs/compose/PhotoResultLauncher
public final fun rememberShareFileLauncher(shareSettings: io/github/vinceglb/filekit/dialogs/FileKitShareSettings /* = ... */): io/github/vinceglb/filekit/dialogs/compose/ShareResultLauncher
public final fun rememberDirectoryPickerLauncher(directory: io/github/vinceglb/filekit/PlatformFile? /* = ... */, dialogSettings: io/github/vinceglb/filekit/dialogs/FileKitDialogSettings /* = ... */, onResult: kotlin/Function1<io/github/vinceglb/filekit/PlatformFile?, kotlin/Unit>): io/github/vinceglb/filekit/dialogs/compose/PickerResultLauncher
public final fun rememberFileSaverLauncher(dialogSettings: io/github/vinceglb/filekit/dialogs/FileKitDialogSettings, onResult: kotlin/Function1<io/github/vinceglb/filekit/PlatformFile?, kotlin/Unit>): io/github/vinceglb/filekit/dialogs/compose/SaverResultLauncher
```

The current Wasm KLIB retained all 5 declarations. The current iOS KLIB retained all 8 and added exactly 4 error-aware declarations.

## Precompiled JVM consumer

[`PrecompiledConsumer.java`](abi/PrecompiledConsumer.java) was compiled with `javac` against the baseline core/dialogs/Compose JARs. The resulting class was then run, without recompilation, against the current JARs. It resolved the legacy directory-picker, file-saver, and JVM `WindowScope` methods and exited with status 0.

## Source call shapes

Compile-only tests cover named arguments and trailing lambdas for legacy and error-aware file, directory, saver, camera, share, and JVM `WindowScope` launchers, including generic `FileKitMode` overloads. They include the exact compatibility call:

```kotlin
rememberFilePickerLauncher(
    type = FileKitType.Image,
    onResult = ::handleResult,
)
```
