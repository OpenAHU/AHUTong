package com.ahu.ahutong.ui.page

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.License
import com.ahu.ahutong.databinding.FragmentLicenseBinding
import com.ahu.ahutong.databinding.ItemLicenseBinding
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.page.state.LicenseViewModel

/**
 * @Author: SinkDev
 * @Date: 2021/8/26-下午6:29
 * @Email: 468766131@qq.com
 */
class LicenseFragment : BaseFragment<FragmentLicenseBinding>() {
    private lateinit var mState: LicenseViewModel

    override fun initViewModel() {
        mState = getFragmentScopeViewModel(LicenseViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_license, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.recyclerLicense.layoutManager = LinearLayoutManager(requireContext())
        dataBinding.recyclerLicense.adapter =
            object : BaseAdapter<License, ItemLicenseBinding>(mState.license) {
                override fun layout(): Int {
                    return R.layout.item_license
                }

                override fun bindingData(binding: ItemLicenseBinding, data: License) {
                    binding.license = data
                    binding.root.setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(data.url)
                        startActivity(intent)
                    }
                }

            }
    }

    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }
    }

}