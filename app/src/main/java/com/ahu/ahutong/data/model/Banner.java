package com.ahu.ahutong.data.model;

import com.google.gson.annotations.SerializedName;

import java.net.MalformedURLException;
import java.net.URL;

public class Banner {

    @SerializedName("title")
    private String title;
    @SerializedName("detailUrl")
    private String detailUrl;
    @SerializedName("imgUrl")
    private String imageUrl;
    @SerializedName("orderWeight")
    private Double orderWeight;
    @SerializedName("id")
    private Integer id;

    public Double getOrderWeight() {
        return orderWeight;
    }

    public void setOrderWeight(Double orderWeight) {
        this.orderWeight = orderWeight;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public boolean isLegal() {
        try {
            String protocol = new URL(detailUrl).getProtocol();
            String protocol1 = new URL(imageUrl).getProtocol();
            return protocol1.matches("https?") && imageUrl.startsWith(protocol1);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

