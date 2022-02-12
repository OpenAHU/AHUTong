package com.ahu.ahutong.data.model;

import com.google.gson.annotations.SerializedName;

public class AppVersion {

    @SerializedName("version")
    private String version;
    @SerializedName("msg")
    private String message;
    @SerializedName("url")
    private String url;
    @SerializedName("plugVersion")
    private String plugVersion;
    @SerializedName("plugUrl")
    private String plugUrl;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPlugVersion() {
        return plugVersion;
    }

    public void setPlugVersion(String plugVersion) {
        this.plugVersion = plugVersion;
    }

    public String getPlugUrl() {
        return plugUrl;
    }

    public void setPlugUrl(String plugUrl) {
        this.plugUrl = plugUrl;
    }

}
