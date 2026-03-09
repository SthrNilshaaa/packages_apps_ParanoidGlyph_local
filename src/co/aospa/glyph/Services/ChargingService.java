/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2018 The LineageOS Project
 *               2020-2024 Paranoid Android
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import co.aospa.glyph.Manager.AnimationManager;
import co.aospa.glyph.Manager.SettingsManager;
import co.aospa.glyph.Manager.StatusManager;

public class ChargingService extends Service {

    private static final String TAG = "GlyphChargingService";
    private static final boolean DEBUG = true;

    private HandlerThread thread;
    private Handler mThreadHandler;

    private BatteryManager mBatteryManager;
    private SensorManager mSensorManager;

    private PowerManager mPowerManager;

    private Sensor mAccelerometerSensor;
    private Sensor mProximitySensor;
    
    private boolean mIsProximityCovered;
    private boolean mIsFaceDown;
    private float mLastX, mLastY, mLastZ;
    private boolean mHasLastAccel;
    
    private long mLastAnimationTime = 0;
    private static final long DEBOUNCE_INTERVAL_MS = 3000;
    private static final float ZFACEDOWN_THRESHOLD = -8.0f;

    private Runnable dismissCharging = new Runnable() {
        @Override
        public void run() {
            AnimationManager.dismissCharging(ChargingService.this);
        }
    };

    @Override
    public void onCreate() {
        if (DEBUG)
            Log.d(TAG, "Creating service");

        // Add a handler thread
        thread = new HandlerThread("ChargingService");
        thread.start();
        Looper looper = thread.getLooper();
        mThreadHandler = new Handler(looper);

        mBatteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        IntentFilter powerMonitor = new IntentFilter();
        powerMonitor.addAction(Intent.ACTION_POWER_CONNECTED);
        powerMonitor.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(mPowerMonitor, powerMonitor);

        Intent intent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        if (isCharging) {
            onPowerConnected();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG)
            Log.d(TAG, "Starting service");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG)
            Log.d(TAG, "Destroying service");
        this.unregisterReceiver(mPowerMonitor);
        onPowerDisconnected();
        thread.quit();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int getBatteryLevel() {
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    private void onPowerConnected() {
        if (DEBUG)
            Log.d(TAG, "Power connected");
        if (DEBUG)
            Log.d(TAG, "Battery level: " + getBatteryLevel());
        playChargingAnimation(true);
        mSensorManager.registerListener(mSensorEventListener,
                mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (mProximitySensor != null) {
            mSensorManager.registerListener(mSensorEventListener,
                    mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void onPowerDisconnected() {
        if (DEBUG)
            Log.d(TAG, "Power disconnected");
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    private void playChargingAnimation(boolean wait) {
        if (mThreadHandler.hasCallbacks(dismissCharging))
            mThreadHandler.removeCallbacks(dismissCharging);
        mThreadHandler.post(() -> {
            if (SettingsManager.isGlyphIndicatorsFlipOnly() || SettingsManager.isGlyphChargingFlipOnly()) {
                if (!StatusManager.isFlipped()) {
                    if (DEBUG)
                        Log.d(TAG, "Indicators restricted to flip-only and device not flipped, ignoring");
                    return;
                }
            }
            AnimationManager.playCharging(ChargingService.this, getBatteryLevel(), wait);
        });
        mThreadHandler.postDelayed(dismissCharging, 1190);
    }

    private final BroadcastReceiver mPowerMonitor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                onPowerConnected();
            } else if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED)) {
                onPowerDisconnected();
            }
        }
    };

    private final SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                
                mIsFaceDown = z < ZFACEDOWN_THRESHOLD;

                if (mHasLastAccel) {
                    float deltaX = Math.abs(x - mLastX);
                    float deltaY = Math.abs(y - mLastY);
                    float deltaZ = Math.abs(z - mLastZ);
                    float totalDelta = deltaX + deltaY + deltaZ;

                    if (totalDelta > 0.15f && mIsFaceDown && mIsProximityCovered && mPowerManager != null && !mPowerManager.isInteractive()) {
                        long now = System.currentTimeMillis();
                        if (now - mLastAnimationTime > DEBOUNCE_INTERVAL_MS) {
                            mLastAnimationTime = now;
                            playChargingAnimation(false);
                        }
                    }
                }

                mLastX = x;
                mLastY = y;
                mLastZ = z;
                mHasLastAccel = true;
            } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                mIsProximityCovered = event.values[0] < event.sensor.getMaximumRange();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
}
