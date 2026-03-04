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
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import co.aospa.glyph.Manager.AnimationManager;

import java.io.IOException;
import java.util.List;

/**
 * Handles synchronous playback of an audio file alongside a GlyphPattern,
 * translating frames into actual LED commands using AnimationManager.
 */
public class GlyphSyncPlayer {

    /** Log Tag. */
    private static final String TAG = "GlyphSyncPlayer";
    /** Debug flag. */
    private static final boolean DEBUG = true;

    /** Application context. */
    private Context mContext;
    /** The media player orchestrating audio playback. */
    private MediaPlayer mMediaPlayer;
    /**
     * Parsed glyph pattern configuration syncing LED triggers corresponding to
     * audio ticks.
     */
    private GlyphPattern mPattern;
    /** Primary sync thread executing frames on time. */
    private Handler mSyncHandler;
    /** Index keeping track of the current animation step. */
    private int mCurrentFrameIndex;
    /** Boolean tracking general activity. */
    private boolean mIsPlaying;
    /**
     * Unix timestamp mapping zero ticks avoiding async drifts during handler
     * execution.
     */
    private long mStartTime;

    /** Observer listening to execution ends. */
    private OnCompletionListener mCompletionListener;

    /**
     * Interface defining playback completion callback.
     */
    public interface OnCompletionListener {
        /** Triggered when the song naturally concludes its stream. */
        void onCompletion();
    }

    /**
     * Main constructor allocating context resources and thread loopers.
     * 
     * @param context Host application context.
     */
    public GlyphSyncPlayer(Context context) {
        mContext = context;
        mSyncHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Begins asynchronous preparation and playback of targeted audio files linking
     * parsed LED steps.
     * 
     * @param audioUri Targeted audio resource pointer.
     * @param pattern  Glyph configuration dictating visual representation.
     * @return True if initialized successfully, False on failures.
     */
    public boolean play(Uri audioUri, GlyphPattern pattern) {
        if (audioUri == null || pattern == null) {
            if (DEBUG)
                Log.e(TAG, "Invalid audio URI or pattern");
            return false;
        }

        if (!GlyphComposerParser.isValid(pattern)) {
            if (DEBUG)
                Log.e(TAG, "Invalid pattern data");
            return false;
        }

        stop();

        mPattern = pattern;
        mCurrentFrameIndex = 0;

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());

            mMediaPlayer.setDataSource(mContext, audioUri);
            mMediaPlayer.setOnPreparedListener(mp -> {
                if (DEBUG)
                    Log.d(TAG, "MediaPlayer prepared, starting playback");
                mp.start();
                mIsPlaying = true;
                mStartTime = System.currentTimeMillis();
                startGlyphSync();
            });

            mMediaPlayer.setOnCompletionListener(mp -> {
                if (DEBUG)
                    Log.d(TAG, "Playback completed");
                stop();
                if (mCompletionListener != null) {
                    mCompletionListener.onCompletion();
                }
            });

            mMediaPlayer.setOnErrorListener((mp, what, extra) -> {
                if (DEBUG)
                    Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                stop();
                return true;
            });

            mMediaPlayer.prepareAsync();
            return true;

        } catch (IOException e) {
            if (DEBUG)
                Log.e(TAG, "Error setting up MediaPlayer", e);
            return false;
        }
    }

    /**
     * Triggers classic audio feedback isolated from glyph functionality.
     * 
     * @param audioUri Local file pointing resource.
     * @return True on success.
     */
    public boolean playWithoutSync(Uri audioUri) {
        if (audioUri == null) {
            if (DEBUG)
                Log.e(TAG, "Invalid audio URI");
            return false;
        }

        stop();

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build());

            mMediaPlayer.setDataSource(mContext, audioUri);
            mMediaPlayer.setOnPreparedListener(mp -> {
                if (DEBUG)
                    Log.d(TAG, "MediaPlayer prepared (no sync)");
                mp.start();
                mIsPlaying = true;
            });

            mMediaPlayer.setOnCompletionListener(mp -> {
                if (DEBUG)
                    Log.d(TAG, "Playback completed (no sync)");
                stop();
                if (mCompletionListener != null) {
                    mCompletionListener.onCompletion();
                }
            });

            mMediaPlayer.prepareAsync();
            return true;

        } catch (IOException e) {
            if (DEBUG)
                Log.e(TAG, "Error setting up MediaPlayer", e);
            return false;
        }
    }

    /**
     * Fully deallocates player state stopping streams overriding animation controls
     * actively terminating.
     */
    public void stop() {
        mIsPlaying = false;

        if (mSyncHandler != null) {
            mSyncHandler.removeCallbacksAndMessages(null);
        }

        if (mMediaPlayer != null) {
            try {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
            } catch (IllegalStateException e) {
                if (DEBUG)
                    Log.e(TAG, "Error stopping MediaPlayer", e);
            }
            mMediaPlayer = null;
        }

        AnimationManager.stopAll();
        mPattern = null;
        mCurrentFrameIndex = 0;
    }

    /**
     * Invokes handler loops synchronizing visual threads triggering LED segments
     * relative to timestamps.
     */
    private void startGlyphSync() {
        if (mPattern == null || mPattern.getFrames() == null) {
            if (DEBUG)
                Log.e(TAG, "No pattern to sync");
            return;
        }

        if (DEBUG)
            Log.d(TAG, "Starting Glyph sync with " + mPattern.getFrames().size() + " frames");
        scheduleNextFrame();
    }

    /**
     * Loops iteratively calling internal handlers computing offset distances
     * queuing events accurately tracking latency.
     */
    private void scheduleNextFrame() {
        if (!mIsPlaying || mPattern == null) {
            return;
        }

        List<GlyphPattern.GlyphFrame> frames = mPattern.getFrames();
        if (mCurrentFrameIndex >= frames.size()) {
            if (DEBUG)
                Log.d(TAG, "All frames completed");
            return;
        }

        GlyphPattern.GlyphFrame frame = frames.get(mCurrentFrameIndex);
        long currentTime = System.currentTimeMillis() - mStartTime;
        long delay = frame.getTimestamp() - currentTime;

        if (delay < 0)
            delay = 0;

        if (DEBUG)
            Log.d(TAG, "Scheduling frame " + mCurrentFrameIndex + " at " + frame.getTimestamp() + "ms (delay: " + delay
                    + "ms)");

        mSyncHandler.postDelayed(() -> {
            if (mIsPlaying) {
                activateGlyphFrame(frame);
                mCurrentFrameIndex++;
                scheduleNextFrame();
            }
        }, delay);
    }

    /**
     * Sends isolated array segment brightness updates to centralized Animation
     * engine for hardware writing.
     * 
     * @param frame Processed struct containing mapping matrices.
     */
    private void activateGlyphFrame(GlyphPattern.GlyphFrame frame) {
        if (frame == null || frame.getZones() == null) {
            return;
        }

        if (DEBUG)
            Log.d(TAG, "Activating frame: zones=" + frame.getZones().length +
                    ", brightness=" + frame.getBrightness() +
                    ", duration=" + frame.getDuration() + "ms");

        int brightness = scaleBrightness(frame.getBrightness());

        for (int zone : frame.getZones()) {
            AnimationManager.singleLedBlink(mContext, zone, brightness, frame.getDuration());
        }
    }

    /**
     * Multiplies physical layout intensities scaling according to users general
     * settings UI config values.
     * 
     * @param patternBrightness Max integer range mapping pattern level.
     * @return Scaled fraction target.
     */
    private int scaleBrightness(int patternBrightness) {
        int userBrightness = co.aospa.glyph.Manager.SettingsManager.getGlyphBrightness();
        return (patternBrightness * userBrightness) / 100;
    }

    /**
     * Checks if music stream remains active externally.
     * 
     * @return True if playing.
     */
    public boolean isPlaying() {
        return mIsPlaying;
    }

    /**
     * Mutates listener hook firing on completion end sequences.
     * 
     * @param listener Interface implementation handling finish tasks.
     */
    public void setOnCompletionListener(OnCompletionListener listener) {
        mCompletionListener = listener;
    }
}