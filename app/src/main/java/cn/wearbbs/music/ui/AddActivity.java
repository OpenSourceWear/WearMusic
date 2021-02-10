package cn.wearbbs.music.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

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

public class AddActivity extends SlideBackActivity {
    Boolean is_start = false;
    FtpServerFactory serverFactory;
    FtpServer server;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
        TextView textView = findViewById(R.id.textView);
        String temp = "用户名：WearMusic\n密码：WearMusic\n端口：2222\nIP：" + getIpAddress() + "\n连接后默认自动进入音乐储存位置";
        textView.setText(temp);
    }
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btnFTP:
                findViewById(R.id.choose).setVisibility(View.GONE);
                findViewById(R.id.FTP).setVisibility(View.VISIBLE);
                break;
            case R.id.btnSelf:
                findViewById(R.id.choose).setVisibility(View.GONE);
                findViewById(R.id.self).setVisibility(View.VISIBLE);
                break;
        }
    }
    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
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
    public void Start_Stop_Ftp(View view) throws FtpException {
        Button btn = findViewById(R.id.btn);
        if(is_start){
            server.stop();
            is_start = false;
            btn.setText("开启");
        }
        else{
            if(!isMobile()){
                init_();
                server.start();
                is_start = true;
                btn.setText("关闭");
            }
            else{
                Toast.makeText(AddActivity.this,"您正在使用流量，无法打开FTP",Toast.LENGTH_SHORT).show();
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
        if(activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        } else {
            return false;
        }
    }

    public void init_() throws FtpException {
        serverFactory = new FtpServerFactory();
        server = serverFactory.createServer();
        //设置访问用户名和密码还有共享路径
        BaseUser baseUser = new BaseUser();
        baseUser.setName("WearMusic");
        baseUser.setPassword("WearMusic");
        File file = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/download/music/");
        if(!file.exists()){
            file.mkdirs();
        }
        baseUser.setHomeDirectory("/storage/emulated/0/Android/data/cn.wearbbs.music/download/music/");

        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        baseUser.setAuthorities(authorities);
        serverFactory.getUserManager().save(baseUser);

        ListenerFactory factory = new ListenerFactory();
        factory.setPort(2222); //设置端口号 非ROOT不可使用1024以下的端口
        serverFactory.addListener("default", factory.createListener());
    }
}