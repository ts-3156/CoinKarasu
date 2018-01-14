package com.coinkarasu.activities.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.R;
import com.coinkarasu.api.cryptocompare.request.BlockingRequest;
import com.coinkarasu.billingmodule.BillingActivity;
import com.coinkarasu.utils.ApiKeyUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;
import com.coinkarasu.utils.UuidUtils;
import com.coinkarasu.utils.cache.DiskBasedCache;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class PreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "PreferencesFragment";

    private Preference.OnPreferenceChangeListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preferences);

        findPreference("pref_app_version").setOnPreferenceClickListener(this);
        findPreference("pref_clear_cache").setOnPreferenceClickListener(this);
        findPreference("pref_clear_config").setOnPreferenceClickListener(this);
        findPreference("pref_open_source_licenses").setOnPreferenceClickListener(this);
        findPreference("pref_remove_ads").setOnPreferenceClickListener(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if (BuildConfig.DEBUG) {
            ListPreference ckHost = (ListPreference) findPreference("pref_change_ck_host");
            CharSequence[] entries = ckHost.getEntries();
            entries[0] = BuildConfig.CK_HOST.substring(7) + " (default)";
            CharSequence[] entryValues = ckHost.getEntryValues();
            entryValues[0] = BuildConfig.CK_HOST;
            ckHost.setEntries(entries);
            ckHost.setEntryValues(entryValues);
            ckHost.setDefaultValue(entryValues[0]);
            int index = Arrays.asList(entryValues).indexOf(PrefHelper.getCkHost(getActivity(), BuildConfig.CK_HOST));
            ckHost.setValueIndex(index < 0 ? 0 : index);
            bindPreferenceSummaryToValue(prefs, "pref_change_ck_host");

            Preference uuidFile = findPreference("pref_uuid_file_exists");
            uuidFile.setSummary(UuidUtils.exists(getActivity()) ? UuidUtils.get(getActivity()) : "No");

            Preference tokenFile = findPreference("pref_token_file_exists");
            tokenFile.setSummary(ApiKeyUtils.exists(getActivity()) ? ApiKeyUtils.get(getActivity()).toString() : ApiKeyUtils.dummy().toString());
        } else {
            getPreferenceScreen().removePreference(findPreference("pref_category_debug"));
        }

        setDefaultSyncFrequencyIfNeeded();
        ((SwitchPreference) findPreference("pref_remove_ads")).setChecked(PrefHelper.isPremium(getActivity()));

        bindPreferenceSummaryToValue(prefs, "pref_sync_frequency");
        bindPreferenceSummaryToValue(prefs, "pref_currency");
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        if (key.equals("pref_app_version")) {
            new GetLatestVersionTask().execute();
        } else if (key.equals("pref_clear_cache")) {
            showDialog(R.string.pref_clear_cache_dialog, "OK", true, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    new ClearCacheTask(getActivity().getCacheDir()).execute();
                }
            });
        } else if (key.equals("pref_clear_config")) {
            showDialog(R.string.pref_clear_config_dialog, "OK", true, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    PrefHelper.clear(getActivity());
                    getActivity().recreate();
                }
            });
        } else if (key.equals("pref_open_source_licenses")) {
            startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));
        } else if (key.equals("pref_remove_ads")) {
            if (PrefHelper.isPremium(getActivity())) {
                ((SwitchPreference) preference).setChecked(true);
                showDialog(R.string.pref_remove_ads_already_available, "OK", false, null);
            } else {
                BillingActivity.start(getActivity(), R.string.billing_dialog_remove_ads);
            }
        }

        return true;
    }

    private void showDialog(int msgResId, String positiveLabel, boolean isNeedNegativeButton, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msgResId).setPositiveButton(positiveLabel, listener);

        if (isNeedNegativeButton) {
            builder.setNegativeButton("Cancel", null);
        }

        builder.create().show();
    }

    private void bindPreferenceSummaryToValue(SharedPreferences prefs, String key) {
        Preference pref = findPreference(key);
        pref.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(pref, prefs.getString(key, ""));
    }

    private void setDefaultSyncFrequencyIfNeeded() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String key = "pref_sync_frequency";

        String stringValue = prefs.getString(key, String.valueOf(PrefHelper.DEFAULT_SYNC_INTERVAL));
        if (!PrefHelper.isPremium(getActivity()) && Integer.valueOf(stringValue) < PrefHelper.MIN_SYNC_INTERVAL) {
            int interval = PrefHelper.setDefaultSyncInterval(getActivity());
            ListPreference pref = (ListPreference) findPreference(key);
            pref.setValueIndex(pref.findIndexOfValue(String.valueOf(interval)));
            if (listener != null) {
                listener.onPreferenceChange(pref, String.valueOf(interval));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        setDefaultSyncFrequencyIfNeeded();
        ((SwitchPreference) findPreference("pref_remove_ads")).setChecked(PrefHelper.isPremium(getActivity()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (Preference.OnPreferenceChangeListener) context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            return;
        }
        listener = (Preference.OnPreferenceChangeListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private static class ClearCacheTask extends AsyncTask<Void, Void, Void> {
        private File file;

        ClearCacheTask(File file) {
            this.file = file;
        }

        @Override
        protected Void doInBackground(Void... params) {
            long start = System.currentTimeMillis();
            new DiskBasedCache(file).clear();
            if (DEBUG) CKLog.d(TAG, "Clear cache elapsed time: "
                    + (System.currentTimeMillis() - start) + "ms");
            return null;
        }
    }

    private class GetLatestVersionTask extends AsyncTask<Void, Void, Void> {
        private static final String URL = "https://coinkarasu.firebaseapp.com/version.json";
        private String version;

        @Override
        protected Void doInBackground(Void... params) {
            JSONObject response = new BlockingRequest(getActivity(), URL).perform();
            if (response != null && response.has("version")) {
                try {
                    version = response.getString("version");
                } catch (JSONException e) {
                    CKLog.e(TAG, e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if (TextUtils.isEmpty(version) || getActivity() == null || getActivity().isFinishing()) {
                showDialog(R.string.pref_app_version_up_to_date, "OK", false, null);
                return;
            }

            PackageInfo pInfo = null;
            try {
                pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                CKLog.e(TAG, e);
            }

            if (pInfo != null && version.equals(pInfo.versionName)) {
                showDialog(R.string.pref_app_version_up_to_date, "OK", false, null);
                return;
            }

            showDialog(R.string.pref_app_version_please_update, "Update", true, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    openPlayStore(getActivity());
                }
            });
        }
    }

    public static void openPlayStore(Context context) {
        String appId = context.getPackageName();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appId));
        boolean found = false;

        final List<ResolveInfo> otherApps = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo otherApp : otherApps) {
            if (!otherApp.activityInfo.applicationInfo.packageName.equals("com.android.vending")) {
                continue;
            }

            ActivityInfo otherAppActivity = otherApp.activityInfo;
            ComponentName componentName =
                    new ComponentName(otherAppActivity.applicationInfo.packageName, otherAppActivity.name);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setComponent(componentName);
            context.startActivity(intent);
            found = true;
            break;
        }

        if (!found) {
            Intent webIntent =
                    new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appId));
            context.startActivity(webIntent);
        }
    }
}
