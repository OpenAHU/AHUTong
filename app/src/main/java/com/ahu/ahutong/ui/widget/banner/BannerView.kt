package com.ahu.ahutong.ui.widget.banner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ahu.ahutong.R


/**
 * @Author: SinkDev
 * @Date: 2021/8/3-上午10:52
 * @Email: 468766131@qq.com
 */
class BannerView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val scaledTouchSlop: Int
    private var lastX = 0f
    private var startX = 0f
    private var lastY = 0f
    private var startY = 0f
    private val MESSAGE_CHANGE_START = 0
    private val MESSAGE_CHANGE_PAGE = 1
    private val MESSAGE_CAHNGE_STOP = 2
    private val viewPager2: ViewPager2
    private val indicatorContainer: LinearLayout
    private val indicatorView: MutableList<ImageView> = mutableListOf()
    private lateinit var adapter: BannerAdapter
    private val paint: Paint

    //指示器的样子
    var indicatorSelectedBitmap: Bitmap
    var indicatorUnselectedBitmap: Bitmap

    //指示器大小
    private var indicatorViewSize: Int = 30

    //指示器之间的距离
    private var indicatorViewMargin: Int = 5

    private val mHander: Handler

    private var isAutoPlay: Boolean = true

    /**
     *  创建一堆构造器重载
     */
    constructor(context: Context?) : this(context, null, 0, 0)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    init {
        val bannerView = View.inflate(context, R.layout.layout_banner, this)
        viewPager2 = bannerView.findViewById(R.id.banner_viewpager)
        indicatorContainer = bannerView.findViewById(R.id.banner_indicator_container)
        viewPager2.registerOnPageChangeCallback(BannerPageChangeCallBack())
        scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        paint = Paint()
        paint.color = Color.RED
        indicatorSelectedBitmap = getIndicatorBitmap(true)
        indicatorUnselectedBitmap = getIndicatorBitmap(false)
        mHander = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    MESSAGE_CHANGE_PAGE -> {
                        viewPager2.setCurrentItem(viewPager2.currentItem + 1, true)
                        if (isAutoPlay) {
                            sendEmptyMessageDelayed(MESSAGE_CHANGE_PAGE, 8000)
                        }
                    }
                    MESSAGE_CAHNGE_STOP -> {
                        isAutoPlay = false
                    }
                    MESSAGE_CHANGE_START -> {
                        isAutoPlay = true
                        sendEmptyMessageDelayed(MESSAGE_CHANGE_PAGE, 8000)
                    }
                }

            }
        }

    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.action
        if (action == MotionEvent.ACTION_DOWN) {
            lastX = ev.rawX
            startX = lastX
            lastY = ev.rawY
            startY = lastY
        } else if (action == MotionEvent.ACTION_MOVE) {
            lastX = ev.rawX
            lastY = ev.rawY
            if (viewPager2.isUserInputEnabled) {
                val distanceX = Math.abs(lastX - startX)
                val distanceY = Math.abs(lastY - startY)
                val disallowIntercept = distanceX > distanceY
                if (viewPager2.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                    distanceX > scaledTouchSlop && distanceX > distanceY
                } else {
                    distanceY > scaledTouchSlop && distanceY > distanceX
                }
                parent.requestDisallowInterceptTouchEvent(disallowIntercept)
            }
        } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            return Math.abs(lastX - startX) > scaledTouchSlop || Math.abs(lastY - startY) > scaledTouchSlop
        }
        return super.onInterceptTouchEvent(ev)
    }

    fun getAdapter(): BannerAdapter {
        return adapter
    }

    /**
     * 设置Adapter
     * @param adapter BannerAdapter
     */
    fun setAdapter(adapter: BannerAdapter) {
        this.adapter = adapter
        indicatorContainer.removeAllViews()
        indicatorView.clear()
        if (adapter.getItemCount() == 0) {
            return
        }
        //根据adapter设置viewPager的Adapter
        viewPager2.adapter = object : RecyclerView.Adapter<BannerHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerHolder {
                return BannerHolder(adapter.getImageView(this@BannerView, viewType))
            }

            override fun onBindViewHolder(holder: BannerHolder, position: Int) {
                holder.itemView.layoutParams =
                    LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            }

            override fun getItemCount(): Int {
                if (adapter.getItemCount() == 0)
                    return 0
                return if (adapter.getItemCount() != 0) adapter.getItemCount() + 2 else 0
            }

            override fun getItemViewType(position: Int): Int {
                return if (position == 0)
                    adapter.getItemCount() - 1 //第一个放最后一张
                else if (position == itemCount - 1)
                    0 //最后一个放第一张
                else
                    position - 1
            }
        }

        //根据adapter设置指示器
        var i = 0
        while (i < adapter.getItemCount()) {
            val imageView = ImageView(context)
            val layoutParams = LayoutParams(indicatorViewSize, indicatorViewSize)
            layoutParams.leftMargin = indicatorViewMargin
            layoutParams.rightMargin = indicatorViewMargin
            imageView.layoutParams = layoutParams
            indicatorView.add(imageView)
            i++
        }
        indicatorView.forEach {
            indicatorContainer.addView(it)
        }
        //设置默认选中
        viewPager2.setCurrentItem(1, false)
        //是否自动切换
        if (isAutoPlay) {
            mHander.sendEmptyMessage(MESSAGE_CHANGE_START)
        }
    }

    /**
     * 设置指示器大小
     * @param size Int
     */
    fun setIndicatorViewSize(size: Int) {
        indicatorViewSize = size
        indicatorView.forEach {
            (it.layoutParams as LayoutParams).apply {
                height = indicatorViewSize
                width = indicatorViewSize
            }
        }
        invalidate()
    }

    /**
     * 指示器大小
     * @return Int
     */
    fun getIndicatorViewSize() = indicatorViewSize


    /**
     * 设置指示器之间的距离
     * @param margin Int
     */
    fun setIndicatorViewMargin(margin: Int) {
        indicatorViewMargin = margin
        indicatorView.forEach {
            (it.layoutParams as LayoutParams).apply {
                leftMargin = indicatorViewMargin
                rightMargin = indicatorViewMargin
            }
        }
        invalidate()
    }

    /**
     * 指示器之间的距离
     * @return Int
     */
    fun getIndicatorViewMargin() = indicatorViewMargin

    /**
     * 是否自动播放
     * @return Boolean
     */
    fun isAutoPlay() = isAutoPlay

    /**
     * 设置是否自动播放
     * @param autoPlay Boolean
     */
    fun setAutoPlay(autoPlay: Boolean) {
        if (!isAutoPlay && autoPlay) {
            mHander.sendEmptyMessage(MESSAGE_CHANGE_START)
        }
        if (isAutoPlay && !autoPlay) {
            mHander.sendEmptyMessage(MESSAGE_CAHNGE_STOP)
        }
    }

    private fun getIndicatorBitmap(isSelected: Boolean): Bitmap {
        if (isSelected) {
            paint.color = Color.BLUE
        } else {
            paint.color = Color.BLACK
        }
        val bitmap =
            Bitmap.createBitmap(indicatorViewSize, indicatorViewSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawCircle(
            indicatorViewSize.toFloat() / 2, indicatorViewSize.toFloat() / 2,
            indicatorViewSize.toFloat() / 2, paint
        )
        canvas.save()
        return bitmap
    }

    inner class BannerPageChangeCallBack : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            //循环播放
            if (position == 0) {
                viewPager2.setCurrentItem(adapter.getItemCount(), false)
                return
            }
            if (position == adapter.getItemCount() + 1) {
                viewPager2.setCurrentItem(1, false)
                return
            }
            //设置指示器
            indicatorView.forEachIndexed { index, imageView ->
                if (index == position - 1) {
                    imageView.setImageBitmap(indicatorSelectedBitmap)
                } else {
                    imageView.setImageBitmap(indicatorUnselectedBitmap)
                }
            }

        }
    }

    abstract class BannerAdapter {
        abstract fun getItemCount(): Int
        abstract fun getImageView(parentView: View, position: Int): ImageView
    }

    inner class BannerHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView)

}