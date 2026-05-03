package com.ahu.ahutong.ui.screen.main

import android.util.Log
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.GradeViewModel
import com.kyant.capsule.ContinuousCapsule
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import androidx.compose.material.icons.filled.Refresh
import kotlinx.coroutines.flow.collect

@Composable
fun Grade(gradeViewModel: GradeViewModel = viewModel()) {
    val grade = gradeViewModel.grade
    val gpaRankInfo = gradeViewModel.gpaRankInfo
    val errorMessage = gradeViewModel.errorMessage
    val context = LocalContext.current
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val scrollState = rememberScrollState()

    PredictiveBackHandler(enabled = searchExpanded) { progress ->
        progress.collect { }
        searchExpanded = false
        searchQuery = ""
    }

    LaunchedEffect(Unit) {
        if (grade == null) {
            gradeViewModel.getGarde()
        }
        if(gpaRankInfo == null) {
            gradeViewModel.getGpaRank()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            gradeViewModel.errorMessage = null
        }
    }

    val schoolYears = GradeViewModel.schoolYears
    val schoolTerms = GradeViewModel.terms.keys.toTypedArray()
    val gradeData = gradeViewModel.grade?.let { grade1 ->
        grade1.termGradeList?.find { term ->
            term.schoolYear == gradeViewModel.schoolYear && term.term == gradeViewModel.schoolTerm
        }
    }
    val currentRank = gpaRankInfo?.gpaSemesterSubs?.find { gpaSemesterSub ->
        gpaSemesterSub.semesterId == gradeData?.gradeList?.first()?.semesterId
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
                            onClick = {
                                gradeViewModel.refreshGrade()
                            }
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
                                imageVector = if (searchExpanded) Icons.Default.Close else Icons.Default.Search,
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = 10.n1 withNight 90.n1,
                            unfocusedTextColor = 10.n1 withNight 90.n1,
                            focusedBorderColor = 20.n1 withNight 80.n1,
                            unfocusedBorderColor = 30.n1 withNight 70.n1,
                            cursorColor = 10.n1 withNight 90.n1,
                            focusedLeadingIconColor = 20.n1 withNight 80.n1,
                            unfocusedLeadingIconColor = 20.n1 withNight 80.n1,
                            focusedTrailingIconColor = 20.n1 withNight 80.n1,
                            unfocusedTrailingIconColor = 20.n1 withNight 80.n1
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        placeholder = { Text(text = "搜索课程", color = 40.n1 withNight 60.n1) }
                    )
                }
            }

            if (!searchExpanded) {
                LazyRow(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(ContinuousCapsule)
                        .background(100.n1 withNight 20.n1),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(schoolYears.toList()) {
                        val isSelected = it == gradeViewModel.schoolYear
                        Text(
                            text = it,
                            modifier = Modifier
                                .clip(ContinuousCapsule)
                                .background(if (isSelected) 90.a1 else Color.Unspecified)
                                .clickable {
                                    gradeViewModel.schoolYear = it
                                    gradeViewModel.schoolTerm = schoolTerms.firstOrNull()
                                }
                                .padding(16.dp, 8.dp),
                            color = if (isSelected) 0.n1 else Color.Unspecified,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                LazyRow(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(ContinuousCapsule)
                        .background(100.n1 withNight 20.n1),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(arrayOf("1", "2")) {
                        val isSelected = it == gradeViewModel.schoolTerm
                        Text(
                            text = it,
                            modifier = Modifier
                                .clip(ContinuousCapsule)
                                .background(if (isSelected) 90.a1 else Color.Unspecified)
                                .clickable { gradeViewModel.schoolTerm = it }
                                .padding(16.dp, 8.dp),
                            color = if (isSelected) 0.n1 else Color.Unspecified,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else {
                if (trimmedQuery.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "搜索结果：${searchResultsByTerm.sumOf { it.second.size }}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (searchResultsByTerm.isEmpty()) {
                            Text(
                                text = "未找到匹配课程",
                                style = MaterialTheme.typography.bodyMedium,
                                color = 50.n1 withNight 80.n1
                            )
                        }
                    }
                } else {
                    Text(
                        text = "输入课程名或课程号开始搜索",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = 50.n1 withNight 80.n1
                    )
                }
            }
            if (!searchExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "本学期平均绩点",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = gradeViewModel.termGradePointAverage,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "本学年平均绩点",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = gradeViewModel.yearGradePointAverage,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "全程平均绩点",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = gradeViewModel.totalGradePointAverage,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "全程专业排名",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = (gpaRankInfo?.majorRank?.toString() ?: "暂无") + "/" + (gpaRankInfo?.majorHeadCount?.toString() ?: "暂无") ,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "该学期专业排名",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = (currentRank?.majorRank?.toString() ?: "暂无") + "/" + (gpaRankInfo?.majorHeadCount?.toString() ?: "暂无"),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "最后更新时间",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = gpaRankInfo?.updatedDateTimeStr ?: "暂无",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            if (searchExpanded && trimmedQuery.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(SmoothRoundedCornerShape(32.dp)),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    searchResultsByTerm.forEach { (term, items) ->
                        Text(
                            text = "${term.schoolYear} 第${term.term}学期",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = 30.n1 withNight 90.n1
                        )
                        Column(
                            modifier = Modifier
                                .clip(SmoothRoundedCornerShape(32.dp)),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items.forEach { item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(SmoothRoundedCornerShape(4.dp))
                                        .background(100.n1 withNight 20.n1)
                                        .clickable {}
                                        .padding(24.dp, 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = item.course,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "成绩: ${item.grade}    绩点: ${item.gradePoint}    学分: ${item.credit}",
                                        color = 30.n1 withNight 90.n1,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "${item.courseNature ?: ""} (${item.courseNum})",
                                        color = 50.n1 withNight 80.n1,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (!searchExpanded && gradeData != null && gradeData.gradeList.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(SmoothRoundedCornerShape(32.dp)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    gradeData.gradeList.forEach {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SmoothRoundedCornerShape(4.dp))
                                .background(100.n1 withNight 20.n1)
                                .clickable {}
                                .padding(24.dp, 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = it.course,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "成绩: ${it.grade}    绩点: ${it.gradePoint}    学分: ${it.credit}",
                                color = 30.n1 withNight 90.n1,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${it.courseNature ?: ""} (${it.courseNum})",
                                color = 50.n1 withNight 80.n1,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
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
