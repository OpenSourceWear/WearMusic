package cn.wearbbs.music;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class choose_lrc extends AppCompatActivity {
    List arr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_lrc);
        File lrc = new File("/sdcard/Android/data/cn.wearbbs.music/temp/temp.lrc");
        arr = new ArrayList();
        try {
            BufferedReader in = new BufferedReader(new FileReader(lrc));
            while((in.readLine())!=null){//使用readLine方法，一次读一行
                arr.add(in.readLine());
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ListView lrcs = findViewById(R.id.lrcs);
        CustomAdapter adapter = new CustomAdapter();
/*        ArrayAdapter adapter = new ArrayAdapter(choose_lrc.this, R.layout.items_2, arr);*/
        lrcs.setAdapter(adapter);
    }
    private class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return arr.size();
        }

        @Override
        public String getItem(int position) {
            return arr.get(position).toString();
        }

        @Override
        public long getItemId(int position) {
            return arr.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.items_2, container, false);
            }

            ((TextView) convertView.findViewById(R.id.title))
                    .setText(getItem(position));
            return convertView;
        }
    }
}