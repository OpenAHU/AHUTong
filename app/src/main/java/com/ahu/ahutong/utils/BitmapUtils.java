package com.ahu.ahutong.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;

import com.ahu.ahutong.R;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BitmapUtils {
    /**
     *创建纯色bitmap
     * @param colorString 颜色
     * @return bitmap
     */
    public static Bitmap createColorBitmap(String colorString){
        Bitmap color = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(color);
        canvas.drawColor(Color.parseColor(colorString));
        return color;
    }
    public static Bitmap compressBitmap(Bitmap bitmap, float reqsW, float reqsH, boolean isAdjust) {
        if (bitmap == null || reqsW == 0 || reqsH == 0) {
            return bitmap;
        }
        if (bitmap.getWidth() > reqsW || bitmap.getHeight() > reqsH) {
            float scaleX = new BigDecimal(reqsW).divide(new BigDecimal(bitmap.getWidth()), 4, RoundingMode.DOWN).floatValue();
            float scaleY = new BigDecimal(reqsH).divide(new BigDecimal(bitmap.getHeight()), 4, RoundingMode.DOWN).floatValue();
            if (isAdjust) {
                scaleX = Math.min(scaleX, scaleY);
                scaleY = Math.min(scaleX, scaleY);
            }
            Matrix matrix = new Matrix();
            matrix.postScale(scaleX, scaleY);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }

    /**
     * 旋转
     * @param angle 度数
     * @param bitmap 图
     * @return 旋转后的图
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap)
    {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (resizedBitmap != bitmap && !bitmap.isRecycled()){
            bitmap.recycle();
            bitmap = null;
        }
        return resizedBitmap;
    }
}
