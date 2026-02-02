# CrazyMobileApp

Kotlin Multiplatform (KMP) starter project for an emergency alert app. The goal is to let users trigger a loud alarm with flashing flashlight and emergency calls by pressing volume up + down (or another selected trigger). Users can configure a custom emergency contact in addition to the police number.

## Planned feature set
- Trigger options: volume up + down, alternative gestures, and customizable triggers.
- Emergency flow: loud siren (police-like audio), flashlight strobe, and automatic calls.
- Settings: user-defined emergency number plus police number (default 911).
- Status tracking: persistent foreground service and in-app status.

## Modules
- `shared`: KMP shared models and interfaces.
- `androidApp`: Android application placeholder.

## Next steps
- Add UI for settings and trigger selection.
- Implement the Android foreground service for siren/flashlight/call flows.
- Add iOS target and platform-specific implementations.
