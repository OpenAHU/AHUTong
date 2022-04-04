package com.ahu.ahutong.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.ahu.ahutong.R;
import com.tencent.mmkv.MMKV;

/**
 * 如果这段代码能够正常工作，那么请记住作者是Simon。
 * 如果不能正常工作，那我也不知道是谁写的。
 */
public class WidgetDialog {
    private static final String Tag = "WidgetDialog";

    public static void showDialog(Context context) {
        if (MMKV.defaultMMKV().decodeBool(Tag)) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog alert = builder.setIcon(R.mipmap.ic_launcher)
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("不再提示", (dialog, which) -> {
                    MMKV.defaultMMKV().encode(Tag, true);
                    dialog.dismiss();
                })
                .setPositiveButton("确定", (dialog, which) -> dialog.dismiss())
                .setTitle("温馨提示")
                .setMessage("安大通客户端现已支持显示课表的桌面小部件。如需小部件稳定运行，请确保安大通具有后台运行权限。")
                .create();
        alert.show();
    }
}
