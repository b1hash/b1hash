package com.yunbao.common.custom;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by cxf on 2018/9/22.
 */

public class TabButtonGroup extends LinearLayout implements View.OnClickListener {

    private TabButton[] mTabButtons;
    private ViewPager mViewPager;
    private int mCurPosition;


    public TabButtonGroup(Context context) {
        this(context, null);
    }

    public TabButtonGroup(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabButtonGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if (childCount > 0) {
            mTabButtons = new TabButton[childCount];
            for (int i = 0; i < childCount; i++) {
                View v = getChildAt(i);
                v.setTag(i);
                v.setOnClickListener(this);
                mTabButtons[i] = (TabButton) v;
            }
        }
    }


    public void setCurPosition(int position) {
        if (position == mCurPosition) {
            if(mOnBtnClickListener!=null){
                mOnBtnClickListener.onClick(position);
            }
            return;
        }
        if (mClickIntercepter != null && mClickIntercepter.needIntercept(position)) {
            return;
        }
        mTabButtons[mCurPosition].setChecked(false);
        mTabButtons[position].setChecked(true);
        mCurPosition = position;
        if (mViewPager != null) {
            mViewPager.setCurrentItem(position, false);
        }
    }


    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag != null) {
            setCurPosition((int) tag);
        }
    }

    public void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
    }

    public void cancelAnim() {
        if (mTabButtons != null) {
            for (TabButton tbn : mTabButtons) {
                if (tbn != null) {
                    tbn.cancelAnim();
                }
            }
        }
    }

    public ClickIntercepter mClickIntercepter;

    public void setClickIntercepter(ClickIntercepter intercepter) {
        mClickIntercepter = intercepter;
    }

    public interface ClickIntercepter {
        boolean needIntercept(int position);
    }


    public void btnPerformClick(int position){
        if (mTabButtons != null&&mTabButtons[position]!=null) {
            mTabButtons[position].performClick();
        }
    }

    public interface OnBtnClickListener{
        void onClick(int index);
    }
    private OnBtnClickListener mOnBtnClickListener;

    public void setOnBtnClickListener(OnBtnClickListener onBtnClickListener) {
        mOnBtnClickListener = onBtnClickListener;
    }
}
