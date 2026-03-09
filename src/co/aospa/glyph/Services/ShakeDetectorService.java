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
package co.aospa.glyph.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Looper;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import co.aospa.glyph.Constants.Constants;
import co.aospa.glyph.Manager.GlyphScheduleManager;
import co.aospa.glyph.Manager.SettingsManager;
import co.aospa.glyph.Manager.StatusManager;
import co.aospa.glyph.Utils.FileUtils;
import co.aospa.glyph.Utils.ResourceUtils;

public class ShakeDetectorService extends Service implements SensorEventListener {

    private static final String TAG = "GlyphShakeDetector";
    private static final boolean DEBUG = false;

    private static final int SHAKE_TIME_WINDOW = 500;
    private static final int SHAKE_COUNT_THRESHOLD = 2;

    private static final int DEFAULT_SENSITIVITY = 35;
    private static final int DEFAULT_SHAKE_COUNT = 2;
    private static final int DEFAULT_HAPTIC_INTENSITY = 50;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mProximitySensor;
    private SharedPreferences mSharedPrefs;
    private PowerManager.WakeLock mWakeLock;
    private PowerManager mPowerManager;
    private Vibrator mVibrator;

    private boolean mProximityBlocked = false;

    // --- PRO SHAKE DETECTION VARIABLES ---
    private long firstShakeSequenceTime = 0;
    private float[] gravity = new float[3];
    private static final int MIN_TIME_BETWEEN_SHAKES_MS = 350;
    private static final int MAX_TIME_BETWEEN_SHAKES_MS = 1500;

    private long mLastShakeTime = 0;
    private int mShakeCount = 0;
    private float mCurrentThreshold = DEFAULT_SENSITIVITY;
    private int mRequiredShakes = DEFAULT_SHAKE_COUNT;
    private int mHapticIntensity = DEFAULT_HAPTIC_INTENSITY;

    @Override
    public void onCreate() {
        super.onCreate();

        if (DEBUG)
            Log.d(TAG, "Service created");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG + ":ShakeWakeLock");

        loadSettings();

        registerSensorListener();

        if (mProximitySensor != null) {
            // Check initial proximity state if possible
            // Note: Proximity sensor usually needs a change event,
            // but some devices support an initial value.
            // We'll rely on the first event normally, but this ensures
            // the listener is ready.
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG)
            Log.d(TAG, "Service started");

        loadSettings();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void loadSettings() {
        // Range provided by user: 2.5f -> 8.0f (Mapped from 0-100)
        int progress = SettingsManager.getGlyphShakeSensitivity();
        float minThreshold = 2.5f;
        float maxThreshold = 8.0f;
        float range = maxThreshold - minThreshold;
        mCurrentThreshold = maxThreshold - ((progress / 100f) * range);

        mRequiredShakes = SettingsManager.getGlyphShakeCount();
        mHapticIntensity = SettingsManager.getGlyphShakeHapticIntensity();

        if (DEBUG)
            Log.d(TAG, "Shake settings loaded - sensitivity: " + mCurrentThreshold +
                    ", required count: " + mRequiredShakes +
                    ", haptic intensity: " + mHapticIntensity);
    }

    private void registerSensorListener() {
        if (mAccelerometer != null && isShakeEnabled() && SettingsManager.isGlyphEnabled()) {
            mSensorManager.registerListener(this, mAccelerometer,
                    SensorManager.SENSOR_DELAY_GAME);
            if (mProximitySensor != null) {
                mSensorManager.registerListener(this, mProximitySensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
            if (DEBUG)
                Log.d(TAG, "Accelerometer and proximity listeners registered with threshold: " + mCurrentThreshold);
        } else {
            if (DEBUG)
                Log.d(TAG, "Shake disabled or Glyph disabled, listener not registered");
        }
    }

    private void unregisterSensorListener() {
        mSensorManager.unregisterListener(this);
        if (DEBUG)
            Log.d(TAG, "Accelerometer listener unregistered");
    }

    private boolean isShakeEnabled() {
        return SettingsManager.isGlyphShakeEnabled();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            mProximityBlocked = event.values[0] < mProximitySensor.getMaximumRange();
            if (DEBUG)
                Log.d(TAG, "Proximity blocked: " + mProximityBlocked);
            return;
        }

        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        // Check if Shake is enabled and Glyph is generally enabled
        if (!isShakeEnabled() || !SettingsManager.isGlyphEnabledIgnoreSchedule()) {
            return;
        }

        // Check if Schedule (Sleep Mode) is active and block if exception is not allowed
        if (GlyphScheduleManager.isScheduleCurrentlyActive(this) && 
            !SettingsManager.isGlyphShakeAllowInSleepEnabled()) {
            return;
        }

        if (mPowerManager != null && mPowerManager.isInteractive()
                && !SettingsManager.isGlyphShakeWhileScreenOnEnabled()) {
            return;
        }

        if (mProximityBlocked) return;

        final float alpha = 0.8f;
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        float x = event.values[0] - gravity[0];
        float y = event.values[1] - gravity[1];
        float z = event.values[2] - gravity[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        float pureMovementThreshold = mCurrentThreshold - 1.0f;
        if (pureMovementThreshold < 0.3f)
            pureMovementThreshold = 0.3f;

        if (gForce > pureMovementThreshold) {
            long now = SystemClock.elapsedRealtime();

            if (now - mLastShakeTime < MIN_TIME_BETWEEN_SHAKES_MS) {
                return;
            }

            if (mShakeCount == 0 || now - mLastShakeTime > MAX_TIME_BETWEEN_SHAKES_MS) {
                mShakeCount = 0;
                firstShakeSequenceTime = now;
            }

            if (now - firstShakeSequenceTime > 2000) {
                mShakeCount = 0;
                firstShakeSequenceTime = now;
            }

            mLastShakeTime = now;
            mShakeCount++;

            if (DEBUG)
                Log.d(TAG, "Shake detected, count: " + mShakeCount + ", accel: " + gForce);

            if (mShakeCount >= mRequiredShakes) {
                onShakeDetected();
                mShakeCount = 0;
                firstShakeSequenceTime = 0;
                gravity = new float[3]; // Reset filter
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void onShakeDetected() {
        if (mProximityBlocked) {
            if (DEBUG)
                Log.d(TAG, "Proximity blocked, ignoring shake");
            return;
        }

        if (DEBUG)
            Log.d(TAG, "Shake gesture triggered, toggling torch");

        if (!SettingsManager.isGlyphEnabledIgnoreSchedule()) {
            if (DEBUG)
                Log.d(TAG, "Glyph completely disabled, ignoring shake");
            return;
        }

        performHapticFeedback();

        if (mWakeLock != null && !mWakeLock.isHeld()) {
            mWakeLock.acquire(3000);
        }

        try {
            boolean currentState = StatusManager.isAllLedActive();
            boolean newState = !currentState;

            StatusManager.setAllLedsActive(newState);
            FileUtils.writeAllLed(newState ? Constants.getMaxBrightness() : 0);

            if (StatusManager.isEssentialLedActive() && !newState) {
                FileUtils.writeSingleLed(
                        ResourceUtils.getInteger("glyph_settings_notifs_essential_led"),
                        Constants.getMaxBrightness() / 100 * 7);
            }

            showToastNotification(newState);

            if (DEBUG)
                Log.d(TAG, "Torch toggled to: " + (newState ? "ON" : "OFF"));

        } catch (Exception e) {
            Log.e(TAG, "Error toggling torch", e);
        } finally {
            if (mWakeLock != null && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
    }

    private void performHapticFeedback() {
        if (mHapticIntensity == 0) {
            if (DEBUG)
                Log.d(TAG, "Haptic feedback disabled by intensity 0");
            return;
        }

        if (mVibrator != null && mVibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int amplitude = (int) (1 + (mHapticIntensity / 100.0) * 254);
                mVibrator.vibrate(VibrationEffect.createOneShot(50, amplitude));
            } else {
                long duration = (long) (10 + (mHapticIntensity / 100.0) * 90);
                mVibrator.vibrate(duration);
            }
            if (DEBUG)
                Log.d(TAG, "Haptic feedback triggered with intensity " + mHapticIntensity);
        }
    }

    private void showToastNotification(boolean torchOn) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            String message = torchOn ? "Glyph Torch ON" : "Glyph Torch OFF";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            if (DEBUG)
                Log.d(TAG, "Toast shown: " + message);
        });
    }

    @Override
    public void onDestroy() {
        if (DEBUG)
            Log.d(TAG, "Service destroyed");

        unregisterSensorListener();

        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        super.onDestroy();
    }
}