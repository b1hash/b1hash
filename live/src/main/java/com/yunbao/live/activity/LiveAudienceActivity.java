package com.yunbao.live.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.opensource.svgaplayer.SVGAImageView;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.bean.GoodsBean;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.custom.MyViewPager;
import com.yunbao.common.dialog.FirstChargeDialogFragment;
import com.yunbao.common.dialog.NotLoginDialogFragment;
import com.yunbao.common.event.LoginInvalidEvent;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.PermissionCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.PermissionUtil;
import com.yunbao.common.utils.RandomUtil;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.game.bean.GameParam;
import com.yunbao.game.event.GameWindowChangedEvent;
import com.yunbao.game.event.OpenGameChargeEvent;
import com.yunbao.game.util.GamePresenter;
import com.yunbao.live.R;
import com.yunbao.live.adapter.LiveRoomScrollAdapter;
import com.yunbao.live.bean.LiveAudienceFloatWindowData;
import com.yunbao.live.bean.LiveBean;
import com.yunbao.live.bean.LiveGuardInfo;
import com.yunbao.live.bean.LiveUserGiftBean;
import com.yunbao.live.bean.VoiceRoomAccPullBean;
import com.yunbao.live.dialog.LiveFunctionDialogFragment;
import com.yunbao.live.dialog.LiveGoodsDialogFragment;
import com.yunbao.live.dialog.LiveVoiceFaceFragment;
import com.yunbao.live.event.LinkMicTxAccEvent;
import com.yunbao.live.event.LiveAudienceChatRoomExitEvent;
import com.yunbao.live.event.LiveRoomChangeEvent;
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;
import com.yunbao.live.interfaces.LiveFunctionClickListener;
import com.yunbao.live.interfaces.LivePushListener;
import com.yunbao.live.livegame.luckpan.dialog.LiveGameLuckpanDialog;
import com.yunbao.live.livegame.star.dialog.LiveGameStarDialog;
import com.yunbao.live.presenter.LiveLinkMicAnchorPresenter;
import com.yunbao.live.presenter.LiveLinkMicPkPresenter;
import com.yunbao.live.presenter.LiveLinkMicPresenter;
import com.yunbao.live.presenter.LiveRoomCheckLivePresenter;
import com.yunbao.live.socket.GameActionListenerImpl;
import com.yunbao.live.socket.SocketChatUtil;
import com.yunbao.live.socket.SocketClient;
import com.yunbao.live.socket.SocketVoiceRoomUtil;
import com.yunbao.live.utils.LiveStorge;
import com.yunbao.live.views.AbsLiveChatRoomPlayViewHolder;
import com.yunbao.live.views.LiveAudienceViewHolder;
import com.yunbao.live.views.LiveChatRoomLinkMicAgoraViewHolder;
import com.yunbao.live.views.LiveChatRoomLinkMicTxViewHolder;
import com.yunbao.live.views.LiveChatRoomPlayAgoraViewHolder;
import com.yunbao.live.views.LiveChatRoomPlayTxViewHolder;
import com.yunbao.live.views.LiveEndViewHolder;
import com.yunbao.live.views.LivePlayAgoraViewHolder;
import com.yunbao.live.views.LivePlayTxViewHolder;
import com.yunbao.live.views.LiveRoomPlayViewHolder;
import com.yunbao.live.views.LiveRoomViewHolder;
import com.yunbao.live.views.LiveVoiceAudienceViewHolder;
import com.yunbao.live.views.LiveChatRoomPlayUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by cxf on 2018/10/10.
 */

public class LiveAudienceActivity extends LiveActivity implements LiveFunctionClickListener, View.OnClickListener {

    private static final String TAG = "LiveAudienceActivity";

    public static void forward(Context context, LiveBean liveBean, int liveType, int liveTypeVal, String key, int position, int liveSdk, boolean isChatRoom, int chatRoomType) {
        Intent intent = new Intent(context, LiveAudienceActivity.class);
        intent.putExtra(Constants.LIVE_BEAN, liveBean);
        intent.putExtra(Constants.LIVE_TYPE, liveType);
        intent.putExtra(Constants.LIVE_TYPE_VAL, liveTypeVal);
        intent.putExtra(Constants.LIVE_KEY, key);
        intent.putExtra(Constants.LIVE_POSITION, position);
        intent.putExtra(Constants.LIVE_SDK, liveSdk);
        intent.putExtra(Constants.IS_CHAT_ROOM, isChatRoom);
        intent.putExtra(Constants.CHAT_ROOM_TYPE, chatRoomType);
        ((FragmentActivity) context).startActivityForResult(intent, 0);
    }

    private boolean mUseScroll = true;
    private String mKey;
    private int mPosition;
    private RecyclerView mRecyclerView;
    private LiveRoomScrollAdapter mRoomScrollAdapter;
    private View mMainContentView;
    private MyViewPager mViewPager;
    private View mFirstPage;
    private ViewGroup mSecondPage;//默认显示第二页
    private FrameLayout mContainerWrap;
    private LiveRoomPlayViewHolder mLivePlayViewHolder;
    private LiveAudienceViewHolder mLiveAudienceViewHolder;
    private LiveVoiceAudienceViewHolder mLiveVoiceAudienceViewHolder;
    private boolean mEnd;
    private boolean mCoinNotEnough;//余额不足
    private LiveRoomCheckLivePresenter mCheckLivePresenter;
    private boolean mLighted;
    private AbsLiveChatRoomPlayViewHolder mLiveChatRoomPlayViewHolder;
    private TextView mNameFirst;
    private View mBtnFollowFirst;
    private View mGroupFirst;
    private int mLastViewPagerIndex;
    private View mBtnLandscape;
    private Handler mLandscapeHandler;
    private boolean mLuckPanSwitch;
    private boolean mGameStarEnable;//星球探宝游戏开关 0 关  1 开
    private boolean mGameLuckPanEnable;//幸运大转盘游戏开关  0 关  1  开
    private String mAgoraToken;

    @Override
    protected void getIntentParams() {
        Intent intent = getIntent();
        mIsChatRoom = intent.getBooleanExtra(Constants.IS_CHAT_ROOM, false);
        setChatRoomType(intent.getIntExtra(Constants.CHAT_ROOM_TYPE, Constants.CHAT_ROOM_TYPE_VOICE));
        mLiveSDK = intent.getIntExtra(Constants.LIVE_SDK, Constants.LIVE_SDK_TX);
        mKey = intent.getStringExtra(Constants.LIVE_KEY);
        if (TextUtils.isEmpty(mKey)) {
            mUseScroll = false;
        }
        mPosition = intent.getIntExtra(Constants.LIVE_POSITION, 0);
        mLiveType = intent.getIntExtra(Constants.LIVE_TYPE, Constants.LIVE_TYPE_NORMAL);
        mLiveTypeVal = intent.getIntExtra(Constants.LIVE_TYPE_VAL, 0);
        mLiveBean = intent.getParcelableExtra(Constants.LIVE_BEAN);
    }

    private boolean isUseScroll() {
        return mUseScroll && CommonAppConfig.LIVE_ROOM_SCROLL;
    }

    @Override
    public <T extends View> T findViewById(@IdRes int id) {
        if (isUseScroll()) {
            if (mMainContentView != null) {
                return mMainContentView.findViewById(id);
            }
        }
        return super.findViewById(id);
    }

    @Override
    protected int getLayoutId() {
        if (isUseScroll()) {
            return R.layout.activity_live_audience_2;
        }
        return R.layout.activity_live_audience;
    }

    public void setScrollFrozen(boolean frozen) {
        if (isUseScroll() && mRecyclerView != null) {
            mRecyclerView.setLayoutFrozen(frozen);
        }
    }

    @Override
    protected void main() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if (isUseScroll()) {
            mRecyclerView = super.findViewById(R.id.recyclerView);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
            mMainContentView = LayoutInflater.from(mContext).inflate(R.layout.activity_live_audience, null, false);
        }
        super.main();
        if (isChatRoom()) {
            if (isTxSdK()) {
                LiveChatRoomPlayTxViewHolder liveChatRoomPlayTxVh = new LiveChatRoomPlayTxViewHolder(mContext, (ViewGroup) findViewById(R.id.play_container));
                final LiveChatRoomLinkMicTxViewHolder liveChatRoomLinkMicTxVh = new LiveChatRoomLinkMicTxViewHolder(mContext, liveChatRoomPlayTxVh.getContainer());
                UserBean anchorInfo = new UserBean();
                anchorInfo.setId(mLiveUid);
                anchorInfo.setUserNiceName(mLiveBean.getUserNiceName());
                anchorInfo.setAvatar(mLiveBean.getAvatar());
                liveChatRoomLinkMicTxVh.setChatRoomType(mChatRoomType, anchorInfo);
                liveChatRoomLinkMicTxVh.addToParent();
                liveChatRoomLinkMicTxVh.subscribeActivityLifeCycle();
                if (isChatRoomTypeVideo()) {
                    liveChatRoomPlayTxVh.setPlayViewProvider(new LiveChatRoomPlayTxViewHolder.PlayViewProvider() {
                        @Override
                        public TextureView getPlayView() {
                            return liveChatRoomLinkMicTxVh.getFirstPreview();
                        }
                    });
                }
                mLiveChatRoomPlayViewHolder = liveChatRoomPlayTxVh;
                mLiveChatRoomLinkMicViewHolder = liveChatRoomLinkMicTxVh;
                mLivePlayViewHolder = liveChatRoomPlayTxVh;
            } else {
                LiveChatRoomPlayAgoraViewHolder liveChatRoomPlayAgoraVh = new LiveChatRoomPlayAgoraViewHolder(mContext, (ViewGroup) findViewById(R.id.play_container));
                LiveChatRoomLinkMicAgoraViewHolder liveChatRoomLinkMicAgoraVh = new LiveChatRoomLinkMicAgoraViewHolder(mContext, liveChatRoomPlayAgoraVh.getContainer());
                liveChatRoomPlayAgoraVh.setLiveChatRoomLinkMicAgoraVh(liveChatRoomLinkMicAgoraVh);
                UserBean anchorInfo = new UserBean();
                anchorInfo.setId(mLiveUid);
                anchorInfo.setUserNiceName(mLiveBean.getUserNiceName());
                anchorInfo.setAvatar(mLiveBean.getAvatar());
                liveChatRoomLinkMicAgoraVh.setChatRoomType(mChatRoomType, anchorInfo);
                liveChatRoomLinkMicAgoraVh.addToParent();
                liveChatRoomLinkMicAgoraVh.subscribeActivityLifeCycle();
                mLiveChatRoomPlayViewHolder = liveChatRoomPlayAgoraVh;
                mLiveChatRoomLinkMicViewHolder = liveChatRoomLinkMicAgoraVh;
                mLivePlayViewHolder = liveChatRoomPlayAgoraVh;
            }
        } else {
            if (isTxSdK()) {
                //腾讯视频播放器
                mLivePlayViewHolder = new LivePlayTxViewHolder(mContext, (ViewGroup) findViewById(R.id.play_container));
            } else {
                mLivePlayViewHolder = new LivePlayAgoraViewHolder(mContext, (ViewGroup) findViewById(R.id.play_container));
            }
        }

        mLivePlayViewHolder.addToParent();
        mLivePlayViewHolder.subscribeActivityLifeCycle();
        mViewPager = (MyViewPager) findViewById(R.id.viewPager);
        if (isChatRoom()) {
            mFirstPage = new View(mContext);
            mFirstPage.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            mFirstPage = LayoutInflater.from(mContext).inflate(R.layout.view_audience_page_first, mViewPager, false);
            mNameFirst = mFirstPage.findViewById(R.id.name_first);
            mBtnFollowFirst = mFirstPage.findViewById(R.id.btn_follow_first);
            mBtnFollowFirst.setOnClickListener(this);
            mGroupFirst = mFirstPage.findViewById(R.id.group_first);
            mFirstPage.findViewById(R.id.btn_back_first).setOnClickListener(this);
            mFirstPage.findViewById(R.id.root_first_page).setOnClickListener(this);
        }
        mSecondPage = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.view_audience_page, mViewPager, false);
        mContainerWrap = mSecondPage.findViewById(R.id.container_wrap);
        mContainer = mSecondPage.findViewById(R.id.container);
        mLiveRoomViewHolder = new LiveRoomViewHolder(mContext, mContainer, (GifImageView) mSecondPage.findViewById(R.id.gift_gif), (SVGAImageView) mSecondPage.findViewById(R.id.gift_svga), mContainerWrap);
        mLiveRoomViewHolder.addToParent();
        mLiveRoomViewHolder.subscribeActivityLifeCycle();
        mBtnLandscape = findViewById(R.id.btn_landscape);
        mBtnLandscape.setOnClickListener(this);

        if (!isChatRoom()) {
            mLiveAudienceViewHolder = new LiveAudienceViewHolder(mContext, mContainer);
            mLiveAudienceViewHolder.addToParent();
//            mLiveAudienceViewHolder.setUnReadCount(getImUnReadCount());
            mLiveBottomViewHolder = mLiveAudienceViewHolder;

            mLiveLinkMicPresenter = new LiveLinkMicPresenter(mContext, mLivePlayViewHolder, false, mLiveSDK, mLiveAudienceViewHolder.getContentView());
            mLiveLinkMicAnchorPresenter = new LiveLinkMicAnchorPresenter(mContext, mLivePlayViewHolder, false, mLiveSDK, findViewById(R.id.root_live_audience));
            mLiveLinkMicPkPresenter = new LiveLinkMicPkPresenter(mContext, mLivePlayViewHolder, false, null);
        } else {
            mViewPager.setCanScroll(false);
            mLiveVoiceAudienceViewHolder = new LiveVoiceAudienceViewHolder(mContext, mContainer);
            mLiveVoiceAudienceViewHolder.addToParent();
//            mLiveVoiceAudienceViewHolder.setUnReadCount(getImUnReadCount());
            mLiveBottomViewHolder = mLiveVoiceAudienceViewHolder;
        }
        mViewPager.setAdapter(new PagerAdapter() {

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                if (position == 0) {
                    container.addView(mFirstPage);
                    return mFirstPage;
                } else {
                    container.addView(mSecondPage);
                    return mSecondPage;
                }
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            }
        });
        mViewPager.setCurrentItem(1);

        if (isUseScroll()) {
            List<LiveBean> list = LiveStorge.getInstance().get(mKey);
            mRoomScrollAdapter = new LiveRoomScrollAdapter(mContext, list, mPosition);
            mRoomScrollAdapter.setActionListener(new LiveRoomScrollAdapter.ActionListener() {
                @Override
                public void onPageSelected(LiveBean liveBean, ViewGroup container, boolean first) {
//                    L.e(TAG, "onPageSelected----->" + liveBean);
                    if (mMainContentView != null && container != null) {
                        ViewParent parent = mMainContentView.getParent();
                        if (parent != null) {
                            ViewGroup viewGroup = (ViewGroup) parent;
                            if (viewGroup != container) {
                                viewGroup.removeView(mMainContentView);
                                container.addView(mMainContentView);
                            }
                        } else {
                            container.addView(mMainContentView);
                        }
                    }
                    if (!first) {
                        checkLive(liveBean);
                    }
                }

                @Override
                public void onPageOutWindow(String liveUid) {
//                    L.e(TAG, "onPageOutWindow----->" + liveUid);
                    if (TextUtils.isEmpty(mLiveUid) || mLiveUid.equals(liveUid)) {
                        LiveHttpUtil.cancel(LiveHttpConsts.CHECK_LIVE);
                        LiveHttpUtil.cancel(LiveHttpConsts.ENTER_ROOM);
                        LiveHttpUtil.cancel(LiveHttpConsts.ROOM_CHARGE);
                        clearRoomData();
                    }
                }

                @Override
                public void onPageInWindow(String liveThumb) {
//                    L.e(TAG, "onPageInWindow----->");
                }
            });
            mRecyclerView.setAdapter(mRoomScrollAdapter);
        }
        setLiveRoomData(mLiveBean);
        enterRoom();
    }


    public void scrollNextPosition() {
        if (mRoomScrollAdapter != null) {
            mRoomScrollAdapter.scrollNextPosition();
        }
    }


    private void setLiveRoomData(LiveBean liveBean) {
        mLiveBean = liveBean;
        mLiveUid = liveBean.getUid();
        mStream = liveBean.getStream();
        mLiveRoomViewHolder.setAvatar(liveBean.getAvatar());
        mLiveRoomViewHolder.setAnchorLevel(liveBean.getLevelAnchor());
        mLiveRoomViewHolder.setName(liveBean.getUserNiceName());
        if (mNameFirst != null) {
            mNameFirst.setText(liveBean.getUserNiceName());
        }
        mLiveRoomViewHolder.setRoomNum(liveBean.getLiangNameTip());
        mLiveRoomViewHolder.setTitle(liveBean.getTitle());
        if (!isChatRoom()) {
            mLivePlayViewHolder.setCover(liveBean.getThumb());
            if (mLiveLinkMicPkPresenter != null) {
                mLiveLinkMicPkPresenter.setLiveUid(mLiveUid);
            }
            if (mLiveLinkMicPresenter != null) {
                mLiveLinkMicPresenter.setLiveUid(mLiveUid);
            }
            mLiveAudienceViewHolder.setLiveInfo(mLiveUid, mStream);
            mLiveAudienceViewHolder.setShopOpen(liveBean.getIsshop() == 1);
        }
    }

    private void clearRoomData() {
        if (mSocketClient != null) {
            mSocketClient.disConnect();
        }
        mSocketClient = null;
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.stopPlay();
        }
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.clearData();
        }
        if (mGamePresenter != null) {
            mGamePresenter.clearGame();
        }
        if (mLiveEndViewHolder != null) {
            mLiveEndViewHolder.removeFromParent();
        }
        if (mLiveLinkMicPresenter != null) {
            mLiveLinkMicPresenter.clearData();
        }
        if (mLiveLinkMicAnchorPresenter != null) {
            mLiveLinkMicAnchorPresenter.clearData();
        }
        if (mLiveLinkMicPkPresenter != null) {
            mLiveLinkMicPkPresenter.clearData();
        }
        setPkBgVisible(false);
        mLighted = false;
        if (mLandscapeHandler != null) {
            mLandscapeHandler.removeCallbacksAndMessages(null);
        }
        if (mBtnLandscape != null && mBtnLandscape.getVisibility() == View.VISIBLE) {
            mBtnLandscape.setVisibility(View.INVISIBLE);
        }
    }

    private void checkLive(LiveBean bean) {
        if (mCheckLivePresenter == null) {
            mCheckLivePresenter = new LiveRoomCheckLivePresenter(mContext, new LiveRoomCheckLivePresenter.ActionListener() {
                @Override
                public void onLiveRoomChanged(LiveBean liveBean, int liveType, int liveTypeVal, int liveSdk) {
                    if (liveBean == null) {
                        return;
                    }
                    setLiveRoomData(liveBean);
                    mLiveType = liveType;
                    mLiveTypeVal = liveTypeVal;
                    if (mRoomScrollAdapter != null) {
                        mRoomScrollAdapter.hideCover();
                    }
                    enterRoom();
                }
            });
        }
        mCheckLivePresenter.checkLive(bean);
    }


    private void enterRoom() {
        LiveHttpUtil.enterRoom(mLiveUid, mStream, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    String pull = obj.getString("pull");
                    if (mLiveBean != null) {
                        mLiveBean.setPull(pull);
                    }
                    mDanmuPrice = obj.getString("barrage_fee");
                    mSocketUserType = obj.getIntValue("usertype");
                    mChatLevel = obj.getIntValue("speak_limit");
                    mDanMuLevel = obj.getIntValue("barrage_limit");
                    if (mLiveRoomViewHolder != null) {
                        mLiveRoomViewHolder.setLiveInfo(mLiveUid, mStream, obj.getIntValue("userlist_time") * 1000);
                        mLiveRoomViewHolder.setVotes(obj.getString("votestotal"));
                        showFollow(obj.getIntValue("isattention"));
                        List<LiveUserGiftBean> list = JSON.parseArray(obj.getString("userlists"), LiveUserGiftBean.class);
                        mLiveRoomViewHolder.setUserList(list);
                        mLiveRoomViewHolder.setUserNum(obj.getIntValue("nums"));
                        mLiveRoomViewHolder.startRefreshUserList();
                        if (mLiveType == Constants.LIVE_TYPE_TIME) {//计时收费
                            mLiveRoomViewHolder.startRequestTimeCharge();
                        }
                    }
                    //连接socket
                    mSocketClient = new SocketClient(obj.getString("chatserver"), LiveAudienceActivity.this);
                    mSocketClient.connect(mLiveUid, mStream);
                    //守护相关
                    mLiveGuardInfo = new LiveGuardInfo();
                    int guardNum = obj.getIntValue("guard_nums");
                    mLiveGuardInfo.setGuardNum(guardNum);
                    JSONObject guardObj = obj.getJSONObject("guard");
                    if (guardObj != null) {
                        mLiveGuardInfo.setMyGuardType(guardObj.getIntValue("type"));
                        mLiveGuardInfo.setMyGuardEndTime(guardObj.getString("endtime"));
                    }
                    if (mLiveRoomViewHolder != null) {
                        mLiveRoomViewHolder.setGuardNum(guardNum);
                        //红包相关
                        mLiveRoomViewHolder.setRedPackBtnVisible(obj.getIntValue("isred") == 1);
                    }
                    int dailytask_switch = obj.getIntValue("dailytask_switch");
                    mTaskSwitch = dailytask_switch == 1;
                    //是否显示转盘
                    boolean showPan = obj.getIntValue("turntable_switch") == 1;
                    mLuckPanSwitch = showPan;
                    mGameStarEnable = obj.getIntValue("game_xqtb_switch") == 1;//星球探宝游戏开关 0 关  1 开;
                    mGameLuckPanEnable = obj.getIntValue("game_xydzp_switch") == 1;//幸运大转盘游戏开关  0 关  1  开
                    if (isChatRoom()) {
                        if (mLiveVoiceAudienceViewHolder != null) {
                            mLiveVoiceAudienceViewHolder.setBtnGameVisible(mGameStarEnable || mGameLuckPanEnable);
                        }
                        if (mLiveChatRoomLinkMicViewHolder != null) {
                            mLiveChatRoomLinkMicViewHolder.showUserList(obj.getJSONArray("mic_list"));
                        }
                    } else {
                        if (mLiveAudienceViewHolder != null) {
                            mLiveAudienceViewHolder.setBtnGameVisible(mGameStarEnable || mGameLuckPanEnable);
                        }
                        if (mLiveLinkMicPresenter != null) {
                            mLiveLinkMicPresenter.setSocketClient(mSocketClient);
                        }
                        //判断是否有连麦，要显示连麦窗口
                        String linkMicUid = obj.getString("linkmic_uid");
                        if (!TextUtils.isEmpty(linkMicUid) && !"0".equals(linkMicUid)) {
                            if (mLiveLinkMicPresenter != null) {
                                mLiveLinkMicPresenter.onLinkMicPlay(linkMicUid);
                            }
                        }
                        //判断是否有主播连麦
                        JSONObject pkInfo = JSON.parseObject(obj.getString("pkinfo"));
                        if (pkInfo != null) {
                            String pkUid = pkInfo.getString("pkuid");
                            if (!TextUtils.isEmpty(pkUid) && !"0".equals(pkUid)) {
                                mLivePlayViewHolder.setAnchorLinkMic(true, 0);
                                if (mLiveLinkMicAnchorPresenter != null) {
                                    mLiveLinkMicAnchorPresenter.setPkUid(pkUid);
                                    mLiveLinkMicAnchorPresenter.setPkStream(pkInfo.getString("pkstream"));
                                    mLiveLinkMicAnchorPresenter.showPkUidFollow(pkUid);
                                }
                                setPkBgVisible(true);
                            } else {
                                if (mLiveLinkMicAnchorPresenter != null) {
                                    mLiveLinkMicAnchorPresenter.hidePkUidFollow();
                                }
                            }
                            if (pkInfo.getIntValue("ifpk") == 1 && mLiveLinkMicPkPresenter != null) {//pk开始了
                                mLiveLinkMicPkPresenter.onEnterRoomPkStart(pkUid, pkInfo.getLongValue("pk_gift_liveuid"), pkInfo.getLongValue("pk_gift_pkuid"), pkInfo.getIntValue("pk_time"));
                            }
                        }

                        //奖池等级
                        int giftPrizePoolLevel = obj.getIntValue("jackpot_level");

                        if (mLiveRoomViewHolder != null) {
                            mLiveRoomViewHolder.showBtn(showPan, giftPrizePoolLevel, dailytask_switch);
                        }

                        //直播间商品
                        JSONObject showGoodsInfo = obj.getJSONObject("show_goods");
                        String goodsId = showGoodsInfo.getString("goodsid");
                        if (!"0".equals(goodsId)) {
                            GoodsBean goodsBean = new GoodsBean();
                            goodsBean.setId(goodsId);
                            goodsBean.setThumb(showGoodsInfo.getString("goods_thumb"));
                            goodsBean.setName(showGoodsInfo.getString("goods_name"));
                            goodsBean.setPriceNow(showGoodsInfo.getString("goods_price"));
                            goodsBean.setType(showGoodsInfo.getIntValue("goods_type"));
                            if (mLiveRoomViewHolder != null) {
                                mLiveRoomViewHolder.setShowGoodsBean(goodsBean);
                            }
                        }

                        //游戏相关
                        if (CommonAppConfig.GAME_ENABLE && mLiveRoomViewHolder != null) {
                            GameParam param = new GameParam();
                            param.setContext(mContext);
                            param.setParentView(mContainerWrap);
                            param.setTopView(mContainer);
                            param.setInnerContainer(mLiveRoomViewHolder.getInnerContainer());
                            param.setGameActionListener(new GameActionListenerImpl(LiveAudienceActivity.this, mSocketClient));
                            param.setLiveUid(mLiveUid);
                            param.setStream(mStream);
                            param.setAnchor(false);
                            param.setCoinName(CommonAppConfig.getInstance().getScoreName());
                            param.setObj(obj);
                            if (mGamePresenter == null) {
                                mGamePresenter = new GamePresenter();
                            }
                            mGamePresenter.setGameParam(param);
                        }
                    }
                    if (mLivePlayViewHolder != null) {
                        if (isTxSdK()) {
                            mLivePlayViewHolder.play(pull);
                        } else {
                            mAgoraToken = obj.getString("user_sw_token");
                            mLivePlayViewHolder.playAgora(pull,
                                    obj.getIntValue("isvideo") == 1, mAgoraToken, mStream, Integer.parseInt(mLiveUid));
                        }
                    }
                }
            }
        });
    }

    /**
     * 结束观看
     */
    private void endPlay() {
        if (mEnd) {
            return;
        }
        mEnd = true;
        //断开socket
        if (mSocketClient != null) {
            mSocketClient.disConnect();
        }
        mSocketClient = null;
        //结束播放
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.release();
        }
        mLivePlayViewHolder = null;
        release();
    }

    @Override
    protected void release() {
        if (mSocketClient != null) {
            mSocketClient.disConnect();
        }
        LiveHttpUtil.cancel(LiveHttpConsts.CHECK_LIVE);
        LiveHttpUtil.cancel(LiveHttpConsts.ENTER_ROOM);
        LiveHttpUtil.cancel(LiveHttpConsts.ROOM_CHARGE);
        CommonHttpUtil.cancel(CommonHttpConsts.GET_BALANCE);
        super.release();
        if (mRoomScrollAdapter != null) {
            mRoomScrollAdapter.release();
        }
        mRoomScrollAdapter = null;
        if (mLandscapeHandler != null) {
            mLandscapeHandler.removeCallbacksAndMessages(null);
        }
        mLandscapeHandler = null;
    }

    /**
     * 观众收到直播结束消息
     */
    @Override
    public void onLiveEnd() {
        super.onLiveEnd();
        endPlay();
        if (isLandscape()) {
            setPortrait();
        }
        if (mBtnLandscape != null && mBtnLandscape.getVisibility() == View.VISIBLE) {
            mBtnLandscape.setVisibility(View.INVISIBLE);
        }
        if (mViewPager != null) {
            if (mViewPager.getCurrentItem() != 1) {
                mViewPager.setCurrentItem(1, false);
            }
            mViewPager.setCanScroll(false);
        }
        if (mLiveEndViewHolder == null) {
            mLiveEndViewHolder = new LiveEndViewHolder(mContext, mSecondPage);
            mLiveEndViewHolder.subscribeActivityLifeCycle();
            mLiveEndViewHolder.addToParent();
        }
        mLiveEndViewHolder.showData(mLiveBean, mStream);
        setScrollFrozen(true);

    }


    /**
     * 观众收到踢人消息
     */
    @Override
    public void onKick(String touid) {
        if (!TextUtils.isEmpty(touid) && touid.equals(CommonAppConfig.getInstance().getUid())) {//被踢的是自己
            exitLiveRoom();
            ToastUtil.show(WordUtil.getString(R.string.live_kicked_2));
        }
    }

    /**
     * 观众收到禁言消息
     */
    @Override
    public void onShutUp(String touid, String content) {
        if (!TextUtils.isEmpty(touid) && touid.equals(CommonAppConfig.getInstance().getUid())) {
            DialogUitl.showSimpleTipDialog(mContext, content);
        }
    }

    @Override
    public void onBackPressed() {
        if (isLandscape()) {
            setPortrait();
            return;
        }
        if (!mEnd && !canBackPressed()) {
            return;
        }
        if (isChatRoom() && !mEnd) {
            Integer[][] arr = null;
            if (isChatRoomTypeVideo() || isSelfChatRoomUpMic()) {
                arr = new Integer[][]{
                        {R.string.a_057, ContextCompat.getColor(mContext, R.color.red)}};
            } else {
                arr = new Integer[][]{
                        {R.string.a_058, ContextCompat.getColor(mContext, R.color.textColor)},
                        {R.string.a_057, ContextCompat.getColor(mContext, R.color.red)}};
            }
            DialogUitl.showStringArrayDialog(mContext, arr, new DialogUitl.StringArrayDialogCallback() {
                @Override
                public void onItemClick(String text, int tag) {
                    if (tag == R.string.a_058) {
                        if (mEnd) {
                            LiveChatRoomPlayUtil.getInstance().setKeepAlive(false);
                            exitLiveRoom();
                        } else {
                            LiveChatRoomPlayUtil.getInstance().setKeepAlive(true);
                            exitLiveRoom();
                            EventBus.getDefault().post(new LiveAudienceChatRoomExitEvent(mLiveBean, null));
                        }
                    } else if (tag == R.string.a_057) {
                        LiveChatRoomPlayUtil.getInstance().setKeepAlive(false);
                        exitLiveRoom();
                    }
                }
            });
        } else {
            LiveAudienceFloatWindowData floatWindowData = getFloatWindowData();
            exitLiveRoom();
            if (CommonAppConfig.getInstance().isShowLiveFloatWindow() && mLiveType != Constants.LIVE_TYPE_TIME) {
                EventBus.getDefault().post(new LiveAudienceChatRoomExitEvent(mLiveBean, floatWindowData));
            }
        }
    }

    private LiveAudienceFloatWindowData getFloatWindowData() {
        LiveAudienceFloatWindowData floatWindowData = new LiveAudienceFloatWindowData();
        floatWindowData.setTxSDK(isTxSdK());
        floatWindowData.setAgoraToken(mAgoraToken);
        floatWindowData.setLiveUid(Integer.parseInt(mLiveUid));
        floatWindowData.setStream(mStream);
        floatWindowData.setLinkMicAudienceUid(getLinkMicUid());
        floatWindowData.setPkUid(getLinkMicAnchorUid());
        return floatWindowData;
    }

    /**
     * 退出直播间
     */
    public void exitLiveRoom() {
        endPlay();
        finish();
    }


    @Override
    protected void onDestroy() {
        if (mLiveAudienceViewHolder != null) {
            mLiveAudienceViewHolder.clearAnim();
        }
        super.onDestroy();
        L.e("LiveAudienceActivity-------onDestroy------->");
    }

    /**
     * 点亮
     */
    public void light() {
        if (!mLighted) {
            mLighted = true;
            int guardType = mLiveGuardInfo != null ? mLiveGuardInfo.getMyGuardType() : Constants.GUARD_TYPE_NONE;
            SocketChatUtil.sendLightMessage(mSocketClient, 1 + RandomUtil.nextInt(6), guardType);
        }
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.playLightAnim();
        }
    }


    /**
     * 计时收费更新主播映票数
     */
    public void roomChargeUpdateVotes() {
        sendUpdateVotesMessage(mLiveTypeVal);
    }

    /**
     * 暂停播放
     */
    public void pausePlay() {
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.pausePlay();
        }
    }

    /**
     * 恢复播放
     */
    public void resumePlay() {
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.resumePlay();
        }
    }

    /**
     * 充值成功
     */
    public void onChargeSuccess() {
        if (mLiveType == Constants.LIVE_TYPE_TIME) {
            if (mCoinNotEnough) {
                mCoinNotEnough = false;
                LiveHttpUtil.roomCharge(mLiveUid, mStream, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0) {
                            roomChargeUpdateVotes();
                            if (mLiveRoomViewHolder != null) {
                                resumePlay();
                                mLiveRoomViewHolder.startRequestTimeCharge();
                            }
                        } else {
                            if (code == 1008) {//余额不足
                                mCoinNotEnough = true;
                                DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.live_coin_not_enough), false,
                                        new DialogUitl.SimpleCallback2() {
                                            @Override
                                            public void onConfirmClick(Dialog dialog, String content) {
                                                RouteUtil.forwardMyCoin(mContext);
                                            }

                                            @Override
                                            public void onCancelClick() {
                                                exitLiveRoom();
                                            }
                                        });
                            }
                        }
                    }
                });
            }
        }
    }

    public void setCoinNotEnough(boolean coinNotEnough) {
        mCoinNotEnough = coinNotEnough;
    }

    /**
     * 游戏窗口变化事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGameWindowChangedEvent(GameWindowChangedEvent e) {
        if (mLiveRoomViewHolder != null) {
            mLiveRoomViewHolder.setOffsetY(e.getGameViewHeight());
        }
    }

    /**
     * 游戏充值页面
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOpenGameChargeEvent(OpenGameChargeEvent e) {
        openChargeWindow();
    }

    /**
     * 腾讯sdk连麦时候切换低延时流
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLinkMicTxAccEvent(LinkMicTxAccEvent e) {
        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayTxViewHolder) {
            ((LivePlayTxViewHolder) mLivePlayViewHolder).onLinkMicTxAccEvent(e.isLinkMic());
        }
    }

    /**
     * 腾讯sdk时候主播连麦回调
     *
     * @param linkMic true开始连麦 false断开连麦
     */
    public void onLinkMicTxAnchor(boolean linkMic) {
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.setAnchorLinkMic(linkMic, 2000);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveRoomChangeEvent(LiveRoomChangeEvent e) {
        LiveBean liveBean = e.getLiveBean();
        if (liveBean != null) {
            String liveUid = liveBean.getUid();
            if (!TextUtils.isEmpty(liveUid) && !liveUid.equals(mLiveUid)) {
                LiveHttpUtil.cancel(LiveHttpConsts.CHECK_LIVE);
                LiveHttpUtil.cancel(LiveHttpConsts.ENTER_ROOM);
                LiveHttpUtil.cancel(LiveHttpConsts.ROOM_CHARGE);
                clearRoomData();

                setLiveRoomData(liveBean);
                mLiveType = e.getLiveType();
                mLiveTypeVal = e.getLiveTypeVal();
                enterRoom();
            }
        }
    }

    /**
     * 打开商品窗口
     */
    public void openGoodsWindow() {
        SocketChatUtil.liveGoodsFloat(mSocketClient);
        LiveGoodsDialogFragment fragment = new LiveGoodsDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_UID, mLiveUid);
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "LiveGoodsDialogFragment");
    }

    /**
     * 打开首冲礼包窗口
     */
    public void openFirstCharge() {
        FirstChargeDialogFragment fragment = new FirstChargeDialogFragment();
        fragment.show(getSupportFragmentManager(), "FirstChargeDialogFragment");
    }

    public void liveGoodsFloat() {
        SocketChatUtil.liveGoodsFloat(mSocketClient);
    }


    /**
     * 打开功能弹窗
     */
    public void showFunctionDialogVoice(boolean hasFace) {
        LiveFunctionDialogFragment fragment = new LiveFunctionDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.HAS_GAME, false);
        bundle.putBoolean(Constants.OPEN_FLASH, false);
        bundle.putBoolean("TASK", mTaskSwitch);
        bundle.putBoolean("LUCK_PAN", mLuckPanSwitch);
        bundle.putBoolean("HAS_FACE", hasFace);
        bundle.putBoolean("HAS_MSG", true);
        boolean isLinkMic = false;
        if (mLiveLinkMicPresenter != null) {
            isLinkMic = mLiveLinkMicPresenter.isLinkMic();
        }
        bundle.putBoolean(Constants.LINK_MIC, isLinkMic);
        fragment.setArguments(bundle);
        fragment.setFunctionClickListener(this);
        fragment.show(getSupportFragmentManager(), "LiveFunctionDialogFragment");
    }

    /**
     * 打开功能弹窗
     */
    public void showFunctionDialog() {
        LiveFunctionDialogFragment fragment = new LiveFunctionDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("HAS_MSG", true);
        boolean isLinkMic = false;
        if (mLiveLinkMicPresenter != null) {
            isLinkMic = mLiveLinkMicPresenter.isLinkMic();
        }
        bundle.putBoolean(Constants.LINK_MIC, isLinkMic);
        fragment.setArguments(bundle);
        fragment.setFunctionClickListener(this);
        fragment.show(getSupportFragmentManager(), "LiveFunctionDialogFragment");
    }

    @Override
    public void onClick(int functionID) {
        switch (functionID) {
            case Constants.LIVE_FUNC_RED_PACK://红包
                openRedPackSendWindow();
                break;
            case Constants.LIVE_FUNC_TASK://每日任务
                openDailyTaskWindow();
                break;
            case Constants.LIVE_FUNC_LUCK://幸运奖池
                openPrizePoolWindow();
                break;
            case Constants.LIVE_FUNC_PAN://幸运转盘
                openLuckPanWindow();
                break;
            case Constants.LIVE_FUNC_SHARE://分享
                openShareWindow();
                break;
            case Constants.LIVE_FUNC_MSG://私信
                openChatListWindow();
                break;
            case Constants.LIVE_FUNC_FACE://表情
                openVoiceRoomFace();
                break;
            case Constants.LIVE_FUNC_LINK_MIC_AUD:
                if (mLiveLinkMicPresenter != null) {
                    mLiveLinkMicPresenter.onLinkMicBtnClick();
                }
                break;
        }
    }


    /**
     * 语音聊天室表情
     */
    public void openVoiceRoomFace() {
        LiveVoiceFaceFragment fragment = new LiveVoiceFaceFragment();
        fragment.show(getSupportFragmentManager(), "LiveVoiceFaceFragment");
    }

    /**
     * 点击上麦下麦按钮
     */
    public void clickVoiceUpMic() {
        if (isSelfChatRoomUpMic()) {
            LiveHttpUtil.userDownVoiceMic(mStream, new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0) {
                        SocketVoiceRoomUtil.userDownMic(mSocketClient, CommonAppConfig.getInstance().getUid(), 0);
                    }
                    ToastUtil.show(msg);
                }
            });
        } else {
            PermissionUtil.request(this, new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            voiceApplyUpMic();
                        }
                    },
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA);
        }
    }


    /**
     * 语音聊天室--用户发起上麦申请
     */
    public void applyMicUp() {
        SocketVoiceRoomUtil.applyMicUp(mSocketClient);
    }


    /**
     * 语音聊天室--观众收到主播同意或拒绝上麦的消息
     *
     * @param toUid    上麦的人的uid
     * @param toName   上麦的人的name
     * @param toAvatar 上麦的人的头像
     * @param position 上麦的人的麦位，从0开始 -1表示拒绝上麦
     */
    @Override
    public void onVoiceRoomHandleApply(String toUid, String toName, String toAvatar, int position) {
        super.onVoiceRoomHandleApply(toUid, toName, toAvatar, position);
        if (!TextUtils.isEmpty(toUid) && toUid.equals(CommonAppConfig.getInstance().getUid())) {////上麦的是自己
            boolean isUpMic = position >= 0;
            if (mLiveVoiceAudienceViewHolder != null) {
                mLiveVoiceAudienceViewHolder.changeMicUp(isUpMic);
            }
            ToastUtil.show(isUpMic ? R.string.a_046 : R.string.a_047);
            if (isUpMic) {
                if (isTxSdK()) {
                    //获取自己的推拉流地址开始推流
                    LiveHttpUtil.getVoiceMicStream(mStream, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0 && info.length > 0) {
                                JSONObject obj = JSON.parseObject(info[0]);
                                String push = obj.getString("push");
//                            final String pull = obj.getString("pull");
                                final String userStream = obj.getString("user_stream");
                                //L.e("语音聊天室----push----> " + push);
                                //L.e("语音聊天室----pull---> " + pull);
                                if (mLiveChatRoomLinkMicViewHolder != null) {
                                    mLiveChatRoomLinkMicViewHolder.startPush(push, new LivePushListener() {
                                        @Override
                                        public void onPreviewStart() {

                                        }

                                        @Override
                                        public void onPushStart() {
                                            SocketVoiceRoomUtil.userPushSuccess(mSocketClient, "", userStream);
                                        }

                                        @Override
                                        public void onPushFailed() {

                                        }
                                    });
                                }
                            }
                        }
                    });

                    //获取主播和麦上其他用户的低延时流地址，开始播流
                    LiveHttpUtil.getVoiceLivePullStreams(mStream, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0) {
                                List<VoiceRoomAccPullBean> list = JSON.parseArray(Arrays.toString(info), VoiceRoomAccPullBean.class);
                                for (VoiceRoomAccPullBean bean : list) {
                                    if (bean.getIsAnchor() == 1) {//主播
                                        if (mLiveChatRoomPlayViewHolder != null) {
                                            mLiveChatRoomPlayViewHolder.changeAccStream(bean.getPull());
                                        }
                                    } else {
                                        if (mLiveChatRoomLinkMicViewHolder != null) {
                                            mLiveChatRoomLinkMicViewHolder.playAccStream(bean.getUid(), bean.getPull(), null);
                                        }
                                    }
                                }
                            }
                        }
                    });
                } else {
                    if (mLiveChatRoomLinkMicViewHolder != null) {
                        mLiveChatRoomLinkMicViewHolder.startPush("", new LivePushListener() {
                            @Override
                            public void onPreviewStart() {

                            }

                            @Override
                            public void onPushStart() {
                                SocketVoiceRoomUtil.userPushSuccess(mSocketClient, "", "");
                            }

                            @Override
                            public void onPushFailed() {

                            }
                        });
                    }
                }
            }
        }

    }

    /**
     * 语音聊天室--所有人收到某人下麦的消息
     *
     * @param uid 下麦的人的uid
     */
    @Override
    public void onVoiceRoomDownMic(String uid, int type) {
        if (!TextUtils.isEmpty(uid) && uid.equals(CommonAppConfig.getInstance().getUid())) {//被下麦的是自己
            if (mLiveVoiceAudienceViewHolder != null) {
                mLiveVoiceAudienceViewHolder.changeMicUp(false);
            }
            if (mLiveChatRoomLinkMicViewHolder != null) {
                mLiveChatRoomLinkMicViewHolder.stopPush();//停止推流
                if (isTxSdK()) {
                    mLiveChatRoomLinkMicViewHolder.stopAllPlay();//停止所有播放
                }
                mLiveChatRoomLinkMicViewHolder.onUserDownMic(uid);
            }
            if (mLiveChatRoomPlayViewHolder != null) {
                if (isTxSdK()) {
                    mLiveChatRoomPlayViewHolder.changeAccStream(null);//切回到普通流播放
                }
            }
            if (type == 1 || type == 2) {//1被主播下麦  2被管理员下麦
                ToastUtil.show(R.string.a_054);
            }
        } else {
            if (mLiveChatRoomLinkMicViewHolder != null) {
                int position = mLiveChatRoomLinkMicViewHolder.getUserPosition(uid);
                if (position != -1) {
                    if (isTxSdK()) {
                        mLiveChatRoomLinkMicViewHolder.stopPlay(position);//停止播放被下麦的人的流
                    } else {
                        mLiveChatRoomLinkMicViewHolder.stopPlay(uid);//停止播放被下麦的人的流
                    }
                    mLiveChatRoomLinkMicViewHolder.onUserDownMic(position);
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
        if (!TextUtils.isEmpty(uid) && uid.equals(CommonAppConfig.getInstance().getUid())) {//被操作人是自己
            if (status == Constants.VOICE_CTRL_OPEN) {
                ToastUtil.show(R.string.a_056);
                if (mLiveVoiceAudienceViewHolder != null) {
                    mLiveVoiceAudienceViewHolder.setVoiceMicClose(false);
                }
                if (mLiveChatRoomLinkMicViewHolder != null) {
                    mLiveChatRoomLinkMicViewHolder.setPushMute(false);
                }
            } else if (status == Constants.VOICE_CTRL_CLOSE) {
                ToastUtil.show(R.string.a_055);
                if (mLiveVoiceAudienceViewHolder != null) {
                    mLiveVoiceAudienceViewHolder.setVoiceMicClose(true);
                }
                if (mLiveChatRoomLinkMicViewHolder != null) {
                    mLiveChatRoomLinkMicViewHolder.setPushMute(true);
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
        if (!TextUtils.isEmpty(uid) && !uid.equals(CommonAppConfig.getInstance().getUid())) {
            if (isTxSdK()) {
                if (isSelfChatRoomUpMic()) {
                    LiveHttpUtil.getNewUpMicPullUrl(userStream, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0 && info.length > 0) {
                                String playUrl = JSON.parseObject(info[0]).getString("play_url");
                                if (mLiveChatRoomLinkMicViewHolder != null) {
                                    mLiveChatRoomLinkMicViewHolder.playAccStream(uid, playUrl, userStream);
                                }
                            }
                        }
                    });
                }
            } else {
                if (mLiveChatRoomLinkMicViewHolder != null) {
                    mLiveChatRoomLinkMicViewHolder.playAccStream(uid, pull, userStream);
                }
            }
        }
    }


    /**
     * 语音聊天室--发送表情
     */
    public void voiceRoomSendFace(int index) {
        SocketVoiceRoomUtil.voiceRoomSendFace(mSocketClient, index);
    }

    /**
     * 聊天室 判断自己是否上麦了
     */
    private boolean isSelfChatRoomUpMic() {
        if (mLiveChatRoomLinkMicViewHolder != null) {
            return mLiveChatRoomLinkMicViewHolder.getUserPosition(CommonAppConfig.getInstance().getUid()) >= 0;
        }
        return false;
    }


    /**
     * 未登录的弹窗
     */
    @Override
    public void showNotLoginDialog() {
        NotLoginDialogFragment fragment = new NotLoginDialogFragment();
        fragment.setActionListener(new NotLoginDialogFragment.ActionListener() {
            @Override
            public void beforeForwardLogin() {
                exitLiveRoom();
            }
        });
        fragment.show(getSupportFragmentManager(), "NotLoginDialogFragment");
    }


    /**
     * 设为横屏
     */
    public void setLandscape() {
        if (mViewPager != null) {
            mLastViewPagerIndex = mViewPager.getCurrentItem();
            if (mLastViewPagerIndex != 0) {
                mViewPager.setCurrentItem(0, false);
            }
            mViewPager.setCanScroll(false);
        }
        setScrollFrozen(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    }

    /**
     * 设为竖屏
     */
    public void setPortrait() {
        if (mViewPager != null) {
            if (mLastViewPagerIndex != 0) {
                mViewPager.setCurrentItem(mLastViewPagerIndex, false);
            }
            mViewPager.setCanScroll(true);
        }
        setScrollFrozen(false);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * 是否横屏
     */
    public boolean isLandscape() {
        return getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean landscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (landscape) {
            //L.e("onConfigurationChanged-------->横屏");
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
            if (mLandscapeHandler == null) {
                mLandscapeHandler = new Handler();
            } else {
                mLandscapeHandler.removeCallbacksAndMessages(null);
            }
            if (mGroupFirst != null && mGroupFirst.getVisibility() != View.VISIBLE) {
                mGroupFirst.setVisibility(View.VISIBLE);
            }
            mLandscapeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mGroupFirst != null && mGroupFirst.getVisibility() == View.VISIBLE) {
                        mGroupFirst.setVisibility(View.INVISIBLE);
                    }
                }
            }, 5000);
        } else {
            //L.e("onConfigurationChanged-------->竖屏");
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            if (mLandscapeHandler != null) {
                mLandscapeHandler.removeCallbacksAndMessages(null);
            }
            if (mGroupFirst != null && mGroupFirst.getVisibility() == View.VISIBLE) {
                mGroupFirst.setVisibility(View.INVISIBLE);
            }
        }
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.changeSize(landscape);
        }
    }

    public void showFollow(int isAttention) {
        super.showFollow(isAttention);
        if (mBtnFollowFirst != null) {
            if (isAttention == 0) {
                if (mBtnFollowFirst.getVisibility() != View.VISIBLE) {
                    mBtnFollowFirst.setVisibility(View.VISIBLE);
                }
            } else {
                if (mBtnFollowFirst.getVisibility() == View.VISIBLE) {
                    mBtnFollowFirst.setVisibility(View.GONE);
                }
            }
        }
    }


    public void onVideoHeightChanged(int videoHeight, int parentHeight) {
        if (mEnd) {
            return;
        }
        if (mLiveBean != null && mLiveBean.getAnyway() == 0) {
            return;
        }
        if (videoHeight > 0) {
            if (mBtnLandscape != null) {
                if (mBtnLandscape.getVisibility() != View.VISIBLE) {
                    mBtnLandscape.setVisibility(View.VISIBLE);
                }
                int y = (parentHeight - videoHeight) / 2 + videoHeight - DpUtil.dp2px(35);
                mBtnLandscape.setY(y);
            }
        } else {
            if (mBtnLandscape != null && mBtnLandscape.getVisibility() == View.VISIBLE) {
                mBtnLandscape.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        int i = v.getId();
        if (i == R.id.btn_landscape) {
            if (checkLogin()) {
                setLandscape();
            }
        } else if (i == R.id.btn_back_first) {
            setPortrait();
        } else if (i == R.id.btn_follow_first) {
            if (mLiveRoomViewHolder != null) {
                mLiveRoomViewHolder.follow();
            }
        } else if (i == R.id.root_first_page) {
            clickFirstPage();
        }
    }

    private void clickFirstPage() {
        if (mGroupFirst != null && mGroupFirst.getVisibility() != View.VISIBLE) {
            if (mLandscapeHandler == null) {
                mLandscapeHandler = new Handler();
            } else {
                mLandscapeHandler.removeCallbacksAndMessages(null);
            }
            mGroupFirst.setVisibility(View.VISIBLE);
            mLandscapeHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mGroupFirst != null && mGroupFirst.getVisibility() == View.VISIBLE) {
                        mGroupFirst.setVisibility(View.INVISIBLE);
                    }
                }
            }, 5000);
        }
    }

    public void openGame(View btnGame) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.view_live_choose_game, null);
        View btnGameStar = v.findViewById(R.id.btn_live_game_star);
        View btnGameLuckpan = v.findViewById(R.id.btn_live_game_luckpan);
        if (!mGameStarEnable) {
            btnGameStar.setVisibility(View.GONE);
        }
        if (!mGameLuckPanEnable) {
            btnGameLuckpan.setVisibility(View.GONE);
        }
        v.measure(0, 0);
        int vw = v.getMeasuredWidth();
        int vh = v.getMeasuredHeight();
        final PopupWindow popupWindow = new PopupWindow(v, vw, vh, true);
        popupWindow.setAnimationStyle(R.style.animCenter);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setOutsideTouchable(true);
        int[] arr = new int[2];
        btnGame.getLocationInWindow(arr);
        int offsetX = arr[0] + btnGame.getWidth() / 2 - vw / 2;
        int offsetY = arr[1] - vh;
        popupWindow.showAtLocation(btnGame, Gravity.LEFT | Gravity.TOP, offsetX, offsetY);
        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.btn_live_game_star) {
                    popupWindow.dismiss();
                    openStarGame();
                } else if (i == R.id.btn_live_game_luckpan) {
                    popupWindow.dismiss();
                    openLuckPanGame();
                }
            }
        };
        btnGameStar.setOnClickListener(onClick);
        btnGameLuckpan.setOnClickListener(onClick);
    }


    /**
     * 星球探宝
     */
    protected void openStarGame() {
        LiveGameStarDialog dialog = new LiveGameStarDialog();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_UID, mLiveUid);
        bundle.putString(Constants.STREAM, mStream);
        dialog.setArguments(bundle);
        dialog.setActionListener(new LiveGameStarDialog.ActionListener() {
            @Override
            public void onClose() {
                mLiveGameStarDialog = null;
            }

            @Override
            public void sendWinSocket(String json) {
                //发送星球探宝中奖消息
                SocketChatUtil.gameXqtbWin(mSocketClient, json);
            }

            @Override
            public void onPackClick() {
                openGiftWindow(true);
            }

            @Override
            public void onChargeClick() {
                openChargeWindow();
            }
        });
        dialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "LiveGameStarDialog");
        mLiveGameStarDialog = dialog;
    }

    /**
     * 幸运转盘
     */
    protected void openLuckPanGame() {
        LiveGameLuckpanDialog dialog = new LiveGameLuckpanDialog();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.LIVE_UID, mLiveUid);
        bundle.putString(Constants.STREAM, mStream);
        dialog.setArguments(bundle);
        dialog.setActionListener(new LiveGameLuckpanDialog.ActionListener() {
            @Override
            public void onClose() {
                mLiveGameLuckpanDialog = null;
            }

            @Override
            public void sendWinSocket(String json) {
                //发送幸运大转盘中奖消息
                SocketChatUtil.gameLuckpanWin(mSocketClient, json);
            }

            @Override
            public void onPackClick() {
                openGiftWindow(true);
            }

            @Override
            public void onChargeClick() {
                openChargeWindow();
            }
        });
        dialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "LiveGameLuckPanDialog");
        mLiveGameLuckpanDialog = dialog;
    }

    /**
     * 声网sdk--> 开始/关闭 观众连麦推流
     *
     * @param isPush true开始推流 ，false结束推流
     */
    public void toggleLinkMicPushAgora(boolean isPush) {
        if (mLivePlayViewHolder != null) {
            mLivePlayViewHolder.toggleLinkMicPushAgora(isPush);
        }
    }

    /**
     * 声网sdk--> 主播与其他主播开始连麦
     */
    public void startAgoraLinkMicAnchor(String pkUid) {
        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).onAnchorLinkMicStart(pkUid);
        }
    }

    /**
     * 声网sdk--> 主播与其他主播断开连麦
     */
    public void closeAgoraLinkMicAnchor() {
        if (mLivePlayViewHolder != null && mLivePlayViewHolder instanceof LivePlayAgoraViewHolder) {
            ((LivePlayAgoraViewHolder) mLivePlayViewHolder).onAnchorLinkMicClose();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginInvalidEvent(LoginInvalidEvent e) {
        hideDialogs();
        exitLiveRoom();
    }
}
