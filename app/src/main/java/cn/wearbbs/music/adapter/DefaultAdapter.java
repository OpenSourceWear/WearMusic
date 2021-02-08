package cn.wearbbs.music.adapter;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.MVApi;
import cn.wearbbs.music.api.MusicPanApi;
import cn.wearbbs.music.api.PlayListApi;
import cn.wearbbs.music.ui.MainActivity;
import cn.wearbbs.music.ui.MusicPanActivity;
import cn.wearbbs.music.ui.PlayListActivity;
import cn.wearbbs.music.ui.SongListActivity;

public class DefaultAdapter extends BaseAdapter {
    private Context context;
    private String jsonString;
    private String mvids;
    private int size;
    private String names;
    private int type;
    int SONGLIST = 0;
    int MUSICPAN = 1;
    public DefaultAdapter(String mvids, String idl, int size, String names, Context context,int type){
        this.context=context;
        this.jsonString = idl;
        this.mvids = mvids;
        this.size = size;
        this.names = names;
        this.type = type;
    }
    @Override
    public int getCount() {
        //return返回的是int类型，也就是页面要显示的数量。
        return size;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    String mvid;
    AlertDialog alertDialog;
    String cookie;
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        List namesList = JSON.parseArray(names);
        if (convertView==null){
            //通过一个打气筒 inflate 可以把一个布局转换成一个view对象
            view=View.inflate(context,R.layout.item,null);
        }else {
            view=convertView;//复用历史缓存对象
        }
        view.findViewById(R.id.mv_icon).setVisibility(View.VISIBLE);
        mvid = JSON.parseArray(mvids).get(position).toString();
        if(mvid.equals("0")){
            view.findViewById(R.id.mv_icon).setVisibility(View.GONE);
        }
        view.findViewById(R.id.title).setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent.putExtra("type", "0");
            intent.putExtra("list", jsonString);
            intent.putExtra("start", String.valueOf(position));
            intent.putExtra("mvids",mvids );
            context.startActivity(intent);
        });
        view.findViewById(R.id.mv_icon).setOnClickListener(v -> {
            Map maps = new HashMap();
            Map detailMaps = new HashMap();
            try {
                maps = new MVApi().getMVUrl(JSON.parseArray(mvids).get(position).toString());
                detailMaps = new MVApi().getMVDetail(JSON.parseArray(mvids).get(position).toString());
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
                intent.putExtra("title", ((Map)JSON.parse(detailMaps.get("data").toString())).get("name").toString());
                intent.putExtra("identity_name", "WearMusic");
                context.startActivity(intent);
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
                    intent.putExtra("title", ((Map)JSON.parse(detailMaps.get("data").toString())).get("name").toString());
                    intent.putExtra("identity_name", "WearMusic");
                    context.startActivity(intent);
                }
                catch(Exception ee)
                {
                    Toast.makeText(context, "你没有安装配套视频软件：腕上视频，请先前往应用商店下载！", Toast.LENGTH_LONG).show();
                    ee.printStackTrace();
                }
            }
        });
        if(type == SONGLIST){
            view.findViewById(R.id.title).setOnLongClickListener(v -> {
                alertDialog = new AlertDialog.Builder(context)
                        .setMessage("要删除该音乐吗？")
                        .setPositiveButton("确定", (dialogInterface, i12) -> {
                            try {
                                File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                                BufferedReader in1 = new BufferedReader(new FileReader(saver));
                                cookie = in1.readLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                new PlayListApi().deletePlayListMusic(SongListActivity.ID,SongListActivity.ids.split(",")[position],cookie);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(context,"删除成功",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, SongListActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                            context.startActivity(intent);
                        })
                        .setNegativeButton("取消", (dialogInterface, i1) -> alertDialog.dismiss())
                        .create();
                alertDialog.show();
                return true;
            });

        }
        else{
            view.findViewById(R.id.title).setOnLongClickListener(v -> {
                alertDialog = new AlertDialog.Builder(context)
                        .setMessage("要删除该音乐吗？")
                        .setPositiveButton("确定", (dialogInterface, i12) -> {
                            try {
                                File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                                BufferedReader in1 = new BufferedReader(new FileReader(saver));
                                cookie = in1.readLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                new MusicPanApi().deletePanMusic(((Map)JSON.parse(JSON.parseArray(jsonString).get(position).toString())).get("id").toString(),cookie);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(context,"删除成功",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, MusicPanActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                            context.startActivity(intent);
                        })
                        .setNegativeButton("取消", (dialogInterface, i1) -> alertDialog.dismiss())
                        .create();
                alertDialog.show();
                return true;
            });
        }
        ((TextView)view.findViewById(R.id.title)).setText(Html.fromHtml(namesList.get(position).toString()));
        return view;
    }
}
