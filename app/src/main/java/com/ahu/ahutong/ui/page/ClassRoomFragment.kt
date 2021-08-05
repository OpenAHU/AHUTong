package com.ahu.ahutong.ui.page

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Developer
import com.ahu.ahutong.databinding.FragmentClassroomBinding
import com.ahu.ahutong.databinding.FragmentDeveloperBinding
import com.ahu.ahutong.databinding.ItemDeveloperBinding
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.page.state.ClassRoomViewModel
import com.ahu.ahutong.ui.page.state.DeveloperViewModel
import java.lang.Exception

class ClassRoomFragment : BaseFragment<FragmentClassroomBinding>(){
    private lateinit var mState: ClassRoomViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(ClassRoomViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_classroom, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    inner class ClickProxy {

    }




}