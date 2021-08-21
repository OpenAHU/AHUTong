package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.util.Log
import android.view.View
import arch.sink.ui.page.BaseFragment
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.model.Rubbish
import com.ahu.ahutong.databinding.FragmentGarbageBinding
import com.ahu.ahutong.databinding.ItemRubbishBinding
import com.ahu.ahutong.ui.page.state.GarbageViewModel
import com.ahu.ahutong.ui.adapter.base.BaseAdapter

/**
 * API: https://api.tianapi.com/txapi/lajifenlei/?key=367f6d1bd8e7cacbb14485af77f1ed6b&word=
 */
class GarbageFragment : BaseFragment<FragmentGarbageBinding>() {
    private lateinit var mState: GarbageViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(GarbageViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_garbage, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ClickProxy().refresh()
        dataBinding.rvResult.layoutManager=LinearLayoutManager(context,RecyclerView.VERTICAL,false)
        /**
         * 不懂如何使用T_T，这个页面等你写吧
         */
//        dataBinding.rvResult.adapter= object : BaseAdapter<Rubbish,ItemRubbishBinding> (mState.liveData.value?.getOrNull()!!) {
//
//            override fun layout(): Int {
//               return R.layout.item_rubbish
//            }
//
//            override fun bindingData(binding: ItemRubbishBinding, data: Rubbish) {
//                binding.bean=data
//            }
//        }
    }


    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }
        fun refresh(){
            dataBinding.recommend.removeAllViews()
                mState.random.shuffled().take(4).forEach {
                    val view=TextView(context)
                    view.text=it
                    dataBinding.recommend.addView(view)
                }

        }
    }


}