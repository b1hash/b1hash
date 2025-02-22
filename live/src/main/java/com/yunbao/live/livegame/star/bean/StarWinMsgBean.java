package com.yunbao.live.livegame.star.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.yunbao.common.utils.LanguageUtil;

/**
 * Created by http://www.yunbaokj.com on 2023/2/27.
 * 中奖消息实体类
 */
public class StarWinMsgBean {

    private String mGiftIcon;
    private String mTitle;
    private String mTitleEn;

    @JSONField(name = "gifticon")
    public String getGiftIcon() {
        return mGiftIcon;
    }

    @JSONField(name = "gifticon")
    public void setGiftIcon(String giftIcon) {
        mGiftIcon = giftIcon;
    }

    @JSONField(name = "title")
    public String getTitle() {
        return mTitle;
    }

    @JSONField(name = "title")
    public void setTitle(String title) {
        mTitle = title;
    }

    @JSONField(name = "title_en")
    public String getTitleEn() {
        return mTitleEn;
    }

    @JSONField(name = "title_en")
    public void setTitleEn(String titleEn) {
        mTitleEn = titleEn;
    }

    public String getTitleTrans() {
        if (LanguageUtil.isZh()) {
            return mTitle;
        } else {
            return mTitleEn;
        }
    }
}
