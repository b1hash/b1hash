package com.yunbao.live.livegame.star.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2023/3/4.
 */
public class StarRecordBean {

   private String mAddTime;
   private String mTitle;
   private List<StarRecordGiftBean> mList;

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
    public List<StarRecordGiftBean> getList() {
        return mList;
    }
    @JSONField(name = "gift_list")
    public void setList(List<StarRecordGiftBean> list) {
        mList = list;
    }
}
