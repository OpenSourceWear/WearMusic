package cn.wearbbs.music.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import cn.wearbbs.music.R;
import cn.wearbbs.music.util.QRCodeUtil;

public class QRCodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        ImageView iv_qrcode = findViewById(R.id.iv_qrcode);
        iv_qrcode.setImageBitmap(QRCodeUtil.createQRCodeBitmap(getIntent().getStringExtra("url"), 512, 512));
    }

    public void onClick(View view) {
        finish();
    }
}