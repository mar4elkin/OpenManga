package org.nv95.openmanga.providers;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.json.JSONException;
import org.json.JSONObject;
import org.nv95.openmanga.BuildConfig;
import org.nv95.openmanga.utils.NetworkUtils;

import java.util.ArrayList;

/**
 * Created by nv95 on 19.02.16.
 */
public class AppUpdatesProvider {

    @Nullable
    private final JSONObject mResponse;

    @WorkerThread
    public AppUpdatesProvider() {
        JSONObject response;
        try {
            response = NetworkUtils.getJsonObject(BuildConfig.SELFUPDATE_URL);
        } catch (Exception e) {
            response = null;
        }
        mResponse = response;
    }

    public boolean isSuccess() {
        try {
            return mResponse != null && "success".equals(mResponse.getString("status"));
        } catch (JSONException e) {
            return false;
        }
    }

    public AppUpdateInfo[] getLatestUpdates() {
        final ArrayList<AppUpdateInfo> updates = new ArrayList<>();
        if (mResponse != null) {
            AppUpdateInfo update = getLatestRelease();
            if (update != null && update.isActual()) {
                update.versionName = "Latest release: v" + update.versionName;
                updates.add(update);
            }
            update = getLatestBeta();
            if (update != null && update.isActual()) {
                update.versionName = "Latest beta: v" + update.versionName;
                updates.add(update);
            }
        }
        return updates.toArray(new AppUpdateInfo[updates.size()]);
    }

    public AppUpdateInfo getLatestAny() {
        AppUpdateInfo beta = getLatestBeta();
        AppUpdateInfo release = getLatestRelease();
        return beta != null && release != null && beta.versionCode > release.versionCode ? beta : release;
    }

    @Nullable
    public AppUpdateInfo getLatest(String branch) {
        if (mResponse == null) {
            return null;
        }
        try {
            return new AppUpdateInfo(mResponse.getJSONObject(branch));
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public AppUpdateInfo getLatestRelease() {
        return getLatest("release");
    }

    @Nullable
    public AppUpdateInfo getLatestBeta() {
        return getLatest("beta");
    }

    public static class AppUpdateInfo {
        protected String versionName;
        protected int versionCode;
        protected String url;

        public String getVersionName() {
            return versionName;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public String getUrl() {
            return url;
        }

        protected AppUpdateInfo(JSONObject jsonObject) throws JSONException {
            this.versionName = jsonObject.getString("version_name");
            this.versionCode = jsonObject.getInt("version");
            this.url = jsonObject.getString("url");
        }

        public boolean isActual() {
            return versionCode > BuildConfig.VERSION_CODE;
        }
    }
}
