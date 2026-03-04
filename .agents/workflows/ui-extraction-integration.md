---
description: Workflow for extracting UI from the /app module and integrating it into the main project.
---

# UI Extraction and Integration Workflow

This workflow describes the process for porting UI components from the `/app` module into the main project source, while ensuring that dialogs and separate screens are excluded.

## Phase 1: Resource Extraction
1. **Analyze Layouts**: Identify the main layout files in `app/src/main/res/layout/` (e.g., `activity_main.xml`).
2. **Copy Layout Content**: Extract the root layout and its children, excluding any parts that trigger dialogs or navigation to separate screens.
3. **Copy Drawable and Styling**:
   - Locate and copy all relevant drawables from `app/src/main/res/drawable/`.
   - Copy styling tokens from `app/src/main/res/values/themes.xml` and `colors.xml` and merge them into the target's `res/values/`.
4. **Copy Animations**: Transfer any custom animation XML files from `app/src/main/res/anim/`.

## Phase 2: Logic Integration
1. **Identify UI Logic**: Look for the corresponding Activity or Fragment in `app/src/main/java/` that handles the UI interactions for the extracted layouts.
2. **Port Logic**:
   - Copy initialization code for widgets (Buttons, SeekBars, Switches).
   - Port interaction listeners (onClick, onProgressChanged).
   - Ensure that the logic is integrated into the existing `AnimationManager` or `SettingsManager` as appropriate, rather than creating new Activities.
3. **Exclude Navigation/Dialogs**: Remove any code that calls `showDialog()` or `Intent(context, SomeOtherActivity.class)`.

## Phase 3: Refactoring and Cleanup
1. **Resolve Dependencies**: Ensure all new UI components are correctly linked to the existing `Constants.java` and internal managers.
2. **Visual Consistency Check**: Run the app and verify that the layout behavior and styling match the original `/app` implementation.
3. **Performance/Cleanliness Check**: Ensure no redundant or duplicate components were introduced.

// turbo
4. Run `./gradlew assembleDebug` to verify the integration doesn't break the build.
