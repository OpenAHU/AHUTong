package com.ahu.ahutong.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @Author: SinkDev
 * @Date: 2021/8/7-下午5:41
 * @Email: 468766131@qq.com
 */
public class News {

    @SerializedName("department")
    private String department;
    @SerializedName("author")
    private String author;
    @SerializedName("title")
    private String title;
    @SerializedName("abstract")
    private String abstractX;
    @SerializedName("releaseTime")
    private String releaseTime;
    @SerializedName("detailUrl")
    private String detailUrl;
    @SerializedName("imageUrlList")
    private List<String> imageUrlList;

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstractX() {
        return abstractX;
    }

    public void setAbstractX(String abstractX) {
        this.abstractX = abstractX;
    }

    public String getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(String releaseTime) {
        this.releaseTime = releaseTime;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public List<String> getImageUrlList() {
        return imageUrlList;
    }

    public void setImageUrlList(List<String> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }
}
