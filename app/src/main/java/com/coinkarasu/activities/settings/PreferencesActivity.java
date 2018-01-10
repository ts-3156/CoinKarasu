package com.coinkarasu.activities.settings;

import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.RingtonePreference;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;

import com.coinkarasu.R;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.billingmodule.BillingActivity;
import com.coinkarasu.utils.PrefHelper;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import io.fabric.sdk.android.Fabric;

public class PreferencesActivity extends AppCompatActivity implements Preference.OnPreferenceChangeListener {

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(R.layout.activity_preferences);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getString(R.string.pref_title));
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.content, new PreferencesFragment())
                .commit();

        updateToolbarColor();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            if (listPreference.getKey().equals("pref_sync_frequency")) {
                int interval = Integer.valueOf(stringValue);
                if (!PrefHelper.isPremium(this) && interval < PrefHelper.MIN_SYNC_INTERVAL) {
                    PrefHelper.setDefaultSyncInterval(this);

                    BillingActivity.start(this, R.string.billing_dialog_sync_interval);
                    return true;
                }
            }

            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

        } else if (preference instanceof RingtonePreference) {
            if (TextUtils.isEmpty(stringValue)) {
                preference.setSummary(R.string.pref_ringtone_silent);

            } else {
                Ringtone ringtone = RingtoneManager.getRingtone(
                        preference.getContext(), Uri.parse(stringValue));

                if (ringtone == null) {
                    preference.setSummary(null);
                } else {
                    String name = ringtone.getTitle(preference.getContext());
                    preference.setSummary(name);
                }
            }

        } else {
            preference.setSummary(stringValue);
        }
        return true;
    }

    private void updateToolbarColor() {
        NavigationKind kind = NavigationKind.edit_tabs;
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(kind.colorResId)));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, kind.colorDarkResId));
        }
    }
}
