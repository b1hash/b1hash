package com.yunbao.im.event;

/**
 * Created by http://www.yunbaokj.com on 2022/12/9.
 */
public class NotificiationClickEvent {
    private int mType;
    private String mData;

    public NotificiationClickEvent(int type, String data) {
        mType=type;
        mData = data;
    }

    public int getType() {
        return mType;
    }

    public String getData() {
        return mData;
    }
}
