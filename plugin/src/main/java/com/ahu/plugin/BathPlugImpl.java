package com.ahu.plugin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import arch.sink.utils.TimeUtils;

public class BathPlugImpl implements BathPlug {
    private int getDay() {
        //20号 南 男         北 女
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse("2021-09-20");
        } catch (ParseException e) {
            throw new RuntimeException("尼玛，换个手机吧，换个能获取日期的！");
        }
        return (int) (TimeUtils.getTimeDistance(new Date(), date) % 2);
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getNorth() {
        switch (getDay()) {
            case 1:
                return "男生";
            case 0:
                return "女生";
            default:
                return "异常";
        }
    }

    @Override
    public String getSouth() {
        switch (getDay()) {
            case 1:
                return "女生";
            case 0:
                return "男生";
            default:
                return "异常";
        }
    }
}
