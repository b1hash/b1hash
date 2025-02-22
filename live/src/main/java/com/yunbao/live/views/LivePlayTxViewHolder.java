package com.yunbao.live.views;

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
import com.alibaba.fastjson.JSONObject;
import com.tencent.liteav.txcvodplayer.renderer.TextureRenderView;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXVodConstants;
import com.tencent.rtmp.TXVodPlayConfig;
import com.tencent.rtmp.TXVodPlayer;
import com.yunbao.common.CommonAppConfig;
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
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;

/**
 * Created by cxf on 2018/10/10.
 * 直播间播放器  腾讯播放器
 */

public class LivePlayTxViewHolder extends LiveRoomPlayViewHolder {

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
    private V2TXLivePlayer mLivePlayer;
    private TXVodPlayer mVodPlayer;
    private boolean mPaused;//是否切后台了
    private boolean mPausedPlay;//是否被动暂停了播放
    private boolean mChangeToLeft;
    private boolean mChangeToAnchorLinkMic;
    private String mUrl;
    private Handler mHandler;
    //    private int mVideoLastProgress;
    private float mVideoWidth;
    private float mVideoHeight;
    private int mRootHeight;
    private Boolean mIsLive;
    private boolean mShowVideoFirstFrame = false;
    private Runnable mVideoFirstFrameRunnable;


    public LivePlayTxViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_play_tx;
    }

    @Override
    public void init() {
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
                    if (mChangeToLeft || mChangeToAnchorLinkMic) {
                        return;
                    }
                    mVideoWidth = width;
                    mVideoHeight = height;
                    changeLiveSize(false);
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
                    hideCover();
                }

                @Override
                public void onVideoLoading(V2TXLivePlayer player, Bundle extraInfo) {
                    L.e(TAG, "V2TXLivePlayerObserver--onVideoLoading: ");
                    if (mLoading != null && mLoading.getVisibility() != View.VISIBLE) {
                        mLoading.setVisibility(View.VISIBLE);
                    }
                }

            });
            mLivePlayer.setRenderFillMode(V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFill);
            mLivePlayer.setRenderView(mLiveView);
        }
        return mLivePlayer;
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
                                if (mHandler == null) {
                                    mHandler = new Handler();
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
                                mHandler.postDelayed(mVideoFirstFrameRunnable, 200);
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


    /**
     * 开始播放
     *
     * @param url 流地址
     */
    @Override
    public void play(String url) {
        L.e(TAG, "play------url----->" + url);
        mIsLive = null;
        if (TextUtils.isEmpty(url)) {
            return;
        }
        try {
            boolean isLive = false;
            int playType = -1;
            if (url.startsWith("trtc://") || url.startsWith("rtmp://")) {
                playType = TXLivePlayer.PLAY_TYPE_LIVE_RTMP;
                isLive = true;
            } else if (url.contains(".flv") || url.contains(".FLV")) {
                playType = TXLivePlayer.PLAY_TYPE_LIVE_FLV;
                isLive = true;
            } else if (url.contains(".m3u8")) {
                playType = TXLivePlayer.PLAY_TYPE_VOD_HLS;
            } else if (url.contains(".mp4") || url.contains(".MP4")) {
                playType = TXLivePlayer.PLAY_TYPE_VOD_MP4;
            }
            if (playType == -1) {
                ToastUtil.show(R.string.live_play_error_2);
                return;
            }
            mIsLive = isLive;
            if (isLive) {
                if (mVideoView != null) {
                    mVideoView.setTranslationX(100000);
                }
                int result = getLivePlayer().startLivePlay(url);
                if (result == 0) {
                    mUrl = url;
                }
            } else {
                if (mVodPlayer != null) {
                    mVodPlayer.setMute(true);
                }
                if (mVideoView != null) {
                    mVideoView.setTranslationX(100000);
                }
                mShowVideoFirstFrame = false;
                getVodPlayer().startVodPlay(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                    p.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    p.gravity = Gravity.CENTER;
                }
                mLiveView.requestLayout();
//                View innerView = mLiveView.getVideoView();
//                if (innerView != null) {
//                    ViewGroup.LayoutParams innerLp = innerView.getLayoutParams();
//                    innerLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
//                    innerLp.height = ViewGroup.LayoutParams.MATCH_PARENT;
//                    innerView.setLayoutParams(innerLp);
//                }
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
                if (mLivePlayer != null) {
                    mLivePlayer.setPlayoutVolume(0);
                }
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
                if (mLivePlayer != null) {
                    mLivePlayer.setPlayoutVolume(100);
                }
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
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mCover != null) {
            mCover.setAlpha(1f);
            if (mCover.getVisibility() != View.VISIBLE) {
                mCover.setVisibility(View.VISIBLE);
            }
        }
        if (mLivePlayer != null) {
            mLivePlayer.stopPlay();
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
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        LiveHttpUtil.cancel(LiveHttpConsts.GET_TX_LINK_MIC_ACC_URL);
        if (mLivePlayer != null) {
            mLivePlayer.stopPlay();
            mLivePlayer.setObserver(null);
        }
        mLivePlayer = null;
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
            params.gravity=Gravity.TOP;
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
            if (mLivePlayer != null) {
                mLivePlayer.setPlayoutVolume(100);
            }
            if (mVodPlayer != null) {
                mVodPlayer.setMute(false);
            }
        }
        mPaused = false;
    }

    @Override
    public void onPause() {
        if (!mPausedPlay) {
            if (mLivePlayer != null) {
                mLivePlayer.setPlayoutVolume(0);
            }
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
     * 腾讯sdk连麦时候切换低延时流
     */
    public void onLinkMicTxAccEvent(boolean linkMic) {
        if (linkMic) {
            LiveHttpUtil.getTxLinkMicAccUrl(
                    ((LiveActivity) mContext).getLiveUid(),
                    ((LiveActivity) mContext).getStream(),
                    new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0 && info.length > 0) {
                                JSONObject obj = JSON.parseObject(info[0]);
                                if (obj != null) {
                                    String accUrl = obj.getString("streamUrlWithSignature");
                                    if (!TextUtils.isEmpty(accUrl)) {
                                        L.e(TAG, "低延时流----->" + accUrl);
                                        if (mLivePlayer != null) {
                                            mLivePlayer.startLivePlay(accUrl);
                                        }
                                    }
                                }
                            }
                        }
                    });
        } else {
            if (mLivePlayer != null) {
                mLivePlayer.startLivePlay(mUrl);
            }
        }

    }

    /**
     * 设置主播连麦模式
     *
     * @param anchorLinkMic
     */
    public void setAnchorLinkMic(final boolean anchorLinkMic, int delayTime) {
        if (mLiveView == null) {
            return;
        }
        if (delayTime < 0) {
            delayTime = 0;
        }
        if (delayTime > 0) {
            if (mHandler == null) {
                mHandler = new Handler();
            }
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mChangeToAnchorLinkMic = anchorLinkMic;
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mLiveView.getLayoutParams();
                    if (anchorLinkMic) {
                        params.width= ViewGroup.LayoutParams.MATCH_PARENT;
                        params.height = DpUtil.dp2px(250);
                        params.topMargin = DpUtil.dp2px(130);
                        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                    } else {
                        params.width= ViewGroup.LayoutParams.MATCH_PARENT;
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
                params.width= ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = DpUtil.dp2px(250);
                params.topMargin = DpUtil.dp2px(130);
                params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            } else {
                params.width= ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                params.topMargin = 0;
                params.gravity = Gravity.CENTER;
            }
            mLiveView.setLayoutParams(params);
        }


    }
}
