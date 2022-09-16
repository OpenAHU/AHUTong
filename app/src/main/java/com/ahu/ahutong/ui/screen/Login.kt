package com.ahu.ahutong.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.ahu.ahutong.R
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@Composable
fun Login(
    userId: TextFieldValue,
    onUserIdChanged: (TextFieldValue) -> Unit,
    password: TextFieldValue,
    onPasswordChanged: (TextFieldValue) -> Unit,
    onLoginButtonClicked: () -> Unit
) {
    var focusIndex by remember { mutableStateOf(0) }
    var passwordVisible by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(96.n1 withNight 10.n1)
            .systemBarsPadding()
            .imePadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.log_in),
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier)
            Box(modifier = Modifier.height(128.dp)) {
                Image(
                    painter = if (focusIndex == 0) painterResource(id = R.mipmap.emoji_username)
                    else painterResource(id = R.mipmap.emoji_password),
                    contentDescription = null
                )
                androidx.compose.animation.AnimatedVisibility(
                    visible = focusIndex == 1,
                    modifier = Modifier.offset(y = 64.dp),
                    enter = fadeIn() + expandIn(),
                    exit = shrinkOut() + fadeOut()
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.emoji_left_hand),
                        contentDescription = null
                    )
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = focusIndex == 1,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset((-16).dp, 64.dp),
                    enter = fadeIn() + expandIn(),
                    exit = shrinkOut() + fadeOut()
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.emoji_right_hand),
                        contentDescription = null
                    )
                }
            }
            Spacer(modifier = Modifier)
            BasicTextField(
                value = userId,
                onValueChange = onUserIdChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(CircleShape)
                    .background(100.n1 withNight 20.n1)
                    .onFocusChanged {
                        if (it.isFocused) {
                            focusIndex = 0
                        }
                    },
                textStyle = LocalTextStyle.current.copy(
                    color = LocalContentColor.current,
                    fontFamily = FontFamily.Monospace
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(64.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    it()
                    if (userId.text.isBlank()) {
                        Text(
                            text = stringResource(id = R.string.hint_userid),
                            color = 30.n1 withNight 80.n1,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            BasicTextField(
                value = password,
                onValueChange = onPasswordChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(CircleShape)
                    .background(100.n1 withNight 20.n1)
                    .onFocusChanged {
                        if (it.isFocused) {
                            focusIndex = 1
                        }
                    },
                textStyle = LocalTextStyle.current.copy(
                    color = LocalContentColor.current,
                    fontFamily = FontFamily.Monospace
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onLoginButtonClicked() }),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(64.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    it()
                    if (password.text.isBlank()) {
                        Text(
                            text = stringResource(id = R.string.hint_wisdom_password),
                            color = 30.n1 withNight 80.n1,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility
                            else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                }
            }
        }
        CompositionLocalProvider(LocalIndication provides rememberRipple(color = 0.n1)) {
            Text(
                text = stringResource(id = R.string.login),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(90.a1 withNight 85.a1)
                    .clickable(
                        role = Role.Button,
                        onClick = onLoginButtonClicked
                    )
                    .padding(24.dp, 16.dp),
                color = 0.n1,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
