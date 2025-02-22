package com.yunbao.main.custom;

import android.content.Context;


import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;

/**
 * Created by http://www.yunbaokj.com on 2023/10/28.
 */
public class MainHomePageTitleView extends ColorTransitionPagerTitleView {

    private int mColor;

    public MainHomePageTitleView(Context context) {
        super(context);
    }

    @Override
    public void setTextColor(int color) {
        mColor = color;
        super.setTextColor(color);
    }

    public void updateColor(boolean checked) {
        int color = checked ? mSelectedColor : mNormalColor;
        if (mColor != color) {
            setTextColor(color);
        }
    }
}
