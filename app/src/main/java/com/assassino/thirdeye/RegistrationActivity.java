package com.assassino.thirdeye;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

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

public class RegistrationActivity extends AppCompatActivity {
//    private SignInClient oneTapClient;
//    private BeginSignInRequest signInRequest;
//
//    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
//    private boolean showOneTapUI = true;
//
//    private FirebaseAuth mAuth;
//    private EditText txtInEmail;
//    private EditText txtInPassword;
//
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_registration);
////
////        txtInEmail = findViewById(R.id.txtInEmail);
////        txtInPassword = findViewById(R.id.txtInPassword);
////        Button btnSignIn = findViewById(R.id.btnSignIn);
////        Button btnSignUp = findViewById(R.id.btnSignUp);
////
////        mAuth = FirebaseAuth.getInstance();
////
////        btnSignUp.setOnClickListener(view -> {
////            if (TextUtils.isEmpty(txtInEmail.getText().toString()) || TextUtils.isEmpty(txtInPassword.getText().toString())) {
////                Toast.makeText(RegistrationActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
////            } else {
////                attemptSignUpAndSignIn();
////            }
////        });
////
////        btnSignIn.setOnClickListener(view -> {
////            if (TextUtils.isEmpty(txtInEmail.getText().toString()) || TextUtils.isEmpty(txtInPassword.getText().toString())) {
////                Toast.makeText(RegistrationActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
////            } else {
////                attemptSignIn();
////            }
////        });
////    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_registration);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        oneTapClient = Identity.getSignInClient(this);
//        signInRequest = BeginSignInRequest.builder()
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                .setSupported(true)
//                // Your server's client ID, not your Android client ID.
//                .setServerClientId(getString(R.string.serverWebClientID))
//                // Show all accounts on the device.
//                .setFilterByAuthorizedAccounts(false)
//                .build()
//            ).build();
//
//        if (isContinuationAllowed()) {
//
//        }
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        // Check if user is signed in (non-null) and show one tap UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser == null && showOneTapUI) {
//            oneTapClient.beginSignIn(signInRequest)
//                .addOnSuccessListener(this, result -> {
//                    try {
//                        startIntentSenderForResult(result.getPendingIntent().getIntentSender(), REQ_ONE_TAP,
//                                null, 0, 0, 0);
//                    } catch (IntentSender.SendIntentException e) {
//                        Log.e(getResources().getString(R.string.tag), "Couldn't start One Tap UI: " + e.getLocalizedMessage());
//                    }
//                })
//                .addOnFailureListener(this, e -> {
//                    // No Google Accounts found. Just continue presenting the signed-out UI.
//                    Log.d(getResources().getString(R.string.tag), e.getLocalizedMessage());
//                });
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == REQ_ONE_TAP) {
//            try {
//                SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
//                String idToken = credential.getGoogleIdToken();
//                if (idToken !=  null) {
//                    //CASE: Got an ID token from Google. Use it to authenticate
//                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
//                    mAuth.signInWithCredential(firebaseCredential)
//                        .addOnSuccessListener(result -> Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show())
//                        .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
//                }
//            } catch (ApiException e) {
//                switch (e.getStatusCode()) {
//                    case CommonStatusCodes.CANCELED:
//                        Log.d(getResources().getString(R.string.tag), "One-tap dialog was closed.");
//                        // Don't re-prompt the user.
//                        showOneTapUI = false;
//                        break;
//                    case CommonStatusCodes.NETWORK_ERROR:
//                        Log.d(getResources().getString(R.string.tag), "One-tap encountered a network error.");
//                        // Try again or just ignore.
//                        break;
//                    default:
//                        Log.d(getResources().getString(R.string.tag), "Couldn't get credential from result."
//                            + e.getLocalizedMessage());
//                        break;
//                }
//
//            }
//        }
//    }
//
//
//    private boolean isContinuationAllowed() {
//        return showOneTapUI;
//    }
//
//    private void attemptSignUpAndSignIn() {
//        mAuth.createUserWithEmailAndPassword(txtInEmail.getText().toString(), txtInPassword.getText().toString())
//            .addOnSuccessListener(result -> {
//                Toast.makeText(RegistrationActivity.this, "Successfully signed up", Toast.LENGTH_SHORT).show();
//                attemptSignIn();
//            })
//            .addOnFailureListener(e -> Toast.makeText(RegistrationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
//    }
//
//    private void attemptSignIn() {
//        mAuth.signInWithEmailAndPassword(txtInEmail.getText().toString(), txtInPassword.getText().toString())
//            .addOnSuccessListener(result -> {
//                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
//                editor.putString("keyEmail",  txtInEmail.getText().toString());
//                editor.putString("keyPassword",  txtInPassword.getText().toString());
//                editor.apply();
//                Toast.makeText(RegistrationActivity.this, "Successfully signed in", Toast.LENGTH_SHORT).show();
//            })
//            .addOnFailureListener(e -> Toast.makeText(RegistrationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
//    }
}