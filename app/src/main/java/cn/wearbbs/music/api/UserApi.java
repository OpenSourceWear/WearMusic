package cn.wearbbs.music.api;

import android.content.Context;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.wearbbs.music.util.NetWorkUtil;
import cn.wearbbs.music.util.UserInfoUtil;

public class UserApi {
    private String result;
    public Map checkLogin(String cookie) throws InterruptedException {
        Thread tmp = new Thread((Runnable)() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/login/status" + "?cookie=" + cookie);
        });
        tmp.start();
        tmp.join();
        return (Map)JSON.parse(result);
    }
    public Map Login(Context context,String name,String password) throws IOException {
        String text;
        if (checkEmail(name)){
            text = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/login?email=" + name + "&password=" + password + "&timestamp=" + System.currentTimeMillis());
            Map maps = (Map)JSON.parse(text);
            if(maps.containsKey("error") || maps.containsKey("msg") || maps.containsKey("message")){
                return maps;
            }
            else if (maps.get("code").toString().equals("200")){
                File dir = new File("/storage/emulated/0/Android/data/cn.wearbbs.music");
                dir.mkdirs();
                File user = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/user.txt");
                user.createNewFile();
                File saver = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/saver.txt");
                saver.createNewFile();

                FileOutputStream outputStream;
                outputStream = new FileOutputStream(user);
                Map profile = (Map)JSON.parse(maps.get("profile").toString());
                outputStream.write(profile.toString().getBytes());
                outputStream.close();


                FileOutputStream outputStream_2;
                outputStream_2 = new FileOutputStream(saver);
                String temp = "{first:\"" + name + "\"" + ",second:\"" + password + "\"}";
                outputStream_2.write(temp.getBytes());
                outputStream_2.close();

                UserInfoUtil.saveUserInfo(context,"cookie",maps.get("cookie").toString());
                return maps;
            }
            else{
                if(maps.get("code").toString().equals("400")){
                    result = "{\"error\":\"请填写手机号/邮箱\"}";
                    return (Map)JSON.parse(result);
                }
                else{
                    return (Map)JSON.parse("{\"error\":\"" + maps.get("msg").toString() + "\"");
                }
            }
        }
        else if(checkMobileNumber(name)){
            text = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/login/cellphone?phone=" + name + "&password=" + password + "&timestamp=" + System.currentTimeMillis());
            Map maps = (Map)JSON.parse(text);
            if(maps.containsKey("error") || maps.containsKey("msg") || maps.containsKey("message")){
                return maps;
            }
            else if (maps.get("code").toString().equals("200")){
                File dir = new File("/storage/emulated/0/Android/data/cn.wearbbs.music");
                dir.mkdirs();
                File user = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/user.txt");
                user.createNewFile();
                File saver = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/saver.txt");
                saver.createNewFile();

                FileOutputStream outputStream;
                outputStream = new FileOutputStream(user);
                Map profile = (Map)JSON.parse(maps.get("profile").toString());
                outputStream.write(profile.toString().getBytes());
                outputStream.close();


                FileOutputStream outputStream_2;
                outputStream_2 = new FileOutputStream(saver);
                String temp = "{first:\"" +name + "\"" + ",second:\"" + password + "\"}";
                outputStream_2.write(temp.getBytes());
                outputStream_2.close();

                UserInfoUtil.saveUserInfo(context,"cookie",maps.get("cookie").toString());
                return maps;
            }
            else{
                if(maps.get("code").toString().equals("400")){
                    result = "{\"error\":\"请填写手机号/邮箱\"}";
                    return (Map)JSON.parse(result);
                }
                else{
                    return (Map)JSON.parse("{\"error\":\"" + maps.get("msg").toString() + "\"");
                }
            }
        }
        else{
            result = "{\"error\":\"请填写手机号/邮箱\"}";
            return (Map)JSON.parse(result);
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
}
