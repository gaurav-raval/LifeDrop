# LifeDrop Android — Java + XML

This is a native Android Studio version of the uploaded LifeDrop blood-bank locator UI.

## Included
- Nearby Centers screen
- Blood Requests screen
- Search centers by name/address
- Blood type filter pills
- Stylized map preview
- Use current location permission request
- Directions button opens the device's map app
- Call center action shows an in-app notification
- Bottom navigation
- Android Java + XML (no WebView)

## Open in Android Studio
1. Extract the ZIP.
2. Open Android Studio.
3. Select **File > Open**.
4. Select the extracted `LifeDropAndroid` folder.
5. Let Gradle sync.
6. If Android Studio asks for a Gradle JDK, select **JDK 17**.
7. If prompted to install SDK 35, install it from SDK Manager.
8. Select an Android emulator or connect a USB-debugging-enabled Android phone.
9. Press the green **Run ▶** button.

## Recommended emulator
Pixel 6/7/8 or any portrait Android device, API 35 or newer.

## Important
The original uploaded app uses sample/fake center data. This Android version keeps that same sample data. The map preview is a lightweight native drawing rather than a Google Maps SDK map, so no Google Maps API key is required.

The project was generated in this environment; Android Studio/Gradle should perform the final dependency resolution and build on your machine.
