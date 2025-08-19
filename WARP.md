# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

<<<<<<< HEAD
## Project Overview

VTU Translate Tool is an Android application built with Kotlin and Jetpack Compose that translates Android strings.xml files from English to Vietnamese using Groq's AI API (Llama 3.1 8B Instant model by default).

## Common Development Commands

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build artifacts
./gradlew clean

# Build and install on connected device
./gradlew installDebug
```

### Testing Commands
```bash
# Run unit tests
./gradlew test

# Run instrumented tests on device/emulator
./gradlew connectedAndroidTest

# Run all tests
./gradlew testDebugUnitTest connectedDebugAndroidTest
```

### Code Quality
```bash
# Run lint checks
./gradlew lint

# Generate lint report
./gradlew lintDebug

# Clean and rebuild
./gradlew clean build
```

## Architecture Overview

### Project Structure
The application follows MVVM architecture with Repository pattern:

- **VTUTranslateApp** (Application class): Initializes and provides singleton repositories
- **MainActivity**: Single activity hosting Compose UI, handles language switching and theme
- **MainViewModel**: Central ViewModel managing UI state and orchestrating business logic
- **Repository Layer**: Handles data operations and API interactions
  - `TranslationRepository`: Core translation logic and XML parsing
  - `GroqRepository` & `GeminiRepository`: AI provider implementations  
  - `PreferencesRepository`: Encrypted settings storage
  - `LogRepository`: Translation logging system

### Data Flow
1. User selects XML file → `TranslationRepository.parseStringsXml()` extracts strings
2. Translation request → Repository determines AI provider based on settings
3. AI API call → Response processed with special handling for technical strings
4. Results displayed → User can edit before saving
5. Background translation supported via `TranslationService` (foreground service)

### Key Technical Decisions

**Translation Intelligence**: The app automatically identifies non-translatable strings (package names, URLs, format specifiers) using pattern matching in `isSpecialNonTranslatableString()`. Strings containing both natural language and technical elements are intelligently processed.

**State Management**: Uses Kotlin StateFlow throughout for reactive UI updates. The ViewModel exposes multiple StateFlows for different UI aspects (translation progress, theme, language).

**Security**: API keys stored using Android's EncryptedSharedPreferences for secure storage.

**Multi-provider Support**: Architecture supports both Groq and Google Gemini APIs with easy switching via settings.

## Build Configuration

### Signing Configuration
The release build requires keystore configuration. The app looks for:
1. `keystore.properties` file with signing credentials
2. Falls back to `Yuusei.jks` with hardcoded credentials (for CI/CD)

### GitHub Actions
Automated builds configured in `.github/workflows/build-release.yml`:
- Triggers on push to main/master/develop branches
- Uses secrets for keystore (KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD)
- Produces signed APK artifacts retained for 90 days

### Gradle Configuration
- Gradle 8.14.2 with aggressive JVM memory settings (8GB heap)
- R8 minification enabled for release builds
- Resource shrinking and APK size optimizations configured
- Multi-language support (English, Vietnamese)

## Dependencies & Technologies

### Core Stack
- **Language**: Kotlin 1.8.10
- **UI Framework**: Jetpack Compose with Material3
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

### Key Libraries
- Retrofit 2.9.0 + OkHttp for API calls
- Kotlinx Serialization for JSON parsing
- Jetpack Navigation Compose for screen navigation
- AndroidX Security Crypto for encrypted preferences
- Kotlin Coroutines for async operations

## Important Implementation Details

### String Processing Logic
The `TranslationRepository` contains sophisticated logic for identifying translatable vs non-translatable strings. Review `isSpecialNonTranslatableString()` and related methods when modifying translation behavior.

### Background Translation
The app includes a foreground service (`TranslationService`) for background translation with notification. This requires FOREGROUND_SERVICE permission and proper lifecycle management in MainActivity.

### Language Switching
The app supports runtime language switching (Vietnamese/English/System) implemented in MainActivity. This recreates the activity to apply language changes system-wide.

### Error Handling
Translation errors are logged via `LogRepository` with different severity levels (INFO, WARNING, ERROR). The UI displays these in a dedicated Log screen for debugging.
=======
Project type: Android app (Kotlin, Jetpack Compose) using Gradle Wrapper

Core context
- Purpose: Translate Android strings.xml from English to Vietnamese using Groq API; provides UI for reviewing/editing and exporting.
- Tooling: Gradle Wrapper, JDK 17, Android SDK. CI via GitHub Actions builds signed release APKs.
- Primary modules: single module app/ with Compose UI, ViewModels, repositories, and a foreground TranslationService.

Common commands (pwsh on Windows)
- Ensure Java and SDK: JDK 17 and ANDROID_HOME/SDK installed (local.properties may point to sdk.dir).
- Gradle wrapper is checked in; prefer it over a global Gradle.

Build
- Assemble debug APK: .\gradlew.bat assembleDebug
- Install debug on connected device: .\gradlew.bat installDebug
- Assemble release APK (unsigned if no keystore available locally): .\gradlew.bat assembleRelease

Tests
- All unit tests (host JVM): .\gradlew.bat testDebugUnitTest
- Single test class: .\gradlew.bat testDebugUnitTest --tests "com.vtu.translate.<TestClassName>"
- Instrumented tests on device/emulator: .\gradlew.bat connectedDebugAndroidTest

Lint and quality
- Android Lint (module): .\gradlew.bat :app:lintDebug
- All variants lint: .\gradlew.bat lint

Useful gradle tasks
- List tasks: .\gradlew.bat tasks --all
- Clean: .\gradlew.bat clean

CI workflow notes
- GitHub Actions workflow at .github/workflows/build-release.yml:
  - Uses Temurin JDK 17.
  - Creates local.properties with sdk.dir.
  - Decodes a base64 keystore from secrets to Yuusei.jks and builds assembleRelease.
  - Uploads signed release APK artifact named VTU-Translate-release-<timestamp>.
- Locally, if you need a signed release, provide keystore.properties at repo root or a Yuusei.jks file as expected by app/build.gradle.

Signing configuration
- app/build.gradle release signing:
  - If keystore.properties exists at repo root, it is loaded for release signing (storeFile, storePassword, keyAlias, keyPassword).
  - Otherwise it falls back to a local Yuusei.jks in repo root. Prefer using keystore.properties locally and GitHub Secrets in CI.

High-level architecture
- UI layer (Jetpack Compose, Material 3):
  - Navigation via androidx.navigation:navigation-compose.
  - Screens under app/src/main/java/com/vtu/translate/ui/screens.
  - Theming under ui/theme.
- State & logic:
  - ViewModels (androidx.lifecycle:lifecycle-viewmodel-compose) orchestrate translation flows, progress, cancellation, and persistence.
  - Repository layer (data/repository) encapsulates business logic for parsing strings.xml, batching requests, handling exclusions (package/class names, URLs, format specifiers), and persistence.
- Networking & serialization:
  - Retrofit + OkHttp with logging-interceptor; kotlinx-serialization converter and JSON for payloads.
  - Interacts with Groq API; model defaults described in README (can be changed in settings UI).
- Background work:
  - Foreground TranslationService declared in AndroidManifest.xml to handle long-running translation and progress notifications.
- Storage & security:
  - EncryptedSharedPreferences via androidx.security:security-crypto for API key and user settings.
- Testing:
  - Unit: junit:junit:4.13.2.
  - Instrumented: androidx.test.ext:junit, espresso, compose ui-test.

Project structure snapshot (selective)
- app/src/main/java/com/vtu/translate/
  - data/model, data/repository
  - ui/screens, ui/viewmodel, ui/theme
  - VtuTranslateApp.kt (Application)
  - MainActivity (entry point)
- app/src/main/AndroidManifest.xml declares MainActivity and TranslationService.

Development tips specific to this repo
- Compose compiler extension set in app/build.gradle; ensure Android Gradle Plugin 8.1.x and Kotlin 1.8.10 are in place (root build.gradle).
- Packaging excludes and resConfigs reduce APK size; if adding libs that bring META-INF files, update packagingOptions accordingly.
- Minimum SDK 24, target 34; MANAGE_EXTERNAL_STORAGE is declared with tools:ignore for scoped storage—verify Play requirements if distributing.

Reference files
- README.md: features, usage steps, and project overview.
- app/build.gradle: product flavors not defined; debug vs release buildTypes; proguard/r8 configured with proguard-android-optimize.txt, proguard-rules.pro, r8-config.pro.
- settings.gradle and root build.gradle: plugin versions and repository configuration.

>>>>>>> 6074418 (Improves string resource parsing)
