# farmi-android — Execution, Validation & Emulator Runbook

Since you do not have a physical Android device, you can use the **Android Emulator** included in the Android SDK tools. This document provides step-by-step instructions on how to set up, run, and validate the **Farmi Android** application on your macOS computer.

---

## 1. Setup & SDK Prerequisites

Ensure the Android command-line tools are available on your system path. The installation on this machine has appended the path to `~/.zprofile`. To apply it to your current shell session, run:
```bash
source ~/.zprofile
```

Verify the tools are active:
```bash
android info
```

---

## 2. Start the Backend Service (Prerequisite)

Before starting the Android application, you must run the **Farmi Backend Service**, as all network calls (device registration, crop loading, and AI chat advisory queries) flow through the REST API.

1. Open a new terminal tab and navigate to the backend directory:
   ```bash
   cd /Users/subbaramreddybasireddy/farmi-project/farmi-backend
   ```
2. Activate the python virtual environment:
   ```bash
   source venv/bin/activate
   ```
3. Start the FastAPI backend server:
   ```bash
   uvicorn app.main:app --host 0.0.0.0 --port 8080 --reload
   ```

---

## 3. Managing the Android Emulator (No Phone Required)

To execute the application, you need to create and boot an Android Virtual Device (AVD).

### 3.1 Install System Images
To run an emulator, you must first download a system image compatible with your architecture (macOS Apple Silicon/ARM64). We target Android API Level 36:
```bash
android sdk install system-images/android-36/google_apis/arm64-v8a
```

### 3.2 Create a Virtual Device
Create a virtual device using one of the available profiles (e.g., `medium_phone`):
```bash
android emulator create medium_phone
```
To see all available profiles, you can run `android emulator create --list-profiles`.

### 3.3 Start the Emulator
Launch the emulator. This command will wait until the device is fully booted and ready:
```bash
android emulator start medium_phone
```
*(Leave this terminal window running, or run the command in the background)*

### 3.4 Troubleshooting Graphics/GPU Freezes (Cold Boot)
If the emulator window appears frozen, does not register input, or throws GPU context errors in the logs, perform a cold boot to start the OS cleanly without loading any corrupted snapshot:
```bash
android emulator start --cold medium_phone
```

---

## 4. Running & Verifying the Application

Once the emulator is booted and active:

### 4.1 Deploy via CLI
First, build the debug APK:
```bash
./gradlew assembleDebug
```

Then, deploy it directly onto the running emulator:
```bash
android run --apks app/build/outputs/apk/debug/app-debug.apk
```
This installs the APK and launches the main activity.

### 4.2 Auto-Grant Location Permissions (Recommended)
To prevent the location permission popup from blocking the app's location sync logic on initial startup:
```bash
adb shell pm grant com.example.farmi android.permission.ACCESS_COARSE_LOCATION
```

---

## 5. Tools for Testing and Verification

You can inspect and debug the application from the command line while it is running on the emulator:

### 5.1 Reading Device Connection Logs
To check the REST API calls (Device Registration, Crops API, Chat advisory query requests), run this command:
```bash
adb logcat -d | grep -E "CropRepository|ChatRepository|LocationSync"
```

### 5.2 Capturing UI Screenshots
Capture a screenshot from the running emulator and save it locally to inspect the visual interface:
```bash
android screen capture --output=output_screenshot.png
```

### 5.3 Inspecting Layouts
To inspect elements and verify that structural styling is correct without visual tools, dump the layout tree in JSON format:
```bash
android layout --pretty
```

---

## 6. Running Automated Tests

Run the unit tests to verify repository logic, sorting behavior, and ViewModel functionality:
```bash
./gradlew test
```
The test reports will be generated at `app/build/reports/tests/testDebugUnitTest/index.html`.
