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

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import co.aospa.glyph.R;
import co.aospa.glyph.Manager.SettingsManager;
import co.aospa.glyph.Utils.ServiceUtils;

/** Quick settings tile: Glyph **/
public class GlyphTileService extends TileService {

    /**
     * Called when the tile becomes visible to the user.
     */
    @Override
    public void onStartListening() {
        super.onStartListening();
        updateState();
    }

    /**
     * Updates the UI state of the tile based on current settings.
     */
    private void updateState() {
        boolean enabled = getEnabled();
        getQsTile().setSubtitle(enabled ? getString(R.string.glyph_accessibility_quick_settings_on)
                : getString(R.string.glyph_accessibility_quick_settings_off));
        getQsTile().setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        getQsTile().updateTile();
    }

    /**
     * Called when the user clicks the tile.
     */
    @Override
    public void onClick() {
        super.onClick();
        setEnabled(!getEnabled());
        updateState();
    }

    /**
     * Gets the current enable state from SettingsManager.
     * 
     * @return true if enabled.
     */
    private boolean getEnabled() {
        return SettingsManager.isGlyphEnabled();
    }

    /**
     * Updates the enable state in SettingsManager and triggers service checks.
     * 
     * @param enabled the new state.
     */
    private void setEnabled(boolean enabled) {
        SettingsManager.enableGlyph(enabled);
        ServiceUtils.checkGlyphService();
    }
}
