package com.FinalProject.group3.ui.order;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.databinding.ActivityAddAddressBinding;
import com.FinalProject.group3.model.Address;
import com.FinalProject.group3.repository.AddressRepository;
import com.FinalProject.group3.utils.InsetsUtil;

/**
 * DL_Add New Address — thêm mới hoặc sửa địa chỉ nhận hàng.
 * Mở bằng startActivityForResult (từ AddressListActivity) — RESULT_OK khi lưu xong.
 */
public class AddAddressActivity extends AppCompatActivity {

    private static final String EXTRA_ADDRESS_ID = "address_id";
    private static final String EXTRA_NAME = "name";
    private static final String EXTRA_PHONE = "phone";
    private static final String EXTRA_PROVINCE = "province";
    private static final String EXTRA_WARD = "ward";
    private static final String EXTRA_DETAIL = "detail";
    private static final String EXTRA_IS_DEFAULT = "is_default";

    private ActivityAddAddressBinding binding;
    private final AddressRepository addressRepo = new AddressRepository();
    private String editingAddressId; // null = thêm mới

    public static Intent intentAdd(Context context) {
        return new Intent(context, AddAddressActivity.class);
    }

    /** Mở ở chế độ SỬA — truyền sẵn dữ liệu địa chỉ cũ qua Intent. */
    public static Intent intentEdit(Context context, Address a) {
        return new Intent(context, AddAddressActivity.class)
                .putExtra(EXTRA_ADDRESS_ID, a.getAddressId())
                .putExtra(EXTRA_NAME, a.getName())
                .putExtra(EXTRA_PHONE, a.getPhone())
                .putExtra(EXTRA_PROVINCE, a.getProvince())
                .putExtra(EXTRA_WARD, a.getWard())
                .putExtra(EXTRA_DETAIL, a.getDetail())
                .putExtra(EXTRA_IS_DEFAULT, a.isDefault());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());

        // Dropdown 34 tỉnh/thành
        binding.actProvince.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                getResources().getStringArray(com.FinalProject.group3.R.array.provinces)));

        // Chế độ sửa: đổ dữ liệu cũ
        editingAddressId = getIntent().getStringExtra(EXTRA_ADDRESS_ID);
        if (editingAddressId != null) {
            binding.tvTitle.setText("Sửa địa chỉ");
            binding.etName.setText(getIntent().getStringExtra(EXTRA_NAME));
            binding.etPhone.setText(getIntent().getStringExtra(EXTRA_PHONE));
            binding.actProvince.setText(getIntent().getStringExtra(EXTRA_PROVINCE), false);
            binding.etWard.setText(getIntent().getStringExtra(EXTRA_WARD));
            binding.etDetail.setText(getIntent().getStringExtra(EXTRA_DETAIL));
            binding.swDefault.setChecked(getIntent().getBooleanExtra(EXTRA_IS_DEFAULT, false));
        }

        binding.btnSave.setOnClickListener(v -> save());
    }

    private void save() {
        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String province = binding.actProvince.getText().toString().trim();
        String ward = binding.etWard.getText().toString().trim();
        String detail = binding.etDetail.getText().toString().trim();

        binding.tilName.setError(null);
        binding.tilPhone.setError(null);
        binding.tilProvince.setError(null);
        binding.tilDetail.setError(null);

        boolean valid = true;
        if (name.isEmpty()) { binding.tilName.setError("Không được để trống"); valid = false; }
        if (phone.length() < 9) { binding.tilPhone.setError("Số điện thoại không hợp lệ"); valid = false; }
        if (province.isEmpty()) { binding.tilProvince.setError("Chọn Tỉnh/Thành phố"); valid = false; }
        if (detail.isEmpty()) { binding.tilDetail.setError("Không được để trống"); valid = false; }
        if (!valid) return;

        Address address = new Address(name, phone, province, ward, detail,
                binding.swDefault.isChecked());
        address.setAddressId(editingAddressId);

        binding.btnSave.setEnabled(false);
        binding.progressBar.setVisibility(android.view.View.VISIBLE);
        addressRepo.saveAddress(address, new AddressRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                setResult(Activity.RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(String error) {
                binding.btnSave.setEnabled(true);
                binding.progressBar.setVisibility(android.view.View.GONE);
                Toast.makeText(AddAddressActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
