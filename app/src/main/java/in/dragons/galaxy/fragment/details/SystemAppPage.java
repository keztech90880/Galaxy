package in.dragons.galaxy.fragment.details;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.percolate.caffeine.ViewUtils;

import in.dragons.galaxy.DetailsFragment;
import in.dragons.galaxy.R;
import in.dragons.galaxy.model.App;

public class SystemAppPage extends AbstractHelper {

    public SystemAppPage(DetailsFragment detailsFragment, App app) {
        super(detailsFragment, app);
    }

    @Override
    public void draw() {
        if (!app.isInstalled()) {
            return;
        }
        ImageView systemAppInfo = ViewUtils.findViewById(detailsFragment.getActivity(), R.id.system_app_info);
        systemAppInfo.setVisibility(View.VISIBLE);
        systemAppInfo.setOnClickListener(v -> startActivity());
    }

    private void startActivity() {
        try {
            detailsFragment.getActivity().startActivity(getIntent());
        } catch (ActivityNotFoundException e) {
            Log.w(getClass().getSimpleName(), "Could not find system app activity");
        }
    }

    private Intent getIntent() {
        Intent intent;
        intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + app.getPackageName()));
        return intent;
    }
}