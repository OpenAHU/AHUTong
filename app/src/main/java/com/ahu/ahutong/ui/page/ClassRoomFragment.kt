package com.ahu.ahutong.ui.page


import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import arch.sink.ui.page.BaseFragment
import arch.sink.ui.page.DataBindingConfig
import com.ahu.ahutong.BR
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Room
import com.ahu.ahutong.databinding.FragmentClassroomBinding
import com.ahu.ahutong.databinding.ItemEmptyRoomBinding
import com.ahu.ahutong.ui.adapter.base.BaseAdapter
import com.ahu.ahutong.ui.dialog.ClassRoomDialog
import com.ahu.ahutong.ui.page.state.ClassRoomViewModel

class ClassRoomFragment : BaseFragment<FragmentClassroomBinding>(), ClassRoomDialog.CallBack {
    private lateinit var mState: ClassRoomViewModel
    private var adapter: BaseAdapter<Room, ItemEmptyRoomBinding>? = null
    override fun initViewModel() {
        mState = getFragmentScopeViewModel(ClassRoomViewModel::class.java)
    }

    override fun getDataBindingConfig(): DataBindingConfig {
        return DataBindingConfig(R.layout.fragment_classroom, BR.state, mState)
            .addBindingParam(BR.proxy, ClickProxy())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataBinding.recycleRooms.layoutManager = LinearLayoutManager(requireContext())
        adapter = object : BaseAdapter<Room, ItemEmptyRoomBinding>(){
            override fun layout(): Int {
                return R.layout.item_empty_room
            }

            override fun bindingData(binding: ItemEmptyRoomBinding, data: Room) {
                binding.room = data
            }
        }
        dataBinding.recycleRooms.adapter = adapter
    }

    override fun observeData() {
        super.observeData()
        mState.rooms.observe(this){
            it.onSuccess {
                adapter?.submitList(it)
            }.onFailure {
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun dialogCallBack(campus: String, time: String) {
        mState.campus.value = campus
        mState.time.value = time
    }

    inner class ClickProxy {
        val back: (() -> Unit) = {
            nav().popBackStack()
        }

        fun selectTime(view: View) {
            val indexOfCampus = ClassRoomViewModel.campuses.indexOf(mState.campus.value)
            val indexOfTime = ClassRoomViewModel.times.indexOf(mState.time.value)
            val dialog: ClassRoomDialog = ClassRoomDialog(indexOfCampus, indexOfTime)
            dialog.show(parentFragmentManager, "ClassRoomPicker")
            dialog.setCallBack(this@ClassRoomFragment)
        }

        fun search(){
            mState.searchEmptyRoom(mState.campus.value, mState.time.value)
        }
    }


}