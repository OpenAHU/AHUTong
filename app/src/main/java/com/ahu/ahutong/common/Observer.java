package com.ahu.ahutong.common;

public interface Observer<T> {
    void onDataChanged(T value);
}
