package com.FinalProject.group3.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.FinalProject.group3.R;
import com.FinalProject.group3.ui.account.LoginActivity;

/**
 * Dialog yêu cầu đăng nhập — thay thế Toast thuần ở mọi điểm cần tài khoản.
 *
 * Cách dùng:
 *   LoginRequiredDialog.show(context, "Đăng nhập để thêm vào giỏ hàng");
 *   LoginRequiredDialog.show(context, "...", () -> { ... hành động khi bấm Để sau ... });
 */
public final class LoginRequiredDialog {

    private LoginRequiredDialog() {}

    public static void show(Context context, String message) {
        show(context, message, null);
    }

    public static void show(Context context, String message, Runnable onDismiss) {
        Dialog dialog = new Dialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_login_required, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.88f);
            dialog.getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
            // Dim mờ backdrop
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setDimAmount(0.5f);
        }

        ((TextView) view.findViewById(R.id.tvDialogMessage)).setText(message);

        view.findViewById(R.id.btnDialogLogin).setOnClickListener(v -> {
            dialog.dismiss();
            context.startActivity(new Intent(context, LoginActivity.class));
        });

        view.findViewById(R.id.btnDialogDismiss).setOnClickListener(v -> {
            dialog.dismiss();
            if (onDismiss != null) onDismiss.run();
        });

        dialog.setOnCancelListener(d -> {
            if (onDismiss != null) onDismiss.run();
        });

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
}
