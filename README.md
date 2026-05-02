# Real-time Clipboard Sync (Mac & Android)

This project enables real-time clipboard synchronization between a macOS device and an Android device using Firebase Realtime Database.

## Project Structure

- `macos-client/`: Contains the Python script for the macOS client.
- `android-client/`: (Coming soon/Placeholder)

## Setup for macOS Client

1. **Install Dependencies**:
   ```bash
   pip install pyperclip firebase-admin
   ```

2. **Firebase Configuration**:
   - Obtain a `serviceaccount.json` file from your Firebase console.
   - Place `serviceaccount.json` inside the `macos-client/` directory.
   - **Note**: This file is ignored by Git for security reasons.

3. **Run the Client**:
   ```bash
   python macos-client/listner.py
   ```

## Security Warning

Never share or commit your `serviceaccount.json` file. It contains private keys that provide full access to your Firebase project.
