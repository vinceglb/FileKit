<div align="center">
  <img src="https://github.com/vinceglb/PickerKotlin/assets/24540801/ef67360d-8b43-4337-adb4-b9089b0d88ce" alt="Picker Kotlin for Kotlin Multiplatform and Compose Multiplatform" />

  <br>
 
  <h1>Picker Kotlin</h1>
  <p>Files, Medias, Folder Picker and File saver library for Kotlin Multiplatform and Compose Multiplatform</p>

  <div>
    <img src="https://img.shields.io/maven-central/v/io.github.vinceglb/picker-core" alt="Picker Kotlin Maven Version" />
    <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android" alt="Badge Android" />
		<img src="https://img.shields.io/badge/Platform-iOS%20%2F%20macOS-lightgrey.svg?logo=apple" alt="Badge iOS" />
		<img src="https://img.shields.io/badge/Platform-JVM-8A2BE2.svg?logo=openjdk" alt="Badge JVM" />
    <img src="https://img.shields.io/badge/Platform-WASM%20%2F%20JS-yellow.svg?logo=javascript" alt="Badge JS" />
  </div>

  <br>
</div>

Picker Kotlin is a library that allows you to pick and save files in a simple way. On each platform, it uses the native file picker API to provide a consistent experience.

## 🚀 Quick Start

Pick a file, a directory or save a file in common code:

```kotlin
// Pick a file
val file = Picker.pickFile()

// Pick a directory
val directory = Picker.pickDirectory()

// Save a file
val file = Picker.saveFile(
    extension = "txt",
    bytes = "Hello, World!".encodeToByteArray()
)
```

Get file information in common code:

```kotlin
val filePath = file?.path
val fileName = file?.name
val bytes = file?.readBytes()
```

Compose Multiplatform integration made simple:

```kotlin
// Pick files from Compose
val launcher = rememberFilePickerLauncher(PickerSelectionMode.Multiple) { files ->
    // Handle picked files
}

// Use the pickerLauncher
Button(onClick = { launcher.launch() }) {
    Text("Pick files")
}
```

![Picker Kotlin Preview](https://github.com/vinceglb/PickerKotlin/assets/24540801/e8a7bc49-41cc-4632-84c4-1013fd23dd76)

## 📦 Installation

```gradle
repositories {
    mavenCentral()
}

dependencies {
    // Enables Picker without Compose dependencies
    implementation("io.github.vinceglb:picker-core:0.3.1")

    // Enables Picker with Composable utilities
    implementation("io.github.vinceglb:picker-compose:0.3.1")
}
```

## ⚡ Initialization

Using **Picker Core on Android** requires an initialization. To be able to use the `ActivityResultContract` under the hood, you need to initialize Picker in your `ComponentActivity`.

```kotlin
// MainActivity.kt
class MyApplication : ComponentActivity() {
    override fun onCreate() {
        super.onCreate()
        Picker.init(this)
    }
}
```

In every other case, Picker is ready to use without any initialization.

## 📄 File Picker

### Picker types

You can pick different types of files with `PickerSelectionType`:
- `Image`: Pick an image file.
- `Video`: Pick a video file.
- `ImageAndVideo`: Pick an image or a video file.
- `File`: Pick any file. It is the default type. It's possible to specify a list of extensions.

```kotlin
val imageType = PickerSelectionType.Image
val videoType = PickerSelectionType.Video
val imageAndVideoType = PickerSelectionType.ImageAndVideo
val fileType = PickerSelectionType.File(extensions = listOf("pdf", "docx"))
```

### Picker modes

You can pick files in different modes with `PickerSelectionMode`. The mode will change the output type. `Single` is the default mode.

```kotlin
val singleMode = PickerSelectionMode.Single
val multipleMode = PickerSelectionMode.Multiple
```

### Launch the picker

You can launch the picker with `Picker.pickFile` or `rememberFilePickerLauncher`:

```kotlin
// Picker Core
val file = Picker.pickFile(
    type = PickerSelectionType.Image,
    mode = PickerSelectionMode.Single,
    title = "Pick an image",
    initialDirectory = "/custom/initial/path"
)

// Picker Compose
val launcher = rememberFilePickerLauncher(
    type = PickerSelectionType.ImageAndVideo,
    mode = PickerSelectionMode.Multiple,
    title = "Pick a media",
    initialDirectory = "/custom/initial/path"
) { files ->
    // Handle the picked files
}
launcher.launch()
```

## 📁 Directory Picker

You can pick a directory with `Picker.pickDirectory` or `rememberDirectoryPickerLauncher`:

```kotlin
// Picker Core
val directory = Picker.pickDirectory(
    title = "Pick a directory",
    initialDirectory = "/custom/initial/path"
)

// Picker Compose
val launcher = rememberDirectoryPickerLauncher(
    title = "Pick a directory",
    initialDirectory = "/custom/initial/path"
) { directory ->
    // Handle the picked directory
}
launcher.launch()
```

The directory picker is available on all platforms, expect for WASM / JS. To check if the directory picker is available from the common code, you can use `Picker.isDirectoryPickerSupported()`.

```kotlin
val directoryModeSupported = Picker.isDirectoryPickerSupported()
```

## 💾 Save File Picker

You can save a file with `Picker.saveFile` or `rememberSaveFilePickerLauncher`:

```kotlin
// Picker Core
val file = Picker.saveFile(
    baseName = "myTextFile",
    extension = "txt",
    initialDirectory = "/custom/initial/path",
    bytes = "Hello, World!".encodeToByteArray()
)

// Picker Compose
val launcher = rememberFileSaverLauncher() { file ->
    // Handle the saved file
}
launcher.launch(
    baseName = "myTextFile",
    extension = "txt",
    initialDirectory = "/custom/initial/path",
    bytes = "Hello, World!".encodeToByteArray()
)
```

## 🧑‍💻 PlatformFile and PlatformDirectory

The `PlatformFile` and `PlatformDirectory` classes are wrappers around the platform file system. It allows you to get the file name, path and read the file content in common code.

```kotlin
val platformFile: PlatformFile = ...

val filePath: String? = platformFile.path
val fileName: String = platformFile.name            // Base name with extension
val baseName: String = platformFile.baseName
val extension: String = platformFile.extension
val bytes: ByteArray = platformFile.readBytes()     // suspend function

val platformDirectory: PlatformDirectory = ...
val directoryPath: String? = platformDirectory.path
```

On each platform, you can get the original platform file:

```kotlin
// Android
val uri: Uri = platformFile.uri
val uri: Uri = platformDirectory.uri

// iOS / macOS
val nsUrl: NSURL = platformFile.nsUrl
val nsUrl: NSURL = platformDirectory.nsUrl

// JVM
val file: java.io.File = platformFile.file
val file: java.io.File = platformDirectory.file

// WASM / JS
val file: org.w3c.files.File = platformFile.file
val file: org.w3c.files.File = // PlatformDirectory not supported on WASM / JS
```

## 🌱 Sample projects

You can find 2 sample projects in the `samples` directory:
- `sample-core`: A Kotlin Multiplatform project using Picker in a shared viewModel targeting Android, JVM, WASM, JS, iOS Swift, macOS Swift and iOS Compose.
- `sample-compose`: A Compose Multiplatform project using Picker in a Composable targeting Android, iOS, JVM, WASM, 

## ✨ Behind the scene

Picker Kotlin uses the native file picker API on each platform:

- On Android, it uses `PickVisualMedia`, `OpenDocument` and `OpenDocumentTree` contracts.
- On iOS, it uses both `UIDocumentPickerViewController` and `PHPickerViewController` APIs.
- On macOS, it uses the `NSOpenPanel` API.
- On JVM, it uses JNA to access the file system on Windows and macOS and Awt FileDialog on Linux.
- On WASM / JS, it uses the `input` element with the `file` type.

Also, Picker Kotlin uses the bear minimum of dependencies to be as lightweight as possible. 

Picker Core uses the following libraries:
- [KotlinX Coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- Only Android: [AndroidX Activity KTX](https://developer.android.com/jetpack/androidx/releases/activity)
- Only JVM: [Java Native Access - JNA](https://github.com/java-native-access/jna/tree/master)

Picker Compose uses the following libraries:
- [Jetbrains Compose Runtime](https://github.com/JetBrains/compose-multiplatform)
- Only Android: [AndroidX Activity Compose](https://developer.android.com/jetpack/androidx/releases/activity)

## 😎 Credits

Picker Kotlin is inspired by the following libraries:

- [compose-multiplatform-file-picker](https://github.com/Wavesonics/compose-multiplatform-file-picker)
- [peekaboo](https://github.com/onseok/peekaboo)
- [Calf](https://github.com/MohamedRejeb/Calf)
- [jnafilechooser](https://github.com/steos/jnafilechooser)
- [swing-jnafilechooser](https://github.com/DJ-Raven/swing-jnafilechooser)
- [IntelliJ Community Foundation](https://github.com/JetBrains/intellij-community/blob/master/platform/util/ui/src/com/intellij/ui/mac/foundation/Foundation.java)

---

Made with ❤️ by Vince
