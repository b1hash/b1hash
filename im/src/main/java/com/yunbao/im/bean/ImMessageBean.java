package com.yunbao.im.bean;

import com.yunbao.common.CommonAppConfig;

import java.io.File;

/**
 * Created by cxf on 2018/7/12.
 * IM 消息实体类
 */

public class ImMessageBean {

    public static final int TYPE_TEXT = 1;
    public static final int TYPE_IMAGE = 2;
    public static final int TYPE_SOUND = 3;
    public static final int TYPE_LOCATION = 4;
    public static final int TYPE_GOODS = 5;

    public static final int STATUS_SEND_ING = 1;//发送消息的状态 发送中
    public static final int STATUS_SEND_SUCC = 2;//发送消息的状态 发送成功
    public static final int STATUS_SEND_FAIL = 3;//发送消息的状态 发送失败

    private EntryMsgBean mEntryMsgBean;
    private int mType;//消息类型
    private int mStatus;//发送状态
    private boolean mRevoked;//是否撤回了
    private String mShowTimeString;

    public ImMessageBean(EntryMsgBean entryMsgBean, int type) {
        mEntryMsgBean = entryMsgBean;
        mType = type;
    }

    public ImMessageBean(EntryMsgBean entryMsgBean, int type, int status) {
        this(entryMsgBean, type);
        mStatus = status;
    }

    public String getMsgId() {
        if (mEntryMsgBean != null) {
            return mEntryMsgBean.getMsgId();
        }
        return "";
    }

    public String getSenderId() {
        if (mEntryMsgBean != null) {
            return mEntryMsgBean.getSenderId();
        }
        return "";
    }


    public String getGroupId() {
        if (mEntryMsgBean != null) {
            return mEntryMsgBean.getGroupId();
        }
        return "";
    }


    public boolean isSelf() {
        if (mEntryMsgBean != null) {
            return mEntryMsgBean.isSelf();
        }
        return false;
    }


    public long getTimestamp() {
        if (mEntryMsgBean != null) {
            return mEntryMsgBean.getTimestamp();
        }
        return 0L;
    }

    public int getType() {
        return mType;
    }


    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public String getTextMsgContent() {
        if (mEntryMsgBean != null && mEntryMsgBean instanceof TextMsgBean) {
            return ((TextMsgBean) mEntryMsgBean).getText();
        }
        return "";
    }

    public ImageMsgBean getImageBean() {
        if (mEntryMsgBean != null && mEntryMsgBean instanceof ImageMsgBean) {
            return (ImageMsgBean) mEntryMsgBean;
        }
        return null;
    }

    public SoundMsgBean getSoundBean() {
        if (mEntryMsgBean != null && mEntryMsgBean instanceof SoundMsgBean) {
            return (SoundMsgBean) mEntryMsgBean;
        }
        return null;
    }

    public LocationMsgBean getLocationBean() {
        if (mEntryMsgBean != null && mEntryMsgBean instanceof LocationMsgBean) {
            return (LocationMsgBean) mEntryMsgBean;
        }
        return null;
    }


    public GoodsMsgBean getGoodsBean() {
        if (mEntryMsgBean != null && mEntryMsgBean instanceof GoodsMsgBean) {
            return (GoodsMsgBean) mEntryMsgBean;
        }
        return null;
    }


    public boolean isRevoked() {
        return mRevoked;
    }

    public void setRevoked(boolean revoked) {
        mRevoked = revoked;
    }

    public void setMsgId(String msgId) {
        if (mEntryMsgBean != null) {
            mEntryMsgBean.setMsgId(msgId);
        }
    }

    public String getShowTimeString() {
        return mShowTimeString;
    }

    public void setShowTimeString(String showTimeString) {
        mShowTimeString = showTimeString;
    }


    public static abstract class EntryMsgBean {
        protected String mMsgId;//消息的id
        protected String mSenderId;//发消息的人的id
        protected String mGroupId;//群组id，如果是单聊消息这个值为空
        protected long mTimestamp;//消息的时间戳
        protected boolean mSelf;//是否是自己发的消息

        public EntryMsgBean(String msgId, String senderId, String groupId, long timestamp, boolean self) {
            mMsgId = msgId;
            mSenderId = senderId;
            mGroupId = groupId;
            mTimestamp = timestamp * 1000;
            mSelf = self;
        }

        public void setMsgId(String msgId) {
            mMsgId = msgId;
        }

        public String getMsgId() {
            return mMsgId != null ? mMsgId : "";
        }

        public String getSenderId() {
            return mSenderId != null ? mSenderId : "";
        }

        public String getGroupId() {
            return mGroupId != null ? mGroupId : "";
        }

        public long getTimestamp() {
            return mTimestamp;
        }

        public boolean isSelf() {
            return mSelf;
        }
    }

    public static class TextMsgBean extends EntryMsgBean {

        private String mText;

        public TextMsgBean(String msgId, String senderId, String groupId, long timestamp, boolean self) {
            super(msgId, senderId, groupId, timestamp, self);
        }

        public void setText(String text) {
            mText = text;
        }

        public String getText() {
            return mText != null ? mText : "";
        }
    }


    public static class ImageMsgBean extends EntryMsgBean {

        private String mLocalPath;//本地文件路径，只对消息发送方有效
        private String mOriginImageUrl;//原图
        private String mThumbImageUrl;//缩略图
        private int mWidth;
        private int mHeight;

        public ImageMsgBean(String msgId, String senderId, String groupId, long timestamp, boolean self) {
            super(msgId, senderId, groupId, timestamp, self);
        }

        public String getLocalPath() {
            return mLocalPath != null ? mLocalPath : "";
        }

        public void setLocalPath(String localPath) {
            mLocalPath = localPath;
        }


        public String getOriginImageUrl() {
            return mOriginImageUrl != null ? mOriginImageUrl : "";
        }

        public void setOriginImageUrl(String originImageUrl) {
            mOriginImageUrl = originImageUrl;
        }

        public String getThumbImageUrl() {
            return mThumbImageUrl != null ? mThumbImageUrl : "";
        }

        public void setThumbImageUrl(String thumbImageUrl) {
            mThumbImageUrl = thumbImageUrl;
        }

        public int getWidth() {
            return mWidth;
        }

        public void setWidth(int width) {
            if (mWidth == 0) {
                mWidth = width;
            }
        }

        public int getHeight() {
            return mHeight;
        }

        public void setHeight(int height) {
            if (mHeight == 0) {
                mHeight = height;
            }
        }
    }

    public static class SoundMsgBean extends EntryMsgBean {

        private int mDuration;//语音长度（秒）
        private File mSoundFile;

        public SoundMsgBean(String msgId, String senderId, String groupId, long timestamp, boolean self) {
            super(msgId, senderId, groupId, timestamp, self);
        }

        public int getDuration() {
            return mDuration;
        }

        public void setDuration(int duration) {
            mDuration = duration;
        }

        public File getSoundFile() {
            if (mSoundFile == null) {
                mSoundFile = new File(CommonAppConfig.IM_SOUND, mMsgId);
            }
            return mSoundFile;
        }

        public boolean isRead() {
            if (mSelf) {
                return true;
            } else {
                return getSoundFile().exists();
            }
        }
    }

    public static class LocationMsgBean extends EntryMsgBean {

        private String mAddress;//位置描述
        private double mLng;//经度
        private double mLat;//纬度

        public LocationMsgBean(String msgId, String senderId, String groupId, long timestamp, boolean self) {
            super(msgId, senderId, groupId, timestamp, self);
        }

        public String getAddress() {
            return mAddress != null ? mAddress : "";
        }

        public void setAddress(String address) {
            mAddress = address;
        }

        public double getLng() {
            return mLng;
        }

        public void setLng(double lng) {
            mLng = lng;
        }

        public double getLat() {
            return mLat;
        }

        public void setLat(double lat) {
            mLat = lat;
        }
    }

    public static class GoodsMsgBean extends EntryMsgBean {

        private String mGoodsId;//商品id

        public GoodsMsgBean(String msgId, String senderId, String groupId, long timestamp, boolean self) {
            super(msgId, senderId, groupId, timestamp, self);
        }

        public String getGoodsId() {
            return mGoodsId;
        }

        public void setGoodsId(String goodsId) {
            mGoodsId = goodsId;
        }
    }


}
