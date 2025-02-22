package com.yunbao.common.utils;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.yunbao.common.CommonAppConfig;

/**
 * Created by http://www.yunbaokj.com on 2022/12/10.
 * 友盟工具类
 */
public class UmengUtil {

    /**
     * 初始化
     */
    public static void init(Context context, boolean isDebug) {
        String umengAppkey = CommonAppConfig.getMetaDataString("UMENG_APPKEY");
        UMConfigure.preInit(context, umengAppkey, context.getPackageName());
        UMConfigure.setLogEnabled(isDebug);
        UMConfigure.submitPolicyGrantResult(context, true);
        UMConfigure.init(context, umengAppkey, context.getPackageName(), UMConfigure.DEVICE_TYPE_PHONE, "");
        // 选用AUTO页面采集模式
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
    }

    /**
     * 用户登录
     *
     * @param loginType 登录类型
     * @param uid       用户uid
     */
    public static void userLogin(String loginType, String uid) {
        MobclickAgent.onProfileSignIn(loginType, uid);
    }

    /**
     * 用户登出
     */
    public static void userLogout() {
        MobclickAgent.onProfileSignOff();
    }


}
