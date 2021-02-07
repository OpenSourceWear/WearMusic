package cn.wearbbs.music.ui;

import androidx.appcompat.app.AppCompatActivity;
import cn.wearbbs.music.R;
import me.panpf.sketch.SketchImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PicActivity extends SlideBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic);
        Intent getIntent = getIntent();
        String url = getIntent.getStringExtra("url");
        SketchImageView sketchImageView = (SketchImageView) findViewById(R.id.image_main);
        sketchImageView.displayImage(url);
        sketchImageView.setOnClickListener(v -> finish());
        sketchImageView.setZoomEnabled(true);
    }
}