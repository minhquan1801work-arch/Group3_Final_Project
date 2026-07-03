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
                        items.add(d);
                    }
                    callback.onSuccess(items);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Thêm sản phẩm vào giỏ ─────────────────────────────────────────────────
    public void addToCart(CartDetail item, SimpleCallback callback) {
        String cartId = getCartId();
        if (cartId == null) { callback.onFailure("Chưa đăng nhập"); return; }

        // Tạo cart document nếu chưa có
        Cart cart = new Cart(cartId);
        db.collection(FirebaseHelper.COL_CARTS).document(cartId).set(cart);

        // Thêm item vào subcollection
        db.collection(FirebaseHelper.COL_CARTS)
                .document(cartId)
                .collection(FirebaseHelper.COL_CART_DETAILS)
                .add(item)
                .addOnSuccessListener(ref -> callback.onSuccess())
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
