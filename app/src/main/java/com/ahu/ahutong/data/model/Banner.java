package com.ahu.ahutong.data.model;

import android.content.Intent;

public class Banner {
    private String imgPath;
    private Intent clickIntent;
    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public Intent getClickIntent() {
        return clickIntent;
    }

    public void setClickIntent(Intent clickIntent) {
        this.clickIntent = clickIntent;
    }
}
