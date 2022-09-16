package arch.sink.ui.page;

import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;


/**
 * @Author SinkDev
 * @Date 2021/7/18-17:20
 * @Email 468766131@qq.com
 */
public class DataBindingConfig {
    private final int layout;
    @NonNull
    private final SparseArray<Object> bindingParams = new SparseArray<Object>();

    public DataBindingConfig(int layout, int vmVarId, @NonNull ViewModel stateViewModel) {
        this.layout = layout;
        bindingParams.put(vmVarId, stateViewModel);
    }

    /**
     * 获取layout Id
     *
     * @return layoutId
     */
    public int getLayout() {
        return layout;
    }

    @NonNull
    public SparseArray<Object> getBindingParams() {
        return bindingParams;
    }

    /**
     * 添加BindingParams
     *
     * @param id     id
     * @param object object
     */
    public DataBindingConfig addBindingParam(int id, @NonNull Object object) {
        bindingParams.put(id, object);
        return this;
    }

}
