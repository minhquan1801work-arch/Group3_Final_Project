package com.FinalProject.group3.ui.account;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.databinding.ActivityAccountInfoBinding;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.FinalProject.group3.utils.InsetsUtil;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * LA.Personal1/2 — Thông tin tài khoản.
 * Chưa đăng nhập → hiện empty state + nút đăng nhập.
 * Đã đăng nhập → form sửa tên, SĐT, giới tính, ngày sinh.
 */
public class AccountInfoActivity extends AppCompatActivity {

    private ActivityAccountInfoBinding binding;
    private String selectedBirthday = "";

    public static Intent intent(Context context) {
        return new Intent(context, AccountInfoActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            // LA.Personal2 — khách chưa đăng nhập
            binding.layoutGuest.setVisibility(View.VISIBLE);
            binding.scrollContent.setVisibility(View.GONE);
            binding.btnLoginGuest.setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)));
            return;
        }

        // LA.Personal1 — đã đăng nhập
        setupGenderSpinner();
        setupDatePicker();
        loadCustomerData(user);

        binding.btnUpdate.setOnClickListener(v -> saveChanges(user.getUid()));

        binding.rowChangePassword.setOnClickListener(v -> {
            String email = user.getEmail();
            if (email != null) {
                FirebaseHelper.getAuth().sendPasswordResetEmail(email)
                        .addOnSuccessListener(t ->
                                Toast.makeText(this,
                                        "Đã gửi email đổi mật khẩu đến " + email,
                                        Toast.LENGTH_LONG).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this,
                        "Tài khoản Google không hỗ trợ đổi mật khẩu qua đây",
                        Toast.LENGTH_SHORT).show();
            }
        });

        binding.rowLogout.setOnClickListener(v -> {
            FirebaseHelper.signOut();
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void setupGenderSpinner() {
        String[] genders = {"Nam", "Nữ", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGender.setAdapter(adapter);
    }

    private void setupDatePicker() {
        View.OnClickListener pick = v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (picker, year, month, day) -> {
                selectedBirthday = String.format(java.util.Locale.US, "%02d/%02d/%04d",
                        day, month + 1, year);
                binding.tvBirthday.setText(selectedBirthday);
            }, c.get(Calendar.YEAR) - 20, c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)).show();
        };
        binding.tvBirthday.setOnClickListener(pick);
        binding.btnPickDate.setOnClickListener(pick);
    }

    private void loadCustomerData(FirebaseUser user) {
        binding.progressBar.setVisibility(View.VISIBLE);
        FirebaseHelper.getDb().collection(FirebaseHelper.COL_CUSTOMERS)
                .document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (doc.exists()) {
                        binding.etName.setText(doc.getString("name"));
                        binding.etPhone.setText(doc.getString("phone"));
                        String birthday = doc.getString("birthday");
                        if (birthday != null && !birthday.isEmpty()) {
                            binding.tvBirthday.setText(birthday);
                            selectedBirthday = birthday;
                        }
                        String gender = doc.getString("gender");
                        if ("Nữ".equals(gender)) binding.spinnerGender.setSelection(1);
                        else if ("Khác".equals(gender)) binding.spinnerGender.setSelection(2);
                    }
                    binding.etEmail.setText(user.getEmail());
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.etEmail.setText(user.getEmail());
                });
    }

    private void saveChanges(String uid) {
        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String gender = binding.spinnerGender.getSelectedItem().toString();

        if (name.isEmpty()) {
            binding.etName.setError("Vui lòng nhập họ và tên");
            return;
        }

        binding.btnUpdate.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", phone);
        updates.put("gender", gender);
        if (!selectedBirthday.isEmpty()) updates.put("birthday", selectedBirthday);

        FirebaseHelper.getDb().collection(FirebaseHelper.COL_CUSTOMERS)
                .document(uid).update(updates)
                .addOnSuccessListener(v -> {
                    binding.btnUpdate.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.btnUpdate.setEnabled(true);
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
