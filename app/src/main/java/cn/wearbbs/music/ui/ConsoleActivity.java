package cn.wearbbs.music.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.xtc.shareapi.share.communication.SendMessageToXTC;
import com.xtc.shareapi.share.manager.ShareMessageManager;
import com.xtc.shareapi.share.shareobject.XTCAppExtendObject;
import com.xtc.shareapi.share.shareobject.XTCShareMessage;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;

import api.MVApi;
import api.MusicApi;
import api.MusicListApi;
import cn.wearbbs.music.R;
import cn.wearbbs.music.util.DownloadUtil;
import cn.wearbbs.music.util.SharedPreferencesUtil;

public class ConsoleActivity extends AppCompatActivity {
    private AudioManager audioManager;
    private int max,musicIndex;
    private Toast toast;
    private JSONArray data;
    private Boolean liked = false,local, repeatOne;
    private String cookie;
    private JSONObject currentMusicInfo;
    private ProgressBar pb_main;
    private MusicApi musicApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_console);

        // 初始化
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        data = JSON.parseArray(getIntent().getStringExtra("data"));
        local  = getIntent().getBooleanExtra("local",false);
        repeatOne = getIntent().getBooleanExtra("repeatOne",false);
        musicApi = new MusicApi(cookie);

        if(repeatOne){
            ImageView iv_repeat = findViewById(R.id.iv_repeat);
            iv_repeat.setImageResource(R.drawable.icon_repeat_one);
        }
        musicIndex = getIntent().getIntExtra("musicIndex",0);
        cookie = SharedPreferencesUtil.getString("cookie", "", this);
        currentMusicInfo = data.getJSONObject(musicIndex);
        if(currentMusicInfo.containsKey("simpleSong")){
            currentMusicInfo = currentMusicInfo.getJSONObject("simpleSong");
        }
        if(currentMusicInfo.containsKey("artists")){
            if(local){
                artistName = currentMusicInfo.getString("artists");
            }
            else{
                artistName = currentMusicInfo.getJSONArray("artists").getJSONObject(0).getString("name");
            }
        }
        else{
            artistName = currentMusicInfo.getJSONArray("ar").getJSONObject(0).getString("name");
        }

        pb_main = findViewById(R.id.pb_main);

        new Thread(()->{
            try {
                MusicListApi api = new MusicListApi(SharedPreferencesUtil.getJSONObject("profile", this).getString("userId"), cookie);
                String[] ids = api.getMusicListDetail(api.getMusicList().getJSONObject(0).getString("id"));
                for (String id : ids) {
                    if (id.equals(currentMusicInfo.getString("id"))) {
                        liked = true;
                    }
                }
                ImageView like_view = findViewById(R.id.iv_like);
                if(liked){
                    like_view.setImageResource(R.drawable.ic_baseline_favorite_24);
                }
                else{
                    like_view.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        if(toast!=null){
            toast.cancel();
        }
        switch (view.getId()) {
            case R.id.main_title:
                finish();
                break;
            case R.id.iv_voiceUp:
                pb_main.setMax(max);
                int value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if(value == max){
                    toast = Toast.makeText(this,"媒体音量已到最高",Toast.LENGTH_SHORT);
                }
                else{
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value + 1,0); //音乐音量
                    pb_main.setProgress(value + 1);
                    new Handler().postDelayed(() -> pb_main.setProgress(0), 3000);
                }
                break;
            case R.id.iv_voiceDown:
                pb_main.setMax(max);
                value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                if(value == 0){
                    toast = Toast.makeText(this,"媒体音量已到最低",Toast.LENGTH_SHORT);
                }
                else{
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value - 1,0); //音乐音量
                    pb_main.setProgress(value - 1);
                    new Handler().postDelayed(() -> pb_main.setProgress(0), 3000);
                }
                break;
            case R.id.iv_like:
                ImageView like_view = findViewById(R.id.iv_like);
                new Thread(()->{
                    Looper.prepare();
                    if(liked){
                        if(musicApi.likeMusic(currentMusicInfo.getString("id"),false)){
                            toast = Toast.makeText(this,"取消收藏成功",Toast.LENGTH_SHORT);
                            liked = false;
                            like_view.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                        }
                        else{
                            toast = Toast.makeText(this,"取消收藏失败",Toast.LENGTH_SHORT);
                        }
                    }
                    else{
                        if(musicApi.likeMusic(currentMusicInfo.getString("id"),true)){
                            toast = Toast.makeText(this,"收藏成功",Toast.LENGTH_SHORT);
                            liked = true;
                            like_view.setImageResource(R.drawable.ic_baseline_favorite_24);
                        }
                        else{
                            toast = Toast.makeText(this,"收藏失败",Toast.LENGTH_SHORT);
                        }
                    }
                    toast.show();
                    Looper.loop();
                }).start();
                break;
            case R.id.iv_download:
                if(local){
                    if(toast!=null){
                        toast.cancel();
                    }
                    toast = Toast.makeText(this,"已下载",Toast.LENGTH_SHORT);
                    toast.show();
                }
                else{
                    checkPermissionForDownload();
                }
                break;
            case R.id.iv_comment:
                if(currentMusicInfo.getString("id")==null){
                    if(toast!=null){
                        toast.cancel();
                    }
                    toast = Toast.makeText(this,"本地音乐暂不支持评论",Toast.LENGTH_SHORT);
                    toast.show();
                }
                else{
                    startActivity(new Intent(ConsoleActivity.this,CommentActivity.class).putExtra("id",currentMusicInfo.getString("id")));
                }
                break;
            case R.id.iv_mv:
                if(local){
                    if(toast!=null){
                        toast.cancel();
                    }
                    toast = Toast.makeText(this,"本地音乐暂不支持播放MV",Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                }
                String mvId;
                mvId = currentMusicInfo.containsKey("mv")?currentMusicInfo.getString("mv"):currentMusicInfo.getString("mvid");
                if(mvId==null||mvId.isEmpty()){
                    if(toast!=null){
                        toast.cancel();
                    }
                    toast = Toast.makeText(this,"当前音乐无对应MV",Toast.LENGTH_SHORT);
                    toast.show();
                }
                else{
                    new Thread(()->{
                        String mvUrl = new MVApi(cookie).getMVUrl(mvId);
                        if (mvUrl == null) {
                            Looper.prepare();
                            if(toast!=null){
                                toast.cancel();
                            }
                            toast = Toast.makeText(this,"当前音乐无对应MV",Toast.LENGTH_SHORT);
                            toast.show();
                            Looper.loop();
                        }
                        else{
                            if(SharedPreferencesUtil.getString("video_player","WristVideo",this).equals("WristButlerPro")){
                                Intent intent = new Intent();
                                intent.putExtra("url", mvUrl);
                                intent.putExtra("title", currentMusicInfo.getString("name"));
                                try {
                                    intent.setClassName("com.cn.awg.pro", "com.cn.awg.pro.g2");
                                    startActivity(intent);
                                }
                                catch(Exception e) {
                                    if(toast!=null){
                                        toast.cancel();
                                    }
                                    toast = Toast.makeText(this, "你没有安装配套视频软件：腕管Pro，请先前往应用商店下载！", Toast.LENGTH_LONG);
                                    toast.show();
                                    e.printStackTrace();
                                }
                            }
                            else{
                                Intent intent = new Intent();
                                intent.putExtra("mode", 1);
                                intent.putExtra("url", mvUrl);
                                intent.putExtra("url_backup", mvUrl);
                                intent.putExtra("title", currentMusicInfo.getString("name"));
                                intent.putExtra("identity_name", "WearMusic");
                                try {
                                    intent.setComponent(new ComponentName("cn.luern0313.wristvideoplayer", "cn.luern0313.wristvideoplayer.ui.PlayerActivity"));
                                    startActivity(intent);
                                }
                                catch(Exception e) {
                                    e.printStackTrace();
                                    try {

                                        intent.setComponent(new ComponentName("cn.luern0313.wristvideoplayer_free", "cn.luern0313.wristvideoplayer_free.ui.PlayerActivity"));
                                        startActivity(intent);
                                    }
                                    catch(Exception ee)
                                    {
                                        if(toast!=null){
                                            toast.cancel();
                                        }
                                        toast = Toast.makeText(this, "你没有安装配套视频软件：腕上视频，请先前往应用商店下载！", Toast.LENGTH_LONG);
                                        toast.show();
                                        ee.printStackTrace();
                                    }
                                }
                            }
                        }
                    }).start();
                }
                break;
            case R.id.iv_share:
                if ("XTC".equals(Build.MANUFACTURER)) {
                    //第一步：创建XTCAppExtendObject对象
                    XTCAppExtendObject xtcAppExtendObject = new XTCAppExtendObject();
                    //设置点击分享的内容启动的页面
                    xtcAppExtendObject.setStartActivity(MainActivity.class.getName());
                    //设置分享的扩展信息，点击分享的内容会将该扩展信息带入跳转的页面
                    xtcAppExtendObject.setExtInfo(currentMusicInfo.getString("id"));
                    //第二步: 音乐封面转BitMap
                    new Thread(()->{
                        Bitmap bitmap = null;
                        try {
                            String albumId;
                            if(currentMusicInfo.containsKey("al")){
                                albumId = currentMusicInfo.getJSONArray("al").getJSONObject(0).getString("id");
                            }
                            else{
                                albumId = currentMusicInfo.getJSONArray("album").getJSONObject(0).getString("id");
                            }
                            bitmap = Glide.with(ConsoleActivity.this)
                                    .asBitmap()
                                    .load(musicApi.getMusicCover(albumId))
                                    .submit(512, 512).get();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //第三步：创建XTCShareMessage对象，并将shareObject属性设置为xtcTextObject对象
                        XTCShareMessage xtcShareMessage = new XTCShareMessage();
                        xtcShareMessage.setShareObject(xtcAppExtendObject);
                        //设置图片
                        xtcShareMessage.setThumbImage(bitmap);
                        //设置文本
                        xtcShareMessage.setDescription(currentMusicInfo + "\n" + artistName);
                        //第四步：创建SendMessageToXTC.Request对象，并设置message属性为xtcShareMessage
                        SendMessageToXTC.Request request = new SendMessageToXTC.Request();
                        request.setMessage(xtcShareMessage);
                        //第五步：创建ShareMessageManager对象，调用sendRequestToXTC方法，传入SendMessageToXTC.Request对象和AppKey
                        ShareMessageManager SMM = new ShareMessageManager(this);
                        SMM.sendRequestToXTC(request, "");
                    }).start();
                }
                else{
                    Intent intent = new Intent(ConsoleActivity.this, QRCodeActivity.class);
                    intent.putExtra("url","https://music.163.com/#/song?id="+currentMusicInfo.getString("id"));
                    startActivity(intent);
                }
                break;
            case R.id.iv_repeat:
                Intent intent = new Intent();
                ImageView iv_repeat = findViewById(R.id.iv_repeat);
                if(repeatOne){
                    // 处于单曲循环模式
                    intent.putExtra("repeatOne",false);
                    iv_repeat.setImageResource(R.drawable.icon_repeat);
                }
                else{
                    // 处于顺序播放模式
                    intent.putExtra("repeatOne",true);
                    iv_repeat.setImageResource(R.drawable.icon_repeat_one);
                }
                setResult(RESULT_OK,intent);
                break;
            case R.id.iv_playlist:
                startActivity(new Intent(ConsoleActivity.this,PlayListActivity.class)
                        .putExtra("local",local)
                        .putExtra("data",data.toJSONString()));
                break;
        }

    }

    public void checkPermissionForDownload(){
        // 读取权限
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        // 检查权限是否已授权
        int hasPermission = checkSelfPermission(permission);
        // 如果没有授权
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            requestPermissions(new String[]{permission}, 0);
        }else {
            // 已授权权限
            download();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0) {//grantResults 数组中存放的是授权结果
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 同意授权
                download();
            }else {
                // 拒绝授权
                toast = Toast.makeText(this,getString(R.string.permission_denied),Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
    String artistName;
    public void download(){
        if(toast!=null){
            toast.cancel();
        }
        toast = Toast.makeText(this,"开始下载，请不要退出界面",Toast.LENGTH_SHORT);
        toast.show();
        String fileName = currentMusicInfo.getString("name") + "--" + (artistName == null ? getString(R.string.unknown) : artistName);
        String rootPath = getExternalFilesDir(null) + "/download";
        new Thread(()->{
            // 保存封面
            String albumId;
            if(currentMusicInfo.containsKey("al")){
                albumId = currentMusicInfo.getJSONObject("al").getString("id");
            }
            else{
                albumId = currentMusicInfo.getJSONObject("album").getString("id");
            }
            savePicture(musicApi.getMusicCover(albumId),rootPath+"/cover/",fileName+".png");

            // 保存歌词
            File lrcDir = new File(rootPath+"/lrc/");
            if(!lrcDir.exists()){
                lrcDir.mkdirs();
            }
            try{
                BufferedWriter out = new BufferedWriter(new FileWriter(rootPath+"/lrc/"+fileName+".lrc"));
                out.write(musicApi.getMusicLyric(currentMusicInfo.getString("id")));
                out.close();
            }
            catch (Exception ignored){}

            // 保存id
            File idDir = new File(rootPath+"/id/");
            if(!idDir.exists()){
                idDir.mkdirs();
            }
            try{
                BufferedWriter out = new BufferedWriter(new FileWriter(rootPath+"/id/"+fileName+".txt"));
                out.write(currentMusicInfo.getString("id"));
                out.close();
            }
            catch (Exception ignored){}

            // 保存音乐
            String url = new MusicApi(cookie).getMusicUrl(currentMusicInfo.getString("id")).replace("http://","https://");
            pb_main.setMax(100);
            new DownloadUtil().download(
                    url, rootPath+"/music",
                     fileName + ".wav",
                    new DownloadUtil.OnDownloadListener() {
                        @Override
                        public void onDownloadSuccess(File file) {
                            pb_main.setProgress(0);
                            Looper.prepare();
                            toast.cancel();
                            toast = Toast.makeText(ConsoleActivity.this,"下载成功",Toast.LENGTH_SHORT);
                            toast.show();
                            Looper.loop();
                        }

                        @Override
                        public void onDownloading(int progress) {
                            Log.d("ConsoleActivity", "onDownloading: Progress "+progress+"%");
                            pb_main.setProgress(progress);
                        }

                        @Override
                        public void onDownloadFailed(Exception e) {
                            pb_main.setProgress(0);
                            Looper.prepare();
                            toast.cancel();
                            toast = Toast.makeText(ConsoleActivity.this,"下载失败",Toast.LENGTH_SHORT);
                            toast.show();
                            Looper.loop();
                        }
                    }
            );

        }).start();

    }

    public void savePicture(String photoUrl,String path,String fileName) {
        new Thread(()->{
            try {
                Bitmap bitmap = Glide.with(ConsoleActivity.this)
                        .asBitmap()
                        .load(photoUrl)
                        .submit(512, 512).get();
                File dirFile = new File(path);
                if (!dirFile.exists()) {
                    dirFile.mkdir();
                }

                File myCaptureFile = new File(path + fileName);
                myCaptureFile.createNewFile();
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, bos);
                bos.flush();
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}