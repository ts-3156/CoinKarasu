package com.example.coinkarasu.activities.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.example.coinkarasu.R;

public class PreferencesFragment extends PreferenceFragment {

    private Preference.OnPreferenceChangeListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preferences);

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
