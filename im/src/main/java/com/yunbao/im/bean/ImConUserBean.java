package com.yunbao.im.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.alibaba.fastjson.annotation.JSONField;
import com.yunbao.common.bean.UserBean;

/**
 * Created by cxf on 2017/8/14.
 * IM 聊天用户 实体类
 */

public class ImConUserBean extends UserBean implements Parcelable {

    private ImConversationBean mConBean;
    private int attent;//我是否关注对方
    private int otherAttent;//对方是否关注我
    private boolean anchorItem;//是否是主播
    private String mTips;
    private int mUnReadCount;

    public ImConUserBean() {

    }

    public ImConversationBean getConBean() {
        return mConBean;
    }

    public void setConBean(ImConversationBean conBean) {
        mConBean = conBean;
        mUnReadCount = conBean != null ? conBean.getUnReadCount() : 0;
        mTips = null;
    }

    public String getConUserId() {
        return mConBean != null ? mConBean.getUserID() : getId();
    }

    public void setUnReadCount(int unReadCount) {
        mUnReadCount = unReadCount;
    }

    public int getUnReadCount() {
        return mUnReadCount;
    }

    public String getLastMsgId() {
        return mConBean != null ? mConBean.getLastMsgId() : "";
    }

    public String getLastMsgString() {
        if (!TextUtils.isEmpty(mTips)) {
            return mTips;
        }
        return mConBean != null ? mConBean.getLastMsgString() : "";
    }

    public long getMsgTime() {
        return mConBean != null ? mConBean.getLastMsgTime() : 0;
    }

    public String getMsgTimeString() {
        return mConBean != null ? mConBean.getLastMsgTimeString() : "";
    }

    public void setTips(String tips) {
        mTips = tips;
    }

    @JSONField(name = "utot")
    public int getAttent() {
        return attent;
    }

    @JSONField(name = "utot")
    public void setAttent(int attent) {
        this.attent = attent;
    }

    @JSONField(name = "ttou")
    public int getOtherAttent() {
        return otherAttent;
    }

    @JSONField(name = "ttou")
    public void setOtherAttent(int otherAttent) {
        this.otherAttent = otherAttent;
    }

    public boolean isAnchorItem() {
        return anchorItem;
    }

    public void setAnchorItem(boolean anchorItem) {
        this.anchorItem = anchorItem;
    }

    public boolean isHasConversation() {
        return mConBean != null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.attent);
        dest.writeInt(this.otherAttent);
    }

    protected ImConUserBean(Parcel in) {
        super(in);
        this.attent = in.readInt();
        this.otherAttent = in.readInt();
    }

    public static final Creator<ImConUserBean> CREATOR = new Creator<ImConUserBean>() {
        @Override
        public ImConUserBean[] newArray(int size) {
            return new ImConUserBean[size];
        }

        @Override
        public ImConUserBean createFromParcel(Parcel in) {
            return new ImConUserBean(in);
        }
    };

}
