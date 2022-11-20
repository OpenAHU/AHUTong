package com.ahu.ahutong.ui.screen.main.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun CourseDetailDialog(
    course: Course,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(SmoothRoundedCornerShape(32.dp))
                .background(96.n1 withNight 10.n1)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "第 ${course.startWeek}-${course.endWeek} 周${
                    when {
                        course.singleDouble == "0" -> ""
                        course.startWeek % 2 == 1 -> "（单周）"
                        else -> "（双周）"
                    }
                    }的周 ${course.weekday}，第 ${course.startTime}-${course.startTime + course.length - 1} 节课",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(80.n1 withNight 30.n1)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(24.dp, 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = course.location,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(80.n1 withNight 30.n1)
                )
                Row(
                    modifier = Modifier.padding(24.dp, 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = course.teacher,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
