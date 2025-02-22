package com.yunbao.live.views;

import android.os.Bundle;
import android.text.TextUtils;

import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.interfaces.AppLifecycleUtil;
import com.yunbao.live.floatwindow.FloatWindowUtil;

import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineEx;

public class LiveChatRoomPlayUtil implements AppLifecycleUtil.LifecycleCallback {

    private static LiveChatRoomPlayUtil sInstance;
    private V2TXLivePlayer mPlayer;
    private boolean mKeepAlive;
    private RtcEngineEx mEngine;

    private LiveChatRoomPlayUtil() {
        AppLifecycleUtil.addLifecycleCallback(this);
    }

    public void setKeepAlive(boolean keepAlive) {
        mKeepAlive = keepAlive;
    }

    public boolean isKeepAlive() {
        return mKeepAlive;
    }

    public static LiveChatRoomPlayUtil getInstance() {
        if (sInstance == null) {
            synchronized (LiveChatRoomPlayUtil.class) {
                if (sInstance == null) {
                    sInstance = new LiveChatRoomPlayUtil();
                }
            }
        }
        return sInstance;
    }

    /**
     * 开始播放
     *
     * @param url 要播放的流地址
     */
    public void startPlay(String url) {
        if (mKeepAlive) {
            mKeepAlive = false;
            return;
        }
        if (mPlayer == null) {
            mPlayer = new V2TXLivePlayerImpl(CommonAppContext.getInstance());
            mPlayer.setObserver(new V2TXLivePlayerObserver() {
                @Override
                public void onError(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
                    FloatWindowUtil.getInstance().release();
                }
            });
        }
        if (!TextUtils.isEmpty(url)) {
            if (mPlayer != null) {
                mPlayer.startLivePlay(url);
            }
        }
    }


    public void stopPlay() {
        if (mKeepAlive) {
            return;
        }
        if (mPlayer != null) {
            try {
                mPlayer.stopPlay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void release() {
        if (mKeepAlive) {
            return;
        }
        if (mPlayer != null) {
            try {
                mPlayer.stopPlay();
                mPlayer.setObserver(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mPlayer = null;
        if (mEngine != null) {
            mEngine.leaveChannel();
            mEngine.stopPreview();
            CommonAppContext.post(RtcEngine::destroy);
        }
        mEngine = null;
        mKeepAlive = false;
    }

    public void setAgoraEngine(RtcEngineEx engine) {
        mEngine = engine;
    }


    private void setMute(boolean mute) {
//        if (mKeepAlive) {
//            return;
//        }
        if (mPlayer != null) {
            try {
                mPlayer.setPlayoutVolume(mute ? 0 : 100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void onAppFrontGround() {
        setMute(false);
    }

    @Override
    public void onAppBackGround() {
        setMute(true);
    }
}
