# Project Spec: KMP Emergency Trigger App

## Goal
Create a Kotlin Multiplatform (KMP) app that can be triggered by a hardware gesture (default: pressing Volume Up + Volume Down simultaneously) or a user-selected alternative, to:
- Play a loud “police-style” siren sound.
- Flash the device flashlight repeatedly.
- Initiate calls to emergency services and a user-configured emergency contact.

## Platform Reality Check (Important)
Android and iOS both enforce safety/permission limits around calling, background hardware access, and hardware button interception. The design below describes **what’s possible without violating OS rules**, and calls out **where user confirmation is required**.

### Android Constraints
- **Auto-calling**: Android does not allow silent/automatic calls without user confirmation. You can launch the dialer or call with `ACTION_CALL` if you hold `CALL_PHONE` permission, but most devices still require user action for emergency numbers. Expect to show a confirmation UI.
- **Hardware buttons**: Intercepting volume buttons reliably requires a foreground service with media session or an accessibility service. Some OEMs are restrictive; fallback gesture needed.
- **Flashlight**: Requires `CAMERA` permission and `CameraManager.setTorchMode`. Works in foreground service.
- **Siren playback**: Use `AudioManager` with alarm/notification stream and max volume.

### iOS Constraints
- **Auto-calling**: iOS does not allow auto-dialing emergency numbers. You can open `tel://` and present a call UI; user must confirm.
- **Hardware buttons**: iOS does not allow intercepting volume button presses for app-defined triggers. Must provide alternate triggers (e.g., in-app big button, Siri Shortcut).
- **Flashlight**: Requires camera permission and device support; can be toggled in app foreground.

## Scope for MVP
1. **Android-first implementation** (KMP shared logic + Android UI and services).
2. Shared KMP module for configuration, settings storage, and state machine.
3. Android module for trigger detection, siren playback, flashlight, and call intents.
4. iOS module for manual trigger and call UI (no volume-button trigger).

## Functional Requirements
- **Trigger options**:
  - Default: Volume Up + Volume Down pressed together.
  - Configurable alternative trigger(s), e.g., on-screen panic button.
- **Emergency numbers**:
  - Police/emergency number per region (default 112/911 based on locale).
  - User-configurable additional emergency contact number.
- **Emergency sequence**:
  1. Start loud “police-style” siren audio loop.
  2. Begin flashlight flashing pattern.
  3. Open call UI for emergency number, then optional user contact.
- **Settings**:
  - Choose trigger method.
  - Set emergency contact number.
  - Test mode (play siren + flashlight without calling).
- **Safety**:
  - Require explicit confirmation before any call.
  - Provide visible cancel/stop button.

## Non-Functional Requirements
- Works in background on Android via a foreground service.
- Clear permissions rationale screens.
- Localized emergency number suggestions.
- Battery-safe flashing (configurable pattern).
- Accessibility-friendly UI.

## Architecture (KMP)
```
shared/
  - EmergencyConfig (data model)
  - TriggerMode (enum)
  - EmergencySequence (state machine)
  - SettingsRepository (KMP storage via Settings or SQLDelight)

androidApp/
  - ForegroundService (trigger listening + execution)
  - VolumeButtonDetector (media session or accessibility)
  - SirenPlayer (AudioTrack / ExoPlayer)
  - FlashlightController (CameraManager)
  - CallLauncher (intent-based)

iosApp/
  - ManualTrigger UI
  - FlashlightController (AVCaptureDevice torch)
  - CallLauncher (openURL tel://)
```

## UX Flow (Android)
1. User configures emergency contact and trigger.
2. On trigger:
   - Show full-screen emergency UI with **Cancel**.
   - Start siren + flashlight.
   - Prompt: “Call emergency services?” with options:
     - Call emergency
     - Call emergency contact
     - Cancel

## Testing Plan
- Unit tests for shared state machine.
- Android instrumentation tests for permission flows.
- Manual tests for volume-trigger reliability across devices.

## Next Steps
1. Generate KMP project scaffold.
2. Implement shared settings + emergency state machine.
3. Build Android foreground service, siren, flashlight, and call intents.
4. Build iOS manual trigger and call UI.
