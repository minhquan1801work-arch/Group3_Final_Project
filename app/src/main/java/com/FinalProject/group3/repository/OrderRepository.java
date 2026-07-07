package com.FinalProject.group3.repository;

import com.FinalProject.group3.model.Order;
import com.FinalProject.group3.model.OrderDetail;
import com.FinalProject.group3.utils.FirebaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.List;

/**
 * Quản lý đơn hàng.
 *
 * Cấu trúc Firestore:
 *   orders/{orderId}                      → Order
 *   orders/{orderId}/orderDetails/{id}    → OrderDetail
 */
public class OrderRepository {

    public interface OrderListCallback {
        void onSuccess(List<Order> orders);
        void onFailure(String error);
    }

    public interface OrderCallback {
        void onSuccess(Order order);
        void onFailure(String error);
    }

    public interface SimpleCallback {
        void onSuccess(String orderId);
        void onFailure(String error);
    }

    private final FirebaseFirestore db = FirebaseHelper.getDb();

    // ── Tạo đơn hàng mới ──────────────────────────────────────────────────────
    public void createOrder(Order order, List<OrderDetail> details, SimpleCallback callback) {
        db.collection(FirebaseHelper.COL_ORDERS)
                .add(order)
                .addOnSuccessListener(ref -> {
                    String orderId = ref.getId();
                    // Thêm order details vào subcollection
                    for (OrderDetail detail : details) {
                        ref.collection(FirebaseHelper.COL_ORDER_DETAILS).add(detail);
                    }
                    callback.onSuccess(orderId);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Nhận đơn khách vãng lai về tài khoản ──────────────────────────────────
    // Khách đặt hàng không đăng nhập → đơn lưu customerId="GUEST" + guestEmail.
    // Khi đăng nhập/đăng ký bằng đúng email đó, gán đơn về tài khoản để hiện
    // trong Lịch sử đơn hàng. Fire-and-forget — gọi mỗi lần mở app đã đăng nhập.
    public void claimGuestOrders(String uid, String email) {
        if (uid == null || email == null || email.isEmpty()) return;
        db.collection(FirebaseHelper.COL_ORDERS)
                .whereEqualTo("customerId", "GUEST")
                .whereEqualTo("guestEmail", email)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (com.google.firebase.firestore.DocumentSnapshot doc : snapshot.getDocuments())
                        doc.getReference().update("customerId", uid);
                });
    }

    // ── Lấy danh sách đơn hàng của user ───────────────────────────────────────
    public void getMyOrders(OrderListCallback callback) {
        String uid = FirebaseHelper.getCurrentUserId();
        if (uid == null) { callback.onFailure("Chưa đăng nhập"); return; }

        db.collection(FirebaseHelper.COL_ORDERS)
                .whereEqualTo("customerId", uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    java.util.List<Order> orders = snapshot.toObjects(Order.class);
                    // Sort mới nhất trước (tránh cần composite index Firestore)
                    orders.sort((a, b) -> {
                        if (a.getCreatedAt() == null) return 1;
                        if (b.getCreatedAt() == null) return -1;
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    });
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Lấy chi tiết 1 đơn hàng ───────────────────────────────────────────────
    public void getOrderById(String orderId, OrderCallback callback) {
        db.collection(FirebaseHelper.COL_ORDERS)
                .document(orderId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) callback.onSuccess(doc.toObject(Order.class));
                    else callback.onFailure("Không tìm thấy đơn hàng");
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Lấy danh sách sản phẩm trong đơn ─────────────────────────────────────
    public void getOrderDetails(String orderId,
                                 com.FinalProject.group3.repository.CartRepository.CartDetailCallback callback) {
        db.collection(FirebaseHelper.COL_ORDERS)
                .document(orderId)
                .collection(FirebaseHelper.COL_ORDER_DETAILS)
                .get()
                .addOnSuccessListener(snapshot ->
                        callback.onSuccess(snapshot.toObjects(
                                com.FinalProject.group3.model.CartDetail.class)))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ── Hủy đơn hàng ──────────────────────────────────────────────────────────
    public void cancelOrder(String orderId,
                             com.FinalProject.group3.repository.CartRepository.SimpleCallback callback) {
        db.collection(FirebaseHelper.COL_ORDERS)
                .document(orderId)
                .update("orderStatus", "CANCELLED")
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
