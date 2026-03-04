---
description: Workflow for 100% precise synchronization of Glyph UI and Logic, including granular flip-only controls.
---

# Glyph Precision Sync Workflow

This workflow provides a step-by-step blueprint for migrating and synchronizing Glyph interface settings with 100% precision, ensuring all UI toggles are perfectly mapped to background service logic.

## Phase 1: Infrastructure (Constants & Settings)

1. **Define Constants**: Add all necessary feature keys to `Constants.java`.
   - Ensure specific keys exist for granular "Flip Only" features (e.g., `GLYPH_CHARGING_FLIP_ONLY`, `GLYPH_PROGRESS_FLIP_ONLY`, `GLYPH_MUSIC_VISUALIZER_FLIP_ONLY`).
2. **Update SettingsManager**: Implement getter/setter methods for every new key.
   - Example: `public static boolean isGlyphChargingFlipOnly()` and `public static void setGlyphChargingFlipOnly(boolean enable)`.
   - Use `PreferenceManager.getDefaultSharedPreferences(ctx)` for persistence.

## Phase 2: UI Layout Integration

1. **Integrated Advanced Cards**: In `activity_glyph_settings.xml`, add `MaterialCardView` for advanced features:
   - **Progress**: Include a main toggle and a nested "Music Progress" toggle.
   - **Flip Only Toggles**: Add dedicated "Flip Only" switches for Volume, Progress, Charging, and Music Visualizer.
2. **Standardize IDs**: Use consistent ID naming conventions (e.g., `switchProgressFlipOnly`, `layoutProgressFlipOnly`).
3. **Hierarchy**: Ensure nested setting layouts (e.g., `layoutChargingFlipOnly`) are placed directly below their parent toggles for intuitive grouping.

## Phase 3: Fragment Logic & Synchronization

1. **View Initialization**: In `SettingsFragment.java`, find all new views using `findViewById` in `initViews`.
2. **State Restoration**: In `setupInitialState`, initialize all switches and visibility states based on `SettingsManager` values.
3. **Listener Implementation**: In `setupListeners`, attach `onCheckedChangeListener` to every switch:
   - **Main Toggles**: Update the setting AND toggle the visibility of any dependent "Flip Only" layout blocks.
   - **Sub-Settings**: Update the setting and post a check to `ServiceUtils::checkGlyphService`.
4. **Visibility Logic**: Ensure that "Flip Only" toggles are only visible when the corresponding main feature is enabled.

## Phase 4: Service-Level Precision Audit

1. **Verify Flip Compliance**: Audit every Glyph service (e.g., `ChargingService`, `ProgressService`, `MusicVisualizerService`).
2. **Implement Dual Flip Checks**: Every animation trigger should check both global and feature-specific flip preferences:
   ```java
   if (SettingsManager.isGlyphIndicatorsFlipOnly() || SettingsManager.isGlyphFeatureFlipOnly()) {
       if (!StatusManager.isFlipped()) return;
   }
   ```
3. **Validate Trigger Points**: Ensure logic is applied at the very start of the animation handler to prevent unnecessary processing.

## Phase 5: Strings & Final Verification

1. **Add String Resources**: Ensure `strings.xml` contains all titles and descriptions for the new features.
2. **100% Precision Audit**:
   - Verify every UI ID matches its Java reference.
   - Verify every setting key matches its `Constants` definition.
   - Verify every service correctly implements the flip-down restriction.

// turbo
3. Run `./gradlew assembleDebug` to ensure all IDs and constants are correctly resolved.
