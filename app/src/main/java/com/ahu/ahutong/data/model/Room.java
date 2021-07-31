package com.ahu.ahutong.data.model;

import com.google.gson.annotations.SerializedName;

public class Room {

    @SerializedName("pos")
    private String pos;
    @SerializedName("seating")
    private String seating;

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getSeating() {
        return seating;
    }

    public void setSeating(String seating) {
        this.seating = seating;
    }
}
