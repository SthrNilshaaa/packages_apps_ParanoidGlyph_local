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

package co.aospa.glyph.Sensors;

import android.annotation.NonNull;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * SensorEventListener designed to detect when the device is flipped face-down
 * to activate the "Flip to Glyph" DND/Glyph mode.
 */
public class FlipToGlyphSensor implements SensorEventListener {

    /** Debug flag. */
    private static final boolean DEBUG = true;
    /** Log tag. */
    private static final String TAG = "FlipToGlyphSensor";

    /** Indicates whether the device is currently in a flipped (face-down) state. */
    private boolean isFlipped = false;
    /** Callback to execute when flip state changes. */
    private final Consumer<Boolean> mOnFlip;

    /** Manager to register and watch sensor events. */
    private SensorManager mSensorManager;
    /** Accelerometer sensor used to calculate orientation. */
    private Sensor mSensorAccelerometer;
    /** Application/Service Context. */
    private Context mContext;

    /**
     * Duration threshold mapping to how long device has to remain face-down to
     * trigger.
     */
    private Duration mTimeThreshold = Duration.ofMillis(1_000L);;
    /** Movement threshold on XY planes to ignore small vibrations. */
    private float mAccelerationThreshold = 0.2f;
    /** Gravity validation threshold on Z axis (pointing down). */
    private float mZAccelerationThreshold = -9.5f;
    /** Lenient Z axis threshold for exiting the flipped state. */
    private float mZAccelerationThresholdLenient = mZAccelerationThreshold + 1.0f;
    /** Previous iteration's absolute XY acceleration. */
    private float mPrevAcceleration = 0;
    /** Timestamp of previous significant XY movement. */
    private long mPrevAccelerationTime = 0;
    /** State flag whether the Z axis matches face-down parameters. */
    private boolean mZAccelerationIsFaceDown = false;
    /** Timestamp when Z acceleration started signaling face-down. */
    private long mZAccelerationFaceDownTime = 0L;

    /** Smoothing weight for moving average. */
    private static final float MOVING_AVERAGE_WEIGHT = 0.5f;
    /** Exponential moving average processor for XY plane. */
    private final ExponentialMovingAverage mCurrentXYAcceleration = new ExponentialMovingAverage(MOVING_AVERAGE_WEIGHT);
    /** Exponential moving average processor for Z axis. */
    private final ExponentialMovingAverage mCurrentZAcceleration = new ExponentialMovingAverage(MOVING_AVERAGE_WEIGHT);

    /**
     * Constructor for FlipToGlyphSensor.
     *
     * @param context Application context
     * @param onFlip  Consumer callback to be triggered upon flip detection.
     */
    public FlipToGlyphSensor(Context context, @NonNull Consumer<Boolean> onFlip) {
        mContext = context;
        mOnFlip = Objects.requireNonNull(onFlip);
        mSensorManager = mContext.getSystemService(SensorManager.class);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER, false);
    }

    /**
     * Called when sensor values have changed. Evaluates the accelerometer data
     * to detect stable face-down orientation.
     *
     * @param event The triggered sensor event.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;

        final float x = event.values[0];
        final float y = event.values[1];
        mCurrentXYAcceleration.updateMovingAverage(x * x + y * y);
        mCurrentZAcceleration.updateMovingAverage(event.values[2]);

        final long curTime = event.timestamp;
        if (Math.abs(mCurrentXYAcceleration.mMovingAverage - mPrevAcceleration) > mAccelerationThreshold) {
            mPrevAcceleration = mCurrentXYAcceleration.mMovingAverage;
            mPrevAccelerationTime = curTime;
        }
        final boolean moving = curTime - mPrevAccelerationTime <= mTimeThreshold.toNanos();

        final float zAccelerationThreshold = isFlipped ? mZAccelerationThresholdLenient : mZAccelerationThreshold;
        final boolean isCurrentlyFaceDown = mCurrentZAcceleration.mMovingAverage < zAccelerationThreshold;
        final boolean isFaceDownForPeriod = isCurrentlyFaceDown
                && mZAccelerationIsFaceDown
                && curTime - mZAccelerationFaceDownTime > mTimeThreshold.toNanos();
        if (isCurrentlyFaceDown && !mZAccelerationIsFaceDown) {
            mZAccelerationFaceDownTime = curTime;
            mZAccelerationIsFaceDown = true;
        } else if (!isCurrentlyFaceDown) {
            mZAccelerationIsFaceDown = false;
        }

        if (!moving && isFaceDownForPeriod && !isFlipped) {
            onFlip(true);
        } else if (!isFaceDownForPeriod && isFlipped) {
            onFlip(false);
        }
    }

    /**
     * Called when the accuracy of the registered sensor has changed.
     *
     * @param sensor   The sensor whose accuracy changed.
     * @param accuracy The new accuracy.
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Triggers the stored consumer callback regarding the new flip state.
     *
     * @param flipped True if flipped face-down, otherwise false.
     */
    private void onFlip(boolean flipped) {
        if (DEBUG)
            Log.d(TAG, "Flipped: " + flipped);
        mOnFlip.accept(flipped);
        isFlipped = flipped;
    }

    /**
     * Starts listening to accelerometer events.
     */
    public void enable() {
        if (DEBUG)
            Log.d(TAG, "Enabling Sensor");
        mSensorManager.registerListener(this, mSensorAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                mContext.getResources().getInteger(
                        com.android.internal.R.integer.config_flipToScreenOffMaxLatencyMicros));
    }

    /**
     * Stops listening to accelerometer events and resets internal flip state.
     */
    public void disable() {
        if (DEBUG)
            Log.d(TAG, "Disabling Sensor");
        onFlip(false);
        mSensorManager.unregisterListener(this, mSensorAccelerometer);
    }

    /**
     * Helper class to compute an exponential moving average for sensor smoothing.
     */
    private final class ExponentialMovingAverage {
        /** The alpha smoothing factor. */
        private final float mAlpha;
        /** Initial average fallback value. */
        private final float mInitialAverage;
        /** The current mathematically smoothed average. */
        private float mMovingAverage;

        /**
         * Initialize moving average with standard alpha.
         * 
         * @param alpha Smoothing factor ranging 0..1
         */
        ExponentialMovingAverage(float alpha) {
            this(alpha, 0.0f);
        }

        /**
         * Initialize moving average with alpha and an initial seed value.
         * 
         * @param alpha          Smoothing factor
         * @param initialAverage Initial starting average
         */
        ExponentialMovingAverage(float alpha, float initialAverage) {
            this.mAlpha = alpha;
            this.mInitialAverage = initialAverage;
            this.mMovingAverage = initialAverage;
        }

        /**
         * Provide a new sensor value to update the moving average.
         * 
         * @param newValue Freshly sampled data point
         */
        void updateMovingAverage(float newValue) {
            mMovingAverage = newValue + mAlpha * (mMovingAverage - newValue);
        }

        /**
         * Reset the moving average to the initial seed value.
         */
        void reset() {
            mMovingAverage = this.mInitialAverage;
        }
    }
}