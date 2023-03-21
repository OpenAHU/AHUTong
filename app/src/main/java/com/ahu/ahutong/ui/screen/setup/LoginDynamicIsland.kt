package com.ahu.ahutong.ui.screen.setup

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahu.ahutong.R
import com.ahu.ahutong.ui.component.LoadingIndicator
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.LoginState
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun BoxScope.LoginDynamicIsland(
    state: LoginState,
    failureMessage: String,
    succeedMessage: String,
    onLogIn: () -> Unit
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .navigationBarsPadding()
            .padding(16.dp)
            .clip(SmoothRoundedCornerShape(32.dp)) // TODO: clip bug
            .background(
                animateColorAsState(
                    targetValue = when (state) {
                        LoginState.Idle -> 90.a1 withNight 85.a1
                        LoginState.InProgress -> 70.a1 withNight 60.a1
                        LoginState.Failed -> Color.Red
                        LoginState.Succeeded -> 70.a1 withNight 60.a1
                    }
                ).value
            )
            .animateContentSize(spring(stiffness = Spring.StiffnessLow))
    ) {
        when (state) {
            LoginState.Idle -> {
                CompositionLocalProvider(LocalIndication provides rememberRipple(color = 0.n1)) {
                    Text(
                        text = stringResource(id = R.string.login),
                        modifier = Modifier
                            .clickable(
                                role = Role.Button,
                                onClick = onLogIn
                            )
                            .padding(24.dp, 16.dp),
                        color = 0.n1,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            LoginState.InProgress -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoadingIndicator(
                        modifier = Modifier.size(56.dp),
                        color = 100.n1,
                        strokeWidth = 6.dp
                    )
                    Text(
                        text = "登录中",
                        modifier = Modifier.padding(4.dp),
                        color = 100.n1,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            LoginState.Failed -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = 100.n1
                    )
                    Text(
                        text = failureMessage,
                        modifier = Modifier.padding(4.dp),
                        color = 100.n1,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

            LoginState.Succeeded -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = 100.n1
                    )
                    Text(
                        text = succeedMessage,
                        modifier = Modifier.padding(4.dp),
                        color = 100.n1,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }
}
