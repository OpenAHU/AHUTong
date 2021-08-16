package simon.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import simon.tuke.Tuke;

public class WidgetListService extends RemoteViewsService {
    public static WidgetListService service;

    @Override
    public void onCreate() {
        service=this;
        super.onCreate();
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext());
    }

    static class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private List<WidgetBean> mData;
        private Context context;

        public ListRemoteViewsFactory(Context context) {
            Tuke.init(this.context=context);
        }

        @Override
        public void onCreate() {
            mData = new ArrayList<>();
            WidgetBean widgetBean = new WidgetBean("上午", null, R.layout.item_widget_head, false);
            mData.add(widgetBean);
            ArrayList<WidgetBean> am = Tuke.get("am", new ArrayList<WidgetBean>());
            mData.addAll(am);

            if (am.size() == 0)
                mData.add(new WidgetBean("空闲", "无课表", R.layout.item_widget_content, false));

            WidgetBean widgetBean1 = new WidgetBean("下午", null, R.layout.item_widget_head, false);
            mData.add(widgetBean1);
            ArrayList<WidgetBean> pm = Tuke.get("pm", new ArrayList<WidgetBean>());
            mData.addAll(pm);

            if (am.size() == 0)
                mData.add(new WidgetBean("空闲", "无课表", R.layout.item_widget_content, false));

            WidgetBean widgetBean2 = new WidgetBean("晚上", null, R.layout.item_widget_head, false);
            mData.add(widgetBean2);
            ArrayList<WidgetBean> ppm = Tuke.get("ppm", new ArrayList<WidgetBean>());
            mData.addAll(ppm);

            if (am.size() == 0)
                mData.add(new WidgetBean("空闲", "无课表", R.layout.item_widget_content, false));

        }

        @Override
        public void onDataSetChanged() {
        }

        @Override
        public void onDestroy() {
            mData.clear();
            mData = null;
            context = null;
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @SuppressLint("RemoteViewLayout")
        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews back = null;
            WidgetBean widgetBean = mData.get(position);
            if (widgetBean.type == R.layout.item_widget_head) {
                back = new RemoteViews(context.getPackageName(), R.layout.item_widget_head);
                back.setTextViewText(R.id.widget_head, widgetBean.name);
            } else if (widgetBean.type == R.layout.item_widget_content) {
                back = new RemoteViews(context.getPackageName(), R.layout.item_widget_content);
                back.setTextViewText(R.id.widget_name, widgetBean.name);
                back.setTextViewText(R.id.widget_time, widgetBean.times);
                if (widgetBean.isFinish) {
                    int textcolor = Color.parseColor("#FFCBCBCB");
                    Bitmap color = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(color);
                    canvas.drawColor(textcolor);
                    back.setImageViewBitmap(R.id.widget_tag, color);
                    back.setTextColor(R.id.widget_name, textcolor);
                    back.setTextColor(R.id.widget_time, textcolor);
                    back.setViewVisibility(R.id.widget_state, View.VISIBLE);
                }
            }
            return back;
        }

        @Override
        public RemoteViews getLoadingView() {

            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
