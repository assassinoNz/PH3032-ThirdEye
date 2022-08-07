package com.assassino.thirdeye;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private ImageAnalysis imageAnalysis;
    private ThirdEyeImageAnalyzer thirdEyeImageAnalyzer;
    private Preview preview;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private CameraSelector cameraSelector;

    private FirebaseAuth mAuth;

    private TextView txtOutResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get firebase auth instance
        mAuth = FirebaseAuth.getInstance();

        //Setup text views
        txtOutResult = findViewById(R.id.txtOutResult);

        //Continue onCreate based on permissions
        if (hasBasicPermissions()) {
            //CASE: App has all required permissions

            //Build analyze use case
            imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

            //Build preview use case
            preview = new Preview.Builder().build();
            PreviewView previewView = findViewById(R.id.viewFinder);
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            //Setup camera options
            cameraProviderFuture = ProcessCameraProvider.getInstance(MainActivity.this);
            cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

            //Bind imageAnalysis use case
            try {
                Camera camera = cameraProviderFuture.get().bindToLifecycle(MainActivity.this, cameraSelector, imageAnalysis);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(getResources().getString(R.string.tag), e.getMessage());
            }

            //Setup image analyzer
            thirdEyeImageAnalyzer = new ThirdEyeImageAnalyzer(MainActivity.this);

            //Setup buttons
            Button btnAnalyze = findViewById(R.id.btnAnalyze);
            btnAnalyze.setOnClickListener(view -> imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(MainActivity.this), thirdEyeImageAnalyzer));
        } else {
            //CASE: App doesn't have all required permissions
            Intent intent = new Intent(MainActivity.this, SetupActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Check if camera preview is needed and bind/unbind that use case accordingly
        boolean allowedCameraPreview = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.prefKey_enableCameraPreview), false);
        try {
            if (allowedCameraPreview) {
                //CASE: Camera preview is enabled
                cameraProviderFuture.get().bindToLifecycle(MainActivity.this, cameraSelector, preview);
            } else {
                //CASE: Camera preview is disabled
                cameraProviderFuture.get().unbind(preview);
            }
        } catch (ExecutionException | InterruptedException e) {
            Log.e(getResources().getString(R.string.tag), e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnuSettings: {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }

            case R.id.mnuAbout: {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
                return true;
            }

            case R.id.mnuHelp: {
                Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent);
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean hasBasicPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    boolean isOnlineAllowed() {
        ConnectivityManager manager = getSystemService(ConnectivityManager.class);
        Network currentNetwork = manager.getActiveNetwork();

        if (currentNetwork != null) {
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(currentNetwork);
            boolean hasNetwork = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.prefKey_allowOnline), true)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            boolean signedIn = this.mAuth.getCurrentUser() != null;

            return hasNetwork && signedIn;
        } else {
            return false;
        }
    }

    void resetAnalyzer() {
        this.imageAnalysis.clearAnalyzer();
    }

    void updateUIWithResult(boolean isSuccess, String result) {
        this.txtOutResult.setText(result);

        boolean allowedVibration = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.prefKey_allowVibrationFeedback), true);
        boolean allowedVoice;
        if (isSuccess) {
            if (allowedVibration) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        } else {
            if (allowedVibration) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }
    }
}