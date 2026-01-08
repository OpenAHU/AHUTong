package com.ahu.ahutong.ui.screen.setup

import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahu.ahutong.AHUApplication
import com.ahu.ahutong.R
import com.ahu.ahutong.data.crawler.manager.CookieManager
import com.ahu.ahutong.data.crawler.manager.TokenManager
import com.ahu.ahutong.data.dao.AHUCache
import com.ahu.ahutong.sdk.RustSDK
import com.ahu.ahutong.ui.state.LoginState
import com.ahu.ahutong.ui.state.LoginViewModel
import com.kyant.capsule.ContinuousCapsule
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.delay


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Login(
    loginViewModel: LoginViewModel = viewModel(),
    onLoggedIn: () -> Unit
) {
    var userID by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    var password by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }
    var focusIndex by rememberSaveable { mutableStateOf(0) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(loginViewModel.state) {
        if (loginViewModel.state == LoginState.Failed) {
            delay(1000)
            loginViewModel.state = LoginState.Idle
        } else if (loginViewModel.state == LoginState.Succeeded) {
            delay(500)
            loginViewModel.state = LoginState.Idle
            onLoggedIn()
        }
    }
    val activity = LocalActivity.current
    DisposableEffect(Unit) {
        val window = activity?.window
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    BackHandler {
        activity?.finish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.log_in),
                modifier = Modifier.padding(24.dp, 32.dp),
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier)
            Box(
                modifier = Modifier
                    .height(128.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Image(
                    painter = if (focusIndex == 0) {
                        painterResource(id = R.mipmap.emoji_username)
                    } else {
                        painterResource(id = R.mipmap.emoji_password)
                    },
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
            // TODO: create a common text field
            BasicTextField(
                value = userID,
                onValueChange = { userID = it },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(ContinuousCapsule)
                    .background(100.n1 withNight 20.n1)
                    .onFocusChanged {
                        if (it.isFocused) {
                            focusIndex = 0
                        }
                    }
                    .autofill(
                        autofillTypes = listOf(AutofillType.Username),
                        onFill = { userID = userID.copy(text = it) }
                    ),
                textStyle = LocalTextStyle.current.copy(
                    color = LocalContentColor.current,
                    fontFamily = FontFamily.Monospace
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                cursorBrush = SolidColor(LocalContentColor.current)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(64.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    it()
                    if (userID.text.isBlank()) {
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
                onValueChange = { password = it },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(ContinuousCapsule)
                    .background(100.n1 withNight 20.n1)
                    .onFocusChanged {
                        if (it.isFocused) {
                            focusIndex = 1
                        }
                    }
                    .autofill(
                        autofillTypes = listOf(AutofillType.Password),
                        onFill = { password = password.copy(text = it) }
                    ),
                textStyle = LocalTextStyle.current.copy(
                    color = LocalContentColor.current,
                    fontFamily = FontFamily.Monospace
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    logIn(
                        loginViewModel = loginViewModel,
                        userID = userID.text,
                        password = password.text
                    )
                }),
                singleLine = true,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                cursorBrush = SolidColor(LocalContentColor.current)
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
                            imageVector = if (passwordVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = null
                        )
                    }
                }
            }
        }
        LoginDynamicIsland(
            state = loginViewModel.state,
            failureMessage = loginViewModel.failureMessage,
            succeedMessage = loginViewModel.succeedMessage
        ) {
            logIn(
                loginViewModel = loginViewModel,
                userID = userID.text,
                password = password.text
            )
        }
    }
}

private fun logIn(
    loginViewModel: LoginViewModel,
    userID: String,
    password: String
) {
    if (userID.isBlank() || password.isBlank()) {
        loginViewModel.state = LoginState.Failed
        loginViewModel.failureMessage = "请将信息填写完整"
    } else {
//        loginViewModel.loginWithServer(
//            userID = userID,
//            wisdomPassword = password
//        )
        AHUApplication.sessionExpired = true
        AHUCache.clearAll()
        RustSDK.initSafe("")
        CookieManager.cookieJar.clear()
        TokenManager.clear()
        AHUCache.setAgreementAccepted()

        loginViewModel.loginWithCrawler(userID = userID, password = password)

    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.autofill(
    autofillTypes: List<AutofillType>,
    onFill: ((String) -> Unit)
) = composed {
    val autofill = LocalAutofill.current
    val autofillNode = AutofillNode(
        onFill = onFill,
        autofillTypes = autofillTypes
    )
    LocalAutofillTree.current += autofillNode

    this
        .onGloballyPositioned {
            autofillNode.boundingBox = it.boundsInWindow()
        }
        .onFocusChanged { focusState ->
            autofill?.run {
                if (focusState.isFocused) {
                    requestAutofillForNode(autofillNode)
                } else {
                    cancelAutofillForNode(autofillNode)
                }
            }
        }
}
