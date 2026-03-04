/*
 * Copyright (C) 2024-2025 Lunaris AOSP
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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

import co.aospa.glyph.Constants.Constants;
import co.aospa.glyph.Utils.ResourceUtils;

/**
 * Manager class responsible for handling Essential LED notifications,
 * identifying which apps are prioritized, and linking custom LED zones to them.
 */
public final class EssentialLedManager {

    /** Log Tag. */
    private static final String TAG = "EssentialLedManager";
    /** Debug flag. */
    private static final boolean DEBUG = true;

    /** Shared preference key storing the list of essential applications. */
    private static final String PREF_ESSENTIAL_APPS = "glyph_essential_apps";
    /** Prefix for storing app-specific LED zone preferences. */
    private static final String PREF_APP_LED_ZONE_PREFIX = "glyph_essential_led_zone_";

    /**
     * Retrieves the set of currently configured essential applications.
     * 
     * @param context Host application context.
     * @return Set containing package names of essential apps.
     */
    public static Set<String> getEssentialApps(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return new HashSet<>(prefs.getStringSet(
                Constants.GLYPH_NOTIFS_SUB_ESSENTIAL,
                new HashSet<>()));
    }

    /**
     * Adds an application to the essential notifications list.
     * 
     * @param context     Host application context.
     * @param packageName Target app package name.
     */
    public static void addEssentialApp(Context context, String packageName) {
        Set<String> apps = getEssentialApps(context);
        apps.add(packageName);
        saveEssentialApps(context, apps);

        if (DEBUG)
            Log.d(TAG, "Added essential app: " + packageName);
    }

    /**
     * Removes an application from the essential notifications list.
     * 
     * @param context     Host application context.
     * @param packageName Target app package name.
     */
    public static void removeEssentialApp(Context context, String packageName) {
        Set<String> apps = getEssentialApps(context);
        apps.remove(packageName);
        saveEssentialApps(context, apps);

        if (DEBUG)
            Log.d(TAG, "Removed essential app: " + packageName);
    }

    /**
     * Saves the modified set of essential apps to persistent storage.
     * 
     * @param context Host application context.
     * @param apps    Updated Set of package names to store.
     */
    private static void saveEssentialApps(Context context, Set<String> apps) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putStringSet(Constants.GLYPH_NOTIFS_SUB_ESSENTIAL, apps).apply();
    }

    /**
     * Retrieves the custom configured LED zone for a specific app.
     * 
     * @param context     Application context.
     * @param packageName The app package name.
     * @return The configured integer zone identifier, or -1 if none is set.
     */
    public static int getAppLedZone(Context context, String packageName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_APP_LED_ZONE_PREFIX + packageName, -1);
    }

    /**
     * Assigns a custom LED zone specifically targeting one app.
     * 
     * @param context     Application context.
     * @param packageName App package identifier.
     * @param zone        Integer zone destination.
     */
    public static void setAppLedZone(Context context, String packageName, int zone) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(PREF_APP_LED_ZONE_PREFIX + packageName, zone).apply();

        if (DEBUG)
            Log.d(TAG, "Set LED zone " + zone + " for app: " + packageName);
    }

    /**
     * Clears overridden custom LED zone assignments restoring defaults.
     * 
     * @param context     Context.
     * @param packageName Target app identifier.
     */
    public static void removeAppLedZone(Context context, String packageName) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(PREF_APP_LED_ZONE_PREFIX + packageName).apply();

        if (DEBUG)
            Log.d(TAG, "Removed custom LED zone for app: " + packageName);
    }

    /**
     * Obtains the target LED zone dynamically falling back to globally matched
     * defaults.
     * 
     * @param context     Host application context.
     * @param packageName Analyzed package name triggering notifications.
     * @return Hardware zone integer directly translatable to LED matrices.
     */
    public static int getEffectiveLedZone(Context context, String packageName) {
        int customZone = getAppLedZone(context, packageName);

        if (customZone != -1) {
            if (DEBUG)
                Log.d(TAG, "Using custom LED zone " + customZone + " for app: " + packageName);
            return customZone;
        }

        int defaultZone = ResourceUtils.getInteger("glyph_settings_notifs_essential_led");
        if (DEBUG)
            Log.d(TAG, "Using default LED zone " + defaultZone + " for app: " + packageName);
        return defaultZone;
    }

    /**
     * Validates if a tracked application is marked essential evaluating persistent
     * records.
     * 
     * @param context     Valid execution context.
     * @param packageName Investigated triggerer identifier.
     * @return True if notification meets essential escalation criteria.
     */
    public static boolean isAppEssential(Context context, String packageName) {
        return getEssentialApps(context).contains(packageName);
    }
}
