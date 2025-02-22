package com.yunbao.im.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by 云豹科技 on 2022/1/18.
 * 视频消息
 */
public class VideoImMsgBean {

    private String mFromUid;
    private String mVideoId;
    private String mAvatar;
    private String mThumb;
    private String mUserName;
    private String mAddTime;
    private String mVideoTitle;
    private String mContent;
    private int mType;

    @JSONField(name = "uid")
    public String getFromUid() {
        return mFromUid;
    }
    @JSONField(name = "uid")
    public void setFromUid(String fromUid) {
        mFromUid = fromUid;
    }

    @JSONField(name = "videoid")
    public String getVideoId() {
        return mVideoId;
    }
    @JSONField(name = "videoid")
    public void setVideoId(String videoId) {
        mVideoId = videoId;
    }
    @JSONField(name = "avatar")
    public String getAvatar() {
        return mAvatar;
    }
    @JSONField(name = "avatar")
    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }
    @JSONField(name = "video_thumb")
    public String getThumb() {
        return mThumb;
    }
    @JSONField(name = "video_thumb")
    public void setThumb(String thumb) {
        mThumb = thumb;
    }
    @JSONField(name = "user_nickname")
    public String getUserName() {
        return mUserName == null ? "" : mUserName;
    }
    @JSONField(name = "user_nickname")
    public void setUserName(String userName) {
        mUserName = userName;
    }
    @JSONField(name = "addtime")
    public String getAddTime() {
        return mAddTime;
    }
    @JSONField(name = "addtime")
    public void setAddTime(String addTime) {
        mAddTime = addTime;
    }

    @JSONField(name = "video_title")
    public String getVideoTitle() {
        return mVideoTitle;
    }
    @JSONField(name = "video_title")
    public void setVideoTitle(String videoTitle) {
        mVideoTitle = videoTitle;
    }

    @JSONField(name = "content")
    public String getContent() {
        return mContent;
    }
    @JSONField(name = "content")
    public void setContent(String content) {
        mContent = content;
    }

    @JSONField(name = "type")
    public int getType() {
        return mType;
    }
    @JSONField(name = "type")
    public void setType(int type) {
        mType = type;
    }
}
