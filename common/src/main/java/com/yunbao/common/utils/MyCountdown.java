package com.yunbao.common.utils;

import android.os.Handler;
import android.os.SystemClock;

/**
 * Created by http://www.yunbaokj.com on 2023/10/23.
 */
public class MyCountdown {

    private static final String TAG = "倒计时";
    private long mNextTime;
    private int mTotalSecond;
    private final Runnable mRunnable;
    private final Handler mHandler;
    private final StringBuilder mStringBuilder;
    private ActionListener mCallback;

    public MyCountdown() {
        mStringBuilder = new StringBuilder();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                mTotalSecond--;
                if (mTotalSecond < 0) {
                    mTotalSecond = 0;
                }
                if (mCallback != null) {
                    if(mCallback instanceof ActionListener2){
                        ((ActionListener2) mCallback).onTimeSecondChanged(mTotalSecond);
                    }else{
                        mCallback.onTimeChanged(getTimeString());
                    }
                }
                if (mTotalSecond > 0) {
                    mNextTime += 1000;
                    mHandler.postAtTime(mRunnable, mNextTime);
                } else {
                    if (mCallback != null) {
                        mCallback.onTimeEnd();
                    }
                }
            }
        };
        mHandler = new Handler();
    }

    public MyCountdown setTotalSecond(int totalSecond) {
        mTotalSecond = totalSecond;
        return this;
    }

    public MyCountdown setCallback(ActionListener callback) {
        mCallback = callback;
        return this;
    }

    public MyCountdown start() {
        mNextTime = SystemClock.uptimeMillis() + 1000;
        mHandler.postAtTime(mRunnable, mNextTime);
        if (mCallback != null) {
            if(mCallback instanceof ActionListener2){
                ((ActionListener2) mCallback).onTimeSecondChanged(mTotalSecond);
            }else{
                mCallback.onTimeChanged(getTimeString());
            }
        }
        L.e(TAG, "start-------->");
        return this;
    }

    private String getTimeString() {
        int hours = mTotalSecond / 3600;
        int minutes = 0;
        int last = mTotalSecond % 3600;
        if (last > 0) {
            minutes = last / 60;
        }
        int seconds = mTotalSecond % 60;
        mStringBuilder.delete(0, mStringBuilder.length());
        if (hours > 0) {
            if (hours < 10) {
                mStringBuilder.append("0");
            }
            mStringBuilder.append(hours);
            mStringBuilder.append(":");
        }
        if (minutes > 0) {
            if (minutes < 10) {
                mStringBuilder.append("0");
            }
            mStringBuilder.append(minutes);
            mStringBuilder.append(":");
        } else {
            mStringBuilder.append("00:");
        }
        if (seconds > 0) {
            if (seconds < 10) {
                mStringBuilder.append("0");
            }
            mStringBuilder.append(seconds);
        } else {
            mStringBuilder.append("00");
        }
        String s = mStringBuilder.toString();
        L.e(TAG, "getTimeString-------->" + s);
        return s;
    }

    public void stop(){
        mHandler.removeCallbacks(mRunnable);
    }

    public void release() {
        mHandler.removeCallbacks(mRunnable);
        mCallback = null;
        L.e(TAG, "release-------->");
    }

    public interface ActionListener {
        void onTimeChanged(String timeStr);

        void onTimeEnd();
    }

    public interface ActionListener2 extends ActionListener{
        void onTimeSecondChanged(int secondCount);
    }
}
