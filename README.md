# RichGram

An Android client prototype with a dark purple interface.

## Current status
- Android/Jetpack Compose starter project
- Chat list, gifts and profile UI mockups
- GitHub Actions debug APK build

**Important:** this is currently an interface prototype. It is not yet connected to Telegram accounts, TDLib, real chats, Stars, Premium or gift transactions. Those features require a separately integrated and tested TDLib Android native build and Telegram API configuration. Never commit `api_hash`, phone login codes, passwords or session files.

## Build
Run `gradle :app:assembleDebug` with JDK 17 and Gradle 8.9 or use the GitHub Actions workflow.

The APK is published as the `RichGram-debug-apk` workflow artifact after a successful build.
