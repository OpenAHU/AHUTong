package com.ahu.ahutong;


import com.ahu.ahutong.common.ObservableData;
import com.ahu.ahutong.common.SingleLiveEvent;
import com.ahu.ahutong.data.model.User;
import com.ahu.plugin.BathPlug;
import com.ahu.plugin.BathPlugImpl;
import com.tencent.bugly.crashreport.CrashReport;


import arch.sink.BaseApplication;

/**
 * @Author SinkDev
 * @Date 2021/7/27-15:48
 * @Email 468766131@qq.com
 */
public class AHUApplication extends BaseApplication {
    // 初始默认值为 AHU_Wisdom
    public static ObservableData<User.UserType> loginType = new ObservableData<>(User.UserType.AHU_Wisdom);
    public static SingleLiveEvent<Boolean> retryLogin = new SingleLiveEvent<>();

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(this, "24521a5b56", BuildConfig.DEBUG);

    }

    private static final BathPlugImpl bath = new BathPlugImpl();

    /**
     * 返回浴室开放计算类
     *
     * @return 类
     */
    public static BathPlug getBath() {
        return bath;
    }
}
