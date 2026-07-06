package com.FinalProject.group3.ui.order;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
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
 * LA.Address — Lựa chọn địa chỉ.
 * - Nút "Thêm địa chỉ mới" (outline) ở trên.
 * - Swipe trái → xác nhận xóa địa chỉ.
 * - Nút "Tiếp tục thanh toán" (đen) ở dưới → trả kết quả về Checkout.
 */
public class AddressListActivity extends AppCompatActivity {

    public static final String RESULT_NAME         = "name";
    public static final String RESULT_PHONE        = "phone";
    public static final String RESULT_FULL_ADDRESS = "full_address";
    public static final String RESULT_ADDRESS_ID   = "address_id";
    private static final String EXTRA_SELECTED_ID  = "selected_id";

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

    public static Intent intentWithSelected(Context context, String selectedAddressId) {
        return new Intent(context, AddressListActivity.class)
                .putExtra(EXTRA_SELECTED_ID, selectedAddressId);
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
        binding.btnConfirm.setOnClickListener(v -> confirmSelection());

        adapter = new AddressAdapter();
        binding.rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAddresses.setAdapter(adapter);

        // Swipe trái → xóa địa chỉ
        new ItemTouchHelper(new SwipeToDeleteCallback()).attachToRecyclerView(binding.rvAddresses);

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

                String preId = getIntent().getStringExtra(EXTRA_SELECTED_ID);
                selectedPos = -1;
                if (preId != null) {
                    for (int i = 0; i < addresses.size(); i++) {
                        if (preId.equals(addresses.get(i).getAddressId())) {
                            selectedPos = i; break;
                        }
                    }
                }
                if (selectedPos == -1 && !addresses.isEmpty()) selectedPos = 0;
                adapter.notifyDataSetChanged();

                boolean empty = addresses.isEmpty();
                binding.tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                binding.btnConfirm.setVisibility(empty ? View.GONE : View.VISIBLE);

                if (empty && !autoOpenedAddForm) {
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

    private void confirmSelection() {
        if (selectedPos < 0 || selectedPos >= addresses.size()) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }
        returnSelected(addresses.get(selectedPos));
    }

    private void returnSelected(Address a) {
        Intent data = new Intent()
                .putExtra(RESULT_NAME, a.getName())
                .putExtra(RESULT_PHONE, a.getPhone())
                .putExtra(RESULT_FULL_ADDRESS, a.fullAddress())
                .putExtra(RESULT_ADDRESS_ID, a.getAddressId());
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    // ── Swipe-to-delete ───────────────────────────────────────────────────────
    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        private final Paint paint = new Paint();

        SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT);
            paint.setColor(Color.parseColor("#D32F2F")); // đỏ
            paint.setAntiAlias(true);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                              @NonNull RecyclerView.ViewHolder target) { return false; }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            Address a = addresses.get(pos);
            // Khôi phục item ngay để tránh blank row trong lúc user chưa xác nhận
            adapter.notifyItemChanged(pos);
            new AlertDialog.Builder(AddressListActivity.this)
                    .setTitle("Xóa địa chỉ")
                    .setMessage("Xóa địa chỉ \"" + a.getName() + "\"?")
                    .setPositiveButton("Xóa", (d, w) ->
                            addressRepo.deleteAddress(a.getAddressId(),
                                    new AddressRepository.SimpleCallback() {
                                        @Override public void onSuccess() { loadAddresses(); }
                                        @Override public void onFailure(String error) {
                                            Toast.makeText(AddressListActivity.this,
                                                    error, Toast.LENGTH_SHORT).show();
                                        }
                                    }))
                    .setNegativeButton("Hủy", null)
                    .show();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                @NonNull RecyclerView.ViewHolder vh,
                                float dX, float dY, int actionState, boolean isActive) {
            View item = vh.itemView;
            // Vẽ nền đỏ phía sau khi kéo trái
            if (dX < 0) {
                RectF bg = new RectF(item.getRight() + dX, item.getTop(),
                        item.getRight(), item.getBottom());
                c.drawRect(bg, paint);

                // Chữ "Xóa"
                Paint textPaint = new Paint();
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(36f);
                textPaint.setAntiAlias(true);
                textPaint.setTextAlign(Paint.Align.CENTER);
                float cx = item.getRight() + dX / 2f;
                float cy = (item.getTop() + item.getBottom()) / 2f
                        - (textPaint.descent() + textPaint.ascent()) / 2f;
                c.drawText("Xóa", cx, cy, textPaint);
            }
            super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive);
        }
    }

    // ── Adapter ───────────────────────────────────────────────────────────────
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
                selectedPos = pos;
                notifyDataSetChanged();
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
