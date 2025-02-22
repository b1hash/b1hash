package com.yunbao.live.views;


import android.content.Context;
import android.view.TextureView;
import android.view.ViewGroup;

import com.yunbao.live.R;
import com.yunbao.live.bean.LiveConfigBean;

/**
 * Created by cxf on 2018/10/7.
 * 声网直播推流 语音聊天室
 */

public class LiveChatRoomPushAgoraViewHolder extends AbsLiveChatRoomPushViewHolder {

    private ViewGroup mContainer;
    //    private boolean mPaused;
    private boolean mPlayBgm;
    private LiveChatRoomLinkMicAgoraViewHolder mLiveChatRoomLinkMicAgoraVh;

    public LiveChatRoomPushAgoraViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public LiveChatRoomPushAgoraViewHolder(Context context, ViewGroup parentView, Object... args) {
        super(context, parentView, args);
    }

    public void setLiveChatRoomLinkMicAgoraVh(LiveChatRoomLinkMicAgoraViewHolder liveChatRoomLinkMicAgoraVh) {
        mLiveChatRoomLinkMicAgoraVh = liveChatRoomLinkMicAgoraVh;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_push_tx_voice;
    }

    @Override
    public void init() {
        mContainer = findViewById(R.id.voice_push_container);
    }

    @Override
    public void startMicrophone() {
    }

    @Override
    public void startPreview(LiveConfigBean liveConfigBean, TextureView textureView) {
        if (mLiveChatRoomLinkMicAgoraVh != null) {
            mLiveChatRoomLinkMicAgoraVh.startPreview(textureView);
        }
    }

    /**
     * 开始声网推流
     */
    @Override
    public void startPushAgora(String token, String channelId) {
        if (mLiveChatRoomLinkMicAgoraVh != null) {
            mLiveChatRoomLinkMicAgoraVh.anchorJoinChannel(token, channelId);
        }
        startCountDown();
    }

    @Override
    public void onPause() {
    }


    @Override
    public void onResume() {
    }

    @Override
    public ViewGroup getContainer() {
        return mContainer;
    }


    @Override
    public void changeToLeft() {
    }

    @Override
    public void changeToBig() {
    }

    /**
     * 切换镜像
     */
    @Override
    public void togglePushMirror() {

    }


    /**
     * 切换镜头
     */
    @Override
    public void toggleCamera() {
    }

    /**
     * 打开关闭闪光灯
     */
    @Override
    public void toggleFlash() {
    }

    /**
     * 开始推流
     *
     * @param pushUrl 推流地址
     */
    @Override
    public void startPush(String pushUrl) {
    }


    @Override
    public void startBgm(String path) {
        mPlayBgm = mLiveChatRoomLinkMicAgoraVh.startBgm(path);
    }

    @Override
    public void pauseBgm() {
//        if (mPlayBgm&&mLivePusher != null) {
//            TXAudioEffectManager audioEffectManager = mLivePusher.getAudioEffectManager();
//            if (audioEffectManager != null) {
//                audioEffectManager.pausePlayMusic(1);
//            }
//        }
    }

    @Override
    public void resumeBgm() {
//        if (mPlayBgm&&mLivePusher != null) {
//            TXAudioEffectManager audioEffectManager = mLivePusher.getAudioEffectManager();
//            if (audioEffectManager != null) {
//                audioEffectManager.resumePlayMusic(1);
//            }
//        }
    }

    @Override
    public void stopBgm() {
        if (mPlayBgm && mLiveChatRoomLinkMicAgoraVh != null) {
            mLiveChatRoomLinkMicAgoraVh.stopBgm();
        }
        mPlayBgm = false;
    }

    @Override
    protected void onCameraRestart() {
    }

    @Override
    public void release() {
        super.release();
        if (mLiveChatRoomLinkMicAgoraVh != null) {
            mLiveChatRoomLinkMicAgoraVh.anchorRelease();
        }
        mLiveChatRoomLinkMicAgoraVh = null;
    }


    /**
     * 设置静音
     */
    @Override
    public void setPushMute(boolean pushMute) {
        if (mLiveChatRoomLinkMicAgoraVh != null) {
            mLiveChatRoomLinkMicAgoraVh.setPushMute(pushMute);
        }
    }


}
