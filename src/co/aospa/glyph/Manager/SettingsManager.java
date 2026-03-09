/*
 * Copyright (C) 2022-2024 Paranoid Android
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.aospa.glyph.Manager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.android.internal.util.ArrayUtils;

import java.util.HashSet;
import java.util.Set;

import co.aospa.glyph.Constants.Constants;
import co.aospa.glyph.Utils.FileUtils;
import co.aospa.glyph.Utils.ResourceUtils;

/**
 * Centralized manager for reading and writing all Glyph-related preferences
 * from SharedPreferences and Secure Settings.
 */
public final class SettingsManager {

    private static final String TAG = "GlyphSettingsManager";
    private static final boolean DEBUG = true;
    private static Context context;

    private static Context getContext() {
        if (context == null) {
            if (Constants.CONTEXT == null) {
                throw new IllegalStateException("Constants.CONTEXT is not initialized");
            }
            context = Constants.CONTEXT;
        }
        return context;
    }

    public static boolean enableGlyph(boolean enable) {
        android.content.Context ctx = getContext();
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_ENABLE, enable).apply();

        return android.provider.Settings.Secure.putInt(ctx.getContentResolver(),
                Constants.GLYPH_ENABLE, enable ? 1 : 0);
    }

    public static boolean isGlyphEnabled() {
        Context ctx = getContext();
        boolean baseEnabled = (Settings.Secure.getInt(ctx.getContentResolver(),
                Constants.GLYPH_ENABLE, 1) != 0
                || PreferenceManager.getDefaultSharedPreferences(ctx)
                        .getBoolean(Constants.GLYPH_ENABLE, false));

        if (GlyphScheduleManager.isScheduleEnabled(ctx) &&
                GlyphScheduleManager.isScheduleCurrentlyActive(ctx)) {
            return false;
        }

        return baseEnabled;
    }

    public static boolean isGlyphEnabledIgnoreSchedule() {
        Context ctx = getContext();
        return (Settings.Secure.getInt(ctx.getContentResolver(),
                Constants.GLYPH_ENABLE, 1) != 0
                || PreferenceManager.getDefaultSharedPreferences(ctx)
                        .getBoolean(Constants.GLYPH_ENABLE, false));
    }

    public static boolean isGlyphFlipEnabled() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_FLIP_ENABLE, false) && isGlyphEnabled();
    }

    public static void setGlyphFlipEnabled(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_FLIP_ENABLE, enable).apply();
    }

    public static int getGlyphBrightness() {
        int[] levels = Constants.getBrightnessLevels();
        int brightnessSetting = getGlyphBrightnessSetting();
        return levels[brightnessSetting - 1];
    }

    public static int getGlyphBrightnessSetting() {
        Context ctx = getContext();
        int d = 3;
        if (FileUtils.readLine("/mnt/vendor/persist/color") == "white")
            d = 2;
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getInt(Constants.GLYPH_BRIGHTNESS, d);
    }

    public static void setGlyphBrightness(int brightness) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putInt(Constants.GLYPH_BRIGHTNESS, brightness).apply();
    }

    public static boolean isGlyphChargingEnabled() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_CHARGING_LEVEL_ENABLE, false) && isGlyphEnabled();
    }

    public static boolean isGlyphPowershareEnabled() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_CHARGING_POWERSHARE_ENABLE, false) && isGlyphEnabled();
    }

    public static void setGlyphChargingLevelEnabled(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_CHARGING_LEVEL_ENABLE, enable).apply();
    }

    public static void setGlyphChargingPowershareEnabled(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_CHARGING_POWERSHARE_ENABLE, enable).apply();
    }


    public static boolean isGlyphProgressScreenOffOnly() {
        return PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(Constants.GLYPH_PROGRESS_SCREEN_OFF_ONLY, false);
    }
    
    public static boolean isGlyphNotifsSubEssentialEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(Constants.GLYPH_NOTIFS_SUB_ESSENTIAL, false);
    }
    
    public static boolean isGlyphNotifsClearOnUnlockEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(Constants.GLYPH_NOTIFS_CLEAR_ON_UNLOCK, true); // True by default for dGlyphs parity
    }

    public static boolean isGlyphAudioMuteEnabled() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_AUDIO_MUTE_ENABLE, false);
    }

    public static void setGlyphAudioMuteEnabled(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_AUDIO_MUTE_ENABLE, enable).apply();
    }

    public static boolean isGlyphCallEnabled() {
        Context ctx = getContext();
        return Settings.Secure.getInt(ctx.getContentResolver(),
                Constants.GLYPH_CALL_ENABLE, 1) != 0 && isGlyphEnabled();
    }

    public static boolean setGlyphCallEnabled(boolean enable) {
        Context ctx = getContext();
        return Settings.Secure.putInt(ctx.getContentResolver(),
                Constants.GLYPH_CALL_ENABLE, enable ? 1 : 0);
    }

    public static String getGlyphCallAnimation() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getString(Constants.GLYPH_CALL_SUB_ANIMATIONS,
                        ResourceUtils.getString("glyph_settings_call_animations_default"));
    }

    public static boolean isGlyphMusicVisualizerEnabled() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_MUSIC_VISUALIZER_ENABLE, false) && isGlyphEnabled();
    }

    public static boolean isGlyphShakeWhileScreenOnEnabled() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_SHAKE_WHILE_SCREEN_ON, false);
    }

    public static void setGlyphShakeWhileScreenOnEnabled(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_SHAKE_WHILE_SCREEN_ON, enable).apply();
    }

    public static boolean isGlyphShakeAllowInSleepEnabled() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_SHAKE_ALLOW_IN_SLEEP, false);
    }

    public static void setGlyphShakeAllowInSleepEnabled(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_SHAKE_ALLOW_IN_SLEEP, enable).apply();
    }

    public static boolean isGlyphVolumeLevelEnabled() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_VOLUME_LEVEL_ENABLE, false) && isGlyphEnabled();
    }

    public static void setGlyphVolumeLevelEnabled(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_VOLUME_LEVEL_ENABLE, enable).apply();
    }

    public static void setGlyphMusicVisualizerEnabled(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_MUSIC_VISUALIZER_ENABLE, enable).apply();
    }

    public static boolean isGlyphNotifsEnabled() {
        Context ctx = getContext();
        return Settings.Secure.getInt(ctx.getContentResolver(),
                Constants.GLYPH_NOTIFS_ENABLE, 1) != 0 && isGlyphEnabled();
    }

    public static boolean setGlyphNotifsEnabled(boolean enable) {
        Context ctx = getContext();
        return Settings.Secure.putInt(ctx.getContentResolver(),
                Constants.GLYPH_NOTIFS_ENABLE, enable ? 1 : 0);
    }

    public static String getGlyphNotifsAnimation() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getString(Constants.GLYPH_NOTIFS_SUB_ANIMATIONS,
                        ResourceUtils.getString("glyph_settings_notifs_animations_default"));
    }

    public static boolean isGlyphNotifsAppEnabled(String app) {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(app, true) && isGlyphNotifsEnabled();
    }

    public static boolean isGlyphNotifsAppEssential(String app) {
        Context ctx = getContext();
        Set<String> selectedValues = PreferenceManager.getDefaultSharedPreferences(ctx)
                .getStringSet(Constants.GLYPH_NOTIFS_SUB_ESSENTIAL, new HashSet<String>());
        return selectedValues.contains(app) && isGlyphNotifsEnabled();
    }

    public static boolean isGlyphAutoBrightnessEnabled() {
        Context ctx = getContext();
        return !ResourceUtils.getString("glyph_light_sensor").isBlank()
                && PreferenceManager.getDefaultSharedPreferences(ctx)
                        .getBoolean(Constants.GLYPH_AUTO_BRIGHTNESS_ENABLE, false)
                && isGlyphEnabled();
    }

    public static int getFlipRingerMode() {
        return android.provider.Settings.Secure.getInt(getContext().getContentResolver(),
                Constants.GLYPH_FLIP_RINGER_MODE, android.media.AudioManager.RINGER_MODE_VIBRATE);
    }

    public static boolean isGlyphComposerEnabled() {
        return android.provider.Settings.Secure.getInt(getContext().getContentResolver(),
                Constants.GLYPH_COMPOSER_ENABLE, 1) == 1;
    }

    public static void setGlyphComposerEnabled(boolean enabled) {
        Settings.Secure.putInt(getContext().getContentResolver(),
                Constants.GLYPH_COMPOSER_ENABLE, enabled ? 1 : 0);
    }

    public static boolean useComposerFallback() {
        return Settings.Secure.getInt(getContext().getContentResolver(),
                Constants.GLYPH_COMPOSER_FALLBACK, 1) == 1;
    }

    public static boolean isGlyphProgressEnabled() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_PROGRESS_ENABLE, false) && isGlyphEnabled();
    }

    public static boolean isGlyphProgressMusicEnabled() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_PROGRESS_MUSIC_ENABLE, false) && isGlyphProgressEnabled();
    }

    public static void setGlyphProgressEnabled(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_PROGRESS_ENABLE, enable).apply();
    }

    public static void setGlyphProgressMusicEnabled(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_PROGRESS_MUSIC_ENABLE, enable).apply();
    }

    public static boolean isGlyphNotifHapticEnabled() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_NOTIF_HAPTIC_ENABLE, true);
    }

    public static int getGlyphNotifHapticStrength() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getInt(Constants.GLYPH_NOTIF_HAPTIC_STRENGTH, 100);
    }

    public static void setGlyphNotifHapticsEnabled(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_NOTIF_HAPTIC_ENABLE, enable).apply();
    }

    public static void setGlyphNotifHapticStrength(int strength) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putInt(Constants.GLYPH_NOTIF_HAPTIC_STRENGTH, strength).apply();
    }

    public static boolean isGlyphVolumeFlipOnly() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_VOLUME_FLIP_ONLY, false);
    }

    public static boolean isGlyphProgressFlipOnly() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_PROGRESS_FLIP_ONLY, false);
    }

    public static boolean isGlyphMusicVisualizerFlipOnly() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_MUSIC_VISUALIZER_FLIP_ONLY, false);
    }

    public static boolean isGlyphChargingFlipOnly() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_CHARGING_FLIP_ONLY, false);
    }

    public static boolean isGlyphIndicatorsFlipOnly() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_INDICATORS_FLIP_ONLY, false);
    }

    public static void setGlyphVolumeFlipOnly(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_VOLUME_FLIP_ONLY, enable).apply();
    }

    public static boolean isGlyphVolumeScreenOffOnly() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_VOLUME_SCREEN_OFF_ONLY, false);
    }

    public static void setGlyphVolumeScreenOffOnly(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_VOLUME_SCREEN_OFF_ONLY, enable).apply();
    }

    public static void setGlyphProgressFlipOnly(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_PROGRESS_FLIP_ONLY, enable).apply();
    }

    public static void setGlyphProgressScreenOffOnly(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_PROGRESS_SCREEN_OFF_ONLY, enable).apply();
    }

    public static void setGlyphMusicVisualizerFlipOnly(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_MUSIC_VISUALIZER_FLIP_ONLY, enable).apply();
    }

    public static void setGlyphChargingFlipOnly(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_CHARGING_FLIP_ONLY, enable).apply();
    }

    public static void setGlyphIndicatorsFlipOnly(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_INDICATORS_FLIP_ONLY, enable).apply();
    }

    public static boolean isGlyphShakeEnabled() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean(Constants.GLYPH_SHAKE_TORCH_ENABLE, false) && isGlyphEnabled();
    }

    public static int getGlyphShakeSensitivity() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getInt(Constants.GLYPH_SHAKE_SENSITIVITY, 50);
    }

    public static int getGlyphShakeCount() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getInt(Constants.GLYPH_SHAKE_COUNT, 2);
    }

    public static int getGlyphShakeHapticIntensity() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getInt(Constants.GLYPH_SHAKE_HAPTIC_INTENSITY, 100);
    }

    public static void setGlyphShakeTorchEnabled(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean(Constants.GLYPH_SHAKE_TORCH_ENABLE, enable).apply();
    }

    public static void setGlyphShakeSensitivity(int sensitivity) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putInt(Constants.GLYPH_SHAKE_SENSITIVITY, sensitivity).apply();
    }

    public static void setGlyphShakeHapticIntensity(int intensity) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putInt(Constants.GLYPH_SHAKE_HAPTIC_INTENSITY, intensity).apply();
    }

    public static void setGlyphShakeCount(int count) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putInt(Constants.GLYPH_SHAKE_COUNT, count).apply();
    }

    public static boolean isGlyphScreenOffOnly() {
        Context ctx = getContext();
        return PreferenceManager.getDefaultSharedPreferences(ctx)
                .getBoolean("glyph_settings_screen_off_only", false);
    }

    public static void setGlyphScreenOffOnly(boolean enable) {
        Context ctx = getContext();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .putBoolean("glyph_settings_screen_off_only", enable).apply();
    }
}
