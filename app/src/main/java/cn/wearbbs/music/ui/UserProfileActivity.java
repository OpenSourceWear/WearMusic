package cn.wearbbs.music.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;

import api.UserApi;
import cn.wearbbs.music.R;
import cn.wearbbs.music.util.SharedPreferencesUtil;

public class UserProfileActivity extends AppCompatActivity {
    private JSONObject profile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile);
        profile = JSON.parseObject(getIntent().getStringExtra("profile"));
        if(profile == null){
            profile = SharedPreferencesUtil.getJSONObject("profile");
        }
        else{
            findViewById(R.id.btn_logout).setVisibility(View.GONE);
        }
        init();
    }

    public void init(){
        ImageView iv_avatar = findViewById(R.id.iv_avatar);
        RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
        Glide.with(this).load(profile.getString("avatarUrl").replace("http://","https://")).apply(options).into(iv_avatar);
        iv_avatar.setOnClickListener(v -> startActivity(new Intent(this, ViewPictureActivity.class).putExtra("url", profile.getString("avatarUrl").replace("http://", "https://"))));

        TextView tv_name = findViewById(R.id.tv_name);
        tv_name.setText(profile.getString("nickname"));

        TextView tv_id = findViewById(R.id.tv_id);
        tv_id.setText(String.format("ID：%s",profile.getString("userId")));
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_title:
                finish();
                break;
            case R.id.btn_logout:
                // 退出登录
                new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("确定要退出登录吗")
                        .setPositiveButton("确定", (dialogInterface, i) -> {
                            UserApi api = new UserApi();
                            api.setCookie(SharedPreferencesUtil.getString("cookie", ""));
                            new Thread(() -> {
                                api.logout();
                                SharedPreferencesUtil.remove("cookie");
                                SharedPreferencesUtil.remove("profile");
                                setResult(RESULT_OK, new Intent().putExtra("logout",true));
                                finish();
                            }).start();
                        })
                        .setNegativeButton("手滑了", (dialogInterface, i) -> dialogInterface.dismiss())
                        .create().show();
                break;
            case R.id.iv_avatar:
                break;
        }

    }
}