package com.coinkarasu.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.R;
import com.coinkarasu.utils.ApiKeyUtils;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;
import com.coinkarasu.utils.Tutorial;
import com.coinkarasu.utils.UuidUtils;
import com.coinkarasu.utils.cache.DiskBasedCache;

import java.io.File;
import java.util.Arrays;

public class DebugPreferencesFragment extends PreferenceFragment implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "DebugPreferencesFragment";

    private Preference.OnPreferenceChangeListener listener;

    public DebugPreferencesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_debug);

        findPreference("pref_clear_cache").setOnPreferenceClickListener(this);
        findPreference("pref_clear_config").setOnPreferenceClickListener(this);
        findPreference("pref_show_first_launch_screen").setOnPreferenceChangeListener(this);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

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

        bindPreferenceSummaryToValue(prefs, "pref_toast_level");

        Preference uuidFile = findPreference("pref_uuid_file_exists");
        uuidFile.setSummary(UuidUtils.exists(getActivity()) ? UuidUtils.get(getActivity()) : "No");

        Preference tokenFile = findPreference("pref_token_file_exists");
        tokenFile.setSummary(ApiKeyUtils.exists(getActivity()) ? ApiKeyUtils.get(getActivity()).toString() : ApiKeyUtils.dummy().toString());
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        if (key.equals("pref_clear_cache")) {
            showDialog(R.string.pref_clear_cache_dialog, "OK", true, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    new ClearCacheTask().execute(getActivity().getCacheDir());
                }
            });
        } else if (key.equals("pref_clear_config")) {
            showDialog(R.string.pref_clear_config_dialog, "OK", true, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    PrefHelper.clear(getActivity());
                    getActivity().recreate();
                }
            });
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

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        if (preference.getKey().equals("pref_show_first_launch_screen")) {
            if ((Boolean) value) {
                Tutorial.reset(getActivity());
            }
        }
        return true;
    }

    private static class ClearCacheTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... params) {
            long start = System.currentTimeMillis();
            new DiskBasedCache(params[0]).clear();
            if (DEBUG) CKLog.d(TAG, "Clear cache elapsed time: "
                    + (System.currentTimeMillis() - start) + "ms");
            return null;
        }
    }
}
