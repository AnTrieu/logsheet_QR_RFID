package com.example.barcode2ds;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

public class ToastManager {
    private static Toast currentToast;
    private static boolean isToastShowing = false;

    public static void showToast(final Context context, final String message) {
        if (!isToastShowing) {
            isToastShowing = true;
            currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            currentToast.show();

            // Đặt lại trạng thái sau khi Toast kết thúc
            currentToast.getView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(View v) {
                    // Không cần làm gì khi view được gắn vào window
                }

                @Override
                public void onViewDetachedFromWindow(View v) {
                    isToastShowing = false;
                }
            });
        }
    }
}