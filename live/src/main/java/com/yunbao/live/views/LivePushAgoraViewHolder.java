package com.yunbao.live.views;


import static io.agora.rtc2.Constants.AUDIO_PROFILE_MUSIC_STANDARD;
import static io.agora.rtc2.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
import static io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER;
import static io.agora.rtc2.Constants.LOCAL_VIDEO_STREAM_STATE_CAPTURING;
import static io.agora.rtc2.Constants.PUB_STATE_PUBLISHED;
import static io.agora.rtc2.Constants.RELAY_STATE_FAILURE;
import static io.agora.rtc2.Constants.RELAY_STATE_RUNNING;
import static io.agora.rtc2.Constants.ScreenScenarioType.SCREEN_SCENARIO_GAMING;
import static io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_DISABLED;
import static io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_ENABLED;
import static io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;

import android.content.Context;
import android.os.Handler;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.beauty.bean.MeiYanValueBean;
import com.yunbao.beauty.interfaces.IBeautyEffectListener;
import com.yunbao.beauty.utils.MhDataManager;
import com.yunbao.beauty.utils.SimpleDataManager;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.ScreenDimenUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.live.R;
import com.yunbao.live.activity.LiveActivity;
import com.yunbao.live.http.LiveHttpUtil;

import java.nio.charset.StandardCharsets;

import io.agora.base.TextureBuffer;
import io.agora.base.TextureBufferHelper;
import io.agora.base.VideoFrame;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.DataStreamConfig;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.ScreenCaptureParameters;
import io.agora.rtc2.gl.EglBaseProvider;
import io.agora.rtc2.video.BeautyOptions;
import io.agora.rtc2.video.ChannelMediaInfo;
import io.agora.rtc2.video.ChannelMediaRelayConfiguration;
import io.agora.rtc2.video.IVideoFrameObserver;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoDenoiserOptions;
import io.agora.rtc2.video.VideoEncoderConfiguration;

/**
 * Created by cxf on 2018/10/7.
 * 声网直播推流
 */

public class LivePushAgoraViewHolder extends AbsLivePushViewHolder {

    private boolean mMirror;
    private boolean mRealMirror;
    private boolean mPlayBgm;
    private boolean mPushSucceed;//是否推流成功
    private boolean mMhBeautyEnable;//是否使用美狐美颜
    private RtcEngineEx mEngine;
    private Handler mHandler;
    private boolean mCameraReady;
    private int mUid;
    private VideoEncoderConfiguration mVideoEncoderConfiguration;
    private int mLinkMicAudienceUid;//观众连麦，连麦观众的uid
    private int mLinkMicAnchorUid;//主播连麦，对方主播的uid
    private boolean mMediaRelaying;//是否在进行跨频道媒体流转发
    private boolean mTryMediaRelayAgain;//跨频道媒体流转发 尝试重连
    private boolean mIsScreenRecord;
    private int mDataStreamId;
    private TextureBufferHelper mTextureBufferHelper;
    private ChannelMediaRelayData mChannelMediaRelayData;

    //声网自带的美颜效果
    private BeautyOptions mBeautyOptions;

    public LivePushAgoraViewHolder(Context context, ViewGroup parentView, boolean isScreenRecord) {
        super(context, parentView, isScreenRecord);
    }

    @Override
    protected void processArguments(Object... args) {
        if (args.length > 0) {
            mIsScreenRecord = (boolean) args[0];
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_push_agora;
    }

    @Override
    public void init() {
        super.init();
        mMhBeautyEnable = CommonAppConfig.getInstance().isMhBeautyEnable();
        if (mIsScreenRecord) {
            ImageView cover = findViewById(R.id.live_cover);
            cover.setVisibility(View.VISIBLE);
            UserBean u = CommonAppConfig.getInstance().getUserBean();
            if (u != null) {
                ImgLoader.displayBlur(mContext, u.getAvatar(), cover);
            }
            mPreView = cover;
        } else {
            mPreView = findViewById(R.id.camera_preview);
        }
        mHandler = new Handler();
        mUid = Integer.parseInt(CommonAppConfig.getInstance().getUid());
        mMirror = true;
        mRealMirror = mMirror;
        createEngine(new Runnable() {
            @Override
            public void run() {
                if (mEngine != null) {
                    mEngine.setClientRole(CLIENT_ROLE_BROADCASTER);
                    mEngine.enableVideo();
                    VideoEncoderConfiguration encoderConfiguration = new VideoEncoderConfiguration(
                            VideoEncoderConfiguration.VD_1280x720,
                            VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                            VideoEncoderConfiguration.STANDARD_BITRATE,
                            VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
                    );
                    encoderConfiguration.orientationMode = ORIENTATION_MODE_FIXED_PORTRAIT;
                    mVideoEncoderConfiguration = encoderConfiguration;
                    if (!mIsScreenRecord) {
                        mEngine.setupLocalVideo(new VideoCanvas(mPreView, RENDER_MODE_HIDDEN, mUid));
                        mEngine.startPreview();
                        mEngine.setLocalRenderMode(RENDER_MODE_HIDDEN, mRealMirror ? VIDEO_MIRROR_MODE_ENABLED : VIDEO_MIRROR_MODE_DISABLED);
                        encoderConfiguration.mirrorMode = mRealMirror ? VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED
                                : VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_DISABLED;
                    }
                    mEngine.setVideoEncoderConfiguration(encoderConfiguration);
                }
            }
        });

    }

    private void createEngine(final Runnable onSuccess){
        new Thread(new Runnable(){

            @Override
            public void run() {
                RtcEngineConfig config = new RtcEngineConfig();
                config.mContext = CommonAppContext.getInstance();
                config.mAppId = CommonAppConfig.getInstance().getConfig().getAgoraAppId();
                config.mChannelProfile = CHANNEL_PROFILE_LIVE_BROADCASTING;
                config.mAudioScenario = AUDIO_PROFILE_MUSIC_STANDARD;
                IRtcEngineEventHandler engineEventHandler = new IRtcEngineEventHandler() {
                    @Override
                    public void onError(int err) {
                        L.e(TAG, "IRtcEngineEventHandler---onError--->" + err);
                    }

                    @Override
                    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                        L.e(TAG, "IRtcEngineEventHandler---onJoinChannelSuccess--->channel: " + channel + " uid: " + uid);

                    }

                    @Override
                    public void onLeaveChannel(RtcStats stats) {
                        L.e(TAG, "IRtcEngineEventHandler---onLeaveChannel--->");
                    }

                    @Override
                    public void onUserJoined(int uid, int elapsed) {
                        L.e(TAG, "IRtcEngineEventHandler---onUserJoined--->" + uid);
                        if (mHandler != null) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mEngine != null) {
                                        int linkMicUid = ((LiveActivity) mContext).getLinkMicUid();
                                        if (linkMicUid == uid) {
                                            if (mLinkMicAudienceUid == 0) {
                                                L.e(TAG, "IRtcEngineEventHandler---观众连麦--->" + uid);
                                                mLinkMicAudienceUid = uid;
                                                TextureView textureView = new TextureView(mContext);
                                                mSmallContainer.addView(textureView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                                                VideoCanvas remote = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, uid);
                                                mEngine.setupRemoteVideo(remote);
                                                JSONObject obj = new JSONObject();
                                                obj.put("method", "ConnectVideo");
                                                obj.put("uid", uid);
                                                sendSeiMessage(obj.toJSONString());
                                            }
                                        } else {
                                            int pkUid = ((LiveActivity) mContext).getLinkMicAnchorUid();
                                            if (pkUid == uid) {
                                                onAnchorLinkMic(uid, true);
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onUserOffline(int uid, int reason) {
                        L.e(TAG, "IRtcEngineEventHandler---onUserOffline--->" + uid);
                        if (mHandler != null) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mEngine != null) {
                                        if (mLinkMicAudienceUid == uid) {
                                            L.e(TAG, "IRtcEngineEventHandler---观众连麦断开--->" + uid);
                                            mEngine.setupRemoteVideo(new VideoCanvas(null, RENDER_MODE_HIDDEN, uid));
                                            mSmallContainer.removeAllViews();
                                            mLinkMicAudienceUid = 0;
                                            JSONObject obj = new JSONObject();
                                            obj.put("method", "ConnectVideo");
                                            obj.put("uid", 0);
                                            sendSeiMessage(obj.toJSONString());
                                        }
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onLocalVideoStateChanged(io.agora.rtc2.Constants.VideoSourceType source, int state, int error) {
                        L.e(TAG, "IRtcEngineEventHandler---onLocalVideoStateChanged--state->" + state);
                        if (state == LOCAL_VIDEO_STREAM_STATE_CAPTURING) {
                            if (mHandler != null) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!mCameraReady) {
                                            mCameraReady = true;
                                            if (mLivePushListener != null) {
                                                mLivePushListener.onPreviewStart();
                                            }
                                            if (!mIsScreenRecord) {
                                                getBeautyValue();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onVideoPublishStateChanged(io.agora.rtc2.Constants.VideoSourceType source, String channel, int oldState, int newState, int elapseSinceLastState) {
                        if (newState == PUB_STATE_PUBLISHED) {
                            if (!mPushSucceed) {
                                mPushSucceed = true;
                                if (mLivePushListener != null) {
                                    mLivePushListener.onPushStart();
                                }
                                L.e(TAG, "IRtcEngineEventHandler---onVideoPublishStateChanged--->推流成功");
                            }
                        }
                    }

                    @Override
                    public void onChannelMediaRelayStateChanged(int state, int code) {
                        L.e(TAG, "IRtcEngineEventHandler---onChannelMediaRelayStateChanged--state-->" + state + " code: " + code);
                        if (state == RELAY_STATE_RUNNING) {
                            mMediaRelaying = true;
                            mTryMediaRelayAgain = false;
                        } else if (state == RELAY_STATE_FAILURE) {
                            if (code == 1 || code == 3 || code == 4 || code == 5 || code == 6 || code == 7 || code == 9) {
                                if (!mTryMediaRelayAgain) {
                                    mTryMediaRelayAgain = true;
                                    startChannelMediaRelay();  //跨频道媒体流转发 尝试重连
                                }
                            }
                        }
                    }

                };
                config.mEventHandler = engineEventHandler;
                try {
                    mEngine = (RtcEngineEx) RtcEngine.create(config);
                    if(mHandler!=null){
                        mHandler.post(onSuccess);
                    }
                } catch (Exception e) {
                    mEngine = null;
                }
            }
        }).start();

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
                        if (mEngine != null) {

                            VideoDenoiserOptions videoDenoiserOptions = new VideoDenoiserOptions();// 视频降噪

                            videoDenoiserOptions.denoiserLevel =
                                    VideoDenoiserOptions.VIDEO_DENOISER_LEVEL_HIGH_QUALITY;
                            videoDenoiserOptions.denoiserMode = VideoDenoiserOptions.VIDEO_DENOISER_AUTO;
                            mEngine.setVideoDenoiserOptions(true, videoDenoiserOptions); // 视频降噪

                            mEngine.registerVideoFrameObserver(new IVideoFrameObserver() {
                                @Override
                                public boolean onCaptureVideoFrame(int sourceType, VideoFrame videoFrame) {
                                    VideoFrame.Buffer buffer = videoFrame.getBuffer();
                                    if (buffer instanceof TextureBuffer) {
                                        TextureBuffer textureBuffer = (TextureBuffer) buffer;
                                        int width = textureBuffer.getWidth();
                                        int height = textureBuffer.getHeight();
                                        int textureId = MhDataManager.getInstance().renderAgora(textureBuffer.getTextureId(), width, height, videoFrame.getRotation());
                                        if (mTextureBufferHelper == null) {
                                            mTextureBufferHelper = TextureBufferHelper.create("MHSDK",
                                                    EglBaseProvider.instance().getRootEglBase().getEglBaseContext());
                                        }
                                        if (mTextureBufferHelper != null) {
                                            VideoFrame.TextureBuffer processBuffer = mTextureBufferHelper.wrapTextureBuffer(
                                                    width, height, VideoFrame.TextureBuffer.Type.RGB, textureId, textureBuffer.getTransformMatrix()
                                            );
                                            videoFrame.replaceBuffer(processBuffer, videoFrame.getRotation(), videoFrame.getTimestampNs());
                                        }
                                    }
                                    return true;
                                }

                                @Override
                                public boolean onPreEncodeVideoFrame(int sourceType, VideoFrame videoFrame) {
                                    return true;
                                }

                                @Override
                                public boolean onMediaPlayerVideoFrame(VideoFrame videoFrame, int mediaPlayerId) {
                                    return true;
                                }

                                @Override
                                public boolean onRenderVideoFrame(String channelId, int uid, VideoFrame videoFrame) {
                                    return true;
                                }

                                @Override
                                public int getVideoFrameProcessMode() {
                                    return IVideoFrameObserver.PROCESS_MODE_READ_WRITE;
                                }

                                @Override
                                public int getVideoFormatPreference() {
                                    return IVideoFrameObserver.VIDEO_PIXEL_DEFAULT;
                                }

                                @Override
                                public boolean getRotationApplied() {
                                    return false;
                                }

                                @Override
                                public boolean getMirrorApplied() {
                                    return false;
                                }

                                @Override
                                public int getObservedFramePosition() {
                                    return IVideoFrameObserver.POSITION_POST_CAPTURER;
                                }
                            });
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
                    if (mEngine != null) {
                        if (mBeautyOptions == null) {
                            mBeautyOptions = new BeautyOptions();
                        }
                        mBeautyOptions.lighteningLevel = meiBai / 10f;
                        mBeautyOptions.smoothnessLevel = moPi / 10f;
                        mBeautyOptions.rednessLevel = hongRun / 10f;
                        mEngine.setBeautyEffectOptions(true, mBeautyOptions);
                    }
                }
            }

            @Override
            public void onFilterChanged(int filterName) {

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
        mMirror = !mMirror;
        setRealMirror(mMirror);
        if (!mMirror) {
            ToastUtil.show(R.string.live_mirror_1);
        } else {
            ToastUtil.show(R.string.live_mirror_0);
        }
    }

    private void setRealMirror(boolean mirror) {
        if (mRealMirror == mirror) {
            return;
        }
        mRealMirror = mirror;
        if (mEngine != null) {
            mEngine.setLocalRenderMode(RENDER_MODE_HIDDEN, mirror ? VIDEO_MIRROR_MODE_ENABLED : VIDEO_MIRROR_MODE_DISABLED);
            if (mVideoEncoderConfiguration != null) {
                mVideoEncoderConfiguration.mirrorMode = mirror ? VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED
                        : VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_DISABLED;
                mEngine.setVideoEncoderConfiguration(mVideoEncoderConfiguration);
            }
        }
    }


    /**
     * 切换镜头
     */
    @Override
    public void toggleCamera() {
        if (!mCameraReady) {
            return;
        }
        if (mEngine != null) {
            if (mFlashOpen) {
                if (mCameraReady) {
                    mEngine.setCameraTorchOn(false);
                }
                mFlashOpen = false;
            }
            mCameraFront = !mCameraFront;
            mEngine.switchCamera();
            if (!mMhBeautyEnable) {
                if (!mCameraFront) {
                    setRealMirror(false);
                } else {
                    setRealMirror(mMirror);
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
        if (mEngine != null) {
            boolean open = !mFlashOpen;
            if (mCameraReady && mEngine.setCameraTorchOn(open) == 0) {
                mFlashOpen = open;
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

    }

    /**
     * 开始声网推流
     */
    @Override
    public void startPushAgora(String token, String channelId) {
        if (mIsScreenRecord) {
            mEngine.setScreenCaptureScenario(SCREEN_SCENARIO_GAMING);
            ScreenCaptureParameters screenCaptureParameters = new ScreenCaptureParameters();
            screenCaptureParameters.videoCaptureParameters.width = 720;
            screenCaptureParameters.videoCaptureParameters.height = (int) (ScreenDimenUtil.getInstance().getScreenRealHeight() * 720f / ScreenDimenUtil.getInstance().getScreenWidth());
            screenCaptureParameters.videoCaptureParameters.framerate = 15;
            screenCaptureParameters.captureVideo = true;
            screenCaptureParameters.captureAudio = true;
            mEngine.startScreenCapture(screenCaptureParameters);
        }
        mStartPush = true;
        if (mEngine != null) {
            ChannelMediaOptions option = new ChannelMediaOptions();
            option.channelProfile = CHANNEL_PROFILE_LIVE_BROADCASTING;
            option.autoSubscribeAudio = true;
            option.autoSubscribeVideo = true;
            option.publishMicrophoneTrack = true;
            option.publishCameraTrack = true;
            if (mIsScreenRecord) {
                option.publishScreenCaptureVideo = true;
                option.publishScreenCaptureAudio = true;
            }
            mEngine.joinChannel(token, channelId, mUid, option);
        }
        startCountDown();
    }


    @Override
    public void startBgm(String path) {
        if (mEngine != null) {
            int res = mEngine.startAudioMixing(path, false, 1, 0);
            if (res == 0) {
                //该方法调节混音音乐文件在本端和远端的播放音量大小。取值范围为 [0,100]，100 （默认值）为原始音量。
                mEngine.adjustAudioMixingVolume(80);
                mPlayBgm = true;
            }
        }
    }

    @Override
    public void pauseBgm() {
    }

    @Override
    public void resumeBgm() {
    }

    @Override
    public void stopBgm() {
        if (mPlayBgm && mEngine != null) {
            mEngine.stopAudioMixing();
        }
        mPlayBgm = false;
    }

    @Override
    protected void onCameraRestart() {
    }

    @Override
    public void release() {
        super.release();
        CommonHttpUtil.cancel(CommonHttpConsts.GET_BEAUTY_VALUE);
        releasePusher();
    }

    private void releasePusher() {
        if (mEngine != null) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    if (mMediaRelaying) {
                        mMediaRelaying = false;
                        mEngine.stopChannelMediaRelay();
                    }
                    mEngine.leaveChannel();
                    if (mIsScreenRecord) {
                        mEngine.stopScreenCapture();
                    } else {
                        mEngine.stopPreview();
                    }
                    RtcEngine.destroy();
                }
            }).start();
            if (mMhBeautyEnable) {
                MhDataManager.getInstance().release();
            } else {
                SimpleDataManager.getInstance().release();
            }
        }
       /* mEngine = null;
        mChannelMediaRelayData = null;
        mTryMediaRelayAgain = false;*/


    }

    @Override
    public void onDestroy() {
        releasePusher();
        super.onDestroy();
    }

    @Override
    public void sendSeiMessage(String msg) {
        L.e(TAG, "IRtcEngineEventHandler--sendSeiMessage--发送消息--->msg: " + msg);
        byte[] data = null;
        try {
            data = msg.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            data = null;
        }
        if (data != null && data.length > 0) {
            if (mEngine != null) {
                if (mDataStreamId == 0) {
                    DataStreamConfig dataStreamConfig = new DataStreamConfig();
                    dataStreamConfig.syncWithAudio = true;
                    dataStreamConfig.ordered = true;
                    mDataStreamId = mEngine.createDataStream(dataStreamConfig);
                }
                mEngine.sendStreamMessage(mDataStreamId, data);
            }
        }
    }

    /**
     * 声网sdk--> 主播与其他主播连麦
     *
     * @param uid 对方主播的uid
     */
    private void onAnchorLinkMic(int uid, boolean fromAgoraHandler) {
        if (mLinkMicAnchorUid == 0) {
            if (fromAgoraHandler) {
                L.e(TAG, "IRtcEngineEventHandler---主播连麦--声网OnUserJoined--->" + uid);
            } else {
                L.e(TAG, "IRtcEngineEventHandler---主播连麦---socket--->" + uid);
            }
            mLinkMicAnchorUid = uid;
            TextureView textureView = new TextureView(mContext);
            mRightContainer.addView(textureView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            VideoCanvas remote = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, uid);
            mEngine.setupRemoteVideo(remote);
        }
    }

    /**
     * 声网sdk--> 主播与其他主播连麦，进行跨频道媒体流转发
     *
     * @param stream   自己主播的stream
     * @param pkUid    对方主播的uid
     * @param pkStream 对方主播的stream
     */
    public void startAgoraLinkMicAnchor(final String stream, String pkUid, final String pkStream) {
        onAnchorLinkMic(Integer.parseInt(pkUid), false);
        LiveHttpUtil.getSwRtcPKToken(stream, pkStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    String srcToken = obj.getString("src_token");
                    String destToken = obj.getString("dest_token");
                    L.e(TAG, "IRtcEngineEventHandler--getSwRtcPKToken--srcToken--->" + srcToken + "--destToken-->" + destToken);
                    mChannelMediaRelayData = new ChannelMediaRelayData(srcToken, stream, destToken, pkStream);
                    startChannelMediaRelay();
                }
            }
        });
    }

    private void startChannelMediaRelay() {
        if (mChannelMediaRelayData != null && mEngine != null) {
            // 配置源频道信息
            ChannelMediaRelayConfiguration mediaRelayConfiguration = new ChannelMediaRelayConfiguration();
            ChannelMediaInfo srcChannelInfo = new ChannelMediaInfo(mChannelMediaRelayData.mSrcChannel, mChannelMediaRelayData.mSrcToken, 0);
            mediaRelayConfiguration.setSrcChannelInfo(srcChannelInfo);
            // 配置目标频道信息
            int myUid = Integer.parseInt(CommonAppConfig.getInstance().getUid());
            ChannelMediaInfo destChannelInfo = new ChannelMediaInfo(mChannelMediaRelayData.mDestChannel, mChannelMediaRelayData.mDestToken, myUid);
            mediaRelayConfiguration.setDestChannelInfo(mChannelMediaRelayData.mDestChannel, destChannelInfo);
            mEngine.startOrUpdateChannelMediaRelay(mediaRelayConfiguration);
        }
    }

    /**
     * 声网sdk--> 主播与其他主播断开连麦
     */
    @Override
    public void closeAgoraLinkMicAnchor() {
        if (mEngine != null) {
            if (mMediaRelaying) {
                mMediaRelaying = false;
                mEngine.stopChannelMediaRelay();
            }
        }
        if (mLinkMicAnchorUid != 0) {
            L.e(TAG, "IRtcEngineEventHandler---主播连麦断开--->");
            if (mRightContainer != null) {
                mRightContainer.removeAllViews();
            }
            mLinkMicAnchorUid = 0;
        }
        mChannelMediaRelayData = null;
        mTryMediaRelayAgain = false;
    }


    private class ChannelMediaRelayData {
        private final String mSrcToken;
        private final String mSrcChannel;
        private final String mDestToken;
        private final String mDestChannel;

        public ChannelMediaRelayData(String srcToken, String srcChannel, String destToken, String destChannel) {
            mSrcToken = srcToken;
            mSrcChannel = srcChannel;
            mDestToken = destToken;
            mDestChannel = destChannel;
        }
    }
}
