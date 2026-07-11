package com.FinalProject.group3.ui.order;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.FinalProject.group3.databinding.ActivityAddAddressBinding;
import com.FinalProject.group3.model.Address;
import com.FinalProject.group3.repository.AddressRepository;
import com.FinalProject.group3.utils.AddressApiHelper;
import com.FinalProject.group3.utils.InsetsUtil;
import com.FinalProject.group3.utils.ValidationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * DL_Add New Address — thêm mới hoặc sửa địa chỉ nhận hàng.
 * Dropdown cascade: Tỉnh/TP → Quận/Huyện → Phường/Xã (API provinces.open-api.vn).
 * Chỉ ô "Số nhà, tên đường" là nhập tự do.
 */
public class AddAddressActivity extends AppCompatActivity {

    private static final String EXTRA_ADDRESS_ID = "address_id";
    private static final String EXTRA_NAME       = "name";
    private static final String EXTRA_PHONE      = "phone";
    private static final String EXTRA_PROVINCE   = "province";
    private static final String EXTRA_DISTRICT   = "district";
    private static final String EXTRA_WARD       = "ward";
    private static final String EXTRA_DETAIL     = "detail";
    private static final String EXTRA_IS_DEFAULT = "is_default";

    private ActivityAddAddressBinding binding;
    private final AddressRepository addressRepo = new AddressRepository();
    private String editingAddressId;

    // Dữ liệu API đang hiện trong dropdown
    private final List<AddressApiHelper.AdminUnit> provinces  = new ArrayList<>();
    private final List<AddressApiHelper.AdminUnit> districts  = new ArrayList<>();
    private final List<AddressApiHelper.AdminUnit> wards      = new ArrayList<>();

    // Code của đơn vị đang chọn (dùng để fetch cấp dưới)
    private int selectedProvinceCode = -1;
    private int selectedDistrictCode = -1;

    // Pre-fill khi edit (tên trước khi load code từ API)
    private String prefillDistrict;
    private String prefillWard;

    public static Intent intentAdd(Context context) {
        return new Intent(context, AddAddressActivity.class);
    }

    public static Intent intentEdit(Context context, Address a) {
        return new Intent(context, AddAddressActivity.class)
                .putExtra(EXTRA_ADDRESS_ID, a.getAddressId())
                .putExtra(EXTRA_NAME,       a.getName())
                .putExtra(EXTRA_PHONE,      a.getPhone())
                .putExtra(EXTRA_PROVINCE,   a.getProvince())
                .putExtra(EXTRA_DISTRICT,   a.getDistrict())
                .putExtra(EXTRA_WARD,       a.getWard())
                .putExtra(EXTRA_DETAIL,     a.getDetail())
                .putExtra(EXTRA_IS_DEFAULT, a.isDefault());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> save());

        // Chế độ sửa: đổ dữ liệu cũ
        editingAddressId = getIntent().getStringExtra(EXTRA_ADDRESS_ID);
        if (editingAddressId != null) {
            binding.tvTitle.setText("Sửa địa chỉ");
            binding.etName.setText(getIntent().getStringExtra(EXTRA_NAME));
            binding.etPhone.setText(getIntent().getStringExtra(EXTRA_PHONE));
            binding.etDetail.setText(getIntent().getStringExtra(EXTRA_DETAIL));
            binding.swDefault.setChecked(getIntent().getBooleanExtra(EXTRA_IS_DEFAULT, false));
            prefillDistrict = getIntent().getStringExtra(EXTRA_DISTRICT);
            prefillWard     = getIntent().getStringExtra(EXTRA_WARD);
            String prefillProvince = getIntent().getStringExtra(EXTRA_PROVINCE);
            if (prefillProvince != null) binding.actProvince.setText(prefillProvince, false);
            if (prefillDistrict != null) binding.actDistrict.setText(prefillDistrict, false);
            if (prefillWard     != null) binding.actWard.setText(prefillWard, false);
        }

        // Disable district/ward cho đến khi có data
        setDropdownEnabled(binding.actDistrict, false);
        setDropdownEnabled(binding.actWard,     false);

        loadProvinces();
    }

    // ── Load tỉnh từ API ─────────────────────────────────────────────────────
    private void loadProvinces() {
        binding.progressBar.setVisibility(View.VISIBLE);
        AddressApiHelper.fetchProvinces(units -> {
            binding.progressBar.setVisibility(View.GONE);
            provinces.clear();
            provinces.addAll(units);
            bindProvinceDropdown();

            // Nếu đang edit và đã có province, tự động load districts
            String preProvince = binding.actProvince.getText().toString().trim();
            if (!preProvince.isEmpty()) {
                AddressApiHelper.AdminUnit matched = findByName(provinces, preProvince);
                if (matched != null) {
                    selectedProvinceCode = matched.code;
                    loadDistricts(matched.code, prefillDistrict);
                }
            }
        });
    }

    private void bindProvinceDropdown() {
        ArrayAdapter<AddressApiHelper.AdminUnit> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, provinces);
        binding.actProvince.setAdapter(adapter);
        binding.actProvince.setThreshold(0);
        binding.actProvince.setKeyListener(null);
        binding.actProvince.setOnClickListener(v -> binding.actProvince.showDropDown());
        binding.actProvince.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.actProvince.showDropDown();
        });
        binding.actProvince.setOnItemClickListener((parent, view, pos, id) -> {
            AddressApiHelper.AdminUnit selected = provinces.get(pos);
            selectedProvinceCode = selected.code;
            // Reset cấp dưới
            binding.actDistrict.setText("", false);
            binding.actWard.setText("", false);
            districts.clear(); wards.clear();
            setDropdownEnabled(binding.actWard, false);
            loadDistricts(selected.code, null);
        });
    }

    // ── Load quận/huyện ──────────────────────────────────────────────────────
    private void loadDistricts(int provinceCode, String prefillName) {
        setDropdownEnabled(binding.actDistrict, false);
        binding.progressBar.setVisibility(View.VISIBLE);
        AddressApiHelper.fetchDistricts(provinceCode, units -> {
            binding.progressBar.setVisibility(View.GONE);
            districts.clear();
            districts.addAll(units);
            bindDistrictDropdown();
            setDropdownEnabled(binding.actDistrict, true);

            // Auto-fill nếu đang edit
            if (prefillName != null && !prefillName.isEmpty()) {
                AddressApiHelper.AdminUnit matched = findByName(districts, prefillName);
                if (matched != null) {
                    binding.actDistrict.setText(matched.name, false);
                    selectedDistrictCode = matched.code;
                    loadWards(matched.code, prefillWard);
                }
            }
        });
    }

    private void bindDistrictDropdown() {
        ArrayAdapter<AddressApiHelper.AdminUnit> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, districts);
        binding.actDistrict.setAdapter(adapter);
        binding.actDistrict.setThreshold(0);
        binding.actDistrict.setKeyListener(null);
        binding.actDistrict.setOnClickListener(v -> {
            if (!districts.isEmpty()) binding.actDistrict.showDropDown();
        });
        binding.actDistrict.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !districts.isEmpty()) binding.actDistrict.showDropDown();
        });
        binding.actDistrict.setOnItemClickListener((parent, view, pos, id) -> {
            AddressApiHelper.AdminUnit selected = districts.get(pos);
            selectedDistrictCode = selected.code;
            binding.actWard.setText("", false);
            wards.clear();
            setDropdownEnabled(binding.actWard, false);
            loadWards(selected.code, null);
        });
    }

    // ── Load phường/xã ────────────────────────────────────────────────────────
    private void loadWards(int districtCode, String prefillName) {
        setDropdownEnabled(binding.actWard, false);
        binding.progressBar.setVisibility(View.VISIBLE);
        AddressApiHelper.fetchWards(districtCode, units -> {
            binding.progressBar.setVisibility(View.GONE);
            wards.clear();
            wards.addAll(units);
            bindWardDropdown();
            setDropdownEnabled(binding.actWard, true);

            if (prefillName != null && !prefillName.isEmpty()) {
                AddressApiHelper.AdminUnit matched = findByName(wards, prefillName);
                if (matched != null) binding.actWard.setText(matched.name, false);
            }
        });
    }

    private void bindWardDropdown() {
        ArrayAdapter<AddressApiHelper.AdminUnit> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, wards);
        binding.actWard.setAdapter(adapter);
        binding.actWard.setThreshold(0);
        binding.actWard.setKeyListener(null);
        binding.actWard.setOnClickListener(v -> {
            if (!wards.isEmpty()) binding.actWard.showDropDown();
        });
        binding.actWard.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !wards.isEmpty()) binding.actWard.showDropDown();
        });
    }

    // ── Validate + Save ───────────────────────────────────────────────────────
    private void save() {
        String name     = binding.etName.getText().toString().trim();
        String phone    = binding.etPhone.getText().toString().trim();
        String province = binding.actProvince.getText().toString().trim();
        String district = binding.actDistrict.getText().toString().trim();
        String ward     = binding.actWard.getText().toString().trim();
        String detail   = binding.etDetail.getText().toString().trim();

        binding.tilName.setError(null);
        binding.tilPhone.setError(null);
        binding.tilProvince.setError(null);
        binding.tilDistrict.setError(null);
        binding.tilWard.setError(null);
        binding.tilDetail.setError(null);

        boolean valid = true;
        if (name.isEmpty())    { binding.tilName.setError("Không được để trống"); valid = false; }
        if (!ValidationUtils.isValidPhone(phone)) {
            binding.tilPhone.setError("Số điện thoại phải gồm đúng 10 chữ số"); valid = false;
        }
        if (province.isEmpty()) { binding.tilProvince.setError("Chọn Tỉnh/Thành phố"); valid = false; }
        if (district.isEmpty()) { binding.tilDistrict.setError("Chọn Quận/Huyện"); valid = false; }
        if (ward.isEmpty())     { binding.tilWard.setError("Chọn Phường/Xã"); valid = false; }
        if (detail.isEmpty())   { binding.tilDetail.setError("Nhập số nhà, tên đường"); valid = false; }
        if (!valid) return;

        Address address = new Address(name, phone, province, district, ward, detail,
                binding.swDefault.isChecked());
        address.setAddressId(editingAddressId);

        binding.btnSave.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
        addressRepo.saveAddress(address, new AddressRepository.SimpleCallback() {
            @Override public void onSuccess() {
                setResult(Activity.RESULT_OK);
                finish();
            }
            @Override public void onFailure(String error) {
                binding.btnSave.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(AddAddressActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setDropdownEnabled(android.widget.AutoCompleteTextView view, boolean enabled) {
        view.setEnabled(enabled);
        // Dim cả TIL cha để viền + mũi tên cũng xám khi chưa chọn cấp trên
        android.view.View til = (android.view.View) view.getParent().getParent();
        til.setAlpha(enabled ? 1f : 0.4f);
    }

    /** Tìm AdminUnit theo name (case-insensitive, partial match). */
    private static AddressApiHelper.AdminUnit findByName(
            List<AddressApiHelper.AdminUnit> list, String name) {
        if (name == null || name.isEmpty()) return null;
        String lower = name.toLowerCase().trim();
        for (AddressApiHelper.AdminUnit u : list)
            if (u.name.toLowerCase().contains(lower) || lower.contains(u.name.toLowerCase()))
                return u;
        return null;
    }
}
