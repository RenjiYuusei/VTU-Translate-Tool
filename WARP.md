# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

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
