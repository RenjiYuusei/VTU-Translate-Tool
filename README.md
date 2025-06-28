# VTU Translate Tool

VTU Translate Tool is an Android application that helps you translate strings.xml files from English to Vietnamese using AI models from OpenRouter.

## Features

- Translate strings.xml files using various AI models
- Support for multiple OpenRouter AI models
  - Gemini models: Gemma 3 27B IT, Gemini 2.0 Flash
  - DeepSeek models: DeepSeek R1 0528, DeepSeek V3 0324
- Securely store your OpenRouter API key
- Save translated strings to a new file
- Detailed logging system with copy and clear functionality

## Getting Started

### Prerequisites

- Android device running Android 7.0 (API level 24) or higher
- OpenRouter API key (can be obtained from [https://openrouter.ai/keys](https://openrouter.ai/keys))

### Installation

1. Download the latest APK from the Releases section
2. Install the APK on your Android device
3. Open the app and navigate to the Settings tab
4. Enter your OpenRouter API key and select your preferred model
5. Save your settings

## Usage

### Translating a strings.xml file

1. In the Translate tab, click "Load File" to select your strings.xml file
2. Review the loaded strings in the top section
3. Click "Translate" to send the strings for translation
4. The translated strings will appear in the bottom section
5. Click "Save File" to save the translated strings to a new file

### Managing Logs

1. Navigate to the Logs tab to view the application logs
2. Use the "Clear Logs" button to clear the log history
3. Use the "Copy Logs" button to copy the logs to the clipboard for troubleshooting

## Build from Source

To build this project from source:

```
git clone https://github.com/your-username/VTU-Translate-Tool.git
cd VTU-Translate-Tool
./gradlew build
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
