package com.ahu.ahutong.data.mock

import android.content.Context
import com.ahu.ahutong.AHUApplication
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object MockScenarioStore {
    private const val PREFS_NAME = "ahutong_mock_scenarios"
    private const val KEY_ACTIVE_SCENARIO_ID = "active_scenario_id"

    fun getActiveScenarioId(): String =
        prefs().getString(KEY_ACTIVE_SCENARIO_ID, null)
            ?: MockScenarioRegistry.defaultScenario.id

    fun setActiveScenarioId(id: String) {
        prefs()
            .edit()
            .putString(KEY_ACTIVE_SCENARIO_ID, id)
            .apply()
    }

    fun clear() {
        prefs()
            .edit()
            .remove(KEY_ACTIVE_SCENARIO_ID)
            .apply()
    }

    private fun prefs() =
        AHUApplication.getApp().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

object MockScenarioController {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val refreshRevision = MutableStateFlow(0L)

    fun scenarios(): List<MockScenarioOption> =
        MockScenarioRegistry.scenarios.map { it.option() }

    fun activeScenarioId(): String =
        MockScenarioStore.getActiveScenarioId()

    fun activeScenario(): MockScenario =
        MockScenarioRegistry.find(activeScenarioId())

    fun selectScenario(id: String): MockScenarioOption {
        val scenario = MockScenarioRegistry.find(id)
        MockScenarioStore.setActiveScenarioId(scenario.id)
        notifyDataChanged()
        return scenario.option()
    }

    fun resetScenario(): MockScenarioOption {
        MockScenarioStore.clear()
        notifyDataChanged()
        return MockScenarioRegistry.defaultScenario.option()
    }

    fun activeDiagnostics(): List<String> =
        MockScenarioDiagnostics.report(activeScenario())

    fun activeValidationIssues(): List<String> =
        MockScenarioDiagnostics.validate(activeScenario())

    fun editableEndpoints(): List<MockEditableEndpointOption> =
        MockEditableEndpoint.entries.map { it.option() }

    fun endpointText(key: String): String {
        val endpoint = MockEditableEndpoint.fromKey(key)
        return MockOverrideStore.get(endpoint) ?: defaultText(endpoint)
    }

    fun endpointHasOverride(key: String): Boolean =
        MockOverrideStore.get(MockEditableEndpoint.fromKey(key)) != null

    fun saveEndpointText(key: String, value: String): String? {
        val endpoint = MockEditableEndpoint.fromKey(key)
        val error = validateEndpointText(endpoint, value)
        if (error != null) return error
        MockOverrideStore.set(endpoint, value)
        notifyDataChanged()
        return null
    }

    fun resetEndpointText(key: String): String {
        val endpoint = MockEditableEndpoint.fromKey(key)
        MockOverrideStore.clear(endpoint)
        notifyDataChanged()
        return defaultText(endpoint)
    }

    fun resetAllEndpointText() {
        MockOverrideStore.clearAll()
        notifyDataChanged()
    }

    fun overriddenEndpointCount(): Int =
        MockOverrideStore.overriddenKeys().size

    fun refreshRevisions(): StateFlow<Long> =
        refreshRevision.asStateFlow()

    private fun notifyDataChanged() {
        refreshRevision.value += 1
    }

    private fun defaultText(endpoint: MockEditableEndpoint): String {
        val scenario = activeScenario()
        return when (endpoint) {
            MockEditableEndpoint.CurrentSchedule -> gson.toJson(scenario.academic.currentSchedule)
            MockEditableEndpoint.NextSchedule -> gson.toJson(scenario.academic.nextSchedule)
            MockEditableEndpoint.Grade -> gson.toJson(scenario.academic.grade)
            MockEditableEndpoint.GpaRank -> gson.toJson(scenario.academic.gpaRankInfo)
            MockEditableEndpoint.Exams -> gson.toJson(scenario.academic.exams)
            MockEditableEndpoint.CardMoney -> gson.toJson(scenario.payment.cardMoney)
            MockEditableEndpoint.CardInfo -> gson.toJson(scenario.payment.cardInfo)
            MockEditableEndpoint.Bathrooms -> gson.toJson(scenario.campus.bathrooms)
            MockEditableEndpoint.BathroomAccounts -> gson.toJson(scenario.campus.bathroomAccounts)
            MockEditableEndpoint.LostFoundCampuses -> gson.toJson(scenario.discovery.lostFoundCampuses)
            MockEditableEndpoint.LostFoundTypes -> gson.toJson(scenario.discovery.lostFoundTypes)
            MockEditableEndpoint.LostFoundItems -> gson.toJson(scenario.discovery.lostFoundItems)
            MockEditableEndpoint.ClassroomBuildings -> gson.toJson(scenario.campus.classroomBuildings)
            MockEditableEndpoint.ClassroomRooms -> gson.toJson(scenario.campus.classroomRooms)
            MockEditableEndpoint.SchoolCalendar -> scenario.campus.calendarJpegBase64
            MockEditableEndpoint.Behavior -> gson.toJson(scenario.behavior)
        }
    }

    private fun validateEndpointText(
        endpoint: MockEditableEndpoint,
        value: String
    ): String? {
        if (value.isBlank()) return "内容不能为空"
        if (endpoint == MockEditableEndpoint.SchoolCalendar) return null
        return runCatching {
            com.google.gson.JsonParser.parseString(value)
        }.fold(
            onSuccess = { null },
            onFailure = { "JSON 格式错误：${it.message}" }
        )
    }
}
