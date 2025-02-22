package com.yunbao.common.bean;

import android.text.TextUtils;

/**
 * Created by cxf on 2018/9/28.
 * 我的 页面的item
 */

public class UserItemBean {

    private int id;
    private String name;
    private String thumb;
    private String href;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public boolean isEqual(UserItemBean bean) {
        if (bean == null) {
            return false;
        }
        if (TextUtils.isEmpty(href) || !href.equals(bean.getHref())) {
            return false;
        }
        if(this.id!=bean.getId()){
            return false;
        }
        if (TextUtils.isEmpty(name) || !name.equals(bean.getName())) {
            return false;
        }
        if (TextUtils.isEmpty(thumb) || !thumb.equals(bean.getThumb())) {
            return false;
        }
        return true;
    }
}
