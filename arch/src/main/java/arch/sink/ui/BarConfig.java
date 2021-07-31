package arch.sink.ui;

import android.graphics.Color;

import androidx.annotation.ColorInt;

/**
 * @Author SinkDev
 * @Date 2021/7/18-19:23
 * @Email 468766131@qq.com
 */
public class BarConfig {
    private @ColorInt int color;
    private boolean light;

    public BarConfig(@ColorInt int color, boolean light) {
        this.color = color;
        this.light = light;
    }
    public BarConfig(){
        this(Color.TRANSPARENT, true);
    }

    public BarConfig color(@ColorInt int color){
        this.color = color;
        return this;
    }

    public BarConfig light(){
        this.light = true;
        return this;
    }

    public BarConfig dark(){
        this.light = false;
        return this;
    }


    public int getColor() {
        return color;
    }

    public boolean isLight() {
        return light;
    }
}
