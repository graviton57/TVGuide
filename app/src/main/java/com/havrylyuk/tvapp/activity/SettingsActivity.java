package com.havrylyuk.tvapp.activity;


import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.havrylyuk.tvapp.R;
import com.havrylyuk.tvapp.data.local.TvContract;
import com.havrylyuk.tvapp.sync.TvGuideSyncAdapter;

import java.util.List;

/**
 *
 *  Created by Igor Havrylyuk on 29.01.2017.
 */
public class SettingsActivity extends AbstractPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences sharedPrefs;

    private static Preference.OnPreferenceChangeListener sBindPrefSummaryToValueListener
                = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };


    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPrefSummaryToValueListener);
        sBindPrefSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                                .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onPause() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();

    }

    @Override
    protected void onResume() {
        sharedPrefs.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_auto_sync_interval_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_schedule_days_count_key)));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (TextUtils.equals(getString(R.string.pref_auto_sync), key)) {
            if (prefs.getBoolean(key, false)) {
                final long interval = Long.parseLong(prefs.getString(
                        getString(R.string.pref_auto_sync_interval_key),
                        getString(R.string.pref_auto_sync_interval_default_value)
                ));
                final long flexTime = interval/ TvGuideSyncAdapter.FLEX_DIVIDER;
                TvGuideSyncAdapter.configurePeriodicSync(getApplicationContext(),interval,flexTime);
            } else {
                ContentResolver.removePeriodicSync(
                        TvGuideSyncAdapter.getSyncAccount(getApplicationContext()),
                        getResources().getString(R.string.content_authority), new Bundle());
            }
        } else if (TextUtils.equals(getString(R.string.pref_auto_sync_interval_key), key)) {
            if (findPreference(key) != null) {
                final ListPreference interval = (ListPreference) findPreference(key);
                interval.setSummary(interval.getEntry());
                TvGuideSyncAdapter.configurePeriodicSync(
                        getApplicationContext(),
                        Long.parseLong(interval.getValue()),
                        Long.parseLong(interval.getValue())/ TvGuideSyncAdapter.FLEX_DIVIDER);
                TvGuideSyncAdapter.syncImmediately(getApplicationContext());
            }
        } else if (TextUtils.equals(getString(R.string.pref_schedule_days_count_key), key)) {
            getContentResolver().delete(TvContract.ProgramEntry.CONTENT_URI, null, null);
            TvGuideSyncAdapter.syncImmediately(getApplicationContext());
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
