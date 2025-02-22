package com.yunbao.live.views;

import static io.agora.rtc2.Constants.AUDIO_PROFILE_MUSIC_STANDARD;
import static io.agora.rtc2.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
import static io.agora.rtc2.Constants.CLIENT_ROLE_AUDIENCE;
import static io.agora.rtc2.Constants.CLIENT_ROLE_BROADCASTER;
import static io.agora.rtc2.Constants.PUB_STATE_PUBLISHED;
import static io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_ENABLED;
import static io.agora.rtc2.video.VideoCanvas.RENDER_MODE_HIDDEN;
import static io.agora.rtc2.video.VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.custom.ItemDecoration;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.L;
import com.yunbao.live.R;
import com.yunbao.live.activity.LiveActivity;
import com.yunbao.live.activity.LiveAnchorActivity;
import com.yunbao.live.activity.LiveAudienceActivity;
import com.yunbao.live.adapter.LiveVideoLinkMicAdapter;
import com.yunbao.live.adapter.LiveVoiceLinkMicAdapter;
import com.yunbao.live.bean.LiveVoiceGiftBean;
import com.yunbao.live.bean.LiveVoiceLinkMicBean;
import com.yunbao.live.bean.LiveVoiceMixUserBean;
import com.yunbao.live.floatwindow.FloatWindowUtil;
import com.yunbao.live.interfaces.LivePushListener;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.ClientRoleOptions;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class LiveChatRoomLinkMicAgoraViewHolder extends AbsLiveChatRoomLinkMicViewHolder {

    private static final String TAG = "LiveChatRoomLinkMicAgoraViewHolder";
    private int mUserCount = 8;
    private List<LiveVoiceLinkMicBean> mList;
    private RefreshAdapter mAdapter;
    private LivePushListener mLivePushListener;
    private Handler mHandler;
    private boolean mPushMute;
    private boolean mPaused;
    private boolean mCameraReady;
    private boolean mPushSucceed;//是否推流成功
    private TextureView[] mTextureViewArr;
    private ImageView[] mCoverArr;
    private Drawable mBgDrawable;
    private View mRootContainer;
    private boolean mChatRoomTypeVideo;
    private RtcEngineEx mEngine;
    private int mLiveUid;
    private int mUid;
    private boolean mIsAnchor;


    public LiveChatRoomLinkMicAgoraViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_chatroom_link_mic_agora;
    }

    @Override
    public void init() {
        mUid = Integer.parseInt(CommonAppConfig.getInstance().getUid());
        mIsAnchor = mContext instanceof LiveAnchorActivity;
        Drawable bgDrawable = ContextCompat.getDrawable(mContext, R.mipmap.bg_live_voice);
        ImageView bgImage = findViewById(R.id.bg_image);
        bgImage.setImageDrawable(bgDrawable);
        mBgDrawable = bgDrawable;
        mList = new ArrayList<>();
        mRootContainer = findViewById(R.id.voice_link_mic_container);
        if (mIsAnchor) {
            mLiveUid = mUid;
        } else {
            mRootContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((LiveAudienceActivity) mContext).light();
                }
            });
        }
        mHandler = new Handler();
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
                if (mEngine == null || mUid == uid) {
                    return;
                }
                if (mChatRoomTypeVideo) {
//                    if (mLiveUid == uid) {
//                        VideoCanvas remote = new VideoCanvas(mTextureViewArr[0], RENDER_MODE_HIDDEN, uid);
//                        mEngine.setupRemoteVideo(remote);
//                    }
                    TextureView textureView = getPlayView(String.valueOf(uid));
                    if (textureView != null) {
                        VideoCanvas remote = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, uid);
                        mEngine.setupRemoteVideo(remote);
                    }
                }

            }

            @Override
            public void onUserOffline(int uid, int reason) {
                L.e(TAG, "IRtcEngineEventHandler---onUserOffline--->" + uid);
                if (mLiveUid == uid) {
                    if (!mIsAnchor && LiveChatRoomPlayUtil.getInstance().isKeepAlive()) {
                        FloatWindowUtil.getInstance().release();
                    }
                }
            }

            @Override
            public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
                L.e(TAG, "IRtcEngineEventHandler---onFirstRemoteVideoFrame--->" + uid);
            }

            @Override
            public void onFirstRemoteAudioFrame(int uid, int elapsed) {
                L.e(TAG, "IRtcEngineEventHandler---onFirstRemoteAudioFrame--->" + uid);
            }



            @Override
            public void onAudioPublishStateChanged(String channel, int oldState, int newState, int elapseSinceLastState) {
                L.e(TAG, "IRtcEngineEventHandler---onAudioPublishStateChanged--->");
                if (newState == PUB_STATE_PUBLISHED) {
                    if (!mPushSucceed) {
                        mPushSucceed = true;
                        if (mLivePushListener != null) {
                            mLivePushListener.onPushStart();
                        }
                        L.e(TAG, "IRtcEngineEventHandler--->推流成功");
                    }
                }
            }
        };
        config.mEventHandler = engineEventHandler;
        try {
            mEngine = (RtcEngineEx) RtcEngine.create(config);
        } catch (Exception e) {
            mEngine = null;
        }
        if (mEngine != null) {
            mEngine.setClientRole(mIsAnchor ? CLIENT_ROLE_BROADCASTER : CLIENT_ROLE_AUDIENCE);
        }
    }

    public void setChatRoomType(int chatRoomType, UserBean anchorInfo) {
        mChatRoomTypeVideo = chatRoomType == Constants.CHAT_ROOM_TYPE_VIDEO;
        if (mChatRoomTypeVideo) {
            mEngine.enableVideo();
            VideoEncoderConfiguration encoderConfiguration = new VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_360x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
            );
            encoderConfiguration.orientationMode = ORIENTATION_MODE_FIXED_PORTRAIT;
            encoderConfiguration.mirrorMode = VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED;
            mEngine.setVideoEncoderConfiguration(encoderConfiguration);
        } else {
            mEngine.disableVideo();
        }
        LiveChatRoomPlayUtil.getInstance().setAgoraEngine(!mIsAnchor && !mChatRoomTypeVideo ? mEngine : null);

        if (chatRoomType == Constants.CHAT_ROOM_TYPE_VOICE) {
            mUserCount = 8;
            for (int i = 0; i < mUserCount; i++) {
                mList.add(new LiveVoiceLinkMicBean());
            }
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) recyclerView.getLayoutParams();
            lp.topMargin = DpUtil.dp2px(230);
            recyclerView.setLayoutParams(lp);
            recyclerView.setLayoutManager(new GridLayoutManager(mContext, 4, GridLayoutManager.VERTICAL, false));
            mAdapter = new LiveVoiceLinkMicAdapter(mContext, mList);
            recyclerView.setAdapter(mAdapter);
        } else {
            mUserCount = 6;
            for (int i = 0; i < mUserCount; i++) {
                mList.add(new LiveVoiceLinkMicBean());
            }
            mTextureViewArr = new TextureView[mUserCount];
            mTextureViewArr[0] = findViewById(R.id.camera_preview_0);
            mTextureViewArr[1] = findViewById(R.id.camera_preview_1);
            mTextureViewArr[2] = findViewById(R.id.camera_preview_2);
            mTextureViewArr[3] = findViewById(R.id.camera_preview_3);
            mTextureViewArr[4] = findViewById(R.id.camera_preview_4);
            mTextureViewArr[5] = findViewById(R.id.camera_preview_5);
            mCoverArr = new ImageView[mUserCount];
            mCoverArr[1] = findViewById(R.id.cover_1);
            mCoverArr[2] = findViewById(R.id.cover_2);
            mCoverArr[3] = findViewById(R.id.cover_3);
            mCoverArr[4] = findViewById(R.id.cover_4);
            mCoverArr[5] = findViewById(R.id.cover_5);
            if (mRootContainer != null) {
                mRootContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        int w = mRootContainer.getWidth();
                        int h = mRootContainer.getHeight();
                        int[] location = new int[2];
                        for (int i = 0; i < mUserCount; i++) {
                            if (mCoverArr[i] != null) {
                                mCoverArr[i].getLocationInWindow(location);
                                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mCoverArr[i].getLayoutParams();
                                lp.width = w;
                                lp.height = h;
                                lp.leftMargin = -location[0];
                                lp.topMargin = -location[1];
                                mCoverArr[i].requestLayout();
                                mCoverArr[i].setImageDrawable(mBgDrawable);
                            }
                        }
                    }
                });
            }

            LiveVoiceLinkMicBean anchorBean = mList.get(0);
            if (anchorBean != null) {
                anchorBean.setUid(anchorInfo.getId());
                anchorBean.setAvatar(anchorInfo.getAvatar());
                anchorBean.setUserName(anchorInfo.getUserNiceName());
                anchorBean.setStatus(Constants.VOICE_CTRL_OPEN);
            }
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new GridLayoutManager(mContext, 3, GridLayoutManager.VERTICAL, false));
            ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 1, 1);
            decoration.setOnlySetItemOffsetsButNoDraw(true);
            recyclerView.addItemDecoration(decoration);
            mAdapter = new LiveVideoLinkMicAdapter(mContext, mList);
            recyclerView.setAdapter(mAdapter);
            findViewById(R.id.group_preview).setVisibility(View.VISIBLE);
        }
    }


    public void setVideoCoversVisible() {
        if ((((LiveActivity) mContext).isChatRoomTypeVideo()) && mCoverArr != null && mList != null) {
            for (int i = 0; i < mCoverArr.length; i++) {
                if (mCoverArr[i] != null) {
                    LiveVoiceLinkMicBean bean = mList.get(i);
                    if (bean != null) {
                        if (bean.getStatus() == Constants.VOICE_CTRL_CLOSE || bean.getStatus() == Constants.VOICE_CTRL_OPEN) {
                            if (mCoverArr[i].getVisibility() != View.INVISIBLE) {
                                mCoverArr[i].setVisibility(View.INVISIBLE);
                            }
                        } else {
                            if (mCoverArr[i].getVisibility() != View.VISIBLE) {
                                mCoverArr[i].setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
        }
    }


    public TextureView getFirstPreview() {
        return mTextureViewArr[0];
    }


    /**
     * 用户上麦
     *
     * @param toUid    上麦人员的uid
     * @param toName   上麦人员的name
     * @param toAvatar 上麦人员的头像
     * @param position 上麦人员的位置
     */
    public void onUserUpMic(String toUid, String toName, String toAvatar, int position) {
        if (TextUtils.isEmpty(toUid)) {
            return;
        }
        LiveVoiceLinkMicBean bean = mList.get(position);
        bean.setUid(toUid);
        bean.setUserName(toName);
        bean.setAvatar(toAvatar);
        bean.setStatus(Constants.VOICE_CTRL_OPEN);
        bean.setFaceIndex(-1);
        bean.setUserStream(null);
        if (mAdapter != null) {
            mAdapter.notifyItemChanged(position);
        }
        if (mHandler != null) {
            mHandler.removeMessages(position);
        }
        setVideoCoversVisible();
    }


    /**
     * 用户下麦
     *
     * @param uid 下麦人员的uid
     */
    public void onUserDownMic(String uid) {
        onUserDownMic(getUserPosition(uid));
    }

    /**
     * 用户下麦
     *
     * @param position 下麦人员的position
     */
    public void onUserDownMic(int position) {
        if (position >= 0 && position < mUserCount) {
            LiveVoiceLinkMicBean bean = mList.get(position);
            bean.setUid(null);
            bean.setUserName(null);
            bean.setAvatar(null);
            bean.setStatus(Constants.VOICE_CTRL_EMPTY);
            bean.setFaceIndex(-1);
            bean.setUserStream(null);
            if (mAdapter != null) {
                mAdapter.notifyItemChanged(position);
            }
            if (mHandler != null) {
                mHandler.removeMessages(position);
            }
            setVideoCoversVisible();
        }
    }


    /**
     * 语音聊天室--主播控制麦位 闭麦开麦禁麦等
     *
     * @param position 麦位
     * @param status   麦位的状态 -1 关麦；  0无人； 1开麦 ； 2 禁麦；
     */
    public void onControlMicPosition(int position, int status) {
        LiveVoiceLinkMicBean bean = mList.get(position);
        bean.setStatus(status);
        if (mAdapter != null) {
            mAdapter.notifyItemChanged(position, Constants.PAYLOAD);
        }
    }

    /**
     * 语音聊天室--收到上麦观众发送表情的消息
     *
     * @param uid       上麦观众的uid
     * @param faceIndex 表情标识
     */
    public void onVoiceRoomFace(String uid, int faceIndex) {
        int position = getUserPosition(uid);
        if (position >= 0 && position < mUserCount) {
            LiveVoiceLinkMicBean bean = mList.get(position);
            bean.setFaceIndex(faceIndex);
            if (mAdapter != null) {
                mAdapter.notifyItemChanged(position, Constants.VOICE_FACE);
            }
            if (mHandler == null) {
                mHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        int pos = msg.what;
                        LiveVoiceLinkMicBean bean0 = mList.get(pos);
                        bean0.setFaceIndex(-1);
                        if (mAdapter != null) {
                            mAdapter.notifyItemChanged(pos, Constants.VOICE_FACE);
                        }
                    }
                };
            } else {
                mHandler.removeMessages(position);
            }
            mHandler.sendEmptyMessageDelayed(position, 5000);
        }
    }

    /**
     * 设置静音
     */
    public void setPushMute(boolean pushMute) {
        if (mPushMute != pushMute) {
            mPushMute = pushMute;
            setMute(pushMute);
        }
    }

    /**
     * 设置推流静音
     */
    private void setMute(boolean pushMute) {
        if (mEngine != null) {
            mEngine.muteLocalAudioStream(pushMute);
        }
    }


    /**
     * 开始推流
     */
    public void startPush(String pushUrl, LivePushListener pushListener) {
        mLivePushListener = pushListener;
        mPushSucceed = false;
        if (mEngine != null) {
            if (mChatRoomTypeVideo) {
                TextureView textureView = getPlayView(CommonAppConfig.getInstance().getUid());
                if (textureView != null) {
                    VideoCanvas videoCanvas = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, mUid);
                    videoCanvas.mirrorMode = VIDEO_MIRROR_MODE_ENABLED;
                    mEngine.setupLocalVideo(videoCanvas);
                    mEngine.startPreview();
                }
            }
            mEngine.setClientRole(CLIENT_ROLE_BROADCASTER);
            mPushMute = false;
            setMute(false);
        }
    }


    /**
     * 停止推流
     */
    public void stopPush() {
        if (mEngine != null) {
            mEngine.setClientRole(CLIENT_ROLE_AUDIENCE);
            if (mChatRoomTypeVideo) {
                mEngine.stopPreview();
                mEngine.setupLocalVideo(new VideoCanvas(null, RENDER_MODE_HIDDEN, mUid));
            }
        }
    }

    /**
     * 语音聊天室--观众下麦
     *
     * @param uid 下麦观众的uid
     */
    @Override
    public void stopPlay(String uid) {
        if (mEngine != null) {
            mEngine.setupRemoteVideo(new VideoCanvas(null, RENDER_MODE_HIDDEN, Integer.parseInt(uid)));
        }
    }

    @Override
    public void stopPlay(int position) {

    }

    /**
     * 语音聊天室--观众上麦
     *
     * @param uid 上麦观众的uid
     */
    public void playAccStream(String uid, String pull, String userStream) {
        /*if (mEngine != null) {
            TextureView textureView = getPlayView(uid);
            if (textureView != null) {
                VideoCanvas remote = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, Integer.parseInt(uid));
                mEngine.setupRemoteVideo(remote);
            }
        }*/
    }


    /**
     * 获取用户在麦上的位置
     */
    public int getUserPosition(String uid) {
        if (!TextUtils.isEmpty(uid)) {
            for (int i = 0; i < mUserCount; i++) {
                LiveVoiceLinkMicBean bean = mList.get(i);
                if (uid.equals(bean.getUid())) {
                    return i;
                }
            }
        }
        return -1;
    }


    /**
     * 获取用户
     */
    public LiveVoiceLinkMicBean getUserBean(int position) {
        if (position >= 0 && position < mUserCount) {
            return mList.get(position);
        }
        return null;
    }


    /**
     * 获取用户
     */
    public LiveVoiceLinkMicBean getUserBean(String toUid) {
        return getUserBean(getUserPosition(toUid));
    }


    /**
     * 主播混流时候获取上麦用户的Stream
     */
    public List<LiveVoiceMixUserBean> getUserStreamForMix() {
        return null;
    }

    /**
     * 显示房间用户数据
     */
    public void showUserList(JSONArray arr) {
        for (int i = 0; i < mUserCount; i++) {
            LiveVoiceLinkMicBean bean = mList.get(i);
            JSONObject obj = arr.getJSONObject(i);
            bean.setUid(obj.getString("id"));
            bean.setUserName(obj.getString("user_nickname"));
            bean.setAvatar(obj.getString("avatar"));
            bean.setStatus(obj.getIntValue("mic_status"));
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        setVideoCoversVisible();
    }


    public List<LiveVoiceGiftBean> getVoiceGiftUserList() {
        List<LiveVoiceGiftBean> list = null;
        int startIndex = ((LiveActivity) mContext).isChatRoomTypeVideo() ? 1 : 0;
        for (int i = startIndex; i < mUserCount; i++) {
            LiveVoiceLinkMicBean bean = mList.get(i);
            if (!bean.isEmpty()) {
                LiveVoiceGiftBean giftUserBean = new LiveVoiceGiftBean();
                giftUserBean.setUid(bean.getUid());
                giftUserBean.setAvatar(bean.getAvatar());
                giftUserBean.setType(i);
                if (list == null) {
                    list = new ArrayList<>();
                }
                list.add(giftUserBean);
            }
        }
        return list;
    }


    @Override
    public void release() {
        mLivePushListener = null;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (!mIsAnchor) {
            if (mEngine != null) {
                if (mChatRoomTypeVideo) {
                    mEngine.stopPreview();
                }
                if (!LiveChatRoomPlayUtil.getInstance().isKeepAlive()) {
                    mEngine.leaveChannel();
                    if (mHandler != null) {
                        mHandler.post(RtcEngine::destroy);
                    }
                }
            }
            mEngine = null;
        }
        mHandler = null;
        super.release();
    }

    @Override
    public void onPause() {
        if (!mPushMute) {
            setMute(true);
        }
        if (mEngine != null) {
            mEngine.muteAllRemoteAudioStreams(true);
        }
        mPaused = true;
    }

    @Override
    public void onResume() {
        if (mPaused) {
            if (!mPushMute) {
                setMute(false);
            }
        }
        if (mEngine != null) {
            mEngine.muteAllRemoteAudioStreams(false);
        }
        mPaused = false;
    }

    private TextureView getPlayView(String uid) {
        int index = getUserPosition(uid);
        if (index >= 0 && index < mTextureViewArr.length) {
            return mTextureViewArr[index];
        }
        return null;
    }

    public void leaveChannel() {
        if (!mIsAnchor && LiveChatRoomPlayUtil.getInstance().isKeepAlive()) {
            return;
        }
        if (mEngine != null) {
            mEngine.leaveChannel();
            if (mChatRoomTypeVideo) {
                mEngine.stopPreview();
            }
        }
    }

    public void audienceJoinChannel(String agoraToken, String channelId, int liveUid) {
        if (!mIsAnchor && LiveChatRoomPlayUtil.getInstance().isKeepAlive()) {
            return;
        }
        mLiveUid = liveUid;
        if (mEngine != null) {
            ChannelMediaOptions option = new ChannelMediaOptions();
            option.channelProfile = CHANNEL_PROFILE_LIVE_BROADCASTING;
            option.clientRoleType = CLIENT_ROLE_AUDIENCE;
            option.autoSubscribeAudio = true;
            option.publishMicrophoneTrack = true;
            if (mChatRoomTypeVideo) {
                option.autoSubscribeVideo = true;
                option.publishCameraTrack = true;
            }
            mEngine.joinChannel(agoraToken, channelId, mUid, option);
        }
    }

    public void startPreview(TextureView textureView) {
        if (mIsAnchor && mEngine != null) {
            VideoCanvas videoCanvas = new VideoCanvas(textureView, RENDER_MODE_HIDDEN, mUid);
            videoCanvas.mirrorMode = VIDEO_MIRROR_MODE_ENABLED;
            mEngine.setupLocalVideo(videoCanvas);
            mEngine.startPreview();
        }
    }

    public void anchorJoinChannel(String agoraToken, String channelId) {
        if (mIsAnchor && mEngine != null) {
            ChannelMediaOptions option = new ChannelMediaOptions();
            option.channelProfile = CHANNEL_PROFILE_LIVE_BROADCASTING;
            option.autoSubscribeAudio = true;
            option.publishMicrophoneTrack = true;
            if (mChatRoomTypeVideo) {
                option.autoSubscribeVideo = true;
                option.publishCameraTrack = true;
            }
            mEngine.joinChannel(agoraToken, channelId, mUid, option);
        }
    }

    public boolean startBgm(String path) {
        if (mIsAnchor && mEngine != null) {
            int res = mEngine.startAudioMixing(path, false, 1, 0);
            if (res == 0) {
                //该方法调节混音音乐文件在本端和远端的播放音量大小。取值范围为 [0,100]，100 （默认值）为原始音量。
                mEngine.adjustAudioMixingVolume(80);
                return true;
            }
        }
        return false;
    }

    public void stopBgm() {
        if (mIsAnchor && mEngine != null) {
            mEngine.stopAudioMixing();
        }
    }

    public void anchorRelease() {
        if (mIsAnchor && mEngine != null) {
            mEngine.leaveChannel();
            if (mChatRoomTypeVideo) {
                mEngine.stopPreview();
            }
            if (mHandler != null) {
                mHandler.post(RtcEngine::destroy);
            }
            mEngine = null;
        }
    }

    public void setLivePushListener(LivePushListener livePushListener) {
        mLivePushListener = livePushListener;
    }
}
