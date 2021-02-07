package cn.wearbbs.music.util;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import cn.wearbbs.music.receiver.MediaButtonReceiver;

public class HeadSetUtil {

    private static HeadSetUtil headSetUtil;
    private OnHeadSetListener headSetListener = null;

    public static HeadSetUtil getInstance() {
        if (headSetUtil == null) {
            headSetUtil = new HeadSetUtil();
        }
        return headSetUtil;
    }

    /**
     * 设置耳机单击双击监听接口 必须在open前设置此接口，否则设置无效
     * @param headSetListener
     */
    public void setOnHeadSetListener(OnHeadSetListener headSetListener) {
        this.headSetListener = headSetListener;
    }

    /**
     * 为MEDIA_BUTTON 意图注册接收器（注册开启耳机线控监听, 请务必在设置接口监听之后再调用此方法，否则接口无效）
     * @param context
     */
    public void open(Context context) {
        if(headSetListener==null){
            throw new IllegalStateException("please set headSetListener");
        }
        AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        ComponentName name = new ComponentName(context.getPackageName(),
                MediaButtonReceiver.class.getName());
        audioManager.registerMediaButtonEventReceiver(name);
        Log.i("ksdinf", "open");
    }
    /**
     * 关闭耳机线控监听
     * @param context
     */
    public void close(Context context) {
        AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        ComponentName name = new ComponentName(context.getPackageName(),
                MediaButtonReceiver.class.getName());
        audioManager.unregisterMediaButtonEventReceiver(name);
    }
    /**
     * 删除耳机单机双击监听接口
     */
    public void delHeadSetListener() {
        this.headSetListener = null;
    }

    /**
     * 获取耳机单击双击接口
     *
     * @return
     */
    public OnHeadSetListener getOnHeadSetListener() {
        return headSetListener;
    }

    /**
     * 耳机按钮单双击监听
     */
    public interface OnHeadSetListener {
        /**
         * 单击触发,主线程。 此接口真正触发是在单击操作1秒后 因为需要判断1秒内是否仍监听到点击，有的话那就是双击了
         */
        public void onClick();
        /**
         * 双击触发，此接口在主线程，可以放心使用
         */
        public void onDoubleClick();
        /**
         * 三连击
         */
        public void onThreeClick();
    }
}

