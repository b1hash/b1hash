package com.yunbao.main.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fm.openinstall.OpenInstall;
import com.fm.openinstall.listener.AppInstallAdapter;
import com.fm.openinstall.model.AppData;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.Constants;
import com.yunbao.common.adapter.ViewPagerAdapter;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.custom.TabButtonGroup;
import com.yunbao.common.dialog.NotCancelableInputDialog;
import com.yunbao.common.event.CloseFloatWindowEvent;
import com.yunbao.common.event.LoginChangeEvent;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.PermissionCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.FloatWindowHelper;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.LocationUtil;
import com.yunbao.common.utils.PermissionUtil;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.VersionUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.im.activity.ChatActivity;
import com.yunbao.im.event.ImUnReadCountEvent;
import com.yunbao.im.event.NotificiationClickEvent;
import com.yunbao.im.utils.ImMessageUtil;
import com.yunbao.im.utils.ImUnReadCount;
import com.yunbao.im.utils.NotificationUtil;
import com.yunbao.live.activity.LiveAnchorActivity;
import com.yunbao.live.bean.LiveBean;
import com.yunbao.live.bean.LiveConfigBean;
import com.yunbao.live.event.LiveAudienceChatRoomExitEvent;
import com.yunbao.live.event.LiveFloatWindowEvent;
import com.yunbao.live.floatwindow.FloatWindowUtil;
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;
import com.yunbao.live.utils.LiveStorge;
import com.yunbao.live.views.LiveChatRoomPlayUtil;
import com.yunbao.main.R;
import com.yunbao.main.bean.BonusBean;
import com.yunbao.main.dialog.FullFunctionDialogFragment;
import com.yunbao.main.dialog.MainStartDialogFragment;
import com.yunbao.main.dialog.TeenagerDialogFragment;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;
import com.yunbao.main.interfaces.MainAppBarLayoutListener;
import com.yunbao.main.interfaces.MainStartChooseCallback;
import com.yunbao.main.presenter.CheckLivePresenter;
import com.yunbao.main.views.AbsMainViewHolder;
import com.yunbao.main.views.BonusViewHolder;
import com.yunbao.main.views.MainActiveViewHolder;
import com.yunbao.main.views.MainHomeShortPlayViewHolder;
import com.yunbao.main.views.MainHomeViewHolder;
import com.yunbao.main.views.MainMeViewHolder;
import com.yunbao.video.activity.AbsVideoPlayActivity;
import com.yunbao.video.activity.VideoRecordActivity;
import com.yunbao.video.utils.VideoStorge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AbsVideoPlayActivity implements MainAppBarLayoutListener {

    private ViewGroup mRootView;
    private TabButtonGroup mTabButtonGroup;
    private ViewPager mViewPager;
    private List<FrameLayout> mViewList;
    private MainHomeViewHolder mHomeViewHolder;
    private MainActiveViewHolder mActiveViewHolder;
//    private MainMallViewHolder mMallViewHolder;
    private MainHomeShortPlayViewHolder mShortPlayViewHolder;
    private MainMeViewHolder mMeViewHolder;
    private AbsMainViewHolder[] mViewHolders;
    private View mBottom;
    private int mDp70;
    private ObjectAnimator mUpAnimator;//向上动画
    private ObjectAnimator mDownAnimator;//向下动画
    private boolean mAnimating;
    private boolean mShowed = true;
    private boolean mHided;

    private CheckLivePresenter mCheckLivePresenter;
    private boolean mFristLoad;
    private long mLastClickBackTime;//上次点击back键的时间
    private AudioManager mAudioManager;
    private boolean mFirstLogin;//是否是第一次登录
    private Handler mHandler;
    private boolean mLoginChanged;
    private Runnable mTeengerTicker;
    private HttpCallback mTeenagerTimeCallback;
    private boolean mIsTeengerTick;
    private BonusViewHolder mBonusViewHolder;
    private View mBtnFullFunction;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void main() {
        super.main();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mRootView = (ViewGroup) findViewById(R.id.root);
        mTabButtonGroup = (TabButtonGroup) findViewById(R.id.tab_group);
        mTabButtonGroup.setClickIntercepter(new TabButtonGroup.ClickIntercepter() {
            @Override
            public boolean needIntercept(int position) {
                if (CommonAppConfig.getInstance().isBaseFunctionMode()) {
                    return true;
                }
                if ((position == 3 || position == 2) && !CommonAppConfig.getInstance().isLogin()) {
                    RouteUtil.forwardLogin(mContext);
                    return true;
                }
                return false;
            }
        });
        mTabButtonGroup.setOnBtnClickListener(new TabButtonGroup.OnBtnClickListener() {
            @Override
            public void onClick(int position) {
                if(position == 0){
                    if(mHomeViewHolder!=null){
                        mHomeViewHolder.changeToHomePage();
                    }
                }
            }
        });
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setOffscreenPageLimit(4);
        mViewList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            FrameLayout frameLayout = new FrameLayout(mContext);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mViewList.add(frameLayout);
        }
        mViewPager.setAdapter(new ViewPagerAdapter(mViewList));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                loadPageData(position, true);
                if (mViewHolders != null) {
                    for (int i = 0, length = mViewHolders.length; i < length; i++) {
                        AbsMainViewHolder vh = mViewHolders[i];
                        if (vh != null) {
                            vh.setShowed(position == i);
                        }
                    }
                }
                if(position==0){
                    boolean videoChecked = mHomeViewHolder != null && mHomeViewHolder.getCurrentItem() == 1;
                    changeStatusBarWhite(videoChecked);
                }else{
                    changeStatusBarWhite(false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabButtonGroup.setViewPager(mViewPager);
        mViewHolders = new AbsMainViewHolder[4];
        mDp70 = DpUtil.dp2px(70);
        mBottom = findViewById(R.id.bottom);
        mUpAnimator = ObjectAnimator.ofFloat(mBottom, "translationY", mDp70, 0);
        mDownAnimator = ObjectAnimator.ofFloat(mBottom, "translationY", 0, mDp70);
        mUpAnimator.setDuration(250);
        mDownAnimator.setDuration(250);
        mUpAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                mShowed = true;
                mHided = false;
            }
        });
        mDownAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimating = false;
                mShowed = false;
                mHided = true;
            }
        });
        mBtnFullFunction = findViewById(R.id.btn_full_function);
        if (CommonAppConfig.getInstance().isBaseFunctionMode()) {
            mBtnFullFunction.setVisibility(View.VISIBLE);
            mBtnFullFunction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FullFunctionDialogFragment fragment = new FullFunctionDialogFragment();
                    fragment.setAgreeCallback(new CommonCallback<Boolean>() {
                        @Override
                        public void callback(Boolean argree) {
                            if (argree) {
                                CommonAppContext.getInstance().startInitSdk();
                                if (mBtnFullFunction != null && mBtnFullFunction.getVisibility() != View.INVISIBLE) {
                                    mBtnFullFunction.setVisibility(View.INVISIBLE);
                                }
                                CommonAppConfig.getInstance().setBaseFunctionMode(false);
                                if (mHomeViewHolder != null) {
                                    mHomeViewHolder.setViewPagerCanScroll(true);
                                }
                            }
                        }
                    });
                    fragment.show(getSupportFragmentManager(), "FullFunctionDialogFragment");
                }
            });
        }
        EventBus.getDefault().register(this);
        checkVersion();
        requestAfterLogin();
        CommonAppConfig.getInstance().setLaunched(true);
        mFristLoad = true;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            boolean exit = intent.getBooleanExtra(Constants.EXIT, false);
            if (exit) {
                finish();
            } else {
                if (mTabButtonGroup != null) {
                    mTabButtonGroup.btnPerformClick(0);
                }
                if (CommonAppConfig.getInstance().isTeenagerType()) {
                    startTeenagerCountdown();
                    if (mBonusViewHolder != null) {
                        mBonusViewHolder.dismiss();
                        mBonusViewHolder = null;
                    }
                } else {
                    mIsTeengerTick = false;
                    if (mHandler != null) {
                        mHandler.removeCallbacks(mTeengerTicker);
                    }
                }
            }
        }
    }

    /**
     * 登录后请求接口
     */
    private void requestAfterLogin() {
        if (CommonAppConfig.getInstance().isLogin()) {
            getAgentCode();//邀请码弹窗
            loginIM();//登录IM
            checkTeenager();
        }
    }


    public void mainClick(View v) {
        if (CommonAppConfig.getInstance().isBaseFunctionMode()) {
            return;
        }
        if (!canClick()) {
            return;
        }
        int i = v.getId();
        if (i == R.id.btn_rank) {
            RankActivity.forward(mContext, 0);
            return;
        }
        if (!checkLogin()) {
            return;
        }
        if (i == R.id.btn_start) {
            showStartDialog();
        } else if (i == R.id.btn_search) {
            SearchActivity.forward(mContext);
        } else if (i == R.id.btn_msg) {
            ChatActivity.forward(mContext);
        } else if (i == R.id.btn_add_active) {
            startActivity(new Intent(mContext, ActivePubActivity.class));
        }
    }

    private void showStartDialog() {
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            ToastUtil.show(R.string.a_137);
            return;
        }
        if (!FloatWindowHelper.checkVoice(false)) {
            return;
        }
        MainStartDialogFragment dialogFragment = new MainStartDialogFragment();
        dialogFragment.setMainStartChooseCallback(mMainStartChooseCallback);
        dialogFragment.show(getSupportFragmentManager(), "MainStartDialogFragment");
    }

    private MainStartChooseCallback mMainStartChooseCallback = new MainStartChooseCallback() {
        @Override
        public void onLiveClick(final JSONObject startLiveInfo) {
            PermissionUtil.request(MainActivity.this,
                    new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            forwardLive(startLiveInfo);
                        }
                    },
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            );
        }

        @Override
        public void onVideoClick() {
            PermissionUtil.request(MainActivity.this,
                    new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            startActivity(new Intent(mContext, VideoRecordActivity.class));
                        }
                    },
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
            );
        }

        @Override
        public void onVoiceClick(final JSONObject startLiveInfo) {
            PermissionUtil.request(MainActivity.this,
                    new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            forwardVoiceLive(startLiveInfo);
                        }
                    },
                    Manifest.permission.RECORD_AUDIO
            );
        }

        @Override
        public void onScreenRecordLive(final JSONObject startLiveInfo) {
            PermissionUtil.request(MainActivity.this,
                    new PermissionCallback() {
                        @Override
                        public void onAllGranted() {
                            forwardScreenRecordLive(startLiveInfo);
                        }
                    },
                    Manifest.permission.RECORD_AUDIO
            );
        }
    };

    /**
     * 开启直播
     */
    private void forwardLive(JSONObject obj) {
        int haveStore = obj.getIntValue("isshop");
        String forbidLiveTip = obj.getString("liveban_title");
        int sdk = obj.getIntValue("live_sdk");
        LiveConfigBean configBean = null;
        if (sdk == Constants.LIVE_SDK_TX) {
            configBean = JSON.parseObject(obj.getString("android_tx"), LiveConfigBean.class);
        }
        LiveAnchorActivity.forward(mContext, sdk, configBean, haveStore, false, forbidLiveTip, false);

    }

    /**
     * 开启录屏直播
     */
    private void forwardScreenRecordLive(JSONObject obj) {
        String forbidLiveTip = obj.getString("liveban_title");
        LiveAnchorActivity.forward(mContext, obj.getIntValue("live_sdk"), null, 0, false, forbidLiveTip, true);
    }

    /**
     * 开启语音直播
     */
    private void forwardVoiceLive(JSONObject obj) {
        int haveStore = obj.getIntValue("isshop");
        String forbidLiveTip = obj.getString("liveban_title");
        int sdk = obj.getIntValue("live_sdk");
        LiveConfigBean configBean = null;
        if (sdk == Constants.LIVE_SDK_TX) {
            configBean = JSON.parseObject(obj.getString("android_tx"), LiveConfigBean.class);
        }
        LiveAnchorActivity.forward(mContext, sdk, configBean, haveStore, true, forbidLiveTip, false);
    }

    /**
     * 检查版本更新
     */
    private void checkVersion() {
        CommonAppConfig.getInstance().getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean configBean) {
                if (configBean != null) {
                    if (configBean.getMaintainSwitch() == 1) {//开启维护
                        DialogUitl.showSimpleTipDialog(mContext, WordUtil.getString(R.string.main_maintain_notice), configBean.getMaintainTips());
                    }
                    if (!VersionUtil.isLatest(configBean.getVersion())) {
                        VersionUtil.showDialog(mContext, configBean, configBean.getDownloadApkUrl());
                    }
                }
            }
        });
    }

    /**
     * 检查codeInstall安装参数
     */
    private void getAgentCode() {
        CommonAppConfig.getInstance().getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean configBean) {
                if (configBean != null && configBean.getOpenInstallSwitch() == 1) {
                    OpenInstall.getInstall(new AppInstallAdapter() {
                        @Override
                        public void onInstall(AppData appData) {
                            String bindData = appData.getData();
//                            ToastUtil.show("OpenInstall:" + bindData);
                            if (TextUtils.isEmpty(bindData)) {
                                checkAgent();
                            } else {
                                String agentCode = null;
                                try {
                                    agentCode = JSON.parseObject(bindData).getString("code");
                                } catch (Exception e) {

                                }
                                if (TextUtils.isEmpty(agentCode)) {
                                    checkAgent();
                                } else {
                                    L.e("OpenInstall---获取到邀请码---> " + agentCode);
                                    MainHttpUtil.setDistribut(agentCode, new HttpCallback() {
                                        @Override
                                        public void onSuccess(int code, String msg, String[] info) {
                                            L.e("OpenInstall---setDistribut---> " + code + "---msg----> " + msg);
                                        }
                                    });
                                }
                            }
                        }
                    });
                } else {
                    checkAgent();
                }
            }
        });
    }


    /**
     * 检查是否显示填写邀请码的弹窗
     */
    private void checkAgent() {
        MainHttpUtil.checkAgent(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    boolean isHasAgent = obj.getIntValue("has_agent") == 1;//是否有上下级关系
                    boolean agentSwitch = obj.getIntValue("agent_switch") == 1;
                    boolean isAgentMust = obj.getIntValue("agent_must") == 1;//是否必填
                    if (!isHasAgent && agentSwitch) {
                        if (mFirstLogin || isAgentMust) {
                            showInvitationCode(isAgentMust);
                        }
                    }
                }
            }
        });
    }

    /**
     * 填写邀请码的弹窗
     */
    private void showInvitationCode(boolean inviteMust) {
        if (inviteMust) {
            NotCancelableInputDialog dialog = new NotCancelableInputDialog();
            dialog.setTitle(WordUtil.getString(R.string.main_input_invatation_code));
            dialog.setActionListener(new NotCancelableInputDialog.ActionListener() {
                @Override
                public void onConfirmClick(String content, final DialogFragment dialog) {
                    if (TextUtils.isEmpty(content)) {
                        ToastUtil.show(R.string.main_input_invatation_code);
                        return;
                    }
                    MainHttpUtil.setDistribut(content, new HttpCallback() {
                        @Override
                        public void onSuccess(int code, String msg, String[] info) {
                            if (code == 0 && info.length > 0) {
                                ToastUtil.show(JSON.parseObject(info[0]).getString("msg"));
                                dialog.dismissAllowingStateLoss();
                            } else {
                                ToastUtil.show(msg);
                            }
                        }
                    });
                }

            });
            dialog.show(getSupportFragmentManager(), "NotCancelableInputDialog");
        } else {
            new DialogUitl.Builder(mContext)
                    .setTitle(WordUtil.getString(R.string.main_input_invatation_code))
                    .setCancelable(true)
                    .setInput(true)
                    .setBackgroundDimEnabled(true)
                    .setClickCallback(new DialogUitl.SimpleCallback() {
                        @Override
                        public void onConfirmClick(final Dialog dialog, final String content) {
                            if (TextUtils.isEmpty(content)) {
                                ToastUtil.show(R.string.main_input_invatation_code);
                                return;
                            }
                            MainHttpUtil.setDistribut(content, new HttpCallback() {
                                @Override
                                public void onSuccess(int code, String msg, String[] info) {
                                    if (code == 0 && info.length > 0) {
                                        ToastUtil.show(JSON.parseObject(info[0]).getString("msg"));
                                        dialog.dismiss();
                                    } else {
                                        ToastUtil.show(msg);
                                    }
                                }
                            });
                        }
                    })
                    .build()
                    .show();
        }
    }

    /**
     * 签到奖励
     */
    private void requestBonus() {
        MainHttpUtil.requestBonus(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    if (obj.getIntValue("bonus_switch") == 0) {
                        return;
                    }
                    int day = obj.getIntValue("bonus_day");
                    if (day <= 0) {
                        return;
                    }
                    List<BonusBean> list = JSON.parseArray(obj.getString("bonus_list"), BonusBean.class);
                    BonusViewHolder bonusViewHolder = new BonusViewHolder(mContext, mRootView);
                    bonusViewHolder.setData(list, day, obj.getString("count_day"));
                    bonusViewHolder.show();
                    mBonusViewHolder = bonusViewHolder;
                }
            }
        });
    }

    /**
     * 登录IM
     */
    private void loginIM() {
        String uid = CommonAppConfig.getInstance().getUid();
        ImMessageUtil.getInstance().loginImClient(uid);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mFristLoad) {
            mFristLoad = false;
            loadPageData(0, true);
            if (mHomeViewHolder != null) {
                mHomeViewHolder.setShowed(true);
            }
            NotificationUtil.checkNotificationsEnabled(mContext);
            getLocation();
            getPushAction();
        }

        if (mLoginChanged) {
            mLoginChanged = false;
            requestAfterLogin();
//            if (mMallViewHolder != null) {
//                mMallViewHolder.showMyShopAvatar();
//            }
        }

        if (CommonAppConfig.getInstance().isTeenagerType()) {
            if (mBonusViewHolder != null) {
                mBonusViewHolder.dismiss();
                mBonusViewHolder = null;
            }
        }
    }

    /**
     * 获取所在位置
     */
    private void getLocation() {
        if (hasLocationPermission()) {
            LocationUtil.getInstance().startLocation();
        }
    }


    @Override
    protected void onDestroy() {
        if (mTabButtonGroup != null) {
            mTabButtonGroup.cancelAnim();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler = null;
        EventBus.getDefault().unregister(this);
        LiveHttpUtil.cancel(LiveHttpConsts.GET_LIVE_SDK);
        MainHttpUtil.cancel(CommonHttpConsts.GET_CONFIG);
        MainHttpUtil.cancel(MainHttpConsts.REQUEST_BONUS);
        MainHttpUtil.cancel(MainHttpConsts.GET_BONUS);
        MainHttpUtil.cancel(MainHttpConsts.CHECK_AGENT);
        MainHttpUtil.cancel(MainHttpConsts.SET_DISTRIBUT);
        MainHttpUtil.cancel(MainHttpConsts.CHANGE_TEENAGER_TIME);
        if (mCheckLivePresenter != null) {
            mCheckLivePresenter.cancel();
        }
        LocationUtil.getInstance().stopLocation();
        CommonAppConfig.getInstance().setGiftListJson(null);
        CommonAppConfig.getInstance().setLaunched(false);
        LiveStorge.getInstance().clear();
        VideoStorge.getInstance().clear();
        FloatWindowUtil.getInstance().dismiss();
        LiveChatRoomPlayUtil.getInstance().setKeepAlive(false);
        LiveChatRoomPlayUtil.getInstance().release();
        super.onDestroy();
    }

    public static void forward(Context context) {
        forward(context, null);
    }

    public static void forward(Context context, Bundle bundle) {
        Intent intent = new Intent(context, MainActivity.class);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        context.startActivity(intent);
    }

    public void watchLive(LiveBean liveBean, String key, int position) {
        watchLive(liveBean, key, position, true);
    }

    /**
     * 观看直播
     */
    public void watchLive(LiveBean liveBean, String key, int position, boolean needShowDialog) {
        if (!FloatWindowHelper.checkVoice(true)) {
            return;
        }
        if (mCheckLivePresenter == null) {
            mCheckLivePresenter = new CheckLivePresenter(mContext);
        }
        if (CommonAppConfig.LIVE_ROOM_SCROLL) {
            mCheckLivePresenter.watchLive(liveBean, key, position, needShowDialog);
        } else {
            mCheckLivePresenter.watchLive(liveBean, needShowDialog);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onImUnReadCountEvent(ImUnReadCountEvent e) {
        if (!CommonAppConfig.getInstance().isPrivateMsgSwitchOpen()) {
            return;
        }
        ImUnReadCount unReadCount = ImMessageUtil.getInstance().getUnReadMsgCount();
        if (unReadCount != null) {
            String count = unReadCount.getTotalUnReadCount();
            if (!TextUtils.isEmpty(count)) {
//                if (mHomeViewHolder != null) {
//                    mHomeViewHolder.setUnReadCount(count);
//                }
                if (mActiveViewHolder != null) {
                    mActiveViewHolder.setUnReadCount(count);
                }
                if (mMeViewHolder != null) {
                    mMeViewHolder.setUnReadCount(count);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        if (curTime - mLastClickBackTime > 2000) {
            mLastClickBackTime = curTime;
            ToastUtil.show(R.string.main_click_next_exit);
            return;
        }
        super.onBackPressed();
    }


    private void loadPageData(int position, boolean needlLoadData) {
        if (mViewHolders == null) {
            return;
        }
        AbsMainViewHolder vh = mViewHolders[position];
        if (vh == null) {
            if (mViewList != null && position < mViewList.size()) {
                FrameLayout parent = mViewList.get(position);
                if (parent == null) {
                    return;
                }
                if (position == 0) {
                    mHomeViewHolder = new MainHomeViewHolder(mContext, parent);
                    mHomeViewHolder.setAppBarLayoutListener(this);
                    vh = mHomeViewHolder;
                } else if (position == 1) {
                    mActiveViewHolder = new MainActiveViewHolder(mContext, parent);
                    mActiveViewHolder.setAppBarLayoutListener(this);
                    vh = mActiveViewHolder;
                } else if (position == 2) {
                    mShortPlayViewHolder = new MainHomeShortPlayViewHolder(mContext, parent);
                    vh = mShortPlayViewHolder;
                } else if (position == 3) {
                    mMeViewHolder = new MainMeViewHolder(mContext, parent);
                    vh = mMeViewHolder;
                }
                if (vh == null) {
                    return;
                }
                mViewHolders[position] = vh;
                vh.addToParent();
                vh.subscribeActivityLifeCycle();
            }
        }
        if (needlLoadData && vh != null) {
            vh.loadData();
        }
    }

    @Override
    public void onOffsetChangedDirection(boolean up) {
        if (!mAnimating) {
            if (up) {
                if (mShowed && mDownAnimator != null) {
                    mDownAnimator.start();
                }
            } else {
                if (mHided && mUpAnimator != null) {
                    mUpAnimator.start();
                }
            }
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (mAudioManager != null) {
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_RAISE,
                            AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (mAudioManager != null) {
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                            AudioManager.ADJUST_LOWER,
                            AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 观众退出语音直播间，显示悬浮窗
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveAudienceExitEvent(LiveAudienceChatRoomExitEvent e) {
        LiveBean liveBean = e.getLiveBean();
        if (liveBean != null) {
            FloatWindowUtil.getInstance().setType(Constants.FLOAT_TYPE_DEFAULT)
                    .setLiveBean(liveBean)
                    .setLiveAudienceFloatWindowData(e.getLiveAudienceAgoraData())
                    .requestPermission();
        }
    }


    /**
     * 点击悬浮窗，进入直播间
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveFloatWindowEvent(LiveFloatWindowEvent e) {
        if (e.getType() == Constants.FLOAT_TYPE_DEFAULT) {
            watchLive(e.getLiveBean(), "", 0, false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCloseFloatWindowEvent(CloseFloatWindowEvent e) {
        FloatWindowUtil.getInstance().dismiss();
    }


    /**
     * 登录状态改变
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginChangeEvent(LoginChangeEvent e) {
        mLoginChanged = true;
        mFirstLogin = e.isFirstLogin();
    }


    public void setCurrentPage(int position) {
        if (mTabButtonGroup != null) {
            mTabButtonGroup.setCurPosition(position);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mViewPager != null) {
            outState.putInt("CurrentItem", mViewPager.getCurrentItem());
        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int savedCurrentItem = savedInstanceState.getInt("CurrentItem", 0);
        if (mViewPager != null) {
            if (savedCurrentItem != mViewPager.getCurrentItem()) {
                mViewPager.setCurrentItem(savedCurrentItem, false);
            }
        }
        if (mTabButtonGroup != null) {
            mTabButtonGroup.setCurPosition(savedCurrentItem);
        }
    }

    /**
     * 是否开启青少年模式
     */
    private void checkTeenager() {
        MainHttpUtil.checkTeenager(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    if (info.length > 0) {
                        JSONObject obj = JSON.parseObject(info[0]);
                        boolean isOpen = obj.getIntValue("status") == 1;
                        CommonAppConfig.getInstance().setTeenagerType(isOpen);
                        if (!isOpen) {
                            boolean isTeenagerShow = SpUtil.getInstance().getBooleanValue(SpUtil.TEENAGER_SHOW);
                            if (!isTeenagerShow) {
                                SpUtil.getInstance().setBooleanValue(SpUtil.TEENAGER_SHOW, true);
                                TeenagerDialogFragment fragment = new TeenagerDialogFragment();
                                fragment.show(getSupportFragmentManager(), "TeenagerDialogFragment");
                            }
                            requestBonus();//每日签到
                        } else {
                            int is_tip = obj.getIntValue("is_tip");
                            if (is_tip == 1) {
                                mIsTeengerTick = false;
                                RouteUtil.teenagerTip(obj.getString("tips"));
                            } else {
                                startTeenagerCountdown();
                            }
                        }
                    }

                }
            }
        });
    }

    /**
     * 启动青少年模式定时器
     */
    private void startTeenagerCountdown() {
        if (mIsTeengerTick) {
            return;
        }
        mIsTeengerTick = true;
        if (mTeengerTicker == null) {
            mTeengerTicker = new Runnable() {
                @Override
                public void run() {
                    if (mHandler == null) {
                        mHandler = new Handler();
                    }
                    long now = SystemClock.uptimeMillis();
                    if (mTeenagerTimeCallback == null) {
                        mTeenagerTimeCallback = new HttpCallback() {
                            @Override
                            public void onSuccess(int code, String msg, String[] info) {
                                if (code != 0) {
                                    mIsTeengerTick = false;
                                    if (mHandler != null) {
                                        mHandler.removeCallbacks(mTeengerTicker);
                                    }
                                    if (code == 10010 || code == 10011) {
                                        RouteUtil.teenagerTip(msg);
                                    }
                                }
                            }
                        };
                    }
                    MainHttpUtil.changeTeenagerTime(mTeenagerTimeCallback);
                    if (mIsTeengerTick) {
                        long next = now + (1000 - now % 1000) + 9000;
                        mHandler.postAtTime(mTeengerTicker, next);
                    }
                }
            };
        }
        mTeengerTicker.run();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNotificiationLiveEvent(NotificiationClickEvent e) {
        if (e == null) {
            return;
        }
        int type = e.getType();
        if (type == Constants.PUSH_TYPE_MESSAGE) {
            ChatActivity.forward(mContext);
        } else if (type == Constants.PUSH_TYPE_LIVE) {
//            enterLiveRoomByPush(e.getData());
        }
    }

    private void getPushAction() {
        Intent intent = getIntent();
        int pushType = intent.getIntExtra(Constants.PUSH_TYPE, 0);
        String pushData = intent.getStringExtra(Constants.PUSH_DATA);
        if (pushType == Constants.PUSH_TYPE_MESSAGE) {
            if (CommonAppConfig.getInstance().isLogin()) {
                ChatActivity.forward(mContext);
            }
        } else if (pushType == Constants.PUSH_TYPE_LIVE) {
//            if (CommonAppConfig.getInstance().isLogin()) {
//                enterLiveRoomByPush(pushData);
//            }
        }
    }

    /**
     * 通过推送的数据进入直播间
     */
    private void enterLiveRoomByPush(String liveUserInfo) {
        if (TextUtils.isEmpty(liveUserInfo)) {
            return;
        }
        try {
            LiveBean liveBean = JSON.parseObject(liveUserInfo, LiveBean.class);
            watchLive(liveBean, "", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBottomVisible(boolean visible){
        if(mBottom!=null){
            mBottom.setTranslationX(visible?0:100000);
        }
    }

}
