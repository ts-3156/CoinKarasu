package com.coinkarasu.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

import com.coinkarasu.R;
import com.coinkarasu.utils.DiskCacheHelper;
import com.coinkarasu.utils.Log;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

public class PreferencesFragment extends PreferenceFragment {
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
        prefAppVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showDialog(R.string.pref_app_version_up_to_date, false, null);

                return true;
            }
        });

        Preference prefClearCache = getPreferenceScreen().findPreference("pref_clear_cache");
        prefClearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                showDialog(R.string.pref_clear_cache_dialog, true, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        long start = System.currentTimeMillis();
                        DiskCacheHelper.clear(getActivity());
                        if (DEBUG) logger.d(TAG, "Clear cache elapsed time: "
                                + (System.currentTimeMillis() - start) + "ms");
                    }
                });

                return true;
            }
        });

        Preference prefOpenSourceLicenses = getPreferenceScreen().findPreference("pref_open_source_licenses");
        prefOpenSourceLicenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), OssLicensesMenuActivity.class));
                return true;
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        bindPreferenceSummaryToValue(prefs, "pref_sync_frequency");
        bindPreferenceSummaryToValue(prefs, "pref_currency");
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

}
