package com.FinalProject.group3.ui.order;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.FinalProject.group3.databinding.ActivityAddressListBinding;
import com.FinalProject.group3.databinding.ItemAddressBinding;
import com.FinalProject.group3.model.Address;
import com.FinalProject.group3.repository.AddressRepository;
import com.FinalProject.group3.utils.InsetsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * LA.Address — Sổ địa chỉ.
 * Luồng logic chọn:
 *  - Người mới (0 địa chỉ) → tự mở form Thêm địa chỉ mới, địa chỉ đầu = mặc định.
 *  - Đã có địa chỉ → tick sẵn địa chỉ mặc định.
 *  - Bấm chọn 1 địa chỉ → trả kết quả về Checkout (RESULT_OK + dữ liệu địa chỉ).
 */
public class AddressListActivity extends AppCompatActivity {

    public static final String RESULT_NAME = "name";
    public static final String RESULT_PHONE = "phone";
    public static final String RESULT_FULL_ADDRESS = "full_address";

    private ActivityAddressListBinding binding;
    private final AddressRepository addressRepo = new AddressRepository();
    private final List<Address> addresses = new ArrayList<>();
    private AddressAdapter adapter;
    private int selectedPos = -1;
    private boolean autoOpenedAddForm = false;

    private final ActivityResultLauncher<Intent> addEditLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) loadAddresses();
            });

    public static Intent intent(Context context) {
        return new Intent(context, AddressListActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddressListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        InsetsUtil.applySystemBarsPadding(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnAddAddress.setOnClickListener(v ->
                addEditLauncher.launch(AddAddressActivity.intentAdd(this)));

        adapter = new AddressAdapter();
        binding.rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAddresses.setAdapter(adapter);

        loadAddresses();
    }

    private void loadAddresses() {
        binding.progressBar.setVisibility(View.VISIBLE);
        addressRepo.getAddresses(new AddressRepository.ListCallback() {
            @Override
            public void onSuccess(List<Address> list) {
                binding.progressBar.setVisibility(View.GONE);
                addresses.clear();
                addresses.addAll(list);

                // Tick sẵn địa chỉ mặc định (repo đã xếp mặc định lên đầu)
                selectedPos = addresses.isEmpty() ? -1 : 0;
                adapter.notifyDataSetChanged();
                binding.tvEmpty.setVisibility(addresses.isEmpty() ? View.VISIBLE : View.GONE);

                // Người mới chưa có địa chỉ → mở luôn form thêm (chỉ tự mở 1 lần)
                if (addresses.isEmpty() && !autoOpenedAddForm) {
                    autoOpenedAddForm = true;
                    addEditLauncher.launch(AddAddressActivity.intentAdd(AddressListActivity.this));
                }
            }

            @Override
            public void onFailure(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(AddressListActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Chọn địa chỉ → trả về Checkout. */
    private void returnSelected(Address a) {
        Intent data = new Intent()
                .putExtra(RESULT_NAME, a.getName())
                .putExtra(RESULT_PHONE, a.getPhone())
                .putExtra(RESULT_FULL_ADDRESS, a.fullAddress());
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemAddressBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Address a = addresses.get(position);
            holder.binding.tvName.setText(a.getName());
            holder.binding.tvPhone.setText(a.getPhone());
            holder.binding.tvFullAddress.setText(a.fullAddress());
            holder.binding.tvDefaultTag.setVisibility(a.isDefault() ? View.VISIBLE : View.GONE);
            holder.binding.rbSelected.setChecked(position == selectedPos);

            holder.binding.getRoot().setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                Address selected = addresses.get(pos);
                // Cập nhật tag mặc định trong danh sách local ngay (UI responsive)
                for (int i = 0; i < addresses.size(); i++)
                    addresses.get(i).setDefault(i == pos);
                selectedPos = pos;
                notifyDataSetChanged();
                // Ghi mặc định mới lên Firestore (async, không block UI)
                addressRepo.setDefaultAddress(selected.getAddressId(),
                        new AddressRepository.SimpleCallback() {
                            @Override public void onSuccess() {}
                            @Override public void onFailure(String error) {}
                        });
                returnSelected(selected);
            });
            holder.binding.btnEdit.setOnClickListener(v ->
                    addEditLauncher.launch(AddAddressActivity.intentEdit(
                            AddressListActivity.this, a)));
        }

        @Override
        public int getItemCount() { return addresses.size(); }

        class VH extends RecyclerView.ViewHolder {
            final ItemAddressBinding binding;
            VH(ItemAddressBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
