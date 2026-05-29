package com.ahu.ahutong.ui.screen.main

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundItem
import com.ahu.ahutong.ui.shape.SmoothRoundedCornerShape
import com.ahu.ahutong.ui.state.LostFoundViewModel
import com.kyant.capsule.ContinuousCapsule
import com.kyant.monet.a1
import com.kyant.monet.n1
import com.kyant.monet.withNight
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostFound(
    lostFoundViewModel: LostFoundViewModel = viewModel()
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val allCampus =
        lostFoundViewModel.allCampus?.`object`.orEmpty()

    val allLostFoundType =
        lostFoundViewModel.allLostFoundType?.`object`.orEmpty()

    val lostFoundList =
        lostFoundViewModel.lostFoundList

    var searchExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    var showPublishSheet by remember {
        mutableStateOf(false)
    }
    var showMyPostSheet by remember {
        mutableStateOf(false)
    }
    var searchQuery by rememberSaveable {
        mutableStateOf("")
    }

    var selectedCampus by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var selectedType by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    var selectedItem by remember {
        mutableStateOf<LostFoundItem?>(null)
    }

    /**
     * 高亮匹配文本
     */
    @Composable
    fun highlightText(
        text: String,
        keyword: String
    ) = buildAnnotatedString {
        if (keyword.isBlank()) {
            append(text)
            return@buildAnnotatedString
        }

        val lowerText = text.lowercase()
        val lowerKeyword = keyword.lowercase()

        var startIndex = 0

        while (true) {
            val matchIndex =
                lowerText.indexOf(
                    lowerKeyword,
                    startIndex
                )

            if (matchIndex == -1) {
                append(
                    text.substring(startIndex)
                )
                break
            }

            append(
                text.substring(
                    startIndex,
                    matchIndex
                )
            )

            pushStyle(
                SpanStyle(
                    background = 90.a1,
                    fontWeight = FontWeight.Bold
                )
            )

            append(
                text.substring(
                    matchIndex,
                    matchIndex + keyword.length
                )
            )

            pop()

            startIndex =
                matchIndex + keyword.length
        }
    }

    /**
     * 搜索 + 筛选
     */
    val filteredList = lostFoundList.filter { item ->
        val campusMatch =
            selectedCampus == null ||
                    item.campusid == selectedCampus

        val typeMatch =
            selectedType == null ||
                    item.typeid == selectedType

        val searchMatch =
            searchQuery.isBlank() ||

                    (item.title?.contains(
                        searchQuery,
                        true
                    ) == true) ||

                    (item.linkman?.contains(
                        searchQuery,
                        true
                    ) == true) ||

                    (item.phone?.contains(
                        searchQuery,
                        true
                    ) == true) ||

                    (item.campusName?.contains(
                        searchQuery,
                        true
                    ) == true) ||

                    (item.lostType?.typeName?.contains(
                        searchQuery,
                        true
                    ) == true) ||

                    (item.pubuser?.userName?.contains(
                        searchQuery,
                        true
                    ) == true) ||

                    (item.num1?.contains(
                        searchQuery,
                        true
                    ) == true) ||

                    (item.createtime?.contains(
                        searchQuery,
                        true
                    ) == true)

        campusMatch && typeMatch && searchMatch
    }

    /**
     * 自动加载更多
     */
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo
                .visibleItemsInfo
                .lastOrNull()
                ?.index
        }
            .distinctUntilChanged()
            .collect { lastVisibleItem ->
                val totalItems =
                    listState.layoutInfo.totalItemsCount

                if (
                    lastVisibleItem != null &&
                    lastVisibleItem == totalItems - 1 &&
                    !lostFoundViewModel.listLoading &&
                    !lostFoundViewModel.isLoadingMore &&
                    lostFoundViewModel.hasMore
                ) {
                    lostFoundViewModel.loadMore()
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement =
                Arrangement.spacedBy(24.dp),
            contentPadding =
                PaddingValues(bottom = 96.dp)
        ) {

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        /**
                         * 左边 1/3
                         */
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            FilterChip(
                                selected =
                                    lostFoundViewModel.currentState == 1,
                                onClick = {
                                    lostFoundViewModel.switchState(1)
                                },
                                label = {
                                    Text(
                                        text = "失物招领",
                                        fontSize = 18.sp,
                                        maxLines = 1
                                    )
                                }
                            )
                        }

                        /**
                         * 中间 1/3
                         */
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            FilterChip(
                                selected =
                                    lostFoundViewModel.currentState == 2,
                                onClick = {
                                    lostFoundViewModel.switchState(2)
                                },
                                label = {
                                    Text(
                                        text = "寻物启事",
                                        fontSize = 18.sp,
                                        maxLines = 1
                                    )
                                }
                            )
                        }

                        /**
                         * 右边 1/3（容器三等分，按钮不拉伸）
                         */
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(ContinuousCapsule)
                                    .background(
                                        100.n1 withNight 30.n1
                                    ),
                                horizontalArrangement =
                                    Arrangement.spacedBy(4.dp),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        lostFoundViewModel.refreshList()

                                        Toast.makeText(
                                            context,
                                            "刷新成功",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector =
                                            Icons.Default.Refresh,
                                        contentDescription = null
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        searchExpanded =
                                            !searchExpanded

                                        if (!searchExpanded) {
                                            searchQuery = ""
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector =
                                            if (searchExpanded)
                                                Icons.Default.Close
                                            else
                                                Icons.Default.Search,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                    if (searchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = ContinuousCapsule,
                            placeholder = {
                                Text("搜索全部信息")
                            }
                        )
                    }
                }
            }

            if (!searchExpanded) {
                item {
                    Row(
                        modifier = Modifier
                            .padding(
                                horizontal = 16.dp
                            )
                            .clip(
                                ContinuousCapsule
                            )
                            .background(
                                100.n1 withNight 20.n1
                            )
                            .padding(8.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected =
                                selectedCampus == null,
                            onClick = {
                                selectedCampus = null
                            },
                            label = {
                                Text("全部校区")
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        LazyRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    8.dp
                                )
                        ) {
                            items(allCampus) { campus ->
                                val selected =
                                    selectedCampus ==
                                            campus.id

                                Text(
                                    text =
                                        campus.campusName,
                                    modifier =
                                        Modifier
                                            .clip(
                                                ContinuousCapsule
                                            )
                                            .background(
                                                if (selected)
                                                    90.a1
                                                else
                                                    Color.Unspecified
                                            )
                                            .clickable {
                                                selectedCampus =
                                                    campus.id
                                            }
                                            .padding(
                                                16.dp,
                                                8.dp
                                            ),
                                    color =
                                        if (selected)
                                            0.n1
                                        else
                                            Color.Unspecified
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .padding(
                                horizontal = 16.dp
                            )
                            .clip(
                                ContinuousCapsule
                            )
                            .background(
                                100.n1 withNight 20.n1
                            )
                            .padding(8.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected =
                                selectedType == null,
                            onClick = {
                                selectedType = null
                            },
                            label = {
                                Text("全部类型")
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        LazyRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    8.dp
                                )
                        ) {
                            items(allLostFoundType) { type ->
                                val selected =
                                    selectedType ==
                                            type.typeId

                                Text(
                                    text =
                                        type.typeName,
                                    modifier =
                                        Modifier
                                            .clip(
                                                ContinuousCapsule
                                            )
                                            .background(
                                                if (selected)
                                                    90.a1
                                                else
                                                    Color.Unspecified
                                            )
                                            .clickable {
                                                selectedType =
                                                    type.typeId
                                            }
                                            .padding(
                                                16.dp,
                                                8.dp
                                            ),
                                    color =
                                        if (selected)
                                            0.n1
                                        else
                                            Color.Unspecified
                                )
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            if (
                                searchExpanded &&
                                searchQuery.isNotBlank()
                            ) {
                                "搜索「$searchQuery」到 ${filteredList.size} 条记录"
                            } else {
                                "共 ${filteredList.size} 条记录"
                            },
                        style =
                            MaterialTheme.typography.titleMedium
                    )

                    TextButton(
                        onClick = {
                            showMyPostSheet = true
                        }
                    ) {
                        Text("管理我的帖子")
                    }
                }
            }

            items(filteredList) { item ->
                Column(
                    modifier = Modifier
                        .padding(
                            horizontal = 16.dp
                        )
                        .fillMaxWidth()
                        .clip(
                            SmoothRoundedCornerShape(
                                4.dp
                            )
                        )
                        .background(
                            100.n1 withNight 20.n1
                        )
                        .clickable {
                            selectedItem = item
                        }
                        .padding(24.dp, 16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text =
                            highlightText(
                                item.title ?: "无标题",
                                searchQuery
                            ),
                        fontWeight =
                            FontWeight.Bold,
                        style =
                            MaterialTheme.typography
                                .titleMedium
                    )

                    Text(
                        buildAnnotatedString {
                            append("联系人：")
                            append(
                                highlightText(
                                    item.linkman ?: "未知",
                                    searchQuery
                                )
                            )
                        }
                    )

                    Text(
                        buildAnnotatedString {
                            append("联系电话：")
                            append(
                                highlightText(
                                    item.phone ?: "未知",
                                    searchQuery
                                )
                            )
                        }
                    )

                    Text(
                        buildAnnotatedString {
                            append("校区：")
                            append(
                                highlightText(
                                    item.campusName ?: "未知",
                                    searchQuery
                                )
                            )
                        }
                    )

                    Text(
                        buildAnnotatedString {
                            append("类型：")
                            append(
                                highlightText(
                                    item.lostType?.typeName
                                        ?: "未知",
                                    searchQuery
                                )
                            )
                        }
                    )

                    Text(
                        buildAnnotatedString {
                            append("证件号：")
                            append(
                                highlightText(
                                    item.num1?:"未知",
                                    searchQuery
                                )
                            )
                        }
                    )

                    Text(
                        text =
                            item.createtime
                                ?: "未知时间",
                        color =
                            50.n1 withNight 80.n1
                    )
                }
            }
            if (lostFoundViewModel.isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = {
                showPublishSheet = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(64.dp)
        ) {
            Text(
                text = "+",
                fontSize = 28.sp
            )
        }

        selectedItem?.let { item ->
            ModalBottomSheet(
                onDismissRequest = {
                    selectedItem = null
                }
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = item.title ?: "无标题",
                        style =
                            MaterialTheme.typography
                                .headlineSmall,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        "联系人：${item.linkman ?: "未知"}"
                    )

                    Text(
                        "联系电话：${item.phone ?: "未知"}"
                    )

                    Text(
                        "校区：${item.campusName ?: "未知"}"
                    )

                    Text(
                        "类型：${item.lostType?.typeName ?: "未知"}"
                    )

                    Text(
                        "发布时间：${item.createtime ?: "未知"}"
                    )
                    Text(
                        "证件号：${item.num1 ?: "未知"}"
                    )

                    if (item.imgs.isNotEmpty()) {
                        Text(
                            text = "相关图片",
                            style =
                                MaterialTheme.typography
                                    .titleMedium
                        )

                        LazyRow(
                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    12.dp
                                )
                        ) {
                            items(item.imgs) { img ->
                                Card(
                                    modifier =
                                        Modifier.size(
                                            180.dp
                                        )
                                ) {
                                    AsyncImage(
                                        model =
                                            img.imgPath,
                                        contentDescription =
                                            null,
                                        modifier =
                                            Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(24.dp)
                    )
                }
            }
        }
    }
    if (showMyPostSheet) {
        val myPosts = lostFoundList.filter {
            it.pubuser?.idNumber ==
                    lostFoundViewModel.currentUserName
        }

        ModalBottomSheet(
            onDismissRequest = {
                showMyPostSheet = false
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "管理我的帖子",
                    style =
                        MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                if (myPosts.isEmpty()) {
                    Text("暂无帖子")
                } else {
                    LazyColumn(
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        items(myPosts) { item ->
                            Card(
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier =
                                            Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text =
                                                item.title
                                                    ?: "无标题",
                                            fontWeight =
                                                FontWeight.Bold
                                        )

                                        Text(
                                            text =
                                                item.createtime
                                                    ?: ""
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            item.id?.let { id ->
                                                lostFoundViewModel
                                                    .deleteLostFound(
                                                        id
                                                    )

                                                Toast.makeText(
                                                    context,
                                                    "删除成功",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    ) {
                                        Text("删除")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * 发帖 BottomSheet
     */
    if (showPublishSheet) {

        var linkman by rememberSaveable {
            mutableStateOf("")
        }

        var phone by rememberSaveable {
            mutableStateOf("")
        }

        var title by rememberSaveable {
            mutableStateOf("")
        }

        var num1 by rememberSaveable {
            mutableStateOf("")
        }

        var publishCampusId by rememberSaveable {
            mutableStateOf<String?>(null)
        }

        var publishTypeId by rememberSaveable {
            mutableStateOf<String?>(null)
        }

        var publishState by rememberSaveable {
            mutableStateOf("1")
        }

        ModalBottomSheet(
            onDismissRequest = {
                showPublishSheet = false
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            ){
                Text(
                    text = "*目前智慧安大图片功能有时无法使用，请大家文字描述尽量详尽",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray.copy(alpha = 0.6f)
                )
                Text(
                    text = "发布帖子",
                    style =
                        MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = linkman,
                    onValueChange = {
                        linkman = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("联系人 *")
                    }
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("联系电话 *")
                    }
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("描述内容 *")
                    }
                )

                OutlinedTextField(
                    value = num1,
                    onValueChange = {
                        num1 = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("证件号（可选）")
                    }
                )

                Text("选择校区")

                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    items(allCampus) { campus ->
                        FilterChip(
                            selected =
                                publishCampusId == campus.id,
                            onClick = {
                                publishCampusId = campus.id
                            },
                            label = {
                                Text(campus.campusName)
                            }
                        )
                    }
                }

                Text("选择类型")

                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    items(allLostFoundType) { type ->
                        FilterChip(
                            selected =
                                publishTypeId == type.typeId,
                            onClick = {
                                publishTypeId = type.typeId
                            },
                            label = {
                                Text(type.typeName)
                            }
                        )
                    }
                }

                Text("选择事件类型")

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = publishState == "1",
                        onClick = {
                            publishState = "1"
                        },
                        label = {
                            Text("失物招领")
                        }
                    )

                    FilterChip(
                        selected = publishState == "2",
                        onClick = {
                            publishState = "2"
                        },
                        label = {
                            Text("寻物启事")
                        }
                    )
                }

                Button(
                    onClick = {

                        if (
                            linkman.isBlank() ||
                            phone.isBlank() ||
                            title.isBlank() ||
                            publishCampusId == null ||
                            publishTypeId == null
                        ) {
                            Toast.makeText(
                                context,
                                "请填写完整信息",
                                Toast.LENGTH_SHORT
                            ).show()

                            return@Button
                        }

                        lostFoundViewModel.publishLostFound(
                            linkman = linkman,
                            phone = phone,
                            title = title,
                            num1 = num1,
                            campusId = publishCampusId!!,
                            typeId = publishTypeId!!,
                            state = publishState
                        )

                        showPublishSheet = false

                        Toast.makeText(
                            context,
                            "发布成功",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("发布")
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}