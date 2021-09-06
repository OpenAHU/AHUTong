package com.ahu.ahutong.data.model;

import com.google.gson.annotations.SerializedName;

public class Banner {

    @SerializedName("title")
    private String title;
    @SerializedName("detailUrl")
    private String detailUrl;
    @SerializedName("imageUrl")
    private String imageUrl;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
