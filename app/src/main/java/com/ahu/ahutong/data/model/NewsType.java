package com.ahu.ahutong.data.model;

/**
 * @Author: SinkDev
 * @Date: 2021/8/5-下午5:04
 * @Email: 468766131@qq.com
 */
public enum NewsType {
    College("院系风采", "colloge"),
    Association("社团动态", "association"),
    Edu("教务通知", "edu"),
    Recruit("校招实习", "recruit");

    final String value;
    final String type;
    NewsType(String value, String type) {
        this.value = value;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
