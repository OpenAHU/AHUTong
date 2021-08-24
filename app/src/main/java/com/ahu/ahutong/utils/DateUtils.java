package com.ahu.ahutong.utils;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    /**
     * 获取当前是周几
     * @return 返回 周几
     */
    public static String getWeek() {
        String[] weeks={"周日","周一","周二","周三","周四","周五","周六"};
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        return weeks[c.get(Calendar.DAY_OF_WEEK)-1];
    }
}
