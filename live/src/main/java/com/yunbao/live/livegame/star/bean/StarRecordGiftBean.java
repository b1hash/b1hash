package com.yunbao.live.livegame.star.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by http://www.yunbaokj.com on 2023/3/4.
 */
public class StarRecordGiftBean {

    private String mGiftIcon;
    private String mCount;

    @JSONField(name = "gifticon")
    public String getGiftIcon() {
        return mGiftIcon;
    }

    @JSONField(name = "gifticon")
    public void setGiftIcon(String giftIcon) {
        mGiftIcon = giftIcon;
    }


    @JSONField(name = "nums")
    public String getCount() {
        return mCount;
    }

    @JSONField(name = "nums")
    public void setCount(String count) {
        mCount = count;
    }


}
