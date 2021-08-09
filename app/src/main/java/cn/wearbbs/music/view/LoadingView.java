package cn.wearbbs.music.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import cn.wearbbs.music.R;

public class LoadingView extends LinearLayout {
    View mView;

    @SuppressLint("InflateParams")
    public LoadingView(Context context) {
        super(context);
        LayoutInflater mInflater = LayoutInflater.from(context);
        mView = mInflater.inflate(R.layout.view_loading, null);
        addView(mView);
    }

    @SuppressLint("InflateParams")
    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = LayoutInflater.from(context);
        mView = mInflater.inflate(R.layout.view_loading, null);
        addView(mView);
    }

}
