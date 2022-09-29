package com.ahu.ahutong.ui.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ahu.ahutong.data.model.User

@Composable
fun AtAGlance(
    user: User,
    navController: NavHostController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp, 24.dp, 16.dp, 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SquigglyUnderlinedText(
            text = buildAnnotatedString {
                withStyle(style = MaterialTheme.typography.headlineLarge.toSpanStyle()) {
                    append("Hi, ")
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(user.name)
                    }
                }
            }
        )
        IconButton(onClick = { navController.navigate("settings") }) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null
            )
        }
    }
}
