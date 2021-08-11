package cn.wearbbs.music.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import cn.wearbbs.music.R;

public class AddMusicActivity extends AppCompatActivity {
    Boolean isStarted = false;
    FtpServerFactory serverFactory;
    FtpServer server;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addmusic);
        TextView tv_hint_ftp = findViewById(R.id.tv_hint_ftp);
        tv_hint_ftp.setText(getString(R.string.hintForFtp).replace("Unknown", getIpAddress()));
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view){
        switch(view.getId()){
            case R.id.btn_ftp:
                try {
                    changeFtpStatus();
                } catch (FtpException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.main_title:
                finish();
                break;
        }
    }

    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                if (!netInterface.isLoopback() && !netInterface.isVirtual() && netInterface.isUp()){
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("FTPServer","IP地址获取失败" + e.toString());
        }
        return "";
    }

    public void changeFtpStatus() throws FtpException {
        Button btn = findViewById(R.id.btn_ftp);
        if(isStarted){
            server.stop();
            isStarted = false;
            btn.setText("开启");
        }
        else{
            if(!isMobile()){
                init();
                server.start();
                isStarted = true;
                btn.setText("关闭");
            }
            else{
                Toast.makeText(AddMusicActivity.this,"您正在使用流量，无法打开FTP",Toast.LENGTH_SHORT).show();
                Log.d("FTPServer","流量状态，打开失败");
            }
        }
    }

    public boolean isMobile() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            return false;
        }
        return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public void init() throws FtpException {
        serverFactory = new FtpServerFactory();
        server = serverFactory.createServer();
        //设置访问用户名和密码还有共享路径
        BaseUser baseUser = new BaseUser();
        baseUser.setName("WearMusic");
        baseUser.setPassword("WearMusic");
        File file = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/files/download/music/");
        if(!file.exists()){
            file.mkdirs();
        }
        baseUser.setHomeDirectory("/storage/emulated/0/Android/data/cn.wearbbs.music/files/download/music/");

        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        baseUser.setAuthorities(authorities);
        serverFactory.getUserManager().save(baseUser);

        ListenerFactory factory = new ListenerFactory();
        factory.setPort(2222); //设置端口号 非ROOT不可使用1024以下的端口
        serverFactory.addListener("default", factory.createListener());
    }

    @Override
    public void onDestroy() {
        if(isStarted){
            server.stop();
            isStarted = false;
        }
        super.onDestroy();
    }
}