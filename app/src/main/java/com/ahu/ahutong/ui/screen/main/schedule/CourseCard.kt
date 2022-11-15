package com.ahu.ahutong.ui.screen.main.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ahu.ahutong.data.model.Course
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.kyant.monet.LocalTonalPalettes
import com.kyant.monet.PaletteStyle
import com.kyant.monet.TonalPalettes.Companion.toTonalPalettes
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.n2
import com.kyant.monet.withNight

@Composable
fun CourseCard(
    course: Course,
    color: Color,
    cellWidth: Dp,
    cellHeight: Dp,
    onClick: (Course) -> Unit
) {
    CompositionLocalProvider(
        LocalTonalPalettes provides color.toTonalPalettes(
            style = PaletteStyle.Vibrant,
            tonalValues = doubleArrayOf() // 此行代码解决了卡顿问题
        )
    ) {
        Box(
            modifier = with(CourseCardSpec) {
                Modifier
                    .size(
                        cellWidth,
                        cellHeight * course.length + cellSpacing * (course.length - 1)
                    )
                    .offset(
                        mainColumnWidth + (cellWidth + cellSpacing) * (course.weekday - 1) + cellSpacing,
                        mainRowHeight + (cellHeight + cellSpacing) * (course.startTime - 1) + cellSpacing
                    )
                    .clip(SmoothRoundedCornerShape(8.dp))
                    .background(70.a1 withNight 60.a1)
                    .pointerInput(Unit) {
                        detectTapGestures { onClick(course) }
                    }
            }
        ) {
            Text(
                text = course.name,
                modifier = Modifier.padding(4.dp),
                color = 100.n1,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 3,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                // TODO: more shortenings
                text = course.location
                    .replace("博学", "博")
                    .replace("楼", "")
                    .replace("育场", "")
                    .replace("夫图书馆", ""),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(4.dp)
                    .clip(SmoothRoundedCornerShape(6.dp))
                    .background(95.a1 withNight 30.n2)
                    .padding(2.dp),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
