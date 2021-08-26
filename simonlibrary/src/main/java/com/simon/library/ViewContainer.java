package com.simon.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class ViewContainer {
    protected View root;
    protected Context context;
    public void createView(ViewGroup viewGroup, Context context){
        this.context=context;
        root= LayoutInflater.from(this.context=context).inflate(resId(),viewGroup,false);
        onCreateView();
        viewGroup.addView(root);
    }
    public void destroy(){
        context=null;
        root=null;
        onDestroy();
    }
    protected abstract int resId();
    protected abstract void onCreateView();
    protected abstract void onDestroy();
    protected final <V extends View> V findViewById(int id) {
        return root.findViewById(id);
    }
}
