# Pixel branding and Settings appearance

On 2026-11-15 the user supplied pixel-icons-2026.zip and approved the name 洗衣机 and Google Settings inspired appearance.

## Reference implementations

- AOSP SettingsLib/Spa: https://github.com/aosp-mirror/platform_frameworks_base/tree/main/packages/SettingsLib/Spa
- Android Now in Android settings: https://github.com/android/nowinandroid/tree/main/feature/settings

Use existing Material/AndroidX components, large page titles, semantic surface colors, 16–24dp spacing and grouped preference rows. Follow system light/dark mode and preserve dynamic colors where already supported. Do not add a web runtime or migrate application architecture for appearance alone.

## Application scope

- App name 洗衣机. Replace all launcher layers and the round icon reference.
- Large Material3 headings, tonal low-elevation machine rows and semantic status colors. Preserve login, washing, QR scan and order operations.
- Keep private elecbill optional and personal signing files excluded.

## Release implementation

- Reuse SettingsTopBar for machine, order and machine-detail pages. The machine navigation item uses the Material laundry icon.
- Verification code input colors use primaryContainer/onPrimaryContainer and surface container roles so login follows the same system palette.
- Retain the existing standard light/dark fallback palettes for Android 8–11 and disabled dynamic colors. Unused generated medium/high-contrast palettes and ColorFamily placeholders were removed because no caller selects them.
- Remove the replaced Android Studio launcher background, unused XML template colors and local-only preview Activity. Keep the actual login, scan, payment, order, countdown and optional electricity implementations.
- A clean checkout builds without the unpublished electricity feature or signing material. Release builds use .signing/personal.properties and .signing/personal.jks when present, otherwise Gradle produces an unsigned artifact. README documents the required SDK/JDK and local signing setup.
- Register countdown broadcasts with ContextCompat.RECEIVER_NOT_EXPORTED so the list works across the declared API 26+ range. Remove obsolete API 33 annotations from the Activity, countdown ViewModel and row. Restrict service broadcasts to the application package so they reach the internal receiver on current Android versions.
