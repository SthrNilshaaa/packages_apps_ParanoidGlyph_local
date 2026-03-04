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

import co.aospa.glyph.Utils.ResourceUtils;

/**
 * Contains thread-safe global state variables tracking active animations
 * and LED statuses to coordinate complex lighting interruptions and priorities.
 */
public final class StatusManager {

    /** Log Tag. */
    private static final String TAG = "GlyphStatusManager";
    /** Debug flag. */
    private static final boolean DEBUG = true;

    private static volatile boolean allLedActive = false;
    private static volatile boolean animationActive = false;
    private static volatile boolean chargingAnimationActive = false;
    private static volatile boolean volumeAnimationActive = false;
    private static volatile boolean callLedActive = false;
    private static volatile boolean essentialLedActive = false;
    private static volatile boolean progressAnimationActive = false;
    private static volatile boolean isFlipped = false;
    private static volatile int progressType = 0;
    private static volatile int progressLedLast = 0;
    private static volatile int chargingLedLast = 0;
    private static int[] batteryArray = new int[ResourceUtils.getInteger("glyph_settings_battery_levels_num")];
    private static volatile int volumeLedLast = 0;
    private static int[] volumeArray = new int[ResourceUtils.getInteger("glyph_settings_volume_levels_num")];
    private static int[] progressArray = new int[ResourceUtils.getInteger("glyph_settings_volume_levels_num")];
    private static volatile int essentialLedZone = -1;

    private static volatile boolean callLedEnabled = false;

    /**
     * Checks if a general animation is currently active.
     * 
     * @return True if active.
     */
    public static boolean isAnimationActive() {
        return animationActive;
    }

    /**
     * Sets the general animation active status.
     * 
     * @param status True if active.
     */
    public static void setAnimationActive(boolean status) {
        animationActive = status;
    }

    /**
     * Checks if the charging animation is currently active.
     * 
     * @return True if active.
     */
    public static boolean isChargingAnimationActive() {
        return chargingAnimationActive;
    }

    /**
     * Sets the charging animation active status.
     * 
     * @param status True if active.
     */
    public static void setChargingAnimationActive(boolean status) {
        chargingAnimationActive = status;
    }

    /**
     * Checks if the volume animation is currently active.
     * 
     * @return True if active.
     */
    public static boolean isVolumeAnimationActive() {
        return volumeAnimationActive;
    }

    /**
     * Sets the volume animation active status.
     * 
     * @param status True if active.
     */
    public static void setVolumeAnimationActive(boolean status) {
        volumeAnimationActive = status;
    }

    /**
     * Checks if all LEDs are currently overridden and active.
     * 
     * @return True if active.
     */
    public static boolean isAllLedActive() {
        return allLedActive;
    }

    /**
     * Sets the all LEDs active status.
     * 
     * @param status True if active.
     */
    public static void setAllLedsActive(boolean status) {
        allLedActive = status;
    }

    /**
     * Checks if a call LED animation is currently active.
     * 
     * @return True if active.
     */
    public static boolean isCallLedActive() {
        return callLedActive;
    }

    /**
     * Sets the call LED active status.
     * 
     * @param status True if active.
     */
    public static void setCallLedActive(boolean status) {
        callLedActive = status;
    }

    /**
     * Checks if the essential LED indicator is currently active.
     * 
     * @return True if active.
     */
    public static boolean isEssentialLedActive() {
        return essentialLedActive;
    }

    /**
     * Sets the essential LED active status.
     * 
     * @param status True if active.
     */
    public static void setEssentialLedActive(boolean status) {
        essentialLedActive = status;
    }

    /**
     * Gets the last updated charging LED index.
     * 
     * @return The LED index.
     */
    public static int getChargingLedLast() {
        return chargingLedLast;
    }

    /**
     * Sets the last updated charging LED index.
     * 
     * @param last The LED index.
     */
    public static void setChargingLedLast(int last) {
        chargingLedLast = last;
    }

    /**
     * Gets the current battery animation array representing LED segments.
     * 
     * @return Array of segment brightnesses.
     */
    public static synchronized int[] getBatteryArray() {
        return batteryArray;
    }

    /**
     * Sets the battery animation array.
     * 
     * @param batteryArrayNext New array of segment brightnesses.
     */
    public static synchronized void setBatteryArray(int[] batteryArrayNext) {
        batteryArray = batteryArrayNext;
    }

    /**
     * Gets the last updated volume LED index.
     * 
     * @return The LED index.
     */
    public static int getVolumeLedLast() {
        return volumeLedLast;
    }

    /**
     * Sets the last updated volume LED index.
     * 
     * @param last The LED index.
     */
    public static void setVolumeLedLast(int last) {
        volumeLedLast = last;
    }

    /**
     * Gets the current volume animation array representing LED segments.
     * 
     * @return Array of segment brightnesses.
     */
    public static synchronized int[] getVolumeArray() {
        return volumeArray;
    }

    /**
     * Sets the volume animation array.
     * 
     * @param volumeArrayNext New array of segment brightnesses.
     */
    public static synchronized void setVolumeArray(int[] volumeArrayNext) {
        volumeArray = volumeArrayNext;
    }

    /**
     * Checks if call LEDs are conceptually enabled by the user or settings.
     * 
     * @return True if enabled.
     */
    public static boolean isCallLedEnabled() {
        return callLedEnabled;
    }

    /**
     * Sets the call LED enabled state.
     * 
     * @param status True if enabled.
     */
    public static void setCallLedEnabled(boolean status) {
        callLedEnabled = status;
    }

    /**
     * Sets the progress animation active status.
     * 
     * @param status True if active.
     */
    public static void setProgressAnimationActive(boolean status) {
        progressAnimationActive = status;
    }

    /**
     * Gets the progress type identifier.
     * 
     * @return The progress type integer.
     */
    public static int getProgressType() {
        return progressType;
    }

    /**
     * Sets the progress type identifier.
     * 
     * @param type The progress type integer.
     */
    public static void setProgressType(int type) {
        progressType = type;
    }

    /**
     * Gets the last updated progress LED index.
     * 
     * @return The LED index.
     */
    public static int getProgressLedLast() {
        return progressLedLast;
    }

    /**
     * Sets the last updated progress LED index.
     * 
     * @param last The LED index.
     */
    public static void setProgressLedLast(int last) {
        progressLedLast = last;
    }

    /**
     * Gets the current progress animation array representing LED segments.
     * 
     * @return Array of segment brightnesses.
     */
    public static synchronized int[] getProgressArray() {
        return progressArray;
    }

    /**
     * Sets the progress animation array.
     * 
     * @param progressArrayNext New array of segment brightnesses.
     */
    public static synchronized void setProgressArray(int[] progressArrayNext) {
        progressArray = progressArrayNext;
    }

    /**
     * Checks if the Glyph hardware is completely idle and not showing any
     * animations.
     * 
     * @return True if hardware is available.
     */
    public static boolean isGlyphIdle() {
        if (isAllLedActive() || isCallLedActive() || isAnimationActive()
                || isChargingAnimationActive() || isVolumeAnimationActive() || isCallLedEnabled()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Obtains the target LED zone dynamically falling back to globally matched
     * defaults.
     * 
     * @return Hardware zone integer directly translatable to LED matrices.
     */
    public static int getEssentialLedZone() {
        if (essentialLedZone == -1) {
            essentialLedZone = ResourceUtils.getInteger("glyph_settings_notifs_essential_led");
        }
        return essentialLedZone;
    }

    /**
     * Mutates the global essential LED zone temporarily for overriding defaults.
     * 
     * @param zone New target zone.
     */
    public static void setEssentialLedZone(int zone) {
        essentialLedZone = zone;
    }

    /**
     * Checks if the device is currently flipped face-down.
     * 
     * @return True if flipped.
     */
    public static boolean isFlipped() {
        return isFlipped;
    }

    /**
     * Sets the device flipped state.
     * 
     * @param flipped True if flipped.
     */
    public static void setFlipped(boolean flipped) {
        isFlipped = flipped;
    }
}
