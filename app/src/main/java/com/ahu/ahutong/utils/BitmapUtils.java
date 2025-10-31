package com.ahu.ahutong.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BitmapUtils {
    /**
     * 创建纯色bitmap
     *
     * @param colorString 颜色
     * @return bitmap
     */
    public static Bitmap createColorBitmap(String colorString) {
        Bitmap color = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(color);
        canvas.drawColor(Color.parseColor(colorString));
        return color;
    }
}
