package cn.wearbbs.music.api;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.wearbbs.music.detail.Data;
import cn.wearbbs.music.util.NetWorkUtil;
import cn.wearbbs.music.util.UserInfoUtil;

public class UserApi {
    private String result;
    public JSONObject checkLogin(String cookie) throws InterruptedException {
        Thread tmp = new Thread(() -> {
            result = NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/login/status" ,cookie);
        });
        tmp.start();
        tmp.join();
        return JSON.parseObject(result);
    }
    public JSONObject Login(Context context,String name,String password) throws IOException {
        JSONObject loginInfo;
        if (checkEmail(name)){
            loginInfo = JSON.parseObject(NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/login?email=" + name + "&password=" + password + "&timestamp=" + System.currentTimeMillis(),""));
        }
        else if(checkMobileNumber(name)){
            loginInfo = JSON.parseObject(NetWorkUtil.sendByGetUrl("https://music.wearbbs.cn/login/cellphone?phone=" + name + "&password=" + password + "&timestamp=" + System.currentTimeMillis(),""));
        }
        else{
            return JSON.parseObject("{\"error\":\"请填写手机号/邮箱\"}");
        }

        if(loginInfo.containsKey("error") || loginInfo.containsKey("msg") || loginInfo.containsKey("message")){
            return loginInfo;
        }
        else if (loginInfo.getInteger("code") == Data.successCode){
            File dir = new File("/storage/emulated/0/Android/data/cn.wearbbs.music");
            dir.mkdirs();
            File user = new File("/storage/emulated/0/Android/data/cn.wearbbs.music/user.txt");
            user.createNewFile();

            FileOutputStream outputStream;
            outputStream = new FileOutputStream(user);
            JSONObject profile = loginInfo.getJSONObject("profile");
            outputStream.write(profile.toString().getBytes());
            outputStream.close();

            UserInfoUtil.saveUserInfo(context,"account",name);
            UserInfoUtil.saveUserInfo(context,"password",password);
            UserInfoUtil.saveUserInfo(context,"cookie",loginInfo.getString("cookie"));
            return loginInfo;
        }
        else{
            if(loginInfo.getInteger("code") == Data.errorCode){
                result = "{\"error\":\"请填写手机号/邮箱\"}";
                return loginInfo;
            }
            else{
                return JSON.parseObject("{\"error\":\"" + loginInfo.getString("msg") + "\"");
            }
        }
    }
    /**
     * 验证邮箱
     *
     * @param email 邮箱
     * @return 验证结果
     */

    public static boolean checkEmail(String email) {
        boolean flag;
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
     * @param mobileNumber 手机号
     * @return 验证结果
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
