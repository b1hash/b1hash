package com.yunbao.live.views;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.ViewGroup;

import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.yunbao.common.utils.L;
import com.yunbao.live.R;
import com.yunbao.live.activity.LiveActivity;

/**
 * Created by cxf on 2018/10/10.
 * 直播间播放器  腾讯播放器
 */

public class LiveChatRoomPlayTxViewHolder extends AbsLiveChatRoomPlayViewHolder {

    private static final String TAG = "LiveVoicePlayTxViewHolder";

    private ViewGroup mContainer;
    private String mAnchorOriginUrl;//主播的最开始的播放地址
    private boolean mChatRoomTypeVideo;
    private V2TXLivePlayer mLivePlayer;
    private PlayViewProvider mPlayViewProvider;
    private boolean mPaused;


    public LiveChatRoomPlayTxViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_play_tx_chatroom;
    }

    @Override
    public void init() {
        mContainer = findViewById(R.id.voice_play_container);
        mChatRoomTypeVideo = ((LiveActivity) mContext).isChatRoomTypeVideo();
    }


    public ViewGroup getContainer() {
        return mContainer;
    }

    private V2TXLivePlayer getLivePlayer() {
        if (mLivePlayer == null) {
            mLivePlayer = new V2TXLivePlayerImpl(mContext);
            mLivePlayer.setObserver(new V2TXLivePlayerObserver() {
                @Override
                public void onError(V2TXLivePlayer player, int code, String msg, Bundle extraInfo) {
                    L.e(TAG, "V2TXLivePlayerObserver--onError: code=" + code + ", msg= " + msg);
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

                }

                @Override
                public void onVideoLoading(V2TXLivePlayer player, Bundle extraInfo) {
                    L.e(TAG, "V2TXLivePlayerObserver--onVideoLoading: ");
                }
            });
            mLivePlayer.setRenderFillMode(V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFill);
            if (mPlayViewProvider != null) {
                TextureView textureView = mPlayViewProvider.getPlayView();
                if (textureView != null) {
                    mLivePlayer.setRenderView(textureView);
                }
            }
        }
        return mLivePlayer;
    }

    @Override
    public void hideCover() {
    }

    @Override
    public void setCover(String coverUrl) {
    }

    /**
     * 暂停播放
     */
    @Override
    public void pausePlay() {

    }

    /**
     * 暂停播放后恢复
     */
    @Override
    public void resumePlay() {

    }

    /**
     * 开始播放
     *
     * @param url 流地址
     */
    @Override
    public void play(String url) {
        mAnchorOriginUrl = url;
        if (mChatRoomTypeVideo) {
            getLivePlayer().startLivePlay(url);
        } else {
            LiveChatRoomPlayUtil.getInstance().startPlay(url);
        }
    }


    /**
     * 播放主播低延时流地址
     *
     * @param accPullUrl 低延时流地址，为空切回到普通流
     */
    @Override
    public void changeAccStream(String accPullUrl) {
        if (mChatRoomTypeVideo) {
            if (mLivePlayer != null) {
                if (TextUtils.isEmpty(accPullUrl)) {
                    mLivePlayer.startLivePlay(mAnchorOriginUrl);
                } else {
                    mLivePlayer.startLivePlay(accPullUrl);
                }
            }
        } else {
            if (TextUtils.isEmpty(accPullUrl)) {
                LiveChatRoomPlayUtil.getInstance().startPlay(mAnchorOriginUrl);
            } else {
                LiveChatRoomPlayUtil.getInstance().startPlay(accPullUrl);
            }
        }
    }

    @Override
    public void stopPlay() {
        if (mChatRoomTypeVideo) {
            if (mLivePlayer != null) {
                mLivePlayer.stopPlay();
            }
        } else {
            LiveChatRoomPlayUtil.getInstance().stopPlay();
        }
    }


    @Override
    public void onPause() {
        if (mChatRoomTypeVideo) {
            if (mLivePlayer != null) {
                mLivePlayer.setPlayoutVolume(0);
            }
        }
        mPaused = true;
    }

    @Override
    public void onResume() {
        if (mPaused) {
            if (mChatRoomTypeVideo) {
                if (mLivePlayer != null) {
                    mLivePlayer.setPlayoutVolume(100);
                }
            }
        }
        mPaused = false;
    }

    @Override
    public void release() {
        if (mChatRoomTypeVideo) {
            if (mLivePlayer != null) {
                mLivePlayer.stopPlay();
                mLivePlayer.setObserver(null);
            }
            mLivePlayer = null;
            mPlayViewProvider = null;
        } else {
            LiveChatRoomPlayUtil.getInstance().release();
        }
        L.e(TAG, "release------->");
    }


    @Override
    public ViewGroup getSmallContainer() {
        return null;
    }


    @Override
    public ViewGroup getRightContainer() {
        return null;
    }

    @Override
    public ViewGroup getPkContainer() {
        return null;
    }

    @Override
    public void changeToLeft() {

    }

    @Override
    public void changeToBig() {

    }

    @Override
    public void onDestroy() {
        release();
    }

    public void setPlayViewProvider(PlayViewProvider provider) {
        mPlayViewProvider = provider;
    }

    public interface PlayViewProvider {
        TextureView getPlayView();
    }

}
