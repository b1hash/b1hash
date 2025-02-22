package com.yunbao.live.views;

import static io.agora.rtc2.Constants.AUDIO_PROFILE_MUSIC_STANDARD;
import static io.agora.rtc2.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
import static io.agora.rtc2.Constants.CLIENT_ROLE_AUDIENCE;
import static io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER;
import static io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_ENABLED;
import static io.agora.rtc2.video.VideoCanvas.RENDER_MODE_FIT;
import static io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.tencent.liteav.txcvodplayer.renderer.TextureRenderView;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXVodConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.ScreenDimenUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.live.R;
import com.yunbao.live.activity.LiveActivity;
import com.yunbao.live.activity.LiveAudienceActivity;
import com.yunbao.live.http.LiveHttpUtil;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.ClientRoleOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcConnection;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

/**
 * Created by cxf on 2018/10/10.
 * 直播间  声网播放
 */

public class LivePlayAgoraViewHolder extends LiveRoomPlayViewHolder {

    private static final String TAG = "LiveTxPlayViewHolder";
    private ViewGroup mRoot;
    private ViewGroup mSmallContainer;
    private ViewGroup mLeftContainer;
    private ViewGroup mRightContainer;
    private ViewGroup mPkContainer;
    private TextureView mLiveView;
    private TextureRenderView mVideoView;
    private View mLoading;
    private ImageView mCover;
    private TXVodPlayer mVodPlayer;
    private boolean mPaused;//是否切后台了
    private boolean mPausedPlay;//是否被动暂停了播放
    private boolean mChangeToLeft;
    private boolean mChangeToAnchorLinkMic;
    private String mUrl;
    private Handler mVideoHandler;
    private Handler mLiveHandler;
    //    private int mVideoLastProgress;
    private float mVideoWidth;
    private float mVideoHeight;
    private int mRootHeight;
    private Boolean mIsLive;
    private boolean mShowVideoFirstFrame = false;
    private Runnable mVideoFirstFrameRunnable;
    private RtcEngineEx mEngine;
    private int mLiveUid;
    private int mUid;
    private int mLinkMicAudienceUid;//观众连麦，连麦观众的uid
    private int mLinkMicAnchorUid;//主播连麦，对方主播的uid
    private IRtcEngineEventHandler mIRtcEngineEventHandler;
//    private RtcConnection mOtherAnchorRtcConnection;//主播连麦，对方主播的RtcConnection

    public LivePlayAgoraViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_play_tx;
    }

    @Override
    public void init() {
        mUid = Integer.parseInt(CommonAppConfig.getInstance().getUid());
        mRoot = (ViewGroup) findViewById(R.id.root);
        mRoot.post(new Runnable() {
            @Override
            public void run() {
                mRootHeight = mRoot.getHeight();
            }
        });
        mSmallContainer = (ViewGroup) findViewById(R.id.small_container);
        mLeftContainer = (ViewGroup) findViewById(R.id.left_container);
        mRightContainer = (ViewGroup) findViewById(R.id.right_container);
        mPkContainer = (ViewGroup) findViewById(R.id.pk_container);
        mLoading = findViewById(R.id.loading);
        mCover = (ImageView) findViewById(R.id.cover);
        mLiveView = (TextureView) findViewById(R.id.live_view);
        mVideoView = (TextureRenderView) findViewById(R.id.video_view);
        mLiveHandler = new Handler();
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
            public void onClientRoleChanged(int oldRole, int newRole, ClientRoleOptions newRoleOptions) {
                L.e(TAG, "IRtcEngineEventHandler---onClientRoleChanged--->oldRole: " + oldRole + " newRole: " + newRole);
            }

            @Override
            public void onClientRoleChangeFailed(int reason, int currentRole) {
                L.e(TAG, "IRtcEngineEventHandler---onClientRoleChangeFailed---->reason: " + reason + " currentRole: " + currentRole);
            }

            @Override
            public void onUserJoined(int uid, int elapsed) {
                L.e(TAG, "IRtcEngineEventHandler---onUserJoined--->" + uid);
                if (mEngine == null || uid == mUid) {
                    return;
                }
                if (mLiveUid == uid) {
                    VideoCanvas remote = new VideoCanvas(mLiveView, RENDER_MODE_HIDDEN, uid);
                    mEngine.setupRemoteVideo(remote);
                } else {
                    int linkMicUid = ((LiveActivity) mContext).getLinkMicUid();
                    if (linkMicUid == uid) {//观众连麦
                        if (mLinkMicAudienceUid == 0) {
                            L.e(TAG, "IRtcEngineEventHandler---观众连麦--->" + uid);
                            mLinkMicAudienceUid = uid;
                            if (mLiveHandler != null) {
                                mLiveHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mEngine != null) {
                                            TextureView textureView = new TextureView(mContext);
                                            mSmallContainer.addView(textureView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                                            VideoCanvas remote = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, uid);
                                            mEngine.setupRemoteVideo(remote);
                                        }
                                    }
                                });
                            }
                        }
                    } else {
                        final int pkUid = ((LiveActivity) mContext).getLinkMicAnchorUid();
                        if (pkUid == uid) {//主播连麦
                            onAnchorLinkMic(pkUid, true);
                        }
                    }
                }
            }

            @Override
            public void onUserOffline(int uid, int reason) {
                L.e(TAG, "IRtcEngineEventHandler---onUserOffline--->" + uid);
                if (mLinkMicAudienceUid == uid) {
                    L.e(TAG, "IRtcEngineEventHandler---观众连麦断开--->" + uid);
                    if (mLiveHandler != null) {
                        mLiveHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mEngine != null) {
                                    mEngine.setupRemoteVideo(new VideoCanvas(null, RENDER_MODE_HIDDEN, uid));
                                    mSmallContainer.removeAllViews();
                                    mLinkMicAudienceUid = 0;
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
                L.e(TAG, "IRtcEngineEventHandler---onFirstRemoteVideoFrame--->" + uid);
                if (mLiveUid == uid) {
                    if (mLiveHandler != null) {
                        mLiveHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                hideCover();
                            }
                        });
                    }
                }
            }

            @Override
            public void onVideoSizeChanged(Constants.VideoSourceType source, int uid, int width, int height, int rotation) {
                L.e(TAG, "IRtcEngineEventHandler---onVideoSizeChanged---source:" + source + " uid: " + uid + " width: " + width + " height: " + height + " rotation: " + rotation);
                if (mLiveUid == uid) {
                    if ((rotation / 90) % 2 == 0) {
                        mVideoWidth = width;
                        mVideoHeight = height;
                    } else {
                        mVideoWidth = height;
                        mVideoHeight = width;
                    }
                    if (mLiveHandler != null) {
                        mLiveHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                changeLiveSize(false);
                            }
                        });
                    }
                }
            }
        };
        config.mEventHandler = engineEventHandler;
        mIRtcEngineEventHandler = engineEventHandler;
        try {
            mEngine = (RtcEngineEx) RtcEngine.create(config);
        } catch (Exception e) {
            mEngine = null;
        }
        if (mEngine != null) {
            mEngine.setClientRole(CLIENT_ROLE_AUDIENCE);
            VideoEncoderConfiguration configuration = new VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_480x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
            );
            configuration.orientationMode = ORIENTATION_MODE_FIXED_PORTRAIT;
            configuration.mirrorMode = VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED;
            mEngine.setVideoEncoderConfiguration(configuration);
            mEngine.enableVideo();
        }
    }

    /**
     * 声网sdk--> 开始/关闭 观众连麦推流
     *
     * @param isPush true开始推流 ，false结束推流
     */
    @Override
    public void toggleLinkMicPushAgora(boolean isPush) {
        if (mEngine != null) {
            if (isPush) {
                TextureView textureView = new TextureView(mContext);
                mSmallContainer.addView(textureView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                VideoCanvas videoCanvas = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, mUid);
                videoCanvas.mirrorMode = VIDEO_MIRROR_MODE_ENABLED;
                mEngine.setupLocalVideo(videoCanvas);
                mEngine.startPreview();
                mEngine.setClientRole(CLIENT_ROLE_BROADCASTER);
            } else {
                mEngine.stopPreview();
                mEngine.setClientRole(CLIENT_ROLE_AUDIENCE);
                mEngine.setupLocalVideo(new VideoCanvas(null, RENDER_MODE_HIDDEN, mUid));
                mSmallContainer.removeAllViews();
            }
        }
    }


    private TXVodPlayer getVodPlayer() {
        if (mVodPlayer == null) {
            mVodPlayer = new TXVodPlayer(mContext);
            TXVodPlayConfig playConfig = new TXVodPlayConfig();
            playConfig.setProgressInterval(200);
            playConfig.setHeaders(CommonAppConfig.HEADER);
            mVodPlayer.setConfig(playConfig);
            mVodPlayer.setLoop(true);
            mVodPlayer.setAutoPlay(false);
            mVodPlayer.setRenderMode(TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN);
            mVodPlayer.setVodListener(new ITXVodPlayListener() {
                @Override
                public void onPlayEvent(TXVodPlayer txVodPlayer, int e, Bundle bundle) {
//                    if (e != 2005) {
//                        L.e(TAG, "------onPlayEvent----->" + e);
//                    }
                    switch (e) {
                        case TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED:
                            if (mVodPlayer != null) {
                                mVodPlayer.resume();
                            }
                            break;
                        case TXLiveConstants.PLAY_EVT_PLAY_BEGIN://播放开始
                            if (mLoading != null && mLoading.getVisibility() == View.VISIBLE) {
                                mLoading.setVisibility(View.INVISIBLE);
                            }
                            break;
                        case TXLiveConstants.PLAY_EVT_PLAY_LOADING:
                            if (mLoading != null && mLoading.getVisibility() != View.VISIBLE) {
                                mLoading.setVisibility(View.VISIBLE);
                            }
                            break;
                        case TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME://第一帧
                            mShowVideoFirstFrame = true;
                            break;
                        case TXLiveConstants.PLAY_EVT_PLAY_END://播放结束
                            replay();
                            break;
                        case TXVodConstants.VOD_PLAY_EVT_CHANGE_RESOLUTION:
                            if (mChangeToLeft || mChangeToAnchorLinkMic) {
                                return;
                            }
                            mVideoWidth = bundle.getInt("EVT_PARAM1", 0);
                            mVideoHeight = bundle.getInt("EVT_PARAM2", 0);
                            changeVideoSize(false);
                            break;
                        case TXLiveConstants.PLAY_ERR_NET_DISCONNECT://播放失败
                        case TXLiveConstants.PLAY_ERR_FILE_NOT_FOUND:
                            ToastUtil.show(WordUtil.getString(R.string.live_play_error));
                            break;
                        case TXLiveConstants.PLAY_EVT_PLAY_PROGRESS:
                            if (mShowVideoFirstFrame) {
                                mShowVideoFirstFrame = false;
                                if (mVideoHandler == null) {
                                    mVideoHandler = new Handler();
                                }
                                if (mVideoFirstFrameRunnable == null) {
                                    mVideoFirstFrameRunnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mVideoView != null) {
                                                mVideoView.setTranslationX(0);
                                            }
                                            hideCover();
                                            mVodPlayer.setMute(false);
                                        }
                                    };
                                }
                                mVideoHandler.postDelayed(mVideoFirstFrameRunnable, 200);
                            }
                            break;
                    }
                }

                @Override
                public void onNetStatus(TXVodPlayer txVodPlayer, Bundle bundle) {

                }
            });
            mVodPlayer.setPlayerView(mVideoView);
        }
        return mVodPlayer;
    }

    public void play(String url) {

    }

    /**
     * 开始播放
     *
     * @param url 流地址
     */
    @Override
    public void playAgora(String url, boolean isVideo, String agoraToken, String channelId, int liveUid) {
        L.e(TAG, "playAgora------url----->" + url);
        mIsLive = !isVideo;
        mLiveUid = -1;
        if (isVideo) {
            if (TextUtils.isEmpty(url)) {
                return;
            }
            int playType = -1;
            if (url.startsWith("trtc://") || url.startsWith("rtmp://")) {
                playType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
            } else if (url.contains(".flv") || url.contains(".FLV")) {
                playType = TXLivePlayer.PLAY_TYPE_LIVE_FLV;
            } else if (url.contains(".m3u8")) {
                playType = TXLivePlayer.PLAY_TYPE_VOD_HLS;
            } else if (url.contains(".mp4") || url.contains(".MP4")) {
                playType = TXLivePlayer.PLAY_TYPE_VOD_MP4;
            }
            if (playType == -1) {
                ToastUtil.show(R.string.live_play_error_2);
                return;
            }
            if (mVodPlayer != null) {
                mVodPlayer.setMute(true);
            }
            if (mVideoView != null) {
                mVideoView.setTranslationX(100000);
            }
            mShowVideoFirstFrame = false;
            getVodPlayer().startVodPlay(url);
        } else {
            mLiveUid = liveUid;
            if (mVideoView != null) {
                mVideoView.setTranslationX(100000);
            }
            if (mEngine != null) {
                ChannelMediaOptions option = new ChannelMediaOptions();
                option.channelProfile = CHANNEL_PROFILE_LIVE_BROADCASTING;
                option.clientRoleType = CLIENT_ROLE_AUDIENCE;
                option.autoSubscribeAudio = true;
                option.autoSubscribeVideo = true;
                option.publishMicrophoneTrack = true;
                option.publishCameraTrack = true;
                mEngine.joinChannel(agoraToken, channelId, mUid, option);
            }
        }

    }


    /**
     * 调整视频播放画面宽高
     */
    private void changeVideoSize(boolean landscape) {
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            float videoRatio = mVideoWidth / mVideoHeight;
            float p1 = mParentView.getWidth();
            float p2 = mParentView.getHeight();
            float parentWidth = p1;
            float parentHeight = p2;
            if (landscape) {
                parentWidth = Math.max(p1, p2);
                parentHeight = Math.min(p1, p2);
            } else {
                parentWidth = Math.min(p1, p2);
                parentHeight = Math.max(p1, p2);
            }
//            L.e("changeVideoSize", "mVideoWidth----->" + mVideoWidth + "  mVideoHeight------>" + mVideoHeight);
//            L.e("changeVideoSize", "parentWidth----->" + parentWidth + "  parentHeight------>" + parentHeight);
            float parentRatio = parentWidth / parentHeight;
            if (videoRatio != parentRatio) {
                FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) mVideoView.getLayoutParams();
                if (videoRatio > 10f / 16f && videoRatio > parentRatio) {
                    p.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    p.height = (int) (parentWidth / videoRatio);
                    p.gravity = Gravity.CENTER;
                } else {
                    p.width = (int) (parentHeight * videoRatio);
                    p.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    p.gravity = Gravity.CENTER;
                }
                mVideoView.requestLayout();
//                View innerView = mVideoView.getVideoView();
//                if (innerView != null) {
//                    ViewGroup.LayoutParams innerLp = innerView.getLayoutParams();
//                    innerLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
//                    innerLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
//                    innerView.setLayoutParams(innerLp);
//                }
                ((LiveAudienceActivity) mContext).onVideoHeightChanged(p.height, mRootHeight);
            }
        }
    }

    /**
     * 调整直播播放画面宽高
     */
    private void changeLiveSize(boolean landscape) {
        if (mChangeToLeft || mChangeToAnchorLinkMic) {
            return;
        }
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            float videoRatio = mVideoWidth / mVideoHeight;
            float p1 = mParentView.getWidth();
            float p2 = mParentView.getHeight();
            float parentWidth = p1;
            float parentHeight = p2;
            if (landscape) {
                parentWidth = Math.max(p1, p2);
                parentHeight = Math.min(p1, p2);
            } else {
                parentWidth = Math.min(p1, p2);
                parentHeight = Math.max(p1, p2);
            }
//            L.e("changeVideoSize", "mVideoWidth----->" + mVideoWidth + "  mVideoHeight------>" + mVideoHeight);
//            L.e("changeVideoSize", "parentWidth----->" + parentWidth + "  parentHeight------>" + parentHeight);
            float parentRatio = parentWidth / parentHeight;
            if (videoRatio != parentRatio) {
                FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) mLiveView.getLayoutParams();
                if (videoRatio > 10f / 16f && videoRatio > parentRatio) {
                    p.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    p.height = (int) (parentWidth / videoRatio);
                    p.gravity = Gravity.CENTER;
                } else {
                    p.width = (int) (parentHeight * videoRatio);
//                    p.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    p.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    p.gravity = Gravity.CENTER;
                }
                mLiveView.requestLayout();
                if (!mChangeToAnchorLinkMic) {
                    ((LiveAudienceActivity) mContext).onVideoHeightChanged(p.height, mRootHeight);
                }
            }
        }
    }

    @Override
    public void changeSize(boolean landscape) {
        if (mIsLive) {
            changeLiveSize(landscape);
        } else {
            changeVideoSize(landscape);
        }
    }

    @Override
    public void hideCover() {
        if (mCover != null && mCover.getAlpha() != 0f) {
            L.e(TAG, "隐藏封面---hideCover--->");
            mCover.animate().alpha(0).setDuration(500).start();
        }
    }

    @Override
    public void setCover(String coverUrl) {
        if (mCover != null) {
            ImgLoader.displayBlur(mContext, coverUrl, mCover);
        }
    }

    /**
     * 循环播放
     */
    private void replay() {
//        if (mVodPlayer != null) {
//            mVodPlayer.seek(0);
//            mVodPlayer.resume();
//        }
    }


    /**
     * 暂停播放
     */
    @Override
    public void pausePlay() {
        if (!mPausedPlay) {
            mPausedPlay = true;
            if (!mPaused) {
//                if (mLivePlayer != null) {
//                    mLivePlayer.setPlayoutVolume(0);
//                }
                if (mVodPlayer != null) {
                    mVodPlayer.setMute(true);
                }
            }
            if (mCover != null) {
                mCover.setAlpha(1f);
                if (mCover.getVisibility() != View.VISIBLE) {
                    mCover.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * 暂停播放后恢复
     */
    @Override
    public void resumePlay() {
        if (mPausedPlay) {
            mPausedPlay = false;
            if (!mPaused) {
//                if (mLivePlayer != null) {
//                    mLivePlayer.setPlayoutVolume(100);
//                }
                if (mVodPlayer != null) {
                    mVodPlayer.setMute(false);
                }
            }
            hideCover();
        }
    }


    @Override
    public void stopPlay() {
        mChangeToLeft = false;
        mChangeToAnchorLinkMic = false;
        mLinkMicAudienceUid = 0;
        mLinkMicAnchorUid = 0;
        if (mVideoHandler != null) {
            mVideoHandler.removeCallbacksAndMessages(null);
        }
        if (mCover != null) {
            mCover.setAlpha(1f);
            if (mCover.getVisibility() != View.VISIBLE) {
                mCover.setVisibility(View.VISIBLE);
            }
        }
        L.e(TAG, "stopPlay-------->");
        if (mEngine != null) {
            mEngine.leaveChannel();
            /*if (mOtherAnchorRtcConnection != null) {
                mEngine.leaveChannelEx(mOtherAnchorRtcConnection);
                mOtherAnchorRtcConnection = null;
            }*/
            mEngine.stopPreview();
        }
        if (mVodPlayer != null) {
            mVodPlayer.pause();
        }
        if (mVideoView != null) {
            mVideoView.setTranslationX(100000);
        }
        mShowVideoFirstFrame = false;
    }

    @Override
    public void release() {
        if (mEngine != null) {
            mEngine.leaveChannel();
            /*if (mOtherAnchorRtcConnection != null) {
                mEngine.leaveChannelEx(mOtherAnchorRtcConnection);
                mOtherAnchorRtcConnection = null;
            }*/
            mEngine.stopPreview();
            mLiveHandler.post(RtcEngine::destroy);
        }
        mEngine = null;
        if (mVideoHandler != null) {
            mVideoHandler.removeCallbacksAndMessages(null);
        }
        mVideoHandler = null;
        if (mVodPlayer != null) {
            mVodPlayer.stopPlay(false);
            mVodPlayer.setVodListener(null);
        }
        mVodPlayer = null;
        L.e(TAG, "release------->");
    }


    @Override
    public ViewGroup getSmallContainer() {
        return mSmallContainer;
    }


    @Override
    public ViewGroup getRightContainer() {
        return mRightContainer;
    }

    @Override
    public ViewGroup getPkContainer() {
        return mPkContainer;
    }

    @Override
    public void changeToLeft() {
        mChangeToLeft = true;
        if (mLiveView != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLiveView.getLayoutParams();
            params.width = ScreenDimenUtil.getInstance().getScreenWidth() / 2;
            params.height = DpUtil.dp2px(250);
            params.topMargin = DpUtil.dp2px(130);
            params.gravity = Gravity.TOP;
            mLiveView.setLayoutParams(params);
        }
        if (mLoading != null && mLeftContainer != null) {
            ViewParent viewParent = mLoading.getParent();
            if (viewParent != null) {
                ((ViewGroup) viewParent).removeView(mLoading);
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(DpUtil.dp2px(24), DpUtil.dp2px(24));
            params.gravity = Gravity.CENTER;
            mLoading.setLayoutParams(params);
            mLeftContainer.addView(mLoading);
        }
    }

    @Override
    public void changeToBig() {
        mChangeToLeft = false;
        if (mLiveView != null) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLiveView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.topMargin = 0;
            mLiveView.setLayoutParams(params);
        }
        if (mLoading != null && mRoot != null) {
            ViewParent viewParent = mLoading.getParent();
            if (viewParent != null) {
                ((ViewGroup) viewParent).removeView(mLoading);
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(DpUtil.dp2px(24), DpUtil.dp2px(24));
            params.gravity = Gravity.CENTER;
            mLoading.setLayoutParams(params);
            mRoot.addView(mLoading);
        }
    }

    @Override
    public void onResume() {
        if (!mPausedPlay && mPaused) {
//            if (mLivePlayer != null) {
//                mLivePlayer.setPlayoutVolume(100);
//            }
            if (mVodPlayer != null) {
                mVodPlayer.setMute(false);
            }
        }
        mPaused = false;
    }

    @Override
    public void onPause() {
        if (!mPausedPlay) {
//            if (mLivePlayer != null) {
//                mLivePlayer.setPlayoutVolume(0);
//            }
            if (mVodPlayer != null) {
                mVodPlayer.setMute(true);
            }
        }
        mPaused = true;
    }

    @Override
    public void onDestroy() {
        release();
        super.onDestroy();
    }


    /**
     * 设置主播连麦模式
     *
     * @param anchorLinkMic
     */
    @Override
    public void setAnchorLinkMic(final boolean anchorLinkMic, int delayTime) {
        if (mLiveView == null) {
            return;
        }
        if (delayTime < 0) {
            delayTime = 0;
        }
        if (delayTime > 0) {
            if (mVideoHandler == null) {
                mVideoHandler = new Handler();
            }
            mVideoHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mChangeToAnchorLinkMic = anchorLinkMic;
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLiveView.getLayoutParams();
                    if (anchorLinkMic) {
                        params.width = ScreenDimenUtil.getInstance().getScreenWidth() / 2;
                        params.height = DpUtil.dp2px(250);
                        params.topMargin = DpUtil.dp2px(130);
                        params.gravity = Gravity.TOP;
                    } else {
                        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        params.topMargin = 0;
                        params.gravity = Gravity.CENTER;
                    }
                    mLiveView.setLayoutParams(params);
                }
            }, delayTime);
        } else {
            mChangeToAnchorLinkMic = anchorLinkMic;
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLiveView.getLayoutParams();
            if (anchorLinkMic) {
                params.width = ScreenDimenUtil.getInstance().getScreenWidth() / 2;
                params.height = DpUtil.dp2px(250);
                params.topMargin = DpUtil.dp2px(130);
                params.gravity = Gravity.TOP;
            } else {
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                params.topMargin = 0;
                params.gravity = Gravity.CENTER;
            }
            mLiveView.setLayoutParams(params);
        }
    }

    /**
     * 主播连麦断开
     */
    public void onAnchorLinkMicClose() {
        if (mLinkMicAnchorUid != 0) {
            L.e(TAG, "IRtcEngineEventHandler---主播连麦断开--->" + mLinkMicAnchorUid);
            if (mRightContainer != null) {
                mRightContainer.removeAllViews();
            }
            mLinkMicAnchorUid = 0;
        }
    }

    /**
     * 主播连麦开始
     */
    public void onAnchorLinkMicStart(String pkUid) {
        onAnchorLinkMic(Integer.parseInt(pkUid), false);
    }

    /**
     * 声网sdk--> 主播与其他主播连麦
     *
     * @param pkUid 对方主播的uid
     */
    private void onAnchorLinkMic(int pkUid, boolean fromAgoraHandler) {
        if (mLinkMicAnchorUid == 0) {
            if (fromAgoraHandler) {
                L.e(TAG, "IRtcEngineEventHandler---主播连麦--声网OnUserJoined--->" + pkUid);
            } else {
                L.e(TAG, "IRtcEngineEventHandler---主播连麦---socket--->" + pkUid);
            }
            mLinkMicAnchorUid = pkUid;
            if (mLiveHandler != null) {
                mLiveHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mEngine != null) {
                            TextureView textureView = new TextureView(mContext);
                            mRightContainer.addView(textureView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            VideoCanvas remote = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, pkUid);
                            mEngine.setupRemoteVideo(remote);
                        }
                    }
                });
            }
        }
    }
}
