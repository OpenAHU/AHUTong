package com.ahu.ahutong.ui.widget.schedule.bean;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;

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

    public ScheduleTheme(@NonNull String config) throws Exception {
        JSONObject jsonObject = new JSONObject(config);
        this.name = jsonObject.optString("name");
        String type = jsonObject.optString("type");
        Class<?> simpleClass = Class.forName(type);
        Constructor<?> constructor = simpleClass.getConstructor(String.class);
        constructor.setAccessible(true);
        this.theme = (Theme) constructor.newInstance(config);
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

    @NonNull
    public String toConfig() throws JSONException {
        JSONObject jsonObject = this.theme.toConfig();
        jsonObject.put("name", this.name);
        return jsonObject.toString();
    }

    public static abstract class Theme {
        public Theme(String config) {

        }

        public abstract void setWeekdayListHeader(@NonNull LinearLayout linearLayout);

        public abstract void setContentBackground(@NonNull LinearLayout linearLayout);

        public abstract void setItem(@NonNull View item, @NonNull Boolean isThisWeek);

        public abstract void setToday(@NonNull TextView view);

        @NonNull
        public abstract JSONObject toConfig();

    }
}
