package cn.wearbbs.music.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import cn.wearbbs.music.R;
import cn.wearbbs.music.ui.MusicListActivity;

public class MusicLibraryAdapter extends RecyclerView.Adapter<MusicLibraryAdapter.ViewHolder> {
    private final JSONArray data;
    private final Activity activity;

    public MusicLibraryAdapter(JSONArray data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_music, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        JSONObject playListDetail = data.getJSONObject(position);
        viewHolder.tv_title.setText(playListDetail.getString("name"));
        viewHolder.tv_artists.setText(String.format("by %s", playListDetail.getJSONObject("creator").getString("nickname")));
        RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(10)).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
        viewHolder.iv_cover.setImageResource(R.drawable.ic_baseline_photo_size_select_actual_24);
        try{
            Glide.with(activity).load(playListDetail.getString("coverImgUrl").replace("http://", "https://")).apply(options).into(viewHolder.iv_cover);
        }
        catch (Exception ignored){}
        viewHolder.ll_main.setOnClickListener(v -> {
            Intent intent = new Intent(activity, MusicListActivity.class);
            intent.putExtra("detail", playListDetail.toJSONString());
            activity.startActivity(intent);
            activity.finish();
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_cover;
        TextView tv_title;
        TextView tv_artists;
        LinearLayout ll_main;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_cover = itemView.findViewById(R.id.iv_cover);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_artists = itemView.findViewById(R.id.tv_artists);
            ll_main = itemView.findViewById(R.id.ll_main);
        }
    }
}
