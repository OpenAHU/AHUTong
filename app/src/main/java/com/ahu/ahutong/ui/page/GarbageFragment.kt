package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.databinding.FragmentGarbageBinding
import com.ahu.ahutong.ui.page.state.GarbageViewModel

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
        dataBinding.rvResult.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)


    }


    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }

        fun refresh() {
            dataBinding.recommend.removeAllViews()
            mState.random.shuffled().take(4).forEach {
                val view = TextView(context)
                view.text = "%s ".format(it)
                dataBinding.recommend.addView(view)
            }

        }
    }


}