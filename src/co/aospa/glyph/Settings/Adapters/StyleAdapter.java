package co.aospa.glyph.Settings.Adapters;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import java.util.List;
import co.aospa.glyph.R;
import co.aospa.glyph.Utils.CustomRingtoneManager;
import co.aospa.glyph.Utils.GlyphEffects;

public class StyleAdapter extends RecyclerView.Adapter<StyleAdapter.ViewHolder> {

    private final Context context;
    private final List<String> names;
    private final List<String> values;
    private final Vibrator vibrator;
    private int selectedPosition;
    private final int audioStreamType;
    private final SharedPreferencesProvider prefsProvider;

    public interface SharedPreferencesProvider {
        int getBrightness();
    }

    public StyleAdapter(Context context, List<String> names, List<String> values, int selectedPosition,
            Vibrator vibrator, int audioStreamType, SharedPreferencesProvider prefsProvider) {
        this.context = context;
        this.names = names;
        this.values = values;
        this.selectedPosition = selectedPosition;
        this.vibrator = vibrator;
        this.audioStreamType = audioStreamType;
        this.prefsProvider = prefsProvider;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_style_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = names.get(position);
        String value = values.get(position);
        holder.textName.setText(name);
        holder.radioButton.setChecked(position == selectedPosition);

        boolean isCustom = name.startsWith("🎵 ");
        holder.btnDelete.setVisibility(isCustom ? View.VISIBLE : View.GONE);

        boolean isSelected = position == selectedPosition;
        holder.card.setStrokeWidth(isSelected ? 3 : 1);
        int primaryColor = context.getColor(android.R.color.black);
        try {
            android.util.TypedValue typedValue = new android.util.TypedValue();
            context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
            primaryColor = typedValue.data;
        } catch (Exception e) {
        }

        holder.card.setStrokeColor(isSelected ? primaryColor : 0x22888888);
        holder.card.setCardBackgroundColor(isSelected ? (primaryColor & 0x15FFFFFF) : 0);

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);

            if (vibrator != null) {
                vibrator.vibrate(VibrationEffect.createOneShot(15, 80));
            }

            new Thread(() -> GlyphEffects.run(value, prefsProvider.getBrightness(), vibrator, context, audioStreamType))
                    .start();
        });

        holder.radioButton.setOnClickListener(v -> holder.itemView.performClick());

        if (isCustom) {
            holder.btnDelete.setOnClickListener(v -> deleteRingtone(position, value));
        }
    }

    private void deleteRingtone(int position, String value) {
        if (CustomRingtoneManager.deleteRingtone(context, value)) {
            names.remove(position);
            values.remove(position);
            if (selectedPosition == position) {
                selectedPosition = 0;
            } else if (selectedPosition > position) {
                selectedPosition--;
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public String getSelectedValue() {
        return values.get(selectedPosition);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioButton;
        TextView textName;
        ImageButton btnDelete;
        MaterialCardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardStyleItem);
            radioButton = itemView.findViewById(R.id.radioStyle);
            textName = itemView.findViewById(R.id.textStyleName);
            btnDelete = itemView.findViewById(R.id.btnDeleteStyle);
        }
    }
}
