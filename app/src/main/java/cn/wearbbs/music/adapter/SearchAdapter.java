package cn.wearbbs.music.adapter;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.wearbbs.music.R;
import cn.wearbbs.music.api.MVApi;
import cn.wearbbs.music.ui.MainActivity;

public class SearchAdapter extends BaseAdapter {
    private List listText;
    private Context context;
    private String id;
    private String idl;
    private List idi;
    List arr;
    String temp_hl;
    List tmp;
    List mvids;
    public SearchAdapter(final List search_list, final String idl, List idi, List tmp, Context context){
        this.listText=search_list;
        this.context=context;
        this.idl = idl;
        this.idi=idi;
        this.tmp = tmp;
        arr = new ArrayList();
        mvids = new ArrayList();
        for (int i = 0; i < listText.size(); i++) {
            Map maps = (Map) JSON.parse(listText.get(i).toString());
            List ar_temp = JSON.parseArray(maps.get("artists").toString());
            Map ar = (Map) JSON.parse(ar_temp.get(0).toString());
            Map tmp_song = (Map)JSON.parse(tmp.get(i).toString());
            mvids.add(tmp_song.get("mvid").toString());
            temp_hl = "<font color='#2A2B2C'>" + maps.get("name").toString() + "</font> - " + "<font color='#999999'>" + ar.get("name").toString() + "</font>";
            arr.add(temp_hl);
        }
    }
    @Override
    public int getCount() {
        //return返回的是int类型，也就是页面要显示的数量。
        return listText.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    Map tmp_song;
    String mvid;
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView==null){
            //通过一个打气筒 inflate 可以把一个布局转换成一个view对象
            view=View.inflate(context,R.layout.item,null);
        }else {
            view=convertView;//复用历史缓存对象
        }
        view.findViewById(R.id.mv_icon).setVisibility(View.VISIBLE);
        tmp_song = (Map) JSON.parse(tmp.get(position).toString());
        mvid = tmp_song.get("mvid").toString();
        if(mvid.equals("0")){
            view.findViewById(R.id.mv_icon).setVisibility(View.GONE);
        }
        view.findViewById(R.id.title).setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            intent.putExtra("type", "0");
            intent.putExtra("list", idl);
            intent.putExtra("start", String.valueOf(position));
            intent.putExtra("mvids", JSON.toJSONString(mvids));
            context.startActivity(intent);
        });
        view.findViewById(R.id.mv_icon).setOnClickListener(v -> {
            tmp_song = (Map) JSON.parse(tmp.get(position).toString());
            mvid = tmp_song.get("mvid").toString();
            if(mvid.equals("0")){
                Toast.makeText(context, "该视频没有对应MV", Toast.LENGTH_SHORT).show();
            }
            else{
                Map maps = null;
                try {
                    maps = new MVApi().getMVUrl(mvid);
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
                        intent.putExtra("title", tmp_song.get("name").toString());
                        intent.putExtra("identity_name", "WearMusic");
                        context.startActivity(intent);
                    }
                    catch(Exception ee)
                    {
                        Toast.makeText(context, "你没有安装配套视频软件：腕上视频，请先前往应用商店下载！", Toast.LENGTH_LONG).show();
                        ee.printStackTrace();
                    }
                }
            }
        });
        ((TextView)view.findViewById(R.id.title)).setText(Html.fromHtml(arr.get(position).toString()));
        return view;
    }
}
