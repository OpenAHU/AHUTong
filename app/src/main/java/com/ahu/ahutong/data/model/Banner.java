package com.ahu.ahutong.data.model;

public class Banner {
    private String imgPath="http://39.106.7.220/img/img.php";
    private String targetUrl = "";
    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}
