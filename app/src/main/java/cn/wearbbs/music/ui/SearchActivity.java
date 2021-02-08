package cn.wearbbs.music.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.SearchAdapter;
import cn.wearbbs.music.api.HotApi;
import cn.wearbbs.music.api.SearchApi;

public class SearchActivity extends SlideBackActivity {
    List arr;
    String temp_hl;
    List tmp;
    String cookie;
    EditText editText;
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
        editText = findViewById(R.id.editText);
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
        tv.setText("加载中\n\n");
        tv.setTextColor(Color.parseColor("#999999"));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(12);
        ((ListView)findViewById(R.id.list)).addFooterView(tv,null,false);
        TagFlowLayout search_page_flowlayout = findViewById(R.id.id_flowlayout);
        final LayoutInflater mInflater = LayoutInflater.from(SearchActivity.this);
        Thread thread = new Thread(()->{
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
            SearchActivity.this.runOnUiThread(()->{
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
                tv.setText("没有更多了");
            });
        });
        thread.start();
    }

    public void menu(View view) {
        Intent intent = new Intent(SearchActivity.this, MenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
    }
    String t;
    Map mapd;
    boolean able = true;
    LinearLayout zs;
    LinearLayout list_layout;
    public void search(View view) {
        t = editText.getText().toString();
        list_layout = findViewById(R.id.list_layout);
        LinearLayout null_layout = findViewById(R.id.null_layout);
        LinearLayout hot_layout = findViewById(R.id.hot_layout);
        zs = findViewById(R.id.zs);
        list_layout.setVisibility(View.VISIBLE);
        null_layout.setVisibility(View.GONE);
        hot_layout.setVisibility(View.GONE);
        zs.setVisibility(View.GONE);
        if(editText.getText().toString().equals("自杀")){
            zs.setVisibility(View.VISIBLE);
            list_layout.setVisibility(View.GONE);
            able=false;
        }
        else{
            findViewById(R.id.loading_layout).setVisibility(View.VISIBLE);
            list_layout.setVisibility(View.GONE);
        }
        Thread thread = new Thread(()->{
            if(able){
                try {
                    mapd = new SearchApi().Search(t,cookie);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            SearchActivity.this.runOnUiThread(()-> {
                changeUI(mapd);
                zs = findViewById(R.id.zs);
                list_layout = findViewById(R.id.list_layout);
                zs.setVisibility(View.GONE);
                list_layout.setVisibility(View.VISIBLE);
            });
        });
        thread.start();

    }
    public void changeUI(Map mapd){
        LinearLayout list_layout = findViewById(R.id.list_layout);
        LinearLayout null_layout = findViewById(R.id.null_layout);
        LinearLayout hot_layout = findViewById(R.id.hot_layout);
        list_layout.setVisibility(View.VISIBLE);
        try {
            Map result = (Map) JSON.parse(mapd.get("result").toString());
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
            } catch (Exception e) {
                list_layout.setVisibility(View.GONE);
                null_layout.setVisibility(View.VISIBLE);
                e.printStackTrace();
            }
        } catch (Exception e) {
            list_layout.setVisibility(View.GONE);
            null_layout.setVisibility(View.VISIBLE);
            hot_layout.setVisibility(View.GONE);
            e.printStackTrace();
        }
        findViewById(R.id.loading_layout).setVisibility(View.GONE);
    }
    public void refresh_list(final List search_list, final String idl, List idi) {
        SearchAdapter adapter = new SearchAdapter(search_list,idl,idi,tmp,this);
        ListView list = findViewById(R.id.list);
        list.setAdapter(adapter);
    }
}
