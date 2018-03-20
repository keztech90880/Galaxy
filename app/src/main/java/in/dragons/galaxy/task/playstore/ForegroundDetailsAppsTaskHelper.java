package in.dragons.galaxy.task.playstore;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.yeriomin.playstoreapi.AuthException;
import com.github.yeriomin.playstoreapi.DetailsResponse;
import com.github.yeriomin.playstoreapi.GooglePlayAPI;
import com.github.yeriomin.playstoreapi.GooglePlayException;
import com.github.yeriomin.playstoreapi.IteratorGooglePlayException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

import in.dragons.galaxy.AccountTypeDialogBuilder;
import in.dragons.galaxy.ContextUtil;
import in.dragons.galaxy.CredentialsEmptyException;
import in.dragons.galaxy.FirstLaunchChecker;
import in.dragons.galaxy.PlayStoreApiAuthenticator;
import in.dragons.galaxy.PreferenceFragment;
import in.dragons.galaxy.R;
import in.dragons.galaxy.model.App;
import in.dragons.galaxy.model.AppBuilder;
import in.dragons.galaxy.model.ReviewBuilder;
import in.dragons.galaxy.task.TaskWithProgress;

public abstract class ForegroundDetailsAppsTaskHelper extends ForegroundUpdatableAppsTaskHelper {

    protected Throwable exception;
    protected TextView errorView;

    protected Context context;
    protected ProgressDialog progressDialog;
    protected View progressIndicator;

    public View getProgressIndicator() {
        return progressIndicator;
    }

    public void setProgressIndicator(View progressIndicator) {
        this.progressIndicator = progressIndicator;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    protected App getResult(GooglePlayAPI api, String packageName) throws IOException {
        DetailsResponse response = api.details(packageName);
        App app = AppBuilder.build(response.getDocV2());
        if (response.hasUserReview()) {
            app.setUserReview(ReviewBuilder.build(response.getUserReview()));
        }
        PackageManager pm = this.getActivity().getPackageManager();
        try {
            app.getPackageInfo().applicationInfo = pm.getApplicationInfo(packageName, 0);
            app.getPackageInfo().versionCode = pm.getPackageInfo(packageName, 0).versionCode;
            app.setInstalled(true);
        } catch (PackageManager.NameNotFoundException e) {
            // App is not installed
        }
        return app;
    }

    @Override
    protected void processIOException(IOException e) {
        if (null != e && e instanceof GooglePlayException && ((GooglePlayException) e).getCode() == 404) {
            ContextUtil.toast(this.getActivity(), R.string.details_not_available_on_play_store);
        }
    }
}
