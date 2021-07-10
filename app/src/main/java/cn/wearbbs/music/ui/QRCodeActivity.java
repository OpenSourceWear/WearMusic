package cn.wearbbs.music.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import cn.wearbbs.music.R;
import cn.wearbbs.music.util.QRCodeUtil;

public class QRCodeActivity extends SlideBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        if(type.equals("0")){
            String id = intent.getStringExtra("id");
            Bitmap temp = QRCodeUtil.createQRCodeBitmap("https://music.163.com/#/song?id=" + id,130,130);
            ImageView QRCode_image = findViewById(R.id.iv_qrcode);
            QRCode_image.setImageBitmap(temp);
        }
        else{
            String ly = intent.getStringExtra("ly");
            Bitmap temp = QRCodeUtil.createQRCodeBitmap(ly,130,130);
            ImageView QRCode_image = findViewById(R.id.iv_qrcode);
            QRCode_image.setImageBitmap(temp);
        }
    }
}