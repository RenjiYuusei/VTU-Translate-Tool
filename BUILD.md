# Build Instructions

## GitHub Actions Build

### Automatic Build
- APK files are automatically built when you push to `main`, `master`, or `develop` branches
- Pull requests to `main` or `master` also trigger builds
- You can manually trigger builds using the "Run workflow" button in GitHub Actions

### Download APK
1. Go to the [Actions tab](https://github.com/username/VTU-Translate-Tool/actions) in your GitHub repository
2. Click on the latest successful workflow run
3. Scroll down to the "Artifacts" section
4. Download either:
   - **Debug APK**: For testing and development
   - **Release APK**: For production use

### APK File Naming
- Debug: `VTU-Translate-debug-{commit_hash}.apk`
- Release: `VTU-Translate-release-{commit_hash}.apk`

## Manual Build

### Prerequisites
- Android Studio or Android SDK
- JDK 17
- Git

### Build Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/username/VTU-Translate-Tool.git
   cd VTU-Translate-Tool
   ```

2. Build debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

3. Build release APK:
   ```bash
   ./gradlew assembleRelease
   ```

4. Find APK files in:
   - Debug: `app/build/outputs/apk/debug/app-debug.apk`
   - Release: `app/build/outputs/apk/release/app-release.apk`

## Creating Releases

To create a GitHub release with APK:

1. Download the release APK from GitHub Actions artifacts
2. Go to [Releases](https://github.com/username/VTU-Translate-Tool/releases) in your repository
3. Click "Create a new release"
4. Choose a tag version (e.g., `v1.0.0`)
5. Add release notes
6. Attach the downloaded APK file
7. Publish the release

## Requirements

### For Building
- Android SDK 34
- Build Tools 34.0.0
- JDK 17

### For Running
- Android 8.0 (API level 26) or higher
- Internet connection for translation
- Groq API key (free at https://console.groq.com/keys)

## Troubleshooting

### Build Fails
- Check if you have the correct JDK version (17)
- Ensure Android SDK is properly installed
- Clean and rebuild: `./gradlew clean assembleRelease`

### APK Installation Issues
- Enable "Install from unknown sources" in Android settings
- Make sure you have enough storage space
- Try uninstalling any previous version first

## Contributing

When contributing to the project:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test the build locally
5. Create a pull request
6. The GitHub Action will automatically build and test your changes
