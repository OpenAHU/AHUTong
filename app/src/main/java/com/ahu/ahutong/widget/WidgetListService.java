package com.ahu.ahutong.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.ahu.ahutong.R;
import com.ahu.ahutong.data.dao.AHUCache;
import com.ahu.ahutong.data.model.Course;
import com.ahu.ahutong.utils.BitmapUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;


public class WidgetListService extends RemoteViewsService {

    public static WidgetListService service;

    @Override
    public void onCreate() {
        service = this;
        Log.e("WidgetListService", "onCreate");
        super.onCreate();
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(getApplicationContext());
    }

    static class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private ArrayList<Object> mData;
        private Context context;

        public ListRemoteViewsFactory(Context context) {
            mData = new ArrayList<>();
            this.context = context;
        }

        @Override
        public void onCreate() {
            Log.e("ListRemoteViewsFactory", "刷新");
            //1-4上午
            //5-8中文
            //9-10晚上
            String year = AHUCache.INSTANCE.getSchoolYear();
            String trim = AHUCache.INSTANCE.getSchoolTerm();
            List<Course> courses = AHUCache.INSTANCE.getSchedule(year == null ? "" : year, trim == null ? "" : trim);
            int thisWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
            List<Course> am = new ArrayList<>(2);
            List<Course> pm = new ArrayList<>(2);
            List<Course> ppm = new ArrayList<>(1);
            if (courses != null)
                for (Course course : courses) {
                    if (course.getWeekday() == thisWeek) {
                        int startTime = course.getStartTime();
                        int endTime = course.getLength() - 1 + course.getStartTime();
                        if (startTime >= 9 && endTime <= 11) {
                            ppm.add(course);
                        } else if (startTime >= 5 && endTime <= 8) {
                            pm.add(course);
                        } else if (startTime >= 1 && endTime <= 4) {
                            am.add(course);
                        }
                    }
                }
            addToData(am, pm, ppm);
        }

        @SafeVarargs
        private final void addToData(List<Course>... lists) {
            String[] times = {"上午", "下午", "晚上"};
            for (int i = 0; i < lists.length; i++) {
                List<Course> list = lists[i];
                mData.add(times[i]);
                list.sort(comparing(Course::getStartTime));
                if (list.size() == 0)
                    mData.add(true);
                else mData.addAll(list);
            }
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

        /*
        记录踩坑：
        安卓对RemoteViews的支持就是一坨屎
        如果遇到数据错乱，千万不要手写缓存机制，因为这样没有任何的作用
        正确解决方法是：处理使用相同布局但功能不同的RemoteViews时，要保证setXXX方法在各种类型处理中都写一次
         */
        @SuppressLint("SimpleDateFormat")
        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews back = null;
            Object bean = mData.get(position);
            if (bean instanceof String) {
                back = new RemoteViews(context.getPackageName(), R.layout.item_widget_head);
                back.setTextViewText(R.id.widget_head, (String) bean);
            } else if (bean instanceof Boolean) {
                back = new RemoteViews(context.getPackageName(), R.layout.item_widget_content);
                back.setTextViewText(R.id.widget_name, "空闲");
                back.setTextViewText(R.id.widget_time, "无课程");
                back.setViewVisibility(R.id.widget_state, View.GONE);
                back.setImageViewBitmap(R.id.widget_tag, BitmapUtils.createColorBitmap("#FFCBCBCB"));
            } else if (bean instanceof Course) {
                Course course = (Course) bean;
                back = new RemoteViews(context.getPackageName(), R.layout.item_widget_content);
                back.setTextViewText(R.id.widget_name, course.getName());
                String[] num2text = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一"};
                String startTime = num2text[course.getStartTime() - 1];
                String endTime = num2text[course.getLength() - 1 + course.getStartTime() - 1];
                String times = "第" + startTime + "节";
                if (!startTime.equals(endTime)) {
                    times += "-" + endTime + "节";
                }
                back.setTextViewText(R.id.widget_time, times);
                SimpleDateFormat formatter = new SimpleDateFormat("HHmm");
                String strTime = formatter.format(new Date());
                int numTime=getTime(strTime);
                Log.e("time",numTime+"当前节数");
                if (course.getStartTime()<=numTime){
                    String StrColor="#FFCBCBCB";
                    int color = Color.parseColor(StrColor);
                    back.setImageViewBitmap(R.id.widget_tag, BitmapUtils.createColorBitmap(StrColor));
                    back.setViewVisibility(R.id.widget_state, View.VISIBLE);
                    back.setTextColor(R.id.widget_name, color);
                    back.setTextColor(R.id.widget_time, color);
                }else {
                    back.setImageViewBitmap(R.id.widget_tag, BitmapUtils.createColorBitmap("#FFFE9900"));
                    back.setViewVisibility(R.id.widget_state, View.GONE);
                    back.setTextColor(R.id.widget_name, Color.BLACK);
                    back.setTextColor(R.id.widget_time, Color.parseColor("#9a000000"));
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

    private static int getTime(String time) {
        int tim = Integer.parseInt(time);
        if (tim >= 820 && tim <= 915) {
            return 1;
        } else if (tim >= 915 && tim <= 1020) {
            return 2;
        } else if (tim >= 1020 && tim <= 1115) {
            return 3;
        } else if (tim >= 1115 && tim <= 1400) {
            return 4;
        } else if (tim >= 1400 && tim <= 1455) {
            return 5;
        } else if (tim >= 1455 && tim <= 1550) {
            return 6;
        } else if (tim >= 1550 && tim <= 1645) {
            return 7;
        } else if (tim >= 1645 && tim <= 1900) {
            return 8;
        } else if (tim >= 1900 && tim <= 1955) {
            return 9;
        } else if (tim >= 1955 && tim <= 2050) {
            return 10;
        } else {
            return 11;
        }
    }
}
