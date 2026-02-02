# Crazy Mobile App (KMP)

This repository scaffolds a Kotlin Multiplatform (KMP) project for an emergency alert workflow. The goal is to build a mobile app that can be triggered by pressing volume up + down simultaneously (or another activation option), then:

- Play a loud police-style siren.
- Flash the device flashlight.
- Call the police and a user-defined emergency contact.

## Planned features

- **Activation modes**: volume buttons, custom gesture, or in-app trigger button.
- **Emergency contacts**: selectable police number and an additional emergency contact.
- **Alert feedback**: flashing flashlight and a siren message that mimics a police arrival announcement.

## Project structure

- `shared/`: Kotlin Multiplatform shared logic and data models.
- `androidApp/`: Android application module (initial UI shell).

## Next steps

1. Implement platform-specific integrations for:
   - Volume button detection on Android/iOS.
   - Flashlight access.
   - Phone call initiation.
   - Audio playback for the siren/announcement.
2. Add settings screens to allow users to set emergency numbers.
3. Expand the shared module with state management and feature workflows.
