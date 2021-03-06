package com.ahu.ahutong.ui.page

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.MainActivity
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Rubbish
import com.ahu.ahutong.databinding.FragmentGarbageBinding
import com.ahu.ahutong.ui.adapter.RubbishAdapter
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

    override fun observeData() {
        super.observeData()
        mState.result.observe(this) {
            it.onSuccess {
                dataBinding.recyclerRubbish.adapter = RubbishAdapter(it, R.layout.item_rubbish)
            }.onFailure {
                Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        activity.setSupportActionBar(dataBinding.toolbar)
        setHasOptionsMenu(true)
        dataBinding.recyclerRubbish.layoutManager = LinearLayoutManager(context)
        dataBinding.recyclerRubbish.adapter = RubbishAdapter(Rubbish.types, R.layout.item_rubbish_type)
        dataBinding.edKeyword.setOnEditorActionListener(actionListener)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //??????????????????
        if (item.itemId == R.id.search_button) {
            val keyWord = dataBinding.edKeyword.text.toString()
            if (keyWord.isNotEmpty()) {
                //??????
                mState.search(keyWord)
            } else {
                Toast.makeText(requireContext(), "?????????????????????", Toast.LENGTH_SHORT).show()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }

    }

    private val actionListener =
        OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyWord = dataBinding.edKeyword.text.toString()
                if (keyWord.isNotEmpty()) {
                    //??????
                    mState.search(keyWord)
                } else {
                    Toast.makeText(requireContext(), "?????????????????????", Toast.LENGTH_SHORT).show()
                }
            }
            false
        }


}