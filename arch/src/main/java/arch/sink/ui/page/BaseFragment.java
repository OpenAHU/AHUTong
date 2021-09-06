package arch.sink.ui.page;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import org.jetbrains.annotations.NotNull;

import arch.sink.BaseApplication;

/**
 * @Author SinkDev
 * @Date 2021/7/18-17:17
 * @Email 468766131@qq.com
 */
public abstract class BaseFragment<R extends ViewDataBinding> extends Fragment {

    protected R dataBinding;

    //ViewModelProvider
    private ViewModelProvider mApplicationVMProvider;
    private ViewModelProvider mActivityVMProvider;
    private ViewModelProvider mFragmentVMProvider;

    protected abstract void initViewModel();


    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewModel();
    }

    @NonNull
    protected abstract DataBindingConfig getDataBindingConfig();



    protected void observeData(){

    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DataBindingConfig dataBindingConfig = getDataBindingConfig();
        //DataBinding

        dataBinding = DataBindingUtil.inflate(inflater, dataBindingConfig.getLayout(), container, false);
        dataBinding.setLifecycleOwner(this);
        SparseArray<Object> bindingParams = dataBindingConfig.getBindingParams();
        //设置变量
        for (int i = 0; i < bindingParams.size(); i++) {
            dataBinding.setVariable(bindingParams.keyAt(i), bindingParams.valueAt(i));
        }
        observeData();
        return dataBinding.getRoot();
    }

    protected NavController nav(){
        return NavHostFragment.findNavController(this);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dataBinding.unbind();
        dataBinding = null;
    }

    /**
     * 获取Application级的ViewModel
     * @param viewModelClass ViewModel Class
     * @return Application级的ViewModel
     */
    protected <T extends ViewModel> T getApplicationScopeViewModel(Class<T> viewModelClass){
        if (getActivity() == null) {
            throw new IllegalStateException("Can't create ViewModelProvider for detached fragment");
        }
        if (mApplicationVMProvider == null){
            BaseApplication application = (BaseApplication) getActivity().getApplication();
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
        if (getActivity() == null) {
            throw new IllegalStateException("Can't create ViewModelProvider for detached fragment");
        }
        if (mActivityVMProvider == null){
            mActivityVMProvider = new ViewModelProvider(getActivity());
        }
        return mActivityVMProvider.get(viewModelClass);
    }

    /**
     * 获取Fragment级的ViewModel
     * @param viewModelClass ViewModel Class
     * @return Fragment级的ViewModel
     */
    protected  <T extends ViewModel> T getFragmentScopeViewModel(Class<T> viewModelClass){
        if (mFragmentVMProvider == null){
            mFragmentVMProvider = new ViewModelProvider(this);
        }
        return mFragmentVMProvider.get(viewModelClass);
    }

}
