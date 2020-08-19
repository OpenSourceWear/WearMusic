package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.xtc.shareapi.share.communication.SendMessageToXTC;
import com.xtc.shareapi.share.manager.ShareMessageManager;
import com.xtc.shareapi.share.shareobject.XTCShareMessage;
import com.xtc.shareapi.share.shareobject.XTCTextObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class choose_lrc extends AppCompatActivity {
    List arr;
    lrc_adapter adapter;
    private static int counter = 0;
    String name;
    String type;
    File lrc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_lrc);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "8903c5a2-a2a5-4244-a1c5-4373b001a565", Analytics.class, Crashes.class);
        }
        Intent intent = getIntent();
        name = intent.getStringExtra("song");
        type = intent.getStringExtra("type");
        try{
            if(!type.equals("1")){
                lrc = new File("/sdcard/Android/data/cn.wearbbs.music/temp/temp.lrc");
            }
            else{
                lrc = new File("/sdcard/Android/data/cn.wearbbs.music/download/lrc/" + name + ".lrc");
            }
        } catch (Exception e) {
            finish();
        }
        arr = new ArrayList();
        try {
            BufferedReader in = new BufferedReader(new FileReader(lrc));
            while((in.readLine())!=null){//使用readLine方法，一次读一行
                arr.add(in.readLine());
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ListView lrcs = findViewById(R.id.lrcs);
        adapter = new lrc_adapter(arr,this);
        lrcs.setAdapter(adapter);
        Toast.makeText(this,"点击标题栏分享歌词",Toast.LENGTH_SHORT).show();
    }
    public void share(View view){
        int temp = countStr(adapter.getChoose(),"\n");
        if((temp-1) > 10){
            Toast.makeText(this,"分享行数不能超过十行",Toast.LENGTH_SHORT).show();
        }
        else{
            if(android.os.Build.BRAND.equals("XTC")){
                //第一步：创建XTCTextObject对象，并设置text属性为要分享的文本内容
                XTCTextObject xtcTextObject = new XTCTextObject();
                xtcTextObject.setText(adapter.getChoose() + "——" + name);

                //第二步：创建XTCShareMessage对象，并将shareObject属性设置为xtcTextObject对象
                XTCShareMessage xtcShareMessage = new XTCShareMessage();
                xtcShareMessage.setShareObject(xtcTextObject);

                //第三步：创建SendMessageToXTC.Request对象，并设置
                SendMessageToXTC.Request request = new SendMessageToXTC.Request();
                request.setMessage(xtcShareMessage);

                //第四步：创建ShareMessageManagr对象，调用sendRequestToXTC方法，传入SendMessageToXTC.Request对象和AppKey
                new ShareMessageManager(this).sendRequestToXTC(request, "");
            }
            else{
                Intent intent = new Intent(choose_lrc.this, qrcode.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                intent.putExtra("type","1");
                intent.putExtra("ly",adapter.getChoose());
                startActivity(intent);
            }
        }
    }
    /**
     * 判断str1中包含str2的个数
     * @param str1
     * @param str2
     * @return counter
     */
    public static int countStr(String str1, String str2) {
        if (str1.indexOf(str2) == -1) {
            return 0;
        } else if (str1.indexOf(str2) != -1) {
            counter++;
            countStr(str1.substring(str1.indexOf(str2) +
                    str2.length()), str2);
            return counter;
        }
        return 0;
    }
}

