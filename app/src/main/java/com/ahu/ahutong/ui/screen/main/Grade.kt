package com.ahu.ahutong.ui.screen.main

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.R
import com.ahu.ahutong.ui.screen.main.component.AnimatedListContent
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.GradeViewModel
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun Grade(gradeViewModel: GradeViewModel = viewModel()) {
    val grade = gradeViewModel.grade
    val schoolYears = GradeViewModel.schoolYears
    val schoolTerms = GradeViewModel.terms.keys.toTypedArray()
    val gradeData = grade?.termGradeList?.find {
        it.schoolYear == gradeViewModel.schoolYear && it.term == gradeViewModel.schoolTerm
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                val isSelected = it == gradeViewModel.schoolYear
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
                            .clickable {
                                gradeViewModel.schoolYear = it
                                gradeViewModel.schoolTerm = schoolTerms.firstOrNull()
                            }
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
                val isSelected = it == gradeViewModel.schoolTerm
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
                            .clickable { gradeViewModel.schoolTerm = it }
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
        }
        if (gradeData != null) {
            AnimatedListContent(targetState = gradeData.gradeList) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clip(SmoothRoundedCornerShape(32.dp)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    it.forEach {
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
                                text = "${it.courseNature} (${it.courseNum})",
                                color = 50.n1 withNight 80.n1,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        } else {
            Text(
                text = "该学期目前没有任何成绩",
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
