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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import co.aospa.glyph.R;
import co.aospa.glyph.Constants.Constants;
import co.aospa.glyph.Manager.SettingsManager;
import co.aospa.glyph.Manager.StatusManager;
import co.aospa.glyph.Utils.FileUtils;
import co.aospa.glyph.Utils.ResourceUtils;

/**
 * Quick settings Tile linking system Torch state directly to Glyph hardware
 * LEDs.
 */
public class TorchTileService extends TileService {

    /** Log Tag. */
    private static final String TAG = "GlyphTorchTile";

    /**
     * Static signature referencing broadcast action links identifying dynamic
     * refresh actions.
     */
    private static final String ACTION_UPDATE_TILE = "co.aospa.glyph.UPDATE_TORCH_TILE";

    /**
     * Dynamic broadcast receiver managing async state transitions checking manual
     * updates passed down.
     */
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        /**
         * Dispatches quick update triggers.
         * 
         * @param context Application context context triggering block.
         * @param intent  Filter matching criteria string intent data context.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            updateState();
        }
    };

    /**
     * Initiator processing intent registries tracking global state hooks ensuring
     * tile acts reliably based on custom events.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter(ACTION_UPDATE_TILE);
        registerReceiver(mUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    /**
     * Clears dependencies.
     */
    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(mUpdateReceiver);
        } catch (Exception e) {
        }
        super.onDestroy();
    }

    /**
     * Callback triggered when visible in settings shade updating initial data
     * states correctly checking for sync statuses.
     */
    @Override
    public void onStartListening() {
        super.onStartListening();
        updateState();
    }

    /**
     * Forcefully re-evaluates all required conditions testing for proper hardware
     * engagement updating states actively.
     */
    private void updateState() {
        if (Constants.CONTEXT == null) {
            Constants.CONTEXT = getApplicationContext();
        }

        boolean glyphEnabled = SettingsManager.isGlyphEnabledIgnoreSchedule();

        if (!glyphEnabled) {
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
            getQsTile().setSubtitle(getString(R.string.glyph_accessibility_quick_settings_disabled));
            getQsTile().updateTile();
            return;
        }

        boolean enabled = getEnabled();
        getQsTile().setSubtitle(enabled ? getString(R.string.glyph_accessibility_quick_settings_on)
                : getString(R.string.glyph_accessibility_quick_settings_off));
        getQsTile().setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        getQsTile().updateTile();
    }

    /**
     * Callback engaging Torch states manipulating hard brightness nodes when UI is
     * triggered.
     */
    @Override
    public void onClick() {
        super.onClick();
        if (!SettingsManager.isGlyphEnabledIgnoreSchedule()) {
            return;
        }
        setEnabled(!getEnabled());
        updateState();
    }

    /**
     * Checks StatusManager confirming any active lights acting globally matching
     * Torch status triggers.
     * 
     * @return boolean tracking validation.
     */
    private boolean getEnabled() {
        return StatusManager.isAllLedActive();
    }

    /**
     * Actively manipulates node files bypassing managers forcing hardware
     * illumination sequences matching toggles directly.
     * 
     * @param enabled state
     */
    private void setEnabled(boolean enabled) {
        StatusManager.setAllLedsActive(enabled);
        FileUtils.writeAllLed(enabled ? Constants.getMaxBrightness() : 0);
        if (StatusManager.isEssentialLedActive() && !enabled)
            FileUtils.writeSingleLed(
                    ResourceUtils.getInteger("glyph_settings_notifs_essential_led"),
                    Constants.getMaxBrightness() / 100 * 7);
    }

    /**
     * Externally exposed static invoking intent triggers communicating to tile
     * instance actively propagating global notification broadcasts globally
     * checking states reliably updating custom items directly.
     * 
     * @param context Application.
     */
    public static void requestTileUpdate(Context context) {
        Intent intent = new Intent(ACTION_UPDATE_TILE);
        context.sendBroadcast(intent);
    }
}