package com.yunbao.im.tpns;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.im.event.ImUnReadCountEvent;
import com.yunbao.im.event.SystemMsgEvent;
import com.yunbao.im.utils.ImMessageUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by http://www.yunbaokj.com on 2022/12/9.
 */
public class TpnsReceiver extends XGPushBaseReceiver {
    @Override
    public void onRegisterResult(Context context, int i, XGPushRegisterResult xgPushRegisterResult) {

    }

    @Override
    public void onUnregisterResult(Context context, int i) {

    }

    @Override
    public void onSetTagResult(Context context, int i, String s) {
        L.e("腾讯TPNS", "设置标签------>" + s);
    }

    @Override
    public void onDeleteTagResult(Context context, int i, String s) {

    }

    @Override
    public void onSetAccountResult(Context context, int i, String s) {

    }

    @Override
    public void onDeleteAccountResult(Context context, int i, String s) {

    }

    @Override
    public void onSetAttributeResult(Context context, int i, String s) {

    }

    @Override
    public void onQueryTagsResult(Context context, int i, String s, String s1) {

    }

    @Override
    public void onDeleteAttributeResult(Context context, int i, String s) {

    }

    @Override
    public void onTextMessage(Context context, XGPushTextMessage xgPushTextMessage) {
        L.e("腾讯TPNS", "收到消息------>");
    }

    @Override
    public void onNotificationClickedResult(Context context, XGPushClickedResult xgPushClickedResult) {
        L.e("腾讯TPNS", "点击通知栏---->" + xgPushClickedResult.getCustomContent());
        SpUtil.getInstance().setStringValue(SpUtil.TPNS_NOTIFY_EXT, xgPushClickedResult.getCustomContent());
    }

    @Override
    public void onNotificationShowedResult(Context context, XGPushShowedResult xgPushShowedResult) {
        L.e("腾讯TPNS", "收到通知，显示通知栏---->" + xgPushShowedResult.getCustomContent());
        try {
            JSONObject customData = JSON.parseObject(xgPushShowedResult.getCustomContent());
            int type = customData.getIntValue("type");
            if (type == Constants.PUSH_TYPE_MESSAGE) {//系统消息
                int systemMsgCount = SpUtil.getInstance().getIntValue(SpUtil.SYSTEM_MSG_COUNT, 0);
                systemMsgCount++;
                SpUtil.getInstance().setIntValue(SpUtil.SYSTEM_MSG_COUNT, systemMsgCount);
                if (CommonAppConfig.getInstance().isLaunched()) {
                    EventBus.getDefault().post(new SystemMsgEvent());
                    EventBus.getDefault().post(new ImUnReadCountEvent());
                    ImMessageUtil.getInstance().playRing();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
