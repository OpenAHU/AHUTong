package com.ahu.ahutong.ui.state

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahu.ahutong.data.AHURepository
import com.ahu.ahutong.data.crawler.model.adwnh.AllCampus
import com.ahu.ahutong.data.crawler.model.adwnh.AllLostFoundType
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundItem
import com.ahu.ahutong.data.crawler.model.adwnh.LostFoundPublishRequest
import com.ahu.ahutong.data.dao.AHUCache
import kotlinx.coroutines.launch

class LostFoundViewModel : ViewModel() {

    // 校区
    var allCampus by mutableStateOf<AllCampus?>(null)
    var campusLoading by mutableStateOf(false)

    // 类型
    var allLostFoundType by mutableStateOf<AllLostFoundType?>(null)
    var typeLoading by mutableStateOf(false)

    // 当前显示的帖子列表
    var lostFoundList by mutableStateOf<List<LostFoundItem>>(emptyList())

    // 当前状态
    // 1=失物招领
    // 2=寻物启事
    var currentState by mutableStateOf(1)
        private set

    // 分页信息
    private var currentPage by mutableStateOf(1)
    private val pageSize = 20
    private var totalPages by mutableStateOf(1)

    // 加载状态
    var listLoading by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var isLoadingMore by mutableStateOf(false)
        private set
    val currentUserName: String
        get() = AHUCache.getCurrentUser()?.xh?:"null"

    var errorMessage by mutableStateOf<String?>(null)

    /**
     * 是否还有更多数据
     */
    val hasMore: Boolean
        get() = currentPage < totalPages

    /**
     * 获取校区列表
     */
    fun getAllCampus(
        forceRefresh: Boolean = false
    ) = viewModelScope.launch {
        campusLoading = true

        try {
            // 非强制刷新先读缓存
            if (!forceRefresh) {
                val cache =
                    AHUCache.getLostFoundCampus()

                if (cache.isNotEmpty()) {
                    allCampus = AllCampus(
                        code = 0,
                        msg = "cache",
                        `object` = cache
                    )
                }
            }

            // 联网更新
            val result =
                AHURepository.getAllCampus()

            if (result.code == 0) {
                allCampus = result.data

                AHUCache.saveLostFoundCampus(
                    result.data.`object`
                )

                Log.d(
                    "lostfound",
                    "allcampus = ${result.data}"
                )

                errorMessage = null
            } else {
                errorMessage = result.msg
            }
        } catch (t: Throwable) {
            // 没缓存时才报错
            if (allCampus == null) {
                errorMessage =
                    t.message ?: "获取校区失败"
            }
        } finally {
            campusLoading = false
        }
    }

    /**
     * 获取分类列表
     */
    fun getAllLostFoundType(
        forceRefresh: Boolean = false
    ) = viewModelScope.launch {
        typeLoading = true

        try {
            // 非强制刷新先读缓存
            if (!forceRefresh) {
                val cache =
                    AHUCache.getLostFoundType()

                if (cache.isNotEmpty()) {
                    allLostFoundType =
                        AllLostFoundType(
                            code = 0,
                            msg = "cache",
                            `object` = cache
                        )
                }
            }

            // 联网更新
            val result =
                AHURepository.getAllLostFoundType()

            if (result.code == 0) {
                allLostFoundType = result.data

                AHUCache.saveLostFoundType(
                    result.data.`object`
                )

                Log.d(
                    "lostfound",
                    "alltype = ${result.data}"
                )

                errorMessage = null
            } else {
                errorMessage = result.msg
            }
        } catch (t: Throwable) {
            if (allLostFoundType == null) {
                errorMessage =
                    t.message ?: "获取类型失败"
            }
        } finally {
            typeLoading = false
        }
    }

    /**
     * 切换状态
     */
    fun switchState(state: Int) {
        if (currentState == state) return

        currentState = state
        currentPage = 1
        totalPages = 1

        // 先读缓存
        lostFoundList = AHUCache.getLostFoundList(state)

        // 再请求最新数据
        fetchFirstPage()
    }

    /**
     * 获取第一页（覆盖）
     */
    fun fetchFirstPage() = viewModelScope.launch {
        listLoading = true
        try {
            val result = AHURepository.getLostFoundList(
                pageNo = 1,
                pageSize = pageSize,
                state = currentState
            )
            Log.d("lostfound", "alllist = ${result.data.toString()}")
            if (result.code == 0) {
                val pageData = result.data.data

                currentPage = pageData.pageNum
                totalPages = pageData.pages

                lostFoundList = pageData.list

                // 覆盖缓存
                AHUCache.saveLostFoundList(
                    currentState,
                    pageData.list
                )

                errorMessage = null
            } else {
                errorMessage = result.msg
            }
        } catch (t: Throwable) {
            errorMessage = t.message ?: "获取列表失败"
        } finally {
            listLoading = false
        }
    }

    /**
     * 刷新
     * 成功后清空缓存并覆盖最新数据
     */
    fun refreshList() {
        viewModelScope.launch {
            isRefreshing = true

            try {
                // 强制刷新校区
                getAllCampus(true)

                // 强制刷新类型
                getAllLostFoundType(true)

                // 强制刷新帖子第一页
                val result =
                    AHURepository.getLostFoundList(
                        pageNo = 1,
                        pageSize = pageSize,
                        state = currentState
                    )

                if (result.code == 0) {
                    val pageData =
                        result.data.data

                    currentPage =
                        pageData.pageNum

                    totalPages =
                        pageData.pages

                    lostFoundList =
                        pageData.list

                    AHUCache.clearLostFoundList(
                        currentState
                    )

                    AHUCache.saveLostFoundList(
                        currentState,
                        pageData.list
                    )

                    errorMessage = null
                } else {
                    errorMessage = result.msg
                }
            } catch (t: Throwable) {
                errorMessage =
                    t.message ?: "刷新失败"
            } finally {
                isRefreshing = false
            }
        }
    }

    /**
     * 加载更多（滑到底触发）
     */
    fun loadMore() {
        if (isLoadingMore || listLoading || !hasMore) return

        viewModelScope.launch {
            isLoadingMore = true
            try {
                val nextPage = currentPage + 1

                val result = AHURepository.getLostFoundList(
                    pageNo = nextPage,
                    pageSize = pageSize,
                    state = currentState
                )

                if (result.code == 0) {
                    val pageData = result.data.data

                    currentPage = pageData.pageNum
                    totalPages = pageData.pages

                    val newList = pageData.list

                    // 追加到当前列表
                    lostFoundList = lostFoundList + newList

                    // 追加缓存
                    AHUCache.appendLostFoundList(
                        currentState,
                        newList
                    )

                    errorMessage = null
                } else {
                    errorMessage = result.msg
                }
            } catch (t: Throwable) {
                errorMessage = t.message ?: "加载更多失败"
            } finally {
                isLoadingMore = false
            }
        }
    }
    fun publishLostFound(
        linkman: String,
        phone: String,
        title: String,
        num1: String,
        campusId: String,
        typeId: String,
        state: String
    ) {
        viewModelScope.launch {
            AHURepository.publishLostFound(
                LostFoundPublishRequest(
                    imgs = emptyList(),
                    linkman = linkman,
                    phone = phone,
                    typeid = typeId,
                    num1 = num1,
                    campusid = campusId,
                    title = title,
                    state = state,
                    auditresult = 1
                )
            )

            refreshList()
        }
    }
    fun deleteLostFound(
        id: String
    ) {
        viewModelScope.launch {
            try {
                val result =
                    AHURepository.deleteLostFound(id)

                if (result.isSuccessful) {
                    lostFoundList =
                        lostFoundList.filterNot {
                            it.id == id
                        }

                    refreshList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    init {
        getAllCampus()
        getAllLostFoundType()

        // 初始化先读缓存
        lostFoundList = AHUCache.getLostFoundList(currentState)

        // 自动联网更新第一页
        fetchFirstPage()
    }
}