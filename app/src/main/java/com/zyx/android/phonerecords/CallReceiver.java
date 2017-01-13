package com.zyx.android.phonerecords;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;


/**
 * 通过广播接收者接收呼入（状态改变的广播）、呼出（呼出电话的广播）电话时发出的广播
 * 再通过TelephonyManager的状态进行更细致的录音节点选择
 */
public class CallReceiver extends BroadcastReceiver {

    private SharedPreferences sp;
    private Context context;


    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        sp = context.getSharedPreferences("config",MODE_PRIVATE);
        
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            //拨出电话的广播——仅呼出时出现-设置呼出标记-获得呼出号码和呼出时间

            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isCall", true);
            String filename = getResultData() + "_D_"+ getCurrentTime();
            editor.putString("filename", filename);
            editor.commit();

            return;
        }else {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            MyPhoneStateListener listener = new MyPhoneStateListener();
            tm.listen(listener,PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    /**
     * 呼入、呼出电话监听器
     * 通过监听器开启服务，进行通话录音
     */
    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            sp =  context.getSharedPreferences("config",MODE_PRIVATE);

            switch (state){
                case TelephonyManager.CALL_STATE_IDLE://变为空闲时——关闭录音
                    stopRecorderService();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK://接听时-开启录音
                    startRecorderService();
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    //响铃——仅呼入时出现-设置呼入标记-获得呼入电话和呼入时间

                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("isCall",false);
                    //此处的filename可以继续优化，比如将incomingname变为姓名
                    String filename = incomingNumber +"_J_" +getCurrentTime() ;
                    editor.putString("filename",filename);
                    editor.commit();

                    break;
            }
        }
    }


    /**
     * 关闭RecorderService服务。
     * 即调用服务中onDestory方法-关闭录音
     */
    private void stopRecorderService() {
        Intent stopintent = new Intent(context,RecorderService.class);
        context.stopService(stopintent);
    }


    /**
     * 启动RecorderService服务。
     * 即调用服务中onCreate方法-开始录音
     */
    private void startRecorderService() {
        Intent intent = new Intent(context,RecorderService.class);
        context.startService(intent);
    }


    /**
     * 获得当前时区的时间。
     * @return
     * 返回yyyyMMddHHmmss类型的时间字符串
     */
    private String getCurrentTime() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss",Locale.getDefault());
        return formatter.format(currentTime);
    }


//    /**
//     * 查看服务是否正在运行
//     * @param className 要查询的服务的全类名
//     * @return
//     * true为正在运行，false为未运行
//     */
//    private boolean isServiceRunning(String className){
//        boolean result = false;
//
//        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> runningServiceInfos = am.getRunningServices(100);
//        for (ActivityManager.RunningServiceInfo runningServiceInfo :runningServiceInfos) {
//            String name = runningServiceInfo.service.getClassName();
//            if (name.equals(className)) {
//                result = true;
//            }
//        }
//        return result;
//    }





}
