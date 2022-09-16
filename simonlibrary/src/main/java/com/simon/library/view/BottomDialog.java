package com.simon.library.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.simon.library.DialogUtil;
import com.simon.library.R;

public abstract class BottomDialog extends DialogFragment {
    private static final String KEY_KEEP_ON_RESTARTED = "KEEP_ON_RESTARTED";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && !savedInstanceState.getBoolean(KEY_KEEP_ON_RESTARTED, true)) {
            dismiss();
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext(), getTheme());
        DialogUtil.setWith(dialog, WindowManager.LayoutParams.MATCH_PARENT);
        DialogUtil.setGravity(dialog, Gravity.BOTTOM);
        DialogUtil.setBackgroundDrawableResource(dialog, R.drawable.bg_bottom_dialog);
        DialogUtil.setAnimations(dialog, R.style.BottomDialogTransition);
        dialog.setCanceledOnTouchOutside(true);
        onInitDialog(dialog);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_KEEP_ON_RESTARTED, keepOnRestarted());
    }

    /**
     * 是否在 Fragment 被重启后保留对话框，默认为 true。
     * <p>
     * 如果你不需要对 Dialog 的状态进行保存，那么可以重写该方法并返回 false。
     */
    protected boolean keepOnRestarted() {
        return true;
    }

    protected abstract void onInitDialog(Dialog dialog);

    /**
     * 禁止返回键关闭
     */
    protected void cancelBackKey(Dialog dialog) {
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        });
    }
}
