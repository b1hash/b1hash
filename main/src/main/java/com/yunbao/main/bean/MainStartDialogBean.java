package com.yunbao.main.bean;

/**
 * Created by http://www.yunbaokj.com on 2023/11/15.
 */
public class MainStartDialogBean {
    private final int mIconRes;
    private final int mTextRes;

    public MainStartDialogBean(int iconRes, int textRes) {
        mIconRes = iconRes;
        mTextRes = textRes;
    }

    public int getIconRes() {
        return mIconRes;
    }

    public int getTextRes() {
        return mTextRes;
    }
}
