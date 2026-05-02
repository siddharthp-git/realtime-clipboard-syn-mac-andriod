# Real-time Clipboard Sync (Mac & Android)

This project enables real-time clipboard synchronization between a macOS device and an Android device using Firebase Realtime Database.

## Project Structure

- `macos-client/`: Contains the Python script for the macOS client.
- `android-client/`: Contains the Android Studio project for the Android client.

## Installation (Android)

1. **Clone the repository**:
2. **Open in Android Studio**: Open the `android-client` folder.
3. **Firebase Configuration**:
   - Download `google-services.json` from your Firebase console.
   - Place it in `android-client/app/`.
4. **Build and Run**: Deploy the app to your Android device.
5. **Permissions**: Enable **Accessibility Service** for the app to allow it to monitor and update the clipboard in the background.

## Installation (macOS)

1. **Download the DMG**: Download `ClipboardSync.dmg` from the releases (or use the one generated).
2. **Install**: Open the DMG and drag `Clipboard Sync` to your Applications folder.
3. **Run**: Launch `Clipboard Sync` from your Applications. It runs in the background with no dock icon.
4. **Auto-Start**: To have it start automatically when you log in:
   - Go to **System Settings** > **General** > **Login Items**.
   - Click the **+** button and select `Clipboard Sync` from your Applications folder.

## Manual Setup (for Development)

## Security Warning

Never share or commit your `serviceaccount.json` file. It contains private keys that provide full access to your Firebase project.
