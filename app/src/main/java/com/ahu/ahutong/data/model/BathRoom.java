package com.ahu.ahutong.data.model;

import com.google.gson.annotations.SerializedName;

public class BathRoom {
    @SerializedName("openStatus")
    private String openStatus;
    @SerializedName("bathroom")
    private String bathroom;

    public String getOpenStatus() {
        return openStatus;
    }

    public void setOpenStatus(String openStatus) {
        this.openStatus = openStatus;
    }

    public String getBathroom() {
        return bathroom;
    }

    public void setBathroom(String bathroom) {
        this.bathroom = bathroom;
    }

    @Override
    public String toString() {
        return "BathRoom{" +
                "openStatus='" + openStatus + '\'' +
                ", bathroom='" + bathroom + '\'' +
                '}';
    }
}
