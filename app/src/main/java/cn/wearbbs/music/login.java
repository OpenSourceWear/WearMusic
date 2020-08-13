package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSON;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.wearbbs.music.MainActivity.verifyStoragePermissions;

public class login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        verifyStoragePermissions(login.this);
    }
    public void login(View view) throws Exception {
        String text;
        EditText pe = findViewById(R.id.pe);
        EditText pw = findViewById(R.id.pw);
        String check = pe.getText().toString();
        if (checkEmail(check)){
            //创建一个线程池
            ExecutorService pool = Executors.newFixedThreadPool(2);
            //创建一个有返回值的任务
            Callable c1 = new LoginCallable(pe.getText().toString(),pw.getText().toString(),0);
            //执行任务并获取Future对象
            Future f1 = pool.submit(c1);
            //从Future对象上获取任务的返回值，并输出到控制台
            text = f1.get().toString();
            //关闭线程池
            pool.shutdown();
            Map maps = (Map)JSON.parse(text);
//            if ();
            if (maps.get("code").toString().equals("200")){
                File dir = new File("/sdcard/Android/data/cn.wearbbs.music");
                dir.mkdirs();
                File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
                user.createNewFile();
                File saver = new File("/sdcard/Android/data/cn.wearbbs.music/saver.txt");
                saver.createNewFile();

                FileOutputStream outputStream;
                outputStream = new FileOutputStream(user);
                Map profile = (Map)JSON.parse(maps.get("profile").toString());
                outputStream.write(profile.toString().getBytes());
                outputStream.close();


                FileOutputStream outputStream_2;
                outputStream_2 = new FileOutputStream(saver);
                String temp = "{first:\"" + pe.getText().toString() + "\"" + ",second:\"" + pw.getText().toString() + "\"}";
                outputStream_2.write(temp.getBytes());
                outputStream_2.close();

                Toast.makeText(this,"登录成功！",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(login.this, menu.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                startActivity(intent);
            }
            else{
                if(maps.get("code").toString().equals("400")){
                    Toast.makeText(this,"请填写手机号/邮箱",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this,maps.get("msg").toString(),Toast.LENGTH_SHORT).show();
                }
            }
        }
        else if(checkMobileNumber(check)){
            //创建一个线程池
            ExecutorService pool = Executors.newFixedThreadPool(2);
            //创建一个有返回值的任务
            Callable c1 = new LoginCallable(pe.getText().toString(),pw.getText().toString(),1);
            //执行任务并获取Future对象
            Future f1 = pool.submit(c1);
            //从Future对象上获取任务的返回值，并输出到控制台
            text = f1.get().toString();
            //关闭线程池
            pool.shutdown();
            Map maps = (Map)JSON.parse(text);
//            if ();
            if (maps.get("code").toString().equals("200")){
                File dir = new File("/sdcard/Android/data/cn.wearbbs.music");
                dir.mkdirs();
                File user = new File("/sdcard/Android/data/cn.wearbbs.music/user.txt");
                user.createNewFile();
                File saver = new File("/sdcard/Android/data/cn.wearbbs.music/saver.txt");
                saver.createNewFile();

                FileOutputStream outputStream;
                outputStream = new FileOutputStream(user);
                Map profile = (Map)JSON.parse(maps.get("profile").toString());
                outputStream.write(profile.toString().getBytes());
                outputStream.close();


                FileOutputStream outputStream_2;
                outputStream_2 = new FileOutputStream(saver);
                String temp = "{first:\"" + pe.getText().toString() + "\"" + ",second:\"" + pw.getText().toString() + "\"}";
                outputStream_2.write(temp.getBytes());
                outputStream_2.close();

                Toast.makeText(this,"登录成功！",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(login.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                startActivity(intent);
            }
            else{
                if(maps.get("code").toString().equals("400")){
                    Toast.makeText(this,"请填写手机号/邮箱",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this,maps.get("msg").toString(),Toast.LENGTH_SHORT).show();
                }
            }
        }
        else{
            Toast.makeText(this,"请填写手机号/邮箱",Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 验证邮箱
     *
     * @param email
     * @return
     */

    public static boolean checkEmail(String email) {
        boolean flag = false;
        try {
            String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
            Pattern regex = Pattern.compile(check);
            Matcher matcher = regex.matcher(email);
            flag = matcher.matches();
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }

    /**
     * 验证手机号码，11位数字，1开通，第二位数必须是3456789这些数字之一 *
     * @param mobileNumber
     * @return
     */
    public static boolean checkMobileNumber(String mobileNumber) {
        boolean flag = false;
        try {
            // Pattern regex = Pattern.compile("^(((13[0-9])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
            Pattern regex = Pattern.compile("^1[345789]\\d{9}$");
            Matcher matcher = regex.matcher(mobileNumber);
            flag = matcher.matches();
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;

        }
        return flag;
    }
    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url_str
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url_str, String param) {
        String result = "";
        try {
            //创建一个URL实例
            URL url = new URL(url_str + "?" + param);

            try {
                //通过URL的openStrean方法获取URL对象所表示的自愿字节输入流
                InputStream is = url.openStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");

                //为字符输入流添加缓冲
                BufferedReader br = new BufferedReader(isr);
                String data = br.readLine();//读取数据

                while (data != null) {//循环读取数据
                    result += data;
                    data = br.readLine();
                }

                br.close();
                isr.close();
                is.close();
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return "{\"msg\":\"登录失败，请检查网络\",\"code\":502,\"message\":\"登录失败，请检查网络\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"msg\":\"登录失败，请检查网络\",\"code\":502,\"message\":\"登录失败，请检查网络\"}";
        }

    }
    static class LoginCallable implements Callable {
        String pet;
        String pwt;
        int tyt;
        LoginCallable(String pe,String pw,int type) throws Exception {
            pet = pe;
            pwt = pw;
            tyt = type;
            call();
        }

        @Override
        public Object call() throws Exception {
            String jg;
            if (tyt == 1){
                jg = sendGet("https://musicapi.leanapp.cn/login/cellphone","phone=" + pet + "&password=" + pwt);
            }
            else{
                jg = sendGet("https://musicapi.leanapp.cn/login","email=" + pet + "&password=" + pwt);
            }
            return jg;
        }

    }
    public void menu(View view){
        Intent intent = new Intent(login.this, menu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
    }



}