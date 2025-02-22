package com.yunbao.im.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.yunbao.common.bean.UserBean;

/**
 * Created by 云豹科技 on 2022/1/18.
 * 视频消息
 */
public class VideoImConcatBean {

    private String mFromUid;
    private String mAddTime;
    private UserBean mUserBean;
    private int mIsAttent;

    @JSONField(name = "uid")
    public String getFromUid() {
        return mFromUid;
    }
    @JSONField(name = "uid")
    public void setFromUid(String fromUid) {
        mFromUid = fromUid;
    }
    @JSONField(name = "addtime")
    public String getAddTime() {
        return mAddTime;
    }
    @JSONField(name = "addtime")
    public void setAddTime(String addTime) {
        mAddTime = addTime;
    }
    @JSONField(name = "userinfo")
    public UserBean getUserBean() {
        return mUserBean;
    }
    @JSONField(name = "userinfo")
    public void setUserBean(UserBean userBean) {
        mUserBean = userBean;
    }
    @JSONField(name = "isattention")
    public int getIsAttent() {
        return mIsAttent;
    }
    @JSONField(name = "isattention")
    public void setIsAttent(int isAttent) {
        mIsAttent = isAttent;
    }
}
