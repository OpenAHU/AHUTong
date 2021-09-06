package arch.sink.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @Author: SinkDev
 * @Date: 2021/8/8-下午4:09
 * @Email: 468766131@qq.com
 */
public class TimeUtils {

    /**
     * 获得两个日期间距多少天
     *
     * @param beginDate 开始日期
     * @param endDate 结束日期
     * @return 天数
     */
    public static long getTimeDistance(Date beginDate, Date endDate) {
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(beginDate);
        fromCalendar.set(Calendar.HOUR_OF_DAY, fromCalendar.getMinimum(Calendar.HOUR_OF_DAY));
        fromCalendar.set(Calendar.MINUTE, fromCalendar.getMinimum(Calendar.MINUTE));
        fromCalendar.set(Calendar.SECOND, fromCalendar.getMinimum(Calendar.SECOND));
        fromCalendar.set(Calendar.MILLISECOND, fromCalendar.getMinimum(Calendar.MILLISECOND));

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(endDate);
        toCalendar.set(Calendar.HOUR_OF_DAY, fromCalendar.getMinimum(Calendar.HOUR_OF_DAY));
        toCalendar.set(Calendar.MINUTE, fromCalendar.getMinimum(Calendar.MINUTE));
        toCalendar.set(Calendar.SECOND, fromCalendar.getMinimum(Calendar.SECOND));
        toCalendar.set(Calendar.MILLISECOND, fromCalendar.getMinimum(Calendar.MILLISECOND));

        long dayDistance = (toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (24 * 60 * 60 *1000)  ;
        dayDistance = Math.abs(dayDistance);

        return dayDistance;
    }

}
