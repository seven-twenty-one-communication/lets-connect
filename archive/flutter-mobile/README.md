# LetsConnect Android app

## Local development

1. Install Android Studio, the Android SDK, and an Android emulator. This
   repository includes the Flutter SDK in `flutter/`.
2. From the repository root, start the backend with the local development
   profile:

   ```powershell
   cd backend
   mvn "-Dspring-boot.run.profiles=dev" spring-boot:run
   ```

   This profile uses an in-memory H2 database and development-only mock
   integrations. The fixed OTP is `123456`.
3. In a second terminal, fetch packages and launch the app on the emulator:

   ```powershell
   cd mobile
   ..\flutter\bin\flutter.bat pub get
   ..\flutter\bin\flutter.bat run
   ```

The Android emulator reaches the host computer at `10.0.2.2`; the app's
default API URL is `http://10.0.2.2:8080/api`. Cleartext HTTP is enabled only
for Android debug builds. For a physical device, use your computer's LAN IP and
keep both devices on the same network:

```powershell
..\flutter\bin\flutter.bat run --dart-define=API_BASE_URL=http://<computer-lan-ip>:8080/api
```

The backend's development voice tokens are placeholders and do not support
real audio calls. Configure real integrations before release.
