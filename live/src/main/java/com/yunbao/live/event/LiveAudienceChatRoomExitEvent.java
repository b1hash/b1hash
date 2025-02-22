package com.yunbao.live.event;

import com.yunbao.live.bean.LiveAudienceFloatWindowData;
import com.yunbao.live.bean.LiveBean;

public class LiveAudienceChatRoomExitEvent {

    private final LiveBean mLiveBean;
    private final LiveAudienceFloatWindowData mLiveAudienceFloatWindowData;

    public LiveAudienceChatRoomExitEvent(LiveBean liveBean, LiveAudienceFloatWindowData liveAudienceFloatWindowData) {
        mLiveBean = liveBean;
        mLiveAudienceFloatWindowData = liveAudienceFloatWindowData;
    }

    public LiveBean getLiveBean() {
        return mLiveBean;
    }

    public LiveAudienceFloatWindowData getLiveAudienceAgoraData() {
        return mLiveAudienceFloatWindowData;
    }
}
