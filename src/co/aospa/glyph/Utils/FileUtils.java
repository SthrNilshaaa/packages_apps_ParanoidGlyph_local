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

package co.aospa.glyph.Utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import co.aospa.glyph.Constants.Constants;

/**
 * Utility class to read and write hardware node values for Glyph functionality.
 */
public final class FileUtils {

    /** Log Tag. */
    private static final String TAG = "GlyphFileUtils";
    /** Debug flag. */
    private static final boolean DEBUG = true;

    /**
     * Reads the first line of the specified file.
     * 
     * @param fileName Path to the file.
     * @return The first line read, or null if failed.
     */
    public static String readLine(String fileName) {
        String line = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName), 512);
            line = reader.readLine();
        } catch (FileNotFoundException e) {
            Log.w(TAG, "No such file " + fileName + " for reading", e);
        } catch (IOException e) {
            Log.e(TAG, "Could not read from file " + fileName, e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return line;
    }

    /**
     * Reads the specified file and parses its contents as an integer.
     * 
     * @param fileName Path to the file.
     * @return Int parsed from the file, or 0 if failed.
     */
    public static int readLineInt(String fileName) {
        String line = readLine(fileName);
        if (line == null) {
            return 0;
        }
        try {
            return Integer.parseInt(line.replace("0x", ""));
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not convert string to int from file " + fileName, e);
        }
        return 0;
    }

    /**
     * Writes a string value to the specified file, ensuring specific mode paths are
     * unlocked first.
     * 
     * @param fileName Path to target file.
     * @param value    Value to write.
     */
    public static void writeLine(String fileName, String value) {
        String modePath = ResourceUtils.getString("glyph_settings_paths_mode_absolute");
        BufferedWriter writerMode = null;
        BufferedWriter writerValue = null;
        try {
            if (!modePath.isBlank()) {
                writerMode = new BufferedWriter(new FileWriter(modePath));
                writerMode.write("1");
            }
            writerValue = new BufferedWriter(new FileWriter(fileName));
            writerValue.write(value);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "No such file " + fileName + " for writing", e);
        } catch (IOException e) {
            Log.e(TAG, "Could not write to file " + fileName, e);
        } finally {
            try {
                if (writerMode != null) {
                    writerMode.close();
                }
                if (writerValue != null) {
                    writerValue.close();
                }
            } catch (IOException e) {
                // Ignored, not much we can do anyway
            }
        }
    }

    /**
     * Helper to write integer values.
     * 
     * @param fileName Path to target file.
     * @param value    Integer value to write.
     */
    public static void writeLine(String fileName, int value) {
        writeLine(fileName, Integer.toString(value));
    }

    /**
     * Helper to write float values.
     * 
     * @param fileName Path to target file.
     * @param value    Float value to write.
     */
    public static void writeLine(String fileName, float value) {
        writeLine(fileName, Float.toString(value));
    }

    /**
     * Writes directly to the hardware node for ALL LEDs simultaneously.
     * 
     * @param value State string to send.
     */
    public static void writeAllLed(String value) {
        writeLine(ResourceUtils.getString("glyph_settings_paths_all_absolute"), value);
    }

    /**
     * Helper to write integer to ALL LEDs node.
     * 
     * @param value Integer intensity flag.
     */
    public static void writeAllLed(int value) {
        writeAllLed(Integer.toString(value));
    }

    /**
     * Helper to write float to ALL LEDs node.
     * 
     * @param value Float intensity parameter.
     */
    public static void writeAllLed(float value) {
        writeAllLed(Integer.toString(Math.round(value)));
    }

    /**
     * Writes frame/array list values mapped to multiple specific LED IDs.
     * 
     * @param value Encoded String array mapping multiple LEDs.
     */
    public static void writeFrameLed(String value) {
        writeLine(ResourceUtils.getString("glyph_settings_paths_frame_absolute"), value);
    }

    /**
     * Array-based frame LED helper converting int array to expected format.
     * 
     * @param value int array of led indices and states.
     */
    public static void writeFrameLed(int[] value) {
        writeFrameLed(Arrays.toString(value).replaceAll("\\[|\\]", "").replace(", ", " "));
    }

    /**
     * Array-based frame LED helper converting float array to expected format.
     * 
     * @param value float array of led intensities.
     */
    public static void writeFrameLed(float[] value) {
        int[] intValue = new int[value.length];
        for (int i = 0; i < value.length; i++) {
            intValue[i] = Math.round(value[i]);
        }
        writeFrameLed(intValue);
    }

    /**
     * Updates an individual specified LED node directly.
     * 
     * @param led   Individual LED string identifier.
     * @param value Brightness or toggle state.
     */
    public static void writeSingleLed(String led, String value) {
        writeLine(ResourceUtils.getString("glyph_settings_paths_single_absolute"), led + " " + value);
    }

    /**
     * Helper passing int LED identifier to string value update.
     * 
     * @param led   Int ID.
     * @param value String state.
     */
    public static void writeSingleLed(int led, String value) {
        writeSingleLed(Integer.toString(led), value);
    }

    /**
     * Helper passing string identifier and int value.
     * 
     * @param led   Ident.
     * @param value State.
     */
    public static void writeSingleLed(String led, int value) {
        writeSingleLed(led, Integer.toString(value));
    }

    /**
     * Helper passing string identifier and float value.
     * 
     * @param led   Ident.
     * @param value float state mapped to integer.
     */
    public static void writeSingleLed(String led, float value) {
        writeSingleLed(led, Integer.toString(Math.round(value)));
    }

    /**
     * Helper passing int identifier and float value.
     * 
     * @param led   Int ID.
     * @param value float.
     */
    public static void writeSingleLed(int led, float value) {
        writeSingleLed(Integer.toString(led), Integer.toString(Math.round(value)));
    }
}
