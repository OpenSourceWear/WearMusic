package cn.wearbbs.music.util;

import android.app.DownloadManager;
import android.net.Uri;
import android.util.Log;

public class DownloadUtil {
    public DownloadUtil(){

    }
    public Long download(String url, String path, String name, DownloadManager downloadManager) {
        //创建下载任务,downloadUrl就是下载链接
        Log.d("WearMusic", "download: " + url);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //指定下载路径和下载文件名
        request.setDestinationInExternalPublicDir(path, name);
        //将下载任务加入下载队列，否则不会进行下载
        long mTaskId = downloadManager.enqueue(request);
        System.out.println(url);
        return mTaskId;
    }
}