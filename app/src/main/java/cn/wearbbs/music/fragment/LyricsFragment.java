package cn.wearbbs.music.fragment;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSONArray;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import cn.jackuxl.api.SongApi;
import cn.wearbbs.music.R;
import cn.wearbbs.music.ui.MainActivity;
import cn.wearbbs.music.util.SharedPreferencesUtil;
import me.wcy.lrcview.LrcView;

public class LyricsFragment extends Fragment {
    private JSONArray data;
    private static int musicIndex;
    private int currentPosition;
    private LrcView lrcView;
    private boolean local;
    public static LyricsFragment newInstance(Intent intent) {
        LyricsFragment fragment = new LyricsFragment();
        Bundle args = new Bundle();
        args.putInt("musicIndex", intent.getIntExtra("musicIndex",0));
        args.putBoolean("local",intent.getBooleanExtra("local",false));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lyrics, container, false);
        data = PlayerFragment.getData();
        if (getArguments() != null && data!=null) {
            musicIndex = getArguments().getInt("musicIndex");
            local = getArguments().getBoolean("local");
            lrcView = view.findViewById(R.id.lv_main);
            lrcView.setDraggable(true, (lrcView, time) -> {
                PlayerFragment.seekTo(time);
                return true;
            });
            updateLyric();
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    //间隔时间
                    handler.sendEmptyMessageDelayed(1, 1000);
                }
            }.start();
            if(!SharedPreferencesUtil.getBoolean("finishConsoleTip",false)){
                view.findViewById(R.id.ll_console_tip).setVisibility(View.VISIBLE);
                view.findViewById(R.id.ll_console_tip).setOnClickListener(v -> {
                    requireActivity().findViewById(R.id.ll_tip).setVisibility(View.VISIBLE);
                    requireActivity().findViewById(R.id.tv_tip_background).setVisibility(View.VISIBLE);
                    MainActivity.setOnTipHideListener(() -> {
                        SharedPreferencesUtil.putBoolean("finishConsoleTip",true);
                        view.findViewById(R.id.ll_console_tip).setVisibility(View.GONE);
                    });
                });
            }
        }
        return view;
    }

    class CurrentPositionBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentPosition = intent.getIntExtra("currentPosition", 0);
        }
    }

    CurrentPositionBroadcast currentPositionBroadcast;

    @Override
    public void onAttach(@NotNull Context context) {
        currentPositionBroadcast = new CurrentPositionBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("cn.wearbbs.music.player.position");
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(currentPositionBroadcast, intentFilter);
        super.onAttach(context);
    }

    public void updateLyric(){
        new Thread(() -> {
            if(local){
                String lrcFile = data.getJSONObject(musicIndex).getString("lrcFile");
                StringBuilder lyric = new StringBuilder();

                if(lrcFile!=null){
                    try {
                        BufferedReader in = new BufferedReader(new FileReader(lrcFile));
                        String str;
                        while ((str = in.readLine()) != null) {
                            lyric.append(str);
                            lyric.append("\n");
                        }
                    } catch (IOException ignored) { }

                    if(lyric.length()==0){
                        lrcView.loadLrc("[00:00.00]无歌词");
                    }
                    else{
                        lrcView.loadLrc(lyric.toString());
                    }
                }
                else{
                    lrcView.loadLrc("[00:00.00]无歌词");
                }
            }
            else{
                SongApi api = new SongApi(SharedPreferencesUtil.getString("cookie", ""));
                try {
                    String lyric;
                    if (data.getJSONObject(0).containsKey("simpleSong")) {
                        lyric = api.getMusicLyric(data.getJSONObject(musicIndex).getJSONObject("simpleSong").getString("id"));
                    }
                    else{
                        lyric = api.getMusicLyric(data.getJSONObject(musicIndex).getString("id"));
                    }
                    if(lyric==null||lyric.isEmpty()){
                        lrcView.loadLrc("[00:00.00]无歌词");
                    }
                    else{
                        lrcView.loadLrc(lyric);
                    }
                } catch (Exception ignored) {
                    lrcView.loadLrc("[00:00.00]无歌词");
                }
            }

        }).start();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(PlayerFragment.getMusicIndex()!=musicIndex){
            musicIndex = PlayerFragment.getMusicIndex();
            lrcView.loadLrc("[00:00.00]加载中");
            updateLyric();
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                PlayerFragment.setPlayOrder(data.getIntExtra("orderId",PlayerFragment.PLAY_ORDER));
            }
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler=new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            if (currentPosition > 0) {
                //lrcView.updateTime(currentPosition);
            }
            //调取子线程
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    };


}