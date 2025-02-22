package com.yunbao.common;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import android.util.Base64;

import com.yunbao.common.event.AppLifecycleEvent;
import com.yunbao.common.interfaces.AppLifecycleUtil;
import com.yunbao.common.utils.FloatWindowHelper;
import com.yunbao.common.utils.L;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;


/**
 * Created by cxf on 2017/8/3.
 */

public class CommonAppContext extends MultiDexApplication {

    private static CommonAppContext sInstance;
    private static Handler sMainThreadHandler;
    private int mCount;
    private boolean mFront;//是否前台


    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        sMainThreadHandler=new Handler();
        registerActivityLifecycleCallbacks();
    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(this);
        super.attachBaseContext(base);
    }

    public static CommonAppContext getInstance() {
        if (sInstance == null) {
            try {
                Class clazz = Class.forName("android.app.ActivityThread");
                Method method = clazz.getMethod("currentApplication", new Class[]{});
                Object obj = method.invoke(null, new Object[]{});
                if (obj != null && obj instanceof CommonAppContext) {
                    sInstance = (CommonAppContext) obj;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }



    public static void postDelayed(Runnable runnable, long delayMillis) {
        if (sMainThreadHandler != null) {
            sMainThreadHandler.postDelayed(runnable, delayMillis);
        }
    }

    public static void post(Runnable runnable) {
        if (sMainThreadHandler != null) {
            sMainThreadHandler.post(runnable);
        }
    }


    private void registerActivityLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mCount++;
                if (!mFront) {
                    mFront = true;
                    L.e("AppContext------->处于前台");
                    EventBus.getDefault().post(new AppLifecycleEvent(true));
                    CommonAppConfig.getInstance().setFrontGround(true);
                    FloatWindowHelper.setFloatWindowVisible(true);
                    AppLifecycleUtil.onAppFrontGround();

                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                mCount--;
                if (mCount == 0) {
                    mFront = false;
                    L.e("AppContext------->处于后台");
                    EventBus.getDefault().post(new AppLifecycleEvent(false));
                    CommonAppConfig.getInstance().setFrontGround(false);
                    FloatWindowHelper.setFloatWindowVisible(false);
                    AppLifecycleUtil.onAppBackGround();
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    /**
     * 获取App签名md5值
     */
    public String getAppSignature() {
        try {
            PackageInfo info =
                    this.getPackageManager().getPackageInfo(this.getPackageName(),
                            PackageManager.GET_SIGNATURES);
            if (info != null) {
                Signature[] signs = info.signatures;
                byte[] bytes = signs[0].toByteArray();
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(bytes);
                bytes = md.digest();
                StringBuilder stringBuilder = new StringBuilder(2 * bytes.length);
                for (int i = 0; ; i++) {
                    if (i >= bytes.length) {
                        return stringBuilder.toString();
                    }
                    String str = Integer.toString(0xFF & bytes[i], 16);
                    if (str.length() == 1) {
                        str = "0" + str;
                    }
                    stringBuilder.append(str);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取facebook散列秘钥
     */
    public String getFacebookHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (Exception e) {
            return "get error";
        }
        return null;
    }

    public boolean isFront() {
        return mFront;
    }

    public void startInitSdk(){

    }
}
