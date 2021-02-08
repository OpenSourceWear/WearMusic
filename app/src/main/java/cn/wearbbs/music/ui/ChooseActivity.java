package cn.wearbbs.music.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.xtc.shareapi.share.communication.SendMessageToXTC;
import com.xtc.shareapi.share.interfaces.IXTCCallback;
import com.xtc.shareapi.share.manager.ShareMessageManager;
import com.xtc.shareapi.share.shareobject.XTCShareMessage;
import com.xtc.shareapi.share.shareobject.XTCTextObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.ChooseAdapter;

public class ChooseActivity extends SlideBackActivity {
    List arr;
    ChooseAdapter adapter;
    private static int counter = 0;
    String name;
    String type;
    String pic;
    File lrc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
        try{
            Intent intent = getIntent();
            name = intent.getStringExtra("song");
            type = intent.getStringExtra("type");
            pic = intent.getStringExtra("pic");
        }
        catch (Exception e){}
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
            while(true){//使用readLine方法，一次读一行
                String tmp_line = in.readLine();
                if(tmp_line!=null){
                    try {

                        tmp_line = tmp_line.replace("[" + MainActivity.getSubString(tmp_line, "[", "]") + "]", "");
                        arr.add(tmp_line);
                        System.out.println(tmp_line);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else{
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ListView lrcs = findViewById(R.id.lrcs);
        adapter = new ChooseAdapter(arr,this);
        lrcs.setAdapter(adapter);
        Toast.makeText(this,"点击标题栏分享歌词",Toast.LENGTH_SHORT).show();
    }
    public void share(View view){
        List choose = adapter.getChoose();
        String tmp = "";
        for (int i = 0;i<choose.size();i++){
            tmp += choose.get(i);
            tmp += "\n";
        }
        tmp += "——" + name;
        int temp = countStr(tmp,"\n");
        if((temp-1) > 20){
            Toast.makeText(this,"分享行数不能超过二十行",Toast.LENGTH_SHORT).show();
        }
        else{
            if (Build.MANUFACTURER.equals("XTC")) {
                //第一步：创建XTCTextObject对象，并设置text属性为要分享的文本内容
                XTCTextObject xtcTextObject = new XTCTextObject();
                xtcTextObject.setText(tmp);
                //第二步：创建XTCShareMessage对象，并将shareObject属性设置为xtcTextObject对象
                XTCShareMessage xtcShareMessage = new XTCShareMessage();
                xtcShareMessage.setShareObject(xtcTextObject);
                //第三步：创建SendMessageToXTC.Request对象，并设置
                SendMessageToXTC.Request request = new SendMessageToXTC.Request();
                request.setMessage(xtcShareMessage);
                //第四步：创建ShareMessageManagr对象，调用sendRequestToXTC方法，传入SendMessageToXTC.Request对象和AppKey
                ShareMessageManager SMA = new ShareMessageManager(this);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round, null);
                SMA.setAppIcon(bitmap);
                SMA.sendRequestToXTC(request, "");
            }
            else {
                Intent intent = new Intent(ChooseActivity.this, QRCodeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                intent.putExtra("type","1");
                intent.putExtra("ly",tmp);
                startActivity(intent);
                finish();
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

