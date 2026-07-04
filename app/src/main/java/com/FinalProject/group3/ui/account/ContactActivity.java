package com.FinalProject.group3.ui.account;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.databinding.ActivityContactBinding;
import com.FinalProject.group3.utils.InsetsUtil;

public class ContactActivity extends AppCompatActivity {

    public static Intent intent(Context context) {
        return new Intent(context, ContactActivity.class);
    }

    private ActivityContactBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        binding.rowSales.setOnClickListener(v -> dialPhone("19001182"));
        binding.rowComplaint.setOnClickListener(v -> dialPhone("19001188"));
        binding.rowEmail.setOnClickListener(v -> sendEmail("glassity4u@gmail.com"));
    }

    private void dialPhone(String number) {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number)));
        } catch (Exception e) {
            Toast.makeText(this, "Không thể gọi điện: " + number, Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail(String email) {
        try {
            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
            i.putExtra(Intent.EXTRA_SUBJECT, "Liên hệ hỗ trợ - Glassity");
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Không tìm thấy ứng dụng email", Toast.LENGTH_SHORT).show();
        }
    }
}
