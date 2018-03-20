package in.dragons.galaxy;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.percolate.caffeine.ViewUtils;
import com.trello.rxlifecycle2.android.FragmentEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.dragons.galaxy.fragment.FilterMenu;
import in.dragons.galaxy.model.App;
import in.dragons.galaxy.task.ForegroundInstalledAppsTaskHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class InstalledAppsFragment extends ForegroundInstalledAppsTaskHelper {

    private boolean includeSystemApps;
    private Map<String, App> installedApps = new HashMap<>();
    private View v;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
        this.setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (v != null) {
            if ((ViewGroup) v.getParent() != null)
                ((ViewGroup) v.getParent()).removeView(v);
            return v;
        }

        getActivity().setTitle(R.string.activity_title_updates_and_other_apps);

        v = inflater.inflate(R.layout.app_installed_inc, container, false);

        setupListView(v, R.layout.two_line_list_item_with_icon);
        setSearchView(this, true);

        loadApps();

        getListView().setOnItemClickListener((parent, view, position, id) -> {
            grabDetails(position);
        });

        includeSystemApps = new FilterMenu((GalaxyActivity) this.getActivity()).getFilterPreferences().isSystemApps();

        registerForContextMenu(getListView());
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.activity_title_updates_and_other_apps);
        checkAppListValidity();
    }

    @Override
    public void loadApps() {
        setProgress();
        setIncludeSystemApps(includeSystemApps);
        Observable.fromCallable(() -> getInstalledApps(true, installedApps))
                .subscribeOn(Schedulers.io())
                .compose(bindUntilEvent(FragmentEvent.PAUSE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {
                    clearApps();
                    List<App> installedApps = new ArrayList<>(result.values());
                    Collections.sort(installedApps);
                    addApps(installedApps);
                    removeProgress();
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_filter).setVisible(true);
        menu.findItem(R.id.filter_system_apps).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.findItem(R.id.action_flag).setVisible(false);
    }

    @Override
    protected void setProgress() {
        ViewUtils.findViewById(v, R.id.progress).setVisibility(View.VISIBLE);
    }

    @Override
    protected void removeProgress() {
        ViewUtils.findViewById(v, R.id.progress).setVisibility(View.GONE);
    }

}