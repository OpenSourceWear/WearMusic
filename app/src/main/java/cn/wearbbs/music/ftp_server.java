package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ftp_server extends AppCompatActivity {
    Boolean is_start = false;
    FtpServerFactory serverFactory;
    FtpServer server;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp_server);
        if (!AppCenter.isConfigured()) {
            AppCenter.start(getApplication(), "8903c5a2-a2a5-4244-a1c5-4373b001a565", Analytics.class, Crashes.class);
        }
        TextView textView = findViewById(R.id.textView);
        Toast.makeText(this,"该功能仍在测试，或许BUG较多，敬请谅解",Toast.LENGTH_SHORT).show();
        String temp = "用户名：WearMusic\n密码：WearMusic\n端口：2222\nIP：Unknow\n连接后默认自动进入歌曲储存位置";
        Thread myThread=new Thread(){//创建子线程
            @Override
            public void run() {
                while (true){
                    try{
                        textView.setText(temp.replace("Unknown",getLocalIPAddress()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        myThread.start();//启动线程
        try {
            init_();
        } catch (FtpException e) {
            Toast.makeText(this,"初始化失败",Toast.LENGTH_SHORT).show();
        }
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
    public static String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> mEnumeration = NetworkInterface
                    .getNetworkInterfaces(); mEnumeration.hasMoreElements();) {
                NetworkInterface intf = mEnumeration.nextElement();
                for (Enumeration<InetAddress> enumIPAddr = intf
                        .getInetAddresses(); enumIPAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIPAddr.nextElement();
                    // 如果不是回环地址
                    if (!inetAddress.isLoopbackAddress()) {
                        // 直接返回本地IP地址
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            System.err.print("error");
        }
        return null;
    }
}