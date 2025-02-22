package com.yunbao.common.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class FirstChargeBean {
    private String mId;
    private String mMoney;
    private String mTitle;
    private String mCoin;
    private List<FirstChargeItemBean> mList;

    @JSONField(name = "id")
    public String getId() {
        return mId;
    }
    @JSONField(name = "id")
    public void setId(String id) {
        mId = id;
    }
    @JSONField(name = "money")
    public String getMoney() {
        return mMoney;
    }
    @JSONField(name = "money")
    public void setMoney(String money) {
        mMoney = money;
    }
    @JSONField(name = "title")
    public String getTitle() {
        return mTitle;
    }
    @JSONField(name = "title")
    public void setTitle(String title) {
        mTitle = title;
    }

    @JSONField(name = "coin")
    public String getCoin() {
        return mCoin;
    }

    @JSONField(name = "coin")
    public void setCoin(String coin) {
        mCoin = coin;
    }

    @JSONField(name = "list")
    public List<FirstChargeItemBean> getList() {
        return mList;
    }
    @JSONField(name = "list")
    public void setList(List<FirstChargeItemBean> list) {
        mList = list;
    }
}
