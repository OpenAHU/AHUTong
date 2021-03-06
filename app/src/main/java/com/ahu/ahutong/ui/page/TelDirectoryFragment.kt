package com.ahu.ahutong.ui.page

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.recyclerview.widget.LinearLayoutManager
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Tel
import com.ahu.ahutong.databinding.FragmentTeldirectoryBinding
import com.ahu.ahutong.databinding.ItemTelBinding
import com.ahu.ahutong.ext.dp
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
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
        for ((index, types) in TelDirectoryViewModel.TelBook.keys.withIndex()) {
            val radioButton =
                LayoutInflater.from(context).inflate(R.layout.item_type, null, false) as RadioButton
            val layoutParams = LinearLayout.LayoutParams(-1, -2)
            layoutParams.marginStart = 20.dp.toInt()
            layoutParams.marginEnd = 20.dp.toInt()
            layoutParams.topMargin = 10.dp.toInt()
            layoutParams.bottomMargin = 10.dp.toInt()
            radioButton.layoutParams = layoutParams
            radioButton.text = types;
            radioButton.id = index;
            radioButton.setOnCheckedChangeListener { t, isChecked ->
                if (isChecked)mState.theme.theme.setItem(t,true)
                else t.setBackgroundColor(Color.TRANSPARENT)
            }
            dataBinding.recyclerType.addView(radioButton)
        }
        dataBinding.recyclerTel.layoutManager =
            LinearLayoutManager(context)
        dataBinding.recyclerType.setOnCheckedChangeListener { radioGroup, i ->
            val btn = radioGroup.findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
            dataBinding.recyclerTel.adapter = object :
                BaseAdapter<Tel, ItemTelBinding>(TelDirectoryViewModel.TelBook.values.toList()[radioGroup.checkedRadioButtonId]) {
                override fun layout(): Int {
                    return R.layout.item_tel
                }

                override fun bindingData(binding: ItemTelBinding, data: Tel) {
                    mState.theme.theme.setItem(binding.tag,true)
                   // binding.tag
                    binding.bean = data
                    binding.proxy=ClickProxy()
                }
            }
        }
        dataBinding.recyclerType.check(0)
    }


    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }
        fun gotoTel(view: View, tel: String) {
            val dialIntent =
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel")) //????????????????????????????????????????????????
            startActivity(dialIntent)
        }
    }


}