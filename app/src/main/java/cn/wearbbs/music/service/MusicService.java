package cn.wearbbs.music.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;

import cn.wearbbs.music.ui.MainActivity;

public class MusicService extends Service {

    public static MediaPlayer mMediaPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = MainActivity.getMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMediaPlayer = MainActivity.getMediaPlayer();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                startPlaySong();
//            }
//        }).start();
        return START_STICKY;
    }

    //开始、暂停播放
    public static void startPlaySong() {
        mMediaPlayer = MainActivity.getMediaPlayer();
        mMediaPlayer.start();
    }

    public static Boolean isPlaying(){
        mMediaPlayer = MainActivity.getMediaPlayer();
        return mMediaPlayer.isPlaying();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlaySong();
    }

    //停止播放销毁对象
    public static void stopPlaySong() {
        if(mMediaPlayer!=null){
            mMediaPlayer.pause();
        }
        else{
            mMediaPlayer = MainActivity.getMediaPlayer();
        }
    }
    public static void seek(int time){
        mMediaPlayer.seekTo(time);
    }
}