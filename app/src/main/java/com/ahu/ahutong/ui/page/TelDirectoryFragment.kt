package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Tel
import com.ahu.ahutong.databinding.FragmentBathroomBinding
import com.ahu.ahutong.databinding.FragmentTeldirectoryBinding
import com.ahu.ahutong.databinding.ItemTelBinding
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.page.state.BathRoomViewModel
import com.ahu.ahutong.ui.page.state.TelDirectoryViewModel

class TelDirectoryFragment : BaseFragment<FragmentTeldirectoryBinding>() {
    private lateinit var mState: TelDirectoryViewModel
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(TelDirectoryViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_teldirectory, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.recyclerPhone.layoutManager=
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        dataBinding.recyclerPhone.adapter= object : BaseAdapter<Tel,ItemTelBinding>() {
            override fun layout(): Int {
                return R.layout.item_tel
            }

            override fun bindingData(binding: ItemTelBinding, data: Tel) {

            }

        }
    }


    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }
    }


}