package arch.sink.ui;

import androidx.appcompat.app.AppCompatActivity;

import arch.sink.utils.BarUtils;

/**
 * @Author SinkDev
 * @Date 2021/7/18-19:40
 * @Email 468766131@qq.com
 */
public class SinkBar {
    public static void applyBarConfig(AppCompatActivity activity, BarConfig barConfig) {
        BarUtils.setStatusBarLightMode(activity, barConfig.isLight());
        BarUtils.setStatusBarColor(activity, barConfig.getColor());

    }
}
