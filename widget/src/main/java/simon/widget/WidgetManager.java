package simon.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import java.util.ArrayList;

import simon.tuke.Tuke;

public class WidgetManager {
    private static Context context;

    public static void init(Context context) {
        WidgetManager.context = context;
    }

    /**
     * 更新桌面小部件
     */
    public static void upData() {
        RemoteViews rv = ClassWidget.update(context);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName cn = new ComponentName(context, ClassWidget.class);
        manager.updateAppWidget(cn, rv);
    }

    /**
     * 更新课表信息
     *
     * @param am  早上的课程
     * @param pm  中午的课程
     * @param ppm 晚上的课程
     */
    public static void updateClass(ArrayList<WidgetBean> am, ArrayList<WidgetBean> pm, ArrayList<WidgetBean> ppm) {
        Tuke.write("am", am);
        Tuke.write("pm", pm);
        Tuke.write("ppm", ppm);
        Tuke.write("num", am.size() + pm.size() + ppm.size());
        upData();
    }

}
