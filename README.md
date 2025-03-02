<div align="center">
  <img src="https://github.com/vinceglb/FileKit/assets/24540801/5ad7fc2d-04fd-4ba7-b5de-b4a7ada753c9" alt="FileKit for Kotlin Multiplatform and Compose Multiplatform" />

  <br>
 
  <h1>FileKit</h1>
  <p>Files, Medias, Folder Picker and File saver library for Kotlin Multiplatform and Compose Multiplatform</p>

  <div>
    <img src="https://img.shields.io/maven-central/v/io.github.vinceglb/filekit-core" alt="FileKit Kotlin Maven Version" />
    <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android" alt="Badge Android" />
		<img src="https://img.shields.io/badge/Platform-iOS%20%2F%20macOS-lightgrey.svg?logo=apple" alt="Badge iOS" />
		<img src="https://img.shields.io/badge/Platform-JVM-8A2BE2.svg?logo=openjdk" alt="Badge JVM" />
    <img src="https://img.shields.io/badge/Platform-WASM%20%2F%20JS-yellow.svg?logo=javascript" alt="Badge JS" />
  </div>

  <br>
</div>

FileKit is a library that allows you to pick and save files in a simple way. On each platform, it uses the native file picker API to provide a consistent experience.

![FileKit Preview](https://github.com/vinceglb/FileKit/assets/24540801/e8a7bc49-41cc-4632-84c4-1013fd23dd76)

## 🌱 Sample projects

You can find 2 sample projects in the `samples` directory:
- `sample-core`: A Kotlin Multiplatform project using FileKit in a shared viewModel targeting Android, JVM, WASM, JS, iOS Swift, macOS Swift and iOS Compose.
- `sample-compose`: A Compose Multiplatform project using FileKit in a Composable targeting Android, iOS, JVM, WASM, 

## ✨ Behind the scene

FileKit uses the native file picker API on each platform:

- On Android, it uses `PickVisualMedia`, `OpenDocument` and `OpenDocumentTree` contracts.
- On iOS, it uses both `UIDocumentPickerViewController` and `PHPickerViewController` APIs.
- On macOS, it uses the `NSOpenPanel` API.
- On JVM, it uses JNA to access the file system on Windows and macOS and XDG Desktop Portal on Linux.
- On WASM / JS, it uses the `input` element with the `file` type.

Also, FileKit uses the bear minimum of dependencies to be as lightweight as possible. 

FileKit Core uses the following libraries:
- [KotlinX Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- Only Android: [AndroidX Activity KTX](https://developer.android.com/jetpack/androidx/releases/activity)
- Only JVM: [Java Native Access - JNA](https://github.com/java-native-access/jna/tree/master)
- Only JVM: [XDG Desktop Portal](https://github.com/hypfvieh/dbus-java)

FileKit Compose uses the following libraries:
- [Jetbrains Compose Runtime](https://github.com/JetBrains/compose-multiplatform)
- Only Android: [AndroidX Activity Compose](https://developer.android.com/jetpack/androidx/releases/activity)

## 😎 Credits

FileKit is inspired by the following libraries:

- [compose-multiplatform-file-picker](https://github.com/Wavesonics/compose-multiplatform-file-picker)
- [peekaboo](https://github.com/onseok/peekaboo)
- [Calf](https://github.com/MohamedRejeb/Calf)
- [jnafilechooser](https://github.com/steos/jnafilechooser)
- [swing-jnafilechooser](https://github.com/DJ-Raven/swing-jnafilechooser)
- [nativefiledialog](https://github.com/mlabbe/nativefiledialog)
- [IFileDialogImp](https://github.com/dbwiddis/IFileDialogImp)
- [IntelliJ Community Foundation](https://github.com/JetBrains/intellij-community/blob/master/platform/util/ui/src/com/intellij/ui/mac/foundation/Foundation.java)
- [file_picker (flutter)](https://pub.dev/packages/file_picker)

---

Made with ❤️ by Vince
