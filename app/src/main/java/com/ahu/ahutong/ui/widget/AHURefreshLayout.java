package com.ahu.ahutong.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.lang.reflect.Field;

/**
 * @Author: SinkDev
 * @Date: 2021/8/3-下午5:46
 * @Email: 468766131@qq.com
 */
public class AHURefreshLayout extends SwipeRefreshLayout {


    public AHURefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        int scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        Class<SwipeRefreshLayout> swipeRefreshLayoutClass = SwipeRefreshLayout.class;
        try {
            Field mTouchSlop = swipeRefreshLayoutClass.getDeclaredField("mTouchSlop");
            mTouchSlop.setAccessible(true);
            mTouchSlop.setInt(this, scaledTouchSlop + 200);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
    }
}
