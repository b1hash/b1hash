package com.yunbao.common.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.yunbao.common.CommonAppContext;

/**
 * Created by cxf on 2017/10/30.
 * 获取屏幕尺寸
 */

public class ScreenDimenUtil {

    private final int mStatusBarHeight;//状态栏高度
    private final int mScreenWidth;
    private final int mScreenHeight;
    private final int mScreenRealHeight;
    private final int mNavigationBarHeight;

    private static ScreenDimenUtil sInstance;

    private ScreenDimenUtil() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) CommonAppContext.getInstance().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        display.getRealMetrics(dm);
        mScreenRealHeight = dm.heightPixels;

        Resources resources = CommonAppContext.getInstance().getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            mStatusBarHeight = resources.getDimensionPixelSize(resourceId);
        } else {
            mStatusBarHeight = 0;
        }
        int navResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (navResourceId > 0) {
            mNavigationBarHeight = resources.getDimensionPixelSize(resourceId);
        } else {
            mNavigationBarHeight = 0;
        }
    }

    public static ScreenDimenUtil getInstance() {
        if (sInstance == null) {
            synchronized (ScreenDimenUtil.class) {
                if (sInstance == null) {
                    sInstance = new ScreenDimenUtil();
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取屏幕的宽度
     *
     * @return
     */
    public int getScreenWidth() {
        return mScreenWidth;
    }

    /**
     * 获取屏幕的高度
     *
     * @return
     */
    public int getScreenHeight() {
        return mScreenHeight;
    }


    public int getScreenRealHeight() {
        return mScreenRealHeight;
    }

    /**
     * 获取状态栏的高度
     *
     * @return
     */
    public int getStatusBarHeight() {
        return mStatusBarHeight;
    }

    public int getNavigationBarHeight() {
        return mNavigationBarHeight;
    }
}
