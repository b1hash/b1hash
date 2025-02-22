package com.yunbao.live.livegame.luckpan.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by http://www.yunbaokj.com on 2023/3/4.
 */
public class LuckRankBean {

   private String mUserName;
   private String mAvatar;
   private String mCoin;

   @JSONField(name = "user_nickname")
    public String getUserName() {
        return mUserName;
    }
    @JSONField(name = "user_nickname")
    public void setUserName(String userName) {
        mUserName = userName;
    }
    @JSONField(name = "avatar")
    public String getAvatar() {
        return mAvatar;
    }
    @JSONField(name = "avatar")
    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }
    @JSONField(name = "total")
    public String getCoin() {
        return mCoin;
    }
    @JSONField(name = "total")
    public void setCoin(String coin) {
        mCoin = coin;
    }
}
