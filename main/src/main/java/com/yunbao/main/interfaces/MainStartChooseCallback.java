package com.yunbao.main.interfaces;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by cxf on 2018/9/29.
 */

public interface MainStartChooseCallback {
    void onLiveClick(JSONObject startLiveInfo);

    void onVideoClick();

    void onVoiceClick(JSONObject startLiveInfo);

    /**
     * 录屏直播
     */
    void onScreenRecordLive(JSONObject startLiveInfo);
}
