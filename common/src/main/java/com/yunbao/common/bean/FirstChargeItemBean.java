package com.yunbao.common.bean;

import com.alibaba.fastjson.annotation.JSONField;

public class FirstChargeItemBean {
    private String mIcon;
    private String mName;
    private String mCount;

    @JSONField(name = "thumb")
    public String getIcon() {
        return mIcon;
    }

    @JSONField(name = "thumb")
    public void setIcon(String icon) {
        mIcon = icon;
    }

    @JSONField(name = "name")
    public String getName() {
        return mName;
    }

    @JSONField(name = "name")
    public void setName(String name) {
        mName = name;
    }

    @JSONField(name = "count")
    public String getCount() {
        return mCount;
    }

    @JSONField(name = "count")
    public void setCount(String count) {
        mCount = count;
    }
}
