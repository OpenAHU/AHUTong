package com.ahu.ahutong.widget;

import android.content.Context;

import com.ahu.ahutong.R;
import com.ahu.ahutong.data.dao.AHUCache;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;


public class WidgetDialog {
    public static void showDialog(Context context) {
        if (!AHUCache.INSTANCE.isShowWidgetTip()) return;
        new MaterialAlertDialogBuilder(context).setIcon(R.mipmap.ic_launcher)
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("不再提示", (dialog, which) -> {
                    AHUCache.INSTANCE.ignoreWidgetTip();
                    dialog.dismiss();
                })
                .setPositiveButton("确定", (dialog, which) -> dialog.dismiss())
                .setTitle("温馨提示")
                .setMessage("安大通客户端现已支持显示课表的桌面小部件。如需小部件稳定运行，请确保安大通具有后台运行权限。")
                .show();
    }
}
