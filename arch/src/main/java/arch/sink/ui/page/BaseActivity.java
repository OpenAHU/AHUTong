package arch.sink.ui.page;


import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import arch.sink.BaseApplication;
import arch.sink.data.manager.NetworkStateManager;
import arch.sink.ui.BarConfig;
import arch.sink.ui.SinkBar;
import arch.sink.utils.NightUtils;

/**
 * @Author SinkDev
 * @Date 2021/7/18-17:16
 * @Email 468766131@qq.com
 */
public abstract class BaseActivity<R extends  ViewDataBinding> extends AppCompatActivity {

    protected R dataBinding;
    //ViewModelProvider
    private ViewModelProvider mApplicationVMProvider;
    private ViewModelProvider mActivityVMProvider;
    //initViewModel
    public abstract void initViewModel();

    @NonNull
    protected abstract DataBindingConfig getDataBindingConfig();

    @NonNull
    protected BarConfig getBarConfig(){
        return new BarConfig();
    }

    protected void loadInitData(){

    }
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewModel();
        //网络状态监听启动
        getLifecycle().addObserver(NetworkStateManager.getInstance());
        //BarConfig
        BarConfig barConfig = getBarConfig();
        SinkBar.applyBarConfig(this, barConfig);
        //获取DataBinding的Config
        DataBindingConfig dataBindingConfig = getDataBindingConfig();
        //DataBinding
        dataBinding = DataBindingUtil.setContentView(this, dataBindingConfig.getLayout());
        dataBinding.setLifecycleOwner(this);
        SparseArray<Object> bindingParams = dataBindingConfig.getBindingParams();
        //设置变量
        for (int i = 0; i < bindingParams.size(); i++) {
            dataBinding.setVariable(bindingParams.keyAt(i), bindingParams.valueAt(i));
        }
        //加载数据
        loadInitData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataBinding.unbind();
        dataBinding = null;
    }

    /**
     * 获取Application级的ViewModel
     * @param viewModelClass ViewModel Class
     * @return Application级的ViewModel
     */
    protected <T extends ViewModel> T getApplicationScopeViewModel(Class<T> viewModelClass){
        if (mApplicationVMProvider == null){
            BaseApplication application = (BaseApplication) getApplication();
            mApplicationVMProvider = new ViewModelProvider(application,
                    ViewModelProvider.AndroidViewModelFactory.getInstance(application));
        }
        return mApplicationVMProvider.get(viewModelClass);
    }

    /**
     * 获取Activity级的ViewModel
     * @param viewModelClass ViewModel Class
     * @return Activity级的ViewModel
     */
    protected <T extends ViewModel> T getActivityScopeViewModel(Class<T> viewModelClass){
        if (mActivityVMProvider == null){
            mActivityVMProvider = new ViewModelProvider(this);
        }
        return mActivityVMProvider.get(viewModelClass);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.fontScale != 1) { //fontScale不为1，需要强制设置为1
            getResources();
        }
    }

    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        if (resources.getConfiguration().fontScale != 1) { //fontScale不为1，需要强制设置为1
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置成默认值，即fontScale为1
            resources.updateConfiguration(newConfig, resources.getDisplayMetrics());
        }
        return resources;
    }


}
