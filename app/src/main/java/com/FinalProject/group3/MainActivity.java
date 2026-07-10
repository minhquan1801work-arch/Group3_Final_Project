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

    /** Extra: mở thẳng tab Trang chủ (nút "Về trang chủ" sau khi đặt hàng) */
    public static final String EXTRA_OPEN_HOME = "open_home";

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
        handleOpenHomeIntent(getIntent());
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
        handleOpenHomeIntent(intent);
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

    /** Nếu intent yêu cầu về Trang chủ (nút "Về trang chủ" sau khi đặt hàng) → navigate tab Home + cuộn lên đầu */
    private void handleOpenHomeIntent(android.content.Intent intent) {
        if (intent == null || !intent.getBooleanExtra(EXTRA_OPEN_HOME, false)) return;
        intent.removeExtra(EXTRA_OPEN_HOME); // tránh navigate lại khi rotate
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        if (navHost == null) return;

        NavController nc = navHost.getNavController();
        if (nc.getCurrentDestination() == null
                || nc.getCurrentDestination().getId() != R.id.homeFragment) {
            nc.navigate(R.id.homeFragment, null, new androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, true)
                    .build());
        }

        // Dù vừa navigate lại hay đã sẵn ở Home từ trước, luôn cuộn lên đầu —
        // post() để đợi transaction fragment (nếu có) commit xong trước khi lấy instance
        navHost.getChildFragmentManager().executePendingTransactions();
        androidx.fragment.app.Fragment current =
                navHost.getChildFragmentManager().getPrimaryNavigationFragment();
        if (current instanceof com.FinalProject.group3.ui.catalog.HomeFragment) {
            ((com.FinalProject.group3.ui.catalog.HomeFragment) current).scrollToTop();
        }
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
        // Gọng nhựa / kim loại lọc theo chất liệu (không phải category kinh_can)
        drawerView.findViewById(R.id.menuGongNhua).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            ProductListActivity.startMaterial(this, ProductListActivity.MAT_NHUA);
        });
        drawerView.findViewById(R.id.menuGongKimLoai).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            ProductListActivity.startMaterial(this, ProductListActivity.MAT_KIM_LOAI);
        });

        // Shape items = HÌNH DÁNG GỌNG (khác dáng mặt ở trang chủ)
        navigateFrameShape(drawerView, R.id.menuShapeTron,   ProductListActivity.SHAPE_TRON);
        navigateFrameShape(drawerView, R.id.menuShapeOval,   ProductListActivity.SHAPE_OVAL);
        navigateFrameShape(drawerView, R.id.menuShapeMatMeo, ProductListActivity.SHAPE_MAT_MEO);
        navigateFrameShape(drawerView, R.id.menuShapeVuong,  ProductListActivity.SHAPE_VUONG);

        navigateAccessory(drawerView, R.id.menuPhuKien, null);
        navigateAccessory(drawerView, R.id.menuHopDung, ProductListActivity.ACC_HOP_DUNG);
        navigateAccessory(drawerView, R.id.menuKhanLau, ProductListActivity.ACC_KHAN_LAU);
        navigateAccessory(drawerView, R.id.menuNuocLau, ProductListActivity.ACC_NUOC_LAU);

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

    private void navigateFrameShape(android.view.View root, int viewId, String frameShape) {
        root.findViewById(viewId).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            ProductListActivity.startFrameShape(this, frameShape);
        });
    }

    private void navigateAccessory(android.view.View root, int viewId, String accessoryType) {
        root.findViewById(viewId).setOnClickListener(v -> {
            drawerLayout.closeDrawer(Gravity.START);
            ProductListActivity.startAccessory(this, accessoryType);
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
        android.view.View navBarCard             = findViewById(R.id.navBarCard);

        btnHome.setOnClickListener(v         -> navigateTab(navController, R.id.homeFragment));
        // Nút danh mục → mở thẳng trang sản phẩm (tất cả SP) thay vì CategoryFragment
        btnCategory.setOnClickListener(v     ->
                com.FinalProject.group3.ui.catalog.ProductListActivity.startAll(this));
        btnNotification.setOnClickListener(v -> navigateTab(navController, R.id.notificationFragment));
        btnProfile.setOnClickListener(v      -> navigateTab(navController, R.id.profileFragment));

        navController.addOnDestinationChangedListener((c, destination, args) -> {
            int id = destination.getId();
            btnHome.setSelected(id == R.id.homeFragment);
            btnCategory.setSelected(id == R.id.categoryFragment);
            btnNotification.setSelected(id == R.id.notificationFragment);
            btnProfile.setSelected(id == R.id.profileFragment);

            // Ẩn footer pill khi đang ở giỏ hàng — toàn màn hình dành cho nội dung cart
            boolean onCart = (id == R.id.cartFragment);
            navBarCard.animate()
                    .translationY(onCart ? navBarCard.getHeight() : 0f)
                    .alpha(onCart ? 0f : 1f)
                    .setDuration(180)
                    .withEndAction(() -> navBarCard.setVisibility(
                            onCart ? android.view.View.GONE : android.view.View.VISIBLE))
                    .start();
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
