package com.ahu.ahutong.widget;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.ahu.ahutong.MainActivity;
import com.ahu.ahutong.R;
import com.ahu.ahutong.data.dao.AHUCache;
import com.ahu.ahutong.data.model.Course;
import com.ahu.ahutong.utils.DateUtils;

import java.util.Calendar;
import java.util.List;

public class ClassWidget extends AppWidgetProvider {

    public static int getClassNum() {
        String year = AHUCache.INSTANCE.getSchoolYear();
        String trim = AHUCache.INSTANCE.getSchoolTerm();
        List<Course> courses = AHUCache.INSTANCE.getSchedule(year == null ? "" : year, trim == null ? "" : trim);
        if (courses == null) {
            return 0;
        }
        int thisWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        int back = 0;
        for (Course course : courses) {
            if (course.getWeekday() == thisWeek) {
                back++;
            }
        }
        return back;
    }

    /**
     * 设置widget的头
     *
     * @param context 必传参数
     * @return 布局
     */
    public static RemoteViews update(Context context) {
        // 获取AppWidget对应的视图
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);

        Intent serviceIntent = new Intent(context, WidgetListService.class);
        remoteViews.setRemoteAdapter(R.id.widget_listview, serviceIntent);

        PendingIntent rootIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), FLAG_ONE_SHOT);
        remoteViews.setOnClickPendingIntent(R.id.widget_root, rootIntent);
        int num = getClassNum();
        remoteViews.setTextViewText(R.id.widget_class, num + "节课");
        remoteViews.setTextViewText(R.id.widget_date, DateUtils.getWeek());
        if (WidgetListService.service != null) {
            WidgetListService.service.onGetViewFactory(null);
        }
        return remoteViews;
    }

    /**
     * 刷新时调用，添加时不会调用
     *
     * @param context          context
     * @param appWidgetManager appWidgetManager
     * @param appWidgetIds     appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, update(context));
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    /**
     * 相当于ondestroy
     *
     * @param context context
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

    }


}
