package com.ahu.ahutong.common;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ObservableData<T> {
    private T value;

    private List<Observer<T>> observerList = new ArrayList<>();

    public ObservableData(T value) {
        this.value = value;
    }

    public void addObserver(@NonNull Observer<T> observer) {
        observerList.add(observer);
        observer.onDataChanged(this.value);
    }

    public void setValue(T value) {
        this.value = value;
        for (Observer<T> observer : observerList) {
            observer.onDataChanged(this.value);
        }
    }
}
