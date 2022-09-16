package arch.sink;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import arch.sink.utils.Utils;


/**
 * @Author SinkDev
 * @Date 2021/7/18-16:56
 * @Email 468766131@qq.com
 */
public class BaseApplication extends Application implements ViewModelStoreOwner {
    //Application ViewModel
    private ViewModelStore mViewModelStore;


    @Override
    public void onCreate() {
        super.onCreate();
        mViewModelStore = new ViewModelStore();
        Utils.init(this);
    }

    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        return mViewModelStore;
    }
}
