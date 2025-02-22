package com.yunbao.im.bean;

/**
 * Created by http://www.yunbaokj.com on 2022/12/7.
 */
public class ImConversationBean {
    private boolean mIsGroup;//是否是群聊
    private String mConversationID;
    private String mUserID;
    private String mGroupID;
    private String mLastMsgId;
    private String mLastMsgString;
    private long mLastMsgTime;
    private String mLastMsgTimeString;
    private int mUnReadCount;

    public ImConversationBean() {

    }

    public boolean isGroup() {
        return mIsGroup;
    }

    public void setGroup(boolean group) {
        mIsGroup = group;
    }

    public String getConversationID() {
        return mConversationID;
    }

    public void setConversationID(String conversationID) {
        mConversationID = conversationID;
    }

    public String getUserID() {
        return mUserID != null ? mUserID : "";
    }

    public void setUserID(String userID) {
        mUserID = userID;
    }

    public String getGroupID() {
        return mGroupID != null ? mGroupID : "";
    }

    public void setGroupID(String groupID) {
        mGroupID = groupID;
    }


    public String getLastMsgId() {
        return mLastMsgId != null ? mLastMsgId : "";
    }

    public void setLastMsgId(String lastMsgId) {
        mLastMsgId = lastMsgId;
    }

    public String getLastMsgString() {
        return mLastMsgString != null ? mLastMsgString : "";
    }

    public void setLastMsgString(String lastMsgString) {
        mLastMsgString = lastMsgString;
    }

    public long getLastMsgTime() {
        return mLastMsgTime;
    }

    public void setLastMsgTime(long lastMsgTime) {
        mLastMsgTime = lastMsgTime*1000;
    }

    public String getLastMsgTimeString() {
        return mLastMsgTimeString;
    }

    public void setLastMsgTimeString(String lastMsgTimeString) {
        mLastMsgTimeString = lastMsgTimeString;
    }

    public int getUnReadCount() {
        return mUnReadCount;
    }

    public void setUnReadCount(int unReadCount) {
        mUnReadCount = unReadCount;
    }

}
