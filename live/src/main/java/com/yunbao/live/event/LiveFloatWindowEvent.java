package com.yunbao.live.event;

import com.yunbao.live.bean.LiveBean;

public class LiveFloatWindowEvent {

    private final LiveBean mLiveBean;
    private final int mType;

    public LiveFloatWindowEvent(LiveBean liveBean, int type) {
        mLiveBean = liveBean;
        mType = type;
    }

    public LiveBean getLiveBean() {
        return mLiveBean;
    }

    public int getType() {
        return mType;
    }
}
