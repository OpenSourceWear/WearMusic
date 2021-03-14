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
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.SearchAdapter;
import cn.wearbbs.music.api.HitokotoApi;
import cn.wearbbs.music.api.HotApi;
import cn.wearbbs.music.api.SearchApi;
import cn.wearbbs.music.util.UserInfoUtil;

public class SearchActivity extends SlideBackActivity {
    List arr;
    List tmp;
    String cookie;
    EditText editText;
    String text = "没有更多了";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        LinearLayout list_layout = findViewById(R.id.ll_list);
        LinearLayout null_layout = findViewById(R.id.ll_noMusic);
        LinearLayout zs = findViewById(R.id.ll_message);
        LinearLayout hot_layout = findViewById(R.id.ll_hot_main);
        list_layout.setVisibility(View.GONE);
        null_layout.setVisibility(View.GONE);
        editText = findViewById(R.id.et_reply);
        hot_layout.setVisibility(View.VISIBLE);
        zs.setVisibility(View.GONE);
        findViewById(R.id.ll_loading).setVisibility(View.GONE);
        String temp = "[]";
        arr = JSON.parseArray(temp);
        cookie = UserInfoUtil.getUserInfo(this,"cookie");
        TextView tv = findViewById(R.id.tv_nomore);
        tv.setText("加载中\n\n");
        TagFlowLayout search_page_flowlayout = findViewById(R.id.id_flowlayout);
        final LayoutInflater mInflater = LayoutInflater.from(SearchActivity.this);
        Thread thread = new Thread(()->{
            Map map = null;
            try {
                map = new HotApi().getHotSearch();
                text = new HitokotoApi().getHitokoto();
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
                        TextView tv_fl = (TextView) mInflater.inflate(R.layout.item_hot,
                                search_page_flowlayout, false);
                        tv_fl.setText(s);
                        return tv_fl;
                    }
                });
                search_page_flowlayout.setOnTagClickListener((view, position, parent) -> {
                    EditText editText = findViewById(R.id.et_reply);
                    editText.setText(hot_list[position]);
                    search(null);
                    return true;
                });
                tv.setText(text+"\n\n");
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
        if(editText.getText().toString().contains("少爷")){
            ((TextView)findViewById(R.id.tv_loading)).setText("欢迎洛府子弟 (*/ω＼*)");
        }
        else{
            ((TextView)findViewById(R.id.tv_loading)).setText("加载中...");
        }
        t = editText.getText().toString();
        list_layout = findViewById(R.id.ll_list);
        LinearLayout null_layout = findViewById(R.id.ll_noMusic);
        LinearLayout hot_layout = findViewById(R.id.ll_hot_main);
        zs = findViewById(R.id.ll_message);
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
            findViewById(R.id.ll_loading).setVisibility(View.VISIBLE);
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
                zs = findViewById(R.id.ll_message);
                zs.setVisibility(View.GONE);
                list_layout = findViewById(R.id.ll_list);
                list_layout.setVisibility(View.VISIBLE);
            });
        });
        thread.start();

    }
    public void changeUI(Map mapd){
        LinearLayout list_layout = findViewById(R.id.ll_list);
        LinearLayout null_layout = findViewById(R.id.ll_noMusic);
        LinearLayout hot_layout = findViewById(R.id.ll_hot_main);
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
        findViewById(R.id.ll_loading).setVisibility(View.GONE);
    }
    public void refresh_list(final List search_list, final String idl, List idi) {
        SearchAdapter adapter = new SearchAdapter(search_list,idl,idi,tmp,this);
        ListView list = findViewById(R.id.list);
        list.setAdapter(adapter);
    }
}
