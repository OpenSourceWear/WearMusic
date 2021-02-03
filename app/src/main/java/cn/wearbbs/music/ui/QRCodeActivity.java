package cn.wearbbs.music.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import cn.wearbbs.music.R;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import cn.wearbbs.music.util.QRCodeUtil;

public class QRCodeActivity extends SlideBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        if(type.equals("0")){
            String id = intent.getStringExtra("id");
            Bitmap temp = QRCodeUtil.createQRCodeBitmap("https://music.163.com/#/song?id=" + id,130,130);
            ImageView QRCode_image = findViewById(R.id.QRCode_image);
            QRCode_image.setImageBitmap(temp);
        }
        else{
            Toast.makeText(QRCodeActivity.this,"请耐心等待，若歌词较长，可能会加载失败",Toast.LENGTH_SHORT).show();
            String ly = intent.getStringExtra("ly");
            Bitmap temp = QRCodeUtil.createQRCodeBitmap(ly,130,130);
            ImageView QRCode_image = findViewById(R.id.QRCode_image);
            QRCode_image.setImageBitmap(temp);
        }
    }
}