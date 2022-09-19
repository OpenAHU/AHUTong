package com.ahu.ahutong.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;


public class AHUScrollView extends NestedScrollView {
    private int lastScrollY = 0;

    public AHUScrollView(@NonNull Context context) {
        super(context);
    }

    public AHUScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AHUScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        scrollTo(0, lastScrollY);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean b = super.onTouchEvent(ev);
        lastScrollY = getScrollY();
        return b;
    }
}
