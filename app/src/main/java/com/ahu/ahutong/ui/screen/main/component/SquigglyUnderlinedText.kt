package com.ahu.ahutong.ui.screen.main.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.sp
import me.saket.extendedspans.ExtendedSpans
import me.saket.extendedspans.SquigglyUnderlineSpanPainter
import me.saket.extendedspans.drawBehind
import me.saket.extendedspans.rememberSquigglyUnderlineAnimator
import kotlin.time.Duration.Companion.seconds

@Composable
fun SquigglyUnderlinedText(
    text: AnnotatedString,
    modifier: Modifier = Modifier
) {
    val underlineAnimator = rememberSquigglyUnderlineAnimator(duration = 2.seconds)
    val extendedSpans = remember {
        ExtendedSpans(
            SquigglyUnderlineSpanPainter(
                width = 2.5.sp,
                wavelength = 60.sp,
                amplitude = 1.5.sp,
                bottomOffset = 2.sp,
                animator = underlineAnimator
            )
        )
    }
    Text(
        modifier = modifier.drawBehind(extendedSpans),
        text = remember(text) {
            extendedSpans.extend(text)
        },
        onTextLayout = { result ->
            extendedSpans.onTextLayout(result)
        }
    )
}
