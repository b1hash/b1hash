package com.yunbao.live.views;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.live.R;

/**
 * Created by cxf on 2018/10/25.
 * 连麦播放小窗口  使用腾讯sdk
 */

public class LiveLinkMicPlayTxViewHolder extends AbsLiveLinkMicPlayViewHolder {

    private static final String TAG = "LiveLinkMicPlayTxViewHolder";
    private V2TXLivePlayer mPlayer;

    public LiveLinkMicPlayTxViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_link_mic_play_tx;
    }

    @Override
    public void init() {
        super.init();
        mPlayer = new V2TXLivePlayerImpl(mContext);
        mPlayer.setObserver(new V2TXLivePlayerObserver() {
            @Override
            public void onError(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
                L.e(TAG, "V2TXLivePlayerObserver--onError: code=" + code + ", msg= " + msg);
//                ToastUtil.show(WordUtil.getString(R.string.live_play_error));
            }

            @Override
            public void onWarning(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
                L.e(TAG, "V2TXLivePlayerObserver--onWarning: code=" + code + ", msg= " + msg);
            }

            @Override
            public void onVideoResolutionChanged(V2TXLivePlayer player, int width, int height) {
                L.e(TAG, "V2TXLivePlayerObserver--onVideoResolutionChanged: width=" + width + ", height= " + height);
            }

            @Override
            public void onConnected(V2TXLivePlayer player, Bundle extraInfo) {
                L.e(TAG, "V2TXLivePlayerObserver--onConnected: ");
            }

            @Override
            public void onVideoPlaying(V2TXLivePlayer player, boolean firstPlay, Bundle extraInfo) {
                L.e(TAG, "V2TXLivePlayerObserver--onVideoPlaying: ");
                if (mLoading != null && mLoading.getVisibility() != View.INVISIBLE) {
                    mLoading.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onVideoLoading(V2TXLivePlayer player, Bundle extraInfo) {
                L.e(TAG, "V2TXLivePlayerObserver--onVideoLoading: ");
                if (mLoading != null && mLoading.getVisibility() != View.VISIBLE) {
                    mLoading.setVisibility(View.VISIBLE);
                }
            }

        });
        mPlayer.setRenderFillMode(V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFill);
        mPlayer.setRenderView((TextureView) findViewById(R.id.video_view));
    }

    @Override
    public void setOnCloseListener(View.OnClickListener onClickListener) {
        if (onClickListener != null) {
            mBtnClose.setVisibility(View.VISIBLE);
            mBtnClose.setOnClickListener(onClickListener);
        }
    }

    /**
     * 开始播放
     *
     * @param url 流地址
     */
    @Override
    public void play(final String url) {
        if (TextUtils.isEmpty(url)) {
            if (mPlayer != null) {
                mPlayer.stopPlay();
            }
            mEndPlay = true;
            return;
        }
        if (mHandler != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mEndPlay = false;
                    if (mPlayer != null) {
                        mPlayer.startLivePlay(url);
                    }
                    L.e(TAG, "play----url--->" + url);
                }
            }, 500);
        }
    }

    @Override
    public void release() {
        mEndPlay = true;
        if (mPlayer != null) {
            mPlayer.stopPlay();
            mPlayer.setObserver(null);
        }
        mPlayer = null;
        if (mBtnClose != null) {
            mBtnClose.setOnClickListener(null);
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        L.e(TAG, "release------->");
    }

    @Override
    public void resume() {
        if (mPaused) {
            mPlayer.setPlayoutVolume(100);
        }
        mPaused = false;
    }

    @Override
    public void pause() {
        if (mPlayer != null) {
            mPlayer.setPlayoutVolume(0);
        }
        mPaused = true;
    }
}
