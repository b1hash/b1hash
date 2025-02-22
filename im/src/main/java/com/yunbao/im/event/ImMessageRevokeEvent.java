package com.yunbao.im.event;

public class ImMessageRevokeEvent {

    private String mToUid;
    private String mMsgId;
    private boolean mSelf;//是否是自己撤回的

    public ImMessageRevokeEvent(String toUid, String msgId, boolean self) {
        mToUid = toUid;
        mMsgId = msgId;
        mSelf = self;
    }

    public String getToUid() {
        return mToUid;
    }

    public void setToUid(String toUid) {
        mToUid = toUid;
    }

    public String getMsgId() {
        return mMsgId;
    }

    public void setMsgId(String msgId) {
        mMsgId = msgId;
    }

    public boolean isSelf() {
        return mSelf;
    }

    public void setSelf(boolean self) {
        mSelf = self;
    }
}
