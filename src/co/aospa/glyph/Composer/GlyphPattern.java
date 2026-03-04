/*
 * Copyright (C) 2024-2025 LunarisAOSP
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

package co.aospa.glyph.Composer;

import java.util.List;

/**
 * Data model representing a parsed Glyph Composer pattern, containing
 * metadata and a list of animation frames synchronized with an audio file.
 */
public class GlyphPattern {

    /** The version of the pattern format. */
    private int version;
    /** The associated audio file name. */
    private String audioFile;
    /** The total duration of the pattern in milliseconds. */
    private long duration;
    /** List of animation frames. */
    private List<GlyphFrame> frames;

    /** Default constructor. */
    public GlyphPattern() {
    }

    /**
     * Constructor initializing all fields.
     * 
     * @param version   Format version.
     * @param audioFile Associated audio file name.
     * @param duration  Total duration in ms.
     * @param frames    List of frames.
     */
    public GlyphPattern(int version, String audioFile, long duration, List<GlyphFrame> frames) {
        this.version = version;
        this.audioFile = audioFile;
        this.duration = duration;
        this.frames = frames;
    }

    /**
     * Gets the pattern version.
     * 
     * @return The format version.
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the pattern version.
     * 
     * @param version The format version.
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Gets the audio file name.
     * 
     * @return The audio file name.
     */
    public String getAudioFile() {
        return audioFile;
    }

    /**
     * Sets the audio file name.
     * 
     * @param audioFile The audio file name.
     */
    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    /**
     * Gets the total duration.
     * 
     * @return Duration in milliseconds.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets the total duration.
     * 
     * @param duration Duration in milliseconds.
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Gets the list of animation frames.
     * 
     * @return A list of {@link GlyphFrame}.
     */
    public List<GlyphFrame> getFrames() {
        return frames;
    }

    /**
     * Sets the list of animation frames.
     * 
     * @param frames A list of {@link GlyphFrame}.
     */
    public void setFrames(List<GlyphFrame> frames) {
        this.frames = frames;
    }

    /**
     * Represents a single frame in the Glyph animation sequence.
     */
    public static class GlyphFrame {
        /** Timestamp of the frame in milliseconds. */
        private long timestamp;
        /** Array of LED zones to activate. */
        private int[] zones;
        /** Brightness level for the zones (0-4095). */
        private int brightness;
        /** Duration to keep the zones active in milliseconds. */
        private int duration;

        /** Default constructor. */
        public GlyphFrame() {
        }

        /**
         * Constructor initializing all fields.
         * 
         * @param timestamp  Frame timestamp in ms.
         * @param zones      Array of zone indices.
         * @param brightness Brightness level.
         * @param duration   Active duration in ms.
         */
        public GlyphFrame(long timestamp, int[] zones, int brightness, int duration) {
            this.timestamp = timestamp;
            this.zones = zones;
            this.brightness = brightness;
            this.duration = duration;
        }

        /**
         * Gets the frame timestamp.
         * 
         * @return Timestamp in milliseconds.
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * Sets the frame timestamp.
         * 
         * @param timestamp Timestamp in milliseconds.
         */
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * Gets the active zones.
         * 
         * @return Array of zone integers.
         */
        public int[] getZones() {
            return zones;
        }

        /**
         * Sets the active zones.
         * 
         * @param zones Array of zone integers.
         */
        public void setZones(int[] zones) {
            this.zones = zones;
        }

        /**
         * Gets the brightness level.
         * 
         * @return Brightness level (0-4095).
         */
        public int getBrightness() {
            return brightness;
        }

        /**
         * Sets the brightness level.
         * 
         * @param brightness Brightness level (0-4095).
         */
        public void setBrightness(int brightness) {
            this.brightness = brightness;
        }

        /**
         * Gets the frame duration.
         * 
         * @return Duration in milliseconds.
         */
        public int getDuration() {
            return duration;
        }

        /**
         * Sets the frame duration.
         * 
         * @param duration Duration in milliseconds.
         */
        public void setDuration(int duration) {
            this.duration = duration;
        }
    }
}