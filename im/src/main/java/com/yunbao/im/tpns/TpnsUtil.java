package com.yunbao.im.tpns;

import android.content.Context;
import android.text.TextUtils;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.Constants;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.LanguageUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by http://www.yunbaokj.com on 2022/12/9.
 */
public class TpnsUtil {

    private static final String TAG = "腾讯TPNS";

    public static void register(boolean isDebug) {
        Context context = CommonAppContext.getInstance();
        //是否允许应用内消息展示
        XGPushConfig.enableShowInMsg(context, false);
        XGPushConfig.enableDebug(context, isDebug);
        XGPushConfig.enablePullUpOtherApp(context, true); //联合保活能力 false关闭 true开启
//        XGPushConfig.enableAutoStart();
//		//开启第三方通道
//		XGPushConfig.enableOtherPush(this,true);
//		//小米通道
//		XGPushConfig.setMiPushAppId(this, "2882303761517672364");
//		XGPushConfig.setMiPushAppKey(this, "5271767210364");
//		//魅族通道
//		XGPushConfig.setMzPushAppId(this, "111888");
//		XGPushConfig.setMzPushAppKey(this, "484a31262cf24dc9b568b5445d9e0495");

        new Thread(new Runnable() {
            @Override
            public void run() {
                XGPushManager.registerPush(CommonAppContext.getInstance(), new XGIOperateCallback() {
                    @Override
                    public void onSuccess(Object data, int flag) {
                        L.e(TAG, "注册成功，设备token为：" + data);
//                        if (CommonAppConfig.getInstance().isLogin()) {
//                            bindPushAccount();
//                        }
                    }

                    @Override
                    public void onFail(Object data, int errCode, String msg) {
                        L.e(TAG, "注册失败，错误码：" + errCode + ",错误信息：" + msg);
                    }
                });
            }
        }).start();

    }


//    public static void unRegister() {
//        XGPushManager.unregisterPush(CommonAppContext.getInstance(), new XGIOperateCallback() {
//            @Override
//            public void onSuccess(Object data, int i) {
//                L.e(TAG, "反注册成功");
//            }
//
//            @Override
//            public void onFail(Object data, int errCode, String msg) {
//                L.e(TAG, "反注册失败，错误码：" + errCode + ",错误信息：" + msg);
//            }
//        });
//    }

    /**
     * 绑定账号
     */
    public static void bindPushAccount() {
        XGPushManager.AccountInfo accountInfo = new XGPushManager.AccountInfo(0, CommonAppConfig.getInstance().getUid());
        XGPushManager.upsertAccounts(CommonAppContext.getInstance(), Arrays.asList(accountInfo),
                new XGIOperateCallback() {
                    @Override
                    public void onSuccess(Object data, int i) {
                        L.e(TAG, "绑定账号成功");
                        changeTag();
                    }

                    @Override
                    public void onFail(Object o, int errCode, String msg) {
                        L.e(TAG, "绑定账号失败，错误码：" + errCode + ",错误信息：" + msg);
                    }
                }
        );
    }

    /**
     * 解绑账号
     */
    public static void clearPushAccount() {
        XGPushManager.clearAccounts(CommonAppContext.getInstance(), new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int i) {
                L.e(TAG, "解绑账号成功");
            }

            @Override
            public void onFail(Object o, int errCode, String msg) {
                L.e(TAG, "解定账号失败，错误码：" + errCode + ",错误信息：" + msg);
            }
        });
    }

    public static void changeTag() {
        String languageTag = "";
        String lang = LanguageUtil.getInstance().getLanguage();
        if (Constants.LANG_EN.equals(lang)) {
            languageTag = "en";
        } else {
            languageTag = "zh-cn";
        }
        if (!TextUtils.isEmpty(languageTag)) {
            Set<String> tagsSet = new HashSet<>(Arrays.asList(CommonAppConfig.getInstance().getUid(), languageTag));
            XGPushManager.clearAndAppendTags(CommonAppContext.getInstance(), "clearAndAppendTags", tagsSet, new XGIOperateCallback() {
                @Override
                public void onSuccess(Object o, int i) {
                    L.e(TAG, "绑定标签成功：");
                }

                @Override
                public void onFail(Object o, int i, String s) {
                    L.e(TAG, "绑定标签失败");
                }
            });
        }
    }

    public static void queryTags() {
        XGPushManager.queryTags(CommonAppContext.getInstance(), "queryTags", 0, 100, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object o, int i) {
                L.e(TAG, "查询标签成功：" + o);
            }

            @Override
            public void onFail(Object o, int i, String s) {

            }
        });
    }
}
