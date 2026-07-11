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

    private static final String ADDRESS =
            "669 QL1A, Khu phố 6, Phường Linh Xuân, Thủ Đức, TP. Hồ Chí Minh";

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

        binding.rowSales.setOnClickListener(v -> dialPhone("18001162"));
        binding.rowComplaint.setOnClickListener(v -> dialPhone("18001160"));
        binding.rowEmail.setOnClickListener(v -> sendEmail("glassity4u@gmail.com"));

        setupMap();
    }

    private void setupMap() {
        binding.frameMap.setOnClickListener(v -> {
            try {
                String encoded = Uri.encode(ADDRESS);
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=" + encoded));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Không mở được Google Maps", Toast.LENGTH_SHORT).show();
            }
        });
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
