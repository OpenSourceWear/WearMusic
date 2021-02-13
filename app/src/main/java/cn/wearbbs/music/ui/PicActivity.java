package cn.wearbbs.music.ui;

import cn.wearbbs.music.R;
import me.panpf.sketch.SketchImageView;

import android.content.Intent;
import android.os.Bundle;

public class PicActivity extends SlideBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);
        Intent getIntent = getIntent();
        String url = getIntent.getStringExtra("url");
        SketchImageView sketchImageView = (SketchImageView) findViewById(R.id.iv_pic);
        sketchImageView.displayImage(url);
        sketchImageView.setOnClickListener(v -> finish());
        sketchImageView.setZoomEnabled(true);
    }
}