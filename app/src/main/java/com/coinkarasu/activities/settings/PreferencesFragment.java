package com.coinkarasu.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import com.coinkarasu.R;
import com.coinkarasu.utils.Log;
import com.coinkarasu.utils.PrefHelper;
import com.coinkarasu.utils.cache.DiskBasedCache;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import java.io.File;

public class PreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "PreferencesFragment";

    private Preference.OnPreferenceChangeListener listener;
    private Log logger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preferences);
        logger = new Log(getActivity());

        Preference prefAppVersion = getPreferenceScreen().findPreference("pref_app_version");
        prefAppVersion.setOnPreferenceClickListener(this);

        Preference prefClearCache = getPreferenceScreen().findPreference("pref_clear_cache");
        prefClearCache.setOnPreferenceClickListener(this);

        Preference prefClearConfig = getPreferenceScreen().findPreference("pref_clear_config");
        prefClearConfig.setOnPreferenceClickListener(this);

        Preference prefOpenSourceLicenses = getPreferenceScreen().findPreference("pref_open_source_licenses");
        prefOpenSourceLicenses.setOnPreferenceClickListener(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        bindPreferenceSummaryToValue(prefs, "pref_sync_frequency");
        bindPreferenceSummaryToValue(prefs, "pref_currency");
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        if (key.equals("pref_app_version")) {
            showDialog(R.string.pref_app_version_up_to_date, false, null);
        } else if (key.equals("pref_clear_cache")) {
            showDialog(R.string.pref_clear_cache_dialog, true, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    new ClearCacheTask(getActivity().getCacheDir()).execute();
                }
            });
        } else if (key.equals("pref_clear_config")) {
            showDialog(R.string.pref_clear_config_dialog, true, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    PrefHelper.clear(getActivity());
                    getActivity().recreate();
                }
            });
        } else if (key.equals("pref_open_source_licenses")) {
            startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));
        }

        return true;
    }

    private void showDialog(int msgResId, boolean isNeedNegativeButton, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msgResId).setPositiveButton("OK", listener);

        if (isNeedNegativeButton) {
            builder.setNegativeButton("CANCEL", null);
        }

        builder.create().show();
    }

    private void bindPreferenceSummaryToValue(SharedPreferences prefs, String key) {
        Preference pref = findPreference(key);
        pref.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(pref, prefs.getString(key, ""));
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
            if (DEBUG) android.util.Log.d(TAG, "Clear cache elapsed time: "
                    + (System.currentTimeMillis() - start) + "ms");
            return null;
        }
    }
}
