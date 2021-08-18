package cn.wearbbs.music.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import cn.wearbbs.music.fragment.PlayerFragment;

public class MusicPlayerService extends Service {
    private static final String TAG = "MusicPlayerService";
    private final MyBinder mBinder = new MyBinder();
    //标记当前歌曲的序号
    private int musicIndex = 0;
    //初始化MediaPlayer
    public MediaPlayer mMediaPlayer;
    private String[] musicArray;
    boolean prepareDone = false;
    MediaPlayer.OnCompletionListener backupListener;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mMediaPlayer != null) {
            mBinder.closePlayer();
        }
        mMediaPlayer = new MediaPlayer();
        this.musicArray = intent.getStringArrayExtra("musicArray");
        this.musicIndex = intent.getIntExtra("musicIndex", 0);
        mMediaPlayer.setOnPreparedListener(mp -> {
            prepareDone = true;
            mBinder.playMusic();
        });
        return super.onStartCommand(intent, flags, startId);
    }

    public class MyBinder extends Binder {
        public void initPlayer() {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(backupListener);
            mMediaPlayer.setOnPreparedListener(mp -> {
                prepareDone = true;
                playMusic();
            });
        }

        /**
         * 播放音乐
         */
        public void playMusic() {
            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();
            }

        }

        /**
         * 暂停播放
         */
        public void pauseMusic() {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }

        /**
         * 准备音频
         */
        public void initAudio() {
            //获取文件路径
            try {
                //设置音频文件到MediaPlayer对象中
                mMediaPlayer.setDataSource(musicArray[musicIndex].replace("http://", "https://"));
                //准备音乐
                Log.d(TAG, "initAudio: 开始准备");
                Log.d(TAG, "initAudio: URL " + musicArray[musicIndex]);
                Log.d(TAG, "initAudio: Index " + musicIndex);
                prepareDone = false;
                mMediaPlayer.prepareAsync();
                // 带线程缓冲会炸..不知道为什么
//                new Thread(()->{
//                    try {
//                        mMediaPlayer.prepare();
//                        prepareDone=true;
//                        Log.d(TAG, "initAudio: 准备完成");
//                    } catch (Exception e) {
//                        Log.d(TAG, "initAudio: 准备失败");
//                        Log.d(TAG, "initAudio: 失败链接"+musicArray[musicIndex]);
//                        e.printStackTrace();
//                    }
//                }).start();
            } catch (Exception e) {
                Log.d(TAG, "initAudio: 准备失败");
                Toast.makeText(getApplicationContext(),"准备音乐失败，若多次出现此问题，请尝试重新登录", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        /**
         * 上一首
         */
        public void preciousMusic() {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                closePlayer();
            }
            initPlayer();
            musicIndex--;
            if(musicIndex<0){
                musicIndex=0;
            }
            initAudio();
        }

        /**
         * 下一首
         */
        public void nextMusic() {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                closePlayer();
            }
            initPlayer();
            musicIndex++;
            if(musicIndex>=musicArray.length){
                musicIndex=0;
            }
            initAudio();
        }

        /**
         * 判断是否正在播放
         */
        public boolean isPlaying() {
            return mMediaPlayer.isPlaying();
        }

        /**
         * 设置音乐索引
         */
        public void setMusicIndex(int index) {
            musicIndex = index;
        }

        /**
         * 获取准备状态
         */
        public boolean getPrepareStatus() {
            return prepareDone;
        }

        /**
         * 关闭播放器
         */
        public void closePlayer() {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }

        public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
            mMediaPlayer.setOnCompletionListener(listener);
            backupListener = listener;
        }

        public int getCurrentPosition() {
            return mMediaPlayer.getCurrentPosition();
        }

        public int getDuration() {
            return mMediaPlayer.getDuration();
        }

        public void setPlayOrder(int orderId){
            switch (orderId){
                case PlayerFragment.PLAY_ORDER:
                    mMediaPlayer.setOnCompletionListener(backupListener);
                    break;
                case PlayerFragment.PLAY_REPEAT_ONE:
                    mMediaPlayer.setOnCompletionListener(mp -> {
                        mp.start();
                        mp.setLooping(true);
                    });
                    break;
                case PlayerFragment.PLAY_SHUFFLE:
                    //TODO:随机播放
                    break;
            }
        }

        public void seekTo(long time){
            if(mMediaPlayer!=null){
                mMediaPlayer.seekTo((int)time);
            }

        }
    }
}