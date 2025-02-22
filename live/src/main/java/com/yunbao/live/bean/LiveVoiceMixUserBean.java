package com.yunbao.live.bean;

/**
 * Created by http://www.yunbaokj.com on 2023/2/1.
 */
public class LiveVoiceMixUserBean {
    private final int mIndex;
    private final String mUid;
    private final String mStream;

    public LiveVoiceMixUserBean(int index,String uid, String stream) {
        mIndex=index;
        mUid = uid;
        mStream = stream;
    }

    public int getIndex() {
        return mIndex;
    }

    public String getUid() {
        return mUid;
    }

    public String getStream() {
        return mStream;
    }

}
