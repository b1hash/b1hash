package com.yunbao.live.livegame.luckpan.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/3/4.
 */
public class LuckpanRecordBean {

   private String mAddTime;
   private String mTitle;
   private List<LuckpanRecordGiftBean> mList;

    @JSONField(name = "addtime")
    public String getAddTime() {
        return mAddTime;
    }
    @JSONField(name = "addtime")
    public void setAddTime(String addTime) {
        mAddTime = addTime;
    }
    @JSONField(name = "title")
    public String getTitle() {
        return mTitle;
    }
    @JSONField(name = "title")
    public void setTitle(String title) {
        mTitle = title;
    }
    @JSONField(name = "gift_list")
    public List<LuckpanRecordGiftBean> getList() {
        return mList;
    }
    @JSONField(name = "gift_list")
    public void setList(List<LuckpanRecordGiftBean> list) {
        mList = list;
    }
}
