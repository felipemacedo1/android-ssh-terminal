# Development Guide

This guide helps you set up a development environment for KTAR.

## Prerequisites

- **Java**: JDK 17 or higher
- **Android SDK**: API 26 (Android 8.0) or higher
- **Android Studio**: Latest stable version recommended
- **Git**: Latest version
- **Gradle**: Included via gradlew

## Environment Setup

### 1. Clone the Repository

```bash
git clone https://github.com/felipemacedo1/ktar.git
cd ktar
```

### 2. Configure Android SDK

KTAR requires Android API 26+. If you don't have it installed:

```bash
# Via Android Studio SDK Manager: Tools > SDK Manager
# Or via command line:
sdkmanager "platforms;android-35" "build-tools;35.0.0"
```

Create or update `local.properties`:

```properties
sdk.dir=/path/to/Android/Sdk
```

### 3. Build the Project

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing configuration)
./gradlew assembleRelease

# Build and run all checks
./gradlew build
```

## Running the App

### On Emulator

```bash
# Create emulator via Android Studio or command line
emulator -avd emulator_name

# Install and run debug build
./gradlew installDebug
adb shell am start -n com.ktar/.MainActivity
```

### On Physical Device

1. Enable USB debugging on device
2. Connect via USB
3. Run:

```bash
./gradlew installDebug
```

## Testing

### Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests "com.ktar.utils.LoggerTest"
```

### Instrumentation Tests

```bash
# Run on connected device/emulator
./gradlew connectedAndroidTest
```

## Code Quality

### Lint

```bash
./gradlew lint
```

### Kotlin Linter (ktlint)

```bash
./gradlew ktlint

# Auto-format
./gradlew ktlintFormat
```

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/ktar/
│   │   │   ├── MainActivity.kt
│   │   │   ├── di/                 # Dependency Injection (Hilt)
│   │   │   ├── data/               # Data layer (repositories, datasources)
│   │   │   ├── domain/             # Domain layer (usecases, models)
│   │   │   ├── ui/                 # UI layer (Compose)
│   │   │   ├── utils/              # Utilities
│   │   │   └── vm/                 # ViewModels
│   │   └── res/                    # Resources (drawable, layout, etc)
│   ├── test/                       # Unit tests
│   └── androidTest/                # Instrumentation tests
└── build.gradle.kts
```

## Architecture

KTAR follows **MVVM + Clean Architecture**:

- **UI Layer**: Jetpack Compose screens, ViewModels
- **Domain Layer**: Use cases, business logic, models
- **Data Layer**: Repositories, local/remote data sources

## Key Dependencies

- **Kotlin Coroutines**: Async operations
- **Jetpack Compose**: Modern UI toolkit
- **Hilt**: Dependency injection
- **SSHJ**: SSH client library
- **DataStore**: Secure preferences
- **SonarQube**: Code quality analysis

## Debugging

### Logcat

```bash
# View all logs
adb logcat

# Filter by tag
adb logcat | grep "ktar"

# Save to file
adb logcat > logcat.log
```

### Android Studio Debugger

1. Set breakpoints in code
2. Run with: `./gradlew installDebug`
3. Attach debugger via Android Studio: Run > Debug 'app'

## Building Release

```bash
# Configure signing in local.properties or build.gradle.kts
./gradlew assembleRelease

# APK location: app/build/outputs/apk/release/
```

## Common Issues

### Build fails with Java version mismatch

```bash
# Check your Java version
java -version

# Should be 17+. Update if needed and set JAVA_HOME
export JAVA_HOME=/path/to/jdk17
```

### Gradle sync fails

```bash
# Clean and rebuild
./gradlew clean
./gradlew build --refresh-dependencies
```

### APK installation fails

```bash
# Uninstall previous version
adb uninstall com.ktar

# Install debug APK
./gradlew installDebug
```

## Performance Profiling

### CPU Profiler

1. Android Studio > View > Tool Windows > Profiler
2. Record CPU usage during app operations
3. Analyze hot spots

### Memory Profiler

1. Profiler > Memory
2. Monitor heap allocation
3. Track potential memory leaks

## Publishing

See [SECURITY.md](SECURITY.md) for security considerations before releasing.

## Getting Help

- Create an issue: [GitHub Issues](https://github.com/felipemacedo1/ktar/issues)
- Start a discussion: [GitHub Discussions](https://github.com/felipemacedo1/ktar/discussions)
- Read existing docs: [docs/](docs/)
