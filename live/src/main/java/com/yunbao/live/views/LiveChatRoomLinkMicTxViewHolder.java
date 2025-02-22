package com.yunbao.live.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.tencent.liteav.audio.TXAudioEffectManager;
import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePusher;
import com.tencent.live2.V2TXLivePusherObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.tencent.live2.impl.V2TXLivePusherImpl;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.custom.ItemDecoration;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.ScreenDimenUtil;
import com.yunbao.live.R;
import com.yunbao.live.activity.LiveActivity;
import com.yunbao.live.activity.LiveAnchorActivity;
import com.yunbao.live.activity.LiveAudienceActivity;
import com.yunbao.live.adapter.LiveVideoLinkMicAdapter;
import com.yunbao.live.adapter.LiveVoiceLinkMicAdapter;
import com.yunbao.live.bean.LiveVoiceGiftBean;
import com.yunbao.live.bean.LiveVoiceLinkMicBean;
import com.yunbao.live.bean.LiveVoiceMixUserBean;
import com.yunbao.live.interfaces.LivePushListener;

import java.util.ArrayList;
import java.util.List;

public class LiveChatRoomLinkMicTxViewHolder extends AbsLiveChatRoomLinkMicViewHolder {

    private static final String TAG = "LiveChatRoomLinkMicViewHolder";
    private int mUserCount = 8;
    private List<LiveVoiceLinkMicBean> mList;
    private RefreshAdapter mAdapter;
    private V2TXLivePusher mVoiceSelfPusher;
    private V2TXLivePusher mVideoSelfPusher;
    private LivePushListener mLivePushListener;
    private V2TXLivePlayer[] mLivePlayerArr;
    private Handler mHandler;
    private boolean mPushMute;
    private boolean mPaused;
    private boolean mPushSucceed;//是否推流成功
    private TextureView[] mTextureViewArr;
    private ImageView[] mCoverArr;
    private boolean mIsFirstTextureViewSmall;
    private Drawable mBgDrawable;
    private View mRootContainer;


    public LiveChatRoomLinkMicTxViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_chatroom_link_mic;
    }

    @Override
    public void init() {
        Drawable bgDrawable = ContextCompat.getDrawable(mContext, R.mipmap.bg_live_voice);
        ImageView bgImage = findViewById(R.id.bg_image);
        bgImage.setImageDrawable(bgDrawable);
        mBgDrawable = bgDrawable;
        mList = new ArrayList<>();
        mRootContainer = findViewById(R.id.voice_link_mic_container);
        if (mContext instanceof LiveAudienceActivity) {
            mRootContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((LiveAudienceActivity) mContext).light();
                }
            });
        }
    }

    public void setChatRoomType(int chatRoomType, UserBean anchorInfo) {
        if (chatRoomType == Constants.CHAT_ROOM_TYPE_VOICE) {
            mUserCount = 8;
            mLivePlayerArr = new V2TXLivePlayerImpl[mUserCount];
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
            mLivePlayerArr = new V2TXLivePlayerImpl[mUserCount];
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

            if (mContext instanceof LiveAnchorActivity) {
                setFirstTextureViewSmall(true);
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

    private void checkFirstTextureViewSmall() {
        if (mContext instanceof LiveAudienceActivity) {
            boolean isSelfUpMic = getUserPosition(CommonAppConfig.getInstance().getUid()) >= 0;//自己是否上麦了
            boolean isOtherUpMic = false;//是否有其他人上麦
            for (int i = 1; i < mList.size(); i++) {
                int status = mList.get(i).getStatus();
                if (status == Constants.VOICE_CTRL_OPEN || status == Constants.VOICE_CTRL_CLOSE) {
                    isOtherUpMic = true;
                    break;
                }
            }
            setFirstTextureViewSmall(isSelfUpMic || !isOtherUpMic);
        }
    }

    private void setFirstTextureViewSmall(boolean isSmall) {
        if (((LiveActivity) mContext).isChatRoomTypeVideo()) {
            if (mIsFirstTextureViewSmall != isSmall) {
                mIsFirstTextureViewSmall = isSmall;
                if (mTextureViewArr[0] != null) {
                    ViewGroup.LayoutParams lp = mTextureViewArr[0].getLayoutParams();
                    if (isSmall) {
                        lp.width = ScreenDimenUtil.getInstance().getScreenWidth() / 3;
                        lp.height = lp.width;
                    } else {
                        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                    mTextureViewArr[0].setLayoutParams(lp);
                }
            }
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
        checkFirstTextureViewSmall();
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
            checkFirstTextureViewSmall();
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
        if (((LiveActivity) mContext).isChatRoomTypeVideo()) {
            if (mVideoSelfPusher != null) {
                TXAudioEffectManager effectManager = mVideoSelfPusher.getAudioEffectManager();
                if (effectManager != null) {
                    effectManager.setVoiceCaptureVolume(pushMute ? 0 : 100);
                }
            }
        } else {
            if (mVoiceSelfPusher != null) {
                TXAudioEffectManager effectManager = mVoiceSelfPusher.getAudioEffectManager();
                if (effectManager != null) {
                    effectManager.setVoiceCaptureVolume(pushMute ? 0 : 100);
                }
            }
        }
    }

    private V2TXLivePusher createPusher() {
        V2TXLivePusherImpl pusher = new V2TXLivePusherImpl(mContext, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTC);
        pusher.setObserver(new V2TXLivePusherObserver() {
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
        V2TXLiveDef.V2TXLiveVideoEncoderParam videoParam = new V2TXLiveDef.V2TXLiveVideoEncoderParam(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution480x360);
        videoParam.videoResolutionMode = V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModePortrait;
        pusher.setVideoQuality(videoParam);
        pusher.setEncoderMirror(true);
        int index = getUserPosition(CommonAppConfig.getInstance().getUid());
        if (mTextureViewArr[index].getVisibility() != View.VISIBLE) {
            mTextureViewArr[index].setVisibility(View.VISIBLE);
        }
        pusher.setRenderView(mTextureViewArr[index]);
        return pusher;
    }

    /**
     * 开始推流
     */
    public void startPush(String pushUrl, LivePushListener pushListener) {
        mLivePushListener = pushListener;
        if (((LiveActivity) mContext).isChatRoomTypeVideo()) {
            startVideoPush(pushUrl);
        } else {
            startVoicePush(pushUrl);
        }
    }

    private void startVideoPush(String pushUrl) {
        V2TXLivePusher pusher = createPusher();
        pusher.startMicrophone();
        pusher.startCamera(true);
        mPushSucceed = false;
        pusher.startPush(pushUrl);
        mVideoSelfPusher = pusher;
    }

    private void startVoicePush(String pushUrl) {
        if (mVoiceSelfPusher == null) {
            mVoiceSelfPusher = new V2TXLivePusherImpl(mContext, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTC);
            mVoiceSelfPusher.setObserver(new V2TXLivePusherObserver() {
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
        mVoiceSelfPusher.startMicrophone();
        mPushMute = false;
        setMute(false);
        mPushSucceed = false;
        mVoiceSelfPusher.startPush(pushUrl);
    }


    /**
     * 停止推流
     */
    public void stopPush() {
        if (((LiveActivity) mContext).isChatRoomTypeVideo()) {
            if (mVideoSelfPusher != null) {
                mVideoSelfPusher.stopPush();
                mVideoSelfPusher.stopCamera();
                mVideoSelfPusher.stopMicrophone();
            }
        } else {
            if (mVoiceSelfPusher != null) {
                mVoiceSelfPusher.stopPush();
                mVoiceSelfPusher.stopMicrophone();
            }
        }

    }

    /**
     * 停止播放
     */
    public void stopPlay(String uid) {
        stopPlay(getUserPosition(uid));
    }


    /**
     * 停止播放
     */
    public void stopPlay(int position) {
        if (mLivePlayerArr != null) {
            if (position >= 0 && position < mUserCount) {
                V2TXLivePlayer player = mLivePlayerArr[position];
                if (player != null && player.isPlaying() == 1) {
                    player.stopPlay();
                }
                if (mTextureViewArr != null && mTextureViewArr[position] != null) {
                    if (mTextureViewArr[position].getVisibility() != View.INVISIBLE) {
                        mTextureViewArr[position].setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
    }

    /**
     * 停止所有播放
     */
    public void stopAllPlay() {
        if (mLivePlayerArr != null) {
            for (V2TXLivePlayer player : mLivePlayerArr) {
                if (player != null && player.isPlaying() == 1) {
                    player.stopPlay();
                }
            }
        }
        if (mTextureViewArr != null) {
            for (int i = 1; i < mTextureViewArr.length; i++) {
                if (mTextureViewArr[i] != null && mTextureViewArr[i].getVisibility() != View.INVISIBLE) {
                    mTextureViewArr[i].setVisibility(View.INVISIBLE);
                }
            }
        }
    }


    /**
     * 语音聊天室--播放上麦观众的低延时流
     *
     * @param uid        上麦观众的uid
     * @param pull       上麦观众的低延时流地址
     * @param userStream 上麦观众的流名，主播混流用
     */
    public void playAccStream(String uid, String pull, String userStream) {
        int position = getUserPosition(uid);
        if (position >= 0 && position < mUserCount) {
            LiveVoiceLinkMicBean bean = mList.get(position);
            bean.setUserStream(userStream);
            V2TXLivePlayer player = mLivePlayerArr[position];
            if (player == null) {
                player = createPlayer(position);
                mLivePlayerArr[position] = player;
            }
            player.startLivePlay(pull);
        }
    }

    private V2TXLivePlayer createPlayer(int position) {
        V2TXLivePlayer player = new V2TXLivePlayerImpl(mContext);
        if (((LiveActivity) mContext).isChatRoomTypeVideo()) {
            player.setRenderFillMode(V2TXLiveDef.V2TXLiveFillMode.V2TXLiveFillModeFill);
            if (mTextureViewArr[position].getVisibility() != View.VISIBLE) {
                mTextureViewArr[position].setVisibility(View.VISIBLE);
            }
            player.setRenderView(mTextureViewArr[position]);
        }
        return player;
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
        List<LiveVoiceMixUserBean> list = null;
        LiveVoiceLinkMicBean linkMicBean = null;
        for (int i = 0; i < mUserCount; i++) {
            linkMicBean = mList.get(i);
            if (linkMicBean.getStatus() > 0) {
                String uid = linkMicBean.getUid();
                String userStream = linkMicBean.getUserStream();
                if (!TextUtils.isEmpty(uid) && !uid.equals(CommonAppConfig.getInstance().getUid()) && !TextUtils.isEmpty(userStream)) {
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(new LiveVoiceMixUserBean(i, uid, userStream));
                }
            }
        }
        return list;
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
        checkFirstTextureViewSmall();
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
        stopAllPlay();
        if (mVideoSelfPusher != null) {
            mVideoSelfPusher.stopPush();
            mVideoSelfPusher.stopCamera();
            mVideoSelfPusher.stopMicrophone();
            mVideoSelfPusher.release();
        }
        mVideoSelfPusher = null;
        if (mVoiceSelfPusher != null) {
            mVoiceSelfPusher.stopPush();
            mVoiceSelfPusher.stopMicrophone();
            mVoiceSelfPusher.release();
        }
        mVoiceSelfPusher = null;
        mLivePushListener = null;
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        super.release();
    }

    @Override
    public void onPause() {
        if (!mPushMute) {
            setMute(true);
        }
        if (mLivePlayerArr != null) {
            for (V2TXLivePlayer player : mLivePlayerArr) {
                if (player != null) {
                    player.setPlayoutVolume(0);
                }
            }
        }
        mPaused = true;
    }

    @Override
    public void onResume() {
        if (mPaused) {
            if (!mPushMute) {
                setMute(false);
            }
            if (mLivePlayerArr != null) {
                for (V2TXLivePlayer player : mLivePlayerArr) {
                    if (player != null) {
                        player.setPlayoutVolume(100);
                    }
                }
            }
        }
        mPaused = false;
    }
}
