package cn.wearbbs.music;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class lrc_adapter extends BaseAdapter {
    private List<String> listText;
    private Context context;
    private String choose = "";
    private Map<Integer,Boolean> map=new HashMap<>();
    public lrc_adapter(List<String> listText,Context context){
        this.listText=listText;
        this.context=context;
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

    public String getChoose() {
        return choose;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView==null){
            //通过一个打气筒 inflate 可以把一个布局转换成一个view对象
            view=View.inflate(context,R.layout.items_2,null);
        }else {
            view=convertView;//复用历史缓存对象
        }
        //单选按钮的文字
        TextView radioText=(TextView)view.findViewById(R.id.tv_check_text);
        radioText.setText(listText.get(position));
        //单选按钮
        final CheckBox checkBox=(CheckBox)view.findViewById(R.id.rb_check_button);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()){
                    map.put(position,true);
                    choose += listText.get(position) + "\n";

                }else {
                    map.remove(position);
                    choose.replace(listText.get(position) + "\n","");
                }
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
