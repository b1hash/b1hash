package com.yunbao.live.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.opensource.svgaplayer.SVGAImageView;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.bean.GoodsBean;
import com.yunbao.common.bean.LevelBean;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.ScreenDimenUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.TaskScheduler;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.live.R;
import com.yunbao.live.activity.LiveActivity;
import com.yunbao.live.activity.LiveAnchorActivity;
import com.yunbao.live.activity.LiveAudienceActivity;
import com.yunbao.live.adapter.LiveChatAdapter;
import com.yunbao.live.adapter.LiveUserAdapter;
import com.yunbao.live.bean.GlobalGiftBean;
import com.yunbao.live.bean.LiveBean;
import com.yunbao.live.bean.LiveBuyGuardMsgBean;
import com.yunbao.live.bean.LiveChatBean;
import com.yunbao.live.bean.LiveDanMuBean;
import com.yunbao.live.bean.LiveEnterRoomBean;
import com.yunbao.live.bean.LiveGiftPrizePoolWinBean;
import com.yunbao.live.bean.LiveLuckGiftWinBean;
import com.yunbao.live.bean.LiveReceiveGiftBean;
import com.yunbao.live.bean.LiveUserGiftBean;
import com.yunbao.live.custom.LiveLightView;
import com.yunbao.live.custom.TopGradual;
import com.yunbao.live.dialog.LiveUserDialogFragment;
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;
import com.yunbao.live.presenter.LiveDanmuPresenter;
import com.yunbao.live.presenter.LiveEnterRoomAnimPresenter;
import com.yunbao.live.presenter.LiveGiftAnimPresenter;
import com.yunbao.live.presenter.LiveLightAnimPresenter;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by cxf on 2018/10/9.
 * 直播间公共逻辑
 */

public class LiveRoomViewHolder extends AbsViewHolder implements View.OnClickListener {

    private int mOffsetY;
    private ViewGroup mRoot;
    private ImageView mAvatar;
    private ImageView mLevelAnchor;
    private TextView mName;
    private TextView mID;
    private View mBtnFollow;
    private TextView mVotesName;//映票名称
    private TextView mVotes;
    private TextView mGuardNum;//守护人数
    private TextView mTvUserNum;//直播间人数
    private RecyclerView mUserRecyclerView;
    private RecyclerView mChatRecyclerView;
    private LiveUserAdapter mLiveUserAdapter;
    private LiveChatAdapter mLiveChatAdapter;
    private View mBtnRedPack;
    private String mLiveUid;
    private String mStream;
    private LiveLightAnimPresenter mLightAnimPresenter;
    private LiveEnterRoomAnimPresenter mLiveEnterRoomAnimPresenter;
    private LiveDanmuPresenter mLiveDanmuPresenter;
    private LiveGiftAnimPresenter mLiveGiftAnimPresenter;
    private HttpCallback mRefreshUserListCallback;
    private HttpCallback mTimeChargeCallback;
    protected int mUserListInterval;//用户列表刷新时间的间隔
    private GifImageView mGifImageView;
    private SVGAImageView mSVGAImageView;
    private ViewGroup mLiveGiftPrizePoolContainer;
    private TextView mLiveTimeTextView;//主播的直播时长
    private int mAnchorLiveTime;//主播直播时间
    private View mGroupGoods;
    private ImageView mGoodsThumb;
    private TextView mGoodsName;
    private TextView mGoodsPrice;
    private GoodsBean mShowGoodsBean;//直播间当前展示的商品
    private LiveRoomBtnViewHolder mBtnViewHolder;
    private int mUserNumValue;
    private StringBuilder mStringBuilder;
    private TaskScheduler mTaskScheduler;
    private static final String TASK_REFRESH_USER_LIST = "refreshUserList";
    private static final String TASK_TIME_CHARGE = "timeCharge";//计时收费房间定时请求接口扣费
    private static final String TASK_ANCHOR_PAUSE = "anchorPause";//主播切后台
    private static final String TASK_ANCHOR_LIGHT = "anchorLight";//主播飘心
    private static final String TASK_ANCHOR_LIVE_TIME = "anchorLiveTime";//主播直播时长
    private static final String TASK_ANCHOR_CHECK_LIVE = "anchorCheckLive";//主播checkLive

    public LiveRoomViewHolder(Context context, ViewGroup parentView, GifImageView gifImageView, SVGAImageView svgaImageView, ViewGroup liveGiftPrizePoolContainer) {
        super(context, parentView);
        mGifImageView = gifImageView;
        mSVGAImageView = svgaImageView;
        mLiveGiftPrizePoolContainer = liveGiftPrizePoolContainer;
        View group_1 = findViewById(R.id.group_1);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) group_1.getLayoutParams();
        params.topMargin = ScreenDimenUtil.getInstance().getStatusBarHeight() + DpUtil.dp2px(5);
        group_1.requestLayout();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_room;
    }

    @Override
    public void init() {
        mRoot = (ViewGroup) findViewById(R.id.root);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mLevelAnchor = (ImageView) findViewById(R.id.level_anchor);
        mName = (TextView) findViewById(R.id.name);
        mID = (TextView) findViewById(R.id.id_val);
        mBtnFollow = findViewById(R.id.btn_follow);
        mVotesName = (TextView) findViewById(R.id.votes_name);
        mVotes = (TextView) findViewById(R.id.votes);
        mGuardNum = (TextView) findViewById(R.id.guard_num);
        mTvUserNum = findViewById(R.id.tv_user_num);
        mTvUserNum.setOnClickListener(this);
        //用户头像列表
        mUserRecyclerView = (RecyclerView) findViewById(R.id.user_recyclerView);
        mUserRecyclerView.setHasFixedSize(true);
        mUserRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mLiveUserAdapter = new LiveUserAdapter(mContext);
        mLiveUserAdapter.setOnItemClickListener(new OnItemClickListener<UserBean>() {
            @Override
            public void onItemClick(UserBean bean, int position) {
                showUserDialog(bean.getId());
            }
        });
        mUserRecyclerView.setAdapter(mLiveUserAdapter);
        //聊天栏
        mChatRecyclerView = (RecyclerView) findViewById(R.id.chat_recyclerView);
        mChatRecyclerView.setHasFixedSize(true);
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mChatRecyclerView.addItemDecoration(new TopGradual());
        mLiveChatAdapter = new LiveChatAdapter(mContext);
        mLiveChatAdapter.setOnItemClickListener(new OnItemClickListener<LiveChatBean>() {
            @Override
            public void onItemClick(LiveChatBean bean, int position) {
                showUserDialog(bean.getId());
            }
        });
        mChatRecyclerView.setAdapter(mLiveChatAdapter);
        mVotesName.setText(CommonAppConfig.getInstance().getVotesName());

        mGroupGoods = findViewById(R.id.group_goods);
        mGoodsThumb = findViewById(R.id.goods_thumb);
        mGoodsName = findViewById(R.id.goods_name);
        mGoodsPrice = findViewById(R.id.goods_price);
        findViewById(R.id.btn_goods_detail).setOnClickListener(this);
        findViewById(R.id.btn_close_live).setOnClickListener(this);
        mBtnFollow.setOnClickListener(this);
        mAvatar.setOnClickListener(this);
        View btnVotes = findViewById(R.id.btn_votes);
        View btnGuard = findViewById(R.id.btn_guard);
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            btnVotes.setVisibility(View.INVISIBLE);
            btnGuard.setVisibility(View.INVISIBLE);
        } else {
            btnVotes.setOnClickListener(this);
            btnGuard.setOnClickListener(this);
        }

        mBtnRedPack = findViewById(R.id.btn_red_pack);
        mBtnRedPack.setOnClickListener(this);
        if (mContext instanceof LiveAudienceActivity) {
            if (!((LiveActivity) mContext).isChatRoom()) {
                mRoot.setOnClickListener(this);
            }
        } else {
            mLiveTimeTextView = (TextView) findViewById(R.id.live_time);
            mLiveTimeTextView.setVisibility(View.VISIBLE);
        }
        mLightAnimPresenter = new LiveLightAnimPresenter(mContext, mParentView);
        mLiveEnterRoomAnimPresenter = new LiveEnterRoomAnimPresenter(mContext, mContentView);
        mTaskScheduler = new TaskScheduler();

    }


    /**
     * 显示直播间左上角按钮
     *
     * @param showPan        是否显示转盘
     * @param prizePoolLevel 奖池等级
     */
    public void showBtn(boolean showPan, int prizePoolLevel, int dailyTaskSwitch) {
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            return;
        }
        if (!showPan && prizePoolLevel < 0 && dailyTaskSwitch != 1) {
            return;
        }
        if (mBtnViewHolder == null) {
            mBtnViewHolder = new LiveRoomBtnViewHolder(mContext, (ViewGroup) findViewById(R.id.btn_container), showPan, prizePoolLevel, dailyTaskSwitch);
            mBtnViewHolder.addToParent();
            mBtnViewHolder.subscribeActivityLifeCycle();
//            mPrizePoolLevel = mBtnViewHolder.getPrizePoolLevel();
//            mPrizePoolGuang = mBtnViewHolder.getPrizePoolGuang();
//            if (mLiveGiftAnimPresenter != null) {
//                mLiveGiftAnimPresenter.setPrizePoolView(mPrizePoolLevel, mPrizePoolGuang);
//            }
        }
        mBtnViewHolder.showPrizeLevel(prizePoolLevel);
    }


    /**
     * 显示主播头像
     */
    public void setAvatar(String url) {
        if (mAvatar != null) {
            ImgLoader.displayAvatar(mContext, url, mAvatar);
        }
    }

    /**
     * 显示主播等级
     */
    public void setAnchorLevel(int anchorLevel) {
        if (mLevelAnchor != null) {
            LevelBean levelBean = CommonAppConfig.getInstance().getAnchorLevel(anchorLevel);
            if (levelBean != null) {
                ImgLoader.display(mContext, levelBean.getThumbIcon(), mLevelAnchor);
            }
        }
    }

    /**
     * 显示用户名
     */
    public void setName(String name) {
        if (mName != null) {
            mName.setText(name);
        }
    }

    /**
     * 显示房间号
     */
    public void setRoomNum(String roomNum) {
        if (mID != null) {
            mID.setText(roomNum);
        }
    }

    /**
     * 显示直播标题
     */
    public void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            if (mLiveGiftAnimPresenter == null) {
                mLiveGiftAnimPresenter = new LiveGiftAnimPresenter(mContext, mContentView, mGifImageView, mSVGAImageView, mLiveGiftPrizePoolContainer);
            }
            mLiveGiftAnimPresenter.showLiveTitleAnim(title);
        }
    }


    /**
     * 显示是否关注
     */
    public void setAttention(int attention) {
        if (mBtnFollow != null) {
            if (attention == 0) {
                if (mBtnFollow.getVisibility() != View.VISIBLE) {
                    mBtnFollow.setVisibility(View.VISIBLE);
                }
            } else {
                if (mBtnFollow.getVisibility() == View.VISIBLE) {
                    mBtnFollow.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 显示刷新直播间用户列表
     */
    public void setUserList(List<LiveUserGiftBean> list) {
        if (mLiveUserAdapter != null) {
            mLiveUserAdapter.refreshList(list);
        }
    }

    /**
     * 显示主播映票数
     */
    public void setVotes(String votes) {
        if (mVotes != null) {
            mVotes.setText(votes);
        }
    }

    /**
     * 显示主播守护人数
     */
    public void setGuardNum(int guardNum) {
        if (mGuardNum != null) {
            if (guardNum > 0) {
                mGuardNum.setText(String.format(WordUtil.getString(R.string.ren), guardNum));
            } else {
                mGuardNum.setText(R.string.main_list_no_data);
            }
        }
    }

    private void showUserNum() {
        if (mTvUserNum != null) {
            String num = "";
            if (mUserNumValue > 99) {
                num = "99+";
            } else {
                if (mUserNumValue < 0) {
                    mUserNumValue = 0;
                }
                num = String.valueOf(mUserNumValue);
            }
            mTvUserNum.setText(num);
        }
    }

    public void setUserNum(int userNum) {
        mUserNumValue = userNum;
        showUserNum();
    }

    public void increaseUserNum(int num) {
//        mUserNumValue += num;
//        showUserNum();
        refreshUserList();
    }


    public void decreaseUserNum(int num) {
//        mUserNumValue -= num;
//        if (mUserNumValue < 0) {
//            mUserNumValue = 0;
//        }
//        showUserNum();
        refreshUserList();
    }

    public void setLiveInfo(String liveUid, String stream, int userListInterval) {
        mLiveUid = liveUid;
        mStream = stream;
        mUserListInterval = userListInterval;
    }

    /**
     * 守护信息发生变化
     */
    public void onGuardInfoChanged(LiveBuyGuardMsgBean bean) {
        setGuardNum(bean.getGuardNum());
        setVotes(bean.getVotes());
        if (mLiveUserAdapter != null) {
            mLiveUserAdapter.onGuardChanged(bean.getUid(), bean.getGuardType());
        }
    }

    /**
     * 设置红包按钮是否可见
     */
    public void setRedPackBtnVisible(boolean visible) {
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            return;
        }
        if (mBtnRedPack != null) {
            if (visible) {
                if (mBtnRedPack.getVisibility() != View.VISIBLE) {
                    mBtnRedPack.setVisibility(View.VISIBLE);
                }
            } else {
                if (mBtnRedPack.getVisibility() == View.VISIBLE) {
                    mBtnRedPack.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.root) {
            light();
        }
        if (!canClick()) {
            return;
        }
        if (i == R.id.avatar) {
            showAnchorUserDialog();

        } else if (i == R.id.btn_follow) {
            follow();

        } else if (i == R.id.btn_votes) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            openContributeWindow();

        } else if (i == R.id.btn_guard) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveActivity) mContext).openGuardListWindow();

        } else if (i == R.id.btn_red_pack) {
            if (!((AbsActivity) mContext).checkLogin()) {
                return;
            }
            ((LiveActivity) mContext).openRedPackListWindow();

        } else if (i == R.id.btn_goods_detail) {
            forwardGoods();
        } else if (i == R.id.btn_close_live) {
            if (mContext instanceof LiveAnchorActivity) {
                ((LiveAnchorActivity) mContext).closeLive();
            } else if (mContext instanceof LiveAudienceActivity) {
                ((LiveAudienceActivity) mContext).onBackPressed();
            }
        } else if (i == R.id.tv_user_num) {
            ((LiveActivity) mContext).showUserListDialog();
        }
    }


    /**
     * 查看直播间当前商品
     */
    private void forwardGoods() {
        if (mContext != null && mContext instanceof LiveAudienceActivity) {
            ((LiveAudienceActivity) mContext).liveGoodsFloat();
        }
        LiveBean liveBean = ((LiveActivity) mContext).getLiveBean();
        if (mShowGoodsBean != null) {
            if (mShowGoodsBean.getType() == 1) {
                RouteUtil.forwardGoodsDetailOutSide(mContext, mShowGoodsBean.getId(), false, liveBean);
            } else {
                RouteUtil.forwardGoodsDetail(mContext, mShowGoodsBean.getId(), false, mLiveUid, null, liveBean);
            }
        }
    }

    /**
     * 关注主播
     */
    public void follow() {
        if (!((AbsActivity) mContext).checkLogin()) {
            return;
        }
        if (TextUtils.isEmpty(mLiveUid)) {
            return;
        }
        CommonHttpUtil.setAttention(mLiveUid, new CommonCallback<Integer>() {
            @Override
            public void callback(Integer isAttention) {
                if (isAttention == 1) {
                    String userName = CommonAppConfig.getInstance().getUserBean().getUserNiceName();
                    ((LiveActivity) mContext).sendSystemMessage(
                            StringUtil.contact(userName, "关注了主播"),
                            StringUtil.contact(userName, " followed the anchor"));
                }
            }
        });
    }

    /**
     * 用户进入房间，用户列表添加该用户
     */
    public void insertUser(LiveUserGiftBean bean) {
        if (mLiveUserAdapter != null) {
            mLiveUserAdapter.insertItem(bean);
        }
    }

    /**
     * 用户进入房间，添加僵尸粉
     */
    public void insertUser(List<LiveUserGiftBean> list) {
        if (mLiveUserAdapter != null) {
            mLiveUserAdapter.insertList(list);
        }
    }

    /**
     * 用户离开房间，用户列表删除该用户
     */
    public void removeUser(String uid) {
        if (mLiveUserAdapter != null) {
            mLiveUserAdapter.removeItem(uid);
        }
    }

    /**
     * 刷新用户列表
     */
    private void refreshUserList() {
        if (mRefreshUserListCallback == null) {
            mRefreshUserListCallback = new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (code == 0 && info.length > 0) {
                        JSONObject obj = JSON.parseObject(info[0]);
                        if (mLiveUserAdapter != null) {
                            List<LiveUserGiftBean> list = JSON.parseArray(obj.getString("userlist"), LiveUserGiftBean.class);
                            mLiveUserAdapter.refreshList(list);
                        }
                        setUserNum(obj.getIntValue("nums"));
                    }
                }
            };
        }
        LiveHttpUtil.getUserList(mLiveUid, mStream, mRefreshUserListCallback);
    }

    /**
     * 开始刷新用户列表
     */
    public void startRefreshUserList() {
        if (mTaskScheduler != null) {
            int interval = mUserListInterval > 0 ? mUserListInterval : 60000;
            mTaskScheduler.startTask(TASK_REFRESH_USER_LIST, interval, interval, new Runnable() {
                @Override
                public void run() {
                    refreshUserList();
                }
            });
        }
    }

    /**
     * 请求计时收费的扣费接口
     */
    private void requestTimeCharge() {
        if (mTimeChargeCallback == null) {
            mTimeChargeCallback = new HttpCallback() {
                @Override
                public void onSuccess(int code, String msg, String[] info) {
                    if (mContext instanceof LiveAudienceActivity) {
                        final LiveAudienceActivity liveAudienceActivity = (LiveAudienceActivity) mContext;
                        if (code == 0) {
                            liveAudienceActivity.roomChargeUpdateVotes();
                        } else {
                            if (mTaskScheduler != null) {
                                mTaskScheduler.cancelTask(TASK_TIME_CHARGE);
                            }
                            liveAudienceActivity.pausePlay();
                            if (code == 1008) {//余额不足
                                liveAudienceActivity.setCoinNotEnough(true);
                                DialogUitl.showSimpleDialog(mContext, WordUtil.getString(R.string.live_coin_not_enough), false,
                                        new DialogUitl.SimpleCallback2() {
                                            @Override
                                            public void onConfirmClick(Dialog dialog, String content) {
                                                RouteUtil.forwardMyCoin(mContext);
                                            }

                                            @Override
                                            public void onCancelClick() {
                                                liveAudienceActivity.exitLiveRoom();
                                            }
                                        });
                            }
                        }
                    }
                }
            };
        }
        LiveHttpUtil.timeCharge(mLiveUid, mStream, mTimeChargeCallback);
    }

    /**
     * 开始请求计时收费的扣费接口
     */
    public void startRequestTimeCharge() {
        if (mTaskScheduler != null) {
            mTaskScheduler.startTask(TASK_TIME_CHARGE, 60000, 60000, new Runnable() {
                @Override
                public void run() {
                    requestTimeCharge();
                }
            });
        }
    }


    /**
     * 添加聊天消息到聊天栏
     */
    public void insertChat(LiveChatBean bean) {
        if (mLiveChatAdapter != null) {
            mLiveChatAdapter.insertItem(bean);
        }
    }

    /**
     * 添加聊天消息到聊天栏
     */
    public void insertChatList(List<LiveChatBean> list) {
        if (mLiveChatAdapter != null) {
            mLiveChatAdapter.insertList(list);
        }
    }

    /**
     * 播放飘心动画
     */
    public void playLightAnim() {
        if (mLightAnimPresenter != null) {
            mLightAnimPresenter.play();
        }
    }

    /**
     * 点亮
     */
    private void light() {
        ((LiveAudienceActivity) mContext).light();
    }

    public void setOffsetY(int offsetY) {
        LiveLightView.sOffsetY = offsetY;
        mOffsetY = offsetY;
    }


    /**
     * 键盘高度变化
     */
    public void onKeyBoardChanged(int keyBoardHeight) {
        if (mRoot != null) {
            if (keyBoardHeight == 0 || keyBoardHeight < 200) {
                mRoot.setTranslationY(0);
                return;
            }
            if (mOffsetY == 0) {
                mRoot.setTranslationY(-keyBoardHeight);
                return;
            }
            if (mOffsetY > 0 && mOffsetY < keyBoardHeight) {
                mRoot.setTranslationY(mOffsetY - keyBoardHeight);
            }
        }
    }

    /**
     * 聊天栏滚到最底部
     */
    public void chatScrollToBottom() {
        if (mLiveChatAdapter != null) {
            mLiveChatAdapter.scrollToBottom();
        }
    }

    /**
     * 用户进入房间 金光一闪,坐骑动画
     */
    public void onEnterRoom(LiveEnterRoomBean bean) {
        if (bean == null) {
            return;
        }
        if (mLiveEnterRoomAnimPresenter != null) {
            mLiveEnterRoomAnimPresenter.enterRoom(bean);
        }
    }

    /**
     * 显示弹幕
     */
    public void showDanmu(LiveDanMuBean bean) {
        if (mVotes != null) {
            mVotes.setText(bean.getVotes());
        }
        if (mLiveDanmuPresenter == null) {
            mLiveDanmuPresenter = new LiveDanmuPresenter(mContext, mParentView);
        }
        mLiveDanmuPresenter.showDanmu(bean);
    }

    /**
     * 显示主播的个人资料弹窗
     */
    private void showAnchorUserDialog() {
        if (TextUtils.isEmpty(mLiveUid)) {
            return;
        }
        showUserDialog(mLiveUid);
    }

    /**
     * 显示个人资料弹窗
     */
    public void showUserDialog(String toUid) {
        if (!TextUtils.isEmpty(mLiveUid) && !TextUtils.isEmpty(toUid)) {
            LiveUserDialogFragment fragment = new LiveUserDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constants.LIVE_UID, mLiveUid);
            bundle.putString(Constants.STREAM, mStream);
            bundle.putString(Constants.TO_UID, toUid);
            fragment.setArguments(bundle);
            fragment.show(((LiveActivity) mContext).getSupportFragmentManager(), "LiveUserDialogFragment");
        }
    }

    /**
     * 直播间贡献榜窗口
     */
    private void openContributeWindow() {
        ((LiveActivity) mContext).openContributeWindow();
    }


    /**
     * 显示礼物动画
     */
    public void showGiftMessage(LiveReceiveGiftBean bean) {
        mVotes.setText(bean.getVotes());
        if (mLiveGiftAnimPresenter == null) {
            mLiveGiftAnimPresenter = new LiveGiftAnimPresenter(mContext, mContentView, mGifImageView, mSVGAImageView, mLiveGiftPrizePoolContainer);
        }
        mLiveGiftAnimPresenter.showGiftAnim(bean);
    }

    /**
     * 增加主播映票数
     *
     * @param deltaVal 增加的映票数量
     */
    public void updateVotes(String deltaVal) {
        if (mVotes == null) {
            return;
        }
        String votesVal = mVotes.getText().toString().trim();
        if (TextUtils.isEmpty(votesVal)) {
            return;
        }
        try {
            double votes = Double.parseDouble(votesVal);
            double addVotes = Double.parseDouble(deltaVal);
            votes += addVotes;
            mVotes.setText(StringUtil.format(votes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ViewGroup getInnerContainer() {
        return (ViewGroup) findViewById(R.id.inner_container);
    }


    /**
     * 主播显示直播时间
     */
    public void startAnchorLiveTime() {
        if (mTaskScheduler != null) {
            mTaskScheduler.startTask(TASK_ANCHOR_LIVE_TIME, 0, 1000, new Runnable() {
                @Override
                public void run() {
                    if (mLiveTimeTextView != null) {
                        if (mStringBuilder == null) {
                            mStringBuilder = new StringBuilder();
                        }
                        mLiveTimeTextView.setText(StringUtil.getDurationText2(mAnchorLiveTime, mStringBuilder));
                    }
                    mAnchorLiveTime += 1;
                }
            });
        }
    }


    /**
     * 主播开始飘心
     */
    public void startAnchorLight() {
        if (mTaskScheduler != null) {
            mTaskScheduler.startTask(TASK_ANCHOR_LIGHT, 0, 500, new Runnable() {
                @Override
                public void run() {
                    playLightAnim();
                }
            });
        }
    }


    /**
     * 主播checkLive
     */
    public void startAnchorCheckLive() {
        if (mTaskScheduler != null) {
            mTaskScheduler.startTask(TASK_ANCHOR_CHECK_LIVE, 30000, 30000, new Runnable() {
                @Override
                public void run() {
                    if (mContext instanceof LiveAnchorActivity) {
                        ((LiveAnchorActivity) mContext).checkLive();
                    }
                }
            });
        }
    }


    /**
     * 主播切后台，50秒后关闭直播
     */
    public void anchorPause() {
        if (mTaskScheduler != null) {
            mTaskScheduler.startTask(TASK_ANCHOR_PAUSE, 50000, 0, new Runnable() {
                @Override
                public void run() {
                    anchorEndLive();
                }
            });
        }
    }

    /**
     * 主播切后台后又回到前台
     */
    public void anchorResume() {
        if (mTaskScheduler != null) {
            mTaskScheduler.cancelTask(TASK_ANCHOR_PAUSE);
        }
    }

    /**
     * 主播结束直播
     */
    private void anchorEndLive() {
        if (mContext != null && mContext instanceof LiveAnchorActivity) {
            ((LiveAnchorActivity) mContext).endLive();
        }
    }



    /**
     * 幸运礼物中奖
     */
    public void onLuckGiftWin(LiveLuckGiftWinBean bean) {
        if (mLiveGiftAnimPresenter == null) {
            mLiveGiftAnimPresenter = new LiveGiftAnimPresenter(mContext, mContentView, mGifImageView, mSVGAImageView, mLiveGiftPrizePoolContainer);
        }
        mLiveGiftAnimPresenter.showLuckGiftWinAnim(bean);
    }


    /**
     * 奖池中奖
     */
    public void onPrizePoolWin(LiveGiftPrizePoolWinBean bean) {
        if (mLiveGiftAnimPresenter == null) {
            mLiveGiftAnimPresenter = new LiveGiftAnimPresenter(mContext, mContentView, mGifImageView, mSVGAImageView, mLiveGiftPrizePoolContainer);
        }
        mLiveGiftAnimPresenter.showPrizePoolWinAnim(bean);
    }

    /**
     * 奖池升级
     */
    public void onPrizePoolUp(String level) {
//        if (mLiveGiftAnimPresenter == null) {
//            mLiveGiftAnimPresenter = new LiveGiftAnimPresenter(mContext, mContentView, mGifImageView, mSVGAImageView, mLiveGiftPrizePoolContainer);
//        }
//        mLiveGiftAnimPresenter.showPrizePoolUp(level);
    }


    /**
     * 全站礼物
     */
    public void onGlobalGift(GlobalGiftBean bean) {
        if (mLiveGiftAnimPresenter == null) {
            mLiveGiftAnimPresenter = new LiveGiftAnimPresenter(mContext, mContentView, mGifImageView, mSVGAImageView, mLiveGiftPrizePoolContainer);
        }
        mLiveGiftAnimPresenter.showGlobalGift(bean);
    }


    /**
     * 设置当前显示的直播间商品
     */
    public void setShowGoodsBean(GoodsBean bean) {
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            return;
        }
        mShowGoodsBean = bean;
        if (bean == null) {
            if (mGoodsThumb != null) {
                mGoodsThumb.setImageDrawable(null);
            }
            if (mGoodsName != null) {
                mGoodsName.setText(null);
            }
            if (mGoodsPrice != null) {
                mGoodsPrice.setText(null);
            }
            if (mGroupGoods != null && mGroupGoods.getVisibility() != View.GONE) {
                mGroupGoods.setVisibility(View.GONE);
            }
        } else {
            if (mGroupGoods != null && mGroupGoods.getVisibility() != View.VISIBLE) {
                mGroupGoods.setVisibility(View.VISIBLE);
            }
            if (mGoodsThumb != null) {
                ImgLoader.display(mContext, bean.getThumb(), mGoodsThumb);
            }
            if (mGoodsName != null) {
                mGoodsName.setText(bean.getName());
            }
            if (mGoodsPrice != null) {
                mGoodsPrice.setText(bean.getPriceNow());
            }
        }
    }

    /**
     * 直播间购物飘屏
     */
    public void onLiveGoodsFloat(String userName) {
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            return;
        }
        if (mLiveGiftAnimPresenter == null) {
            mLiveGiftAnimPresenter = new LiveGiftAnimPresenter(mContext, mContentView, mGifImageView, mSVGAImageView, mLiveGiftPrizePoolContainer);
        }
        mLiveGiftAnimPresenter.onLiveGoodsFloat(userName);
    }

    public void release() {
        LiveHttpUtil.cancel(LiveHttpConsts.GET_USER_LIST);
        LiveHttpUtil.cancel(LiveHttpConsts.TIME_CHARGE);
        CommonHttpUtil.cancel(CommonHttpConsts.SET_ATTENTION);
        if (mTaskScheduler != null) {
            mTaskScheduler.cancelAllTasks();
        }
        mTaskScheduler = null;
        if (mLightAnimPresenter != null) {
            mLightAnimPresenter.release();
        }
        if (mLiveEnterRoomAnimPresenter != null) {
            mLiveEnterRoomAnimPresenter.release();
        }
        if (mLiveGiftAnimPresenter != null) {
            mLiveGiftAnimPresenter.release();
        }
        mRefreshUserListCallback = null;
        mTimeChargeCallback = null;
        if (mBtnViewHolder != null) {
            mBtnViewHolder.release();
        }
        mBtnViewHolder = null;
    }

    public void clearData() {
        LiveHttpUtil.cancel(LiveHttpConsts.GET_USER_LIST);
        LiveHttpUtil.cancel(LiveHttpConsts.TIME_CHARGE);
        CommonHttpUtil.cancel(CommonHttpConsts.SET_ATTENTION);
        if (mTaskScheduler != null) {
            mTaskScheduler.cancelAllTasks();
        }
        if (mAvatar != null) {
            mAvatar.setImageDrawable(null);
        }
        if (mLevelAnchor != null) {
            mLevelAnchor.setImageDrawable(null);
        }
        if (mName != null) {
            mName.setText("");
        }
        if (mID != null) {
            mID.setText("");
        }
        if (mVotes != null) {
            mVotes.setText("");
        }
        if (mGuardNum != null) {
            mGuardNum.setText("");
        }
        if (mLiveUserAdapter != null) {
            mLiveUserAdapter.clear();
        }
        if (mLiveChatAdapter != null) {
            mLiveChatAdapter.clear();
        }
        if (mLiveEnterRoomAnimPresenter != null) {
            mLiveEnterRoomAnimPresenter.cancelAnim();
            mLiveEnterRoomAnimPresenter.resetAnimView();
        }
        if (mLiveDanmuPresenter != null) {
            mLiveDanmuPresenter.release();
            mLiveDanmuPresenter.reset();
        }
        if (mLiveGiftAnimPresenter != null) {
            mLiveGiftAnimPresenter.cancelAllAnim();
        }
        setShowGoodsBean(null);
    }
}
