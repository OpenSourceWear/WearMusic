package cn.wearbbs.music;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.xtc.shareapi.share.communication.SendMessageToXTC;
import com.xtc.shareapi.share.manager.ShareMessageManager;
import com.xtc.shareapi.share.shareobject.XTCAppExtendObject;
import com.xtc.shareapi.share.shareobject.XTCShareMessage;

import java.io.File;

public class console extends AppCompatActivity {
    String id;
    String name;
    String artists;
    String song;
    String url;
    Long mTaskId;
    String type;
    DownloadManager downloadManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        if(type.equals("0")){
            id = intent.getStringExtra("id");
            name = intent.getStringExtra("name");
            artists = intent.getStringExtra("artists");
            song = intent.getStringExtra("song");
            url = intent.getStringExtra("url");
            LinearLayout l2 = findViewById(R.id.l2);
            l2.setVisibility(View.VISIBLE);
        }
        else{
            LinearLayout l2 = findViewById(R.id.l2);
            l2.setVisibility(View.GONE);
        }
    }
    public void voice_up(View view){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(audioManager.STREAM_MUSIC);
        int value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(value == max){
            Toast.makeText(this,"媒体音量已到最高",Toast.LENGTH_SHORT).show();
        }
        else{
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,value + 1,0); //音乐音量
            Toast.makeText(this,Integer.toString(value + 1),Toast.LENGTH_SHORT).show();
        }
    }
    public void voice_down(View view){
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(value == 0){
            Toast.makeText(this,"媒体音量已到最低",Toast.LENGTH_SHORT).show();
        }
        else{
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,value - 1,0); //音乐音量
            Toast.makeText(this,Integer.toString(value - 1),Toast.LENGTH_SHORT).show();
        }
    }
    public void message(View view){
        Intent intent = new Intent(console.this, message.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        intent.putExtra("id",id);
        startActivity(intent);
    }
    public void share(View view){
        if(android.os.Build.BRAND.equals("XTC")){
            //第一步：创建XTCAppExtendObject对象
            XTCAppExtendObject xtcAppExtendObject = new XTCAppExtendObject();
            //设置点击分享的内容启动的页面
            xtcAppExtendObject.setStartActivity(MainActivity.class.getName());
            //设置分享的扩展信息，点击分享的内容会将该扩展信息带入跳转的页面
            xtcAppExtendObject.setExtInfo(id);
            //第二步：创建XTCShareMessage对象，并将shareObject属性设置为xtcTextObject对象
            XTCShareMessage xtcShareMessage = new XTCShareMessage();
            xtcShareMessage.setShareObject(xtcAppExtendObject);
            //设置图片
            xtcShareMessage.setThumbImage(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
            //设置文本
            xtcShareMessage.setDescription(name + "\n" + artists);
            //第三步：创建SendMessageToXTC.Request对象，并设置message属性为xtcShareMessage
            SendMessageToXTC.Request request = new SendMessageToXTC.Request();
            request.setMessage(xtcShareMessage);
//            request.setFlag(1);//设置跳转参数，设置为1为分享成功停留到微聊或者好友圈，设置为0或者不设置分享成功会返回原分享界面

            //第四步：创建ShareMessageManagr对象，调用sendRequestToXTC方法，传入SendMessageToXTC.Request对象和AppKey
            new ShareMessageManager(this).sendRequestToXTC(request, "");
        }
        else{
            Intent intent = new Intent(console.this, qrcode.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent.putExtra("type","0");
            intent.putExtra("id",id);
            startActivity(intent);
        }
    }
    public void lyrics(View view){
        Toast.makeText(this,"点击播放器标题即可进入歌词页面",Toast.LENGTH_SHORT).show();
    }
    public void download(View view){
        if(type.equals("0")){
            //创建下载任务,downloadUrl就是下载链接
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            //指定下载路径和下载文件名
            request.setDestinationInExternalPublicDir("/Android/data/cn.wearbbs.music/download/", song + ".mp3");
            //获取下载管理器
            downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            //将下载任务加入下载队列，否则不会进行下载
            mTaskId = downloadManager.enqueue(request);
            Toast.makeText(this,"已加入下载队列，请勿退出控制台",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this,"已下载",Toast.LENGTH_SHORT).show();
        }
    }
}

