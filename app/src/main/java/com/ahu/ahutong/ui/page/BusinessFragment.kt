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
import com.ahu.ahutong.databinding.FragmentBusinessBinding
import com.ahu.ahutong.databinding.ItemBusinessBinding
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.page.state.BusinessViewModel

class BusinessFragment() : BaseFragment<FragmentBusinessBinding>() {
    private lateinit var mState: BusinessViewModel

    override fun initViewModel() {
        mState = getFragmentScopeViewModel(BusinessViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_business, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.recyclerBusiness.layoutManager = LinearLayoutManager(requireContext())
        dataBinding.recyclerBusiness.adapter =
            object : BaseAdapter<Developer, ItemBusinessBinding>(mState.partner) {
                override fun layout(): Int {
                    return R.layout.item_business
                }

                override fun bindingData(binding: ItemBusinessBinding, data: Developer) {
                    binding.dev = data
                    binding.proxy = ClickProxy()
                }
            }
    }

    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }

        fun gotoQQ(view: View, uri: String) {
            try {
                val i1 = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                i1.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                view.context.startActivity(i1)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "您并没有安装QQ或者Tim", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
