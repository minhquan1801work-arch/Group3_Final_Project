package com.FinalProject.group3;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Màn hình chính SAU KHI đã đăng nhập/đăng ký/chọn Khách (từ WelcomeActivity).
 * Chứa 4 tab bằng BottomNavigationView + Navigation Component: Trang chủ,
 * Danh mục, Giỏ hàng, Cá nhân — chuyển tab chỉ swap Fragment, KHÔNG tạo lại
 * Activity, nên MainActivity chỉ trải qua onCreate() một lần khi mở app.
 *
 * Ôn lại vòng đời Activity (đúng nội dung slide chương 05 - Activity & Intent):
 *  - onCreate(): app "hiện lên" lần đầu, khởi tạo UI. Chạy đúng 1 lần trừ khi
 *    Activity bị hệ thống huỷ hẳn (xoay màn hình, hết bộ nhớ...).
 *  - onStart()/onResume(): app đang ở FOREGROUND, người dùng thấy và tương tác được.
 *  - onPause(): có Activity/Dialog khác che một phần lên trên (không còn nhận input),
 *    ví dụ khi mở Google Sign-In popup từ WelcomeActivity.
 *  - onStop(): app bị đẩy xuống BACKGROUND (người dùng bấm Home, hoặc mở Activity
 *    khác full-screen như ProductListActivity) — vẫn còn sống trong bộ nhớ, chưa bị hủy.
 *  - onDestroy(): Activity bị huỷ hẳn — do người dùng thoát (back hết stack) hoặc
 *    hệ thống thu hồi bộ nhớ khi app chạy ẩn quá lâu.
 * Khác với "Cancel/Stop" của 1 tiến trình hệ điều hành thông thường, Android
 * không cho code tự "kill" Activity khác — chỉ có thể finish() Activity của
 * chính mình, phần còn lại do hệ điều hành quản lý dựa trên vòng đời trên.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity-Lifecycle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: app khởi tạo UI lần đầu");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        com.FinalProject.group3.utils.InsetsUtil.applySystemBarsPadding(findViewById(R.id.main));

        FragmentManager fm = getSupportFragmentManager();
        NavHostFragment navHostFragment = (NavHostFragment) fm.findFragmentById(R.id.navHostFragment);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNav, navController);
        }

        setupNotificationDot(bottomNav);
    }

    /**
     * Chấm đỏ trên icon chuông (footer pill, theo Figma) — snapshot listener
     * nên tự bật/tắt realtime khi có thông báo UNREAD mới.
     */
    private void setupNotificationDot(BottomNavigationView bottomNav) {
        String uid = com.FinalProject.group3.utils.FirebaseHelper.getCurrentUserId();
        if (uid == null) return; // khách chưa đăng nhập → không có badge

        com.google.android.material.badge.BadgeDrawable badge =
                bottomNav.getOrCreateBadge(R.id.notificationFragment);
        badge.setBackgroundColor(getColor(R.color.nav_badge_red));
        badge.setVisible(false);

        com.FinalProject.group3.utils.FirebaseHelper.getDb()
                .collection(com.FinalProject.group3.utils.FirebaseHelper.COL_NOTIFICATIONS)
                .whereEqualTo("customerId", uid)
                .whereEqualTo("status", "UNREAD")
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null) return;
                    badge.setVisible(!snapshot.isEmpty());
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: app chuẩn bị hiện lên foreground");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: app đang ở foreground, người dùng tương tác được");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: có màn hình khác che lên trên (vd: dialog, Activity trong suốt)");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: app bị đẩy xuống background (mở Activity khác/bấm Home)");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Activity bị huỷ hẳn");
    }
}
