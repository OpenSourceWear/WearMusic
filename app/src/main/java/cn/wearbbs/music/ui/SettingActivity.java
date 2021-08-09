package cn.wearbbs.music.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import cn.wearbbs.music.R;
import cn.wearbbs.music.util.UserInfoUtil;

/**
 * @author JackuXL
 */
public class SettingActivity extends AppCompatActivity {

    Switch switchFM;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        switchFM = findViewById(R.id.switch_fm);
        String needFM = UserInfoUtil.getUserInfo(this,"needFM");
        if("true".equals(needFM)){
            switchFM.setChecked(true);
        }
        switchFM.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                UserInfoUtil.saveUserInfo(SettingActivity.this,"needFM","true");
            }
            else{
                UserInfoUtil.saveUserInfo(SettingActivity.this,"needFM","false");
            }
        });
    }
    public void onClick(View view){
        Intent intent = new Intent(SettingActivity.this, AboutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

}