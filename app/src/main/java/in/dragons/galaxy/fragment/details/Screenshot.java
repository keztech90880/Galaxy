package in.dragons.galaxy.fragment.details;

import android.content.Intent;
import android.widget.Gallery;

import com.percolate.caffeine.ViewUtils;

import in.dragons.galaxy.DetailsFragment;
import in.dragons.galaxy.FullscreenImageActivity;
import in.dragons.galaxy.ImageAdapter;
import in.dragons.galaxy.R;
import in.dragons.galaxy.model.App;

public class Screenshot extends AbstractHelper {

    public Screenshot(DetailsFragment detailsFragment, App app) {
        super(detailsFragment, app);
    }

    @Override
    public void draw() {
        if (app.getScreenshotUrls().size() > 0) drawGallery();
    }

    private void drawGallery() {
        Gallery gallery = (ViewUtils.findViewById(detailsFragment.getActivity(), R.id.screenshots_gallery));
        int screenWidth = detailsFragment.getActivity().getWindowManager().getDefaultDisplay().getWidth();
        gallery.setAdapter(new ImageAdapter(detailsFragment.getActivity(), app.getScreenshotUrls(), screenWidth));
        gallery.setSpacing(15);
        gallery.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(detailsFragment.getActivity(), FullscreenImageActivity.class);
            intent.putExtra(FullscreenImageActivity.INTENT_SCREENSHOT_NUMBER, position);
            detailsFragment.getActivity().startActivity(intent);
        });
    }
}