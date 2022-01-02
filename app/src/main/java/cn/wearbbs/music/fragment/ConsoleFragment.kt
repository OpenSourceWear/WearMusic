package cn.wearbbs.music.fragment

import android.Manifest
import android.media.AudioManager
import android.widget.ProgressBar
import cn.jackuxl.api.SongApi
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import cn.wearbbs.music.R
import cn.wearbbs.music.application.MainApplication
import cn.wearbbs.music.util.SharedPreferencesUtil

import cn.jackuxl.api.MusicListApi.*
import android.annotation.SuppressLint
import cn.wearbbs.music.util.ToastUtil
import android.content.Intent
import cn.wearbbs.music.ui.CommentActivity
import cn.jackuxl.api.MVApi.*
import android.content.ComponentName
import cn.wearbbs.music.ui.QRCodeActivity
import org.greenrobot.eventbus.EventBus
import cn.wearbbs.music.event.MessageEvent
import cn.wearbbs.music.ui.PlayListActivity
import android.os.Build
import android.content.pm.PackageManager
import android.app.ProgressDialog
import android.content.Context
import cn.wearbbs.music.util.DownloadUtil
import cn.wearbbs.music.util.DownloadUtil.OnDownloadListener
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import cn.jackuxl.api.MVApi
import cn.jackuxl.api.MusicListApi
import cn.wearbbs.music.databinding.FragmentConsoleBinding
import com.bumptech.glide.Glide
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import java.io.*


class ConsoleFragment : Fragment(), View.OnClickListener {
    private lateinit var binding: FragmentConsoleBinding
    private lateinit var audioManager: AudioManager
    private lateinit var pbMain: ProgressBar
    private lateinit var songApi: SongApi

    private var max = 0
    private var orderId = 0
    private var data: JSONArray? = null
    private var liked = false
    private var local: Boolean? = null
    private var cookie: String? = null
    private var artistName: String? = null

    private var currentMusicInfo: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentConsoleBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_console, container, false)
        data = PlayerFragment.getData()
        if (arguments != null && data != null && data!!.size > 0) {
            // 初始化
            audioManager = MainApplication.getContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
            max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            local = arguments?.getBoolean("local", false)
            orderId = PlayerFragment.getPlayOrder()
            binding.ivRepeat.setOnClickListener(this)
            when (orderId) {
                PlayerFragment.PLAY_ORDER -> binding.ivRepeat.setImageResource(R.drawable.icon_play_order)
                PlayerFragment.PLAY_REPEAT_ONE -> binding.ivRepeat.setImageResource(R.drawable.icon_play_repeat_one)
                PlayerFragment.PLAY_SHUFFLE -> {}
            }
            cookie = SharedPreferencesUtil.getString("cookie", "")
            songApi = SongApi(cookie)
            currentMusicInfo = arguments?.getInt("musicIndex", 0)?.let { data?.getJSONObject(it) }
            if (currentMusicInfo?.containsKey("simpleSong") == true) {
                currentMusicInfo = currentMusicInfo?.getJSONObject("simpleSong")
            }
            artistName = if (currentMusicInfo?.containsKey("artists") == true) {
                if (local == true) {
                    currentMusicInfo?.getString("artists")
                } else {
                    currentMusicInfo?.getJSONArray("artists")?.getJSONObject(0)?.getString("name")
                }
            } else if (currentMusicInfo?.containsKey("ar") == true) {
                currentMusicInfo?.getJSONArray("ar")?.getJSONObject(0)?.getString("name")
            } else {
                getString(R.string.unknown)
            }
            pbMain = requireActivity().findViewById(R.id.pb_main)
            if (currentMusicInfo?.containsKey("id") == true) {
                Thread {
                    try {
                        val api = MusicListApi(
                            SharedPreferencesUtil.getJSONObject("profile").getString("userId"),
                            cookie
                        )
                        val ids: Array<String> = api.getMusicListDetail(
                            api.getMusicList().getJSONObject(0).getString("id")
                        )
                        for (id in ids) {
                            if (id == currentMusicInfo?.getString("id")) {
                                liked = true
                            }
                        }
                        requireActivity().runOnUiThread {
                            binding.ivLike.apply {
                                if(liked){
                                    setImageResource(R.drawable.ic_baseline_favorite_24)
                                }
                                else{
                                    setImageResource(R.drawable.ic_baseline_favorite_border_24)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            }


            // 点击事件

            binding.apply {
                ivVoiceDown.setOnClickListener(this@ConsoleFragment)
                ivVoiceUp.setOnClickListener(this@ConsoleFragment)
                ivDownload.setOnClickListener(this@ConsoleFragment)
                ivComment.setOnClickListener(this@ConsoleFragment)
                ivShare.setOnClickListener(this@ConsoleFragment)
                ivMv.setOnClickListener(this@ConsoleFragment)
                ivPlaylist.setOnClickListener(this@ConsoleFragment)
                ivLike.setOnClickListener(this@ConsoleFragment)
            }
        }
        return view
    }

    @SuppressLint("NonConstantResourceId")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.iv_voiceUp -> {
                pbMain.max = max
                val value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (value == max) {
                    ToastUtil.show(requireActivity(), "媒体音量已到最高")
                } else {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value + 1, 0) //音乐音量
                    pbMain.progress = value + 1
                }
            }
            R.id.iv_voiceDown -> {
                pbMain.max = max
                val value = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (value == 0) {
                    ToastUtil.show(requireActivity(), "媒体音量已到最低")
                } else {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value - 1, 0) //音乐音量
                    pbMain.progress = value - 1
                }
            }
            R.id.iv_like -> Thread {
                try {
                    if (liked) {
                        if (songApi.likeMusic(currentMusicInfo?.getString("id"), false)) {
                            liked = false
                        } else {
                            ToastUtil.show(requireActivity(), "取消收藏失败")
                        }
                    } else {
                        if (songApi.likeMusic(currentMusicInfo?.getString("id"), true)) {
                            liked = true
                        } else {
                            ToastUtil.show(requireActivity(), "收藏失败")
                        }
                    }
                } catch (e: Exception) {
                    ToastUtil.show(requireActivity(), "收藏失败")
                }
                requireActivity().runOnUiThread {
                    val like_view = requireView().findViewById<ImageView>(R.id.iv_like)
                    if (liked) {
                        like_view.setImageResource(R.drawable.ic_baseline_favorite_24)
                    } else {
                        like_view.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                    }
                }
            }.start()
            R.id.iv_download -> if (local == true) {
                ToastUtil.show(requireActivity(), "已下载")
            } else {
                checkPermissionForDownload()
            }
            R.id.iv_comment -> if (currentMusicInfo?.getString("id") == null) {
                ToastUtil.show(requireActivity(), "本地音乐暂不支持评论")
            } else {
                startActivity(
                    Intent(requireActivity(), CommentActivity::class.java).putExtra(
                        "id",
                        currentMusicInfo?.getString("id")
                    )
                )
            }
            R.id.iv_mv -> {
                if (local!!) {
                    ToastUtil.show(requireActivity(), "本地音乐暂不支持播放MV")
                    return
                }
                val mvId: String? =
                    if (currentMusicInfo?.containsKey("mv") == true)
                        currentMusicInfo?.getString("mv")
                    else
                        currentMusicInfo?.getString("mvid")
                if (mvId == null || mvId.isEmpty()) {
                    ToastUtil.show(requireActivity(), "当前音乐无对应MV")
                } else {
                    Thread {
                        val mvUrl: String? = MVApi(cookie).getMVUrl(mvId)
                        if (mvUrl == null) {
                            ToastUtil.show(requireActivity(), "当前音乐无对应MV")
                        } else {
                            if (SharedPreferencesUtil.getString(
                                    "video_player",
                                    "WristVideo"
                                ) == "WristButlerPro"
                            ) {
                                val intent = Intent()
                                intent.putExtra("url", mvUrl)
                                intent.putExtra("title", currentMusicInfo!!.getString("name"))
                                try {
                                    intent.setClassName("com.cn.awg.pro", "com.cn.awg.pro.g2")
                                    startActivity(intent)
                                    PlayerFragment.pauseMusic()
                                } catch (e: Exception) {
                                    ToastUtil.show(
                                        requireActivity(),
                                        "视频播放器 腕管Pro 启动失败，请检查是否已安装该应用"
                                    )
                                    e.printStackTrace()
                                }
                            } else {
                                val intent = Intent()
                                intent.putExtra("mode", 1)
                                intent.putExtra("url", mvUrl)
                                intent.putExtra("url_backup", mvUrl)
                                intent.putExtra("title", currentMusicInfo!!.getString("name"))
                                intent.putExtra("identity_name", "WearMusic")
                                try {
                                    intent.component = ComponentName(
                                        "cn.luern0313.wristvideoplayer",
                                        "cn.luern0313.wristvideoplayer.ui.PlayerActivity"
                                    )
                                    startActivity(intent)
                                    PlayerFragment.pauseMusic()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    try {
                                        intent.component = ComponentName(
                                            "cn.luern0313.wristvideoplayer_free",
                                            "cn.luern0313.wristvideoplayer_free.ui.PlayerActivity"
                                        )
                                        startActivity(intent)
                                        PlayerFragment.pauseMusic()
                                    } catch (ee: Exception) {
                                        ToastUtil.show(
                                            requireActivity(),
                                            "视频播放器 腕上视频 启动失败，请检查是否已安装该应用"
                                        )
                                    }
                                }
                            }
                        }
                    }.start()
                }
            }
            R.id.iv_share -> startActivity(
                Intent(
                    requireActivity(),
                    QRCodeActivity::class.java
                ).putExtra(
                    "url",
                    "https://music.163.com/#/song?id=" + currentMusicInfo!!.getString("id")
                )
            )
            R.id.iv_repeat -> {
                val intent = Intent()
                val iv_repeat = requireView().findViewById<ImageView>(R.id.iv_repeat)
                when (orderId) {
                    PlayerFragment.PLAY_ORDER -> {
                        // 处于顺序播放模式
                        orderId = PlayerFragment.PLAY_REPEAT_ONE
                        intent.putExtra("orderId", PlayerFragment.PLAY_REPEAT_ONE)
                        iv_repeat.setImageResource(R.drawable.icon_play_repeat_one)
                    }
                    PlayerFragment.PLAY_REPEAT_ONE -> {
                        // 处于单曲循环模式
                        orderId = PlayerFragment.PLAY_ORDER
                        intent.putExtra("orderId", PlayerFragment.PLAY_ORDER)
                        iv_repeat.setImageResource(R.drawable.icon_play_order)
                    }
                    PlayerFragment.PLAY_SHUFFLE -> {}
                }
                PlayerFragment.setPlayOrder(orderId)
            }
            R.id.iv_playlist -> {
                EventBus.getDefault().postSticky(MessageEvent(data!!.toJSONString()))
                startActivity(
                    Intent(requireActivity(), PlayListActivity::class.java)
                        .putExtra("local", local)
                )
            }
        }
    }

    private fun checkPermissionForDownload() {
        // 读取权限
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= 23) {
            // 检查权限是否已授权
            val hasPermission = MainApplication.getContext().checkSelfPermission(permission)
            // 如果没有授权
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                // 请求权限
                requestPermissions(arrayOf(permission), 0)
            } else {
                // 已授权权限
                downloadCurrentMusic()
            }
        } else {
            downloadCurrentMusic()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty()) { //grantResults 数组中存放的是授权结果
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 同意授权
                downloadCurrentMusic()
            } else {
                // 拒绝授权
                ToastUtil.show(requireActivity(), getString(R.string.permission_denied))
            }
        }
    }

    private lateinit var progressDialog: ProgressDialog
    private fun downloadCurrentMusic() {
        val fileName =
            currentMusicInfo?.getString("name") + "--" + if (artistName == null) getString(R.string.unknown) else artistName
        val rootPath =
            MainApplication.getContext().getExternalFilesDir(null).toString() + "/download"
        Thread {

            // 保存封面
            val albumId: Int? = if (currentMusicInfo?.containsKey("al") == true) {
                currentMusicInfo?.getJSONObject("al")?.getInteger("id")
            } else {
                currentMusicInfo?.getJSONObject("album")?.getInteger("id")
            }
            savePicture(songApi.getSongCover(albumId?:0), "$rootPath/cover/", "$fileName.png")

            // 保存歌词
            val lrcDir = File("$rootPath/lrc/")
            if (!lrcDir.exists()) {
                lrcDir.mkdirs()
            }
            try {
                val out = BufferedWriter(FileWriter("$rootPath/lrc/$fileName.lrc"))
                out.write(songApi.getMusicLyric(currentMusicInfo!!.getString("id")))
                out.close()
            } catch (ignored: Exception) {
            }

            // 保存id
            val idDir = File("$rootPath/id/")
            if (!idDir.exists()) {
                idDir.mkdirs()
            }
            try {
                val out = BufferedWriter(FileWriter("$rootPath/id/$fileName.txt"))
                out.write(currentMusicInfo!!.getString("id"))
                out.close()
            } catch (ignored: Exception) {
            }

            // 保存音乐
            val url: String = SongApi(cookie).getMusicUrl(currentMusicInfo!!.getString("id"))
                .replace("http://", "https://")
            requireActivity().runOnUiThread {
                progressDialog = ProgressDialog(requireActivity())
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                // 设置ProgressDialog 标题
                progressDialog.setTitle("提示")
                // 设置ProgressDialog 提示信息
                progressDialog.setMessage("当前下载进度:")
                // 设置ProgressDialog 是否可以按退回按键取消
                progressDialog.setCancelable(false)
                progressDialog.show()
                progressDialog.max = 100
            }
            DownloadUtil().download(
                url, "$rootPath/music",
                "$fileName.wav",
                object : OnDownloadListener {
                    override fun onDownloadSuccess(file: File) {
                        ToastUtil.show(requireActivity(), "下载成功")
                        requireActivity().runOnUiThread { progressDialog.dismiss() }
                    }

                    override fun onDownloading(progress: Int) {
                        Log.d("ConsoleActivity", "onDownloading: Progress $progress%")
                        requireActivity().runOnUiThread { progressDialog.progress = progress }
                    }

                    override fun onDownloadFailed(e: Exception) {
                        ToastUtil.show(requireActivity(), "下载失败")
                        requireActivity().runOnUiThread { progressDialog.dismiss() }
                    }
                }
            )
        }.start()
    }

    private fun savePicture(photoUrl: String?, path: String, fileName: String) {
        Thread {
            try {
                val bitmap = Glide.with(requireActivity())
                    .asBitmap()
                    .load(photoUrl)
                    .submit(512, 512).get()
                val dirFile = File(path)
                if (!dirFile.exists()) {
                    dirFile.mkdir()
                }
                val myCaptureFile = File(path + fileName)
                myCaptureFile.createNewFile()
                val bos = BufferedOutputStream(FileOutputStream(myCaptureFile))
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, bos)
                bos.flush()
                bos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    companion object {
        @JvmStatic
        fun newInstance(intent: Intent): ConsoleFragment {
            val fragment = ConsoleFragment()
            val args = Bundle()
            args.putInt("musicIndex", intent.getIntExtra("musicIndex", 0))
            args.putBoolean("local", intent.getBooleanExtra("local", false))
            fragment.arguments = args
            return fragment
        }
    }
}