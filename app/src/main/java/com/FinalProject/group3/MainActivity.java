package com.FinalProject.group3;

import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.FinalProject.group3.ui.catalog.CollectionActivity;
import com.FinalProject.group3.ui.catalog.ProductListActivity;
import com.FinalProject.group3.ui.catalog.SearchActivity;
import com.FinalProject.group3.utils.InsetsUtil;

public class MainActivity extends AppCompatActivity {

    /** Extra: mở thẳng tab Giỏ hàng (icon giỏ ở header ProductDetail dùng) */
    public static final String EXTRA_OPEN_CART = "open_cart";

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InsetsUtil.applySystemBarsPadding(findViewById(R.id.main));

        drawerLayout = findViewById(R.id.drawerLayout);

        FragmentManager fm = getSupportFragmentManager();
        NavHostFragment navHostFragment = (NavHostFragment) fm.findFragmentById(R.id.navHostFragment);
        if (navHostFragment != null) {
            setupBottomNav(navHostFragment.getNavController());
        }

        setupDrawer();
        setupNotificationDot();
        handleOpenCartIntent(getIntent());
        claimGuestOrders();
    }

    /** Đơn đặt lúc chưa đăng nhập (customerId="GUEST") → gán về tài khoản theo email */
    private void claimGuestOrders() {
        com.google.firebase.auth.FirebaseUser user =
                com.FinalProject.group3.utils.FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) return;
        new com.FinalProject.group3.repository.OrderRepository()
                .claimGuestOrders(user.getUid(), user.getEmail());
    }

    @Override
    protected void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleOpenCartIntent(intent);
    }

    /** Nếu intent yêu cầu mở giỏ (từ icon giỏ ở ProductDetail) → navigate tab Giỏ */
    private void handleOpenCartIntent(android.content.Intent intent) {
        if (intent == null || !intent.getBooleanExtra(EXTRA_OPEN_CART, false)) return;
        intent.removeExtra(EXTRA_OPEN_CART); // tránh mở lại khi rotate
        if (com.FinalProject.group3.utils.FirebaseHelper.getCurrentUserId() == null) {
            com.FinalProject.group3.utils.LoginRequiredDialog.show(
                    this, "Đăng nhập để xem giỏ hàng của bạn");
            return;
        }
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        if (navHost != null) navHost.getNavController().navigate(R.id.cartFragment);
    }

    public void openDrawer() {
        if (drawerLayout != null) drawerLayout.openDrawer(Gravity.START);
    }

    private void setupDrawer() {
        android.view.View drawerView = drawerLayout.getChildAt(1);

        drawerView.findViewById(R.id.btnDrawerClose).setOnClickListener(v ->
                drawerLayout.closeDrawer(Gravity.START));

        drawerView.findViewById(R.id.btnDrawerSearch).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            startActivity(new android.content.Intent(this, SearchActivity.class));
        });

        drawerView.findViewById(R.id.btnDrawerCart).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            if (com.FinalProject.group3.utils.FirebaseHelper.getCurrentUserId() == null) {
                com.FinalProject.group3.utils.LoginRequiredDialog.show(
                        this, "Đăng nhập để xem giỏ hàng của bạn");
                return;
            }
            NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.navHostFragment);
            if (navHost != null) navHost.getNavController().navigate(R.id.cartFragment);
        });

        // Nam / Nữ tab toggle
        TextView tabNam = drawerView.findViewById(R.id.tabNam);
        TextView tabNu  = drawerView.findViewById(R.id.tabNu);
        android.view.View indicator = drawerView.findViewById(R.id.tabIndicator);

        tabNam.setOnClickListener(v -> {
            tabNam.setTypeface(null, android.graphics.Typeface.BOLD);
            tabNu.setTypeface(null, android.graphics.Typeface.NORMAL);
            indicator.animate().translationX(0).setDuration(150).start();
        });
        tabNu.setOnClickListener(v -> {
            tabNu.setTypeface(null, android.graphics.Typeface.BOLD);
            tabNam.setTypeface(null, android.graphics.Typeface.NORMAL);
            indicator.post(() ->
                    indicator.animate().translationX(tabNu.getLeft()).setDuration(150).start());
        });

        // Menu items → ProductListActivity
        navigate(drawerView, R.id.menuKinhMat,   ProductListActivity.CAT_KINH_MAT, null, "Kính Mát");
        navigate(drawerView, R.id.menuGongNhua,  ProductListActivity.CAT_KINH_CAN, null, "Kính Gọng Nhựa");
        navigate(drawerView, R.id.menuGongKimLoai, ProductListActivity.CAT_KINH_CAN, null, "Kính Gọng Kim Loại");

        navigateShape(drawerView, R.id.menuShapeTron,   ProductListActivity.SHAPE_TRON,      "Kính Gọng Tròn");
        navigateShape(drawerView, R.id.menuShapeOval,   ProductListActivity.SHAPE_TRAI_XOAN, "Kính Gọng Oval");
        navigateShape(drawerView, R.id.menuShapeMatMeo, ProductListActivity.SHAPE_TRAI_TIM,  "Kính Gọng Mắt Mèo");
        navigateShape(drawerView, R.id.menuShapeVuong,  ProductListActivity.SHAPE_VUONG,     "Kính Gọng Vuông");

        navigate(drawerView, R.id.menuPhuKien, ProductListActivity.CAT_PHU_KIEN, null, "Phụ Kiện");
        navigate(drawerView, R.id.menuHopDung, ProductListActivity.CAT_PHU_KIEN, null, "Hộp Đựng Kính");
        navigate(drawerView, R.id.menuKhanLau, ProductListActivity.CAT_PHU_KIEN, null, "Khăn Lau Kính");
        navigate(drawerView, R.id.menuNuocLau, ProductListActivity.CAT_PHU_KIEN, null, "Nước Lau Kính");

        drawerView.findViewById(R.id.menuBST).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            startActivity(new android.content.Intent(this, CollectionActivity.class));
        });
        navigateCollection(drawerView, R.id.menuMonochrome, "Monochrome Collection");
        navigateCollection(drawerView, R.id.menuEssential,  "Essential Acetate");
        navigateCollection(drawerView, R.id.menuSunlight,   "Sunlight Studio");

        drawerView.findViewById(R.id.menuVeGlassity).setOnClickListener(v ->
                drawerLayout.closeDrawer(Gravity.START));

        drawerView.findViewById(R.id.menuBlogChonKinh).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            com.FinalProject.group3.ui.catalog.BlogActivity.start(this, 1);
        });
    }

    private void navigate(android.view.View root, int viewId, String catId, String shape, String title) {
        root.findViewById(viewId).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            ProductListActivity.start(this, catId, shape, title);
        });
    }

    private void navigateShape(android.view.View root, int viewId, String shape, String title) {
        root.findViewById(viewId).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            ProductListActivity.start(this, null, shape, title);
        });
    }

    private void navigateCollection(android.view.View root, int viewId, String collectionName) {
        root.findViewById(viewId).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            android.content.Intent intent = new android.content.Intent(this, CollectionActivity.class);
            intent.putExtra(CollectionActivity.EXTRA_COLLECTION, collectionName);
            startActivity(intent);
        });
    }

    private void setupBottomNav(NavController navController) {
        android.widget.ImageView btnHome         = findViewById(R.id.btnNavHome);
        android.widget.ImageView btnCategory     = findViewById(R.id.btnNavCategory);
        android.widget.ImageView btnNotification = findViewById(R.id.btnNavNotification);
        android.widget.ImageView btnProfile      = findViewById(R.id.btnNavProfile);

        btnHome.setOnClickListener(v         -> navigateTab(navController, R.id.homeFragment));
        btnCategory.setOnClickListener(v     -> navigateTab(navController, R.id.categoryFragment));
        btnNotification.setOnClickListener(v -> navigateTab(navController, R.id.notificationFragment));
        btnProfile.setOnClickListener(v      -> navigateTab(navController, R.id.profileFragment));

        navController.addOnDestinationChangedListener((c, destination, args) -> {
            int id = destination.getId();
            btnHome.setSelected(id == R.id.homeFragment);
            btnCategory.setSelected(id == R.id.categoryFragment);
            btnNotification.setSelected(id == R.id.notificationFragment);
            btnProfile.setSelected(id == R.id.profileFragment);
        });
    }

    private void navigateTab(NavController navController, int destinationId) {
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == destinationId) return;
        androidx.navigation.NavOptions options = new androidx.navigation.NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setPopUpTo(R.id.homeFragment, false)
                .build();
        navController.navigate(destinationId, null, options);
    }

    private void setupNotificationDot() {
        android.view.View dot = findViewById(R.id.dotNotification);
        String uid = com.FinalProject.group3.utils.FirebaseHelper.getCurrentUserId();
        if (uid == null) return;

        com.FinalProject.group3.utils.FirebaseHelper.getDb()
                .collection(com.FinalProject.group3.utils.FirebaseHelper.COL_NOTIFICATIONS)
                .whereEqualTo("customerId", uid)
                .whereEqualTo("status", "UNREAD")
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null) return;
                    dot.setVisibility(snapshot.isEmpty()
                            ? android.view.View.GONE : android.view.View.VISIBLE);
                });
    }
}
