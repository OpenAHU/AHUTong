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
import com.ahu.ahutong.databinding.FragmentDeveloperBinding
import com.ahu.ahutong.databinding.ItemDeveloperBinding
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.page.state.DeveloperViewModel
import java.lang.Exception

/**
 *
 * @Author: SinkDev
 * @Date: 2021/7/31-下午8:40
 * @Email: 468766131@qq.com
 */
class DeveloperFragment() : BaseFragment<FragmentDeveloperBinding>(){
    private lateinit var mState: DeveloperViewModel

    override fun initViewModel() {
        mState = getFragmentScopeViewModel(DeveloperViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_developer, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.recyclerDeveloper.layoutManager = LinearLayoutManager(requireContext())
        dataBinding.recyclerDeveloper.adapter = object: BaseAdapter<Developer, ItemDeveloperBinding>(mState.developers){
            override fun layout(): Int {
                return R.layout.item_developer
            }
            override fun bindingData(binding: ItemDeveloperBinding, data: Developer) {
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
                i1.action = Intent.ACTION_VIEW
                view.context.startActivity(i1)
            }catch (e: Exception){
                Toast.makeText(requireContext(), "您并没有安装QQ或者Tim", Toast.LENGTH_SHORT).show()
            }

        }
    }




}