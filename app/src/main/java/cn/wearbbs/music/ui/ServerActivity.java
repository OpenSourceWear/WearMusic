package cn.wearbbs.music.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import cn.wearbbs.music.R;

public class ServerActivity extends SlideBackActivity {
    Boolean is_start = false;
    FtpServerFactory serverFactory;
    FtpServer server;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "9250a12d-0fa9-4292-99fc-9d09dcc32012", Analytics.class, Crashes.class);
        }
        TextView textView = findViewById(R.id.textView);
        Toast.makeText(this,"该功能仍在测试，或许BUG较多，敬请谅解",Toast.LENGTH_SHORT).show();
        String temp = "用户名：WearMusic\n密码：WearMusic\n端口：2222\nIP：" + getIpAddress() + "\n连接后默认自动进入歌曲储存位置";
        textView.setText(temp);
        try {
            init_();
        } catch (FtpException e) {
            Toast.makeText(this,"初始化失败",Toast.LENGTH_SHORT).show();
        }
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
            System.err.println("IP地址获取失败" + e.toString());
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
            server.start();
            is_start = true;
            btn.setText("关闭");
        }

    }
    public void init_() throws FtpException {
        serverFactory = new FtpServerFactory();
        server = serverFactory.createServer();
        //设置访问用户名和密码还有共享路径
        BaseUser baseUser = new BaseUser();
        baseUser.setName("WearMusic");
        baseUser.setPassword("WearMusic");
        File file = new File("/sdcard/Android/data/cn.wearbbs.music/download/music/");
        if(!file.exists()){
            file.mkdirs();
        }
        baseUser.setHomeDirectory("/sdcard/Android/data/cn.wearbbs.music/download/music/");

        List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        baseUser.setAuthorities(authorities);
        serverFactory.getUserManager().save(baseUser);

        ListenerFactory factory = new ListenerFactory();
        factory.setPort(2222); //设置端口号 非ROOT不可使用1024以下的端口
        serverFactory.addListener("default", factory.createListener());
    }
}