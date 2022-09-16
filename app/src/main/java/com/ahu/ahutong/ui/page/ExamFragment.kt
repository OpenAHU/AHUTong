package com.ahu.ahutong.ui.page

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
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
            mState.loadExam()
        }

        //加载数据
        mState.loadExam(false)
        dataBinding.refreshLayout.isRefreshing = true

    }

    @SuppressLint("SetTextI18n")
    override fun observeData() {
        mState.data.observe(this) { result ->
            result.onSuccess {
                if (it.isEmpty())
                    Toast.makeText(requireContext(), R.string.empty_exam, Toast.LENGTH_SHORT).show()
                else
                    dataBinding.cardTip.text = "共 ${it.size} 条考试信息"
                adapter?.submitList(it)
            }.onFailure {
                dataBinding.cardTip.text = it.message
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