package com.ahu.ahutong.appwidget

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.ahu.ahutong.appwidget.MonetEngine.palettes
import com.ahu.ahutong.data.dao.PreferencesKeys
import androidx.datastore.preferences.preferencesDataStore
import com.kyant.monet.LocalTonalPalettes
import com.kyant.monet.TonalPalettes
import com.kyant.monet.toColor
import com.kyant.monet.toSrgb
import com.kyant.monet.TonalPalettes.Companion.toTonalPalettes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "user_pref")

data class WidgetColors(
    val background: Color,
    val off: Color,
    val on: Color,
    val activatedRow: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val ongoingPrimaryText: Color,
    val ongoingSecondaryText: Color
)


inline val Number.a1: Color
    get() = MonetEngine.palettes.accent1(toDouble())

inline val Number.a2: Color
    get() = MonetEngine.palettes.accent2(toDouble())

inline val Number.a3: Color
    get() = MonetEngine.palettes.accent3(toDouble())

inline val Number.n1: Color
    get() = MonetEngine.palettes.neutral1(toDouble())

inline val Number.n2: Color
    get() = MonetEngine.palettes.neutral2(toDouble())


object MonetEngine {

    var palettes: TonalPalettes =
        Color(android.R.color.holo_blue_bright).toTonalPalettes()

}

fun resolve(context: Context): WidgetColors {


    val colorRes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        android.R.color.system_accent1_500
    } else {
        android.R.color.holo_blue_bright
    }

    palettes = Color(ContextCompat.getColor(context, colorRes)).toTonalPalettes()

    val keyColor = resolveKeyColor(context)

    val palettes: TonalPalettes =
        Color(keyColor)
            .toSrgb()
            .toColor()
            .toTonalPalettes()

    val isDark =
        (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

    fun pick(light: Color, dark: Color) = if (isDark) dark else light

    return WidgetColors(

        //val backgroundColor = cp(100.n1, 20.n1) val offColor = cp(70.n1, 60.n1) val onColor = cp(50.a1, 90.a1) val activatedRowColor = cp(90.a1, 70.a1) val primaryTextColor = cp(10.n1, 95.n1) val secondaryTextColor = cp(50.n1, 80.n1)

        background = pick(
            100.n1,
            20.n1
        ),

        off = pick(
            70.n1,
            60.n1
        ),

        on = pick(
            50.a1,
            90.a1
        ),

        activatedRow = pick(
            90.a1,
            70.a1
        ),

        primaryText = pick(
            10.n1,
            95.n1
        ),

        secondaryText = pick(
            50.n1,
            80.n1
        ),

        ongoingPrimaryText = pick(
            0.n1,
            0.n1
        ),

        ongoingSecondaryText = pick(
            20.n1,
            20.n1
        )
    )
}

private fun resolveKeyColor(context: Context): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.getColor(context, android.R.color.system_accent1_500)
    } else {
        0xFF007FAC.toInt()
    }
}
