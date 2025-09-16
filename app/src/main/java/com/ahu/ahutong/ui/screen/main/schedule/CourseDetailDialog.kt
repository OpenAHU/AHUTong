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

   val courses = course.weekIndexes.last() - course.weekIndexes.first()

    val numToChinese = mapOf(
        1 to "一",
        2 to "二",
        3 to "三",
        4 to "四",
        5 to "五",
        6 to "六",
        7 to "七"
    )
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
                    text = "第${
                        when {
                            courses  == course.weekIndexes.size - 1 -> "${course.weekIndexes.first()} - ${course.weekIndexes.last()} "   //[1,2,3,4,5,6]
                            courses == (course.weekIndexes.size - 1) * 2 && course.weekIndexes.first()%2==0 -> "${course.weekIndexes.first()} - ${course.weekIndexes.last()} (双周)"    // [1,3,5,7]  [5,7,9,11,13] 
                            courses == (course.weekIndexes.size - 1) * 2 && course.weekIndexes.first()%2==0 -> "${course.weekIndexes.first()} - ${course.weekIndexes.last()} (单周)"    // [2,4,6,8]
                            else -> course.weekIndexes.toString()  //[1,2,3,5,6,7,11]
                        }
                    }周的周${numToChinese[course.weekday]}，第 ${course.startTime}-${course.startTime + course.length - 1} 节课",
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
