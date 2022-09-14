package com.ahu.ahutong.data;

import androidx.annotation.NonNull;

/**
 * @Author: Sink
 * @Date: 2021/7/31-下午8:40
 * @Email: 468766131@qq.com
 */
public class AHUResponse<T> {
    private T data;
    private String msg;
    private Integer code;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public boolean isSuccessful(){
        return code == 0;
    }

    @NonNull
    @Override
    public String toString() {
        return "AHUResponse{" +
                "data=" + data +
                ", msg='" + msg + '\'' +
                ", code=" + code +
                '}';
    }
}
