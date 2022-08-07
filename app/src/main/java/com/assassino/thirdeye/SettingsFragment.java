package com.assassino.thirdeye;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
    private final SettingsActivity settingsActivity;

    public SettingsFragment(SettingsActivity settingsActivity) {
        this.settingsActivity = settingsActivity;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        //Add preference click listener to keySignIn to sign out if signed in or prompt one-tap UI if not signed in
        Preference keySignIn = findPreference(getResources().getString(R.string.prefKey_signIn));
        keySignIn.setOnPreferenceClickListener(preference -> {
            if (settingsActivity.isSignedIn()) {
                settingsActivity.signOut();
            } else {
                settingsActivity.startOneTapProcess();
            }

            //Update UI to reflect the signed in status
            updateUI();
            return true;
        });

        //Update UI to reflect the signed in status
        updateUI();
    }

    private void updateUI() {
        Preference keySignIn = findPreference(getResources().getString(R.string.prefKey_signIn));
        if (settingsActivity.isSignedIn()) {
            keySignIn.setSummary(R.string.prefKey_keySignIn_summaryTrue);
        } else {
            keySignIn.setSummary(R.string.prefKey_keySignIn_summaryFalse);
        }
    }
}
