package com.assassino.thirdeye;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
    private final SettingsActivity settingsActivity;

    public SettingsFragment(SettingsActivity settingsActivity) {
        this.settingsActivity = settingsActivity;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        //Add preference change listener to keyAllowOnline to prompt one-tap UI if not signed in
        SwitchPreferenceCompat keyAllowOnline = findPreference(getResources().getString(R.string.prefKey_allowOnline));
        keyAllowOnline.setOnPreferenceChangeListener((preference, newValue) -> {
            if (!settingsActivity.isSignedIn()) {
                settingsActivity.startOneTapProcess();
            }
            return true;
        });

    }
}
