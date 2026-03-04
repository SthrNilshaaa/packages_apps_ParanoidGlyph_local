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

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to parse GlyphComposer pattern files (JSON format) from
 * local storage or URIs into {@link GlyphPattern} objects.
 */
public class GlyphComposerParser {

    /** Log Tag. */
    private static final String TAG = "GlyphComposerParser";
    /** Debug flag. */
    private static final boolean DEBUG = true;

    /**
     * Parses a Glyph pattern JSON file from the given file path.
     * 
     * @param filePath Absolute path to the .glyphring or .json pattern file.
     * @return A populated {@link GlyphPattern} object or null if parsing fails.
     */
    public static GlyphPattern parseFromFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            if (DEBUG)
                Log.e(TAG, "Invalid file path");
            return null;
        }

        File file = new File(filePath);
        if (!file.exists() || !file.canRead()) {
            if (DEBUG)
                Log.e(TAG, "File does not exist or cannot be read: " + filePath);
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            GlyphPattern pattern = parseJson(json.toString());
            if (DEBUG)
                Log.d(TAG, "Successfully parsed pattern from: " + filePath);
            return pattern;

        } catch (IOException e) {
            if (DEBUG)
                Log.e(TAG, "Error reading file: " + filePath, e);
            return null;
        } catch (JSONException e) {
            if (DEBUG)
                Log.e(TAG, "Invalid JSON format in: " + filePath, e);
            return null;
        }
    }

    /**
     * Parses a Glyph pattern JSON file from a given Content URI.
     * 
     * @param context Application context to resolve the URI.
     * @param uri     Content URI pointing to the pattern file.
     * @return A populated {@link GlyphPattern} object or null if parsing fails.
     */
    public static GlyphPattern parseFromUri(Context context, Uri uri) {
        if (uri == null) {
            if (DEBUG)
                Log.e(TAG, "Invalid URI");
            return null;
        }

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            GlyphPattern pattern = parseJson(json.toString());
            if (DEBUG)
                Log.d(TAG, "Successfully parsed pattern from URI: " + uri);
            return pattern;

        } catch (IOException e) {
            if (DEBUG)
                Log.e(TAG, "Error reading URI: " + uri, e);
            return null;
        } catch (JSONException e) {
            if (DEBUG)
                Log.e(TAG, "Invalid JSON format from URI: " + uri, e);
            return null;
        }
    }

    /**
     * Internal method to parse the JSON string into a GlyphPattern object.
     * 
     * @param jsonString The raw JSON string.
     * @return A populated {@link GlyphPattern}.
     * @throws JSONException If the JSON structure is invalid.
     */
    private static GlyphPattern parseJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);

        GlyphPattern pattern = new GlyphPattern();
        pattern.setVersion(json.optInt("version", 1));
        pattern.setAudioFile(json.optString("audio_file", ""));
        pattern.setDuration(json.optLong("duration", 0));

        JSONArray framesArray = json.getJSONArray("frames");
        List<GlyphPattern.GlyphFrame> frames = new ArrayList<>();

        for (int i = 0; i < framesArray.length(); i++) {
            JSONObject frameJson = framesArray.getJSONObject(i);
            GlyphPattern.GlyphFrame frame = new GlyphPattern.GlyphFrame();

            frame.setTimestamp(frameJson.getLong("timestamp"));
            frame.setBrightness(frameJson.getInt("brightness"));
            frame.setDuration(frameJson.getInt("duration"));

            // Parse zones array
            JSONArray zonesArray = frameJson.getJSONArray("zones");
            int[] zones = new int[zonesArray.length()];
            for (int j = 0; j < zonesArray.length(); j++) {
                zones[j] = zonesArray.getInt(j);
            }
            frame.setZones(zones);

            frames.add(frame);
        }

        pattern.setFrames(frames);
        return pattern;
    }

    /**
     * Constructs the matching Glyph pattern path for a given audio file path.
     * Assumes the pattern file has the same name with a .glyphring extension.
     * 
     * @param audioPath The path to the audio file.
     * @return The path to the Glyph pattern file, or null if not found.
     */
    public static String getGlyphPatternPath(String audioPath) {
        if (audioPath == null)
            return null;

        String basePath = audioPath;
        int lastDot = audioPath.lastIndexOf('.');
        if (lastDot > 0) {
            basePath = audioPath.substring(0, lastDot);
        }

        String glyphPath = basePath + ".glyphring";
        File glyphFile = new File(glyphPath);

        if (glyphFile.exists() && glyphFile.canRead()) {
            if (DEBUG)
                Log.d(TAG, "Found Glyph pattern file: " + glyphPath);
            return glyphPath;
        }

        if (DEBUG)
            Log.d(TAG, "No Glyph pattern found for: " + audioPath);
        return null;
    }

    /**
     * Validates if the parsed pattern contains valid data (duration, frames,
     * zones).
     * 
     * @param pattern The {@link GlyphPattern} to validate.
     * @return True if valid, false otherwise.
     */
    public static boolean isValid(GlyphPattern pattern) {
        if (pattern == null)
            return false;
        if (pattern.getFrames() == null || pattern.getFrames().isEmpty())
            return false;
        if (pattern.getDuration() <= 0)
            return false;

        for (GlyphPattern.GlyphFrame frame : pattern.getFrames()) {
            if (frame.getZones() == null || frame.getZones().length == 0)
                return false;
            if (frame.getBrightness() < 0 || frame.getBrightness() > 4095)
                return false;
            if (frame.getTimestamp() < 0)
                return false;
        }

        return true;
    }
}