package com.FinalProject.group3.repository;

import com.FinalProject.group3.model.Cart;
import com.FinalProject.group3.model.CartDetail;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý giỏ hàng.
 *
 * Cấu trúc Firestore:
 *   carts/{cartId}                  → Cart
 *   carts/{cartId}/cartDetails/{id} → CartDetail
 *
 * Mỗi user chỉ có 1 cart. cartId = customerId để dễ lookup.
 */
public class CartRepository {

    public interface CartDetailCallback {
        void onSuccess(List<CartDetail> items);
        void onFailure(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String error);
    }

    /** Callback trả về id document vừa tạo — dùng cho luồng "Mua ngay"
     *  (BPMN: thêm vào giỏ → đưa thẳng item đó sang Checkout). */
    public interface IdCallback {
        void onSuccess(String cartDetailId);
        void onFailure(String error);
    }

    private final FirebaseFirestore db = FirebaseHelper.getDb();

    // ── Lấy cartId từ customerId ───────────────────────────────────────────────
    // cartId == customerId để đơn giản
    private String getCartId() {
        return FirebaseHelper.getCurrentUserId();
    }

    // ── Lấy tất cả item trong giỏ ─────────────────────────────────────────────
    public void getCartItems(CartDetailCallback callback) {
        String cartId = getCartId();
        if (cartId == null) { callback.onFailure("Chưa đăng nhập"); return; }

        db.collection(FirebaseHelper.COL_CARTS)
                .document(cartId)
                .collection(FirebaseHelper.COL_CART_DETAILS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    // Map thủ công thay vì toObjects(): nếu document lỡ chứa field
                    // trùng tên với @DocumentId (data seed cũ / nhập tay sai),
                    // toObjects() sẽ ném RuntimeException làm CRASH app.
                    // Cách này an toàn với mọi data bẩn.
                    List<CartDetail> items = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        CartDetail d = new CartDetail(
                                doc.getString("productId"),
                                doc.getLong("quantity") != null ? doc.getLong("quantity").intValue() : 1,
                                doc.getString("color"));
                        d.setCartDetailId(doc.getId());
                        d.setAddedAt(doc.getLong("addedAt") != null ? doc.getLong("addedAt") : 0L);
                        items.add(d);
                    }
                    // Sắp xếp mới → cũ: item vừa thêm luôn nằm ĐẦU danh sách giỏ
                    java.util.Collections.sort(items,
                            (a, b) -> Long.compare(b.getAddedAt(), a.getAddedAt()));
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Thêm sản phẩm vào giỏ ─────────────────────────────────────────────────
    public void addToCart(CartDetail item, SimpleCallback callback) {
        addToCartReturningId(item, new IdCallback() {
            @Override public void onSuccess(String cartDetailId) { callback.onSuccess(); }
            @Override public void onFailure(String error) { callback.onFailure(error); }
        });
    }

    // ── Thêm sản phẩm vào giỏ, trả về id item (luồng Mua ngay / auto-tick) ────
    // Nếu giỏ đã có cùng productId + màu → cộng dồn số lượng thay vì tạo dòng mới.
    public void addToCartReturningId(CartDetail item, IdCallback callback) {
        String cartId = getCartId();
        if (cartId == null) { callback.onFailure("Chưa đăng nhập"); return; }

        Cart cart = new Cart(cartId);
        db.collection(FirebaseHelper.COL_CARTS).document(cartId).set(cart);

        db.collection(FirebaseHelper.COL_CARTS)
                .document(cartId)
                .collection(FirebaseHelper.COL_CART_DETAILS)
                .whereEqualTo("productId", item.getProductId())
                .whereEqualTo("color", item.getColor())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        // Đã có → cộng dồn quantity + đẩy addedAt lên mới nhất
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        long oldQty = doc.getLong("quantity") != null ? doc.getLong("quantity") : 1;
                        doc.getReference()
                                .update("quantity", oldQty + item.getQuantity(),
                                        "addedAt", System.currentTimeMillis())
                                .addOnSuccessListener(v -> callback.onSuccess(doc.getId()))
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    } else {
                        // Chưa có → tạo mới. Ghi bằng Map (quy tắc @DocumentId của dự án)
                        java.util.Map<String, Object> data = new java.util.HashMap<>();
                        data.put("productId", item.getProductId());
                        data.put("quantity", item.getQuantity());
                        data.put("color", item.getColor());
                        data.put("addedAt", System.currentTimeMillis());
                        db.collection(FirebaseHelper.COL_CARTS)
                                .document(cartId)
                                .collection(FirebaseHelper.COL_CART_DETAILS)
                                .add(data)
                                .addOnSuccessListener(ref -> callback.onSuccess(ref.getId()))
                                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Cập nhật số lượng ─────────────────────────────────────────────────────
    public void updateQuantity(String cartDetailId, int newQuantity, SimpleCallback callback) {
        String cartId = getCartId();
        if (cartId == null) { callback.onFailure("Chưa đăng nhập"); return; }

        db.collection(FirebaseHelper.COL_CARTS)
                .document(cartId)
                .collection(FirebaseHelper.COL_CART_DETAILS)
                .document(cartDetailId)
                .update("quantity", newQuantity)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Cập nhật màu sắc (variant) ────────────────────────────────────────────
    public void updateColor(String cartDetailId, String newColor, SimpleCallback callback) {
        String cartId = getCartId();
        if (cartId == null) { callback.onFailure("Chưa đăng nhập"); return; }

        db.collection(FirebaseHelper.COL_CARTS)
                .document(cartId)
                .collection(FirebaseHelper.COL_CART_DETAILS)
                .document(cartDetailId)
                .update("color", newColor)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Cập nhật màu + số lượng cùng lúc (BottomSheet chỉnh variant) ──────────
    public void updateItem(String cartDetailId, String newColor, int newQuantity, SimpleCallback callback) {
        String cartId = getCartId();
        if (cartId == null) { callback.onFailure("Chưa đăng nhập"); return; }

        db.collection(FirebaseHelper.COL_CARTS)
                .document(cartId)
                .collection(FirebaseHelper.COL_CART_DETAILS)
                .document(cartDetailId)
                .update("color", newColor, "quantity", newQuantity)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Xóa item khỏi giỏ ─────────────────────────────────────────────────────
    public void removeFromCart(String cartDetailId, SimpleCallback callback) {
        String cartId = getCartId();
        if (cartId == null) { callback.onFailure("Chưa đăng nhập"); return; }

        db.collection(FirebaseHelper.COL_CARTS)
                .document(cartId)
                .collection(FirebaseHelper.COL_CART_DETAILS)
                .document(cartDetailId)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Xóa toàn bộ giỏ sau khi đặt hàng ─────────────────────────────────────
    public void clearCart(SimpleCallback callback) {
        String cartId = getCartId();
        if (cartId == null) { callback.onFailure("Chưa đăng nhập"); return; }

        db.collection(FirebaseHelper.COL_CARTS)
                .document(cartId)
                .collection(FirebaseHelper.COL_CART_DETAILS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (var doc : snapshot.getDocuments()) doc.getReference().delete();
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
