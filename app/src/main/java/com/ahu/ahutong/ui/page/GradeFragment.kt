package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Grade
import com.ahu.ahutong.databinding.FragmentGradeBinding
import com.ahu.ahutong.databinding.ItemGradeBinding
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.page.state.GradeViewModel
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * 成绩单
 */
class GradeFragment : BaseFragment<FragmentGradeBinding>(), AdapterView.OnItemSelectedListener {
    private lateinit var mState: GradeViewModel
    private var adapter: BaseAdapter<Grade.TermGradeListBean.GradeListBean, ItemGradeBinding>? =
        null

    override fun initViewModel() {
        mState = getFragmentScopeViewModel(GradeViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_grade, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun observeData() {
        super.observeData()/*
        mState.result.observe(this) {
            it.onSuccess {
                mState.grade = it
                mState.totalGradePointAverage = it.totalGradePointAverage
                if (mState.schoolTerm.value == null || mState.schoolYear.value == null) {
                    Toast.makeText(requireContext(), "请选择学年，学期", Toast.LENGTH_SHORT).show()
                    return@observe
                }
                upDateData()
            }.onFailure {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
            dataBinding.refreshLayout.isRefreshing = false
        }*/

        /*mState.schoolYear.observe(this) {
            upDateData()
        }

        mState.schoolTerm.observe(this) {
            upDateData()
        }*/
    }

    private fun upDateData() {
        if (mState.schoolYear != null && mState.schoolTerm != null) {
            val grade = mState.grade
            if (grade == null) {
                mState.getGarde()
                return
            }
            var yearGrade = 0f
            var termTotal = 0f
            for (termGrade in grade.termGradeList) {
                if (termGrade.schoolYear.equals(mState.schoolYear)) {
                    yearGrade += (termGrade.termGradePointAverage.toFloat() * termGrade.termGradePoint.toFloat())
                    termTotal += termGrade.termGradePoint.toFloat()
                    if (termGrade.term.equals(mState.schoolTerm)) {
                        mState.termGradePointAverage = termGrade.termGradePointAverage
                        adapter?.submitList(termGrade.gradeList)
                    }
                }
            }
            if (termTotal == 0f) {
                mState.yearGradePointAverage = "暂无"
            } else {
                val gradePointAverage = yearGrade / termTotal
                val df = DecimalFormat("#.##")
                df.roundingMode = RoundingMode.HALF_UP
                mState.yearGradePointAverage = df.format(gradePointAverage)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 设置sp
        dataBinding.spSchoolYear.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            GradeViewModel.schoolYears
        )
        dataBinding.spTerm.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            GradeViewModel.terms.keys.toTypedArray()
        )
        dataBinding.spSchoolYear.onItemSelectedListener = this
        dataBinding.spTerm.onItemSelectedListener = this
        // recycler
        dataBinding.recycleGrades.layoutManager = LinearLayoutManager(requireContext())
        adapter = object : BaseAdapter<Grade.TermGradeListBean.GradeListBean, ItemGradeBinding>() {
            override fun layout(): Int {
                return R.layout.item_grade
            }

            override fun bindingData(
                binding: ItemGradeBinding,
                data: Grade.TermGradeListBean.GradeListBean
            ) {
                binding.grade = data
            }
        }
        dataBinding.recycleGrades.adapter = adapter

        dataBinding.refreshLayout.setOnRefreshListener {
            mState.getGarde(true)
        }

        mState.getGarde()
        dataBinding.refreshLayout.isRefreshing = true
    }

    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        parent?.let {
            when (it.id) {
                R.id.sp_schoolYear -> {
                    mState.schoolYear = GradeViewModel.schoolYears[position]
                }

                R.id.sp_term -> {
                    mState.schoolTerm = GradeViewModel.terms.keys.toTypedArray()[position]
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        parent?.let {
            when (it.id) {
                R.id.sp_schoolYear -> {
                    mState.schoolYear = null
                }

                R.id.sp_term -> {
                    mState.schoolTerm = null
                }
            }
        }
    }
}
