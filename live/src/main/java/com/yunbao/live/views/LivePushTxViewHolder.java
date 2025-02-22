package com.yunbao.live.views;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.liteav.audio.TXAudioEffectManager;
import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.liteav.device.TXDeviceManager;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePusher;
import com.tencent.live2.V2TXLivePusherObserver;
import com.tencent.live2.impl.V2TXLivePusherImpl;
import com.yunbao.beauty.bean.MeiYanValueBean;
import com.yunbao.beauty.interfaces.IBeautyEffectListener;
import com.yunbao.beauty.utils.MhDataManager;
import com.yunbao.beauty.utils.SimpleDataManager;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.BitmapUtil;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.ScreenDimenUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.live.LiveConfig;
import com.yunbao.live.R;
import com.yunbao.live.bean.LiveConfigBean;
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;
import com.yunbao.live.interfaces.TxLinkMicPusher;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by cxf on 2018/10/7.
 * 腾讯云直播推流
 */

public class LivePushTxViewHolder extends AbsLivePushViewHolder implements TxLinkMicPusher {

    private V2TXLivePusher mLivePusher;
    private boolean mMirror;
    private LiveConfigBean mLiveConfigBean;
    private boolean mPlayBgm;
    private boolean mPushSucceed;//是否推流成功
    private boolean mMhBeautyEnable;//是否使用美狐美颜

    public LivePushTxViewHolder(Context context, ViewGroup parentView, LiveConfigBean liveConfigBean) {
        super(context, parentView, liveConfigBean);
    }

    @Override
    protected void processArguments(Object... args) {
        if (args.length > 0) {
            mLiveConfigBean = (LiveConfigBean) args[0];
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_push_tx;
    }

    @Override
    public void init() {
        super.init();
        mMhBeautyEnable = CommonAppConfig.getInstance().isMhBeautyEnable();
        if (mLiveConfigBean == null) {
            mLiveConfigBean = LiveConfig.getDefaultTxConfig();
        }
        mLivePusher = new V2TXLivePusherImpl(mContext, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTC);
        V2TXLiveDef.V2TXLiveVideoResolution videoResolution = null;
        int resolution = mLiveConfigBean.getTargetResolution();
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
        videoParam.videoFps = mLiveConfigBean.getTargetFps();
        videoParam.minVideoBitrate = mLiveConfigBean.getVideoKBitrateMin();
        videoParam.videoBitrate = mLiveConfigBean.getVideoKBitrateMax();
        mLivePusher.setVideoQuality(videoParam);
        mLivePusher.setAudioQuality(V2TXLiveDef.V2TXLiveAudioQuality.V2TXLiveAudioQualityDefault);
        mMirror = true;
        mLivePusher.setEncoderMirror(mMirror);
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
                getBeautyValue();
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

            @Override
            public int onProcessVideoFrame(V2TXLiveDef.V2TXLiveVideoFrame srcFrame, V2TXLiveDef.V2TXLiveVideoFrame dstFrame) {
                if (mMhBeautyEnable) {
                    dstFrame.texture.textureId = MhDataManager.getInstance().render(srcFrame.texture.textureId, srcFrame.width, srcFrame.height);
                }
                return 0;
            }

        });
        mPreView = findViewById(R.id.camera_preview);
        mLivePusher.setRenderView((TextureView) mPreView);
        mLivePusher.startCamera(mCameraFront);
        mLivePusher.startMicrophone();
    }


    /**
     * 获取美颜参数
     */
    private void getBeautyValue() {
        CommonHttpUtil.getBeautyValue(new HttpCallback() {

            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (mMhBeautyEnable) {
                        MhDataManager.getInstance().init().setMeiYanChangedListener(getMeiYanChangedListener());
                        MeiYanValueBean meiYanValueBean = JSON.parseObject(info[0], MeiYanValueBean.class);
                        MhDataManager.getInstance()
                                .setMeiYanValue(meiYanValueBean)
                                .useMeiYan().restoreBeautyValue();
                        if (mLivePusher != null) {
                            mLivePusher.enableCustomVideoProcess(true,
                                    V2TXLiveDef.V2TXLivePixelFormat.V2TXLivePixelFormatTexture2D,
                                    V2TXLiveDef.V2TXLiveBufferType.V2TXLiveBufferTypeTexture);
                        }
                    } else {
                        SimpleDataManager.getInstance().create().setMeiYanChangedListener(getMeiYanChangedListener());
                        int meiBai = obj.getIntValue("skin_whiting");
                        int moPi = obj.getIntValue("skin_smooth");
                        int hongRun = obj.getIntValue("skin_tenderness");
                        SimpleDataManager.getInstance().setData(meiBai, moPi, hongRun);
                    }
                }
            }
        });
    }

    @Override
    public IBeautyEffectListener getMeiYanChangedListener() {
        return new IBeautyEffectListener() {
            @Override
            public void onMeiYanChanged(int meiBai, boolean meiBaiChanged, int moPi, boolean moPiChanged, int hongRun, boolean hongRunChanged) {
                if (meiBaiChanged || moPiChanged || hongRunChanged) {
                    if (mLivePusher != null) {
                        TXBeautyManager txBeautyManager = mLivePusher.getBeautyManager();
                        if (txBeautyManager != null) {
                            if (meiBaiChanged) {
                                txBeautyManager.setWhitenessLevel(meiBai);
                            }
                            if (moPiChanged) {
                                txBeautyManager.setBeautyLevel(moPi);
                            }
                            if (hongRunChanged) {
                                txBeautyManager.setWhitenessLevel(hongRun);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFilterChanged(int filterName) {
                if (!CommonAppConfig.getInstance().isMhBeautyEnable()) {
                    if (mLivePusher != null) {
                        TXBeautyManager manager = mLivePusher.getBeautyManager();
                        if (manager != null) {
                            if (filterName == 0) {
                                manager.setFilter(null);
                            } else {
                                Bitmap bitmap = BitmapUtil.getInstance().decodeBitmap(filterName);
                                manager.setFilter(bitmap);
                            }
                        }
                    }
                }
            }

            @Override
            public boolean isUseMhFilter() {
                return true;
            }

            @Override
            public boolean isTieZhiEnable() {
                return !mIsPlayGiftSticker;
            }
        };
    }


    @Override
    public void changeToLeft() {
        if (mPreView != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mPreView.getLayoutParams();
            params.width = mPreView.getWidth() / 2;
            params.height = DpUtil.dp2px(250);
            params.topMargin = DpUtil.dp2px(130);
            mPreView.setLayoutParams(params);
        }
    }

    @Override
    public void changeToBig() {
        if (mPreView != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mPreView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.topMargin = 0;
            mPreView.setLayoutParams(params);
        }
    }

    /**
     * 切换镜像
     */
    @Override
    public void togglePushMirror() {
        if (mLivePusher != null) {
            mMirror = !mMirror;
            mLivePusher.setEncoderMirror(mMirror);
            if (!mMirror) {
                ToastUtil.show(R.string.live_mirror_1);
            } else {
                ToastUtil.show(R.string.live_mirror_0);
            }
        }
    }


    /**
     * 切换镜头
     */
    @Override
    public void toggleCamera() {
        if (mLivePusher != null) {
            TXDeviceManager deviceManager = mLivePusher.getDeviceManager();
            if (deviceManager != null) {
                if (mFlashOpen) {
                    deviceManager.enableCameraTorch(false);
                    mFlashOpen = false;
                }
                mCameraFront = !mCameraFront;
                deviceManager.switchCamera(mCameraFront);
                if (!mCameraFront) {
                    mLivePusher.setEncoderMirror(false);
                } else {
                    mLivePusher.setEncoderMirror(mMirror);
                }
            }
        }
    }

    /**
     * 打开关闭闪光灯
     */
    @Override
    public void toggleFlash() {
        if (mCameraFront) {
            ToastUtil.show(R.string.live_open_flash);
            return;
        }
        if (mLivePusher != null) {
            boolean open = !mFlashOpen;
            TXDeviceManager deviceManager = mLivePusher.getDeviceManager();
            if (deviceManager != null) {
                if (deviceManager.enableCameraTorch(open)) {
                    mFlashOpen = open;
                }
            }
        }
    }

    /**
     * 开始推流
     *
     * @param pushUrl 推流地址
     */
    @Override
    public void startPush(String pushUrl) {
        mStartPush = true;
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
                audioEffectManager.setAllMusicVolume(70);//设置所有背景音乐的本地音量和远端音量的大小,如果将 volume 设置成 100 之后感觉音量还是太小，可以将 volume 最大设置成 150，但超过 100 的 volume 会有爆音的风险，请谨慎操作。
                audioEffectManager.setVoiceEarMonitorVolume(70);//通过该接口您可以设置耳返特效中声音的音量大小。取值范围为0 - 100，默认值：100。如果将 volume 设置成 100 之后感觉音量还是太小，可以将 volume 最大设置成 150，但超过 100 的 volume 会有爆音的风险，请谨慎操作。
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
//        if (mPlayBgm && mLivePusher != null) {
//            TXAudioEffectManager audioEffectManager = mLivePusher.getAudioEffectManager();
//            if (audioEffectManager != null) {
//                audioEffectManager.pausePlayMusic(1);
//            }
//        }
    }

    @Override
    public void resumeBgm() {
//        if (mPlayBgm && mLivePusher != null) {
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
//        if (mLivePusher != null && mPreView != null) {
//            mLivePusher.startCameraPreview((TXCloudVideoView) mPreView);
//        }
    }

    @Override
    public void release() {
        super.release();
        CommonHttpUtil.cancel(CommonHttpConsts.GET_BEAUTY_VALUE);
        LiveHttpUtil.cancel(LiveHttpConsts.LINK_MIC_TX_MIX_STREAM);
        JSONObject obj = new JSONObject();
        obj.put("method", "endlive");
        sendSeiMessage(obj.toJSONString());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                releasePusher();
            }
        }, 500);
    }

    private void releasePusher() {
        if (mLivePusher != null) {
            if (mLivePusher.isPushing() == 1) {
                mLivePusher.stopPush();
            }
            stopBgm();
            mLivePusher.stopMicrophone();
            mLivePusher.stopCamera();
            mLivePusher.setObserver(null);
            mLivePusher.release();
            if (mMhBeautyEnable) {
                MhDataManager.getInstance().release();
            } else {
                SimpleDataManager.getInstance().release();
            }
        }
        mLivePusher = null;
    }

    @Override
    public void onDestroy() {
        releasePusher();
        super.onDestroy();
    }

    private Bitmap decodeResource(Resources resources, int id) {
        TypedValue value = new TypedValue();
        resources.openRawResource(id, value);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTargetDensity = value.density;
        return BitmapFactory.decodeResource(resources, id, opts);
    }


    /**
     * 腾讯sdk连麦时候主播混流
     *
     * @param linkMicType 混流类型 1主播与主播连麦  0 用户与主播连麦
     * @param touid       连麦者的uid
     * @param toStream    连麦者的stream
     */
    @Override
    public void onLinkMicTxMixStreamEvent(int linkMicType, String touid, String toStream) {
        if (linkMicType == Constants.LINK_MIC_TYPE_NORMAL) {
            mixStreamWithLinkMicUser(touid, toStream);
        } else {
            mixStreamWithAnchor(touid, toStream);
        }
    }

    /**
     * 主播与连麦观众进行混流
     *
     * @param touid    连麦者的uid
     * @param toStream 连麦者的stream
     */
    private void mixStreamWithLinkMicUser(String touid, String toStream) {
        if (mLivePusher == null) {
            return;
        }
        if (!TextUtils.isEmpty(touid) && !TextUtils.isEmpty(toStream)) {
            V2TXLiveDef.V2TXLiveTranscodingConfig config = new V2TXLiveDef.V2TXLiveTranscodingConfig();
            config.videoWidth = ScreenDimenUtil.getInstance().getScreenWidth();
            config.videoHeight = ScreenDimenUtil.getInstance().getScreenRealHeight();
            config.videoBitrate = 2500;
            config.videoFramerate = 15;
            config.videoGOP = 2;
            config.audioSampleRate = 48000;
            config.audioBitrate = 64;
            config.audioChannels = 2;
            config.mixStreams = new ArrayList<>();
            // 主播摄像头的画面位置
            V2TXLiveDef.V2TXLiveMixStream local = new V2TXLiveDef.V2TXLiveMixStream();
            local.userId = CommonAppConfig.getInstance().getUid();
            local.streamId = null; // 本地画面不用填写 streamID，远程需要
            local.x = 0;
            local.y = 0;
            local.width = ScreenDimenUtil.getInstance().getScreenWidth();
            local.height = ScreenDimenUtil.getInstance().getScreenRealHeight();
            local.zOrder = 0;   // zOrder 为 0 代表主播画面位于最底层
            config.mixStreams.add(local);
            //连麦者的画面位置
            V2TXLiveDef.V2TXLiveMixStream remoteA = new V2TXLiveDef.V2TXLiveMixStream();
            remoteA.userId = touid;
            remoteA.streamId = toStream; // 本地画面不用填写 streamID，远程需要
            remoteA.x = (int) (ScreenDimenUtil.getInstance().getScreenWidth() * 0.75f); //仅供参考
            remoteA.y = (int) (ScreenDimenUtil.getInstance().getScreenHeight() * 0.75f) - DpUtil.dp2px(120) + ScreenDimenUtil.getInstance().getNavigationBarHeight(); //仅供参考
            remoteA.width = (int) (ScreenDimenUtil.getInstance().getScreenWidth() * 0.25f); //仅供参考
            remoteA.height = (int) (ScreenDimenUtil.getInstance().getScreenHeight() * 0.25f); //仅供参考
            remoteA.zOrder = 1;
            config.mixStreams.add(remoteA);
            // 发起云端混流
            mLivePusher.setMixTranscodingConfig(config);
        } else {
            V2TXLiveDef.V2TXLiveTranscodingConfig config = new V2TXLiveDef.V2TXLiveTranscodingConfig();
            config.videoWidth = 540;
            config.videoHeight = 960;
            config.videoBitrate = 1500;
            config.videoFramerate = 15;
            config.videoGOP = 2;
            config.audioSampleRate = 48000;
            config.audioBitrate = 64;
            config.audioChannels = 2;
            config.mixStreams = new ArrayList<>();

            // 主播摄像头的画面位置
            V2TXLiveDef.V2TXLiveMixStream local = new V2TXLiveDef.V2TXLiveMixStream();
            local.userId = CommonAppConfig.getInstance().getUid();
            local.streamId = null; // 本地画面不用填写 streamID，远程需要
            local.x = 0;
            local.y = 0;
            local.width = 540;
            local.height = 960;
            local.zOrder = 0;   // zOrder 为 0 代表主播画面位于最底层
            config.mixStreams.add(local);
            mLivePusher.setMixTranscodingConfig(config);
        }

    }


    /**
     * 主播与另一个主播进行混流
     *
     * @param touid    连麦者的uid
     * @param toStream 连麦者的stream
     */
    private void mixStreamWithAnchor(String touid, String toStream) {
        if (mLivePusher == null) {
            return;
        }
        if (!TextUtils.isEmpty(touid) && !TextUtils.isEmpty(toStream)) {
            V2TXLiveDef.V2TXLiveTranscodingConfig config = new V2TXLiveDef.V2TXLiveTranscodingConfig();
            config.videoWidth = 540;
            config.videoHeight = 360;
            config.videoBitrate = 1500;
            config.videoFramerate = 15;
            config.videoGOP = 2;
            config.audioSampleRate = 48000;
            config.audioBitrate = 64;
            config.audioChannels = 2;
            config.mixStreams = new ArrayList<>();
            // 主播摄像头的画面位置
            V2TXLiveDef.V2TXLiveMixStream local = new V2TXLiveDef.V2TXLiveMixStream();
            local.userId = CommonAppConfig.getInstance().getUid();
            local.streamId = null; // 本地画面不用填写 streamID，远程需要
            local.x = 0;
            local.y = 0;
            local.width = 270;
            local.height = 360;
            local.zOrder = 0;   // zOrder 为 0 代表主播画面位于最底层
            config.mixStreams.add(local);
            //连麦者的画面位置
            V2TXLiveDef.V2TXLiveMixStream remoteA = new V2TXLiveDef.V2TXLiveMixStream();
            remoteA.userId = touid;
            remoteA.streamId = toStream; // 本地画面不用填写 streamID，远程需要
            remoteA.x = 270; //仅供参考
            remoteA.y = 0; //仅供参考
            remoteA.width = 270; //仅供参考
            remoteA.height = 360; //仅供参考
            remoteA.zOrder = 1;
            config.mixStreams.add(remoteA);
            // 发起云端混流
            mLivePusher.setMixTranscodingConfig(config);
        } else {
            V2TXLiveDef.V2TXLiveTranscodingConfig config = new V2TXLiveDef.V2TXLiveTranscodingConfig();
            config.videoWidth = 540;
            config.videoHeight = 960;
            config.videoBitrate = 1500;
            config.videoFramerate = 15;
            config.videoGOP = 2;
            config.audioSampleRate = 48000;
            config.audioBitrate = 64;
            config.audioChannels = 2;
            config.mixStreams = new ArrayList<>();
            // 主播摄像头的画面位置
            V2TXLiveDef.V2TXLiveMixStream local = new V2TXLiveDef.V2TXLiveMixStream();
            local.userId = CommonAppConfig.getInstance().getUid();
            local.streamId = null; // 本地画面不用填写 streamID，远程需要
            local.x = 0;
            local.y = 0;
            local.width = 540;
            local.height = 960;
            local.zOrder = 0;   // zOrder 为 0 代表主播画面位于最底层
            config.mixStreams.add(local);
            // 发起云端混流
            mLivePusher.setMixTranscodingConfig(config);
        }

    }

    public void sendSeiMessage(String msg) {
        L.e(TAG, "V2TXLivePusherObserver--sendSeiMessage--发送消息--->msg: " + msg);
        if (mLivePusher != null) {
            byte[] data = null;
            try {
                data = msg.getBytes(StandardCharsets.UTF_8);
            } catch (Exception e) {
                data = null;
            }
            if (data != null && data.length > 0) {
                mLivePusher.sendSeiMessage(242, data);
            }
        }
    }


}
