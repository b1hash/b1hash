package com.yunbao.live.bean;

/**
 * Created by http://www.yunbaokj.com on 2024/2/22.
 */
public class LiveAudienceFloatWindowData {
    private boolean mIsTxSDK;
    private String mAgoraToken;
    private int mLiveUid;
    private String mStream;
    private int mPkUid;//连麦主播的uid
    private int mLinkMicAudienceUid;//连麦观众的uid

    public LiveAudienceFloatWindowData(){

    }

    public boolean isTxSDK() {
        return mIsTxSDK;
    }

    public void setTxSDK(boolean txSDK) {
        mIsTxSDK = txSDK;
    }

    public String getAgoraToken() {
        return mAgoraToken;
    }

    public void setAgoraToken(String agoraToken) {
        mAgoraToken = agoraToken;
    }

    public int getLiveUid() {
        return mLiveUid;
    }

    public void setLiveUid(int liveUid) {
        mLiveUid = liveUid;
    }

    public String getStream() {
        return mStream;
    }

    public void setStream(String stream) {
        mStream = stream;
    }

    public int getPkUid() {
        return mPkUid;
    }

    public void setPkUid(int pkUid) {
        mPkUid = pkUid;
    }

    public int getLinkMicAudienceUid() {
        return mLinkMicAudienceUid;
    }

    public void setLinkMicAudienceUid(int linkMicAudienceUid) {
        mLinkMicAudienceUid = linkMicAudienceUid;
    }

    @Override
    public String toString() {
        return "LiveAudienceFloatWindowData{" +
                "mIsTxSDK=" + mIsTxSDK +
                ", mAgoraToken='" + mAgoraToken + '\'' +
                ", mLiveUid=" + mLiveUid +
                ", mStream='" + mStream + '\'' +
                ", mPkUid=" + mPkUid +
                ", mLinkMicAudienceUid=" + mLinkMicAudienceUid +
                '}';
    }
}
