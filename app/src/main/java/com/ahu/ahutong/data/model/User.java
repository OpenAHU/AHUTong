package com.ahu.ahutong.data.model;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("name")
    private String name;
    @SerializedName("xh")
    private String xh;

    public User() {

    }

    public User(String name) {
        this.name = name;
    }

    public User(String name,String xh){
        this.name = name;
        this.xh = xh;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getXh() {
        return xh;
    }

    public void setXh(String xh) {
        this.xh = xh;
    }

    public enum UserType {
        //使用智慧安大登录的
        AHU_Wisdom("2"),
        // 爬虫
        AHU_LOCAL("0");

        private final String type;

        UserType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        @NonNull
        @Override
        public String toString() {
            return type;
        }

    }

}
