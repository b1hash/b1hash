package com.yunbao.common.event;

/**
 * Created by http://www.yunbaokj.com on 2022/7/12.
 */
public class AppLifecycleEvent {

    private boolean mFrontGround;

    public AppLifecycleEvent(boolean frontGround) {
        mFrontGround = frontGround;
    }

    public boolean isFrontGround() {
        return mFrontGround;
    }


}
