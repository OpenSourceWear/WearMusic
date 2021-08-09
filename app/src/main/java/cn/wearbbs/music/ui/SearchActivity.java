package cn.wearbbs.music.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import api.MusicApi;
import cn.wearbbs.music.R;
import cn.wearbbs.music.adapter.MusicAdapter;
import cn.wearbbs.music.util.SharedPreferencesUtil;
import cn.wearbbs.music.view.LoadingView;
import cn.wearbbs.music.view.MessageView;

/**
 * 搜索
 */
public class SearchActivity extends SlideBackActivity {
    MusicApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        api = new MusicApi(SharedPreferencesUtil.getString("cookie", "", this));
        TextView tv_noMore = findViewById(R.id.tv_noMore);
        tv_noMore.setText(R.string.loading);
        // 获取热搜
        new Thread(() -> {
            JSONArray hot = api.getHot();
            if (hot.size() > 0) {
                TagFlowLayout tfl_hot = findViewById(R.id.tfl_hot);
                String[] hotList = new String[10];
                for (int i = 0; i < 10; i++) {
                    hotList[i] = hot.getJSONObject(i).getString("first");
                }
                runOnUiThread(() -> {
                    tv_noMore.setText(R.string.no_more);
                    tfl_hot.setAdapter(new TagAdapter<String>(hotList) {
                        @Override
                        public View getView(FlowLayout parent, int position, String s) {
                            final LayoutInflater mInflater = LayoutInflater.from(SearchActivity.this);
                            TextView tv_hot = (TextView) mInflater.inflate(R.layout.item_hot,
                                    tfl_hot, false);
                            tv_hot.setText(s);
                            return tv_hot;
                        }
                    });
                    tfl_hot.setOnTagClickListener((view, position, parent) -> {
                        EditText editText = findViewById(R.id.et_search);
                        editText.setText(hotList[position]);
                        findViewById(R.id.ll_hot_main).setVisibility(View.GONE);
                        search();
                        return true;
                    });
                });
            }
        }).start();
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_title:
                Intent intent = new Intent(SearchActivity.this, MenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
                break;
            case R.id.iv_search:
                findViewById(R.id.ll_hot_main).setVisibility(View.GONE);
                search();
                break;
        }
    }

    public void search() {
        // 搜索
        EditText et_search = findViewById(R.id.et_search);
        LoadingView lv_loading = findViewById(R.id.lv_loading);
        MessageView mv_message = findViewById(R.id.mv_message);

        lv_loading.setVisibility(View.VISIBLE);
        findViewById(R.id.rv_search).setVisibility(View.GONE);
        mv_message.setVisibility(View.GONE);

        new Thread(() -> {
            try{
                String keyword = et_search.getText().toString();
                JSONArray data = api.searchMusic(keyword);
                if (data.size() == 0) {
                    runOnUiThread(() -> {
                        lv_loading.setVisibility(View.GONE);
                        mv_message.setVisibility(View.VISIBLE);
                        if (keyword.contains("自杀")) {
                            // 部分含“自杀”关键词的搜索，API不返回搜索结果，需要特殊显示（彩蛋）
                            mv_message.setText(R.string.msg_suicide);
                            mv_message.setImageResource(R.drawable.ic_baseline_family_restroom_24);
                        } else {
                            // 数据为空
                            mv_message.setContent(MessageView.NO_MUSIC,null);
                        }
                    });
                } else {
                    MusicAdapter adapter = new MusicAdapter(data, this);
                    runOnUiThread(() -> {
                        lv_loading.setVisibility(View.GONE);
                        findViewById(R.id.rv_search).setVisibility(View.VISIBLE);
                        findViewById(R.id.mv_message).setVisibility(View.GONE);
                        RecyclerView rv_search = findViewById(R.id.rv_search);
                        rv_search.setLayoutManager(new LinearLayoutManager(this));
                        rv_search.setAdapter(adapter);
                    });
                }
            }
            catch (Exception e){
                runOnUiThread(()->{
                    mv_message.setVisibility(View.VISIBLE);
                    mv_message.setContent(MessageView.LOAD_FAILED, v -> {
                        mv_message.setVisibility(View.GONE);
                        search();
                    });
                });
            }
        }).start();
    }
}