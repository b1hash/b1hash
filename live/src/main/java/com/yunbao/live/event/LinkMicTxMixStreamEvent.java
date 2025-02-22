package com.yunbao.live.event;

/**
 * Created by cxf on 2019/3/25.
 * 腾讯连麦的时候 主播混流
 */

public class LinkMicTxMixStreamEvent {

    private int mType;
    private String mToUid;
    private String mToStream;

    public LinkMicTxMixStreamEvent(int type, String touid, String toStream) {
        mType = type;
        mToUid = touid;
        mToStream = toStream;
    }

    public int getType() {
        return mType;
    }

    public String getToUid() {
        return mToUid;
    }

    public String getToStream() {
        return mToStream;
    }
}
