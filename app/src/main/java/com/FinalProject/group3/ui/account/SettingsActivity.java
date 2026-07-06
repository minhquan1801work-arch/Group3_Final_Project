package com.FinalProject.group3.ui.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.databinding.ActivitySettingsBinding;
import com.FinalProject.group3.utils.InsetsUtil;

public class SettingsActivity extends AppCompatActivity {

    public static Intent intent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.rowNotification.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        });

        binding.rowLocation.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));

        binding.rowLanguage.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_LOCALE_SETTINGS)));

        binding.rowBiometric.setOnClickListener(v -> {
            Intent biometric = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            startActivity(biometric);
        });
    }
}
