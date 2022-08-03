package com.assassino.thirdeye;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class SetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        //Setup permission launcher
        ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::continueIfPermissionsGranted
        );

        //Setup buttons
        Button btnGrantPermissions = findViewById(R.id.btnGrantPermissions);
        btnGrantPermissions.setOnClickListener(view -> permissionLauncher.launch(Manifest.permission.CAMERA));
    }

    private void continueIfPermissionsGranted(boolean hasPermissions) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(SetupActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(SetupActivity.this, "Please grant permissions", Toast.LENGTH_SHORT).show();
        }
    }
}