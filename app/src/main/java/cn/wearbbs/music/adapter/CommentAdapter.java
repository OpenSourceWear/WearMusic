package cn.wearbbs.music.adapter;

import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import cn.carbs.android.expandabletextview.library.ExpandableTextView;
import cn.wearbbs.music.R;
import cn.wearbbs.music.api.CommentApi;
import cn.wearbbs.music.application.MyApplication;
import cn.wearbbs.music.detail.Data;
import cn.wearbbs.music.util.UserInfoUtil;

/**
 * @author JackuXL
 */
public class CommentAdapter extends BaseAdapter {
    private final List<String> contentList,nameList,idList;
    private final CommentApi api;
    private final List<Boolean> liked;
    private final List<String> avatarList;
    String cookie;

    /**
     * 评论列表 Adapter
     * @param contentList 评论内容列表
     * @param nameList 昵称列表
     * @param idList 评论ID列表
     * @param api 评论API对象
     */
    public CommentAdapter(List<String> contentList, List<String> nameList,List<String> idList,CommentApi api,List<Boolean> liked,List<String> avatarList){
        this.contentList=contentList;
        this.nameList=nameList;
        this.idList=idList;
        this.api = api;
        this.liked = liked;
        this.avatarList = avatarList;
    }
    @Override
    public int getCount() {
        //return返回的是int类型，也就是页面要显示的数量。
        return contentList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    JSONObject result;
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        view=View.inflate(MyApplication.getContext(),R.layout.item_comment,null);
        view.findViewById(R.id.iv_thumb).setOnClickListener(v -> {
            cookie = UserInfoUtil.getUserInfo(MyApplication.getContext(),"cookie");
            if(liked.get(position)){
                new Thread(()-> {
                    try {
                        result = api.likeComment(idList.get(position), Data.dislikeMode);
                        if(result.getInteger("code")!=Data.successCode){
                            Looper.prepare();
                            Toast.makeText(MyApplication.getContext(),"未知错误",Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                ((ImageView)view.findViewById(R.id.iv_thumb)).setImageResource(R.drawable.ic_outline_thumb_up_24);
            }
            else{
                new Thread(()-> {
                    try {
                        api.likeComment(idList.get(position),Data.likeMode);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
                ((ImageView)view.findViewById(R.id.iv_thumb)).setImageResource(R.drawable.ic_baseline_thumb_up_24);
            }
        });
        ((ExpandableTextView)view.findViewById(R.id.et_content)).setText(contentList.get(position));
        ((TextView)view.findViewById(R.id.tv_name)).setText(nameList.get(position));
        if(liked.get(position)){
            ((ImageView)view.findViewById(R.id.iv_thumb)).setImageResource(R.drawable.ic_baseline_thumb_up_24);
        }
        else{
            ((ImageView)view.findViewById(R.id.iv_thumb)).setImageResource(R.drawable.ic_outline_thumb_up_24);
        }
        ImageView iv_avatar = view.findViewById(R.id.iv_avatar);
        RequestOptions options = RequestOptions.circleCropTransform().placeholder(R.drawable.ic_baseline_supervised_user_circle_24);
        Glide.with(MyApplication.getContext()).load(avatarList.get(position)).apply(options).into(iv_avatar);
        return view;
    }
}
