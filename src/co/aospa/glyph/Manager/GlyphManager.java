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

import co.aospa.glyph.Utils.FileUtils;
import co.aospa.glyph.Utils.ResourceUtils;

/**
 * High-level manager for controlling Nothing Phone Glyph LEDs.
 * Bridges the UI/Effects logic with the low-level sysfs FileUtils.
 */
public final class GlyphManager {

    public static final int MAX_BRIGHTNESS = 4095;

    public enum Glyph {
        CAMERA("glyph_settings_paths_rear_cam_absolute"),
        DIAGONAL("glyph_settings_paths_front_cam_absolute"),
        MAIN("glyph_settings_paths_round_absolute"),
        LINE("glyph_settings_paths_vline_absolute"),
        DOT("glyph_settings_paths_dot_absolute"),
        SINGLE_LED("glyph_settings_paths_video_absolute");

        public final String resourceKey;

        Glyph(String resourceKey) {
            this.resourceKey = resourceKey;
        }

        public String getPath() {
            return ResourceUtils.getString(resourceKey);
        }
    }

    /**
     * Sets the brightness for a specific Glyph LED group.
     */
    public static void setBrightness(Glyph glyph, int brightness) {
        String path = glyph.getPath();
        if (path.isEmpty())
            return;

        // Handle special case for video LED if needed, otherwise standard write
        if (glyph == Glyph.SINGLE_LED) {
            // Some firmwares use an effect node for the video led
            String effectPath = ResourceUtils.getString("glyph_settings_paths_video_effect_absolute");
            if (!effectPath.isEmpty()) {
                FileUtils.writeLine(effectPath, brightness > 0 ? "1" : "0");
                return;
            }
        }

        FileUtils.writeLine(path, brightness);
    }

    /**
     * Updates individual LEDs by index (0-11 or 0-14 depending on NP1/NP2).
     */
    public static void setBrightnessSingle(int index, int brightness) {
        FileUtils.writeSingleLed(index, brightness);
    }

    /**
     * Updates all LEDs simultaneously via the frame node.
     */
    public static void setFrame(int[] values) {
        FileUtils.writeFrameLed(values);
    }

    /**
     * Toggles all LEDs on or off.
     */
    public static void toggleAll(boolean turnOn) {
        int val = turnOn ? MAX_BRIGHTNESS : 0;
        FileUtils.writeAllLed(val);
    }
}
