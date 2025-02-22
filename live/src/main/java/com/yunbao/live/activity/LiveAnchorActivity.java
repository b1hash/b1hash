package com.yunbao.live.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.beauty.interfaces.IBeautyViewHolder;
import com.yunbao.beauty.views.BeautyViewHolder;
import com.yunbao.beauty.views.SimpleBeautyViewHolder;
import com.opensource.svgaplayer.SVGAImageView;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.bean.GoodsBean;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.dialog.NotCancelableDialog;
import com.yunbao.common.event.LoginInvalidEvent;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.DateFormatUtil;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.LogUtil;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.game.bean.GameParam;
import com.yunbao.game.dialog.GameDialogFragment;
import com.yunbao.game.event.GameWindowChangedEvent;
import com.yunbao.game.util.GamePresenter;
import com.yunbao.im.event.ImUnReadCountEvent;
import com.yunbao.im.utils.ImMessageUtil;
import com.yunbao.im.utils.ImUnReadCount;
import com.yunbao.live.R;
import com.yunbao.live.bean.LiveBean;
import com.yunbao.live.bean.LiveConfigBean;
import com.yunbao.live.bean.LiveGuardInfo;
import com.yunbao.live.bean.LiveReceiveGiftBean;
import com.yunbao.live.bean.LiveVoiceMixUserBean;
import com.yunbao.live.dialog.LiveFunctionDialogFragment;
import com.yunbao.live.dialog.LiveLinkMicListDialogFragment;
import com.yunbao.live.dialog.LiveShopDialogFragment;
import com.yunbao.live.dialog.LiveVoiceControlFragment;
import com.yunbao.live.dialog.SuperAdminTipDialogFragment;
import com.yunbao.live.event.LinkMicTxMixStreamEvent;
import com.yunbao.live.event.LiveVoiceMicStatusEvent;
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;
import com.yunbao.live.interfaces.LiveFunctionClickListener;
import com.yunbao.live.interfaces.LivePushListener;
import com.yunbao.live.interfaces.TxLinkMicPusher;
import com.yunbao.live.music.LiveMusicDialogFragment;
import com.yunbao.live.presenter.LiveLinkMicAnchorPresenter;
import com.yunbao.live.presenter.LiveLinkMicPkPresenter;
import com.yunbao.live.presenter.LiveLinkMicPresenter;
import com.yunbao.live.socket.GameActionListenerImpl;
import com.yunbao.live.socket.SocketChatUtil;
import com.yunbao.live.socket.SocketClient;
import com.yunbao.live.socket.SocketVoiceRoomUtil;
import com.yunbao.live.views.AbsLiveChatRoomPushViewHolder;
import com.yunbao.live.views.AbsLivePushViewHolder;
import com.yunbao.live.views.LiveAnchorViewHolder;
import com.yunbao.live.views.LiveChatRoomLinkMicAgoraViewHolder;
import com.yunbao.live.views.LiveChatRoomPushAgoraViewHolder;
import com.yunbao.live.views.LiveEndViewHolder;
import com.yunbao.live.views.LiveGoodsAddViewHolder;
import com.yunbao.live.views.LiveMusicViewHolder;
import com.yunbao.live.views.LivePlatGoodsAddViewHolder;
import com.yunbao.live.views.LivePushAgoraViewHolder;
import com.yunbao.live.views.LivePushScreenTxViewHolder;
import com.yunbao.live.views.LivePushTxViewHolder;
import com.yunbao.live.views.LiveReadyViewHolder;
import com.yunbao.live.views.LiveReadyChatRoomViewHolder;
import com.yunbao.live.views.LiveRoomViewHolder;
import com.yunbao.live.views.LiveVoiceAnchorViewHolder;
import com.yunbao.live.views.LiveChatRoomLinkMicTxViewHolder;
import com.yunbao.live.views.LiveChatRoomPushTxViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by cxf on 2018/10/7.
 * 主播直播间
 */

public class LiveAnchorActivity extends LiveActivity implements LiveFunctionClickListener {

    private static final String TAG = "LiveAnchorActivity";
    private LiveGoodsAddViewHolder mLiveGoodsAddViewHolder;
    private LivePlatGoodsAddViewHolder mLivePlatGoodsAddViewHolder;

    public static void forward(Context context, int liveSdk, LiveConfigBean bean, int haveStore, boolean isVoiceChatRoom, String forbidLiveTip, boolean isScreenRecord) {
        Intent intent = new Intent(context, LiveAnchorActivity.class);
        intent.putExtra(Constants.LIVE_SDK, liveSdk);
        if (bean != null) {
            intent.putExtra(Constants.LIVE_CONFIG, bean);
        }
        intent.putExtra(Constants.HAVE_STORE, haveStore);
        intent.putExtra(Constants.TIP, forbidLiveTip);
        intent.putExtra(Constants.IS_CHAT_ROOM, isVoiceChatRoom);
        intent.putExtra(Constants.SCREEN_RECORD, isScreenRecord);
        context.startActivity(intent);
    }

    private ViewGroup mRoot;
    private ViewGroup mContainerWrap;
    private AbsLivePushViewHolder mLivePushViewHolder;
    private LiveReadyViewHolder mLiveReadyViewHolder;
    private IBeautyViewHolder mLiveBeautyViewHolder;
    private LiveAnchorViewHolder mLiveAnchorViewHolder;
    private LiveVoiceAnchorViewHolder mLiveVoiceAnchorViewHolder;
    private LiveMusicViewHolder mLiveMusicViewHolder;
    private boolean mStartPreview;//是否开始预览
    private boolean mStartLive;//是否开始了直播
    private List<Integer> mGameList;//游戏开关
    private boolean mBgmPlaying;//是否在播放背景音乐
    private LiveConfigBean mLiveConfigBean;
    private HttpCallback mCheckLiveCallback;
    private File mLogFile;
    private int mReqCount;
    private boolean mPaused;
    private boolean mNeedCloseLive = true;
    private boolean mSuperCloseLive;//是否是超管关闭直播
    private boolean mLoginInvalid;//登录是否失效
    private boolean mEnd;
    private LiveLinkMicListDialogFragment mLiveLinkMicListDialogFragment;
    private AbsLiveChatRoomPushViewHolder mLiveChatRoomPushViewHolder;
    private boolean mIsScreenRecord;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_live_anchor;
    }

    @Override
    protected void main() {
        super.main();
        Intent intent = getIntent();
        mLiveSDK = intent.getIntExtra(Constants.LIVE_SDK, Constants.LIVE_SDK_TX);
        mLiveConfigBean = intent.getParcelableExtra(Constants.LIVE_CONFIG);
        int haveStore = intent.getIntExtra(Constants.HAVE_STORE, 0);
        String forbidLiveTip = intent.getStringExtra(Constants.TIP);
        mIsChatRoom = intent.getBooleanExtra(Constants.IS_CHAT_ROOM, false);
        mIsScreenRecord = intent.getBooleanExtra(Constants.SCREEN_RECORD, false);
        mRoot = (ViewGroup) findViewById(R.id.root);
        mSocketUserType = Constants.SOCKET_USER_TYPE_ANCHOR;
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        mLiveUid = u.getId();
        mLiveBean = new LiveBean();
        mLiveBean.setUid(mLiveUid);
        mLiveBean.setUserNiceName(u.getUserNiceName());
        mLiveBean.setAvatar(u.getAvatar());
        mLiveBean.setAvatarThumb(u.getAvatarThumb());
        mLiveBean.setLevelAnchor(u.getLevelAnchor());
        mLiveBean.setGoodNum(u.getGoodName());
        mLiveBean.setCity(u.getCity());

        if (isChatRoom()) {
            if (isTxSdK()) {
                mLiveChatRoomPushViewHolder = new LiveChatRoomPushTxViewHolder(mContext, (ViewGroup) findViewById(R.id.preview_container));
                mLiveChatRoomLinkMicViewHolder = new LiveChatRoomLinkMicTxViewHolder(mContext, mLiveChatRoomPushViewHolder.getContainer());
                mLiveChatRoomLinkMicViewHolder.addToParent();
                mLiveChatRoomLinkMicViewHolder.subscribeActivityLifeCycle();
                mLivePushViewHolder = mLiveChatRoomPushViewHolder;
            } else {
                LiveChatRoomPushAgoraViewHolder liveChatRoomPushAgoraVh = new LiveChatRoomPushAgoraViewHolder(mContext, (ViewGroup) findViewById(R.id.preview_container));
                LiveChatRoomLinkMicAgoraViewHolder liveChatRoomLinkMicAgoraVh = new LiveChatRoomLinkMicAgoraViewHolder(mContext, liveChatRoomPushAgoraVh.getContainer());
                liveChatRoomLinkMicAgoraVh.setLivePushListener(new LivePushListener() {
                    @Override
                    public void onPreviewStart() {
                        //开始预览回调
                        mStartPreview = true;
                    }

                    @Override
                    public void onPushStart() {
                        //开始推流回调
                        LiveHttpUtil.changeLive(mStream);
                    }

                    @Override
                    public void onPushFailed() {
                        //推流失败回调
                        ToastUtil.show(R.string.live_push_failed);
                    }
                });
                liveChatRoomPushAgoraVh.setLiveChatRoomLinkMicAgoraVh(liveChatRoomLinkMicAgoraVh);
                liveChatRoomLinkMicAgoraVh.addToParent();
                liveChatRoomLinkMicAgoraVh.subscribeActivityLifeCycle();
                mLiveChatRoomPushViewHolder = liveChatRoomPushAgoraVh;
                mLiveChatRoomLinkMicViewHolder = liveChatRoomLinkMicAgoraVh;
                mLivePushViewHolder = liveChatRoomPushAgoraVh;
            }
        } else {
            if (isTxSdK()) {
                //添加推流预览控件
                if (mIsScreenRecord) {
                    mLivePushViewHolder = new LivePushScreenTxViewHolder(mContext, (ViewGroup) findViewById(R.id.preview_container));
                } else {
                    mLivePushViewHolder = new LivePushTxViewHolder(mContext, (ViewGroup) findViewById(R.id.preview_container), mLiveConfigBean);
                }
            } else {
                mLivePushViewHolder = new LivePushAgoraViewHolder(mContext, (ViewGroup) findViewById(R.id.preview_container), mIsScreenRecord);
            }
        }

        mLivePushViewHolder.setLivePushListener(new LivePushListener() {
            @Override
            public void onPreviewStart() {
                //开始预览回调
                mStartPreview = true;
            }

            @Override
            public void onPushStart() {
                //开始推流回调
                LiveHttpUtil.changeLive(mStream);
            }

            @Override
            public void onPushFailed() {
                //推流失败回调
                ToastUtil.show(R.string.live_push_failed);
            }
        });
        mLivePushViewHolder.addToParent();
        mLivePushViewHolder.subscribeActivityLifeCycle();
        mContainerWrap = (ViewGroup) findViewById(R.id.container_wrap);
        mContainer = (ViewGroup) findViewById(R.id.container);
        //添加开播前设置控件
        if (!isChatRoom()) {
            mLiveReadyViewHolder = new LiveReadyViewHolder(mContext, mContainer, mLiveSDK, haveStore, forbidLiveTip, mIsScreenRecord);
        } else {
            mLiveReadyViewHolder = new LiveReadyChatRoomViewHolder(mContext, mContainer, mLiveSDK, haveStore, forbidLiveTip);
        }
        mLiveReadyViewHolder.addToParent();
        mLiveReadyViewHolder.subscribeActivityLifeCycle();
        if (!isChatRoom()) {
            mLiveLinkMicPresenter = new LiveLinkMicPresenter(mContext, mLivePushViewHolder, true, mLiveSDK, mContainer);
            mLiveLinkMicPresenter.setLiveUid(mLiveUid);
            mLiveLinkMicAnchorPresenter = new LiveLinkMicAnchorPresenter(mContext, mLivePushViewHolder, true, mLiveSDK, mRoot);
            mLiveLinkMicPkPresenter = new LiveLinkMicPkPresenter(mContext, mLivePushViewHolder, true, mContainer);
        }
    }


    public boolean isStartPreview() {
        return mStartPreview || mIsScreenRecord;
    }

    /**
     * 主播直播间功能按钮点击事件
     *
     * @param functionID
     */
    @Override
    public void onClick(int functionID) {
        switch (functionID) {
            case Constants.LIVE_FUNC_BEAUTY://美颜
                beauty();
                break;
            case Constants.LIVE_FUNC_CAMERA://切换镜头
                toggleCamera();
                break;
            case Constants.LIVE_FUNC_FLASH://切换闪光灯
                toggleFlash();
                break;
            case Constants.LIVE_FUNC_MUSIC://伴奏
                openMusicWindow();
                break;
            case Constants.LIVE_FUNC_SHARE://分享
                openShareWindow();
                break;
            case Constants.LIVE_FUNC_GAME://游戏
                openGameWindow();
                break;
            case Constants.LIVE_FUNC_RED_PACK://红包
                openRedPackSendWindow();
                break;
            case Constants.LIVE_FUNC_LINK_MIC://连麦
                openLinkMicAnchorWindow();
                break;
            case Constants.LIVE_FUNC_MIRROR://镜像
                togglePushMirror();
                break;
            case Constants.LIVE_FUNC_TASK://每日任务
                openDailyTaskWindow();
                break;
            case Constants.LIVE_FUNC_LUCK://幸运奖池
                openPrizePoolWindow();
                break;
            case Constants.LIVE_FUNC_LINK_MIC_AUD:
                if (mLiveAnchorViewHolder != null) {
                    mLiveAnchorViewHolder.changeLinkMicEnable();
                }
                break;
        }
    }

    /**
     * 切换镜像
     */
    private void togglePushMirror() {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.togglePushMirror();
        }
    }


    public void openShop(boolean isOpen) {
        if (mLiveAnchorViewHolder != null) {
            mLiveAnchorViewHolder.setShopBtnVisible(isOpen);
        }
    }


    //打开相机前执行
    public void beforeCamera() {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.setOpenCamera(true);
        }
    }


    /**
     * 切换镜头
     */
    public void toggleCamera() {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.toggleCamera();
        }
    }

    /**
     * 切换闪光灯
     */
    public void toggleFlash() {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.toggleFlash();
        }
    }

    /**
     * 设置美颜
     */

    public void beauty() {
        if (mLiveBeautyViewHolder == null) {
            if (CommonAppConfig.getInstance().isMhBeautyEnable()) {
                mLiveBeautyViewHolder = new BeautyViewHolder(mContext, mRoot);
            } else {
                mLiveBeautyViewHolder = new SimpleBeautyViewHolder(mContext, mRoot, isTxSdK());
            }
            mLiveBeautyViewHolder.setVisibleListener(new IBeautyViewHolder.VisibleListener() {
                @Override
                public void onVisibleChanged(boolean visible) {
                    if (mLiveReadyViewHolder != null) {
                        if (visible) {
                            mLiveReadyViewHolder.hide();
                        } else {
                            mLiveReadyViewHolder.show();
                        }
                    }
                }
            });
        }
        mLiveBeautyViewHolder.show();
    }


    /**
     * 打开音乐窗口
     */
    private void openMusicWindow() {
        if (isLinkMic() || isLinkMicAnchor()) {
            ToastUtil.show(R.string.link_mic_not_bgm);
            return;
        }
        LiveMusicDialogFragment fragment = new LiveMusicDialogFragment();
        fragment.setActionListener(new LiveMusicDialogFragment.ActionListener() {
            @Override
            public void onChoose(String musicId) {
                if (mLivePushViewHolder != null) {
                    if (mLiveMusicViewHolder == null) {
                        mLiveMusicViewHolder = new LiveMusicViewHolder(mContext, mContainer, mLivePushViewHolder);
                        mLiveMusicViewHolder.subscribeActivityLifeCycle();
                        mLiveMusicViewHolder.addToParent();
                    }
                    mLiveMusicViewHolder.play(musicId);
                    mBgmPlaying = true;
                }
            }
        });
        fragment.show(getSupportFragmentManager(), "LiveMusicDialogFragment");
    }

    /**
     * 关闭背景音乐
     */
    public void stopBgm() {
        if (mLiveMusicViewHolder != null) {
            mLiveMusicViewHolder.release();
        }
        mLiveMusicViewHolder = null;
        mBgmPlaying = false;
    }

    public boolean isBgmPlaying() {
        return mBgmPlaying;
    }


    /**
     * 打开功能弹窗
     */
    public void showFunctionDialog() {
        LiveFunctionDialogFragment fragment = new LiveFunctionDialogFragment();
        Bundle bundle = new Bundle();
        boolean hasGame = false;
        if (CommonAppConfig.GAME_ENABLE && mGameList != null) {
            hasGame = mGameList.size() > 0;
        }
        bundle.putBoolean(Constants.HAS_GAME, hasGame);
        bundle.putBoolean(Constants.OPEN_FLASH, mLivePushViewHolder != null && mLivePushViewHolder.isFlashOpen());
        bundle.putBoolean("TASK", mTaskSwitch);
        bundle.putBoolean("screenRecord", mIsScreenRecord);
        boolean isLinkMic = false;
        if (mLiveAnchorViewHolder != null) {
            isLinkMic = mLiveAnchorViewHolder.isLinkMicEnable();
        }
        bundle.putBoolean(Constants.LINK_MIC, isLinkMic);
        fragment.setArguments(bundle);
        fragment.setFunctionClickListener(this);
        fragment.show(getSupportFragmentManager(), "LiveFunctionDialogFragment");
    }

    public void hideLinkMicAnchorWindow() {
        if (mLiveLinkMicListDialogFragment != null) {
            mLiveLinkMicListDialogFragment.dismissAllowingStateLoss();
        }
        mLiveLinkMicListDialogFragment = null;
    }

    public void hideLinkMicAnchorWindow2() {
        mLiveLinkMicListDialogFragment = null;
    }

    /**
     * 打开主播连麦窗口
     */
    private void openLinkMicAnchorWindow() {
        if (mLiveLinkMicAnchorPresenter != null && !mLiveLinkMicAnchorPresenter.canOpenLinkMicAnchor()) {
            return;
        }
        LiveLinkMicListDialogFragment fragment = new LiveLinkMicListDialogFragment();
        mLiveLinkMicListDialogFragment = fragment;
        fragment.show(getSupportFragmentManager(), "LiveLinkMicListDialogFragment");
    }


    /**
     * 打开选择游戏窗口
     */
    private void openGameWindow() {
        if (isLinkMic() || isLinkMicAnchor()) {
            ToastUtil.show(R.string.live_link_mic_cannot_game);
            return;
        }
        if (mGamePresenter != null) {
            GameDialogFragment fragment = new GameDialogFragment();
            fragment.setGamePresenter(mGamePresenter);
            fragment.show(getSupportFragmentManager(), "GameDialogFragment");
        }
    }

    /**
     * 关闭游戏
     */
    public void closeGame() {
        if (mGamePresenter != null) {
            mGamePresenter.closeGame();
        }
    }

    /**
     * 开播成功
     *
     * @param data createRoom返回的数据
     */
    public void startLiveSuccess(String data, int liveType, int liveTypeVal, String title) {
        if (isChatRoom()) {
            if (mLiveChatRoomLinkMicViewHolder != null) {
                mLiveChatRoomLinkMicViewHolder.setChatRoomType(mChatRoomType, CommonAppConfig.getInstance().getUserBean());
                if (mLiveChatRoomPushViewHolder != null) {
                    if (isChatRoomTypeVideo()) {
                        mLiveChatRoomPushViewHolder.startPreview(mLiveConfigBean, mLiveChatRoomLinkMicViewHolder.getFirstPreview());
                    } else {
                        mLiveChatRoomPushViewHolder.startMicrophone();
                    }
                }
            }
        }
        mLiveType = liveType;
        mLiveTypeVal = liveTypeVal;
        if (mLiveBean != null) {
            mLiveBean.setTitle(title);
        }
        //处理createRoom返回的数据
        JSONObject obj = JSON.parseObject(data);
        mStream = obj.getString("stream");
        mDanmuPrice = obj.getString("barrage_fee");
        String playUrl = obj.getString("pull");
        L.e("createRoom----播放地址--->" + playUrl);
        mLiveBean.setPull(playUrl);
        mTxAppId = obj.getString("tx_appid");
        //移除开播前的设置控件，添加直播间控件
        if (mLiveReadyViewHolder != null) {
            mLiveReadyViewHolder.removeFromParent();
            mLiveReadyViewHolder.release();
        }
        mLiveReadyViewHolder = null;
        if (mLiveRoomViewHolder == null) {
            mLiveRoomViewHolder = new LiveRoomViewHolder(mContext, mContainer, (GifImageView) findViewById(R.id.gift_gif), (SVGAImageView) findViewById(R.id.gift_svga), mContainerWrap);
            mLiveRoomViewHolder.addToParent();
            mLiveRoomViewHolder.subscribeActivityLifeCycle();
            mLiveRoomViewHolder.setLiveInfo(mLiveUid, mStream, obj.getIntValue("userlist_time") * 1000);
            mLiveRoomViewHolder.setVotes(obj.getString("votestotal"));
            mLiveRoomViewHolder.setUserNum(0);
            UserBean u = CommonAppConfig.getInstance().getUserBean();
            if (u != null) {
                mLiveRoomViewHolder.setRoomNum(u.getLiangNameTip());
                mLiveRoomViewHolder.setName(u.getUserNiceName());
                mLiveRoomViewHolder.setAvatar(u.getAvatar());
                mLiveRoomViewHolder.setAnchorLevel(u.getLevelAnchor());
            }
            mLiveRoomViewHolder.startAnchorLight();
        }

        //开始推流
        if (mLivePushViewHolder != null) {
            if (isTxSdK()) {
                if (mIsScreenRecord) {
                    startService(new Intent(this, ScreenRecordService.class));
                }
                mLivePushViewHolder.startPush(obj.getString("push"));
            } else {
                mLivePushViewHolder.startPushAgora(obj.getString("user_sw_token"), mStream);
            }
        }
        //开始显示直播时长
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.startAnchorLiveTime();
            mLiveRoomViewHolder.startAnchorCheckLive();
        }
        mStartLive = true;
        mLiveRoomViewHolder.startRefreshUserList();

        //守护相关
        mLiveGuardInfo = new LiveGuardInfo();
        int guardNum = obj.getIntValue("guard_nums");
        mLiveGuardInfo.setGuardNum(guardNum);
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.setGuardNum(guardNum);
        }

        //连接socket
        if (mSocketClient == null) {
            mSocketClient = new SocketClient(obj.getString("chatserver"), this);
        }
        mSocketClient.connect(mLiveUid, mStream);

        int dailytask_switch = obj.getIntValue("dailytask_switch");
        mTaskSwitch = dailytask_switch == 1;
        if (!isChatRoom()) {
            if (mLiveAnchorViewHolder == null) {
                mLiveAnchorViewHolder = new LiveAnchorViewHolder(mContext, mContainer);
                mLiveAnchorViewHolder.addToParent();
                mLiveAnchorViewHolder.setUnReadCount(((LiveActivity) mContext).getImUnReadCount());
            }
            mLiveBottomViewHolder = mLiveAnchorViewHolder;

            if (mLiveLinkMicPresenter != null) {
                mLiveLinkMicPresenter.setSocketClient(mSocketClient);
            }
            if (mLiveLinkMicAnchorPresenter != null) {
                mLiveLinkMicAnchorPresenter.setSocketClient(mSocketClient);
                mLiveLinkMicAnchorPresenter.setPlayUrl(playUrl);
                mLiveLinkMicAnchorPresenter.setSelfStream(mStream);
            }
            if (mLiveLinkMicPkPresenter != null) {
                mLiveLinkMicPkPresenter.setSocketClient(mSocketClient);
                mLiveLinkMicPkPresenter.setLiveUid(mLiveUid);
                mLiveLinkMicPkPresenter.setSelfStream(mStream);
            }

            //奖池等级
            int giftPrizePoolLevel = obj.getIntValue("jackpot_level");

            if (mLiveRoomViewHolder != null) {
                mLiveRoomViewHolder.showBtn(false, giftPrizePoolLevel, dailytask_switch);
            }
            //游戏相关
            if (CommonAppConfig.GAME_ENABLE) {
                mGameList = JSON.parseArray(obj.getString("game_switch"), Integer.class);
                GameParam param = new GameParam();
                param.setContext(mContext);
                param.setParentView(mContainerWrap);
                param.setTopView(mContainer);
                param.setInnerContainer(mLiveRoomViewHolder.getInnerContainer());
                param.setGameActionListener(new GameActionListenerImpl(LiveAnchorActivity.this, mSocketClient));
                param.setLiveUid(mLiveUid);
                param.setStream(mStream);
                param.setAnchor(true);
                param.setCoinName(CommonAppConfig.getInstance().getScoreName());
                param.setObj(obj);
                mGamePresenter = new GamePresenter(param);
                mGamePresenter.setGameList(mGameList);
            }

        } else {
            if (mLiveVoiceAnchorViewHolder == null) {
                mLiveVoiceAnchorViewHolder = new LiveVoiceAnchorViewHolder(mContext, mContainer);
                mLiveVoiceAnchorViewHolder.addToParent();
                mLiveVoiceAnchorViewHolder.setUnReadCount(((LiveActivity) mContext).getImUnReadCount());
            }
            mLiveBottomViewHolder = mLiveVoiceAnchorViewHolder;
        }
    }

    /**
     * 关闭直播
     */
    public void closeLive() {
        if (isChatRoom()) {
            DialogUitl.showStringArrayDialog(mContext,
                    new Integer[][]{
                            {R.string.a_057, ContextCompat.getColor(mContext, R.color.red)}},
                    new DialogUitl.StringArrayDialogCallback() {
                        @Override
                        public void onItemClick(String text, int tag) {
                            if (tag == R.string.a_057) {
                                endLive();
                            }
                        }
                    });
        } else {
            DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.live_end_live), new DialogUitl.SimpleCallback() {
                @Override
                public void onConfirmClick(Dialog dialog, String content) {
                    endLive();
                }
            });
        }
    }


    /**
     * 结束直播
     */
    public void endLive() {
        if (mEnd) {
            return;
        }
        mEnd = true;
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.closeLinkMic();
        }
        //请求关播的接口
        LiveHttpUtil.stopLive(mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    //断开socket
                    if (mSocketClient != null) {
                        mSocketClient.disConnect();
                        mSocketClient = null;
                    }

                    if (mLiveEndViewHolder == null) {
                        mLiveEndViewHolder = new LiveEndViewHolder(mContext, mRoot);
                        mLiveEndViewHolder.subscribeActivityLifeCycle();
                        mLiveEndViewHolder.addToParent();
                        mLiveEndViewHolder.showData(mLiveBean, mStream);
                    }
                    release();
                    mStartLive = false;
                    if (mSuperCloseLive) {

                        LiveHttpUtil.getLiveBanInfo(new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                if (code == 0 && info.length > 0) {
                                    JSONObject obj = JSON.parseObject(info[0]);
                                    String banMsg = obj.getString("ban_msg");
                                    String banNum = obj.getString("ban_num");
                                    CharSequence tip = banMsg;
                                    int startIndex = banMsg.indexOf(banNum);
                                    if (startIndex >= 0) {
                                        SpannableString spannableString = new SpannableString(banMsg);
                                        spannableString.setSpan(new ForegroundColorSpan(0xffff0000), startIndex, startIndex + banNum.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        tip = spannableString;
                                    }
                                    showSuperAdminTipDialog(tip);

                                }
                            }
                        });


                    }
                } else {
                    ToastUtil.show(msg);
                }
            }

            @Override
            public boolean showLoadingDialog() {
                return true;
            }

            @Override
            public Dialog createLoadingDialog() {
                return DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.live_end_ing));
            }

            @Override
            public void onLoginInvalid() {
                mStartLive = false;
                release();
                finish();
                CommonAppConfig.getInstance().clearLoginInfo();
                //退出IM
                ImMessageUtil.getInstance().logoutImClient();
                if (mSuperCloseLive) {
                    RouteUtil.forwardLogin(mContext, WordUtil.getString(R.string.live_illegal));
                } else if (mLoginInvalid) {
                    RouteUtil.forwardLogin(mContext, WordUtil.getString(R.string.login_tip_0));
                } else {
                    RouteUtil.forwardLogin(mContext);
                }
            }

            @Override
            public boolean isUseLoginInvalid() {
                return true;
            }
        });
    }


    @Override
    public void onBackPressed() {
        if (mLiveBeautyViewHolder != null && mLiveBeautyViewHolder.isShowed()) {
            mLiveBeautyViewHolder.hide();
            return;
        }
        if (mStartLive) {
            if (!canBackPressed()) {
                return;
            }
            closeLive();
        } else {
            if (mLivePushViewHolder != null) {
                mLivePushViewHolder.release();
            }
            if (mLiveLinkMicPresenter != null) {
                mLiveLinkMicPresenter.release();
            }
            mLivePushViewHolder = null;
            mLiveLinkMicPresenter = null;
            superBackPressed();
        }
    }


    public void superBackPressed() {
        super.onBackPressed();
    }

    public void release() {
        if (mSocketClient != null) {
            mSocketClient.disConnect();
            mSocketClient = null;
        }
        LiveHttpUtil.cancel(LiveHttpConsts.CHANGE_LIVE);
        LiveHttpUtil.cancel(LiveHttpConsts.STOP_LIVE);
        LiveHttpUtil.cancel(LiveHttpConsts.LIVE_PK_CHECK_LIVE);
        LiveHttpUtil.cancel(LiveHttpConsts.SET_LINK_MIC_ENABLE);
        CommonHttpUtil.cancel(CommonHttpConsts.CHECK_TOKEN_INVALID);
        if (mLiveReadyViewHolder != null) {
            mLiveReadyViewHolder.release();
        }
        if (mLiveMusicViewHolder != null) {
            mLiveMusicViewHolder.release();
        }
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.release();
        }
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.release();
        }
        if (mGamePresenter != null) {
            mGamePresenter.release();
        }
        mLiveMusicViewHolder = null;
        mLiveReadyViewHolder = null;
        mLivePushViewHolder = null;
        mLiveLinkMicPresenter = null;
        mLiveBeautyViewHolder = null;
        mGamePresenter = null;
        super.release();
    }

    @Override
    protected void onDestroy() {
        LiveHttpUtil.cancel(LiveHttpConsts.ANCHOR_CHECK_LIVE);
        LiveHttpUtil.cancel(LiveHttpConsts.GET_LIVE_BAN_INFO);
        super.onDestroy();
        L.e("LiveAnchorActivity-------onDestroy------->");
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (!isChatRoom() && !mIsScreenRecord) {
            if (mNeedCloseLive && mLiveRoomViewHolder != null) {
                mLiveRoomViewHolder.anchorPause();
            }
            sendSystemMessage("主播离开一下，精彩不中断，不要走开哦",
                    "The anchor leaves for a while, the excitement will not be interrupted, don't go away");
        }
        mPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isChatRoom() && !mIsScreenRecord && mPaused) {
            if (mLiveRoomViewHolder != null) {
                mLiveRoomViewHolder.anchorResume();
            }
            sendSystemMessage("主播回来了！", "The host is back!");
            CommonHttpUtil.checkTokenInvalid();
        }
        mPaused = false;
        mNeedCloseLive = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginInvalidEvent(LoginInvalidEvent e) {
        onAnchorInvalid();
    }

    /**
     * 直播间  主播登录失效
     */
    @Override
    public void onAnchorInvalid() {
        mLoginInvalid = true;
        super.onAnchorInvalid();
        endLive();
    }

    /**
     * 超管关闭直播间
     */
    @Override
    public void onSuperCloseLive() {
        mSuperCloseLive = true;
        super.onAnchorInvalid();
        endLive();
    }


    /**
     * 超管给直播间发过来的提示信息
     *
     * @param tip
     */
    private void showSuperAdminTipDialog(CharSequence tip) {
        SuperAdminTipDialogFragment fragment = new SuperAdminTipDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putCharSequence(Constants.TIP, tip);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "SuperAdminTipDialogFragment");
    }

    /**
     * 踢人
     */
    @Override
    public void onKick(String touid) {
        if (mLivePushViewHolder != null) {
            JSONObject obj = new JSONObject();
            obj.put("method", "kick");
            obj.put("touid", touid);
            mLivePushViewHolder.sendSeiMessage(obj.toJSONString());
        }
    }

    /**
     * 直播间警告
     *
     * @param warningTip 警告提示语
     */
    @Override
    public void onLiveRoomWarning(String warningTip) {
        showSuperAdminTipDialog(warningTip);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGameWindowChangedEvent(GameWindowChangedEvent e) {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.setOffsetY(e.getGameViewHeight());
        }
        if (mLiveAnchorViewHolder != null) {
            mLiveAnchorViewHolder.setGameBtnVisible(e.isOpen());
        }
    }


    /**
     * 主播与主播连麦  主播收到其他主播发过来的连麦申请
     */
    @Override
    public void onLinkMicAnchorApply(UserBean u, String stream) {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onLinkMicAnchorApply(u, stream);
        }
    }

    /**
     * 主播与主播连麦  对方主播拒绝连麦的回调
     */
    @Override
    public void onLinkMicAnchorRefuse() {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onLinkMicAnchorRefuse();
        }
    }

    /**
     * 主播与主播连麦  对方主播无响应的回调
     */
    @Override
    public void onLinkMicAnchorNotResponse() {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onLinkMicNotResponse();
        }
    }

    /**
     * 主播与主播连麦  对方主播正在游戏
     */
    @Override
    public void onlinkMicPlayGaming() {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onlinkMicPlayGaming();
        }
    }


    /**
     * 主播与主播连麦  对方主播正在忙的回调
     */
    @Override
    public void onLinkMicAnchorBusy() {
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.onLinkMicAnchorBusy();
        }
    }

    /**
     * 发起主播连麦申请
     *
     * @param pkUid  对方主播的uid
     * @param stream 对方主播的stream
     */
    public void linkMicAnchorApply(final String pkUid, final String stream) {
        if (isBgmPlaying()) {
            DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.link_mic_close_bgm), new DialogUitl.SimpleCallback() {
                @Override
                public void onConfirmClick(Dialog dialog, String content) {
                    stopBgm();
                    checkLiveAnchorMic(pkUid, stream);
                }
            });
        } else {
            checkLiveAnchorMic(pkUid, stream);
        }
    }

    /**
     * 发起主播连麦申请
     *
     * @param pkUid  对方主播的uid
     * @param stream 对方主播的stream
     */
    private void checkLiveAnchorMic(final String pkUid, String stream) {
        LiveHttpUtil.livePkCheckLive(pkUid, stream, mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    if (isTxSdK()) {
                        String playUrl = null;
                        JSONObject obj = JSON.parseObject(info[0]);
                        if (obj != null) {
                            String accUrl = obj.getString("pull");//自己主播的低延时流
                            if (!TextUtils.isEmpty(accUrl)) {
                                playUrl = accUrl;
                            }
                        }
                        if (mLiveLinkMicAnchorPresenter != null) {
                            mLiveLinkMicAnchorPresenter.applyLinkMicAnchor(pkUid, playUrl, mStream);
                        }
                    } else {
                        if (mLiveLinkMicAnchorPresenter != null) {
                            mLiveLinkMicAnchorPresenter.applyLinkMicAnchor(pkUid, null, mStream);
                        }
                    }
                } else {
                    ToastUtil.show(msg);
                }
            }
        });
    }

    /**
     * 设置连麦pk按钮是否可见
     */
    public void setPkBtnVisible(boolean visible) {
        if (mLiveAnchorViewHolder != null) {
            if (visible) {
                if (mLiveLinkMicAnchorPresenter.isLinkMic()) {
                    mLiveAnchorViewHolder.setPkBtnVisible(true);
                }
            } else {
                mLiveAnchorViewHolder.setPkBtnVisible(false);
            }
        }
    }

    /**
     * 发起主播连麦pk
     */
    public void applyLinkMicPk() {
        String pkUid = null;
        if (mLiveLinkMicAnchorPresenter != null) {
            pkUid = mLiveLinkMicAnchorPresenter.getPkUid();
        }
        if (!TextUtils.isEmpty(pkUid) && mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.applyLinkMicPk(pkUid, mStream);
        }
    }

    /**
     * 主播与主播PK  主播收到对方主播发过来的PK申请的回调
     *
     * @param u      对方主播的信息
     * @param stream 对方主播的stream
     */
    @Override
    public void onLinkMicPkApply(UserBean u, String stream) {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkApply(u, stream);
        }
    }

    /**
     * 主播与主播PK  对方主播拒绝pk的回调
     */
    @Override
    public void onLinkMicPkRefuse() {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkRefuse();
        }
    }

    /**
     * 主播与主播PK   对方主播正在忙的回调
     */
    @Override
    public void onLinkMicPkBusy() {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkBusy();
        }
    }

    /**
     * 主播与主播PK   对方主播无响应的回调
     */
    @Override
    public void onLinkMicPkNotResponse() {
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.onLinkMicPkNotResponse();
        }
    }

    /**
     * 腾讯sdk连麦时候主播混流
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLinkMicTxMixStreamEvent(LinkMicTxMixStreamEvent e) {
        if (mLivePushViewHolder != null && mLivePushViewHolder instanceof TxLinkMicPusher) {
            ((TxLinkMicPusher) mLivePushViewHolder).onLinkMicTxMixStreamEvent(e.getType(), e.getToUid(), e.getToStream());
        }
    }

    /**
     * 主播checkLive
     */
    public void checkLive() {
        if (mCheckLiveCallback == null) {
            mCheckLiveCallback = new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0 && info.length > 0) {
                        int status = JSON.parseObject(info[0]).getIntValue("status");
                        printLog(DateFormatUtil.getCurTimeString2() + " <=== " + mReqCount + "----status=" + status + "\n");
                        if (status == 0) {
                            NotCancelableDialog dialog = new NotCancelableDialog();
                            dialog.setContent(WordUtil.getString(R.string.live_anchor_error));
                            dialog.setActionListener(new NotCancelableDialog.ActionListener() {
                                @Override
                                public void onConfirmClick(Context context, DialogFragment dialog) {
                                    dialog.dismiss();
                                    release();
                                    superBackPressed();
                                }
                            });
                            dialog.show(getSupportFragmentManager(), "VersionUpdateDialog");
                        }
                    }
                }

            };
        }
        mReqCount++;
        printLog(DateFormatUtil.getCurTimeString2() + " ===> " + mReqCount + "\n");
        LiveHttpUtil.anchorCheckLive(mLiveUid, mStream, mCheckLiveCallback);
    }


    private void printLog(String content) {
        if (mLogFile == null) {
            File dir = new File(CommonAppConfig.LOG_PATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            mLogFile = new File(dir, DateFormatUtil.getCurTimeString2() + "_" + mLiveUid + "_" + mStream + ".txt");
        }
//        L.e(TAG, content);
        LogUtil.print(mLogFile, content);
    }

    /**
     * 打开商品窗口
     */
    public void openGoodsWindow() {
        LiveShopDialogFragment fragment = new LiveShopDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_UID, mLiveUid);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "LiveShopDialogFragment");
    }


    public void forwardAddGoods() {
        if (mLiveGoodsAddViewHolder == null) {
            mLiveGoodsAddViewHolder = new LiveGoodsAddViewHolder(mContext, mPageContainer);
            mLiveGoodsAddViewHolder.subscribeActivityLifeCycle();
            mLiveGoodsAddViewHolder.addToParent();
        }
        mLiveGoodsAddViewHolder.show();
    }

    /**
     * 添加代卖的平台商品
     */
    public void forwardAddPlatGoods() {
        if (mLivePlatGoodsAddViewHolder == null) {
            mLivePlatGoodsAddViewHolder = new LivePlatGoodsAddViewHolder(mContext, mPageContainer);
            mLivePlatGoodsAddViewHolder.subscribeActivityLifeCycle();
            mLivePlatGoodsAddViewHolder.addToParent();
        }
        mLivePlatGoodsAddViewHolder.show();
    }


    @Override
    protected boolean canBackPressed() {
        if (mLiveGoodsAddViewHolder != null && mLiveGoodsAddViewHolder.isShowed()) {
            mLiveGoodsAddViewHolder.hide();
            return false;
        }
        if (mLivePlatGoodsAddViewHolder != null && mLivePlatGoodsAddViewHolder.isShowed()) {
            mLivePlatGoodsAddViewHolder.hide();
            return false;
        }
        return super.canBackPressed();
    }

    /**
     * 显示道具礼物
     */
    public void showStickerGift(LiveReceiveGiftBean bean) {
        if (CommonAppConfig.getInstance().isMhBeautyEnable() && mLivePushViewHolder != null) {
            String stickerGiftId = bean.getStickerId();
            float playTime = bean.getPlayTime();
            if (!TextUtils.isEmpty(stickerGiftId) && playTime > 0) {
                mLivePushViewHolder.setLiveStickerGift(stickerGiftId, (long) (playTime * 1000));
            }
        }
    }


    /**
     * 发送展示直播间商品的消息
     */
    public void sendLiveGoodsShow(GoodsBean goodsBean) {
        SocketChatUtil.sendLiveGoodsShow(mSocketClient, goodsBean);
    }


    /**
     * 语音聊天室--主播打开控麦窗口
     */
    public void controlMic() {
        LiveVoiceControlFragment fragment = new LiveVoiceControlFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_STREAM, mStream);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "LiveVoiceControlFragment");
    }


    /**
     * 语音聊天室--主播收到观众申请上麦
     */
    @Override
    public void onVoiceRoomApplyUpMic() {
        if (mLiveVoiceAnchorViewHolder != null) {
            mLiveVoiceAnchorViewHolder.setApplyUpMicTipShow(true);
        }
    }


    /**
     * 语音聊天室--主播同意或拒绝用户的上麦申请
     *
     * @param position 上麦的位置 从0开始 -1表示拒绝
     */
    public void handleMicUpApply(UserBean bean, int position) {
        SocketVoiceRoomUtil.handleMicUpApply(mSocketClient, bean, position);
    }


    /**
     * 语音聊天室--所有人收到某人下麦的消息
     *
     * @param uid 下麦的人的uid
     */
    @Override
    public void onVoiceRoomDownMic(String uid, int type) {
        if (mLiveChatRoomLinkMicViewHolder != null) {
            int position = mLiveChatRoomLinkMicViewHolder.getUserPosition(uid);
            if (position != -1) {
                mLiveChatRoomLinkMicViewHolder.onUserDownMic(position);
                if (isTxSdK()) {
                    mLiveChatRoomLinkMicViewHolder.stopPlay(position);//停止播放被下麦的人的流
                } else {
                    mLiveChatRoomLinkMicViewHolder.stopPlay(uid);//停止播放被下麦的人的流
                }
                EventBus.getDefault().post(new LiveVoiceMicStatusEvent(position, Constants.VOICE_CTRL_EMPTY));
                if (isTxSdK()) {
                    liveChatRoomAnchorMix();//重新混流
                }
            }
        }
    }


    /**
     * 语音聊天室--主播控制麦位 闭麦开麦禁麦等
     *
     * @param uid      被操作人的uid
     * @param position 麦位
     * @param status   麦位的状态 -1 关麦；  0无人； 1开麦 ； 2 禁麦；
     */
    @Override
    public void onControlMicPosition(String uid, int position, int status) {
        super.onControlMicPosition(uid, position, status);
        EventBus.getDefault().post(new LiveVoiceMicStatusEvent(position, status));
        if (isChatRoomTypeVideo() && !TextUtils.isEmpty(uid) && uid.equals(CommonAppConfig.getInstance().getUid())) {//被操作人是自己
            if (status == Constants.VOICE_CTRL_OPEN) {
//                ToastUtil.show(R.string.a_056);
                if (mLiveChatRoomPushViewHolder != null) {
                    mLiveChatRoomPushViewHolder.setPushMute(false);
                }
            } else if (status == Constants.VOICE_CTRL_CLOSE) {
//                ToastUtil.show(R.string.a_055);
                if (mLiveChatRoomPushViewHolder != null) {
                    mLiveChatRoomPushViewHolder.setPushMute(true);
                }
            }
        }
    }


    /**
     * 语音聊天室--观众上麦后推流成功，把自己的播放地址广播给所有人
     *
     * @param uid        上麦观众的uid
     * @param pull       上麦观众的播流地址
     * @param userStream 上麦观众的流名，主播混流用
     */
    @Override
    public void onVoiceRoomPushSuccess(final String uid, String pull, final String userStream) {
        if (isTxSdK()) {
            LiveHttpUtil.getNewUpMicPullUrl(userStream, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0 && info.length > 0) {
                        String playUrl = JSON.parseObject(info[0]).getString("play_url");
                        if (mLiveChatRoomLinkMicViewHolder != null) {
                            mLiveChatRoomLinkMicViewHolder.playAccStream(uid, playUrl, userStream);
                        }
                        liveChatRoomAnchorMix();//重新混流
                    }
                }
            });
        } else {
            if (mLiveChatRoomLinkMicViewHolder != null) {
                mLiveChatRoomLinkMicViewHolder.playAccStream(uid, "", userStream);
            }
        }
    }

    /**
     * 语音直播间主播混流
     */
    private void liveChatRoomAnchorMix() {
        if (mLiveChatRoomLinkMicViewHolder != null) {
            List<LiveVoiceMixUserBean> userStreamList = mLiveChatRoomLinkMicViewHolder.getUserStreamForMix();
            if (mLiveChatRoomPushViewHolder != null) {
                if (isChatRoomTypeVideo()) {
                    mLiveChatRoomPushViewHolder.voiceRoomAnchorMixVideo(userStreamList);
                } else {
                    mLiveChatRoomPushViewHolder.voiceRoomAnchorMix(userStreamList);
                }
            }
        }
    }

    /**
     * 监听私信未读消息数变化事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImUnReadCountEvent(ImUnReadCountEvent e) {
        ImUnReadCount unReadCount = ImMessageUtil.getInstance().getUnReadMsgCount();
        if (unReadCount != null) {
            String count = unReadCount.getLiveRoomUnReadCount();
            if (!TextUtils.isEmpty(count) && mLiveBottomViewHolder != null) {
                mLiveBottomViewHolder.setUnReadCount(count);
            }
        }
    }

    /**
     * 声网sdk--> 主播与其他主播开始连麦
     */
    public void startAgoraLinkMicAnchor(String pkUid, String pkStream) {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.startAgoraLinkMicAnchor(mStream, pkUid, pkStream);
            JSONObject obj = new JSONObject();
            obj.put("method", "LiveConnect");
            obj.put("pkuid", pkUid);
            mLivePushViewHolder.sendSeiMessage(obj.toJSONString());
        }
    }

    /**
     * 声网sdk--> 主播与其他主播断开连麦
     */
    public void closeAgoraLinkMicAnchor() {
        if (mLivePushViewHolder != null) {
            mLivePushViewHolder.closeAgoraLinkMicAnchor();
            JSONObject obj = new JSONObject();
            obj.put("method", "LiveConnect");
            obj.put("pkuid", 0);
            mLivePushViewHolder.sendSeiMessage(obj.toJSONString());
        }
    }

}
