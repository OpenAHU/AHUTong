package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Exam
import com.ahu.ahutong.databinding.FragmentExamBinding
import com.ahu.ahutong.databinding.FragmentTeldirectoryBinding
import com.ahu.ahutong.databinding.ItemExamBinding
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.page.state.ExamViewModel
import com.ahu.ahutong.ui.page.state.TelDirectoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 考场查询
 */
class ExamFragment : BaseFragment<FragmentExamBinding>() {
    private lateinit var mState: ExamViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(ExamViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_exam, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.recyclerExams.layoutManager=LinearLayoutManager(context)
        dataBinding.recyclerExams.adapter= object : BaseAdapter<Exam,ItemExamBinding>(mState.data) {
            override fun layout(): Int {
                return R.layout.item_exam
            }

            override fun bindingData(binding: ItemExamBinding, data: Exam) {
                binding.examInfo=data
            }
        }
    }

    override fun observeData() {
        dataBinding.refreshLayout.setOnRefreshListener {
            Toast.makeText(context,"",1).show()
            GlobalScope.launch(Dispatchers.Default) {//简简单单用一下伪协程（在此留下思考：当前页面退出时，会不会未停止协程导致内存泄漏）2021：8：13
                Thread.sleep(5000)
                dataBinding.refreshLayout.isRefreshing=false
                //回应上面的问题：会内存泄漏，也会出现null安全导致app闪退 2021:8:14
                /*
                Kotlin在这部分的编码如下
                 ResultKt.throwOnFailure(var1);
                     Thread.sleep(5000L);
                     SwipeRefreshLayout var10000 = ExamFragment.access$getDataBinding$p(ExamFragment.this).refreshLayout;
                     Intrinsics.checkNotNullExpressionValue(var10000, "dataBinding.refreshLayout");
                     var10000.setRefreshing(false);//很明显这里会有问题
                 */
                //然而kt能准确的在log中发现出错的代码在 dataBinding.refreshLayout.isRefreshing=false 这行
                //原来是经过反编译以后发现，smali 中的 .line 是对应的kt的行数
                //留下思考：怎么能优雅的解决周期不同步导致的null安全问题
                //回应：使用lifecycle
            }
        }
    }

    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }
    }


}