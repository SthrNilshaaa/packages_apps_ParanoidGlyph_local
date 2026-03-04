package co.aospa.glyph.Settings.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.materialswitch.MaterialSwitch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import co.aospa.glyph.R;

public class AppInfoAdapter extends RecyclerView.Adapter<AppInfoAdapter.AppViewHolder> implements Filterable {

    public static class AppInfo {
        public String name;
        public String packageName;
        public Drawable icon;
        public boolean isEnabled;
        public boolean isSystemApp;
    }

    private final Context context;
    private final PackageManager pm;
    private final SharedPreferences prefs;

    private List<AppInfo> rawApps = new ArrayList<>();
    private List<AppInfo> allApps = new ArrayList<>();
    private List<AppInfo> filteredApps = new ArrayList<>();
    private Set<String> enabledPackages;
    private boolean showSystemApps = false;
    private CharSequence currentQuery = "";

    private final ExecutorService iconLoaderExecutor = Executors.newFixedThreadPool(4);

    public AppInfoAdapter(Context context) {
        this.context = context;
        this.pm = context.getPackageManager();
        this.prefs = context.getSharedPreferences("Glyph Settings", Context.MODE_PRIVATE);
        this.enabledPackages = new HashSet<>(prefs.getStringSet("essential_apps", new HashSet<>()));
        loadApps();
    }

    public void setShowSystemApps(boolean show) {
        this.showSystemApps = show;
        applyFilters();
    }

    private void setCurrentQuery(CharSequence query) {
        this.currentQuery = query;
        applyFilters();
    }

    private void applyFilters() {
        List<AppInfo> level1 = new ArrayList<>();
        for (AppInfo row : rawApps) {
            if (!showSystemApps && row.isSystemApp && !row.isEnabled) {
                continue;
            }
            level1.add(row);
        }
        allApps = level1;

        if (currentQuery == null || currentQuery.toString().isEmpty()) {
            filteredApps = allApps;
        } else {
            List<AppInfo> level2 = new ArrayList<>();
            for (AppInfo row : allApps) {
                if (row.name.toLowerCase().contains(currentQuery.toString().toLowerCase())) {
                    level2.add(row);
                }
            }
            filteredApps = level2;
        }
        notifyDataSetChanged();
    }

    private void loadApps() {
        new Thread(() -> {
            List<AppInfo> loadedApps = new ArrayList<>();

            if (AppListCache.cachedApps != null) {
                for (AppInfo cache : AppListCache.cachedApps) {
                    AppInfo info = new AppInfo();
                    info.name = cache.name;
                    info.packageName = cache.packageName;
                    info.icon = cache.icon;
                    info.isSystemApp = cache.isSystemApp;
                    info.isEnabled = enabledPackages.contains(info.packageName);
                    loadedApps.add(info);
                }
            } else {
                List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                for (ApplicationInfo packageInfo : packages) {
                    if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                        AppInfo info = new AppInfo();
                        info.name = packageInfo.loadLabel(pm).toString();
                        info.packageName = packageInfo.packageName;
                        info.icon = null;
                        info.isEnabled = enabledPackages.contains(info.packageName);
                        info.isSystemApp = (packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                        loadedApps.add(info);
                    }
                }
            }

            Collections.sort(loadedApps, (a1, a2) -> {
                if (a1.isEnabled && !a2.isEnabled)
                    return -1;
                if (!a1.isEnabled && a2.isEnabled)
                    return 1;
                return a1.name.toLowerCase().compareTo(a2.name.toLowerCase());
            });

            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    rawApps = new ArrayList<>(loadedApps);
                    applyFilters();
                });
            }
        }).start();
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo app = filteredApps.get(position);
        holder.textName.setText(app.name);

        if (app.icon == null) {
            holder.imageIcon.setImageDrawable(null);
            iconLoaderExecutor.execute(() -> {
                try {
                    ApplicationInfo info = pm.getApplicationInfo(app.packageName, 0);
                    final Drawable loadedIcon = pm.getApplicationIcon(info);
                    if (context instanceof android.app.Activity) {
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            app.icon = loadedIcon;
                            if (holder.getBindingAdapterPosition() == position) {
                                holder.imageIcon.setImageDrawable(app.icon);
                            }
                        });
                    }
                } catch (PackageManager.NameNotFoundException ignored) {
                }
            });
        } else {
            holder.imageIcon.setImageDrawable(app.icon);
        }

        holder.switchEnabled.setOnCheckedChangeListener(null);
        holder.switchEnabled.setChecked(app.isEnabled);
        holder.itemView.setOnClickListener(v -> holder.switchEnabled.toggle());

        holder.switchEnabled.setOnCheckedChangeListener((btn, isChecked) -> {
            app.isEnabled = isChecked;
            if (isChecked) {
                enabledPackages.add(app.packageName);
            } else {
                enabledPackages.remove(app.packageName);
            }
            prefs.edit().putStringSet("essential_apps", enabledPackages).apply();

            for (AppInfo origApp : rawApps) {
                if (origApp.packageName.equals(app.packageName)) {
                    origApp.isEnabled = isChecked;
                    break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredApps.size();
    }

    @Override
    public Filter getFilter() {
        return new AppFilter();
    }

    private class AppFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            return new FilterResults();
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            setCurrentQuery(constraint);
        }
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        public TextView textName;
        public ImageView imageIcon;
        public MaterialSwitch switchEnabled;

        public AppViewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textAppName);
            imageIcon = itemView.findViewById(R.id.imageAppIcon);
            switchEnabled = itemView.findViewById(R.id.switchAppEnabled);
        }
    }
}
