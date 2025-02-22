package com.yunbao.main.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.HtmlConfig;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.activity.WebViewActivity;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.bean.LevelBean;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.bean.UserItemBean;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.CommonIconUtil;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.im.activity.ChatActivity;
import com.yunbao.im.utils.ImMessageUtil;
import com.yunbao.im.utils.ImUnReadCount;
import com.yunbao.live.activity.LiveRecordActivity;
import com.yunbao.live.activity.LuckPanRecordActivity;
import com.yunbao.live.activity.RoomManageActivity;
import com.yunbao.main.R;
import com.yunbao.main.activity.DailyTaskActivity;
import com.yunbao.main.activity.EditProfileActivity;
import com.yunbao.main.activity.FamilyActivity;
import com.yunbao.main.activity.FansActivity;
import com.yunbao.main.activity.FollowActivity;
import com.yunbao.main.activity.MainActivity;
import com.yunbao.main.activity.MyActiveActivity;
import com.yunbao.main.activity.MyProfitActivity;
import com.yunbao.main.activity.MyVideoActivity;
import com.yunbao.main.activity.SettingActivity;
import com.yunbao.main.activity.TeenagerActivity;
import com.yunbao.main.activity.ThreeDistributActivity;
import com.yunbao.main.adapter.MainMeAdapter;
import com.yunbao.main.adapter.MainMeAdapter2;
import com.yunbao.main.dialog.UserAvatarPreviewDialog;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;
import com.yunbao.mall.activity.BuyerActivity;
import com.yunbao.mall.activity.GoodsCollectActivity;
import com.yunbao.mall.activity.PayContentActivity1;
import com.yunbao.mall.activity.PayContentActivity2;
import com.yunbao.mall.activity.SellerActivity;

import java.util.List;

/**
 * Created by cxf on 2018/9/22.
 * 我的
 */

public class MainMeViewHolder extends AbsMainViewHolder implements OnItemClickListener<UserItemBean>, View.OnClickListener {

    //    private AppBarLayout mAppBarLayout;
    private ImageView mAvatar;
    private TextView mName;
    private ImageView mSex;
    private ImageView mLevelAnchor;
    private ImageView mLevel;
    private TextView mID;
    private TextView mFollowNum;
    private TextView mFansNum;
    private TextView mCollectNum;
    private boolean mPaused;
    private RecyclerView mRecyclerView1;
    private RecyclerView mRecyclerView2;

    private MainMeAdapter mAdapter1;
    private MainMeAdapter2 mAdapter2;
    private TextView mRedPoint;//显示未读消息数量的红点
    private TextView mCoin;

//    private ImageView mVipImg;
    private TextView mVipTip;
    private TextView mBtnVip;

    private View mGroupVip;
    private View mGroupCharge;
    private View mGroupMyService;
    private String mAvatarUrl;
    private String mCoinName;


    public MainMeViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_me;
    }

    @Override
    public void init() {
        setStatusHeight();
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mAvatar.setOnClickListener(this);
        mName = (TextView) findViewById(R.id.name);
        mSex = (ImageView) findViewById(R.id.sex);
        mLevelAnchor = (ImageView) findViewById(R.id.level_anchor);
        mLevel = (ImageView) findViewById(R.id.level);
        mID = (TextView) findViewById(R.id.id_val);
        mFollowNum = findViewById(R.id.follow_num);
        mFansNum = findViewById(R.id.fans_num);
        mCollectNum = findViewById(R.id.collect_num);
        mRedPoint = (TextView) findViewById(R.id.red_point);
        mCoin = (TextView) findViewById(R.id.coin);
        ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
        if (configBean != null) {
            mCoinName=configBean.getCoinName();
        }
        findViewById(R.id.btn_follow).setOnClickListener(this);
        findViewById(R.id.btn_fans).setOnClickListener(this);
        findViewById(R.id.btn_collect).setOnClickListener(this);
        findViewById(R.id.btn_edit).setOnClickListener(this);
        findViewById(R.id.btn_msg).setOnClickListener(this);
        findViewById(R.id.btn_wallet).setOnClickListener(this);
        findViewById(R.id.btn_detail).setOnClickListener(this);
        findViewById(R.id.btn_setting).setOnClickListener(this);
        findViewById(R.id.btn_auth).setOnClickListener(this);
        mBtnVip = findViewById(R.id.btn_vip);
        mBtnVip.setOnClickListener(this);

//        mVipImg = findViewById(R.id.vip_img);
        mVipTip = findViewById(R.id.vip_tip);

        mRecyclerView1 = findViewById(R.id.recyclerView1);
        mRecyclerView1.setLayoutManager(new GridLayoutManager(mContext, 5, GridLayoutManager.VERTICAL, false));
        mRecyclerView2 = findViewById(R.id.recyclerView2);
        mRecyclerView2.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

        mGroupVip = findViewById(R.id.group_vip);
        mGroupCharge = findViewById(R.id.group_charge);
        mGroupMyService = findViewById(R.id.group_my_service);

        ImUnReadCount unReadCount = ImMessageUtil.getInstance().getUnReadMsgCount();
        String totalUnReadCount =
                unReadCount != null ? unReadCount.getTotalUnReadCount() : "0";
        setUnReadCount(totalUnReadCount);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isShowed() && mPaused) {
            loadData();
        }
        mPaused = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainHttpUtil.cancel(MainHttpConsts.GET_BASE_INFO);
    }

    @Override
    public void loadData() {
        if (CommonAppConfig.getInstance().isLogin()) {
            if (isFirstLoadData()) {
                showData1();
            }
            MainHttpUtil.getBaseInfo(mCallback);
        } else {
            if (mContext != null) {
                ((MainActivity) mContext).setCurrentPage(0);
            }
        }
    }

    private CommonCallback<UserBean> mCallback = new CommonCallback<UserBean>() {
        @Override
        public void callback(UserBean bean) {
            showData();
        }
    };

    private void showData1() {
        String userBeanJson = SpUtil.getInstance().getStringValue(SpUtil.USER_INFO);
        if (TextUtils.isEmpty(userBeanJson)) {
            return;
        }
        JSONObject obj = JSON.parseObject(userBeanJson);
        UserBean u = JSON.toJavaObject(obj, UserBean.class);
        mAvatarUrl = u.getAvatar();
        ImgLoader.displayAvatar(mContext, mAvatarUrl, mAvatar);
        mName.setText(u.getUserNiceName());
        mSex.setImageResource(CommonIconUtil.getSexIcon(u.getSex()));
        CommonAppConfig appConfig = CommonAppConfig.getInstance();
        LevelBean anchorLevelBean = appConfig.getAnchorLevel(u.getLevelAnchor());
        if (anchorLevelBean != null) {
            ImgLoader.display(mContext, anchorLevelBean.getThumb(), mLevelAnchor);
        }
        LevelBean levelBean = appConfig.getLevel(u.getLevel());
        if (levelBean != null) {
            ImgLoader.display(mContext, levelBean.getThumb(), mLevel);
        }
        mID.setText(u.getLiangNameTip());
        mFollowNum.setText(StringUtil.toWan(u.getFollows()));
        mFansNum.setText(StringUtil.toWan(u.getFans()));
        mCollectNum.setText(StringUtil.toWan(obj.getIntValue("goods_collect_nums")));
    }

    private void showData() {
        String userBeanJson = SpUtil.getInstance().getStringValue(SpUtil.USER_INFO);
        if (TextUtils.isEmpty(userBeanJson)) {
            return;
        }
        JSONObject obj = JSON.parseObject(userBeanJson);
        UserBean u = JSON.toJavaObject(obj, UserBean.class);
        mAvatarUrl = u.getAvatar();
        ImgLoader.displayAvatar(mContext, mAvatarUrl, mAvatar);
        mName.setText(u.getUserNiceName());
        mSex.setImageResource(CommonIconUtil.getSexIcon(u.getSex()));
        CommonAppConfig appConfig = CommonAppConfig.getInstance();
        LevelBean anchorLevelBean = appConfig.getAnchorLevel(u.getLevelAnchor());
        if (anchorLevelBean != null) {
            ImgLoader.display(mContext, anchorLevelBean.getThumb(), mLevelAnchor);
        }
        LevelBean levelBean = appConfig.getLevel(u.getLevel());
        if (levelBean != null) {
            ImgLoader.display(mContext, levelBean.getThumb(), mLevel);
        }
        mID.setText(u.getLiangNameTip());
        mFollowNum.setText(StringUtil.toWan(u.getFollows()));
        mFansNum.setText(StringUtil.toWan(u.getFans()));
        mCollectNum.setText(StringUtil.toWan(obj.getIntValue("goods_collect_nums")));

        JSONArray arr = obj.getJSONArray("list");

        if (CommonAppConfig.getInstance().isTeenagerType()) {
            if (mGroupVip.getVisibility() != View.GONE) {
                mGroupVip.setVisibility(View.GONE);
            }
            if (mGroupCharge.getVisibility() != View.GONE) {
                mGroupCharge.setVisibility(View.GONE);
            }
            if (mGroupMyService.getVisibility() != View.GONE) {
                mGroupMyService.setVisibility(View.GONE);
            }

            JSONObject obj2 = arr.getJSONObject(0);
            List<UserItemBean> list2 = JSON.parseArray(obj2.getString("list"), UserItemBean.class);
            if (mAdapter2 == null) {
                mAdapter2 = new MainMeAdapter2(mContext, list2);
                mAdapter2.setOnItemClickListener(this);
                mRecyclerView2.setAdapter(mAdapter2);
            } else {
                mAdapter2.setList(list2);
            }

        } else {
            if (mGroupVip.getVisibility() != View.VISIBLE) {
                mGroupVip.setVisibility(View.VISIBLE);
            }
            if (mGroupCharge.getVisibility() != View.VISIBLE) {
                mGroupCharge.setVisibility(View.VISIBLE);
            }
            if (mGroupMyService.getVisibility() != View.VISIBLE) {
                mGroupMyService.setVisibility(View.VISIBLE);
            }
            String coin=u.getCoin();
            String content=coin+mCoinName;
            SpannableString spannableString = new SpannableString(content);
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, coin.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mCoin.setText(spannableString);

            JSONObject vipInfo = obj.getJSONObject("vip");
            if (vipInfo.getIntValue("type") != 0) {
                if (mBtnVip != null) {
                    mBtnVip.setText(R.string.a_113);
                }
                if (mVipTip != null) {
                    mVipTip.setText(String.format(WordUtil.getString(R.string.a_114), vipInfo.getString("endtime")));
                }
//                if (mVipImg != null) {
//                    mVipImg.setImageResource(R.mipmap.icon_main_me_vip_4);
//                }
            } else {
                if (mBtnVip != null) {
                    mBtnVip.setText(R.string.guard_buy);
                }
                if (mVipTip != null) {
                    mVipTip.setText(R.string.a_115);
                }
//                if (mVipImg != null) {
//                    mVipImg.setImageResource(R.mipmap.icon_main_me_vip_3);
//                }
            }

            JSONObject obj1 = arr.getJSONObject(0);
            List<UserItemBean> list1 = JSON.parseArray(obj1.getString("list"), UserItemBean.class);
            if (mAdapter1 == null) {
                mAdapter1 = new MainMeAdapter(mContext, list1);
                mAdapter1.setOnItemClickListener(this);
                mRecyclerView1.setAdapter(mAdapter1);
            } else {
                mAdapter1.setList(list1);
            }
            JSONObject obj2 = arr.getJSONObject(1);
            List<UserItemBean> list2 = JSON.parseArray(obj2.getString("list"), UserItemBean.class);
            if (mAdapter2 == null) {
                mAdapter2 = new MainMeAdapter2(mContext, list2);
                mAdapter2.setOnItemClickListener(this);
                mRecyclerView2.setAdapter(mAdapter2);
            } else {
                mAdapter2.setList(list2);
            }

        }
    }


    @Override
    public void onItemClick(UserItemBean bean, int position) {
        if (bean.getId() == 22) {//我的小店
            forwardMall();
            return;
        } else if (bean.getId() == 24) {//付费内容
            forwardPayContent();
            return;
        }
        String url = bean.getHref();
        if (TextUtils.isEmpty(url)) {
            switch (bean.getId()) {
                case 1:
                    forwardProfit();
                    break;
                case 2:
                    forwardCoin();
                    break;
                case 13:
                    forwardSetting();
                    break;
                case 19:
                    forwardMyVideo();
                    break;
                case 20:
                    forwardRoomManage();
                    break;
                case 23://我的动态
                    mContext.startActivity(new Intent(mContext, MyActiveActivity.class));
                    break;
                case 25://每日任务
                    mContext.startActivity(new Intent(mContext, DailyTaskActivity.class));
                    break;
                case 26://中奖记录
                    luckPanRecord();
                    break;
                case 27://青少年模式
                    TeenagerActivity.forward(mContext);
                    break;
            }
        } else {
            if (bean.getId() == 8) {//三级分销
                ThreeDistributActivity.forward(mContext, bean.getName(), url);
            } else if (bean.getId() == 6) {//家族中心
                FamilyActivity.forward(mContext, url);
            } else {
                WebViewActivity.forward(mContext, url);
            }
        }
    }

    /**
     * 我的小店 商城
     */
    private void forwardMall() {
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u != null) {
            if (u.getIsOpenShop() == 0) {
                BuyerActivity.forward(mContext);
            } else {
                SellerActivity.forward(mContext);
            }
        }

    }


    /**
     * 付费内容
     */
    private void forwardPayContent() {
        UserBean u = CommonAppConfig.getInstance().getUserBean();
        if (u != null) {
            if (u.getIsOpenPayContent() == 0) {
                PayContentActivity1.forward(mContext);
            } else {
                PayContentActivity2.forward(mContext);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_edit) {
            RouteUtil.forwardUserHome(mContext, CommonAppConfig.getInstance().getUid());
        } else if (i == R.id.btn_follow) {
            forwardFollow();

        } else if (i == R.id.btn_fans) {
            forwardFans();

        } else if (i == R.id.btn_collect) {
            if (CommonAppConfig.getInstance().isTeenagerType()) {
                ToastUtil.show(com.yunbao.common.R.string.a_137);
            } else {
                mContext.startActivity(new Intent(mContext, GoodsCollectActivity.class));
            }

        } else if (i == R.id.btn_msg) {
            ChatActivity.forward(mContext);
        } else if (i == R.id.btn_wallet) {
            RouteUtil.forwardMyCoin(mContext);
        } else if (i == R.id.btn_detail) {
            WebViewActivity.forward(mContext, HtmlConfig.DETAIL);
        } else if (i == R.id.btn_vip) {
            WebViewActivity.forward(mContext, HtmlConfig.SHOP);
        } else if (i == R.id.btn_setting) {
            forwardSetting();
        } else if (i == R.id.btn_auth) {
            WebViewActivity.forward(mContext, HtmlConfig.AUTH);
        } else if (i == R.id.avatar) {
            showAvatarDialog();
        }
    }

    private void showAvatarDialog() {
        UserAvatarPreviewDialog dialog = new UserAvatarPreviewDialog();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.URL, mAvatarUrl);
        dialog.setArguments(bundle);
        dialog.show(((AbsActivity) mContext).getSupportFragmentManager(), "UserAvatarPreviewDialog");
    }

    /**
     * 编辑个人资料
     */
    private void forwardEditProfile() {
        mContext.startActivity(new Intent(mContext, EditProfileActivity.class));
    }

    /**
     * 我的关注
     */
    private void forwardFollow() {
        FollowActivity.forward(mContext, CommonAppConfig.getInstance().getUid());
    }

    /**
     * 我的粉丝
     */
    private void forwardFans() {
        FansActivity.forward(mContext, CommonAppConfig.getInstance().getUid());
    }

    /**
     * 直播记录
     */
    private void forwardLiveRecord() {
        LiveRecordActivity.forward(mContext, CommonAppConfig.getInstance().getUserBean());
    }

    /**
     * 我的收益
     */
    private void forwardProfit() {
        mContext.startActivity(new Intent(mContext, MyProfitActivity.class));
    }

    /**
     * 我的钻石
     */
    private void forwardCoin() {
        RouteUtil.forwardMyCoin(mContext);
    }

    /**
     * 设置
     */
    private void forwardSetting() {
        mContext.startActivity(new Intent(mContext, SettingActivity.class));
    }

    /**
     * 我的视频
     */
    private void forwardMyVideo() {
        mContext.startActivity(new Intent(mContext, MyVideoActivity.class));
    }

    /**
     * 房间管理
     */
    private void forwardRoomManage() {
        mContext.startActivity(new Intent(mContext, RoomManageActivity.class));
    }

    /**
     * 转盘中奖记录
     */
    private void luckPanRecord() {
        mContext.startActivity(new Intent(mContext, LuckPanRecordActivity.class));
    }


    /**
     * 显示未读消息
     */
    public void setUnReadCount(String unReadCount) {
        if (mRedPoint != null) {
            if ("0".equals(unReadCount)) {
                if (mRedPoint.getVisibility() == View.VISIBLE) {
                    mRedPoint.setVisibility(View.INVISIBLE);
                }
            } else {
                if (mRedPoint.getVisibility() != View.VISIBLE) {
                    mRedPoint.setVisibility(View.VISIBLE);
                }
                mRedPoint.setText(unReadCount);
            }
        }
    }


}
