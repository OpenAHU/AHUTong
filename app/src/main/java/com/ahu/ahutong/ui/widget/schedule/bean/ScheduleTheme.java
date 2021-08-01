package com.ahu.ahutong.ui.widget.schedule.bean;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

/**
 * @Author: SinkDev
 * @Date: 2021/8/1-下午2:13
 * @Email: 468766131@qq.com
 */
public class ScheduleTheme {
    @NonNull
    private String name;
    @NonNull
    private Theme theme;

    public ScheduleTheme(@NonNull String name, @NonNull Theme theme) {
        this.name = name;
        this.theme = theme;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public Theme getTheme() {
        return theme;
    }

    public void setTheme(@NonNull Theme theme) {
        this.theme = theme;
    }

    public static abstract class Theme{
        public abstract void setWeekdayListHeader(@NonNull LinearLayout linearLayout);
        public abstract void setContentBackground(@NonNull LinearLayout linearLayout);
        public abstract void setItem(@NonNull View item, @NonNull Boolean isThisWeek);
        public abstract void setToday(@NonNull TextView view);
    }
}
