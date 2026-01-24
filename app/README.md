# app

Android application module for the Water Reminder app.

## What it contains
- Jetpack Compose UI screens and components.
- Local persistence (Room + DataStore).
- Background tasks (WorkManager) for reminders and sync.
- Networking stack (Retrofit + OkHttp + Moshi).

## Key files
- [build.gradle.kts](build.gradle.kts): Module build configuration and build-time API settings.
- [src/main/AndroidManifest.xml](src/main/AndroidManifest.xml): App manifest and permissions.
- [src/main/res/xml/network_security_config.xml](src/main/res/xml/network_security_config.xml): Network security rules.

## Build notes
- Debug builds use the same HTTPS base URL as release.
- API key is injected via build config or runtime config file.

