package com.ahu.ahutong.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.ui.page.state.GradeViewModel
import com.ahu.ahutong.ui.screen.component.AnimatedListContent
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun Grade(gradeViewModel: GradeViewModel = viewModel()) {
    val grade = gradeViewModel.result.value?.getOrNull() ?: com.ahu.ahutong.data.model.Grade()
    val schoolYearData = grade.termGradeList.map { it.schoolYear to it.term }
    val schoolYears = schoolYearData.map { it.first }.distinct()
    var schoolYear by remember { mutableStateOf(schoolYears.first()) }
    val schoolTerms = schoolYearData.filter { it.first == schoolYear }.map { it.second }
    var schoolTerm by remember { mutableStateOf(schoolTerms.first()) }
    val gradeData = grade.termGradeList.find {
        it.schoolYear == schoolYear && it.term == schoolTerm
    } ?: return
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(96.n1 withNight 10.n1)
            .systemBarsPadding()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 8.dp, 16.dp, 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.grade),
                style = MaterialTheme.typography.headlineMedium
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(schoolYears) {
                val isSelected = it == schoolYear
                CompositionLocalProvider(
                    LocalIndication provides rememberRipple(
                        color = if (isSelected) 100.n1 withNight 0.n1
                        else 0.n1 withNight 100.n1
                    )
                ) {
                    Text(
                        text = it,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                animateColorAsState(
                                    targetValue = if (isSelected) 40.a1 withNight 90.a1
                                    else 100.n1 withNight 20.n1
                                ).value
                            )
                            .clickable { schoolYear = it }
                            .padding(16.dp, 8.dp),
                        color = animateColorAsState(
                            targetValue = if (isSelected) 100.n1 withNight 0.n1
                            else 0.n1 withNight 100.n1
                        ).value,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(schoolTerms) {
                val isSelected = it == schoolTerm
                CompositionLocalProvider(
                    LocalIndication provides rememberRipple(
                        color = if (isSelected) 100.n1 withNight 0.n1
                        else 0.n1 withNight 100.n1
                    )
                ) {
                    Text(
                        text = it,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                animateColorAsState(
                                    targetValue = if (isSelected) 40.a1 withNight 90.a1
                                    else 100.n1 withNight 20.n1
                                ).value
                            )
                            .clickable { schoolTerm = it }
                            .padding(16.dp, 8.dp),
                        color = animateColorAsState(
                            targetValue = if (isSelected) 100.n1 withNight 0.n1
                            else 0.n1 withNight 100.n1
                        ).value,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        AnimatedListContent(targetState = gradeData.gradeList) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(32.dp)),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                it.forEach {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
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
                            text = "${it.courseNature} (${it.courseNum})",
                            color = 50.n1 withNight 80.n1,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
