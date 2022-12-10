package com.ahu.ahutong.ui.screen.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ahu.ahutong.R
import com.ahu.ahutong.data.model.Tel
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.TelDirectoryViewModel
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

// TODO: implement search query
@Composable
fun PhoneBook() {
    val context = LocalContext.current
    var dialData by rememberSaveable { mutableStateOf<Tel?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf("师生综合服务大厅") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .systemBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp, 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.phone_book),
                style = MaterialTheme.typography.headlineMedium
            )
            Row {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                }
            }
        }
        Categories(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )
        Telephones(
            selectedCategory = selectedCategory,
            onItemClick = {
                if (it.tel != null && it.tel2 != null && it.tel != it.tel2) {
                    dialData = it
                } else {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_DIAL,
                            Uri.parse("tel:0551-${it.tel ?: it.tel2}")
                        )
                    )
                }
            }
        )
    }
    DialDialog(
        onDismiss = { dialData = null },
        tel = dialData
    )
}

@Composable
private fun Categories(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(CircleShape)
            .background(100.n1 withNight 20.n1),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TelDirectoryViewModel.TelBook.keys.toList()) {
            val isSelected = it == selectedCategory
            Text(
                text = it,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) 90.a1 else Color.Unspecified)
                    .clickable { onCategorySelected(it) }
                    .padding(16.dp, 8.dp),
                color = if (isSelected) 0.n1 else Color.Unspecified,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun Telephones(
    selectedCategory: String,
    onItemClick: (Tel) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .clip(SmoothRoundedCornerShape(32.dp)),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        TelDirectoryViewModel.TelBook.getValue(selectedCategory).forEach {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SmoothRoundedCornerShape(4.dp))
                    .background(100.n1 withNight 20.n1)
                    .clickable { onItemClick(it) }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        it.tel != null && it.tel2 != null && it.tel == it.tel2 -> {
                            Tel(tel = it.tel)
                        }

                        it.tel != null && it.tel2 == null -> {
                            Tel(tel = it.tel, campus = "磬苑")
                        }

                        it.tel == null && it.tel2 != null -> {
                            Tel(tel = it.tel2, campus = "龙河")
                        }

                        it.tel != null && it.tel2 != null && it.tel != it.tel2 -> {
                            Tel(tel = it.tel, campus = "磬苑")
                            Tel(tel = it.tel2, campus = "龙河")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Tel(
    tel: String,
    campus: String? = null
) {
    campus?.let {
        Text(
            text = it,
            modifier = Modifier
                .padding(4.dp)
                .clip(SmoothRoundedCornerShape(8.dp))
                .background(90.a1 withNight 30.n1)
                .padding(8.dp, 2.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
    Text(
        text = tel,
        color = 50.n1 withNight 80.n1,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun DialDialog(
    onDismiss: () -> Unit,
    tel: Tel?
) {
    val context = LocalContext.current
    if (tel != null) {
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
                        text = "请选择校区",
                        style = MaterialTheme.typography.headlineMedium
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
                    Text(
                        text = "磬苑校区",
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:0551-${tel.tel}")))
                                onDismiss()
                            }
                            .padding(24.dp, 16.dp),
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(80.n1 withNight 30.n1)
                    )
                    Text(
                        text = "龙河校区",
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:0551-${tel.tel2}")))
                                onDismiss()
                            }
                            .padding(24.dp, 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
