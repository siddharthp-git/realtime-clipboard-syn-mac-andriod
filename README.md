# Real-time Clipboard Sync (Mac & Android)

This project enables real-time clipboard synchronization between a macOS device and an Android device using Firebase Realtime Database.

## Project Structure

- `macos-client/`: Contains the Python script for the macOS client.
- `android-client/`: (Coming soon/Placeholder)

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
