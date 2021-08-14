package com.ahu.ahutong.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.ahu.ahutong.R
import com.ahu.ahutong.utils.BitmapUtils
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan


class EmojiView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
): View(context, attrs, defStyleAttr, defStyleRes){
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0): this(
        context,
        attrs,
        defStyleAttr,
        0
    )
    constructor(context: Context, attrs: AttributeSet? = null): this(context, attrs, 0, 0)
    constructor(context: Context): this(context, null, 0, 0)


    private var hand_right_rect: Rect
    private var hand_left_rect: Rect
    private val hand_src_rect: Rect
    private val bm_dest_rect: Rect
    private val bm_src_rect: Rect
    private var bm_right_hand: Bitmap
    private var bm_left_hand: Bitmap
    private var bm_password: Bitmap
    private var bm_username: Bitmap
    private val paint: Paint
    private val bm_width = dip2px(120f)
    private val bm_height = dip2px(103f)
    private val hand_width = dip2px(40f)
    private val hand_height  = dip2px(80f)
    var isOpen = true
    private var eye_x = dip2px(34f)
    private var eye_y = dip2px(70f)
    private var hand_x = getXByY(dip2px(150f))
    private var hand_y = dip2px(150f)
    //旋转后的长宽
    private val h_width = (hand_width * sin(Math.toRadians(70.0)) + hand_height * cos(Math.toRadians(70.0))).toInt()
    private val h_height = (hand_width * cos(Math.toRadians(70.0)) + hand_height * sin(Math.toRadians(70.0))).toInt()
    /**
     * 初始化
     */
    init {
        //设置透明背景
        setBackgroundColor(Color.TRANSPARENT)
        //初始化图片资源
        bm_username = BitmapFactory.decodeResource(resources, R.mipmap.emoji_username)
        bm_password = BitmapFactory.decodeResource(resources, R.mipmap.emoji_password)
        bm_left_hand = BitmapFactory.decodeResource(resources, R.mipmap.emoji_left_hand)
        bm_right_hand = BitmapFactory.decodeResource(resources, R.mipmap.emoji_right_hand)
        //压缩图片
        bm_username = BitmapUtils.compressBitmap(bm_username, bm_width, bm_height, true)
        bm_password = BitmapUtils.compressBitmap(bm_password, bm_width, bm_height, true)
        bm_left_hand = BitmapUtils.compressBitmap(bm_left_hand, hand_width, hand_height, false)
        bm_right_hand = BitmapUtils.compressBitmap(bm_right_hand, hand_width, hand_height, false)
        //旋转手
        val matrix = Matrix()
        //向右旋转20度
        matrix.setRotate(20f)
        bm_left_hand = Bitmap.createBitmap(
            bm_left_hand,
            0,
            0,
            hand_width.toInt(),
            hand_height.toInt(),
            matrix,
            true
        )
        matrix.reset()
        //向左旋转20度
        matrix.setRotate(-20f)
        bm_right_hand = Bitmap.createBitmap(
            bm_right_hand,
            0,
            0,
            hand_width.toInt(),
            hand_height.toInt(),
            matrix,
            true
        )
        //笔
        paint = Paint()
        //抗锯齿
        paint.isAntiAlias = true
        //矩形
        bm_src_rect = Rect(0, 0, bm_width.toInt(), bm_height.toInt())
        val off_x = dip2px(100f).toInt() - bm_width.toInt()/2
        val off_y = dip2px(75f).toInt() - bm_height.toInt()/2
        bm_dest_rect = Rect(off_x, off_y, bm_width.toInt() + off_x, bm_height.toInt() + off_y)
        //手矩形
        hand_src_rect = Rect(0, 0, h_width, h_height)
        hand_left_rect = Rect(
            hand_x.toInt(),
            hand_y.toInt(),
            h_width + hand_x.toInt(),
            h_height + hand_y.toInt()
        )
        hand_right_rect = Rect(
            dip2px(125f).toInt() - hand_x.toInt(), hand_y.toInt(), h_width + dip2px(
                130f
            ).toInt() - hand_x.toInt(), h_height + hand_y.toInt()
        )
    }

    /**
     * 设置固定的宽高
     * @param widthMeasureSpec Int
     * @param heightMeasureSpec Int
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(dip2px(200f).toInt(), dip2px(150f).toInt())
    }

    /**
     * 绘制
     * @param canvas Canvas
     */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null){
            return
        }
        if (isOpen){
            canvas.drawBitmap(bm_username, bm_src_rect, bm_dest_rect, paint)
        }else{
            canvas.drawBitmap(bm_password, bm_src_rect, bm_dest_rect, paint)
        }
        canvas.drawBitmap(bm_left_hand, hand_src_rect, hand_left_rect, paint)
        canvas.drawBitmap(bm_right_hand, hand_src_rect, hand_right_rect, paint)
    }

    /**
     * 进入输入用户名的状态
     */
    fun open(){
        isOpen = true
        val moveVa = ValueAnimator.ofFloat(hand_y, dip2px(150f))
            .setDuration(300)
        moveVa.addUpdateListener { animation ->
            hand_x = getXByY(animation.animatedValue as Float)
            hand_y = animation.animatedValue as Float
            hand_left_rect = Rect(
                hand_x.toInt(),
                hand_y.toInt(),
                h_width + hand_x.toInt(),
                h_height + hand_y.toInt()
            )
            hand_right_rect = Rect(
                dip2px(125f).toInt() - hand_x.toInt(),
                hand_y.toInt(),
                h_width + dip2px(130f).toInt() - hand_x.toInt(),
                h_height + hand_y.toInt()
            )
            invalidate()
        }
        moveVa.start()
    }

    /**
     *进入输入密码的状态
     */
    fun close(){
        isOpen = false
        val moveVa = ValueAnimator.ofFloat(hand_y, eye_y)
            .setDuration(300)
        moveVa.addUpdateListener { animation ->
            hand_x = getXByY(animation.animatedValue as Float)
            hand_y = animation.animatedValue as Float
            hand_left_rect = Rect(
                hand_x.toInt(),
                hand_y.toInt(),
                h_width + hand_x.toInt(),
                h_height + hand_y.toInt()
            )
            hand_right_rect = Rect(
                dip2px(125f).toInt() - hand_x.toInt(),
                hand_y.toInt(),
                h_width + dip2px(130f).toInt() - hand_x.toInt(),
                h_height + hand_y.toInt()
            )
            invalidate()
        }
        moveVa.start()
    }

    /**
     * dp 转 px
     * @param dpValue Float
     * @return Float
     */
    private fun dip2px(dpValue: Float): Float {
        val scale: Float = resources.displayMetrics.density
        return dpValue * scale + 0.5f
    }


    /**
     * get x by Y
     */
    private fun getXByY(y: Float): Float{
        return ((y - eye_y)/tan(Math.toRadians(dip2px(110f).toDouble())) + eye_x).toFloat()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        bm_username.recycle()
        bm_password.recycle()
        bm_right_hand.recycle()
        bm_left_hand.recycle()
    }
}