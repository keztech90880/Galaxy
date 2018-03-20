package in.dragons.galaxy;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.trello.rxlifecycle2.android.FragmentEvent;

import in.dragons.galaxy.fragment.details.AppLists;
import in.dragons.galaxy.fragment.details.BackToPlayStore;
import in.dragons.galaxy.fragment.details.Beta;
import in.dragons.galaxy.fragment.details.DownloadOptions;
import in.dragons.galaxy.fragment.details.DownloadOrInstall;
import in.dragons.galaxy.fragment.details.GeneralDetails;
import in.dragons.galaxy.fragment.details.Permissions;
import in.dragons.galaxy.fragment.details.Review;
import in.dragons.galaxy.fragment.details.Screenshot;
import in.dragons.galaxy.fragment.details.Share;
import in.dragons.galaxy.fragment.details.SystemAppPage;
import in.dragons.galaxy.fragment.details.Video;
import in.dragons.galaxy.model.App;
import in.dragons.galaxy.task.playstore.ForegroundDetailsAppsTaskHelper;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DetailsFragment extends ForegroundDetailsAppsTaskHelper {

    protected View v;
    protected DownloadOrInstall downloadOrInstallFragment;
    protected String packageName;

    protected static App app;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.details_activity_layout, container, false);
        setSearchView(this, true);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        packageName = arguments.getString("PackageName");

        loadApps();
    }

    @Override
    public void onPause() {
        if (null != downloadOrInstallFragment) {
            downloadOrInstallFragment.unregisterReceivers();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        redrawButtons();
        super.onResume();
    }

    @Override
    public void loadApps() {
        Observable.fromCallable(() -> getResult(new PlayStoreApiAuthenticator(this.getActivity()).getApi(), packageName))
                .subscribeOn(Schedulers.io())
                .compose(bindUntilEvent(FragmentEvent.PAUSE))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((result) -> {

                    DetailsFragment.app = result;
                    this.redrawDetails(result);

                }, throwable -> processException(throwable));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        new DownloadOptions((GalaxyActivity) this.getActivity(), app).inflate(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return new DownloadOptions((GalaxyActivity) this.getActivity(), app).onContextItemSelected(item);
    }

    private void redrawDetails(App app) {
        new GeneralDetails(this, app).draw();
        new Permissions(this, app).draw();
        new Screenshot(this, app).draw();
        new Review(this, app).draw();
        new AppLists(this, app).draw();
        new BackToPlayStore(this, app).draw();
        new Share(this, app).draw();
        new SystemAppPage(this, app).draw();
        new Video(this, app).draw();
        new Beta(this, app).draw();
        if (null != downloadOrInstallFragment) {
            downloadOrInstallFragment.unregisterReceivers();
        }
        downloadOrInstallFragment = new DownloadOrInstall((GalaxyActivity) this.getActivity(), app);
        redrawButtons();
        new DownloadOptions((GalaxyActivity) this.getActivity(), app).draw();

        getActivity().setTitle(app.getDisplayName());
    }

    private void redrawButtons() {
        if (null != downloadOrInstallFragment) {
            downloadOrInstallFragment.unregisterReceivers();
            downloadOrInstallFragment.registerReceivers();
            downloadOrInstallFragment.draw();
        }
    }
}
