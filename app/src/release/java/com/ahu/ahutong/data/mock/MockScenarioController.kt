package com.ahu.ahutong.data.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class MockScenarioOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String
)

data class MockEditableEndpointOption(
    val key: String,
    val title: String,
    val subtitle: String
)

object MockScenarioController {
    private val refreshRevision = MutableStateFlow(0L)

    private val disabled = MockScenarioOption(
        id = "release-disabled",
        title = "Release",
        subtitle = "Mock scenarios are only available in debug builds.",
        badge = "OFF"
    )

    fun scenarios(): List<MockScenarioOption> = listOf(disabled)

    fun activeScenarioId(): String = disabled.id

    fun selectScenario(id: String): MockScenarioOption = disabled

    fun resetScenario(): MockScenarioOption = disabled

    fun activeDiagnostics(): List<String> =
        listOf("Release 构建不包含 Mock 场景诊断。")

    fun activeValidationIssues(): List<String> = emptyList()

    fun editableEndpoints(): List<MockEditableEndpointOption> = emptyList()

    fun endpointText(key: String): String = ""

    fun endpointHasOverride(key: String): Boolean = false

    fun saveEndpointText(key: String, value: String): String? =
        "Release 构建不支持 Mock 数据编辑。"

    fun resetEndpointText(key: String): String = ""

    fun resetAllEndpointText() = Unit

    fun overriddenEndpointCount(): Int = 0

    fun refreshRevisions(): StateFlow<Long> = refreshRevision
}
