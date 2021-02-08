package cn.wearbbs.music.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.carbs.android.expandabletextview.library.ExpandableTextView;
import cn.wearbbs.music.R;
import cn.wearbbs.music.api.CommentApi;

public class CommentAdapter extends BaseAdapter {
    private List listText;
    private Context context;
    private List arr_name;
    private String id;
    private List id_list;
    private List liked_items;
    Map map = new HashMap();
    String cookie;
    public CommentAdapter(List listText, List arr_name,String id,List id_list,Context context){
        this.listText=listText;
        this.context=context;
        this.arr_name=arr_name;
        this.id=id;
        this.id_list=id_list;
        this.liked_items = new ArrayList();
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


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView==null){
            //通过一个打气筒 inflate 可以把一个布局转换成一个view对象
            view=View.inflate(context,R.layout.item_comment,null);
        }else {
            view=convertView;//复用历史缓存对象
        }
        view.findViewById(R.id.like).setOnClickListener(v -> {
            try {
                File saver = new File("/sdcard/Android/data/cn.wearbbs.music/cookie.txt");
                BufferedReader in = new BufferedReader(new FileReader(saver));
                cookie = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(liked_items.contains(position)){
                Thread thread = new Thread(()-> {
                    try {
                        new CommentApi().dislikeComment(id,id_list.get(position).toString(),cookie);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
                ((ImageView)view.findViewById(R.id.like_icon)).setImageResource(R.drawable.ic_baseline_favorite_border_24);
                liked_items.remove(map.get(position));
            }
            else{
                Thread thread = new Thread(()-> {
                    try {
                        new CommentApi().likeComment(id,id_list.get(position).toString(),cookie);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
                ((ImageView)view.findViewById(R.id.like_icon)).setImageResource(R.drawable.ic_baseline_favorite_24);
                liked_items.add(position);
                map.put(position,liked_items.size()-1);
            }
        });
        ((ExpandableTextView)view.findViewById(R.id.title)).setText(listText.get(position).toString());
        ((TextView)view.findViewById(R.id.name)).setText(arr_name.get(position).toString());
        return view;
    }
}
