/*
 * Copyright (C) 2015 The CyanogenMod Project
 * Copyright (C) 2017 The LineageOS Project
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

package co.aospa.glyph.Tiles;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.preference.PreferenceManager;

import co.aospa.glyph.Constants.Constants;
import co.aospa.glyph.R;
import co.aospa.glyph.Manager.SettingsManager;
import co.aospa.glyph.Utils.ServiceUtils;

/** Quick settings tile for managing the Music Visualizer toggle state. **/
public class MusicVisualizerTileService extends TileService {

    /** Resolver to bind settings observer hooks. */
    private ContentResolver mContentResolver;
    /** ContentObserver checking active toggle status for UI updates. */
    private SettingObserver mSettingObserver;

    /**
     * Initiates variables resolving contexts and linking observer registries.
     */
    @Override
    public void onCreate() {
        mContentResolver = this.getContentResolver();
        mSettingObserver = new SettingObserver();
        mSettingObserver.register(mContentResolver);
    }

    /**
     * Lifecycle trigger processing tile visiblity states. Ensure active toggle
     * sync.
     */
    @Override
    public void onStartListening() {
        super.onStartListening();
        updateState();
    }

    /**
     * Internally propagates the setting checks forcing visual representations
     * matching actual app state flags.
     */
    private void updateState() {
        if (!SettingsManager.isGlyphEnabled()) {
            getQsTile().setSubtitle(getString(R.string.glyph_accessibility_quick_settings_unavailable));
            getQsTile().setState(Tile.STATE_INACTIVE);
        } else {
            boolean enabled = getEnabled();
            getQsTile().setSubtitle(enabled ? getString(R.string.glyph_accessibility_quick_settings_on)
                    : getString(R.string.glyph_accessibility_quick_settings_off));
            getQsTile().setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
    }

    /**
     * Triggers active boolean toggles. Disables music visualization manually.
     */
    @Override
    public void onClick() {
        super.onClick();
        if (SettingsManager.isGlyphEnabled()) {
            setEnabled(!getEnabled());
            updateState();
        }
    }

    /**
     * Verifies stored preferences if visually enabled.
     * 
     * @return True if feature active.
     */
    private boolean getEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(Constants.GLYPH_MUSIC_VISUALIZER_ENABLE, false);
    }

    /**
     * Edits physical preferences storing custom toggle. Checks master glyph
     * services internally.
     * 
     * @param enabled Set state boolean.
     */
    private void setEnabled(boolean enabled) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefs.edit().putBoolean(Constants.GLYPH_MUSIC_VISUALIZER_ENABLE, enabled).apply();
        ServiceUtils.checkGlyphService();
    }

    /**
     * Lifecycle method clearing memory allocations destroying context links.
     * Unregisters content watcher safely.
     */
    @Override
    public void onDestroy() {
        mSettingObserver.unregister(mContentResolver);
        super.onDestroy();
    }

    /**
     * Nested class tracking state changes asynchronously.
     */
    private class SettingObserver extends ContentObserver {
        /**
         * Standard observer initiator.
         */
        public SettingObserver() {
            super(new Handler());
        }

        /**
         * Overloads standard logic explicitly observing GLYPH root statuses.
         * 
         * @param cr content resolver instance linking observers context.
         */
        public void register(ContentResolver cr) {
            cr.registerContentObserver(Settings.Secure.getUriFor(
                    Constants.GLYPH_ENABLE), false, this);
        }

        /**
         * Deactivates internal observers closing listening links.
         * 
         * @param cr Valid context content registry instance point.
         */
        public void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        /**
         * Overidden response intercepting URI events checking changes manually.
         * Dispatches quick setting updates dynamically.
         * 
         * @param selfChange flag tracking.
         * @param uri        location mapping setting that changed state.
         */
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri.equals(Settings.Secure.getUriFor(Constants.GLYPH_ENABLE))) {
                updateState();
            }
        }
    }
}
