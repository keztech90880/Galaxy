package in.dragons.galaxy.fragment.details;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.percolate.caffeine.ViewUtils;

import in.dragons.galaxy.DetailsFragment;
import in.dragons.galaxy.R;
import in.dragons.galaxy.model.App;

public abstract class AbstractHelper {

    protected DetailsFragment detailsFragment;
    protected App app;

    abstract public void draw();

    public AbstractHelper(DetailsFragment detailsFragment, App app) {
        this.detailsFragment = detailsFragment;
        this.app = app;
    }

    protected void setText(int viewId, String text) {
        TextView textView = ViewUtils.findViewById(detailsFragment.getActivity(), viewId);
        if (null != textView)
            textView.setText(text);
    }

    protected void setText(int viewId, int stringId, Object... text) {
        setText(viewId, detailsFragment.getActivity().getString(stringId, text));
    }

    void initExpandableGroup(int viewIdHeader, int viewIdContainer, final View.OnClickListener l) {
        TextView viewHeader = ViewUtils.findViewById(detailsFragment.getActivity(), viewIdHeader);
        viewHeader.setVisibility(View.VISIBLE);
        final LinearLayout viewContainer = ViewUtils.findViewById(detailsFragment.getActivity(), viewIdContainer);
        viewHeader.setOnClickListener(v -> {
            boolean isExpanded = viewContainer.getVisibility() == View.VISIBLE;
            if (isExpanded) {
                viewContainer.setVisibility(View.GONE);
                ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_more, 0);
            } else {
                if (null != l) {
                    l.onClick(v);
                }
                viewContainer.setVisibility(View.VISIBLE);
                ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less, 0);
            }
        });
    }

    void initExpandableGroup(int viewIdHeader, int viewIdContainer) {
        initExpandableGroup(viewIdHeader, viewIdContainer, null);
    }
}