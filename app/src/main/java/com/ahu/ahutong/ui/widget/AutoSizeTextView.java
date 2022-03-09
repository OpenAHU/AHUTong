package com.ahu.ahutong.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * 如果这段代码能够正常工作，那么请记住作者是Simon。
 * 如果不能正常工作，那我也不知道是谁写的。
 */
public class AutoSizeTextView extends AppCompatTextView {

    public AutoSizeTextView(@NonNull Context context) {
        super(context);
    }

    public AutoSizeTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoSizeTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAutoSizeText(CharSequence text) {
        int mTextViewWidth = dip2px( 100);
        while (true) {
            float textWidth = getPaint().measureText((String) text);
            if (textWidth > mTextViewWidth) {
                int textSize = (int) getTextSize();
                textSize = textSize - 2;
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            } else {
                break;
            }
        }
        setText(text);
    }
    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
