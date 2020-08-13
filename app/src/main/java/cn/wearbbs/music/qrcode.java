package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

public class qrcode extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        if(type.equals("0")){
            String id = intent.getStringExtra("id");
            Bitmap temp = QRCodeUtil.createQRCodeBitmap("https://music.163.com/#/song?id=" + id,130,130);
            ImageView QRCode_image = findViewById(R.id.QRCode_image);
            QRCode_image.setImageBitmap(temp);
        }
        else{
            Toast.makeText(qrcode.this,"请耐心等待，若歌词较长，可能会加载失败",Toast.LENGTH_SHORT).show();
            String ly = intent.getStringExtra("ly");
            Bitmap temp = QRCodeUtil.createQRCodeBitmap(ly,130,130);
            ImageView QRCode_image = findViewById(R.id.QRCode_image);
            QRCode_image.setImageBitmap(temp);
        }
    }
}