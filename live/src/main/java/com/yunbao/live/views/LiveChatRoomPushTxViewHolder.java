package com.yunbao.live.views;


import android.content.Context;
import android.os.Bundle;
import android.view.TextureView;
import android.view.ViewGroup;

import com.tencent.liteav.audio.TXAudioEffectManager;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePusher;
import com.tencent.live2.V2TXLivePusherObserver;
import com.tencent.live2.impl.V2TXLivePusherImpl;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.utils.L;
import com.yunbao.live.LiveConfig;
import com.yunbao.live.R;
import com.yunbao.live.bean.LiveConfigBean;
import com.yunbao.live.bean.LiveVoiceMixUserBean;
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/10/7.
 * 腾讯云直播推流 语音聊天室
 */

public class LiveChatRoomPushTxViewHolder extends AbsLiveChatRoomPushViewHolder  {

    private V2TXLivePusher mLivePusher;
    private ViewGroup mContainer;
    //    private boolean mPaused;
    private boolean mPlayBgm;
    private boolean mPushSucceed;//是否推流成功
    private boolean mPushMute;

    public LiveChatRoomPushTxViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public LiveChatRoomPushTxViewHolder(Context context, ViewGroup parentView, Object... args) {
        super(context, parentView, args);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_push_tx_voice;
    }

    @Override
    public void init() {
        mContainer = findViewById(R.id.voice_push_container);
        mLivePusher = new V2TXLivePusherImpl(mContext, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTC);
        mLivePusher.setObserver(new V2TXLivePusherObserver() {

            @Override
            public void onError(int code, String msg, Bundle extraInfo) {
                L.e(TAG, "V2TXLivePusherObserver--onError: code=" + code + ", msg= " + msg);
            }

            @Override
            public void onWarning(int code, String msg, Bundle extraInfo) {
                L.e(TAG, "V2TXLivePusherObserver--onWarning: code=" + code + ", msg= " + msg);
            }

            @Override
            public void onCaptureFirstAudioFrame() {
                L.e(TAG, "V2TXLivePusherObserver--onCaptureFirstAudioFrame: 获得首帧音频");
            }

            @Override
            public void onCaptureFirstVideoFrame() {
                L.e(TAG, "V2TXLivePusherObserver--onCaptureFirstVideoFrame: 获得首帧视频");
                if (mLivePushListener != null) {
                    mLivePushListener.onPreviewStart();
                }
            }

            @Override
            public void onPushStatusUpdate(V2TXLiveDef.V2TXLivePushStatus status, String msg, Bundle extraInfo) {
                L.e(TAG, "V2TXLivePusherObserver--onPushStatusUpdate: msg=" + msg);
                if (status == V2TXLiveDef.V2TXLivePushStatus.V2TXLivePushStatusConnectSuccess) {
                    if (!mPushSucceed) {
                        mPushSucceed = true;
                        if (mLivePushListener != null) {
                            mLivePushListener.onPushStart();
                        }
                        L.e(TAG, "onPushStatusUpdate--->推流成功");
                    }
                }
            }
        });

    }

    @Override
    public void startMicrophone() {
        if (mLivePusher != null) {
            mLivePusher.startMicrophone();
        }
    }
    @Override
    public void startPreview(LiveConfigBean liveConfigBean, TextureView textureView) {
        if (mLivePusher == null) {
            return;
        }
        if (liveConfigBean == null) {
            liveConfigBean = LiveConfig.getDefaultTxConfig();
        }
        V2TXLiveDef.V2TXLiveVideoResolution videoResolution = null;
        int resolution = liveConfigBean.getTargetResolution();
        if (resolution == 1) {
            videoResolution = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution960x540;
        } else if (resolution == 2) {
            videoResolution = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution1280x720;
        } else if (resolution == 3) {
            videoResolution = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution1920x1080;
        } else {
            videoResolution = V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution960x540;
        }
        V2TXLiveDef.V2TXLiveVideoEncoderParam videoParam = new V2TXLiveDef.V2TXLiveVideoEncoderParam(videoResolution);
        videoParam.videoResolutionMode = V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModePortrait;
        videoParam.videoFps = liveConfigBean.getTargetFps();
        videoParam.minVideoBitrate = liveConfigBean.getVideoKBitrateMin();
        videoParam.videoBitrate = liveConfigBean.getVideoKBitrateMax();
        mLivePusher.setVideoQuality(videoParam);
        mLivePusher.setAudioQuality(V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityDefault);
        mLivePusher.setEncoderMirror(true);
        mLivePusher.setRenderView(textureView);
        mLivePusher.startCamera(true);
        mLivePusher.startMicrophone();
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
        if (mLivePusher != null) {
            mLivePusher.startPush(pushUrl);
        }
        startCountDown();
    }


    @Override
    public void startBgm(String path) {
        if (mLivePusher != null) {
            TXAudioEffectManager audioEffectManager = mLivePusher.getAudioEffectManager();
            if (audioEffectManager != null) {
                audioEffectManager.setAllMusicVolume(40);//设置所有背景音乐的本地音量和远端音量的大小,如果将 volume 设置成 100 之后感觉音量还是太小，可以将 volume 最大设置成 150，但超过 100 的 volume 会有爆音的风险，请谨慎操作。
                audioEffectManager.setVoiceEarMonitorVolume(40);//通过该接口您可以设置耳返特效中声音的音量大小。取值范围为0 - 100，默认值：100。如果将 volume 设置成 100 之后感觉音量还是太小，可以将 volume 最大设置成 150，但超过 100 的 volume 会有爆音的风险，请谨慎操作。
                TXAudioEffectManager.AudioMusicParam audioMusicParam = new TXAudioEffectManager.AudioMusicParam(1, path);
                audioMusicParam.publish = true;
                audioMusicParam.endTimeMS = 0;
                audioEffectManager.startPlayMusic(audioMusicParam);
                mPlayBgm = true;
            }
        }
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
        if (mPlayBgm && mLivePusher != null) {
            TXAudioEffectManager audioEffectManager = mLivePusher.getAudioEffectManager();
            if (audioEffectManager != null) {
                audioEffectManager.stopPlayMusic(1);
            }
        }
        mPlayBgm = false;
    }

    @Override
    protected void onCameraRestart() {
    }

    @Override
    public void release() {
        super.release();
        LiveHttpUtil.cancel(LiveHttpConsts.LINK_MIC_TX_MIX_STREAM);
        if (mLivePusher != null) {
            if (mLivePusher.isPushing() == 1) {
                mLivePusher.stopPush();
            }
            stopBgm();
            mLivePusher.stopCamera();
            mLivePusher.stopMicrophone();
            mLivePusher.setObserver(null);
            mLivePusher.release();
        }
        mLivePusher = null;
    }


    /**
     * 语音直播间主播混流
     *
     * @param userStreamList 上麦观众的流名
     */
    @Override
    public void voiceRoomAnchorMixVideo(List<LiveVoiceMixUserBean> userStreamList) {
        if (mLivePusher == null) {
            return;
        }
        if (userStreamList == null || userStreamList.size() == 0) {
            V2TXLiveDef.V2TXLiveTranscodingConfig config = new V2TXLiveDef.V2TXLiveTranscodingConfig();
            config.videoWidth = 720;
            config.videoHeight = 720;
            config.videoBitrate = 1500;
            config.videoFramerate = 15;
            config.videoGOP = 2;
            config.audioSampleRate = 48000;
            config.audioBitrate = 64;
            config.audioChannels = 2;
            config.backgroundImage="236298";
            config.mixStreams = new ArrayList<>();
            // 主播摄像头的画面位置
            V2TXLiveDef.V2TXLiveMixStream local = new V2TXLiveDef.V2TXLiveMixStream();
            local.userId = CommonAppConfig.getInstance().getUid();
            local.streamId = null; // 本地画面不用填写 streamID，远程需要
            local.x = 0;
            local.y = 0;
            local.width = 720;
            local.height = 720;
            config.mixStreams.add(local);
            // 发起云端混流
            mLivePusher.setMixTranscodingConfig(config);
        } else {
            V2TXLiveDef.V2TXLiveTranscodingConfig config = new V2TXLiveDef.V2TXLiveTranscodingConfig();
            config.videoWidth = 1080;
            config.videoHeight = 720;
            config.videoBitrate = 1500;
            config.videoFramerate = 15;
            config.videoGOP = 2;
            config.audioSampleRate = 48000;
            config.audioBitrate = 64;
            config.audioChannels = 2;
            config.backgroundImage="236298";
            config.mixStreams = new ArrayList<>();
            // 主播摄像头的画面位置
            V2TXLiveDef.V2TXLiveMixStream local = new V2TXLiveDef.V2TXLiveMixStream();
            local.userId = CommonAppConfig.getInstance().getUid();
            local.streamId = null; // 本地画面不用填写 streamID，远程需要
            local.x = 0;
            local.y = 0;
            local.width = 360;
            local.height = 360;
            local.zOrder = 0;
            config.mixStreams.add(local);

            //上麦观众的
            for (int i = 0; i < userStreamList.size(); i++) {
                LiveVoiceMixUserBean bean = userStreamList.get(i);
                V2TXLiveDef.V2TXLiveMixStream remote = new V2TXLiveDef.V2TXLiveMixStream();
                remote.userId = bean.getUid();
                remote.streamId = bean.getStream();
                int index = bean.getIndex();
                remote.x = (index % 3) * 360;
                remote.y = (index / 3) * 360;
                remote.width = 360;
                remote.height = 360;
                remote.zOrder = index;
                config.mixStreams.add(remote);
            }
            // 发起云端混流
            mLivePusher.setMixTranscodingConfig(config);
        }
    }

    /**
     * 语音直播间主播混流
     *
     * @param userStreamList 上麦观众的流名
     */
    @Override
    public void voiceRoomAnchorMix(List<LiveVoiceMixUserBean> userStreamList) {
        if (mLivePusher == null) {
            return;
        }
        V2TXLiveDef.V2TXLiveTranscodingConfig config = new V2TXLiveDef.V2TXLiveTranscodingConfig();
        config.audioSampleRate = 48000;
        config.audioBitrate = 64;
        config.audioChannels = 2;
        config.mixStreams = new ArrayList<>();
        // 主播的
        V2TXLiveDef.V2TXLiveMixStream local = new V2TXLiveDef.V2TXLiveMixStream();
        local.userId = CommonAppConfig.getInstance().getUid();
        local.streamId = null; // 本地画面不用填写 streamID，远程需要
        local.inputType = V2TXLiveDef.V2TXLiveMixInputType.V2TXLiveMixInputTypePureAudio;
        config.mixStreams.add(local);

        //上麦观众的
        int size = 0;
        if (userStreamList != null && (size = userStreamList.size()) > 0) {
            for (int i = 0; i < size; i++) {
                LiveVoiceMixUserBean bean = userStreamList.get(i);
                V2TXLiveDef.V2TXLiveMixStream remote = new V2TXLiveDef.V2TXLiveMixStream();
                remote.userId = bean.getUid();
                remote.streamId = bean.getStream();
                remote.inputType = V2TXLiveDef.V2TXLiveMixInputType.V2TXLiveMixInputTypePureAudio;
                config.mixStreams.add(remote);

            }
        }
        // 发起云端混流
        mLivePusher.setMixTranscodingConfig(config);
    }


    /**
     * 设置静音
     */
    @Override
    public void setPushMute(boolean pushMute) {
        if (mPushMute != pushMute) {
            mPushMute = pushMute;
            if (mLivePusher != null) {
                TXAudioEffectManager effectManager = mLivePusher.getAudioEffectManager();
                if (effectManager != null) {
                    effectManager.setVoiceCaptureVolume(pushMute ? 0 : 100);
                }
            }
        }
    }


}
