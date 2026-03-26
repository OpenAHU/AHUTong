package com.ahu.ahutong.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.crawler.api.jwxt.JwxtApi
import com.ahu.ahutong.data.crawler.model.jwxt.DateTimeSegmentCmd
import com.ahu.ahutong.data.crawler.model.jwxt.FreeRoom
import com.ahu.ahutong.data.crawler.model.jwxt.GetBuildingsResponseItem
import com.ahu.ahutong.data.crawler.model.jwxt.GetFreeRoomsRequest
import com.ahu.ahutong.ext.launchSafe
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

class FreeClassroomViewModel : ViewModel() {
    val campusOptions = listOf(
        CampusOption(id = 1, name = "磬苑校区"),
        CampusOption(id = 2, name = "龙河校区")
    )
    val selectedCampusId = MutableStateFlow<Int?>(null)
    val buildings = MutableStateFlow<List<GetBuildingsResponseItem>>(emptyList())
    val selectedBuildingIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedUnits = MutableStateFlow<Set<Int>>(emptySet())
    val startDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val endDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val isLoadingBuildings = MutableStateFlow(false)
    val isSearching = MutableStateFlow(false)
    val freeRooms = MutableStateFlow<List<FreeRoom>>(emptyList())
    val errorMessage = MutableStateFlow<String?>(null)
    private val buildingsCache = mutableMapOf<Int, List<GetBuildingsResponseItem>>()

    init {
        selectCampus(1)
    }

    fun selectCampus(campusId: Int) = viewModelScope.launchSafe {
        if (selectedCampusId.value == campusId) return@launchSafe
        selectedCampusId.value = campusId
        selectedBuildingIds.value = emptySet()
        freeRooms.value = emptyList()
        loadBuildings(campusId)
    }

    fun toggleBuilding(buildingId: Int) {
        selectedBuildingIds.value = selectedBuildingIds.value.toMutableSet().apply {
            if (contains(buildingId)) remove(buildingId) else add(buildingId)
        }
    }

    fun toggleUnit(unit: Int) {
        selectedUnits.value = selectedUnits.value.toMutableSet().apply {
            if (contains(unit)) remove(unit) else add(unit)
        }
    }

    fun toggleUnitsRange(start: Int, end: Int) {
        val range = (start..end).toSet()
        val current = selectedUnits.value
        selectedUnits.value = if (range.all { it in current }) current - range else current + range
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        startDate.value = start
        endDate.value = end
    }

    fun setStartDate(date: LocalDate) {
        startDate.value = date
        if (endDate.value.isBefore(date)) {
            endDate.value = date
        }
    }

    fun setEndDate(date: LocalDate) {
        endDate.value = date
        if (startDate.value.isAfter(date)) {
            startDate.value = date
        }
    }

    fun searchFreeRooms() = viewModelScope.launchSafe {
        val campusId = selectedCampusId.value ?: run {
            errorMessage.value = "请先选择校区"
            return@launchSafe
        }
        val allBuildings = buildings.value
        if (allBuildings.isEmpty()) {
            errorMessage.value = "当前校区暂无教学楼数据"
            return@launchSafe
        }
        val buildingIds = if (selectedBuildingIds.value.isEmpty()) {
            allBuildings.map { it.id }
        } else {
            selectedBuildingIds.value.toList()
        }
        val units = if (selectedUnits.value.isEmpty()) {
            (1..13).map { it.toString() }
        } else {
            selectedUnits.value.sorted().map { it.toString() }
        }
        val start = startDate.value.toString()
        val end = endDate.value.toString()
        isSearching.value = true
        errorMessage.value = null
        runCatching {
            val allRooms = mutableListOf<FreeRoom>()
            buildingIds.forEach { buildingId ->
                val response = JwxtApi.API.getFreeRooms(
                    GetFreeRoomsRequest(
                        buildingId = buildingId.toString(),
                        campusId = campusId.toString(),
                        dateTimeSegmentCmd = DateTimeSegmentCmd(
                            startDateTime = start,
                            endDateTime = end,
                            units = units
                        )
                    )
                )
                allRooms += response.roomList
            }
            freeRooms.value = allRooms
                .distinctBy { "${it.id}-${it.building.id}" }
                .sortedWith(compareBy({ it.building.nameZh }, { it.floor }, { it.nameZh }))
        }.onFailure {
            errorMessage.value = it.message ?: "查询失败"
        }
        isSearching.value = false
    }

    private suspend fun loadBuildings(campusId: Int) {
        if (buildingsCache.containsKey(campusId)) {
            buildings.value = buildingsCache[campusId] ?: emptyList()
            return
        }
        isLoadingBuildings.value = true
        runCatching {
            val data = JwxtApi.API.getBuildings(campusId = campusId)
            val sortedData = data.sortedBy { it.nameZh }
            buildingsCache[campusId] = sortedData
            buildings.value = sortedData
        }.onFailure {
            buildings.value = emptyList()
            errorMessage.value = it.message ?: "获取教学楼失败"
        }
        isLoadingBuildings.value = false
    }
}

data class CampusOption(
    val id: Int,
    val name: String
)
