package simon.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import simon.tuke.Tuke;
import static android.app.PendingIntent.FLAG_ONE_SHOT;

public class ClassWidget extends AppWidgetProvider {

    /*
     * Widget并没有运行在我们App的进程中，而是运行在系统的SystemServer进程中
     */

    /**
     * 设置widget的头
     * @param context 必传参数
     * @return 布局
     */
    public static RemoteViews Update(Context context) {
        // 获取AppWidget对应的视图
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);

        Intent serviceIntent = new Intent(context, WidgetListService.class);
        remoteViews.setRemoteAdapter(R.id.widget_listview, serviceIntent);
        Intent intent= null;
        try {
            intent = new Intent(context,Class.forName("com.ahu.ahutong.MainActivity"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        PendingIntent rootIntent = PendingIntent.getActivity(context, 0, intent, FLAG_ONE_SHOT);
        remoteViews.setOnClickPendingIntent(R.id.widget_root, rootIntent);
        int num = Tuke.get("num");
        remoteViews.setTextViewText(R.id.widget_class, num + "节课");
        remoteViews.setTextViewText(R.id.widget_date, getWeekOfDate());
        if (WidgetListService.service!=null)
        WidgetListService.service.onGetViewFactory(null);
        return remoteViews;
    }

    /**
     * 刷新时调用，添加时不会调用
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @SuppressLint("RemoteViewLayout")
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            // 调用集合管理器对集合进行更
            appWidgetManager.updateAppWidget(appWidgetId, Update(context));
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        //让其支持自定义控件的方法如下
        /**
         *         Bitmap bitmap=Bitmap.createBitmap(4000,500, Bitmap.Config.ARGB_8888);
         *         Canvas canvas=new Canvas(bitmap);
         *         View root= LayoutInflater.from(context).inflate(R.layout.activity_main,null,false);
         *         root.draw(canvas);
         */
        //这顿操作下来，再把bitmap设置到imageview上，就相当于展示了一个linearlayout了
        //不用质疑，Kwgt就是这样做的
    }

    /**
     * 首次添加时调用
     *
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Tuke.init(context);
    }

    /**
     * 被删除调用
     *
     * @param context
     * @param appWidgetIds
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    /**
     * 相当于ondestroy
     *
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    ///////////工具/////////
    private static String getWeekOfDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd");
        String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar cal = Calendar.getInstance();
        //  cal.setTime(new Date());
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        String back = weekDays[w] + " " + formatter.format(cal.getTime());
        return back;
    }

}
