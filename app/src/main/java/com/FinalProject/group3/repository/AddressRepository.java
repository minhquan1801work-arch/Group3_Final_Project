package com.FinalProject.group3.repository;

import com.FinalProject.group3.model.Address;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;

import java.util.Comparator;
import java.util.List;

/**
 * Sổ địa chỉ của user: customers/{uid}/addresses (LA.Address).
 * Luật mặc định:
 *  - Địa chỉ ĐẦU TIÊN của user luôn là mặc định.
 *  - Khi set 1 địa chỉ làm mặc định → bỏ cờ mặc định của các địa chỉ khác (batch).
 */
public class AddressRepository {

    public interface ListCallback {
        void onSuccess(List<Address> addresses);
        void onFailure(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String error);
    }

    private CollectionReference col() {
        return FirebaseHelper.getDb()
                .collection(FirebaseHelper.COL_CUSTOMERS)
                .document(FirebaseHelper.getCurrentUserId())
                .collection("addresses");
    }

    /** Lấy toàn bộ địa chỉ, địa chỉ mặc định xếp lên đầu. */
    public void getAddresses(ListCallback callback) {
        col().get()
                .addOnSuccessListener(snapshot -> {
                    List<Address> list = snapshot.toObjects(Address.class);
                    list.sort(Comparator.comparing(Address::isDefault).reversed());
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Thêm mới (addressId == null) hoặc cập nhật địa chỉ.
     * Nếu isDefault = true → bỏ mặc định của tất cả địa chỉ còn lại trong cùng batch.
     */
    public void saveAddress(Address address, SimpleCallback callback) {
        col().get().addOnSuccessListener(snapshot -> {
            // Người mới (chưa có địa chỉ nào) → địa chỉ đầu tiên bắt buộc là mặc định
            boolean isFirst = snapshot.isEmpty()
                    || (snapshot.size() == 1 && address.getAddressId() != null
                        && snapshot.getDocuments().get(0).getId().equals(address.getAddressId()));
            if (isFirst) address.setDefault(true);

            WriteBatch batch = FirebaseHelper.getDb().batch();
            DocumentReference target = address.getAddressId() == null
                    ? col().document() : col().document(address.getAddressId());

            if (address.isDefault()) {
                for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments())
                    if (!doc.getId().equals(target.getId()))
                        batch.update(doc.getReference(), "default", false);
            }

            // KHÔNG ghi addressId vào document (@DocumentId) — set từng field
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("name", address.getName());
            data.put("phone", address.getPhone());
            data.put("province", address.getProvince());
            data.put("ward", address.getWard());
            data.put("detail", address.getDetail());
            data.put("default", address.isDefault()); // Firestore map theo getter isDefault() -> field "default"
            data.put("createdAt", address.getCreatedAt() == null
                    ? com.google.firebase.firestore.FieldValue.serverTimestamp()
                    : address.getCreatedAt());
            batch.set(target, data);

            batch.commit()
                    .addOnSuccessListener(v -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
