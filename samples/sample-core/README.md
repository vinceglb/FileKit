# Sample Core

This sample project demonstrates the use of FileKit Core in a shared viewModel targeting Android, JVM, WASM, JS, iOS Swift, macOS Swift and iOS Compose.

## 🌱 Important parts

The important parts of this sample are:

- [MainViewModel.kt](https://github.com/vinceglb/FileKit/blob/main/samples/sample-core/shared/src/commonMain/kotlin/io/github/vinceglb/sample/core/MainViewModel.kt) in the shared module. It's here where FileKit is used.
- [App.kt](https://github.com/vinceglb/FileKit/blob/main/samples/sample-core/composeApp/src/commonMain/kotlin/io/github/vinceglb/sample/core/compose/App.kt) in the composeApp module where the state is consumed and the viewModel is called.
- [ContentView.swift](https://github.com/vinceglb/FileKit/blob/main/samples/sample-core/appleApps/macOSApp/ContentView.swift) in the iOSApp and macOSApp modules where the state is consumed and the viewModel is called in Swift.
- [MainActivity.kt](https://github.com/vinceglb/FileKit/blob/main/samples/sample-core/composeApp/src/androidMain/kotlin/io/github/vinceglb/sample/core/compose/MainActivity.kt) where we initialize FileKit in Android.

## 🚀 Running the sample project

By openning FileKit project in Android Studio, IntelliJ IDEA or Fleet, you will be able to run the sample project on all platforms.

```bash
# Run on JVM
./gradlew samples:sample-core:composeApp:run

# Run on WASM
samples:sample-core:composeApp:wasmJsBrowserDevelopmentRun
```
