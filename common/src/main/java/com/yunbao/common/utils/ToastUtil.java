package com.yunbao.common.utils;

import android.text.TextUtils;

import com.hjq.toast.ToastUtils;

/**
 * Created by cxf on 2017/8/3.
 */

public class ToastUtil {

    //    private static Toast sToast;
    private static long sLastTime;
    private static String sLastString;


    public static void show(int res) {
        show(WordUtil.getString(res));
    }

    public static void show(String s) {
        try {
            if (TextUtils.isEmpty(s)) {
                return;
            }
            long curTime = System.currentTimeMillis();
            if (curTime - sLastTime > 2000) {
                sLastTime = curTime;
                sLastString = s;
                ToastUtils.show(s);

            } else {
                if (!s.equals(sLastString)) {
                    sLastTime = curTime;
                    sLastString = s;

                    ToastUtils.show(s);

                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
