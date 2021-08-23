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
import com.ahu.ahutong.databinding.ItemExamBinding
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.page.state.ExamViewModel

/**
 * 考场查询
 */
class ExamFragment : BaseFragment<FragmentExamBinding>() {
    private lateinit var mState: ExamViewModel
    private var adapter: BaseAdapter<Exam, ItemExamBinding>? = null
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(ExamViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_exam, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.recyclerExams.layoutManager = LinearLayoutManager(context)
        adapter = object : BaseAdapter<Exam, ItemExamBinding>() {
            override fun layout(): Int {
                return R.layout.item_exam
            }

            override fun bindingData(binding: ItemExamBinding, data: Exam) {
                binding.examInfo = data
            }
        }
        dataBinding.recyclerExams.adapter = adapter
        //刷新
        dataBinding.refreshLayout.setOnRefreshListener {
            mState.loadExam(true)
        }

        //加载数据
        mState.loadExam()
        dataBinding.refreshLayout.isRefreshing = true

    }

    override fun observeData() {
        mState.data.observe(this){
            it.onSuccess {
                mState.size.value = it.size
                if(it.size == 0){
                    Toast.makeText(requireContext(), "当前没有考试信息。", Toast.LENGTH_SHORT).show()
                }
                adapter?.submitList(it)
            }.onFailure {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
            dataBinding.refreshLayout.isRefreshing = false
        }
    }

    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }
    }


}