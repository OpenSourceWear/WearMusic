package cn.wearbbs.music.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import cn.wearbbs.music.R;

public class ContactActivity extends SlideBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
    }
    public void feed_back(View view){
        startActivity(new Intent(ContactActivity.this, FeedBackActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }
}