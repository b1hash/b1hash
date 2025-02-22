package com.yunbao.live.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.zyyoona7.wheel.IWheelEntity;

/**
 * Created by 云豹科技 on 2022/3/7.
 */
public class SuperForbidTimeBean implements IWheelEntity {
    private String mId;
    private String mName;


    @JSONField(name = "id")
    public String getId() {
        return mId;
    }
    @JSONField(name = "id")
    public void setId(String id) {
        mId = id;
    }

    @JSONField(name = "name")
    public String getName() {
        return mName;
    }

    @JSONField(name = "name")
    public void setName(String name) {
        mName = name;
    }

    @Override
    public String getWheelText() {
        return mName;
    }
}
