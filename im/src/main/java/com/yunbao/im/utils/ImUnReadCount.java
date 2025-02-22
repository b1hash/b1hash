package com.yunbao.im.utils;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.SpUtil;

/**
 * Created by http://www.yunbaokj.com on 2022/12/8.
 */
public class ImUnReadCount {
    private int mTotalCount;
    private int mGoodsOrderCount;
    private int mConcatCount;
    private int mLikeCount;
    private int mAtCount;
    private int mCommentCount;

    public void clear() {
        mTotalCount = 0;
        mGoodsOrderCount = 0;
        mConcatCount = 0;
        mLikeCount = 0;
        mAtCount = 0;
        mCommentCount = 0;
    }

    private String getCountString(int count) {
        if (count < 0) {
            count = 0;
        }
        if (count > 99) {
            return "99+";
        } else {
            return String.valueOf(count);
        }
    }

    public void setTotalCount(int totalCount) {
        mTotalCount = totalCount;
    }

    public void setGoodsOrderCount(int goodsOrderCount) {
        mGoodsOrderCount = goodsOrderCount;
    }

    public void setConcatCount(int concatCount) {
        mConcatCount = concatCount;
    }

    public void setLikeCount(int likeCount) {
        mLikeCount = likeCount;
    }

    public void setAtCount(int atCount) {
        mAtCount = atCount;
    }

    public void setCommentCount(int commentCount) {
        mCommentCount = commentCount;
    }

    public String getTotalUnReadCount() {
        return getTotalUnReadCount(true);
    }

    public String getTotalUnReadCount(boolean isSystemMsgCount) {
        if (!CommonAppConfig.getInstance().isLogin()) {
            return "0";
        }
        int count = mTotalCount;
        if (isSystemMsgCount) {
            count += SpUtil.getInstance().getIntValue(SpUtil.SYSTEM_MSG_COUNT, 0);
        }
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            count -= mGoodsOrderCount;
        }
        return getCountString(count);
    }

    public String getLiveRoomUnReadCount() {
        if (!CommonAppConfig.getInstance().isLogin()) {
            return "0";
        }
        int count = mTotalCount;
        int systemMsgCount = SpUtil.getInstance().getIntValue(SpUtil.SYSTEM_MSG_COUNT, 0);
        count += systemMsgCount;
        count -= mConcatCount;
        count -= mLikeCount;
        count -= mAtCount;
        count -= mCommentCount;
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            count -= mGoodsOrderCount;
        }
        return getCountString(count);
    }

    public String getConcatUnReadCount() {
        if (!CommonAppConfig.getInstance().isLogin()) {
            return "0";
        }
        return getCountString(mConcatCount);
    }

    public String getLikeUnReadCount() {
        if (!CommonAppConfig.getInstance().isLogin()) {
            return "0";
        }
        return getCountString(mLikeCount);
    }

    public String getAtUnReadCount() {
        if (!CommonAppConfig.getInstance().isLogin()) {
            return "0";
        }
        return getCountString(mAtCount);
    }

    public String getCommentUnReadCount() {
        if (!CommonAppConfig.getInstance().isLogin()) {
            return "0";
        }
        return getCountString(mCommentCount);
    }
}
