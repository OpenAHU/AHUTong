package com.simon.library.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NumberPickerView extends View {

    // 默认的未选中文本的颜色
    private static final int DEFAULT_TEXT_COLOR_NORMAL = 0XFF333333;

    // 默认的选中文本的颜色
    private static final int DEFAULT_TEXT_COLOR_SELECTED = 0XFFF56313;

    // 默认的未选中文本大小
    private static final int DEFAULT_TEXT_SIZE_NORMAL_SP = 14;

    // 默认的选中文本大小
    private static final int DEFAULT_TEXT_SIZE_SELECTED_SP = 16;

    // 默认的内容右侧文本的大小
    private static final int DEFAULT_TEXT_SIZE_HINT_SP = 14;

    // 默认的右侧内容与内容的距离
    private static final int DEFAULT_MARGIN_START_OF_HINT_DP = 8;

    // 在 wrap_content 模式下，右侧文字和控件右侧的距离
    private static final int DEFAULT_MARGIN_END_OF_HINT_DP = 8;

    // 默认的选中线颜色
    private static final int DEFAULT_DIVIDER_COLOR = 0XFFF56313;

    private float mDividerHeight = 2f; // 选中线高度

    // 默认的选中线左右距离
    private static final int DEFAULT_DIVIDER_MARGIN_HORIZONTAL = 0;

    // 默认的展示数量，偶数会被做处理
    private static final int DEFAULT_SHOWN_COUNT = 3;

    //==以下三个属性均与handler有关==//
    // message's what argument to refresh current state, used by mHandler
    private static final int HANDLER_WHAT_REFRESH = 1;
    // message's what argument to respond value changed event, used by mHandler
    private static final int HANDLER_WHAT_LISTENER_VALUE_CHANGED = 2;
    // message's what argument to request layout, used by mHandlerInMainThread
    private static final int HANDLER_WHAT_REQUEST_LAYOUT = 3;

    // interval time to scroll the distance of one item's height
    private static final int HANDLER_INTERVAL_REFRESH = 32;//millisecond

    // in millisecond unit, default duration of scrolling an item' distance
    private static final int DEFAULT_INTERVAL_REVISE_DURATION = 300;

    // max and min durations when scrolling from one value to another
    private static final int DEFAULT_MIN_SCROLL_BY_INDEX_DURATION = DEFAULT_INTERVAL_REVISE_DURATION;
    private static final int DEFAULT_MAX_SCROLL_BY_INDEX_DURATION = DEFAULT_INTERVAL_REVISE_DURATION * 2;

    private static final boolean DEFAULT_SHOW_DIVIDER = true;
    private static final boolean DEFAULT_CURRENT_ITEM_INDEX_EFFECT = false;
    private static final boolean DEFAULT_RESPOND_CHANGE_ON_DETACH = false;

    private int mTextColorNormal = DEFAULT_TEXT_COLOR_NORMAL;
    private int mTextColorSelected = DEFAULT_TEXT_COLOR_SELECTED;
    private int mTextColorHint = DEFAULT_TEXT_COLOR_SELECTED;
    private int mTextSizeNormal = 0;
    private int mTextSizeSelected = 0;
    private int mTextSizeHint = 0;
    private int mWidthOfHintText = 0;
    private int mWidthOfAlterHint = 0;
    private int mMarginStartOfHint = 0;
    private int mMarginEndOfHint = 0;
    private int mDividerColor = DEFAULT_DIVIDER_COLOR;
    private int mDividerMarginL = DEFAULT_DIVIDER_MARGIN_HORIZONTAL;
    private int mDividerMarginR = DEFAULT_DIVIDER_MARGIN_HORIZONTAL;
    private int mShownCount = DEFAULT_SHOWN_COUNT;
    private int mMinShowIndex = -1;
    private int mMaxShowIndex = -1;

    private int mMaxWidthOfDisplayedValues = 0;
    private int mMaxHeightOfDisplayedValues = 0;
    private int mMaxWidthOfAlterArrayWithMeasureHint = 0;
    private int mMaxWidthOfAlterArrayWithoutMeasureHint = 0;
    private int mPrevPickedIndex = 0;
    private int mMiniVelocityFling = 150;
    private int mScaledTouchSlop = 8;
    private String mHintText;

    // private String mAlterHint;
    //friction used by scroller when fling
    private float mFriction = 1f;
    private float mTextSizeNormalCenterYOffset = 0f;
    private float mTextSizeSelectedCenterYOffset = 0f;
    private float mTextSizeHintCenterYOffset = 0f;
    //true to set to the current position, false set position to 0
    private boolean mCurrentItemIndexEffect = DEFAULT_CURRENT_ITEM_INDEX_EFFECT;
    //true if NumberPickerView has initialized
    private boolean mHasInit = false;
    // if displayed values' number is less than show count, then this value will be false.

    // if you want you set to linear mode from wrap mode when scrolling, then this value will be true.
    private boolean mPendingWrapToLinear = false;

    // this is to set which thread to respond onChange... listeners including
    // OnValueChangeListener, OnValueChangeListenerRelativeToRaw and OnScrollListener when view is
    // scrolling or starts to scroll or stops scrolling.

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;

    private final Paint mPaintDivider = new Paint();
    private final TextPaint mPaintText = new TextPaint();
    private final Paint mPaintHint = new Paint();

    private String[] mDisplayedValues;


    private HandlerThread mHandlerThread;
    private Handler mHandlerInNewThread;
    private Handler mHandlerInMainThread;

    private final Map<String, Integer> mTextWidthCache = new ConcurrentHashMap<>();

    /**
     * 内容选中的回调
     */
    public interface OnValueChangeListener {
        /**
         *
         * @param picker 控件本身
         * @param oldVal 选中前的内容下标
         * @param newVal 选中后的内容下标
         */
        void onValueChange(NumberPickerView picker, int oldVal, int newVal);
    }

    /**
     * 设置选中线的粗细
     * @param height 粗细
     */
    public void setDividerHeight(float height) {
        mDividerHeight = height;
    }

    /**
     * 设置选中线距离左侧的距离
     * @param marginL 距离
     */
    public void setDividerMarginL(int marginL) {
        mDividerMarginL = marginL;
    }

    /**
     * 设置选中线距离右侧的距离
     * @param marginR 距离
     */
    public void setDividerMarginR(int marginR) {
        mDividerMarginR = marginR;
    }

    private OnValueChangeListener mOnValueChangeListener; //compatible for NumberPicker


    // The current scroll state of the NumberPickerView.
    public NumberPickerView(Context context) {
        super(context);
        init(context);
    }

    public NumberPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NumberPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(50);
    }


    private void init(Context context) {
        mScroller = new Scroller(context);
        mMiniVelocityFling = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        if (mTextSizeNormal == 0) {
            mTextSizeNormal = sp2px(context, DEFAULT_TEXT_SIZE_NORMAL_SP);
        }
        if (mTextSizeSelected == 0) {
            mTextSizeSelected = sp2px(context, DEFAULT_TEXT_SIZE_SELECTED_SP);
        }
        if (mTextSizeHint == 0) {
            mTextSizeHint = sp2px(context, DEFAULT_TEXT_SIZE_HINT_SP);
        }
        if (mMarginStartOfHint == 0) {
            mMarginStartOfHint = dp2px(context, DEFAULT_MARGIN_START_OF_HINT_DP);
        }
        if (mMarginEndOfHint == 0) {
            mMarginEndOfHint = dp2px(context, DEFAULT_MARGIN_END_OF_HINT_DP);
        }
        mPaintDivider.setColor(mDividerColor);
        mPaintDivider.setAntiAlias(true);
        mPaintDivider.setStyle(Paint.Style.STROKE);
        mPaintDivider.setStrokeWidth(mDividerHeight);


        mPaintText.setColor(mTextColorNormal);
        mPaintText.setAntiAlias(true);
        mPaintText.setTextAlign(Align.CENTER);

        mPaintHint.setColor(mTextColorHint);
        mPaintHint.setAntiAlias(true);
        mPaintHint.setTextAlign(Align.CENTER);
        mPaintHint.setTextSize(mTextSizeHint);

        if (mShownCount % 2 == 0) {
            mShownCount++;
        }
        if (mMinShowIndex == -1 || mMaxShowIndex == -1) {
            updateValueForInit();
        }
        initHandler();
    }

    @SuppressLint("HandlerLeak")
    private void initHandler() {
        mHandlerThread = new HandlerThread("HandlerThread-For-Refreshing");
        mHandlerThread.start();

        mHandlerInNewThread = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLER_WHAT_REFRESH:
                        if (!mScroller.isFinished()) {

                            mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH, 0, 0, msg.obj), HANDLER_INTERVAL_REFRESH);
                        } else {
                            int duration = 0;
                            int willPickIndex;
                            //if scroller finished(not scrolling), then adjust the position
                            if (mCurrDrawFirstItemY != 0) {//need to adjust

                                if (mCurrDrawFirstItemY < (-mItemHeight / 2)) {
                                    //adjust to scroll upward
                                    duration = (int) ((float) DEFAULT_INTERVAL_REVISE_DURATION * (mItemHeight + mCurrDrawFirstItemY) / mItemHeight);
                                    mScroller.startScroll(0, mCurrDrawGlobalY, 0, mItemHeight + mCurrDrawFirstItemY, duration * 3);
                                    willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY + mItemHeight + mCurrDrawFirstItemY);
                                } else {
                                    //adjust to scroll downward
                                    duration = (int) ((float) DEFAULT_INTERVAL_REVISE_DURATION * (-mCurrDrawFirstItemY) / mItemHeight);
                                    mScroller.startScroll(0, mCurrDrawGlobalY, 0, mCurrDrawFirstItemY, duration * 3);
                                    willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY + mCurrDrawFirstItemY);
                                }
                                postInvalidate();
                            } else {

                                //get the index which will be selected
                                willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY);
                            }
                            Message changeMsg = getMsg(HANDLER_WHAT_LISTENER_VALUE_CHANGED, mPrevPickedIndex, willPickIndex, msg.obj);
                            mHandlerInMainThread.sendMessageDelayed(changeMsg, duration * 2L);
                        }
                        break;
                    case HANDLER_WHAT_LISTENER_VALUE_CHANGED:
                        respondPickedValueChanged(msg.arg1, msg.arg2, msg.obj);
                        break;
                }
            }
        };
        //Main Handler
        mHandlerInMainThread = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case HANDLER_WHAT_REQUEST_LAYOUT:
                        requestLayout();
                        break;
                    case HANDLER_WHAT_LISTENER_VALUE_CHANGED:
                        respondPickedValueChanged(msg.arg1, msg.arg2, msg.obj);
                        break;
                }
            }
        };
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        updateMaxWHOfDisplayedValues(false);
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        super.onSizeChanged(w, h, oldWidth, oldHeight);
        mViewWidth = w;
        mViewHeight = h;
        mItemHeight = mViewHeight / mShownCount;
        mViewCenterX = ((float) (mViewWidth + getPaddingLeft() - getPaddingRight())) / 2;
        int defaultValue = 0;
        if (getOneRecycleSize() > 1) {
            if (mHasInit) {
                defaultValue = getValue();
            } else if (mCurrentItemIndexEffect) {
                defaultValue = mCurrDrawFirstItemIndex + (mShownCount - 1) / 2;
            }
        }
        correctPositionByDefaultValue(defaultValue);
        updateFontAttr();
        updateNotWrapYLimit();
        updateDividerAttr();
        mHasInit = true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mHandlerThread == null || !mHandlerThread.isAlive()) {
            initHandler();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandlerThread.quit();
        //These codes are for dialog or PopupWindow which will be used for more than once.
        //Not an elegant solution, if you have any good idea, please let me know, thank you.
        if (mItemHeight == 0) {
            return;
        }
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            mCurrDrawGlobalY = mScroller.getCurrY();
            calculateFirstItemParameterByGlobalY();
            if (mCurrDrawFirstItemY != 0) {
                if (mCurrDrawFirstItemY < (-mItemHeight / 2)) {
                    mCurrDrawGlobalY = mCurrDrawGlobalY + mItemHeight + mCurrDrawFirstItemY;
                } else {
                    mCurrDrawGlobalY = mCurrDrawGlobalY + mCurrDrawFirstItemY;
                }
                calculateFirstItemParameterByGlobalY();
            }

        }
        // see the comments on mRespondChangeOnDetach, if mRespondChangeOnDetach is false,
        // please initialize NumberPickerView's data every time setting up NumberPickerView,
        // set the demo of GregorianLunarCalendar
        int currPickedIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY);
        // if this view is used in same dialog or PopupWindow more than once, and there are several
        // NumberPickerViews linked, such as Gregorian Calendar with MonthPicker and DayPicker linked,
        // set mRespondChangeWhenDetach true to respond onValueChanged callbacks if this view is scrolling
        // when detach from window, but this solution is unlovely and may cause NullPointerException
        // (even i haven't found this NullPointerException),
        // so I highly recommend that every time setting up a reusable dialog with a NumberPickerView in it,
        // please initialize NumberPickerView's data, and in this way, you can set mRespondChangeWhenDetach false.
        if (currPickedIndex != mPrevPickedIndex && DEFAULT_RESPOND_CHANGE_ON_DETACH) {
            try {
                if (mOnValueChangeListener != null) {
                    mOnValueChangeListener.onValueChange(NumberPickerView.this, mPrevPickedIndex, currPickedIndex);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mPrevPickedIndex = currPickedIndex;
    }

    private int getOneRecycleSize() {
        return mMaxShowIndex - mMinShowIndex + 1;
    }

    /**
     * 设置展示的内容
     * @param newDisplayedValues 内容集合
     * @param pickedIndex 被选中内容的下标
     * @param needRefresh 是否立即刷新view
     */
    public void setDisplayedValuesAndPickedIndex(String[] newDisplayedValues, int pickedIndex, boolean needRefresh) {
        stopScrolling();
        if (newDisplayedValues == null) {
            throw new IllegalArgumentException("newDisplayedValues should not be null.");
        }
        updateContent(newDisplayedValues);
        updateMaxWHOfDisplayedValues(true);
        updateNotWrapYLimit();
        updateValue();
        mPrevPickedIndex = Math.max(pickedIndex, 0) + mMinShowIndex;
        correctPositionByDefaultValue(Math.max(pickedIndex, 0));
        if (needRefresh) {
            mHandlerInNewThread.sendMessageDelayed(getMsg(), 0);
            postInvalidate();
        }
    }

    /**
     * 设置展示的内容
     * @param newDisplayedValues 展示的内容
     * @param needRefresh 是否立即刷新view
     */
    public void setDisplayedValues(String[] newDisplayedValues, boolean needRefresh) {
        setDisplayedValuesAndPickedIndex(newDisplayedValues, 0, needRefresh);
    }


    /**
     *从当前下标移到到某个下标
     * @param toValue     你想要移到到的下标
     * @param needRespond 是否想要回调onValueChange
     */
    public void smoothScrollToValue(int toValue, boolean needRespond) {
        smoothScrollToValue(getValue(), toValue, needRespond);
    }

    /**
     * 从某个下标移到到另一个下标
     * @param fromValue 开始的下标
     * @param toValue 要移到到的下标
     */
    public void smoothScrollToValue(int fromValue, int toValue) {
        smoothScrollToValue(fromValue, toValue, true);
    }

    /**
     * 从某个下标移到到另一个下标
     * @param fromValue 开始的下标
     * @param toValue 要移到到的下标
     * @param needRespond 是否想要回调onValueChange
     */
    public void smoothScrollToValue(int fromValue, int toValue, boolean needRespond) {
        int deltaIndex;
        deltaIndex = toValue - fromValue;
        setValue(fromValue);
        if (fromValue == toValue) {
            return;
        }
        scrollByIndexSmoothly(deltaIndex, needRespond);
    }

    /**
     * used by handlers to respond onchange callbacks
     *
     * @param oldVal        prevPicked value
     * @param newVal        currPicked value
     * @param respondChange if want to respond onchange callbacks
     */
    private void respondPickedValueChanged(int oldVal, int newVal, Object respondChange) {

        if (oldVal != newVal) {
            if (!(respondChange instanceof Boolean) || (Boolean) respondChange) {
                if (mOnValueChangeListener != null) {
                    mOnValueChangeListener.onValueChange(NumberPickerView.this, oldVal, newVal);
                }
            }
        }
        mPrevPickedIndex = newVal;
        if (mPendingWrapToLinear) {
            mPendingWrapToLinear = false;
            internalSetWrapToLinear();
        }
    }

    private void scrollByIndexSmoothly(int deltaIndex) {
        scrollByIndexSmoothly(deltaIndex, true);
    }

    /**
     * @param deltaIndex  the delta index it will scroll by
     * @param needRespond need Respond to the ValueChange callback When Scrolling, default is false
     */
    private void scrollByIndexSmoothly(int deltaIndex, boolean needRespond) {

        int willPickRawIndex = getPickedIndexRelativeToRaw();
        if (willPickRawIndex + deltaIndex > mMaxShowIndex) {
            deltaIndex = mMaxShowIndex - willPickRawIndex;
        } else if (willPickRawIndex + deltaIndex < mMinShowIndex) {
            deltaIndex = mMinShowIndex - willPickRawIndex;
        }

        int duration;
        int dy;
        if (mCurrDrawFirstItemY < (-mItemHeight / 2)) {
            //scroll upwards for a distance of less than mItemHeight
            dy = mItemHeight + mCurrDrawFirstItemY;
            duration = (int) ((float) DEFAULT_INTERVAL_REVISE_DURATION * (mItemHeight + mCurrDrawFirstItemY) / mItemHeight);
            if (deltaIndex < 0) {
                duration = -duration - deltaIndex * DEFAULT_INTERVAL_REVISE_DURATION;
            } else {
                duration = duration + deltaIndex * DEFAULT_INTERVAL_REVISE_DURATION;
            }
        } else {
            //scroll downwards for a distance of less than mItemHeight
            dy = mCurrDrawFirstItemY;
            duration = (int) ((float) DEFAULT_INTERVAL_REVISE_DURATION * (-mCurrDrawFirstItemY) / mItemHeight);
            if (deltaIndex < 0) {
                duration = duration - deltaIndex * DEFAULT_INTERVAL_REVISE_DURATION;
            } else {
                duration = duration + deltaIndex * DEFAULT_INTERVAL_REVISE_DURATION;
            }
        }
        dy = dy + deltaIndex * mItemHeight;
        if (duration < DEFAULT_MIN_SCROLL_BY_INDEX_DURATION) {
            duration = DEFAULT_MIN_SCROLL_BY_INDEX_DURATION;
        }
        if (duration > DEFAULT_MAX_SCROLL_BY_INDEX_DURATION) {
            duration = DEFAULT_MAX_SCROLL_BY_INDEX_DURATION;
        }
        mScroller.startScroll(0, mCurrDrawGlobalY, 0, dy, duration);
        if (needRespond) {
            mHandlerInNewThread.sendMessageDelayed(getMsg(), duration / 4);
        } else {
            mHandlerInNewThread.sendMessageDelayed(getMsg(HANDLER_WHAT_REFRESH, 0, 0, false), duration / 4);
        }
        postInvalidate();
    }

    /**
     * 设置下标
     * @param value 下标
     */
    public void setValue(int value) {
        setPickedIndexRelativeToRaw(value);
    }

    /**
     * 获取下标
     * @return 下标
     */
    public int getValue() {
        return getPickedIndexRelativeToRaw();
    }

    /**
     * 获取当前选中的内容
     * @return 选中的内容
     */
    public String getContentByCurrValue() {
        return mDisplayedValues[getValue()];
    }

    /**
     * 设置展示内容右侧的内容
     * @param hintText 右侧的内容
     */
    public void setHintText(String hintText) {
        if (isStringEqual(mHintText, hintText)) {
            return;
        }
        mHintText = hintText;
        mTextSizeHintCenterYOffset = getTextCenterYOffset(mPaintHint.getFontMetrics());
        mWidthOfHintText = getTextWidth(mHintText, mPaintHint);
        mHandlerInMainThread.sendEmptyMessage(HANDLER_WHAT_REQUEST_LAYOUT);
    }

    /**
     * 设置未选中文字的颜色
     * @param normalTextColor 颜色
     */
    public void setNormalTextColor(int normalTextColor) {
        if (mTextColorNormal == normalTextColor) {
            return;
        }
        mTextColorNormal = normalTextColor;
        postInvalidate();
    }

    /**
     * 设置选中文字的颜色
     * @param selectedTextColor 颜色
     */
    public void setSelectedTextColor(int selectedTextColor) {
        if (mTextColorSelected == selectedTextColor) {
            return;
        }
        mTextColorSelected = selectedTextColor;
        postInvalidate();
    }

    /**
     * 设置内容右侧文字的颜色
     * @param hintTextColor 颜色
     */
    public void setHintTextColor(int hintTextColor) {
        if (mTextColorHint == hintTextColor) {
            return;
        }
        mTextColorHint = hintTextColor;
        mPaintHint.setColor(mTextColorHint);
        postInvalidate();
    }

    /**
     * 设置选中线颜色
     * @param dividerColor 颜色
     */
    public void setDividerColor(int dividerColor) {
        if (mDividerColor == dividerColor) {
            return;
        }
        mDividerColor = dividerColor;
        mPaintDivider.setColor(mDividerColor);
        postInvalidate();
    }

    private void setPickedIndexRelativeToRaw(int pickedIndexToRaw) {
        if (mMinShowIndex > -1) {
            if (mMinShowIndex <= pickedIndexToRaw && pickedIndexToRaw <= mMaxShowIndex) {
                mPrevPickedIndex = pickedIndexToRaw;
                correctPositionByDefaultValue(pickedIndexToRaw - mMinShowIndex);
                postInvalidate();
            }
        }
    }

    private int getPickedIndexRelativeToRaw() {
        int willPickIndex;
        if (mCurrDrawFirstItemY != 0) {
            if (mCurrDrawFirstItemY < (-mItemHeight / 2)) {
                willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY + mItemHeight + mCurrDrawFirstItemY);
            } else {
                willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY + mCurrDrawFirstItemY);
            }
        } else {
            willPickIndex = getWillPickIndexByGlobalY(mCurrDrawGlobalY);
        }
        return willPickIndex;
    }


    private void setMinAndMaxShowIndex(int minShowIndex, int maxShowIndex, boolean needRefresh) {
        if (minShowIndex > maxShowIndex) {
            throw new IllegalArgumentException("minShowIndex should be less than maxShowIndex, minShowIndex is "
                    + minShowIndex + ", maxShowIndex is " + maxShowIndex + ".");
        }
        if (mDisplayedValues == null) {
            throw new IllegalArgumentException("mDisplayedValues should not be null, you need to set mDisplayedValues first.");
        } else {
            if (minShowIndex < 0) {
                throw new IllegalArgumentException("minShowIndex should not be less than 0, now minShowIndex is " + minShowIndex);
            } else if (minShowIndex > mDisplayedValues.length - 1) {
                throw new IllegalArgumentException("minShowIndex should not be greater than (mDisplayedValues.length - 1), now " +
                        "(mDisplayedValues.length - 1) is " + (mDisplayedValues.length - 1) + " minShowIndex is " + minShowIndex);
            }

            if (maxShowIndex > mDisplayedValues.length - 1) {
                throw new IllegalArgumentException("maxShowIndex should not be greater than (mDisplayedValues.length - 1), now " +
                        "(mDisplayedValues.length - 1) is " + (mDisplayedValues.length - 1) + " maxShowIndex is " + maxShowIndex);
            }
        }
        mMinShowIndex = minShowIndex;
        mMaxShowIndex = maxShowIndex;
        if (needRefresh) {
            mPrevPickedIndex = mMinShowIndex;
            correctPositionByDefaultValue(0);
            postInvalidate();
        }
    }

    /**
     * 设置滑动的阻力
     * @param friction 默认的是 ViewConfiguration.get(mContext).getScrollFriction()
     *                 如果你使用 setFriction(2 * ViewConfiguration.get(mContext).getScrollFriction())
     *                 阻力将是原来的二倍
     */
    public void setFriction(float friction) {
        if (friction <= 0) {
            throw new IllegalArgumentException("you should set a a positive float friction, now friction is " + friction);
        }
        mFriction = ViewConfiguration.getScrollFriction() / friction;
    }

    /**
     * 设置选中回调
     * @param listener 回调的接口
     */
    public void setOnValueChangedListener(OnValueChangeListener listener) {
        mOnValueChangeListener = listener;
    }

    /**
     * 设置内容的字体
     * @param typeface 字体
     */
    public void setContentTextTypeface(Typeface typeface) {
        mPaintText.setTypeface(typeface);
    }

    /**
     * 设置内容右侧文字的字体
     * @param typeface 字体
     */
    public void setHintTextTypeface(Typeface typeface) {
        mPaintHint.setTypeface(typeface);
    }

    //return index relative to mDisplayedValues from 0.
    private int getWillPickIndexByGlobalY(int globalY) {
        if (mItemHeight == 0) {
            return 0;
        }
        int willPickIndex = globalY / mItemHeight + mShownCount / 2;
        int index = getIndexByRawIndex(willPickIndex, getOneRecycleSize());
        if (0 <= index && index < getOneRecycleSize()) {
            return index + mMinShowIndex;
        } else throw new RuntimeException("出错");
    }

    private int getIndexByRawIndex(int index, int size) {
        if (size <= 0) {
            return 0;
        }
        return index;
    }

    private void internalSetWrapToLinear() {
        int rawIndex = getPickedIndexRelativeToRaw();
        correctPositionByDefaultValue(rawIndex - mMinShowIndex);
        postInvalidate();
    }

    private void updateDividerAttr() {
        int mDividerIndex0 = mShownCount / 2;
        int mDividerIndex1 = mDividerIndex0 + 1;
        dividerY0 = mDividerIndex0 * mViewHeight / mShownCount;
        dividerY1 = mDividerIndex1 * mViewHeight / mShownCount;
        if (mDividerMarginL < 0) {
            mDividerMarginL = 0;
        }
        if (mDividerMarginR < 0) {
            mDividerMarginR = 0;
        }

        if (mDividerMarginL + mDividerMarginR == 0) {
            return;
        }
        if (getPaddingLeft() + mDividerMarginL >= mViewWidth - getPaddingRight() - mDividerMarginR) {
            int surplusMargin = getPaddingLeft() + mDividerMarginL + getPaddingRight() + mDividerMarginR - mViewWidth;
            mDividerMarginL = (int) (mDividerMarginL - (float) surplusMargin * mDividerMarginL / (mDividerMarginL + mDividerMarginR));
            mDividerMarginR = (int) (mDividerMarginR - (float) surplusMargin * mDividerMarginR / (mDividerMarginL + mDividerMarginR));
        }
    }

    private int mNotWrapLimitYTop;
    private int mNotWrapLimitYBottom;

    private void updateFontAttr() {
        if (mTextSizeNormal > mItemHeight) {
            mTextSizeNormal = mItemHeight;
        }
        if (mTextSizeSelected > mItemHeight) {
            mTextSizeSelected = mItemHeight;
        }

        if (mPaintHint == null) {
            throw new IllegalArgumentException("mPaintHint should not be null.");
        }
        mPaintHint.setTextSize(mTextSizeHint);
        mTextSizeHintCenterYOffset = getTextCenterYOffset(mPaintHint.getFontMetrics());
        mWidthOfHintText = getTextWidth(mHintText, mPaintHint);

        if (mPaintText == null) {
            throw new IllegalArgumentException("mPaintText should not be null.");
        }
        mPaintText.setTextSize(mTextSizeSelected);
        mTextSizeSelectedCenterYOffset = getTextCenterYOffset(mPaintText.getFontMetrics());
        mPaintText.setTextSize(mTextSizeNormal);
        mTextSizeNormalCenterYOffset = getTextCenterYOffset(mPaintText.getFontMetrics());
    }

    private void updateNotWrapYLimit() {
        mNotWrapLimitYTop = 0;
        mNotWrapLimitYBottom = -mShownCount * mItemHeight;
        if (mDisplayedValues != null) {
            mNotWrapLimitYTop = (getOneRecycleSize() - mShownCount / 2 - 1) * mItemHeight;
            mNotWrapLimitYBottom = -(mShownCount / 2) * mItemHeight;
        }
    }

    private float downYGlobal = 0;
    private float downY = 0;

    private int limitY(int currDrawGlobalYPreferred) {
        if (currDrawGlobalYPreferred < mNotWrapLimitYBottom) {
            currDrawGlobalYPreferred = mNotWrapLimitYBottom;
        } else if (currDrawGlobalYPreferred > mNotWrapLimitYTop) {
            currDrawGlobalYPreferred = mNotWrapLimitYTop;
        }
        return currDrawGlobalYPreferred;
    }

    private boolean mFlagMayPress = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        if (mItemHeight == 0) {
            return true;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        float currY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mFlagMayPress = true;
                mHandlerInNewThread.removeMessages(HANDLER_WHAT_REFRESH);
                stopScrolling();
                downY = currY;
                downYGlobal = mCurrDrawGlobalY;

                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float spanY = downY - currY;

                if (!(mFlagMayPress && (-mScaledTouchSlop < spanY && spanY < mScaledTouchSlop))) {
                    mFlagMayPress = false;
                    mCurrDrawGlobalY = limitY((int) (downYGlobal + spanY));
                    calculateFirstItemParameterByGlobalY();
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
                if (mFlagMayPress) {
                    click(event);
                } else {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000);
                    int velocityY = (int) (velocityTracker.getYVelocity() * mFriction);
                    if (Math.abs(velocityY) > mMiniVelocityFling) {
                        mScroller.fling(0, mCurrDrawGlobalY, 0, -velocityY,
                                Integer.MIN_VALUE, Integer.MAX_VALUE, limitY(Integer.MIN_VALUE), limitY(Integer.MAX_VALUE));
                        invalidate();

                    }
                    mHandlerInNewThread.sendMessageDelayed(getMsg(), 0);
                    releaseVelocityTracker();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                downYGlobal = mCurrDrawGlobalY;
                stopScrolling();
                mHandlerInNewThread.sendMessageDelayed(getMsg(), 0);
                break;
        }
        return true;
    }

    private void click(MotionEvent event) {
        float y = event.getY();
        for (int i = 0; i < mShownCount; i++) {
            if (mItemHeight * i <= y && y < mItemHeight * (i + 1)) {
                clickItem(i);
                break;
            }
        }
    }

    private void clickItem(int showCountIndex) {
        if (0 <= showCountIndex && showCountIndex < mShownCount) {
            //clicked the showCountIndex of the view
            scrollByIndexSmoothly(showCountIndex - mShownCount / 2);
        }  //wrong

    }

    private float getTextCenterYOffset(Paint.FontMetrics fontMetrics) {
        if (fontMetrics == null) {
            return 0;
        }
        return Math.abs(fontMetrics.top + fontMetrics.bottom) / 2;
    }

    private int mViewWidth;
    private int mViewHeight;
    private int mItemHeight;
    private float dividerY0;
    private float dividerY1;
    private float dividerOffset;
    private float mViewCenterX;

    /**
     * 设置选中线的高度（相对VIEW中心）
     * @param offset 高度
     */
    public void setDividerPadding(float offset) {
        dividerOffset = offset;
    }

    //defaultPickedIndex relative to the shown part
    private void correctPositionByDefaultValue(int defaultPickedIndex) {
        mCurrDrawFirstItemIndex = defaultPickedIndex - (mShownCount - 1) / 2;
        mCurrDrawFirstItemIndex = getIndexByRawIndex(mCurrDrawFirstItemIndex, getOneRecycleSize());
        if (mItemHeight == 0) {
            mCurrentItemIndexEffect = true;
        } else {
            mCurrDrawGlobalY = mCurrDrawFirstItemIndex * mItemHeight;
            calculateFirstItemParameterByGlobalY();
        }
    }

    //first shown item's content index, corresponding to the Index of mDisplayedValued
    private int mCurrDrawFirstItemIndex = 0;
    //the first shown item's Y
    private int mCurrDrawFirstItemY = 0;
    //global Y corresponding to scroller
    private int mCurrDrawGlobalY = 0;

    @Override
    public void computeScroll() {
        if (mItemHeight == 0) {
            return;
        }
        if (mScroller.computeScrollOffset()) {
            mCurrDrawGlobalY = mScroller.getCurrY();
            calculateFirstItemParameterByGlobalY();
            postInvalidate();
        }
    }

    private void calculateFirstItemParameterByGlobalY() {
        mCurrDrawFirstItemIndex = (int) Math.floor((float) mCurrDrawGlobalY / mItemHeight);
        mCurrDrawFirstItemY = -(mCurrDrawGlobalY - mCurrDrawFirstItemIndex * mItemHeight);
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void updateMaxWHOfDisplayedValues(boolean needRequestLayout) {
        updateMaxWidthOfDisplayedValues();
        updateMaxHeightOfDisplayedValues();
        if (needRequestLayout &&
                (mSpecModeW == MeasureSpec.AT_MOST || mSpecModeH == MeasureSpec.AT_MOST)) {
            mHandlerInMainThread.sendEmptyMessage(HANDLER_WHAT_REQUEST_LAYOUT);
        }
    }

    private int mSpecModeW = MeasureSpec.UNSPECIFIED;
    private int mSpecModeH = MeasureSpec.UNSPECIFIED;

    private int measureWidth(int measureSpec) {
        int result;
        int specMode = mSpecModeW = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            int marginOfHint = Math.max(mWidthOfHintText, mWidthOfAlterHint) == 0 ? 0 : mMarginEndOfHint;
            int gapOfHint = Math.max(mWidthOfHintText, mWidthOfAlterHint) == 0 ? 0 : mMarginStartOfHint;

            int maxWidth = Math.max(mMaxWidthOfAlterArrayWithMeasureHint,
                    Math.max(mMaxWidthOfDisplayedValues, mMaxWidthOfAlterArrayWithoutMeasureHint)
                            + 2 * (gapOfHint + Math.max(mWidthOfHintText, mWidthOfAlterHint) + marginOfHint));
            result = this.getPaddingLeft() + this.getPaddingRight() + maxWidth;//MeasureSpec.UNSPECIFIED
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result;
        int specMode = mSpecModeH = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            int maxHeight = mShownCount * (mMaxHeightOfDisplayedValues);
            result = this.getPaddingTop() + this.getPaddingBottom() + maxHeight;//MeasureSpec.UNSPECIFIED
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawContent(canvas);
        drawLine(canvas);
        drawHint(canvas);
        //drawEdgeEffect(canvas);发现系统自带了设置边缘渐变，故删除
    }

//    private void drawEdgeEffect(Canvas canvas) {
//        Paint mPaint = new Paint();
//        mPaint.setAntiAlias(true);
//        mPaint.setStrokeWidth(3);
//        mPaint.setStyle(Paint.Style.FILL);
//        mPaint.setColor(Color.YELLOW);
//
//        LinearGradient top = new LinearGradient(0, 0, 0, 100, Color.WHITE, Color.TRANSPARENT, Shader.TileMode.CLAMP);
//        mPaint.setShader(top);
//        canvas.drawRect(0, 0, getWidth(), 400, mPaint);
//
//
//        Paint mp = new Paint();
//        mp.setAntiAlias(true);
//        mp.setStrokeWidth(3);
//        mp.setStyle(Paint.Style.FILL);
//        mp.setColor(Color.BLUE);
//
//        LinearGradient bottom = new LinearGradient(0, getHeight() - 100, 0, getHeight(), Color.TRANSPARENT, Color.WHITE, Shader.TileMode.CLAMP);
//        mp.setShader(bottom);
//        canvas.drawRect(0, getHeight() - 400, getWidth(), getHeight(), mp);
//    }


    private void drawContent(Canvas canvas) {
        int index;
        int textColor;
        float textSize;
        float fraction = 0f;// fraction of the item in state between normal and selected, in[0, 1]
        float textSizeCenterYOffset;

        for (int i = 0; i < mShownCount + 1; i++) {
            float y = mCurrDrawFirstItemY + mItemHeight * i;
            index = getIndexByRawIndex(mCurrDrawFirstItemIndex + i, getOneRecycleSize());
            if (i == mShownCount / 2) {//this will be picked
                fraction = (float) (mItemHeight + mCurrDrawFirstItemY) / mItemHeight;
                textColor = getEvaluateColor(fraction, mTextColorNormal, mTextColorSelected);
                textSize = getEvaluateSize(fraction, mTextSizeNormal, mTextSizeSelected);
                textSizeCenterYOffset = getEvaluateSize(fraction, mTextSizeNormalCenterYOffset,
                        mTextSizeSelectedCenterYOffset);
            } else if (i == mShownCount / 2 + 1) {
                textColor = getEvaluateColor(1 - fraction, mTextColorNormal, mTextColorSelected);
                textSize = getEvaluateSize(1 - fraction, mTextSizeNormal, mTextSizeSelected);
                textSizeCenterYOffset = getEvaluateSize(1 - fraction, mTextSizeNormalCenterYOffset,
                        mTextSizeSelectedCenterYOffset);
            } else {
                textColor = mTextColorNormal;
                textSize = mTextSizeNormal;
                textSizeCenterYOffset = mTextSizeNormalCenterYOffset;
            }
            mPaintText.setColor(textColor);
            mPaintText.setTextSize(textSize);
            int height = mItemHeight / 2;
            if (0 <= index && index < getOneRecycleSize()) {
                CharSequence str = mDisplayedValues[index + mMinShowIndex];

                canvas.drawText(str.toString(), mViewCenterX,
                        y + height + textSizeCenterYOffset, mPaintText);
            }
        }
    }


    private void drawLine(Canvas canvas) {
        //true to show the two dividers
        if (DEFAULT_SHOW_DIVIDER) {
            canvas.drawLine(getPaddingLeft() + mDividerMarginL,
                    dividerY0 + dividerOffset, mViewWidth - getPaddingRight() - mDividerMarginR, dividerY0 + dividerOffset, mPaintDivider);
            canvas.drawLine(getPaddingLeft() + mDividerMarginL,
                    dividerY1 - dividerOffset, mViewWidth - getPaddingRight() - mDividerMarginR, dividerY1 - dividerOffset, mPaintDivider);
        }
    }

    private void drawHint(Canvas canvas) {
        if (TextUtils.isEmpty(mHintText)) {
            return;
        }
        float offsetX = (mMaxWidthOfDisplayedValues + mWidthOfHintText) >> 1;
        canvas.drawText(mHintText,
                mViewCenterX + offsetX + mMarginStartOfHint,
                (dividerY0 + dividerY1) / 2 + mTextSizeHintCenterYOffset, mPaintHint);
    }

    private void updateMaxWidthOfDisplayedValues() {
        float savedTextSize = mPaintText.getTextSize();
        mPaintText.setTextSize(mTextSizeSelected);
        mMaxWidthOfDisplayedValues = getMaxWidthOfTextArray(mDisplayedValues, mPaintText);
        mMaxWidthOfAlterArrayWithMeasureHint = 0;
        mMaxWidthOfAlterArrayWithoutMeasureHint = 0;
        mPaintText.setTextSize(mTextSizeHint);
        mWidthOfAlterHint = 0;
        mPaintText.setTextSize(savedTextSize);
    }

    private int getMaxWidthOfTextArray(CharSequence[] array, Paint paint) {
        if (array == null) {
            return 0;
        }
        int maxWidth = 0;
        for (CharSequence item : array) {
            if (item != null) {
                int itemWidth = getTextWidth(item, paint);
                maxWidth = Math.max(itemWidth, maxWidth);
            }
        }
        return maxWidth;
    }

    private int getTextWidth(CharSequence text, Paint paint) {
        if (TextUtils.isEmpty(text)) {
            return 0;
        }
        String key = text.toString();

        if (mTextWidthCache.containsKey(key)) {
            Integer integer = mTextWidthCache.get(key);
            if (integer != null) {
                return integer;
            }
        }

        int value = (int) (paint.measureText(key) + 0.5f);
        mTextWidthCache.put(key, value);
        return value;
    }

    private void updateMaxHeightOfDisplayedValues() {
        float savedTextSize = mPaintText.getTextSize();
        mPaintText.setTextSize(mTextSizeSelected);
        mMaxHeightOfDisplayedValues = (int) (mPaintText.getFontMetrics().bottom - mPaintText.getFontMetrics().top + 0.5);
        mPaintText.setTextSize(savedTextSize);
    }


    private void updateContent(String[] newDisplayedValues) {
        mDisplayedValues = newDisplayedValues;
    }

    //used in setDisplayedValues
    private void updateValue() {
        inflateDisplayedValuesIfNull();
        mMinShowIndex = 0;
        mMaxShowIndex = mDisplayedValues.length - 1;
    }

    private void updateValueForInit() {
        inflateDisplayedValuesIfNull();
        if (mMinShowIndex == -1) {
            mMinShowIndex = 0;
        }
        if (mMaxShowIndex == -1) {
            mMaxShowIndex = mDisplayedValues.length - 1;
        }
        setMinAndMaxShowIndex(mMinShowIndex, mMaxShowIndex, false);
    }

    private void inflateDisplayedValuesIfNull() {
        if (mDisplayedValues == null) {
            mDisplayedValues = new String[1];
            mDisplayedValues[0] = "0";
        }
    }

    /**
     * 某些情况可能需要停止滑动，故提供这个方法
     */
    public void stopScrolling() {
        if (mScroller != null) {
            if (!mScroller.isFinished()) {
                mScroller.startScroll(0, mScroller.getCurrY(), 0, 0, 1);
                mScroller.abortAnimation();
                postInvalidate();
            }
        }
    }


    private Message getMsg() {
        return getMsg(NumberPickerView.HANDLER_WHAT_REFRESH, 0, 0, null);
    }

    private Message getMsg(int what, int arg1, int arg2, Object obj) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        msg.obj = obj;
        return msg;
    }

    //===下面是工具方法===//
    private boolean isStringEqual(String a, String b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
    }

    private int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private int dp2px(Context context, float dpValue) {
        final float densityScale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * densityScale + 0.5f);
    }

    private int getEvaluateColor(float fraction, int startColor, int endColor) {
        int a, r, g, b;
        int sA = (startColor & 0xff000000) >>> 24;
        int sR = (startColor & 0x00ff0000) >>> 16;
        int sG = (startColor & 0x0000ff00) >>> 8;
        int sB = (startColor & 0x000000ff);

        int eA = (endColor & 0xff000000) >>> 24;
        int eR = (endColor & 0x00ff0000) >>> 16;
        int eG = (endColor & 0x0000ff00) >>> 8;
        int eB = (endColor & 0x000000ff);

        a = (int) (sA + (eA - sA) * fraction);
        r = (int) (sR + (eR - sR) * fraction);
        g = (int) (sG + (eG - sG) * fraction);
        b = (int) (sB + (eB - sB) * fraction);

        return a << 24 | r << 16 | g << 8 | b;
    }

    private float getEvaluateSize(float fraction, float startSize, float endSize) {
        return startSize + (endSize - startSize) * fraction;
    }

}