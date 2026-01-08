package com.ahu.ahutong.ui.screen.main

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.kyant.capsule.ContinuousCapsule
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneBook() {
    val context = LocalContext.current
    var dialData by rememberSaveable { mutableStateOf<Tel?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf("师生综合服务大厅") }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchQuery = ""
    }

    val allTels = remember {
        TelDirectoryViewModel.TelBook.values.flatten()
    }

    val searchResults = if (searchQuery.isBlank()) {
        emptyList()
    } else {
        allTels.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    (it.tel?.contains(searchQuery) == true) ||
                    (it.tel2?.contains(searchQuery) == true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
            .systemBarsPadding()
    ) {
        if (isSearchActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    isSearchActive = false
                    searchQuery = ""
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    placeholder = { Text("搜索电话或部门") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    } else null
                )
            }
        } else {
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
                    IconButton(onClick = { isSearchActive = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    }
                }
            }
        }

        if (isSearchActive) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        Text(
                            text = "未找到相关结果",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(searchResults) { tel ->
                        TelItem(
                            tel = tel,
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
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
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
        }
    }
    DialDialog(
        onDismiss = { dialData = null },
        tel = dialData
    )
}

@Composable
private fun TelItem(
    tel: Tel,
    onItemClick: (Tel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SmoothRoundedCornerShape(4.dp))
            .background(100.n1 withNight 20.n1)
            .clickable { onItemClick(tel) }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = tel.name,
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                tel.tel != null && tel.tel2 != null && tel.tel == tel.tel2 -> {
                    Tel(tel = tel.tel)
                }

                tel.tel != null && tel.tel2 == null -> {
                    Tel(tel = tel.tel, campus = "磬苑")
                }

                tel.tel == null && tel.tel2 != null -> {
                    Tel(tel = tel.tel2, campus = "龙河")
                }

                tel.tel != null && tel.tel2 != null && tel.tel != tel.tel2 -> {
                    Tel(tel = tel.tel, campus = "磬苑")
                    Tel(tel = tel.tel2, campus = "龙河")
                }
            }
        }
    }
}

@Composable
private fun Categories(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clip(ContinuousCapsule)
            .background(100.n1 withNight 20.n1),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TelDirectoryViewModel.TelBook.keys.toList()) {
            val isSelected = it == selectedCategory
            Text(
                text = it,
                modifier = Modifier
                    .clip(ContinuousCapsule)
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
                                context.startActivity(
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:0551-${tel.tel}"))
                                )
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
                                context.startActivity(
                                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:0551-${tel.tel2}"))
                                )
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
