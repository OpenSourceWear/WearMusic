package cn.wearbbs.music.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.wearbbs.music.R;
public class ChooseAdapter extends BaseAdapter {
    private List<String> listText;
    private Context context;
    private List choose;
    private List positions;
    Map Keymap = new HashMap();
    private Map<Integer,Boolean> map=new HashMap<>();
    public ChooseAdapter(List<String> listText,Context context){
        this.listText=listText;
        this.context=context;
        choose = new ArrayList();
        positions = new ArrayList();
    }
    @Override
    public int getCount() {
        //return返回的是int类型，也就是页面要显示的数量。
        return listText.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public List getChoose() {
        choose = new ArrayList();
        Collections.sort(positions);
        for (int i = 0;i<positions.size();i++){
            choose.add(listText.get(Integer.parseInt(positions.get(i).toString())));
        }
        Log.d("Choose",positions.toString());
        return choose;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView==null){
            //通过一个打气筒 inflate 可以把一个布局转换成一个view对象
            view=View.inflate(context,R.layout.item_choose,null);
        }else {
            view=convertView;//复用历史缓存对象
        }
        //单选按钮的文字
        TextView radioText= view.findViewById(R.id.tv_check_text);
        radioText.setText(listText.get(position));
        //单选按钮
        final CheckBox checkBox= view.findViewById(R.id.rb_check_button);
        checkBox.setOnClickListener(v -> {
            if (checkBox.isChecked()){
                map.put(position,true);
                positions.add(position);
                Keymap.put(position,positions.size()-1);
            }else {
                map.remove(position);
                positions.remove(Keymap.get(position));

            }
        });
        if(map!=null&&map.containsKey(position)){
            checkBox.setChecked(true);
        }else {
            checkBox.setChecked(false);
        }
        return view;
    }
}
