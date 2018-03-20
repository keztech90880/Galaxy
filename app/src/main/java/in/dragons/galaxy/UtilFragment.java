package in.dragons.galaxy;

import android.app.Fragment;
import android.view.View;

import com.percolate.caffeine.ViewUtils;

public abstract class UtilFragment extends Fragment {

    public void setSearchView(Fragment f, Boolean b) {
        if (b)
            ViewUtils.findViewById(f.getActivity(), R.id.search_toolbar).setVisibility(View.VISIBLE);
        else
            ViewUtils.findViewById(f.getActivity(), R.id.search_toolbar).setVisibility(View.GONE);
    }
}
