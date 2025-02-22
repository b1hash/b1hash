package com.yunbao.phonelive;

import android.text.TextUtils;

import com.fm.openinstall.OpenInstall;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.config.IToastInterceptor;
import com.meihu.beautylibrary.MHSDK;
import com.mob.MobSDK;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.live.TXLiveBase;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.utils.DecryptUtil;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.UmengUtil;
import com.yunbao.im.tpns.TpnsUtil;
import com.yunbao.im.utils.ImMessageUtil;


/**
 * Created by cxf on 2017/8/3.
 */

public class AppContext extends CommonAppContext {

    private boolean mBeautyInited;

    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
        ToastUtils.setInterceptor(new IToastInterceptor() {
            @Override
            public boolean intercept(CharSequence charSequence) {
                return !CommonAppContext.getInstance().isFront();
            }
        });
        L.setDeBug(BuildConfig.DEBUG);
    }

    public static void initSdk() {
        CommonAppContext context = CommonAppContext.getInstance();
        if (BuildConfig.DEBUG) {
            L.e("应用签名：" + context.getAppSignature());
            //L.e("facebook散列秘钥------>" + context.getFacebookHashKey());
        }
        //腾讯云直播鉴权url
        String liveLicenceUrl = "http://license.vod2.myqcloud.com/license/v1/96c55a58/TXLiveSDK.licence";
        //腾讯云直播鉴权key
        String liveKey = "7410cc8a1fc40aa9";
        //腾讯云视频鉴权url
        String ugcLicenceUrl = "https://license.vod2.myqcloud.com/license/v2/1334243532_1/v_cube.license";
        //腾讯云视频鉴权key
        String ugcKey = "8d22e506d9f8235869485f670ddc32b6";
        TXLiveBase.getInstance().setDebug(BuildConfig.DEBUG);
        TXLiveBase.getInstance().setLicence(context, liveLicenceUrl, liveKey, ugcLicenceUrl, ugcKey);
        //初始化腾讯bugly
        CrashReport.initCrashReport(context);
        CrashReport.setAppVersion(context, CommonAppConfig.getInstance().getVersion());
        //初始化ShareSdk
        MobSDK.init(context);
        MobSDK.submitPolicyGrantResult(true);
        //初始化IM
        ImMessageUtil.getInstance().init();
        //初始化腾讯TPNS 移动推送
        TpnsUtil.register(BuildConfig.DEBUG);
        //初始化友盟统计
        UmengUtil.init(context, BuildConfig.DEBUG);
        //OpenInstall
        OpenInstall.init(context);
    }

    /**
     * 初始化美狐
     */
    public void initBeautySdk(String beautyAppId, String beautyKey) {
        if (!TextUtils.isEmpty(beautyAppId) && !TextUtils.isEmpty(beautyKey)) {
            if (!mBeautyInited) {
                mBeautyInited = true;
                if (CommonAppConfig.isYunBaoApp()) {
                    beautyAppId = DecryptUtil.decrypt(beautyAppId);
                    beautyKey = DecryptUtil.decrypt(beautyKey);
                }
                MHSDK.init(this, beautyAppId, beautyKey);
                CommonAppConfig.getInstance().setMhBeautyEnable(true);
                L.e("美狐初始化----AppId--->" + beautyAppId + "---AppKey--->" + beautyKey);
            }
        } else {
            CommonAppConfig.getInstance().setMhBeautyEnable(false);
        }
    }

    @Override
    public void startInitSdk() {
        initSdk();
    }

}
