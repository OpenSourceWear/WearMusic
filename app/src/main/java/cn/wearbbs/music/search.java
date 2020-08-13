package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class search extends AppCompatActivity {
    List arr;
    String temp_hl;
    Callable c1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        LinearLayout list_layout = findViewById(R.id.list_layout);
        LinearLayout null_layout = findViewById(R.id.null_layout);
        list_layout.setVisibility(View.VISIBLE);
        null_layout.setVisibility(View.GONE);
        String temp = "[]";
        arr = JSON.parseArray(temp);
    }
    public void menu(View view){
        Intent intent = new Intent(search.this, menu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
        startActivity(intent);
    }
    public void search(View view) throws Exception {
        String text;
        EditText editText = findViewById(R.id.editText);
        //创建一个线程池
        ExecutorService pool = Executors.newFixedThreadPool(2);
        //创建一个有返回值的任务
        try{
            c1 = new LoginCallable_3(editText.getText().toString());
            //执行任务并获取Future对象
            Future f1 = pool.submit(c1);
            //从Future对象上获取任务的返回值，并输出到控制台
            text = f1.get().toString();
            //关闭线程池
            pool.shutdown();
            Map maps = (Map)JSON.parse(text);
            Map result = (Map)JSON.parse(maps.get("result").toString());
            String song_name;
            try{
                song_name = result.get("songs").toString();
                List songsList = JSONObject.parseArray(song_name);
                List<Map<String, Object>> idItems = new ArrayList<Map<String, Object>>();
                for (int i = 0; i < songsList.size(); i++ ) {
                    Map<String, Object> idItem = new HashMap<String, Object>();
                    Map nm = (Map)JSON.parse(songsList.get(i).toString());
                    List ar_temp = JSON.parseArray(nm.get("artists").toString());
                    Map ar = (Map)JSON.parse(ar_temp.get(0).toString());
                    idItem.put("name", nm.get("name"));
                    idItem.put("id", nm.get("id"));
                    idItem.put("artists", ar.get("name"));
                    idItems.add(idItem);
                }
                String jsonString = JSON.toJSONString(idItems);
                refresh_list(songsList,jsonString);
            } catch (Exception e) {
                LinearLayout list_layout = findViewById(R.id.list_layout);
                LinearLayout null_layout = findViewById(R.id.null_layout);
                list_layout.setVisibility(View.GONE);
                null_layout.setVisibility(View.VISIBLE);
                e.printStackTrace();
            }
        } catch (Exception e) {
            Toast.makeText(this,"请输入搜索内容",Toast.LENGTH_SHORT).show();
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
                return "{\"msg\":\"搜索失败，请检查网络\",\"code\":502,\"message\":\"搜索失败，请检查网络\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"msg\":\"搜索失败，请检查网络\",\"code\":502,\"message\":\"搜索失败，请检查网络\"}";
        }
    }
    static class LoginCallable_3 implements Callable {
        String sts;
        LoginCallable_3(String st) throws Exception {
            call();
            sts = st;
        }

        @Override
        public Object call() throws Exception {
            String jg = sendGet("https://musicapi.leanapp.cn/search","keywords=" + sts);
            return jg;
        }
    }
    public void refresh_list(final List search_list, final String idl){
        for (int i = 0; i < search_list.size(); i++ ) {
            Map maps = (Map)JSON.parse(search_list.get(i).toString());
            List ar_temp = JSON.parseArray(maps.get("artists").toString());
            Map ar = (Map)JSON.parse(ar_temp.get(0).toString());
            temp_hl = "<font color='#FFFFFF'>" + maps.get("name").toString() +  "</font> - " + "<font color='#999999'>" + ar.get("name").toString() + "</font>";
            arr.add(temp_hl);
        }
        ArrayAdapter adapter = new ArrayAdapter(search.this, R.layout.items, arr){
            public Object getItem(int position)
            {
                return Html.fromHtml(arr.get(position).toString());
            }
        };
//        SimpleAdapter sampleAdapter = new SimpleAdapter(this
//                , listItems
//                , R.layout.items
//                , new String[] {"img", "name"}
//                , new int[] { R.id.image, R.id.title}
//        );
        ListView list = findViewById(R.id.list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(search.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//刷新
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);//防止重复
                intent.putExtra("type", "0");
                intent.putExtra("list", idl);
                intent.putExtra("start",String.valueOf(i));
                startActivity(intent);
            }
        });
        list.setAdapter(adapter);
    }
}