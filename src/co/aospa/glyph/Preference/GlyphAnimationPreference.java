/*
 * Copyright (C) 2023-2024 Paranoid Android
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

package co.aospa.glyph.Preference;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.FrameLayout;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import co.aospa.glyph.R;
import co.aospa.glyph.Constants.Constants;
import co.aospa.glyph.Utils.ResourceUtils;

/**
 * Custom Preference class which handles rendering and displaying interactive
 * Glyph animations
 * within the settings dashboard.
 */
public class GlyphAnimationPreference extends Preference {

    /** Log tag. */
    private final String TAG = "GlyphAnimationPreference";
    /** Debug flag. */
    private final boolean DEBUG = true;

    /** Reference to the host Activity. */
    private Activity mActivity;

    /** Internal name corresponding to the specific animation pattern. */
    private String animationName;
    /** Flag to completely stop the animation thread loop. */
    private boolean animationTerminated;
    /** Flag to pause/resume in-progress animation. */
    private boolean animationPaused = true;
    /** Time spacing (in ms) to delay between looping occurrences. */
    private int animationTimeBetween = 0;
    /** String slugs mapping to the device's Glyph LED components. */
    private String[] animationSlugs;
    /** ImageView arrays corresponding to individual physical Glyph LEDs. */
    private ImageView[] animationImgs;

    /** Root view layout for the inflated preference. */
    private View mRootView;
    /** Standard click listener to pass events up. */
    private final View.OnClickListener mClickListener = v -> performClick(v);

    /**
     * Simple constructor initializing the layout.
     * 
     * @param context Host context.
     */
    public GlyphAnimationPreference(Context context) {
        super(context);
        setActivity(context);
        setLayout(R.layout.glyph_settings_preview);
    }

    /**
     * Standard constructor initializing the layout.
     * 
     * @param context Host context.
     * @param attrs   Attribute sets.
     */
    public GlyphAnimationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setActivity(context);
        setLayout(R.layout.glyph_settings_preview);
    }

    /**
     * Advanced constructor initializing the layout.
     * 
     * @param context      Host context.
     * @param attrs        Attribute sets.
     * @param defStyleAttr Default attribute styles.
     */
    public GlyphAnimationPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setActivity(context);
        setLayout(R.layout.glyph_settings_preview);
    }

    /**
     * Advanced constructor initializing custom layout resource.
     * 
     * @param context      Host context.
     * @param attrs        Attribute sets.
     * @param defStyleAttr Default attribute styles.
     * @param defStyleRes  Custom layout resource ID.
     */
    public GlyphAnimationPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        setActivity(context);
        setLayout(defStyleRes);
    }

    /**
     * Inflates the custom layout view hierarchy.
     * 
     * @param layoutResource layout ID.
     */
    private void setLayout(int layoutResource) {
        setLayoutResource(R.layout.glyph_settings_preview_frame);
        mRootView = LayoutInflater.from(getContext())
                .inflate(layoutResource, null, false);
        setShouldDisableView(false);
    }

    /**
     * Retrieves and caches the host Activity from the provided context wrapper.
     * 
     * @param context Context base.
     */
    private void setActivity(Context context) {
        if (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                mActivity = (Activity) context;
            } else {
                setActivity(((ContextWrapper) context).getBaseContext());
            }
        }
    }

    /**
     * Links custom layout with the preference representation hierarchy.
     * 
     * @param holder View holder maintaining the view bindings.
     */
    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        holder.itemView.setOnClickListener(mClickListener);

        final boolean selectable = isSelectable();
        holder.itemView.setFocusable(isSelectable());
        holder.itemView.setClickable(isSelectable());

        FrameLayout layout = (FrameLayout) holder.itemView;
        layout.removeAllViews();
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        layout.addView(mRootView);
    }

    /**
     * Triggered when the preference is attached to the visible fragment view.
     * It initiates the underlying background thread to begin rendering.
     */
    @Override
    public void onAttached() {
        super.onAttached();
        if (DEBUG)
            Log.d(TAG, "onAttached");
        startAnimation();
    }

    /**
     * Triggered when the preference is cleared from viewing.
     * Shuts down internal animation thread explicitly to avoid leaks.
     */
    @Override
    public void onDetached() {
        super.onDetached();
        if (DEBUG)
            Log.d(TAG, "onDetached");
        stopAnimation();
    }

    /**
     * Loads the target identifiers, initializes the ImageView array,
     * and spawns the rendering thread.
     */
    private void startAnimation() {
        animationSlugs = ResourceUtils.getStringArray("glyph_settings_animations_slugs");
        animationImgs = new ImageView[animationSlugs.length];
        for (int i = 0; i < animationSlugs.length; i++) {
            animationImgs[i] = (ImageView) mRootView.findViewById(
                    ResourceUtils.getIdentifier("preview_device_" + animationSlugs[i], "id"));
        }
        animationThread.start();
    }

    /**
     * Requests thread termination gracefully.
     */
    private void stopAnimation() {
        animationTerminated = true;
        animationThread.interrupt();
    }

    /**
     * Simplest overload: pauses or unpauses current loaded animation file.
     * 
     * @param play True to resume playback, false to stop rendering.
     */
    public void updateAnimation(boolean play) {
        updateAnimation(play, animationName, 0);
    }

    /**
     * Changes current animation file name and playback state.
     * 
     * @param play True to load and run it, false to reset.
     * @param name Key defining animation.
     */
    public void updateAnimation(boolean play, String name) {
        updateAnimation(play, name, 0);
    }

    /**
     * Modifies current animation, playback state, and changes intra-looping timing
     * threshold.
     * 
     * @param play True to operate rendering thread, false sets neutral visual blank
     *             slate.
     * @param name Identifier mapping sequence file.
     * @param time Break duration resting between loops.
     */
    public void updateAnimation(boolean play, String name, int time) {
        animationTimeBetween = time;
        animationName = name;
        animationPaused = !play;
        animationThread.interrupt();
    }

    /**
     * Internal background thread processing line-by-line frame execution of
     * brightness adjustments.
     */
    Thread animationThread = new Thread() {
        /** Core loop blocking onto parsed CSV data logic. */
        @Override
        public void run() {
            while (!animationTerminated) {
                while (animationPaused) {
                }
                if (DEBUG)
                    Log.d(TAG, "Displaying animation | name: " + animationName);
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                        ResourceUtils.getAnimation(animationName)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.replace(" ", "");
                        line = line.endsWith(",") ? line.substring(0, line.length() - 1) : line;
                        String[] split = line.split(",");
                        if (Constants.getDevice().equals("phone1") && split.length == 5) { // Phone (1) pattern on Phone
                                                                                           // (1)
                            mActivity.runOnUiThread(() -> {
                                for (int i = 0; i < animationSlugs.length; i++) {
                                    setGlyphsDrawable(animationImgs[i], Integer.parseInt(split[i]));
                                }
                            });
                        } else if (Constants.getDevice().equals("phone2") && split.length == 5) { // Phone (1) pattern
                                                                                                  // on Phone (2)
                            mActivity.runOnUiThread(() -> {
                                setGlyphsDrawable(animationImgs[0], Integer.parseInt(split[0]));
                                setGlyphsDrawable(animationImgs[1], Integer.parseInt(split[0]));
                                setGlyphsDrawable(animationImgs[2], Integer.parseInt(split[1]));
                                setGlyphsDrawable(animationImgs[3], Integer.parseInt(split[2]));
                                setGlyphsDrawable(animationImgs[4], Integer.parseInt(split[2]));
                                setGlyphsDrawable(animationImgs[5], Integer.parseInt(split[2]));
                                setGlyphsDrawable(animationImgs[6], Integer.parseInt(split[2]));
                                setGlyphsDrawable(animationImgs[7], Integer.parseInt(split[2]));
                                setGlyphsDrawable(animationImgs[8], Integer.parseInt(split[2]));
                                setGlyphsDrawable(animationImgs[9], Integer.parseInt(split[3]));
                                setGlyphsDrawable(animationImgs[10], Integer.parseInt(split[4]));
                            });
                        } else if (Constants.getDevice().equals("phone2") && split.length == 33) { // Phone (2) pattern
                                                                                                   // on Phone (2)
                            mActivity.runOnUiThread(() -> {
                                setGlyphsDrawable(animationImgs[0], Integer.parseInt(split[0]));
                                setGlyphsDrawable(animationImgs[1], Integer.parseInt(split[1]));
                                setGlyphsDrawable(animationImgs[2], Integer.parseInt(split[2]));
                                setGlyphsDrawable(animationImgs[3], Integer.parseInt(split[3]));
                                setGlyphsDrawable(animationImgs[4], Integer.parseInt(split[19]));
                                setGlyphsDrawable(animationImgs[5], Integer.parseInt(split[20]));
                                setGlyphsDrawable(animationImgs[6], Integer.parseInt(split[21]));
                                setGlyphsDrawable(animationImgs[7], Integer.parseInt(split[22]));
                                setGlyphsDrawable(animationImgs[8], Integer.parseInt(split[23]));
                                setGlyphsDrawable(animationImgs[9], Integer.parseInt(split[25]));
                                setGlyphsDrawable(animationImgs[10], Integer.parseInt(split[24]));
                            });
                        } else {
                            if (DEBUG)
                                Log.d(TAG,
                                        "Animation line length mismatch | name: " + animationName + " | line: " + line);
                            updateAnimation(false);
                        }
                        Thread.sleep(16, 666000);
                    }
                    Thread.sleep(animationTimeBetween);
                } catch (Exception e) {
                    if (DEBUG)
                        Log.d(TAG,
                                "Exception while displaying animation | name: " + animationName + " | exception: " + e);
                } finally {
                    if (animationPaused) {
                        if (DEBUG)
                            Log.d(TAG, "Pause displaying animation | name: " + animationName);
                        mActivity.runOnUiThread(() -> {
                            for (int i = 0; i < animationSlugs.length; i++) {
                                setGlyphsDrawable(animationImgs[i], 0);
                            }
                        });
                    }
                }
            }
        }

        /**
         * Modifies brightness of UI element proportional to the passed brightness
         * limits.
         * 
         * @param imageView  The UI view to modify.
         * @param brightness Raw int parsing map ranging up to maximal allowed bounds.
         */
        private void setGlyphsDrawable(ImageView imageView, int brightness) {
            if (brightness <= 0) {
                imageView.setAlpha(0.3f);
            } else {
                float brightnessFactor = (float) (0.4 + 0.6 * (brightness / (double) Constants.getMaxBrightness()));
                imageView.setAlpha(brightnessFactor);
            }
        }
    };
}
