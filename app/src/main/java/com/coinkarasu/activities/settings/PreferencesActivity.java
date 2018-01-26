package com.coinkarasu.activities.settings;

import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.coinkarasu.BuildConfig;
import com.coinkarasu.R;
import com.coinkarasu.activities.etc.NavigationKind;
import com.coinkarasu.billingmodule.BillingActivity;
import com.coinkarasu.tasks.InitializeThirdPartyAppsTask;
import com.coinkarasu.tasks.InsertLaunchEventTask;
import com.coinkarasu.utils.CKLog;
import com.coinkarasu.utils.PrefHelper;
import com.google.firebase.analytics.FirebaseAnalytics;

public class PreferencesActivity extends AppCompatActivity implements
        PreferencesFragment.OnFragmentInteractionListener,
        InitializeThirdPartyAppsTask.FirebaseAnalyticsReceiver {

    private static final boolean DEBUG = CKLog.DEBUG;
    private static final String TAG = "PreferencesActivity";

    private FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        CKLog.setContext(this);
        new InsertLaunchEventTask().execute(this);
        new InitializeThirdPartyAppsTask().execute(this);

        updateToolbarTitle(R.string.pref_title);
        updateToolbarColor();

        PreferenceManager.setDefaultValues(this, R.xml.fragment_preferences, false);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content, new PreferencesFragment())
                    .commit();
        }
    }

    @Override
    public void onNestedPreferenceSelected(String key) {
        Fragment fragment = null;

        if (BuildConfig.DEBUG && key.equals("pref_debug")) {
            fragment = new DebugPreferencesFragment();
        }

        if (fragment != null) {
            updateToolbarTitle(R.string.pref_header_debug);

            getFragmentManager().beginTransaction()
                    .replace(R.id.content, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            updateToolbarTitle(R.string.pref_title);
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                    interval = PrefHelper.setDefaultSyncInterval(this);
                    listPreference.setValueIndex(listPreference.findIndexOfValue(String.valueOf(interval)));

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

    private void updateToolbarTitle(int resId) {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getString(resId));
        }
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

    @Override
    public void onResume() {
        super.onResume();
        CKLog.setContext(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        CKLog.releaseContext();
    }

    public void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics) {
        this.firebaseAnalytics = firebaseAnalytics;
    }
}
