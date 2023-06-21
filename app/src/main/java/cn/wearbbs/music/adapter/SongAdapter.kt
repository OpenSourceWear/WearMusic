package cn.wearbbs.music.adapter

import cn.jackuxl.model.Song
import androidx.recyclerview.widget.RecyclerView
import android.app.Activity
import android.view.ViewGroup
import android.view.LayoutInflater
import cn.wearbbs.music.R
import cn.jackuxl.api.SongApi
import cn.wearbbs.music.util.SharedPreferencesUtil
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.Glide
import android.content.Intent
import android.view.View
import android.widget.ImageView
import cn.wearbbs.music.ui.MainActivity
import org.greenrobot.eventbus.EventBus
import cn.wearbbs.music.event.MessageEvent
import com.google.gson.Gson
import android.widget.TextView
import android.widget.LinearLayout
import cn.jackuxl.model.Artist
import java.lang.Exception

class SongAdapter : RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private val data: List<Song>
    private val activity: Activity
    private var header: View? = null

    constructor(data: List<Song>, activity: Activity) {
        this.data = data
        this.activity = activity
    }

    constructor(data: List<Song>, activity: Activity, header: View?) {
        this.data = data
        this.activity = activity
        this.header = header
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == ITEM_TYPE_HEADER && header!=null) {
            ViewHolder(header!!)
        } else {
            ViewHolder(
                LayoutInflater.from(activity).inflate(R.layout.item_music, parent, false)
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && header != null) {
            ITEM_TYPE_HEADER
        } else {
            ITEM_TYPE_CONTENT
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        var position = position
        if (getItemViewType(position) == ITEM_TYPE_CONTENT) {
            if (header != null) {
                position--
            }
            val (album, _, artists, _, _, _, _, _, _, _, name) = data[position]
            val imgUrl: Array<String?> = arrayOf("")
            // 兼容音乐云盘
//            if (songDetail.containsKey("simpleSong")) {
//                songDetail = songDetail.getJSONObject("simpleSong");
//            }
            //           if (songDetail.containsKey("artists")) {
            val artist: Artist = artists[0]
            val api = SongApi(
                SharedPreferencesUtil.getString(
                    "cookie",
                    ""
                )
            )
            Thread {
                try {
                    imgUrl[0] = api.getSongCover(album.id)
                    activity.runOnUiThread {
                        viewHolder.ivCover?.setImageResource(R.drawable.ic_baseline_photo_size_select_actual_24)
                        val options = RequestOptions.bitmapTransform(RoundedCorners(10))
                            .placeholder(R.drawable.ic_baseline_photo_size_select_actual_24)
                            .error(R.drawable.ic_baseline_photo_size_select_actual_24)
                        try {
                            viewHolder.ivCover?.let {
                                Glide.with(activity).load(imgUrl[0]?.replace("http://", "https://"))
                                    .apply(options).into(it)
                            }
                        } catch (ignored: Exception) {
                            ignored.printStackTrace()
                        }
                    }
                } catch (ignored: Exception) {
                    ignored.printStackTrace()
                }
            }.start()
            //            } else {
//                artists = songDetail.getJSONArray("ar");
//                imgUrl[0] = songDetail.getJSONObject("al").getString("picUrl");
//                RequestOptions options = RequestOptions.bitmapTransform(new RoundedCorners(10)).placeholder(R.drawable.ic_baseline_photo_size_select_actual_24).error(R.drawable.ic_baseline_photo_size_select_actual_24);
//                try{
//                    Glide.with(activity).load(imgUrl[0].replace("http://", "https://")).apply(options).into(viewHolder.iv_cover);
//                }
//                catch (Exception ignored) { }
//            }
            viewHolder.tvTitle?.text = name
            if(artist.name.isNullOrBlank()){
                artist.name = activity.getString(R.string.unknown);
            }
            viewHolder.tvArtists?.text = artist.name
            val finalPosition = position
            viewHolder.llMain?.setOnClickListener {
                val intent = Intent(activity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                EventBus.getDefault().post(MessageEvent(Gson().toJson(data)))
                intent.putExtra("musicIndex", finalPosition)
                activity.startActivity(intent)
                activity.finish()
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivCover: ImageView? = itemView.findViewById(R.id.iv_cover)
        var tvTitle: TextView? = itemView.findViewById(R.id.tv_title)
        var tvArtists: TextView? = itemView.findViewById(R.id.tv_artists)
        var llMain: LinearLayout? = itemView.findViewById(R.id.ll_main)
    }

    companion object {
        const val ITEM_TYPE_HEADER = 0
        const val ITEM_TYPE_CONTENT = 1
    }
}