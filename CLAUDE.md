# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :filekit-core:build

# Run tests
./gradlew test

# Run specific platform tests
./gradlew :filekit-core:jvmTest
./gradlew :filekit-core:wasmJsTest
./gradlew :filekit-core:iosSimulatorArm64Test

# Clean build
./gradlew clean

# Publish to Maven Central
./gradlew publishToSonatype
```

## Project Architecture

FileKit is a Kotlin Multiplatform library for cross-platform file operations. The codebase is organized into several modules:

### Core Modules
- **filekit-core**: Core file operations and PlatformFile abstraction
- **filekit-dialogs**: File picker and save dialogs 
- **filekit-dialogs-compose**: Compose Multiplatform integration
- **filekit-coil**: Coil image loading integration

### Platform Structure
The library uses Kotlin Multiplatform's hierarchical structure:
- `commonMain`: Shared code across all platforms
- `webMain`: Web-specific implementations (JS/WASM)
- `nonWebMain`: Non-web platforms (Android, iOS, JVM)
- `jvmAndNativeMain`: JVM and native platforms
- Platform-specific: `androidMain`, `iosMain`, `jvmMain`, etc.

### Key Concepts
- **PlatformFile**: Core abstraction for files across platforms, with platform-specific implementations
- **FileKit**: Main API class with platform-specific implementations
- **BookmarkData**: Persistent file access on non-web platforms
- Platform-specific exception handling and utilities

### Testing
Tests are organized by platform with shared test resources in `nonWebTest/resources/`. iOS tests require copying test resources via the `copyTestResourcesToIos` task.

## Development Notes

- Uses explicit API mode (`explicitApi()`)
- Supports Android (minSdk 21), iOS, macOS, JVM, JS, and WASM
- Version managed in `gradle.properties` (VERSION_NAME=0.10.0)
- Uses Gradle version catalogs in `gradle/libs.versions.toml`