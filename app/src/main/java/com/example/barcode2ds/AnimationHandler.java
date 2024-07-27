package com.example.barcode2ds;

import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;

public class AnimationHandler {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void setButtonAnimation(final Button button) {
        final int normalColor = button.getBackgroundTintList().getDefaultColor();
        final int pressedColor = adjustColorBrightness(normalColor, -0.2f);

        button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        button.setScaleX(0.9f);
                        button.setScaleY(0.9f);
                        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(pressedColor));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        button.setScaleX(1.0f);
                        button.setScaleY(1.0f);
                        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(normalColor));
                        break;
                }
                return false;
            }
        });
    }

    private static int adjustColorBrightness(int color, float factor) {
        int a = android.graphics.Color.alpha(color);
        int r = Math.round(android.graphics.Color.red(color) * (1 + factor));
        int g = Math.round(android.graphics.Color.green(color) * (1 + factor));
        int b = Math.round(android.graphics.Color.blue(color) * (1 + factor));
        return android.graphics.Color.argb(a, Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }
}
