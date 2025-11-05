package com.ahu.ahutong.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahu.ahutong.ui.components.LocalIsLiquidGlassEnabled
import com.ahu.ahutong.ui.state.PreferencesViewModel
import com.kyant.monet.LocalTonalPalettes
import com.kyant.monet.TonalPalettes.Companion.toTonalPalettes
import com.kyant.monet.n1
import com.kyant.monet.toColor
import com.kyant.monet.toSrgb

@Composable
fun AHUTheme(content: @Composable () -> Unit) {
    val keyColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        colorResource(id = android.R.color.system_accent1_500)
    } else {
        Color(0xFF007FAC)
    }

    val preferencesViewModel: PreferencesViewModel = hiltViewModel()

    MaterialTheme {
        CompositionLocalProvider(
            LocalTonalPalettes provides keyColor.toSrgb().toColor().toTonalPalettes(),
            LocalContentColor provides if (isSystemInDarkTheme()) 100.n1 else 0.n1,
            LocalIsLiquidGlassEnabled provides preferencesViewModel.useLiquidGlass.collectAsState().value
        ) {
            content()
        }
    }
}
