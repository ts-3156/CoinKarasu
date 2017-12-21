package com.example.coinkarasu.activities.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

import com.example.coinkarasu.R;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

public class PreferencesFragment extends PreferenceFragment {

    private Preference.OnPreferenceChangeListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preferences);

        Preference prefAppVersion = getPreferenceScreen().findPreference("pref_app_version");
        prefAppVersion.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.pref_app_version_up_to_date)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder.create().show();

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
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

}
