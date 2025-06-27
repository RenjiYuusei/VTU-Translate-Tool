# VTU Translate Tool

An Android application for translating `strings.xml` files using various AI models via the OpenRouter API.

## Features

- **File-based Translation**: Load a `strings.xml` file, and the tool will translate its string values into Vietnamese.
- **AI Model Selection**: Choose from a variety of free models provided by OpenRouter, including:
  - `google/gemini-2.0-flash-exp:free`
  - `google/gemma-3-27b-it:free`
  - `deepseek/deepseek-r1-0528:free`
  - `deepseek/deepseek-chat-v3-0324:free`
- **Simple UI**: A clean, tab-based interface built with Jetpack Compose.
  - **Translate Tab**: Load your file, view original and translated content side-by-side, and save the result.
  - **Settings Tab**: Configure your OpenRouter API key and select your preferred translation model.
  - **Log Tab**: View a detailed log of the translation process.
- **Secure API Key Storage**: Your API key is saved locally using Jetpack DataStore.

## Getting Started

### Prerequisites

- Android Studio (latest version recommended)
- An Android device or emulator
- An API key from [OpenRouter.ai](https://openrouter.ai/keys)

### Build and Run

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/your-username/VTU-Translate-Tool.git
    ```
    *(Note: Replace `your-username` with your actual GitHub username or the correct repository path.)*

2.  **Open in Android Studio**:
    -   Open Android Studio.
    -   Select "Open" and navigate to the cloned repository folder.
    -   Let Android Studio sync the Gradle project.

3.  **Configure API Key**:
    -   Run the application on an emulator or a physical device.
    -   Navigate to the **Settings** tab.
    -   Click the "Get API Key from OpenRouter" button to get your key if you don't have one.
    -   Paste your API key into the input field.
    -   Select a translation model from the dropdown.
    -   Click "Save".

4.  **Translate a File**:
    -   Navigate to the **Translate** tab.
    -   Click "Load strings.xml" and select your file.
    -   The original content will be displayed.
    -   Click "Translate" to start the process. The progress will be shown.
    -   Once finished, the translated content will appear, and you can save it using the "Save Translated File" button.

## Project Structure

-   **/app/src/main/java/com/vtu/translate**: Main application package.
    -   **/data**: Contains `SettingsRepository` and `LogRepository` for data handling (DataStore, Singleton).
    -   **/network**: Retrofit setup for OpenRouter API communication (`OpenRouterApi`, DTOs).
    -   **/ui**: Jetpack Compose UI code.
        -   **/screens**: Composable functions for each screen (`TranslateScreen`, `SettingsScreen`, `LogsScreen`).
        -   **/theme**: Standard UI theme files.
    -   **/viewmodel**: ViewModels (`TranslateViewModel`, `SettingsViewModel`, `LogsViewModel`) to hold and manage UI-related data.
-   **/app/src/main/res**: Android resources (drawables, values, layouts).

## Dependencies

-   **Jetpack Compose**: For building the UI.
-   **Retrofit**: For making network requests to the OpenRouter API.
-   **Kotlin Coroutines**: For asynchronous operations.
-   **Jetpack DataStore**: For persisting settings.
-   **Lifecycle & ViewModel KTX**: For `ViewModel` and `viewModelScope`. 