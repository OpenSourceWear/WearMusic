package cn.wearbbs.music.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.view.View;
import cn.wearbbs.music.R;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

public class AboutActivity extends SlideBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
    }
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_title:
                startActivity(new Intent(AboutActivity.this, MenuActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
            case R.id.check_update:
                startActivity(new Intent(AboutActivity.this, UpdateActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
        }

    }
    public void cu(View view){
        startActivity(new Intent(AboutActivity.this, ContactActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
    }
}