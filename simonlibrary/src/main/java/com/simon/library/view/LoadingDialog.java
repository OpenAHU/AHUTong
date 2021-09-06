package com.simon.library.view;


import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.simon.library.R;

public class LoadingDialog {
    private Context context;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private final LinearLayout root;

    public LoadingDialog(Context context) {
        builder = new AlertDialog.Builder(this.context = context);
        root = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
    }

    public LoadingDialog setMessage(String str) {
        TextView tv = root.findViewById(R.id.tv);
        tv.setText(str);
        return this;
    }

    public LoadingDialog setMessage(int resID) {
        TextView tv = root.findViewById(R.id.tv);
        tv.setText(resID);
        return this;
    }

    public AlertDialog create() {
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.setContentView(root);
        Window window = dialog.getWindow();

        float width =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 250, Resources.getSystem().getDisplayMetrics());


        if (window != null) {
            window.setLayout((int) width, -2);
        }

        return dialog;
    }

    public void dismiss() {
        dialog.dismiss();
        context = null;
        builder = null;
        dialog = null;
    }
}
