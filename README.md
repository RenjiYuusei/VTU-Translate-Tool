# VTU Translate Tool

An Android application for translating `strings.xml` files using various AI models via the OpenRouter API.

## Features

-   **Translate `strings.xml`:** Easily upload, translate, and save your Android string resource files.
-   **OpenRouter Integration:** Leverages the OpenRouter API to provide access to a variety of powerful AI models.
-   **Supported Models:**
    -   `google/gemma-3-27b-it:free`
    -   `deepseek/deepseek-r1-0528:free`
    -   `deepseek/deepseek-chat-v3-0324:free`
    -   `google/gemini-2.0-flash-exp:free`
-   **Bilingual Interface:** The application UI is in Vietnamese, while the source code is in English.
-   **Modern UI:** Built with Jetpack Compose for a modern, declarative UI.
-   **Tab-based Navigation:**
    -   **Translate Tab:** Manage the file translation process.
    -   **Settings Tab:** Configure your OpenRouter API key and select your preferred translation model.
    -   **Log Tab:** View detailed logs of the translation process.

## Tech Stack

-   **Language:** Kotlin
-   **UI:** Jetpack Compose
-   **Architecture:** MVVM (Model-View-ViewModel)
-   **Asynchronous Programming:** Kotlin Coroutines
-   **Networking:** Retrofit
-   **Data Persistence:** Jetpack DataStore

## How to Use

1.  **Get an API Key:** Obtain an API key from [OpenRouter](https://openrouter.ai/keys).
2.  **Configure the App:**
    -   Navigate to the **Settings** tab.
    -   Enter and save your API key.
    -   Select your desired translation model from the dropdown list.
3.  **Translate:**
    -   Go to the **Translate** tab.
    -   Click "Load strings.xml" to upload your file.
    -   Click "Translate All" to start the translation.
    -   The original and translated content will be displayed side-by-side.
4.  **Save:**
    -   Once the translation is complete, click "Save Translated File" to save the new `strings.xml` file.

## Building from Source

To build the project from the source code, you will need Android Studio. Clone the repository and open it in Android Studio. The project should sync and build automatically. 