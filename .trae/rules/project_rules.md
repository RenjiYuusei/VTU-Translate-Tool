# Project Rules for VTU Translate Tool

This document outlines the coding conventions, best practices, and project guidelines for the VTU Translate Tool. Adhering to these rules ensures code consistency, readability, and maintainability.

## 1. General Principles

- **Language**: The primary language for the codebase (comments, variable names, etc.) is **English**.
- **Code Formatting**: Follow the official [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html). Use the default Android Studio formatter.
- **Immutability**: Prefer `val` over `var` wherever possible. Use immutable collections (e.g., `List`, `Set`, `Map`) over their mutable counterparts unless mutability is explicitly required.

## 2. Project Structure

Follow the existing project structure as outlined in the `README.md`. New files should be placed in the appropriate packages:

- `data/model`: For data classes (e.g., API responses).
- `data/repository`: For repositories that handle data logic and interact with data sources.
- `ui/screens`: For Composable screens.
- `ui/viewmodel`: For ViewModels associated with the screens.
- `ui/theme`: For theming and styling.

## 3. Naming Conventions

- **Classes and Interfaces**: PascalCase (e.g., `TranslateViewModel`, `GroqRepository`).
- **Functions**: camelCase (e.g., `startTranslation`, `saveTranslatedFile`).
- **Variables and Properties**: camelCase (e.g., `apiKey`, `translationStatus`).
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_MODEL`, `API_BASE_URL`).
- **Composable Functions**: PascalCase (e.g., `MainScreen`, `SettingsDialog`).

## 4. Jetpack Compose

- **State Management**: Use `ViewModel` to hold and manage UI state. State should be exposed to Composables as `StateFlow` or `State`.
- **Recomposition**: Write Composables that are idempotent and free of side effects. Avoid complex logic within Composables; move it to the `ViewModel`.
- **Previews**: Provide `@Preview` annotations for all reusable Composables to facilitate development and testing.
- **Modifiers**: Accept a `Modifier` parameter as the first optional parameter for all Composables that emit UI. This enhances reusability and customization.

## 5. Dependency Management

- **Libraries**: Use the versions defined in the `app/build.gradle` file. When adding a new library, ensure it is necessary and does not introduce unnecessary complexity.
- **BOM (Bill of Materials)**: Use the Compose BOM (`androidx.compose:compose-bom`) to manage Jetpack Compose library versions.

## 6. API Interaction (Retrofit & OkHttp)

- **Error Handling**: Implement robust error handling for all API calls. Use `try-catch` blocks or the `Result` class to handle exceptions and different response states (success, error).
- **Models**: Use Kotlin Serialization (`kotlinx-serialization-json`) for parsing JSON responses. Annotate data classes with `@Serializable`.
- **Security**: Do not hardcode API keys or other secrets in the source code. Use `EncryptedSharedPreferences` or other secure storage mechanisms to store sensitive information.

## 7. Coroutines

- **Dispatchers**: Use the appropriate dispatcher for the task (e.g., `Dispatchers.IO` for network/disk operations, `Dispatchers.Main` for UI updates).
- **Scope**: Launch coroutines from a `viewModelScope` in ViewModels to ensure they are automatically canceled when the ViewModel is cleared.

## 8. Git

- **Commit Messages**: Write clear and concise commit messages. Follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) specification.

By following these rules, we can maintain a high-quality and collaborative development environment.
