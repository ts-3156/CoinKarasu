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
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PreferencesFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener {
    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "PreferencesFragment";

    private OnFragmentInteractionListener listener;

    public PreferencesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preferences);

        findPreference("pref_account_grade").setOnPreferenceClickListener(this);
        findPreference("pref_app_version").setOnPreferenceClickListener(this);
        findPreference("pref_open_source_licenses").setOnPreferenceClickListener(this);
        findPreference("pref_remove_ads").setOnPreferenceClickListener(this);

        if (BuildConfig.DEBUG) {
            findPreference("pref_debug").setOnPreferenceClickListener(this);
        } else {
            getPreferenceScreen().removePreference(findPreference("pref_category_debug"));
        }

        findPreference("pref_app_version").setSummary(getString(R.string.pref_app_version_summary, BuildConfig.VERSION_NAME));

        setDefaultSyncFrequencyIfNeeded(); // bindPreferenceSummaryToValue()の前に一度呼ぶ必要がある
        ((SwitchPreference) findPreference("pref_remove_ads")).setChecked(PrefHelper.isPremium(getActivity()));

        if (PrefHelper.isPremium(getActivity())) {
            findPreference("pref_account_grade").setSummary(R.string.pref_account_grade_summary_premium);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        bindPreferenceSummaryToValue(prefs, "pref_sync_frequency");
        bindPreferenceSummaryToValue(prefs, "pref_currency");
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        switch (key) {
            case "pref_account_grade":
                BillingActivity.start(getActivity(), -1);
                break;
            case "pref_app_version":
                new GetLatestVersionTask(getActivity()).setListener(new GetLatestVersionTask.Listener() {
                    @Override
                    public void finished(String version) {
                        if (getActivity() == null || getActivity().isFinishing() || isDetached() || !isAdded()) {
                            return;
                        }

                        if (TextUtils.isEmpty(version)) {
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
                }).execute();
                break;
            case "pref_open_source_licenses":
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.pref_open_source_licenses_title));
                startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));
                break;
            case "pref_remove_ads":
                if (PrefHelper.isPremium(getActivity())) {
                    ((SwitchPreference) preference).setChecked(true);
                    showDialog(R.string.pref_remove_ads_already_available, "OK", false, null);
                } else {
                    BillingActivity.start(getActivity(), R.string.billing_dialog_remove_ads);
                }
                break;
            case "pref_debug":
                if (BuildConfig.DEBUG) {
                    listener.onNestedPreferenceSelected(key);
                }
                break;
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
        listener = (OnFragmentInteractionListener) context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            return;
        }
        listener = (OnFragmentInteractionListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private static class GetLatestVersionTask extends AsyncTask<Void, Void, String> {
        private static final String TAG = "GetLatestVersionTask";
        private static final String URL = "https://coinkarasu.firebaseapp.com/version.json";

        private Context context;
        private Listener listener;

        public GetLatestVersionTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            String version = null;
            JSONObject response = new BlockingRequest(context, URL).perform();

            if (response != null && response.has("version")) {
                try {
                    version = response.getString("version");
                } catch (JSONException e) {
                    CKLog.e(TAG, e);
                }
            }

            return version;
        }

        @Override
        protected void onPostExecute(String version) {
            if (listener != null) {
                listener.finished(version);
            }
            context = null;
            listener = null;
        }

        public GetLatestVersionTask setListener(Listener listener) {
            this.listener = listener;
            return this;
        }

        interface Listener {
            void finished(String version);
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
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appId));
            context.startActivity(webIntent);
        }
    }

    public interface OnFragmentInteractionListener extends Preference.OnPreferenceChangeListener {
        void onNestedPreferenceSelected(String key);
    }
}
