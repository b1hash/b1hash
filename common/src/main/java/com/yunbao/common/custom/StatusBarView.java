package com.yunbao.common.custom;

import android.content.Context;

import androidx.annotation.Nullable;

import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;

import com.yunbao.common.CommonAppContext;
import com.yunbao.common.utils.ScreenDimenUtil;


/**
 * Created by 云豹科技 on 2021/10/22.
 */
public class StatusBarView extends View {

    private final int mStatusBarHeight;

    public StatusBarView(Context context) {
        this(context, null);
    }

    public StatusBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            mStatusBarHeight = resources.getDimensionPixelSize(resourceId);
        } else {
            mStatusBarHeight = 0;
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mStatusBarHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
