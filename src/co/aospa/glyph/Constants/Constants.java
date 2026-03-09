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

package co.aospa.glyph.Constants;

import android.content.Context;

import co.aospa.glyph.Utils.ResourceUtils;

/**
 * Central class for holding essential application constants, setting keys,
 * and shared global variables used throughout the Glyph system.
 */
public final class Constants {

    /** Tag used for logging. */
    private static final String TAG = "GlyphConstants";
    /** Debug flag for logging. */
    private static final boolean DEBUG = true;

    /** Global application context. */
    public static Context CONTEXT;
    /** Maximum brightness level allowed for a Glyph pattern. */
    public static final int MAX_PATTERN_BRIGHTNESS = 4095;

    /** Cached device codename (e.g., "phone1" or "phone2"). */
    private static String device = null;

    /** Currently set brightness limit. */
    private static int brightness = -1;
    /** Maximum hardware-supported brightness limit. */
    private static int brightnessMax = -1;
    /** Array of brightness levels matching ambient light conditions. */
    private static int[] brightnessLevels = null;
    /** Array containing supported pattern animation lengths in milliseconds. */
    private static int[] supportedAnimationPatternLengths = null;

    /** Key for main Glyph enable toggle. */
    public static final String GLYPH_ENABLE = "glyph_enable";
    /** Key for Flip to Glyph feature toggle. */
    public static final String GLYPH_FLIP_ENABLE = "glyph_settings_flip_toggle";
    /** Key for Glyph brightness setting. */
    public static final String GLYPH_BRIGHTNESS = "glyph_settings_brightness";
    /** Category key for charging settings. */
    public static final String GLYPH_CHARGING_CATEGORY = "glyph_settings_charging";
    /** Key for charging level indicator toggle. */
    public static final String GLYPH_CHARGING_LEVEL_ENABLE = "glyph_settings_charging_level";
    /** Key for powershare (reverse wireless charging) indicator toggle. */
    public static final String GLYPH_CHARGING_POWERSHARE_ENABLE = "glyph_settings_charging_powershare";
    /** Category key for call settings. */
    public static final String GLYPH_CALL_CATEGORY = "glyph_settings_call";
    /** Key for incoming call Glyph toggle. */
    public static final String GLYPH_CALL_ENABLE = "glyph_settings_call_toggle";
    /** Key for sub-preview of call pattern. */
    public static final String GLYPH_CALL_SUB_PREVIEW = "glyph_settings_call_sub_preview";
    /** Key for selecting call animations. */
    public static final String GLYPH_CALL_SUB_ANIMATIONS = "glyph_settings_call_sub_animations";
    /** Key for enabling specific call animations. */
    public static final String GLYPH_CALL_SUB_ENABLE = "glyph_settings_call_sub_toggle";
    /** Key for music visualizer toggle. */
    public static final String GLYPH_MUSIC_VISUALIZER_ENABLE = "glyph_settings_music_visualizer_toggle";
    /** Key for general notifications Glyph toggle. */
    public static final String GLYPH_NOTIFS_ENABLE = "glyph_settings_notifs_toggle";
    /** Key for sub-preview of notification pattern. */
    public static final String GLYPH_NOTIFS_SUB_PREVIEW = "glyph_settings_notifs_sub_preview";
    /** Key for selecting notification animations. */
    public static final String GLYPH_NOTIFS_SUB_ANIMATIONS = "glyph_settings_notifs_sub_animations";
    /** Key for Essential notification toggle. */
    public static final String GLYPH_NOTIFS_SUB_ESSENTIAL = "glyph_settings_notifs_sub_essential";
    /** Category key for notification sub-settings. */
    public static final String GLYPH_NOTIFS_SUB_CATEGORY = "glyph_settings_notifs_sub";
    /** Key for enabling specific notification animations. */
    public static final String GLYPH_NOTIFS_SUB_ENABLE = "glyph_settings_notifs_sub_toggle";
    /** Key for volume level indicator toggle. */
    public static final String GLYPH_VOLUME_LEVEL_ENABLE = "glyph_settings_volume_level_toggle";
    /** Key for auto-brightness toggle. */
    public static final String GLYPH_AUTO_BRIGHTNESS_ENABLE = "glyph_settings_auto_brightness_toggle";
    /** Key for shake device for torch toggle. */
    public static final String GLYPH_SHAKE_TORCH_ENABLE = "glyph_settings_shake_torch_toggle";
    public static final String GLYPH_SHAKE_WHILE_SCREEN_ON = "glyph_settings_shake_screen_on";
    public static final String GLYPH_SHAKE_ALLOW_IN_SLEEP = "glyph_settings_shake_allow_sleep";
    /** Key for shake torch sensitivity setting. */
    public static final String GLYPH_SHAKE_SENSITIVITY = "glyph_settings_shake_sensitivity";
    /** Key for the number of required shakes. */
    public static final String GLYPH_SHAKE_COUNT = "glyph_settings_shake_count";
    /** Key for shake haptic intensity setting. */
    public static final String GLYPH_SHAKE_HAPTIC_INTENSITY = "glyph_settings_shake_haptic_intensity";
    /** Key for setting the ringer mode when flipped (e.g., silent). */
    public static final String GLYPH_FLIP_RINGER_MODE = "glyph_settings_flip_ringer_mode";
    /** Key for enabling Glyph Composer feature. */
    public static final String GLYPH_COMPOSER_ENABLE = "glyph_settings_composer_enable";
    /** Key for fallback composer ringtones. */
    public static final String GLYPH_COMPOSER_FALLBACK = "glyph_settings_composer_fallback";
    /** Key for composer preview setting. */
    public static final String GLYPH_COMPOSER_PREVIEW = "glyph_settings_composer_preview";
    /** Key for setting a manual activation schedule. */
    public static final String GLYPH_SCHEDULE = "glyph_settings_schedule";
    /** Key for music playback progress toggle. */
    public static final String GLYPH_PROGRESS_MUSIC_ENABLE = "glyph_settings_progress_music_toggle";
    /** Key for general progress indicator toggle. */
    public static final String GLYPH_PROGRESS_ENABLE = "glyph_settings_progress_toggle";
    public static final String GLYPH_PROGRESS_FLIP_ONLY = "glyph_progress_flip_only";
    public static final String GLYPH_PROGRESS_SCREEN_OFF_ONLY = "glyph_settings_progress_screen_off_only";
    public static final String GLYPH_CHARGING_FLIP_ONLY = "glyph_charging_flip_only";
    public static final String GLYPH_MUSIC_VISUALIZER_FLIP_ONLY = "glyph_music_visualizer_flip_only";

    /** Key for notification haptics toggle. */
    public static final String GLYPH_NOTIF_HAPTIC_ENABLE = "glyph_settings_notif_haptic_enable";
    /** Key for notification haptic strength setting. */
    public static final String GLYPH_NOTIF_HAPTIC_STRENGTH = "glyph_settings_notif_haptic_strength";
    /** Key for volume flip only toggle. */
    public static final String GLYPH_VOLUME_FLIP_ONLY = "glyph_settings_volume_flip_only";
    /** Key for disabling animation when screen is on. */
    public static final String GLYPH_NOTIFS_CLEAR_ON_UNLOCK = "glyph_settings_notifs_clear_on_unlock";
    public static final String GLYPH_VOLUME_SCREEN_OFF_ONLY = "glyph_settings_volume_screen_off_only";
    /** Key for indicators flip only toggle. */
    public static final String GLYPH_INDICATORS_FLIP_ONLY = "glyph_settings_indicators_flip_only";
    /** Key for audio mute custom ringtones toggle. */
    public static final String GLYPH_AUDIO_MUTE_ENABLE = "glyph_settings_audio_mute_enable";

    /** Array of package names to ignore for Glyph interactions. */
    public static final String[] APPS_TO_IGNORE = {
            "android",
            "com.android.traceur",
            // "com.google.android.dialer",
            "com.google.android.setupwizard",
            "dev.kdrag0n.dyntheme.privileged.sys"
    };

    /** Array of notification signatures to ignore. */
    public static final String[] NOTIFS_TO_IGNORE = {
            "com.google.android.dialer:phone_incoming_call",
            "com.google.android.dialer:phone_ongoing_call",
            "com.android.systemui:BAT"
    };

    /**
     * Retrieves the current device codename.
     * 
     * @return The codename of the device (e.g., "phone1").
     */
    public static String getDevice() {
        if (device == null)
            device = ResourceUtils.getString("glyph_settings_device");

        return device;
    }

    /**
     * Sets the global maximum brightness.
     * 
     * @param b Brightness value to set.
     * @return True if successful, false if the value exceeds supported maximum.
     */
    public static boolean setBrightness(int b) {
        if (b > ResourceUtils.getInteger("glyph_settings_brightness_max"))
            return false;

        brightness = b;
        return true;
    }

    /**
     * Retrieves the currently set brightness level.
     * 
     * @return The brightness value.
     */
    public static int getBrightness() {
        if (brightness == -1)
            brightness = ResourceUtils.getInteger("glyph_settings_brightness_max");

        return brightness;
    }

    /**
     * Retrieves the absolute maximum brightness hardware limit.
     * 
     * @return Maximum hardware brightness.
     */
    public static int getMaxBrightness() {
        if (brightnessMax == -1)
            brightnessMax = ResourceUtils.getInteger("glyph_settings_brightness_max");

        return brightnessMax;
    }

    /**
     * Retrieves an array of supported brightness levels logic.
     * 
     * @return Int array of brightness levels.
     */
    public static int[] getBrightnessLevels() {
        if (brightnessLevels == null)
            brightnessLevels = ResourceUtils.getIntArray("glyph_settings_brightness_levels");

        return brightnessLevels;
    }

    /**
     * Retrieves an array of supported lengths for animated patterns.
     * 
     * @return Int array of animation lengths (ms).
     */
    public static int[] getSupportedAnimationPatternLengths() {
        if (supportedAnimationPatternLengths == null)
            supportedAnimationPatternLengths = ResourceUtils
                    .getIntArray("glyph_settings_animations_supported_pattern_lengths");

        return supportedAnimationPatternLengths;
    }

}