package com.yunbao.im.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.im.event.NotificiationClickEvent;

import org.greenrobot.eventbus.EventBus;

public class ImMsgNotifyActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(0);
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String ext = SpUtil.getInstance().getStringValue(SpUtil.TPNS_NOTIFY_EXT);
                L.e("腾讯TPNS----ImMsgNotifyActivity--->" + ext);
                if (TextUtils.isEmpty(ext)) {
                    if (!CommonAppConfig.getInstance().isLaunched()) {
                        RouteUtil.forwardLauncher();
                    }
                } else {
                    SpUtil.getInstance().setStringValue(SpUtil.TPNS_NOTIFY_EXT, "");
                    try {
                        JSONObject customData = JSON.parseObject(ext);
                        int type = customData.getIntValue("type");
                        if (type == Constants.PUSH_TYPE_LIVE) {//直播
                            String liveUserInfo = customData.getString("userinfo");
                            if (!CommonAppConfig.getInstance().isLaunched()) {
                                RouteUtil.forwardLauncher(type, liveUserInfo);
                            } else {
                                if(CommonAppConfig.getInstance().isLogin()){
                                    if (CommonAppConfig.getInstance().getTopActivityType() != Constants.PUSH_TYPE_LIVE) {
                                        EventBus.getDefault().post(new NotificiationClickEvent(type, liveUserInfo));
                                    }
                                }
                            }
                        } else if (type == Constants.PUSH_TYPE_MESSAGE) {//系统消息
                            if (!CommonAppConfig.getInstance().isLaunched()) {
                                RouteUtil.forwardLauncher(type, "");
                            } else {
                                if(CommonAppConfig.getInstance().isLogin()){
                                    if (CommonAppConfig.getInstance().getTopActivityType() != Constants.PUSH_TYPE_MESSAGE) {
                                        EventBus.getDefault().post(new NotificiationClickEvent(type, ""));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                finish();
            }
        },500);

    }


    @Override
    protected void onDestroy() {
        L.e("腾讯TPNS----ImMsgNotifyActivity--->onDestroy" );
        super.onDestroy();
    }
}
