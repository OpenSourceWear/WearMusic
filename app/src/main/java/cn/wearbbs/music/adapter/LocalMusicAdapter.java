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

import java.io.File;

import cn.wearbbs.music.R;
import cn.wearbbs.music.ui.MainActivity;

public class LocalMusicAdapter extends RecyclerView.Adapter<LocalMusicAdapter.ViewHolder> {
    private final JSONArray data;
    private final Activity activity;

    public LocalMusicAdapter(JSONArray data, Activity activity) {
        this.data = data;
        this.activity = activity;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_music, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {
        JSONObject currentMusicInfo = data.getJSONObject(position);
        viewHolder.iv_cover.setImageResource(R.drawable.ic_baseline_photo_size_select_actual_24);
        if(currentMusicInfo.getString("coverFile")!=null){
            RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(10)).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
            Glide.with(activity).load(currentMusicInfo.getString("coverFile")).apply(options).into(viewHolder.iv_cover);
        }
        String name = getFileName(new File(currentMusicInfo.getString("musicFile")));

        if(name.contains("--")){
            viewHolder.tv_title.setText(name.substring(0,name.indexOf("-")));
            viewHolder.tv_artists.setText(name.substring(name.lastIndexOf("-")+1));
        }
        else{
            viewHolder.tv_title.setText(name);
            viewHolder.tv_artists.setText(activity.getString(R.string.unknown));
        }

        data.getJSONObject(position).put("name",viewHolder.tv_title.getText());
        data.getJSONObject(position).put("artists",viewHolder.tv_artists.getText());

        viewHolder.ll_main.setOnClickListener(v -> {
            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("data", data.toJSONString());
            intent.putExtra("musicIndex", position);
            intent.putExtra("local",true);
            activity.startActivity(intent);
            activity.finish();
        });
    }

    private String getFileName(File file){
        return file.getName().substring(0,file.getName().lastIndexOf("."));
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