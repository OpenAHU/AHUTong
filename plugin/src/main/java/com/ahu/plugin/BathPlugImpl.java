package com.ahu.plugin;

import java.util.Calendar;

public class BathPlugImpl implements BathPlug{
    private int getWeek(){
        int week= Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        return week<0?7:week;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getNorth() {//1357女，246男
        switch (getWeek()){
            case 1:
            case 3:
            case 5:
            case 7:
                return "女生";
            default:
                return "男生";
        }
    }

    @Override
    public String getSouth() {//1357男，246女
        switch (getWeek()){
            case 1:
            case 3:
            case 5:
            case 7:
                return "男生";
            default:
                return "女生";
        }
    }
}
