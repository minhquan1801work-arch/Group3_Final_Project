package com.FinalProject.group3.ui.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.FinalProject.group3.R;
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

        binding.rowLanguage.setOnClickListener(v -> showLanguagePicker());

        binding.rowBiometric.setOnClickListener(v -> {
            Intent biometric = new Intent(Settings.ACTION_SECURITY_SETTINGS);
            startActivity(biometric);
        });
    }

    // Đổi ngôn ngữ trong app (không phụ thuộc ngôn ngữ hệ thống) — chỉ áp dụng đủ
    // cho luồng Auth/Cart/Checkout/Payment, các màn khác vẫn tiếng Việt.
    private void showLanguagePicker() {
        String[] labels = { getString(R.string.settings_language_vi), getString(R.string.settings_language_en) };
        String[] tags = { "vi", "en" };

        LocaleListCompat current = AppCompatDelegate.getApplicationLocales();
        int checkedIndex = (!current.isEmpty() && "en".equals(current.get(0).getLanguage())) ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_language_dialog_title)
                .setSingleChoiceItems(labels, checkedIndex, (dialog, which) -> {
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tags[which]));
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
