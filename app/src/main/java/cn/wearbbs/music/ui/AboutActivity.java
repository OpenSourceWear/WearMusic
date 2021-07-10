package cn.wearbbs.music.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import cn.wearbbs.music.R;

/**
 * @author JackuXL
 */
public class AboutActivity extends SlideBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
    public void onClick(View view){
        startActivity(new Intent(AboutActivity.this, UpdateActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }
    public void cu(View view){
        startActivity(new Intent(AboutActivity.this, ContactActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }
}