package cn.wearbbs.music.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.MVApi;
import cn.wearbbs.music.ui.MainActivity;

public class PlayListAdapter extends BaseAdapter {
    private Context context;
    private String jsonString;
    private String mvids;
    private int size;
    private String names;
    private int type;
    private int musicIndex;
    public PlayListAdapter(String mvids, String idl, int size, String names, Context context, int type,int musicIndex){
        this.context=context;
        this.jsonString = idl;
        this.mvids = mvids;
        this.size = size;
        this.names = names;
        this.type = type;
        this.musicIndex = musicIndex;
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
        view.findViewById(R.id.iv_mv).setVisibility(View.VISIBLE);
        if(position == musicIndex){
            view.findViewById(R.id.iv_playing).setVisibility(View.VISIBLE);
        }
        if(mvids!=null){
            mvid = JSON.parseArray(mvids).get(position).toString();
            if(mvid.equals("0")){
                view.findViewById(R.id.iv_mv).setVisibility(View.GONE);
            }
        }
        view.findViewById(R.id.iv_mv).setOnClickListener(v -> {
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
        view.findViewById(R.id.title).setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent.putExtra("type", "3");
            intent.putExtra("list", jsonString);
            intent.putExtra("start", String.valueOf(position));
            intent.putExtra("mvids",mvids);
            context.startActivity(intent);
        });
        ((TextView)view.findViewById(R.id.title)).setText(Html.fromHtml(namesList.get(position).toString()));
        return view;
    }
}
