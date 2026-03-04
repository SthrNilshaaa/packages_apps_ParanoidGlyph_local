package co.aospa.glyph.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.app.AlertDialog;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.Slider;

import co.aospa.glyph.R;
import co.aospa.glyph.Constants.Constants;
import co.aospa.glyph.Manager.GlyphManager;
import co.aospa.glyph.Manager.SettingsManager;
import co.aospa.glyph.Utils.CustomRingtoneManager;
import co.aospa.glyph.Utils.GlyphEffects;
import co.aospa.glyph.Utils.ServiceUtils;
import co.aospa.glyph.Manager.ShakeManager;
import co.aospa.glyph.Settings.Adapters.StyleAdapter;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment implements StyleAdapter.SharedPreferencesProvider {

    private MaterialSwitch switchMaster, switchFlip, switchScreenOff, switchAssistantMic,
            switchBatteryBar, switchVolumeBar, switchRingNotifHaptics, switchVolumeFlipOnly,
            switchShake, switchMusicVisualizer, switchProgress, switchProgressMusic,
            switchIndicatorsFlipOnly, switchProgressFlipOnly, switchMusicVisualizerFlipOnly,
            switchChargingFlipOnly;
    private Slider sliderBrightness, sliderShakeSensitivity, sliderShakeHapticStrength, sliderRingNotifHapticStrength;
    private MaterialCardView cardRingtones, cardNotifications, cardFlipStyle, cardEssentialLights, cardImport,
            cardShakeToGlyph;
    private TextView textCurrentCallStyle, textCurrentNotifStyle, textCurrentFlipStyle, textImportWarning;
    private LinearLayout layoutRingNotifHapticStrength, layoutVolumeFlipOnly, layoutShakeSettings,
            layoutMusicVisualizerFlipOnly, layoutProgressMusic, layoutProgressFlipOnly,
            layoutChargingFlipOnly;
    private RadioGroup rgShakeCount;
    private ImageView spacewar;
    private RecyclerView rvCallStyles, rvNotifStyles;

    private Vibrator vibrator;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable previewRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_glyph_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        vibrator = getContext() != null ? getContext().getSystemService(Vibrator.class) : null;
        initViews(view);
        setupInitialState();
        setupListeners();
    }

    private void initViews(View view) {
        switchMaster = view.findViewById(R.id.switchAll);
        sliderBrightness = view.findViewById(R.id.sliderMain);
        spacewar = view.findViewById(R.id.spacewar);

        cardRingtones = view.findViewById(R.id.cardRingtones);
        cardNotifications = view.findViewById(R.id.cardNotifications);
        textCurrentCallStyle = view.findViewById(R.id.textCurrentCallStyle);
        textCurrentNotifStyle = view.findViewById(R.id.textCurrentNotifStyle);

        cardImport = view.findViewById(R.id.cardImport);
        textImportWarning = view.findViewById(R.id.textImportWarning);

        switchRingNotifHaptics = view.findViewById(R.id.switchRingNotifHaptics);
        layoutRingNotifHapticStrength = view.findViewById(R.id.layoutRingNotifHapticStrength);
        sliderRingNotifHapticStrength = view.findViewById(R.id.sliderRingNotifHapticStrength);

        switchFlip = view.findViewById(R.id.switchFlip);
        textCurrentFlipStyle = view.findViewById(R.id.textCurrentFlipStyle);
        cardFlipStyle = view.findViewById(R.id.cardFlipStyle);

        cardEssentialLights = view.findViewById(R.id.cardEssentialLights);
        switchScreenOff = view.findViewById(R.id.switchScreenOff);

        cardShakeToGlyph = view.findViewById(R.id.cardShakeToGlyph);
        switchShake = view.findViewById(R.id.switchShake);
        layoutShakeSettings = view.findViewById(R.id.layoutShakeSettings);
        sliderShakeSensitivity = view.findViewById(R.id.seekBar_sensitivity);
        sliderShakeHapticStrength = view.findViewById(R.id.seekBar_haptic_strength);
        rgShakeCount = view.findViewById(R.id.rg_shake_count);

        switchAssistantMic = view.findViewById(R.id.switchAssistantMic);
        switchBatteryBar = view.findViewById(R.id.switchBatteryBar);
        layoutChargingFlipOnly = view.findViewById(R.id.layoutChargingFlipOnly);
        switchChargingFlipOnly = view.findViewById(R.id.switchChargingFlipOnly);
        switchVolumeBar = view.findViewById(R.id.switchVolumeBar);
        layoutVolumeFlipOnly = view.findViewById(R.id.layoutVolumeFlipOnly);
        switchVolumeFlipOnly = view.findViewById(R.id.switchVolumeFlipOnly);

        switchMusicVisualizer = view.findViewById(R.id.switchMusicVisualizer);
        layoutMusicVisualizerFlipOnly = view.findViewById(R.id.layoutMusicVisualizerFlipOnly);
        switchMusicVisualizerFlipOnly = view.findViewById(R.id.switchMusicVisualizerFlipOnly);

        switchProgress = view.findViewById(R.id.switchProgress);
        layoutProgressMusic = view.findViewById(R.id.layoutProgressMusic);
        switchProgressMusic = view.findViewById(R.id.switchProgressMusic);
        layoutProgressFlipOnly = view.findViewById(R.id.layoutProgressFlipOnly);
        switchProgressFlipOnly = view.findViewById(R.id.switchProgressFlipOnly);

        switchIndicatorsFlipOnly = view.findViewById(R.id.switchIndicatorsFlipOnly);

        rvCallStyles = view.findViewById(R.id.rvCallStyles);
        rvNotifStyles = view.findViewById(R.id.rvNotifStyles);
    }

    private void setupInitialState() {
        boolean enabled = SettingsManager.isGlyphEnabledIgnoreSchedule();
        switchMaster.setChecked(enabled);

        sliderBrightness.setValue(SettingsManager.getGlyphBrightnessSetting());
        updateOutlineAlpha(sliderBrightness.getValue());

        switchRingNotifHaptics.setChecked(SettingsManager.isGlyphNotifHapticsEnabled());
        sliderRingNotifHapticStrength.setValue(SettingsManager.getGlyphNotifHapticStrength());
        layoutRingNotifHapticStrength.setVisibility(switchRingNotifHaptics.isChecked() ? View.VISIBLE : View.GONE);

        switchFlip.setChecked(SettingsManager.isGlyphFlipEnabled());
        switchScreenOff.setChecked(SettingsManager.isGlyphScreenOffOnly());

        switchShake.setChecked(SettingsManager.isGlyphShakeTorchEnabled());
        sliderShakeSensitivity.setValue(SettingsManager.getGlyphShakeSensitivity());
        sliderShakeHapticStrength.setValue(SettingsManager.getGlyphShakeHapticIntensity());
        int shakeCount = SettingsManager.getGlyphShakeCount();
        if (shakeCount == 1)
            rgShakeCount.check(R.id.rb_one);
        else if (shakeCount == 2)
            rgShakeCount.check(R.id.rb_two);
        else if (shakeCount == 3)
            rgShakeCount.check(R.id.rb_three);
        layoutShakeSettings.setVisibility(switchShake.isChecked() ? View.VISIBLE : View.GONE);

        switchAssistantMic.setChecked(false); // Not implemented yet in backend as per main project
        switchBatteryBar.setChecked(SettingsManager.isGlyphChargingLevelEnabled());
        switchChargingFlipOnly.setChecked(SettingsManager.isGlyphChargingFlipOnly());
        layoutChargingFlipOnly.setVisibility(switchBatteryBar.isChecked() ? View.VISIBLE : View.GONE);

        switchVolumeBar.setChecked(SettingsManager.isGlyphVolumeLevelEnabled());
        switchVolumeFlipOnly.setChecked(SettingsManager.isGlyphVolumeFlipOnly());
        layoutVolumeFlipOnly.setVisibility(switchVolumeBar.isChecked() ? View.VISIBLE : View.GONE);

        switchMusicVisualizer.setChecked(SettingsManager.isGlyphMusicVisualizerEnabled());
        switchMusicVisualizerFlipOnly.setChecked(SettingsManager.isGlyphMusicVisualizerFlipOnly());
        layoutMusicVisualizerFlipOnly.setVisibility(switchMusicVisualizer.isChecked() ? View.VISIBLE : View.GONE);

        switchProgress.setChecked(SettingsManager.isGlyphProgressEnabled());
        switchProgressMusic.setChecked(SettingsManager.isGlyphProgressMusicEnabled());
        switchProgressFlipOnly.setChecked(SettingsManager.isGlyphProgressFlipOnly());
        layoutProgressMusic.setVisibility(switchProgress.isChecked() ? View.VISIBLE : View.GONE);
        layoutProgressFlipOnly.setVisibility(switchProgress.isChecked() ? View.VISIBLE : View.GONE);

        switchIndicatorsFlipOnly.setChecked(SettingsManager.isGlyphIndicatorsFlipOnly());

        updateStyleLabels();
        refreshUIState();
    }

    private void setupListeners() {
        switchMaster.setOnCheckedChangeListener((v, isChecked) -> {
            quickTick(15, 120);
            SettingsManager.enableGlyph(isChecked);
            refreshUIState();
            mHandler.post(ServiceUtils::checkGlyphService);
        });

        sliderBrightness.addOnChangeListener((s, value, fromUser) -> {
            updateOutlineAlpha(value);
            if (fromUser) {
                quickTick(10, 50);
                SettingsManager.setGlyphBrightness((int) value);
                if (SettingsManager.isGlyphEnabled()) {
                    if (previewRunnable != null)
                        mHandler.removeCallbacks(previewRunnable);
                    updateHardware((int) (value * (4095f / 5f)));
                    previewRunnable = () -> updateHardware(0);
                    mHandler.postDelayed(previewRunnable, 1500);
                }
            }
        });

        switchRingNotifHaptics.setOnCheckedChangeListener((v, isChecked) -> {
            quickTick(20, 100);
            SettingsManager.setGlyphNotifHapticsEnabled(isChecked);
            layoutRingNotifHapticStrength.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        sliderRingNotifHapticStrength.addOnChangeListener((s, value, fromUser) -> {
            if (fromUser) {
                SettingsManager.setGlyphNotifHapticStrength((int) value);
            }
        });

        sliderRingNotifHapticStrength.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                quickTick(30, (int) slider.getValue());
            }
        });

        switchFlip.setOnCheckedChangeListener((v, isChecked) -> {
            quickTick(20, 100);
            SettingsManager.setGlyphFlipEnabled(isChecked);
            mHandler.post(ServiceUtils::checkGlyphService);
        });

        switchScreenOff.setOnCheckedChangeListener((v, isChecked) -> SettingsManager.setGlyphScreenOffOnly(isChecked));

        switchShake.setOnCheckedChangeListener((v, isChecked) -> {
            quickTick(20, 100);
            SettingsManager.setGlyphShakeTorchEnabled(isChecked);
            layoutShakeSettings.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            mHandler.postDelayed(() -> {
                if (isChecked)
                    ShakeManager.startShakeService(getContext());
                else
                    ShakeManager.stopShakeService(getContext());
            }, 100);
        });

        sliderShakeSensitivity.addOnChangeListener((s, value, fromUser) -> {
            if (fromUser)
                SettingsManager.setGlyphShakeSensitivity((int) value);
        });

        sliderShakeHapticStrength.addOnChangeListener((s, value, fromUser) -> {
            if (fromUser)
                SettingsManager.setGlyphShakeHapticIntensity((int) value);
        });

        sliderShakeHapticStrength.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                quickTick(30, (int) slider.getValue());
            }
        });

        rgShakeCount.setOnCheckedChangeListener((group, checkedId) -> {
            int count = 2;
            if (checkedId == R.id.rb_one)
                count = 1;
            else if (checkedId == R.id.rb_two)
                count = 2;
            else if (checkedId == R.id.rb_three)
                count = 3;
            SettingsManager.setGlyphShakeCount(count);
        });

        switchBatteryBar.setOnCheckedChangeListener((v, isChecked) -> {
            SettingsManager.setGlyphChargingLevelEnabled(isChecked);
            layoutChargingFlipOnly.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            mHandler.post(ServiceUtils::checkGlyphService);
        });

        switchChargingFlipOnly
                .setOnCheckedChangeListener((v, isChecked) -> SettingsManager.setGlyphChargingFlipOnly(isChecked));

        switchVolumeBar.setOnCheckedChangeListener((v, isChecked) -> {
            SettingsManager.setGlyphVolumeLevelEnabled(isChecked);
            layoutVolumeFlipOnly.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            mHandler.post(ServiceUtils::checkGlyphService);
        });

        switchVolumeFlipOnly
                .setOnCheckedChangeListener((v, isChecked) -> SettingsManager.setGlyphVolumeFlipOnly(isChecked));

        switchProgress.setOnCheckedChangeListener((v, isChecked) -> {
            SettingsManager.setGlyphProgressEnabled(isChecked);
            layoutProgressMusic.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            layoutProgressFlipOnly.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            mHandler.post(ServiceUtils::checkGlyphService);
        });

        switchProgressMusic
                .setOnCheckedChangeListener((v, isChecked) -> SettingsManager.setGlyphProgressMusicEnabled(isChecked));

        switchProgressFlipOnly
                .setOnCheckedChangeListener((v, isChecked) -> SettingsManager.setGlyphProgressFlipOnly(isChecked));

        switchMusicVisualizer.setOnCheckedChangeListener((v, isChecked) -> {
            SettingsManager.setGlyphMusicVisualizerEnabled(isChecked);
            layoutMusicVisualizerFlipOnly.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            mHandler.post(ServiceUtils::checkGlyphService);
        });

        switchMusicVisualizerFlipOnly.setOnCheckedChangeListener(
                (v, isChecked) -> SettingsManager.setGlyphMusicVisualizerFlipOnly(isChecked));

        switchIndicatorsFlipOnly
                .setOnCheckedChangeListener((v, isChecked) -> SettingsManager.setGlyphIndicatorsFlipOnly(isChecked));

        cardImport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/ogg");
            startActivityForResult(intent, 1111);
        });

        // Click listeners for style cards (Temporarily opening existing
        // fragments/activities if possible, or TBD)
        cardRingtones.setOnClickListener(v -> toggleStyles(rvCallStyles, "call"));
        cardNotifications.setOnClickListener(v -> toggleStyles(rvNotifStyles, "notif"));
        cardFlipStyle.setOnClickListener(v -> toggleStyles(null, "flip"));
    }

    private void toggleStyles(RecyclerView rv, String type) {
        if (rv == null)
            return;
        if (rv.getVisibility() == View.VISIBLE) {
            rv.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.VISIBLE);
            setupStyleList(rv, type);
        }
    }

    private void setupStyleList(RecyclerView rv, String type) {
        List<String> names = new ArrayList<>();
        List<String> values = new ArrayList<>();

        // Add default styles
        String[] defaults = { "static", "pulse", "blink" };
        for (String s : defaults) {
            names.add(s.substring(0, 1).toUpperCase() + s.substring(1));
            values.add(s);
        }

        // Add custom ringtones
        List<File> custom = CustomRingtoneManager.getImportedRingtones(getContext());
        for (File f : custom) {
            names.add("🎵 " + f.getName());
            values.add(f.getName());
        }

        int selectedPos = 0;
        String current = type.equals("call") ? SettingsManager.getGlyphCallAnimation()
                : SettingsManager.getGlyphNotifsAnimation();
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).equals(current)) {
                selectedPos = i;
                break;
            }
        }

        StyleAdapter adapter = new StyleAdapter(getContext(), names, values, selectedPos, vibrator,
                type.equals("call") ? android.media.AudioManager.STREAM_RING
                        : android.media.AudioManager.STREAM_NOTIFICATION,
                this);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);
    }

    @Override
    public int getBrightness() {
        return (int) (sliderBrightness.getValue() * (4095f / 5f));
    }

    private void updateOutlineAlpha(float value) {
        if (spacewar != null) {
            spacewar.setAlpha(0.2f + (value / 5f) * 0.8f);
        }
    }

    private void refreshUIState() {
        boolean enabled = SettingsManager.isGlyphEnabled();
        float alpha = enabled ? 1.0f : 0.5f;

        MaterialCardView[] cards = { cardRingtones, cardNotifications, cardFlipStyle, cardEssentialLights,
                cardShakeToGlyph };
        for (MaterialCardView c : cards) {
            if (c != null) {
                c.setEnabled(enabled);
                c.setAlpha(alpha);
            }
        }
        sliderBrightness.setEnabled(enabled);
    }

    private void updateStyleLabels() {
        // This would normally fetch from Settings.Secure via SettingsManager
        // For now, placeholders
        textCurrentCallStyle.setText("Default");
        textCurrentNotifStyle.setText("Default");
        textCurrentFlipStyle.setText("Default");
    }

    private void quickTick(int d, int a) {
        if (vibrator != null && vibrator.hasVibrator()) {
            VibrationAttributes attrs = new VibrationAttributes.Builder()
                    .setUsage(VibrationAttributes.USAGE_ALARM)
                    .build();
            vibrator.vibrate(VibrationEffect.createOneShot(d, a), attrs);
        }
    }

    private void updateHardware(int val) {
        for (GlyphManager.Glyph g : GlyphManager.Glyph.getBasicGlyphs())
            GlyphManager.setBrightness(g, val);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1111 && resultCode == -1 && data != null && data.getData() != null) {
            File f = CustomRingtoneManager.importOgg(getContext(), data.getData(),
                    "imported_" + System.currentTimeMillis() + ".ogg");
            if (f != null) {
                updateStyleLabels();
                if (Constants.CONTEXT == null) {
                    Constants.CONTEXT = getContext();
                }
            }
        }
    }
}
