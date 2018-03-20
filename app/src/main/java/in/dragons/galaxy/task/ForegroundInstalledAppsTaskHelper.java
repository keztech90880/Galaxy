package in.dragons.galaxy.task;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import in.dragons.galaxy.AppListFragment;
import in.dragons.galaxy.GalaxyActivity;
import in.dragons.galaxy.fragment.FilterMenu;
import in.dragons.galaxy.model.App;

public abstract class ForegroundInstalledAppsTaskHelper extends AppListFragment {

    protected Context context;
    protected View progressIndicator;

    protected void setContext(Context context) {
        this.context = context;
    }

    protected boolean includeSystemApps = false;

    protected void setIncludeSystemApps(boolean includeSystemApps) {
        this.includeSystemApps = includeSystemApps;
    }

    static protected App getInstalledApp(PackageManager pm, String packageName) {
        try {
            App app = new App(pm.getPackageInfo(packageName, PackageManager.GET_META_DATA | PackageManager.GET_PERMISSIONS));
            app.setDisplayName(pm.getApplicationLabel(app.getPackageInfo().applicationInfo).toString());
            return app;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    protected static Map<String, App> filterSystemApps(Map<String, App> apps) {
        Map<String, App> result = new HashMap<>();
        for (App app : apps.values()) {
            if (!app.isSystem()) {
                result.put(app.getPackageName(), app);
            }
        }
        return result;
    }

    protected Map<String, App> getInstalledApps(boolean includeDisabled, Map<String, App> installedApps) {
        PackageManager pm = this.getActivity().getPackageManager();
        for (PackageInfo reducedPackageInfo : pm.getInstalledPackages(0)) {
            if (!includeDisabled
                    && null != reducedPackageInfo.applicationInfo
                    && !reducedPackageInfo.applicationInfo.enabled
                    ) {
                continue;
            }
            App app = getInstalledApp(pm, reducedPackageInfo.packageName);
            if (null != app) {
                installedApps.put(app.getPackageName(), app);
            }
        }
        if (!includeSystemApps) {
            installedApps = filterSystemApps(installedApps);
        }
        return installedApps;
    }

    protected void checkAppListValidity() {
        AppListValidityCheckTask task = new AppListValidityCheckTask((GalaxyActivity) this.getActivity());
        task.setIncludeSystemApps(new FilterMenu((GalaxyActivity) this.getActivity()).getFilterPreferences().isSystemApps());
        task.execute();
    }
}