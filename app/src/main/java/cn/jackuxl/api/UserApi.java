package cn.jackuxl.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.jackuxl.util.NetWorkUtil;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserApi {
    private JSONObject userInfo;
    private String password;
    private String phoneNumber;
    private String email;
    private String key;

    public UserApi() {
        userInfo = new JSONObject();
        password = null;
        phoneNumber = null;
        email = null;
        key = null;
    }

    /**
     * 登录
     * 自动识别已填写信息选择方式
     *
     * @return 状态码
     */
    public int login() {
        if (password == null) {
            return 401;
        }
        if (phoneNumber != null) {
            return loginByPhoneNumber();
        } else if (email != null) {
            return loginByEmail();
        } else {
            return 401;
        }
    }

    /**
     * 通过手机号登录
     * 请在登录前手动设置密码及手机号
     *
     * @return 状态码
     */
    public int loginByPhoneNumber() {
        if (password == null || phoneNumber == null) {
            return 401;
        }
        String result = NetWorkUtil.sendByGetUrl(String.format("/login/cellphone?phone=%s&md5_password=%s", phoneNumber, password), null);
        if (result == null) {
            return -1;
        }
        userInfo = JSON.parseObject(result);
        return userInfo.getInteger("code");
    }

    /**
     * 通过手机号登录
     * 请在登录前手动设置密码及邮箱
     *
     * @return 状态码
     */
    public int loginByEmail() {
        if (password == null || email == null) {
            return 401;
        }
        String result = NetWorkUtil.sendByGetUrl(String.format("/login?email=%s&md5_password=%s", email, password), null);
        if (result == null) {
            return -1;
        }
        userInfo = JSON.parseObject(result);
        return userInfo.getInteger("code");
    }

    /**
     * 设置手机号码
     * 请在使用手机登录前执行
     *
     * @param phoneNumber 手机号码（+86）
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * 设置邮箱
     * 请在使用邮箱登录前执行
     *
     * @param email 邮箱
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 设置密码
     * 请在登录前执行
     *
     * @param password 密码
     * @param md5      是否已进行md5加密（若为否则自动加密）
     */
    public void setPassword(String password, Boolean md5) {
        if (!md5) {
            this.password = encodeByMD5(password);
        } else {
            this.password = password;
        }
    }

    /**
     * 设置Cookie
     *
     * @param cookie cookie
     */
    public void setCookie(String cookie) {
        userInfo.put("cookie", cookie);
    }

    /**
     * 申请二维码密钥
     *
     * @return 是否获取成功
     */
    public Boolean getKey() {
        String result = NetWorkUtil.sendByGetUrl("/login/qr/key?timestamp=" + System.currentTimeMillis(), null);
        if (result == null || JSON.parseObject(result).getJSONObject("data").getString("unikey") == null) {
            return false;
        } else {
            key = JSON.parseObject(result).getJSONObject("data").getString("unikey");
            return true;
        }
    }

    /**
     * 创建二维码
     *
     * @return base64二维码
     */
    public String createQRCode() {
        String result = NetWorkUtil.sendByGetUrl(String.format("/login/qr/create?key=%s&qrimg=1&timestamp=" + System.currentTimeMillis(), key), null);
        if (result == null || JSON.parseObject(result).getJSONObject("data").getString("qrimg") == null) {
            return null;
        } else {
            return JSON.parseObject(result).getJSONObject("data").getString("qrimg");
        }
    }

    /**
     * 检查二维码状态
     *
     * @return 状态码
     */
    public int checkQRStatus() {
        String result = NetWorkUtil.sendByGetUrl(String.format("/login/qr/check?key=%s&timestamp=" + System.currentTimeMillis(), key), "");
        if (result == null) {
            return -1;
        } else {
            JSONObject tmp = JSON.parseObject(result);
            int code = JSON.parseObject(result).getInteger("code");
            if (code == 803) {
                userInfo.put("cookie", tmp.getString("cookie"));
            }
            return code;
        }
    }

    /**
     * 获取Cookie
     * 请先登录
     *
     * @return cookie
     */
    public String getCookie() {
        return userInfo.getString("cookie");
    }

    /**
     * 获取用户信息
     * 请先登录
     *
     * @return 用户信息
     */
    public JSONObject getProfile() {
        if (userInfo.getString("profile") == null) {
            return JSON.parseObject(NetWorkUtil.sendByGetUrl("/login/status", getCookie())).getJSONObject("data").getJSONObject("profile");
        }
        return userInfo.getJSONObject("profile");
    }

    public JSONObject checkLogin() {
        return JSON.parseObject(NetWorkUtil.sendByGetUrl("/login/status", getCookie())).getJSONObject("data");
    }

    public void logout() {
        NetWorkUtil.sendByGetUrl("/logout", getCookie());
    }

    /**
     * md5加密
     *
     * @param str 待加密字符串
     */
    public String encodeByMD5(String str) {
        byte[] secretBytes;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    str.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("md5 error");
        }
        StringBuilder md5code = new StringBuilder(new BigInteger(1, secretBytes).toString(16));
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code.insert(0, "0");
        }
        return md5code.toString();
    }

}
