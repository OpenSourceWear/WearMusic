package cn.wearbbs.music.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Flow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.HotApi;
import cn.wearbbs.music.api.MVApi;
import cn.wearbbs.music.api.MusicApi;
import cn.wearbbs.music.api.SearchApi;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends SlideBackActivity {
    List arr;
    String temp_hl;
    List tmp;
    String cookie;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
        LinearLayout list_layout = findViewById(R.id.list_layout);
        LinearLayout null_layout = findViewById(R.id.null_layout);
        LinearLayout zs = findViewById(R.id.zs);
        LinearLayout hot_layout = findViewById(R.id.hot_layout);
        list_layout.setVisibility(View.GONE);
        null_layout.setVisibility(View.GONE);
        hot_layout.setVisibility(View.VISIBLE);
        zs.setVisibility(View.GONE);
        findViewById(R.id.loading_layout).setVisibility(View.GONE);
        String temp = "[]";
        arr = JSON.parseArray(temp);
        File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(saver));
            cookie = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView tv = new TextView(this);
        tv.setText("没有更多了\n\n");
        tv.setTextColor(Color.parseColor("#999999"));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(12);
        ((ListView)findViewById(R.id.list)).addFooterView(tv,null,false);
        TagFlowLayout search_page_flowlayout = findViewById(R.id.id_flowlayout);
        final LayoutInflater mInflater = LayoutInflater.from(SearchActivity.this);
        Map map = null;
        try {
            map = new HotApi().getHotSearch();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String[] hot_list = new String[10];
        Map result = (Map)JSON.parse(map.get("result").toString());
        List hots = JSON.parseArray(result.get("hots").toString());
        for (int i = 0; i<10;i++){
            hot_list[i] = ((Map)JSON.parse(hots.get(i).toString())).get("first").toString();
        }
        search_page_flowlayout.setAdapter(new TagAdapter<String>(hot_list)
            {
                @Override
                public View getView(FlowLayout parent, int position, String s)
                {
                    TextView tv = (TextView) mInflater.inflate(R.layout.item_hot,
                            search_page_flowlayout, false);
                    tv.setText(s);
                    return tv;
                }
            });
        search_page_flowlayout.setOnTagClickListener((view, position, parent) -> {
            EditText editText = findViewById(R.id.editText);
            editText.setText(hot_list[position]);
            search(null);
            return true;
        });
    }

    public void menu(View view) {
        Intent intent = new Intent(SearchActivity.this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
    }

    public void search(View view) {
        LinearLayout list_layout = findViewById(R.id.list_layout);
        LinearLayout null_layout = findViewById(R.id.null_layout);
        LinearLayout hot_layout = findViewById(R.id.hot_layout);
        LinearLayout zs = findViewById(R.id.zs);
        list_layout.setVisibility(View.VISIBLE);
        null_layout.setVisibility(View.GONE);
        hot_layout.setVisibility(View.GONE);
        zs.setVisibility(View.GONE);
        findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
        EditText editText = findViewById(R.id.editText);
        try {
            if(editText.getText().toString().equals("自杀")){
                zs.setVisibility(View.VISIBLE);
            }
            else{
                Map maps = (Map) new SearchApi().Search(editText.getText().toString(),cookie);
                Map result = (Map) JSON.parse(maps.get("result").toString());
                tmp = JSONObject.parseArray(result.get("songs").toString());
                String song_name;
                try {
                    song_name = result.get("songs").toString();
                    List songsList = JSONObject.parseArray(song_name);
                    List<Map<String, Object>> idItems = new ArrayList<Map<String, Object>>();
                    for (int i = 0; i < songsList.size(); i++) {
                        Map<String, Object> idItem = new HashMap<String, Object>();
                        Map nm = (Map) JSON.parse(songsList.get(i).toString());
                        List ar_temp = JSON.parseArray(nm.get("artists").toString());
                        Map ar = (Map) JSON.parse(ar_temp.get(0).toString());
                        idItem.put("name", nm.get("name"));
                        idItem.put("id", nm.get("id"));
                        Map album = (Map) nm.get("album");
                        idItem.put("albumId", album.get("id"));
                        idItem.put("artists", ar.get("name"));
                        idItems.add(idItem);
                    }
                    String jsonString = JSON.toJSONString(idItems);
                    refresh_list(songsList, jsonString, idItems);
                    Toast.makeText(this,"长按歌曲名观看MV",Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    list_layout.setVisibility(View.GONE);
                    null_layout.setVisibility(View.VISIBLE);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
            list_layout.setVisibility(View.GONE);
            null_layout.setVisibility(View.VISIBLE);
            hot_layout.setVisibility(View.GONE);
            e.printStackTrace();
        }
        findViewById(R.id.loading_layout).setVisibility(View.GONE);
    }

    public void refresh_list(final List search_list, final String idl, List idi) {
        arr = new ArrayList();
        LinearLayout list_layout = findViewById(R.id.list_layout);
        LinearLayout null_layout = findViewById(R.id.null_layout);
        list_layout.setVisibility(View.VISIBLE);
        null_layout.setVisibility(View.GONE);
        List mvids = new ArrayList();
        for (int i = 0; i < search_list.size(); i++) {
            Map maps = (Map) JSON.parse(search_list.get(i).toString());
            List ar_temp = JSON.parseArray(maps.get("artists").toString());
            Map ar = (Map) JSON.parse(ar_temp.get(0).toString());
            Map tmp_song = (Map)JSON.parse(tmp.get(i).toString());
            mvids.add(tmp_song.get("mvid").toString());
            temp_hl = "<font color='#2A2B2C'>" + maps.get("name").toString() + "</font> - " + "<font color='#999999'>" + ar.get("name").toString() + "</font>";
            arr.add(temp_hl);
        }
        ArrayAdapter adapter = new ArrayAdapter(SearchActivity.this, R.layout.item, arr) {
            public Object getItem(int position) {
                return Html.fromHtml(arr.get(position).toString());
            }
        };
        ListView list = findViewById(R.id.list);
        list.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent intent = new Intent(SearchActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent.putExtra("type", "0");
            intent.putExtra("list", idl);
            intent.putExtra("start", String.valueOf(i));
            intent.putExtra("mvids", JSON.toJSONString(mvids));
            startActivity(intent);
        });
        list.setOnItemLongClickListener((adapterView, view, i, l) -> {
            Map tmp_song = (Map)JSON.parse(tmp.get(i).toString());
            String mvid = tmp_song.get("mvid").toString();
            if(mvid.equals("0")){
                Toast.makeText(SearchActivity.this, "该视频没有对应MV", Toast.LENGTH_SHORT).show();
            }
            else{
                Map maps = null;
                try {
                    maps = (Map) new MVApi().MV(mvid);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Map data = (Map) JSON.parse(maps.get("data").toString());
                try
                {
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("cn.luern0313.wristvideoplayer", "cn.luern0313.wristvideoplayer.ui.PlayerActivity"));
                    intent.putExtra("mode", 1);
                    intent.putExtra("url", data.get("url").toString());
                    intent.putExtra("url_backup", data.get("url").toString());
                    intent.putExtra("title", tmp_song.get("name").toString());
                    intent.putExtra("identity_name", getString(R.string.app_name));
                    startActivityForResult(intent, 0);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    try
                    {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("cn.luern0313.wristvideoplayer_free", "cn.luern0313.wristvideoplayer_free.ui.PlayerActivity"));
                        intent.putExtra("mode", 1);
                        intent.putExtra("url", data.get("url").toString());
                        intent.putExtra("url_backup", data.get("url").toString());
                        intent.putExtra("title", tmp_song.get("name").toString());
                        intent.putExtra("identity_name", getString(R.string.app_name));
                        startActivityForResult(intent, 0);
                    }
                    catch(Exception ee)
                    {
                        Toast.makeText(SearchActivity.this, "你没有安装配套视频软件：腕上视频，请先前往应用商店下载！", Toast.LENGTH_LONG).show();
                        ee.printStackTrace();
                    }
                }
            }
            return true;
        });
        list.setAdapter(adapter);
    }
}
