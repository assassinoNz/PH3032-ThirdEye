package com.assassino.thirdeye;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SettingsActivity extends AppCompatActivity {
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Start settings fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment(SettingsActivity.this))
                .commit();
        }

        //Get firebase shared auth instance
        mAuth = FirebaseAuth.getInstance();

        //Setup one-tap UI
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                // Your server's client ID, not your Android client ID.
                .setServerClientId(getString(R.string.serverWebClientID))
                // Show all accounts on the device.
                .setFilterByAuthorizedAccounts(false)
                .build()
            ).build();
    }

    @Override
    public void onStart() {
        super.onStart();

        //Check if user is signed in and show one tap UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null && showOneTapUI) {
            //CASE: One-tap UI must be shown
            startOneTapProcess();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                String idToken = credential.getGoogleIdToken();
                if (idToken !=  null) {
                    //CASE: Got an ID token from Google. Use it to authenticate
                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                    mAuth.signInWithCredential(firebaseCredential)
                        .addOnSuccessListener(result -> Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            } catch (ApiException e) {
                switch (e.getStatusCode()) {
                    case CommonStatusCodes.CANCELED:
                        //CASE: User cancelled one-tap UI
                        //Don't re-prompt the user.
                        showOneTapUI = false;
                        break;
                    case CommonStatusCodes.NETWORK_ERROR:
                        Toast.makeText(SettingsActivity.this, "There was a network problem when signing you in", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(SettingsActivity.this, "There was a problem signing you in", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    public void startOneTapProcess() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this, result -> {
                try {
                    startIntentSenderForResult(result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
                            null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    Toast.makeText(SettingsActivity.this, "There was a problem signing you in", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(this, e -> {
                //CASE: No Google Accounts found. Just continue presenting the signed-out UI.
                Toast.makeText(SettingsActivity.this, "Your device doesn't have any Google accounts", Toast.LENGTH_SHORT).show();
            });
    }

    public boolean isSignedIn() {
        return this.mAuth.getCurrentUser() != null;
    }
}