package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class user extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        String avatarUrl = intent.getStringExtra("avatarUrl");
        ImageView gi = findViewById(R.id.gi);
        RequestOptions requestOptions = RequestOptions.circleCropTransform();
        Glide.with(user.this).load(avatarUrl).apply(requestOptions).into(gi);
        TextView text = findViewById(R.id.text);
        text.setText(userName);
//        text.setText(userName + "\n粉丝：");
//        try {
//            init_fans();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
    public void menu(View view){
        Intent intent = new Intent(user.this, menu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
    }
    public void init_fans() throws Exception {
        String text;
        //创建一个线程池
        ExecutorService pool = Executors.newFixedThreadPool(2);
        //创建一个有返回值的任务
        Callable c1 = new LoginCallable_2();
        //执行任务并获取Future对象
        Future f1 = pool.submit(c1);
        //从Future对象上获取任务的返回值，并输出到控制台
        text = f1.get().toString();
        //关闭线程池
        pool.shutdown();
        Map maps = (Map)JSON.parse(text);
        System.out.println(maps.toString());
        Map profile = (Map)JSON.parse(maps.get("profile").toString());
        if (maps.get("code").toString().equals("200")){
            TextView textView = findViewById(R.id.text);
            String temp = textView.getText().toString()  + profile.get("followeds").toString() + "\n";
            textView.setText(temp);
        }
        else{
            Toast.makeText(this,"加载失败，请检查网络" ,Toast.LENGTH_SHORT).show();
        }
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
    static class LoginCallable_2 implements Callable {

        LoginCallable_2() throws Exception {
            call();
        }

        @Override
        public Object call() throws Exception {
            File saver = new File("/sdcard/Android/data/cn.wearbbs.music/saver.txt");
            BufferedReader in = new BufferedReader(new FileReader(saver));
            String jg = sendGet("https://musicapi.leanapp.cn/login/cellphone","phone=" + ((Map) JSON.parse(in.readLine())).get("first").toString() + "&password=" + ((Map) JSON.parse(in.readLine())).get("second").toString());
            return jg;
        }
    }
    public void logout(View view){
        if(delete("/sdcard/Android/data/cn.wearbbs.music")){
            Toast.makeText(this,"退出成功！",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(user.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
            startActivity(intent);
        }
        else{
            Toast.makeText(this,"退出失败，请检查文件权限或重试",Toast.LENGTH_SHORT).show();
        }
    }
    public boolean delete(String path){
        File file = new File(path);
        if(!file.exists()){
            return false;
        }
        if(file.isFile()){
            return file.delete();
        }
        File[] files = file.listFiles();
        for (File f : files) {
            if(f.isFile()){
                if(!f.delete()){
                    System.out.println(f.getAbsolutePath()+" delete error!");
                    return false;
                }
            }else{
                if(!this.delete(f.getAbsolutePath())){
                    return false;
                }
            }
        }
        return file.delete();
    }
}