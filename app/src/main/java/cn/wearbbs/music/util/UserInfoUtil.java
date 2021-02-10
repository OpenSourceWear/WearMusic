package cn.wearbbs.music.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class UserInfoUtil {
    public UserInfoUtil(){

    }
    public static boolean saveUserInfo(Context context, String filename, String content) {
        try {
            // 使用Android上下问获取当前项目的路径
            File file = new File(context.getFilesDir(), filename + ".txt");
            // 创建输出流对象
            FileOutputStream fos = new FileOutputStream(file);
            // 向文件中写入信息
            fos.write((content).getBytes());
            // 关闭输出流对象
            fos.close();
            return true;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
    public static String getUserInfo(Context context, String filename) {
        try {
            // 创建FIle对象
            File file = new File(context.getFilesDir(), filename + ".txt");
            // 创建FileInputStream对象
            FileInputStream fis = new FileInputStream(file);
            // 创建BufferedReader对象
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            // 获取文件中的内容
            String content = br.readLine();
            // 关闭流对象
            fis.close();
            br.close();
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}