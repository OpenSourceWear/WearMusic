package cn.wearbbs.music.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import api.MusicApi;
import cn.carbs.android.expandabletextview.library.ExpandableTextView;
import cn.wearbbs.music.R;
import cn.wearbbs.music.ui.MenuActivity;
import cn.wearbbs.music.ui.UserProfileActivity;
import cn.wearbbs.music.util.SharedPreferencesUtil;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private final JSONArray data;
    private final Activity activity;
    private final String id;
    public static final int ITEM_TYPE_HEADER = 0;
    public static final int ITEM_TYPE_CONTENT = 1;
    private View header = null;

    public CommentAdapter(JSONArray data, String id,Activity activity) {
        this.data = data;
        this.activity = activity;
        this.id = id;
    }

    public CommentAdapter(JSONArray data, String id,Activity activity,View header) {
        this.data = data;
        this.activity = activity;
        this.id = id;
        this.header = header;
    }

    @NotNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==ITEM_TYPE_HEADER){
            return new CommentAdapter.ViewHolder(header);
        }
        else{
            return new CommentAdapter.ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_comment, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0&&header!=null){
            return ITEM_TYPE_HEADER;
        }
        else{
            return ITEM_TYPE_CONTENT;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        if(getItemViewType(position)==ITEM_TYPE_CONTENT){
            if(header!=null){
                position--;
            }
            JSONObject currentCommentInfo = data.getJSONObject(position);
            viewHolder.iv_avatar.setImageResource(R.drawable.ic_baseline_photo_size_select_actual_24);
            RequestOptions options = RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
            Glide.with(activity).load(currentCommentInfo.getJSONObject("user").getString("avatarUrl").replace("http://","https://")).apply(options).into(viewHolder.iv_avatar);
            viewHolder.tv_name.setText(currentCommentInfo.getJSONObject("user").getString("nickname"));
            viewHolder.etv_content.setText(data.getJSONObject(position).getString("content"));
            if(currentCommentInfo.getBoolean("liked")){
                viewHolder.iv_thumb.setImageResource(R.drawable.ic_baseline_thumb_up_24);
            }
            else{
                viewHolder.iv_thumb.setImageResource(R.drawable.ic_outline_thumb_up_24);
            }
            viewHolder.iv_thumb.setOnClickListener(v -> new Thread(()->{
                Looper.prepare();
                if(currentCommentInfo.getBoolean("liked")){
                    if(new MusicApi(SharedPreferencesUtil.getString("cookie",""))
                            .likeComment(id,currentCommentInfo.getString("commentId"), false)){
                        currentCommentInfo.put("liked",false);
                        activity.runOnUiThread(()->viewHolder.iv_thumb.setImageResource(R.drawable.ic_outline_thumb_up_24));
                    }
                    else{
                        Toast.makeText(activity,"取消点赞失败",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    if(new MusicApi(SharedPreferencesUtil.getString("cookie",""))
                            .likeComment(id,currentCommentInfo.getString("commentId"), true)){
                        currentCommentInfo.put("liked",true);
                        activity.runOnUiThread(()->viewHolder.iv_thumb.setImageResource(R.drawable.ic_baseline_thumb_up_24));
                    }
                    else{
                        Toast.makeText(activity,"点赞失败",Toast.LENGTH_SHORT).show();
                    }
                }
                Looper.loop();
            }).start());
            View.OnClickListener onUserClickListener = v -> activity.startActivity(new Intent(activity, UserProfileActivity.class).putExtra("profile",currentCommentInfo.getString("user")));
            viewHolder.tv_name.setOnClickListener(onUserClickListener);
            viewHolder.iv_avatar.setOnClickListener(onUserClickListener);
        }
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_avatar;
        TextView tv_name;
        ImageView iv_thumb;
        ExpandableTextView etv_content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_avatar = itemView.findViewById(R.id.iv_avatar);
            tv_name = itemView.findViewById(R.id.tv_name);
            iv_thumb = itemView.findViewById(R.id.iv_thumb);
            etv_content = itemView.findViewById(R.id.et_content);
        }
    }
}
