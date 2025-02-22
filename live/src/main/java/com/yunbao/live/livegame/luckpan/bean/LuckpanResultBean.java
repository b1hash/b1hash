package com.yunbao.live.livegame.luckpan.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.yunbao.common.utils.LanguageUtil;

/**
 * Created by http://www.yunbaokj.com on 2023/3/4.
 */
public class LuckpanResultBean {

    private String mGiftIcon;
    private String mGiftName;
    private String mGiftNameEn;
    private String mCount;
    private String mTotalCoin;

    @JSONField(name = "gifticon")
    public String getGiftIcon() {
        return mGiftIcon;
    }

    @JSONField(name = "gifticon")
    public void setGiftIcon(String giftIcon) {
        mGiftIcon = giftIcon;
    }

    public String getGiftNameTrans(){
        if(LanguageUtil.isZh()){
            return mGiftName;
        }else{
            return mGiftNameEn;
        }
    }

    @JSONField(name = "giftname")
    public String getGiftName() {
        return mGiftName;
    }

    @JSONField(name = "giftname")
    public void setGiftName(String giftName) {
        mGiftName = giftName;
    }
    @JSONField(name = "giftname_en")
    public String getGiftNameEn() {
        return mGiftNameEn;
    }
    @JSONField(name = "giftname_en")
    public void setGiftNameEn(String giftNameEn) {
        mGiftNameEn = giftNameEn;
    }
    @JSONField(name = "nums")
    public String getCount() {
        return mCount;
    }

    @JSONField(name = "nums")
    public void setCount(String count) {
        mCount = count;
    }

    @JSONField(name = "total")
    public String getTotalCoin() {
        return mTotalCoin;
    }

    @JSONField(name = "total")
    public void setTotalCoin(String totalCoin) {
        mTotalCoin = totalCoin;
    }
}
