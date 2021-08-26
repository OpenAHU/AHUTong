package com.simon.library.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.simon.library.R;

public class LoadingDialog {
    private Context context;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private LinearLayout root;

    public LoadingDialog(Context context) {
        builder = new AlertDialog.Builder(this.context = context);
        root= (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_loading,null,false);
    }

    public LoadingDialog setMessage(String str) {
       TextView tv= root.findViewById(R.id.tv);
       tv.setText(str);
        return this;
    }

    public LoadingDialog setMessage(int resID) {
        TextView tv= root.findViewById(R.id.tv);
        tv.setText(resID);
        return this;
    }

    public AlertDialog create() {
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.setContentView(root);
        return dialog;
    }

    public void dismiss() {
        dialog.dismiss();
        context=null;
        builder=null;
        dialog=null;
    }
}
