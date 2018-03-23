package in.dragons.galaxy;

import android.app.DownloadManager;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.percolate.caffeine.ViewUtils;
import com.trello.rxlifecycle2.android.FragmentEvent;

import java.util.Collections;

import in.dragons.galaxy.task.playstore.ForegroundUpdatableAppsTaskHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.DOWNLOAD_SERVICE;

public class UpdatableAppsFragment extends ForegroundUpdatableAppsTaskHelper {

    private DownloadManager.Query query;
    private DownloadManager dm;
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

        v = inflater.inflate(R.layout.app_updatable_inc, container, false);

        setupListView(v, R.layout.two_line_list_item_with_icon);
        setSearchView(this, true);
        setupDelta();
        setupFab();

        loadApps();

        getListView().setOnItemClickListener((parent, view, position, id) -> grabDetails(position));
        registerForContextMenu(getListView());

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        setSearchView(this, true);
        new UpdateAllReceiver((GalaxyActivity) this.getActivity());
        checkAppListValidity();
    }

    @Override
    public void loadApps() {
        setProgress();
        Observable.fromCallable(() -> getResult(new PlayStoreApiAuthenticator(this.getActivity()).getApi()))
                .subscribeOn(Schedulers.io())
                .compose(bindUntilEvent(FragmentEvent.PAUSE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {

                    clearApps();
                    Collections.sort(result);

                    addApps(result);
                    removeProgress();

                    if (success() && result.isEmpty())
                        ViewUtils.findViewById(v, R.id.unicorn).setVisibility(View.VISIBLE);
                    else {
                        setText(R.id.updates_txt, R.string.list_update_all_txt, result.size());
                        setupButtons();
                    }
                }, this::processException);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.action_ignore || item.getItemId() == R.id.action_unwhitelist) {
            String packageName = getAppByListPosition(info.position).getPackageName();
            BlackWhiteListManager manager = new BlackWhiteListManager(this.getActivity());
            if (item.getItemId() == R.id.action_ignore) {
                manager.add(packageName);
            } else {
                manager.remove(packageName);
            }
            removeApp(packageName);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void removeApp(String packageName) {
        super.removeApp(packageName);
        if (updatableApps.isEmpty()) {
            v.findViewById(R.id.unicorn).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void clearApps() {
        ((AppListAdapter) getListView().getAdapter()).clear();
    }

    @Override
    protected void setProgress() {
        ViewUtils.findViewById(v, R.id.progress).setVisibility(View.VISIBLE);
    }

    @Override
    protected void removeProgress() {
        ViewUtils.findViewById(v, R.id.progress).setVisibility(View.GONE);
    }

    public void launchUpdateAll() {
        ((GalaxyApplication) getActivity().getApplicationContext()).setBackgroundUpdating(true);
        new UpdateChecker().onReceive(UpdatableAppsFragment.this.getActivity(), getActivity().getIntent());
        ViewUtils.findViewById(v, R.id.update_all).setVisibility(View.GONE);
        ViewUtils.findViewById(v, R.id.update_cancel).setVisibility(View.VISIBLE);
    }

    public void setupFab() {
        FloatingActionButton fab = ViewUtils.findViewById(this.getActivity(), R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setImageResource(R.drawable.ic_update);
        fab.setOnClickListener(view -> loadApps());
    }

    public void setupButtons() {
        Button update = ViewUtils.findViewById(v, R.id.update_all);
        Button cancel = ViewUtils.findViewById(v, R.id.update_cancel);
        TextView txt = ViewUtils.findViewById(v, R.id.updates_txt);

        update.setVisibility(View.VISIBLE);

        update.setOnClickListener(v -> {
            launchUpdateAll();
            update.setVisibility(View.GONE);
            cancel.setVisibility(View.VISIBLE);
            txt.setText(R.string.list_updating);
        });

        cancel.setOnClickListener(v -> {
            query = new DownloadManager.Query();
            query.setFilterByStatus(DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING);
            dm = (DownloadManager) this.getActivity().getSystemService(DOWNLOAD_SERVICE);
            Cursor c = dm.query(query);
            while (c.moveToNext()) {
                dm.remove(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)));
            }
            update.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.GONE);
            setText(R.id.updates_txt, R.string.list_update_all_txt, updatableApps.size());
        });
    }

    public void setupDelta() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        TextView delta = ViewUtils.findViewById(v, R.id.updates_setting);
        delta.setText(sharedPreferences.getBoolean("PREFERENCE_DOWNLOAD_DELTAS", true) ? R.string.delta_enabled : R.string.delta_disabled);
        delta.setVisibility(View.VISIBLE);
    }
}
