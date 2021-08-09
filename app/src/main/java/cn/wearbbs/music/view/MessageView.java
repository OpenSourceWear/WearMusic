package cn.wearbbs.music.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import cn.wearbbs.music.R;

public class MessageView extends LinearLayout {
    private final View mView;
    public final static int NO_LOGIN = 0,
            LOAD_FAILED = 1,
            NO_MUSIC = 2;
    @SuppressLint("InflateParams")
    public MessageView(Context context) {
        super(context);
        LayoutInflater mInflater = LayoutInflater.from(context);
        mView = mInflater.inflate(R.layout.view_message, null);
        addView(mView);
    }

    @SuppressLint("InflateParams")
    public MessageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = LayoutInflater.from(context);
        mView = mInflater.inflate(R.layout.view_message, null);
        addView(mView);
    }

    public void setImageResource(@DrawableRes int resId) {
        ImageView iv_img = findViewById(R.id.iv_img);
        iv_img.setImageResource(resId);
    }

    public void setText(@StringRes int resId) {
        TextView tv_img = findViewById(R.id.tv_img);
        tv_img.setText(resId);
    }

    public void setImageOnClickListener(OnClickListener listener){
        ImageView iv_img = findViewById(R.id.iv_img);
        iv_img.setOnClickListener(listener);
    }


    public void setContent(int contentId,@Nullable OnClickListener listener){
        switch (contentId){
            case NO_LOGIN:
                setText(R.string.msg_noLogin);
                setImageResource(R.drawable.ic_baseline_login_24);
                setImageOnClickListener(v -> {});
                break;
            case LOAD_FAILED:
                setText(R.string.load_failed);
                setImageResource(R.drawable.ic_baseline_refresh_24);
                setImageOnClickListener(listener);
                break;
            case NO_MUSIC:
                setText(R.string.msg_noMusic);
                setImageResource(R.drawable.ic_baseline_assignment_24);
                setImageOnClickListener(v -> {});
                break;
        }
    }


}
