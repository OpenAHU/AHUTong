package com.ahu.ahutong.ui.screen.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.data.crawler.model.jwxt.CourseGrade
import com.ahu.ahutong.data.model.Grade
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.GradeViewModel
import com.kyant.capsule.ContinuousCapsule
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Grade(gradeViewModel: GradeViewModel = viewModel()) {
    val grade = gradeViewModel.grade
    val gpaRankInfo = gradeViewModel.gpaRankInfo
    val errorMessage = gradeViewModel.errorMessage
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var termMenuExpanded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (grade == null) gradeViewModel.getGarde()
        if (gpaRankInfo == null) gradeViewModel.getGpaRank()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            gradeViewModel.errorMessage = null
        }
    }

    val gradeData = gradeViewModel.grade?.termGradeList?.find {
        it.schoolYear == gradeViewModel.schoolYear &&
                it.term == gradeViewModel.schoolTerm
    }

    val currentRank = gpaRankInfo?.gpaSemesterSubs?.find {
        it.semesterId == gradeData?.gradeList?.firstOrNull()?.semesterId
    }

    val trimmedQuery = if (searchExpanded) searchQuery.trim() else ""

    fun fuzzyContains(text: String, query: String): Boolean {
        if (query.isBlank()) return false
        val q = query.filterNot { it.isWhitespace() }
        if (q.isEmpty()) return false
        val pattern = q.map { Regex.escape(it.toString()) }.joinToString(".*")
        return Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(text)
    }

    val searchResultsByTerm = gradeViewModel.grade?.termGradeList
        ?.mapNotNull { term ->
            val matches = term.gradeList
                ?.filter { item ->
                    val q = trimmedQuery
                    q.isNotEmpty() && (
                            fuzzyContains(item.course ?: "", q) ||
                                    fuzzyContains(item.courseNum ?: "", q) ||
                                    fuzzyContains(item.courseNature ?: "", q)
                            )
                }
                .orEmpty()
            if (matches.isEmpty()) null else term to matches
        }
        .orEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.grade),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Row(
                        modifier = Modifier
                            .clip(ContinuousCapsule)
                            .background(100.n1 withNight 30.n1)
                    ) {
                        IconButton(
                            onClick = { gradeViewModel.refreshGrade() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "刷新成绩"
                            )
                        }

                        IconButton(
                            onClick = {
                                searchExpanded = !searchExpanded
                                if (!searchExpanded) searchQuery = ""
                            }
                        ) {
                            Icon(
                                imageVector = if (searchExpanded)
                                    Icons.Default.Close
                                else
                                    Icons.Default.Search,
                                contentDescription = null
                            )
                        }
                    }
                }

                if (searchExpanded) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = ContinuousCapsule,
                        placeholder = {
                            Text("搜索课程")
                        }
                    )
                }
            }

            // 改成学期下拉选择（替代原来的学年+学期双筛选）
            if (!searchExpanded) {
                val allTerms = gradeViewModel.grade?.termGradeList
                    ?.sortedWith(
                        compareByDescending<Grade.TermGradeListBean> {
                            // 提取学年起始值，例如 "2023-2024" -> 2023
                            it.schoolYear.substringBefore("-").toIntOrNull() ?: 0
                        }.thenByDescending {
                            it.term.toIntOrNull() ?: 0
                        }
                    )
                    .orEmpty()
                val selectedTermText =
                    "${gradeViewModel.schoolYear} 第${gradeViewModel.schoolTerm}学期"

                ExposedDropdownMenuBox(
                    expanded = termMenuExpanded,
                    onExpandedChange = {
                        termMenuExpanded = !termMenuExpanded
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = selectedTermText,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = ContinuousCapsule,
                        label = { Text("选择学期") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = termMenuExpanded
                            )
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = termMenuExpanded,
                        onDismissRequest = {
                            termMenuExpanded = false
                        }
                    ) {
                        allTerms.forEach { term ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${term.schoolYear} 第${term.term}学期"
                                    )
                                },
                                onClick = {
                                    gradeViewModel.schoolYear = term.schoolYear
                                    gradeViewModel.schoolTerm = term.term
                                    termMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (!searchExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val infoList = listOf(
                        "本学期平均绩点" to gradeViewModel.termGradePointAverage,
                        "本学年平均绩点" to gradeViewModel.yearGradePointAverage,
                        "全程平均绩点" to gradeViewModel.totalGradePointAverage,
                        "全程专业排名" to ((gpaRankInfo?.majorRank ?: "暂无").toString() + "/" + (gpaRankInfo?.majorHeadCount ?: "暂无")),
                        "该学期专业排名" to ((currentRank?.majorRank ?: "暂无").toString() + "/" + (gpaRankInfo?.majorHeadCount ?: "暂无")),
                        "最后更新时间" to (gpaRankInfo?.updatedDateTimeStr ?: "暂无")
                    )

                    infoList.forEach { (title, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(title, style = MaterialTheme.typography.titleMedium)
                            Text(value, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            if (searchExpanded && trimmedQuery.isNotBlank()) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    searchResultsByTerm.forEach { (term, items) ->
                        Text(
                            text = "${term.schoolYear} 第${term.term}学期",
                            style = MaterialTheme.typography.titleMedium
                        )

                        items.forEach { item ->
                            GradeCard(item)
                        }
                    }
                }
            } else if (!searchExpanded && gradeData != null && gradeData.gradeList.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    gradeData.gradeList.forEach {
                        GradeCard(it)
                    }
                }
            } else if (!searchExpanded) {
                Text(
                    text = "该学期目前没有任何成绩",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
private fun GradeCard(
    item: Grade.TermGradeListBean.GradeListBean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SmoothRoundedCornerShape(4.dp))
            .background(100.n1 withNight 20.n1)
            .padding(24.dp, 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = item.course ?: "",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "成绩: ${item.grade}    绩点: ${item.gradePoint}    学分: ${item.credit}",
            color = 30.n1 withNight 90.n1,
            style = MaterialTheme.typography.bodyLarge
        )

        Text(
            text = "${item.courseNature ?: ""} (${item.courseNum ?: ""})",
            color = 50.n1 withNight 80.n1,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
