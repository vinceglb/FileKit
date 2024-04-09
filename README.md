<div align="center">
  <img src="https://github.com/vinceglb/PickerKotlin/assets/24540801/59a2ca58-dfff-4606-b9b9-6c1132dfbd9c" alt="Picker Kotlin for Kotlin Multiplatform and Compose Multiplatform" />

  <br>
 
  <h1>Picker Kotlin</h1>
  <p>Files, Medias and Folder Picker library for Kotlin Multiplatform and Compose Multiplatform</p>

  <div>
    <img src="https://img.shields.io/maven-central/v/io.github.vinceglb/picker-core" alt="Picker Kotlin Maven Version" />
    <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android" alt="Badge Android" />
		<img src="https://img.shields.io/badge/Platform-iOS%20%2F%20macOS-lightgrey.svg?logo=apple" alt="Badge iOS" />
		<img src="https://img.shields.io/badge/Platform-JVM-8A2BE2.svg?logo=openjdk" alt="Badge JVM" />
    <img src="https://img.shields.io/badge/Platform-WASM%20%2F%20JS-yellow.svg?logo=javascript" alt="Badge JS" />
  </div>

  <br>
</div>

Picker Kotlin is a library that allows you to pick files, medias and folders in a simple way. On each platform, it uses the native file picker API to provide a consistent experience.
- Picker Core is compatible with Kotlin Multiplatform without Compose dependencies.
- Picker Compose adds Compose Multiplatform support with a `rememberPickerLauncher` Composable.

## 🚀 Quick Start

### Picker Core

With `Picker`, you can pick files, medias and folders on each target from your common code.

```kotlin
// Choose a mode: SingleFile(), MultipleFiles() or Directory
val mode = PickerSelectionMode.SingleFile()

// Pick a file
val file = Picker.pick(mode)

// Get file information
val fileName = file?.name
val filePath = file?.path
val bytes = file?.readBytes()
```

### Picker Compose

Easily integrate Picker with Compose Multiplatform.

```kotlin
// Choose a mode: SingleFile(), MultipleFiles() or Directory
val mode = PickerSelectionMode.SingleFile()

// Pick a file
val pickerLauncher = rememberPickerLauncher(mode) { file ->
    // Handle the picked file
    val fileName = file?.name
    val filePath = file?.path
    val bytes = file?.readBytes()
}

// Use the pickerLauncher
Button(onClick = { pickerLauncher.launch() }) {
    Text("Pick a file")
}
```

## 📦 Installation

```gradle
repositories {
    mavenCentral()
}

dependencies {
    // Enables Picker without Compose dependencies
    implementation("io.github.vinceglb:picker-core:0.1.0")

    // Enables Picker with rememberPickerLauncher Composable
    implementation("io.github.vinceglb:picker-compose:0.1.0")
}
```

## ⚡ Initialization

No initialization is required, **except for Picker Core on Android**. Picker Compose works out of the box, even on Android.

```kotlin
// MainActivity.kt
class MyApplication : ComponentActivity() {
    override fun onCreate() {
        super.onCreate()
        Picker.init(this)
    }
}
```
## 📖 Documentation

### Picker modes

With `SingleFile()` and `MultipleFiles()` you can specify the file extensions you want to pick. By default, all files are allowed.

```kotlin
val singleFileMode = PickerSelectionMode.SingleFile(
    extensions = listOf("jpg", "jpeg", "png")
)

val multipleFilesMode = PickerSelectionMode.MultipleFiles(
    extensions = listOf("jpg", "jpeg", "png")
)

val directoryMode = PickerSelectionMode.Directory
```

### Picker Core

It is possible to customize the picker dialog:

```kotlin
val files = Picker.pick(
    mode = PickerSelectionMode.MultipleFiles(),
    title = "Pick some files",
    initialDirectory = "/custom/initial/path"
)
```

### Picker Compose

The `rememberPickerLauncher` function allows you to create a launcher that can be used to easily pick files from a Composable.

```kotlin
val mode = PickerSelectionMode.Directory

val pickerLauncher = rememberPickerLauncher(
    mode = mode,
    title = "Pick a directory",
    initialDirectory = "/custom/initial/path"
) { file ->
    // Handle the picked file
}
```

### PlatformFile

The `PlatformFile` class is a wrapper around the platform file system. It allows you to get the file name, path and read the file content in common code.

```kotlin
val platformFile: PlatformFile = ...

val fileName: String = platformFile.name
val filePath: String? = platformFile.path
val bytes: ByteArray = platformFile.readBytes()   // suspend function
```

On each platform, you can get the original platform file:

```kotlin
// Android
val uri: Uri = platformFile.uri

// iOS / macOS
val nsUrl: NSURL = platformFile.nsUrl

// JVM
val file: java.io.File = platformFile.file

// WASM / JS
val file: org.w3c.files.File = platformFile.file
```

### Directory Mode

The directory mode is available on all platforms, expect for WASM / JS. To check if the directory picker is available from the common code, you can use `PickerSelectionMode.Directory.isSupported`.

```kotlin
val directoryModeSupported = PickerSelectionMode.Directory.isSupported
```

## 🌱 Sample projects

You can find 2 sample projects in the `samples` directory:
- `sample-core`: A Kotlin Multiplatform project using Picker in a shared viewModel targeting Android, JVM, WASM, JS, iOS Swift, macOS Swift and iOS Compose.
- `sample-compose`: A Compose Multiplatform project using Picker in a Composable targeting Android, iOS, JVM, WASM, 

## ✨ Behind the scene

Kotlin Picker uses the native file picker API on each platform:

- On Android, it uses the `ActivityResultContract` API.
- On iOS, it uses the `UIDocumentPickerViewController` API.
- On macOS, it uses the `NSOpenPanel` API.
- On JVM, it uses JNA to access the file system (Windows, macOS). Linux is supported but not tested.
- On WASM / JS, it uses the `input` element with the `file` type.

Also, Kotlin Picker uses the bear minimum of dependencies to be as lightweight as possible. 

Picker Core uses the following libraries:
- [KotlinX Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- Only Android: [AndroidX Activity KTX](https://developer.android.com/jetpack/androidx/releases/activity)
- Only JVM: [Java Native Access - JNA](https://github.com/java-native-access/jna/tree/master)

Picker Compose uses the following libraries:
- [Jetbrains Compose Runtime](https://github.com/JetBrains/compose-multiplatform)
- Only Android: [AndroidX Activity Compose](https://developer.android.com/jetpack/androidx/releases/activity)

## 😎 Credits

Kotlin Picker is inspired by the following libraries:

- [compose-multiplatform-file-picker](https://github.com/Wavesonics/compose-multiplatform-file-picker)
- [peekaboo](https://github.com/onseok/peekaboo)
- [jnafilechooser](https://github.com/steos/jnafilechooser)
- [swing-jnafilechooser](https://github.com/DJ-Raven/swing-jnafilechooser)
- [IntelliJ Community Foundation](https://github.com/JetBrains/intellij-community/blob/master/platform/util/ui/src/com/intellij/ui/mac/foundation/Foundation.java)

---

Made with ❤️ by Vince
