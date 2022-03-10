package com.ahu.ahutong.common;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

public class SingleLiveEvent<T> extends MutableLiveData<T> {

    private static final String TAG = "SingleLiveEvent";

    private final AtomicBoolean mPending = new AtomicBoolean(false);

    private Handler handler = new Handler(Looper.getMainLooper());

    @MainThread
    public void observe(LifecycleOwner owner, final Observer<? super T> observer) {

        if (hasActiveObservers()) {
            Log.w(TAG, "Multiple observers registered but only one will be notified of changes.");
        }

        // Observe the internal MutableLiveData
        super.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(@Nullable T t) {
                if (mPending.compareAndSet(true, false)) {  //
                    observer.onChanged(t);
                }
            }
        });
    }

    /**
     * modify by bzb
     * 当 setValue时 mPending.set(true) compareAndSet(true,false) （true(set(true)后的“原值”) == true(except 值)）=true  -> mPending.set(false（update值）)  所以 再次 observer
     * 时 该值为false 所以不会执行 onChanged（t）方法
     *
     * @param t
     */
    @MainThread
    public void setValue(@Nullable T t) {
        mPending.set(true);
        super.setValue(t);
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    public void call() {
        setValue(null);
    }

    @Override
    public void postValue(T value) {
        handler.post(() -> setValue(value));
    }

    public void callFromOtherThread() {
        handler.post(this::call);
    }
}