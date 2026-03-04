/*
 * Copyright (C) 2023-2024 Paranoid Android
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

package co.aospa.glyph.Utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.android.internal.util.ArrayUtils;

import java.io.InputStream;
import java.io.IOException;

import co.aospa.glyph.R;
import co.aospa.glyph.Constants.Constants;

/**
 * Utility to fetch configuration resources dynamically from packages
 * to allow unified constants fetching.
 */
public final class ResourceUtils {

    /** Log tag. */
    private static final String TAG = "GlyphResourceUtils";
    /** Debug flag. */
    private static final boolean DEBUG = true;

    /** Statically cached context reference. */
    private static Context context;
    /** Cached AssetManager. */
    private static AssetManager assetManager;
    /** Cached Resources object. */
    private static Resources resources;

    /** Persistently cached list of call animation file strings. */
    private static String[] callAnimations = null;
    /** Persistently cached list of notification animations file strings. */
    private static String[] notificationAnimations = null;

    /**
     * Retrieves application context lazily.
     * 
     * @return Cached context.
     */
    private static Context getContext() {
        if (context == null) {
            context = Constants.CONTEXT;
            if (context == null) {
                throw new IllegalStateException("Constants.CONTEXT is not initialized");
            }
        }
        return context;
    }

    /**
     * Safe initializer retrieving AssetManager.
     * 
     * @return Target application asset manager.
     */
    private static AssetManager getAssetManager() {
        if (assetManager == null) {
            assetManager = getContext().getAssets();
        }
        return assetManager;
    }

    /**
     * Safe initializer retrieving Resources instance.
     * 
     * @return Target app resources.
     */
    private static Resources getResources() {
        if (resources == null) {
            resources = getContext().getResources();
        }
        return resources;
    }

    /**
     * Uses reflection helper to lookup R.id integer bindings.
     * 
     * @param id   The name.
     * @param type E.g. "bool", "string".
     * @return Integer identifier reference.
     */
    public static int getIdentifier(String id, String type) {
        return getResources().getIdentifier(id, type, getContext().getPackageName());
    }

    /**
     * Dynamic bool lookup.
     * 
     * @param id key identifier.
     * @return retrieved resource boolean.
     */
    public static Boolean getBoolean(String id) {
        return getResources().getBoolean(getIdentifier(id, "bool"));
    }

    /**
     * Dynamic string lookup.
     * 
     * @param id key identifier.
     * @return text configuration.
     */
    public static String getString(String id) {
        return getResources().getString(getIdentifier(id, "string"));
    }

    /**
     * Dynamic int lookup.
     * 
     * @param id key identifier.
     * @return integer property.
     */
    public static int getInteger(String id) {
        return getResources().getInteger(getIdentifier(id, "integer"));
    }

    /**
     * Dynamic String list lookup.
     * 
     * @param id Key ID.
     * @return String assortment mapping keys.
     */
    public static String[] getStringArray(String id) {
        return getResources().getStringArray(getIdentifier(id, "array"));
    }

    /**
     * Dynamic int list lookup.
     * 
     * @param id Target list key id.
     * @return primitive integer array mapping configuration states.
     */
    public static int[] getIntArray(String id) {
        return getResources().getIntArray(getIdentifier(id, "array"));
    }

    /**
     * Fetches array of custom file paths mapped to call visualizers dynamically
     * indexing assets.
     * 
     * @return String array of valid notification files stripped of extension.
     */
    public static String[] getCallAnimations() {
        if (callAnimations == null) {
            try {
                String[] assets = getAssetManager().list("call");
                for (int i = 0; i < assets.length; i++) {
                    assets[i] = assets[i].replaceAll(".csv", "");
                }
                callAnimations = assets;
            } catch (IOException e) {
            }
        }
        return callAnimations;
    }

    /**
     * Checks "notification/" internal asset folder iteratively mapping strings
     * matching pattern sets.
     * 
     * @return Verified assets string array.
     */
    public static String[] getNotificationAnimations() {
        if (notificationAnimations == null) {
            try {
                String[] assets = getAssetManager().list("notification");
                for (int i = 0; i < assets.length; i++) {
                    assets[i] = assets[i].replaceAll(".csv", "");
                }
                notificationAnimations = assets;
            } catch (IOException e) {
            }
        }
        return notificationAnimations;
    }

    /**
     * Loads explicit InputStream of file path parsing a chosen custom Call setting
     * CSV data file.
     * 
     * @param name Key mapping corresponding filename in asset tree.
     * @return InputStream pipeline of matched entry.
     * @throws IOException Input pipeline throws upon validation failure.
     */
    public static InputStream getCallAnimation(String name) throws IOException {
        if (callAnimations == null)
            getCallAnimations();

        if (ArrayUtils.contains(callAnimations, name))
            return getAssetManager().open("call/" + name + ".csv");

        return getAssetManager()
                .open("call/" + ResourceUtils.getString("glyph_settings_call_animations_default") + ".csv");
    }

    /**
     * Loads raw InputStream targeting the chosen string key matched on notification
     * directory files.
     * 
     * @param name Specific string name identifier matching CSV resource target.
     * @return InputStream object referencing the physical system file.
     * @throws IOException Errors if no fallback found default tree path.
     */
    public static InputStream getNotificationAnimation(String name) throws IOException {
        if (notificationAnimations == null)
            getNotificationAnimations();

        if (ArrayUtils.contains(notificationAnimations, name))
            return getAssetManager().open("notification/" + name + ".csv");

        return getAssetManager()
                .open("call/" + ResourceUtils.getString("glyph_settings_notifs_animations_default") + ".csv");
    }

    /**
     * Primary lookup switching method parsing either "call/" folder trees,
     * "notification/" folders, or falling back locally implicitly.
     * 
     * @param name Asset mapping string to pull InputStream pointer.
     * @return Valid Stream output interface.
     * @throws IOException Throw upstream.
     */
    public static InputStream getAnimation(String name) throws IOException {
        if (callAnimations == null)
            getCallAnimations();
        if (notificationAnimations == null)
            getNotificationAnimations();

        if (ArrayUtils.contains(callAnimations, name)) {
            return getCallAnimation(name);
        }

        if (ArrayUtils.contains(notificationAnimations, name)) {
            return getNotificationAnimation(name);
        }

        return getAssetManager().open(name + ".csv");
    }
}