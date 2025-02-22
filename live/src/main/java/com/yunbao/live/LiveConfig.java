package com.yunbao.live;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.yunbao.common.CommonAppContext;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.live.bean.LiveConfigBean;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by cxf on 2018/10/7.
 * 直播相关的参数配置
 */

public class LiveConfig {

    public static LiveConfigBean getDefaultTxConfig() {
        LiveConfigBean bean = new LiveConfigBean();
        bean.setTargetResolution(1);
        bean.setTargetFps(15);
        bean.setTargetGop(1);
        bean.setVideoKBitrate(1200);
        bean.setVideoKBitrateMax(1500);
        bean.setVideoKBitrateMin(800);
        bean.setAudioKBitrate(48000);
        return bean;
    }

    /**
     * 获取手机相关信息，开播时候用到
     */
    public static String getSystemParams() {
        String sysParams = StringUtil.contact(
                android.os.Build.BRAND,//手机厂商
                "_",
                android.os.Build.MODEL,//手机型号
                "_",
                android.os.Build.VERSION.RELEASE,//系统版本号
                "_",
                getMemory(),
                "_",
                getNetworkType()
        );
        L.e("开播", "手机信息------> " + sysParams);
        return sysParams;
    }

    /**
     * 获取手机内存大小
     */
    public static String getMemory() {
        String MemorySize = "NONE";
        BufferedReader br = null;
        try {
            FileReader fileReader = new FileReader("/proc/meminfo");
            br = new BufferedReader(fileReader, 8192);
            String str = br.readLine();// 读取meminfo第一行，系统总内存大小
            if (!TextUtils.isEmpty(str)) {
                String[] array = str.split("\\s+");
                int totalRam = (int) Math.ceil(Double.valueOf(array[1]) / (1024 * 1024));
                MemorySize = StringUtil.contact(String.valueOf(totalRam), "GB");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return MemorySize;
    }

    /**
     * 获取当前网络类型
     **/
    public static String getNetworkType() {
        String netType = "NONE";
        ConnectivityManager manager = (ConnectivityManager) CommonAppContext.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = "WIFI";
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            TelephonyManager telephonyManager = (TelephonyManager) CommonAppContext.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
                    && !telephonyManager.isNetworkRoaming()) {
                netType = "4G";
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    && !telephonyManager.isNetworkRoaming()) {
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                    || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                    || nSubType == TelephonyManager.NETWORK_TYPE_CDMA
                    && !telephonyManager.isNetworkRoaming()) {
                netType = "2G";
            } else
                netType = "NO DISPLAY";
        }
        return netType;
    }

}
