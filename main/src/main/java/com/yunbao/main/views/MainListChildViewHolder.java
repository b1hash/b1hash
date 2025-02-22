package com.yunbao.main.views;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.custom.MyRadioButton;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.main.R;
import com.yunbao.main.adapter.MainListAdapter;
import com.yunbao.main.bean.ListBean;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2019/2/23.
 */

public class MainListChildViewHolder extends AbsMainViewHolder implements View.OnClickListener {
    public static final int TYPE_PROFIT = 1;//收益榜
    public static final int TYPE_CONTRIBUTE = 0;//贡献榜
    private int mType;
    private static final String DAY = "day";
    private static final String WEEK = "week";
    private static final String MONTH = "month";
    private static final String TOTAL = "total";
    private String mDataType = DAY;
    private SmartRefreshLayout mSmartRefreshLayout;
    private ImageView mAvatar1;
    private ImageView mAvatar2;
    private ImageView mAvatar3;
    private TextView mName1;
    private TextView mName2;
    private TextView mName3;
    private TextView mVotes1;
    private TextView mVotes2;
    private TextView mVotes3;
    private MyRadioButton mBtnFollow1;
    private MyRadioButton mBtnFollow2;
    private MyRadioButton mBtnFollow3;
    private View mDataGroup1;
    private View mDataGroup2;
    private View mDataGroup3;
    private RecyclerView mRecyclerView;
    private View mNoDataTip;
    protected MainListAdapter mAdapter;
    private String mCoinName;
    private String mFollow;
    private String mFollowing;
    private ListBean mTop1;
    private ListBean mTop2;
    private ListBean mTop3;

    public MainListChildViewHolder(Context context, ViewGroup parentView, int type) {
        super(context, parentView, type);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_list_page;
    }

    @Override
    protected void processArguments(Object... args) {
        mType = (int) args[0];
    }

    @Override
    public void init() {
        findViewById(R.id.btn_day).setOnClickListener(this);
        findViewById(R.id.btn_week).setOnClickListener(this);
        findViewById(R.id.btn_month).setOnClickListener(this);
        findViewById(R.id.btn_total).setOnClickListener(this);
        mSmartRefreshLayout = (SmartRefreshLayout) findViewById(com.yunbao.common.R.id.refreshLayout);
        mSmartRefreshLayout.setEnableRefresh(true);
        mSmartRefreshLayout.setEnableLoadMore(false);
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshData();
            }
        });
        findViewById(R.id.item_1).setOnClickListener(this);
        findViewById(R.id.item_2).setOnClickListener(this);
        findViewById(R.id.item_3).setOnClickListener(this);
        mAvatar1 = (ImageView) findViewById(R.id.avatar_1);
        mAvatar2 = (ImageView) findViewById(R.id.avatar_2);
        mAvatar3 = (ImageView) findViewById(R.id.avatar_3);
        mName1 = (TextView) findViewById(R.id.name_1);
        mName2 = (TextView) findViewById(R.id.name_2);
        mName3 = (TextView) findViewById(R.id.name_3);
        mVotes1 = (TextView) findViewById(R.id.votes_1);
        mVotes2 = (TextView) findViewById(R.id.votes_2);
        mVotes3 = (TextView) findViewById(R.id.votes_3);
        mBtnFollow1 = (MyRadioButton) findViewById(R.id.btn_follow_1);
        mBtnFollow2 = (MyRadioButton) findViewById(R.id.btn_follow_2);
        mBtnFollow3 = (MyRadioButton) findViewById(R.id.btn_follow_3);
        mBtnFollow1.setOnClickListener(this);
        mBtnFollow2.setOnClickListener(this);
        mBtnFollow3.setOnClickListener(this);
        mDataGroup1 = findViewById(R.id.data_group_1);
        mDataGroup2 = findViewById(R.id.data_group_2);
        mDataGroup3 = findViewById(R.id.data_group_3);
        mNoDataTip = findViewById(R.id.no_data_tip);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        CommonAppConfig appConfig = CommonAppConfig.getInstance();
        mCoinName = mType == TYPE_PROFIT ? appConfig.getVotesName() : appConfig.getCoinName();
        mAdapter = new MainListAdapter(mContext, mCoinName);
        mRecyclerView.setAdapter(mAdapter);
        mFollow = WordUtil.getString(R.string.follow);
        mFollowing = WordUtil.getString(R.string.following);
    }

    private void showTop() {
        if (mTop1 != null) {
            if (mDataGroup1 != null && mDataGroup1.getVisibility() != View.VISIBLE) {
                mDataGroup1.setVisibility(View.VISIBLE);
            }
            if (mAvatar1 != null) {
                ImgLoader.display(mContext, mTop1.getAvatarThumb(), mAvatar1);
            }
            if (mName1 != null) {
                mName1.setText(mTop1.getUserNiceName());
            }
            if (mVotes1 != null) {
                mVotes1.setText(StringUtil.contact(mTop1.getTotalCoinFormat(), " ", mCoinName));
            }
            if (mBtnFollow1 != null) {
                if (mTop1.getAttention() == 1) {
                    mBtnFollow1.doChecked(true);
                    mBtnFollow1.setText(mFollowing);
                } else {
                    mBtnFollow1.doChecked(false);
                    mBtnFollow1.setText(mFollow);
                }
            }
        } else {
            if (mDataGroup1 != null && mDataGroup1.getVisibility() != View.INVISIBLE) {
                mDataGroup1.setVisibility(View.INVISIBLE);
            }
        }
        if (mTop2 != null) {
            if (mDataGroup2 != null && mDataGroup2.getVisibility() != View.VISIBLE) {
                mDataGroup2.setVisibility(View.VISIBLE);
            }
            if (mAvatar2 != null) {
                ImgLoader.display(mContext, mTop2.getAvatarThumb(), mAvatar2);
            }
            if (mName2 != null) {
                mName2.setText(mTop2.getUserNiceName());
            }
            if (mVotes2 != null) {
                mVotes2.setText(StringUtil.contact(mTop2.getTotalCoinFormat(), " ", mCoinName));
            }
            if (mBtnFollow2 != null) {
                if (mTop2.getAttention() == 1) {
                    mBtnFollow2.doChecked(true);
                    mBtnFollow2.setText(mFollowing);
                } else {
                    mBtnFollow2.doChecked(false);
                    mBtnFollow2.setText(mFollow);
                }
            }
        } else {
            if (mDataGroup2 != null && mDataGroup2.getVisibility() != View.INVISIBLE) {
                mDataGroup2.setVisibility(View.INVISIBLE);
            }
        }

        if (mTop3 != null) {
            if (mDataGroup3 != null && mDataGroup3.getVisibility() != View.VISIBLE) {
                mDataGroup3.setVisibility(View.VISIBLE);
            }
            if (mAvatar3 != null) {
                ImgLoader.display(mContext, mTop3.getAvatarThumb(), mAvatar3);
            }
            if (mName3 != null) {
                mName3.setText(mTop3.getUserNiceName());
            }
            if (mVotes3 != null) {
                mVotes3.setText(StringUtil.contact(mTop3.getTotalCoinFormat(), " ", mCoinName));
            }
            if (mBtnFollow3 != null) {
                if (mTop3.getAttention() == 1) {
                    mBtnFollow3.doChecked(true);
                    mBtnFollow3.setText(mFollowing);
                } else {
                    mBtnFollow3.doChecked(false);
                    mBtnFollow3.setText(mFollow);
                }
            }
        } else {
            if (mDataGroup3 != null && mDataGroup3.getVisibility() != View.INVISIBLE) {
                mDataGroup3.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void loadData() {
        if (!isFirstLoadData()) {
            return;
        }
        refreshData();
    }

    private void refreshData() {
        HttpCallback httpCallback = new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<ListBean> list = JSON.parseArray(Arrays.toString(info), ListBean.class);
                    if (list == null || list.size() == 0) {
                        if (mNoDataTip != null && mNoDataTip.getVisibility() != View.VISIBLE) {
                            mNoDataTip.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (mNoDataTip != null && mNoDataTip.getVisibility() != View.INVISIBLE) {
                            mNoDataTip.setVisibility(View.INVISIBLE);
                        }
                    }
                    int size = list.size();
                    if (size > 0) {
                        mTop1 = list.get(0);
                    } else {
                        mTop1 = null;
                    }
                    if (size > 1) {
                        mTop2 = list.get(1);
                    } else {
                        mTop2 = null;
                    }
                    if (size > 2) {
                        mTop3 = list.get(2);
                    } else {
                        mTop3 = null;
                    }
                    showTop();
                    if (size > 3) {
                        List<ListBean> list2 = list.subList(3, list.size());
                        if (mAdapter != null) {
                            mAdapter.refreshData(list2);
                        }
                    } else {
                        if (mAdapter != null) {
                            mAdapter.clearData();
                        }
                    }
                }
            }

            @Override
            public void onFinish() {
                if (mSmartRefreshLayout != null) {
                    mSmartRefreshLayout.finishRefresh(true);
                }
            }
        };
        if (mType == TYPE_PROFIT) {
            MainHttpUtil.profitList(mDataType, 1, httpCallback);
        } else {
            MainHttpUtil.consumeList(mDataType, 1, httpCallback);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainHttpUtil.cancel(MainHttpConsts.PROFIT_LIST);
        MainHttpUtil.cancel(MainHttpConsts.CONSUME_LIST);
        CommonHttpUtil.cancel(CommonHttpConsts.SET_ATTENTION);
    }

    private boolean isFollowChanged(ListBean bean, MyRadioButton radioButton, String touid, int isAttention) {
        if (bean != null && touid.equals(bean.getUid())) {
            bean.setAttention(isAttention);
            if (radioButton != null) {
                if (isAttention == 1) {
                    radioButton.doChecked(true);
                    radioButton.setText(mFollowing);
                } else {
                    radioButton.doChecked(false);
                    radioButton.setText(mFollow);
                }
            }
            return true;
        }
        return false;
    }

    public void onFollowEvent(String touid, int isAttention) {
        if (TextUtils.isEmpty(touid)) {
            return;
        }
        if (isFollowChanged(mTop1, mBtnFollow1, touid, isAttention)) {
            return;
        }
        if (isFollowChanged(mTop2, mBtnFollow2, touid, isAttention)) {
            return;
        }
        if (isFollowChanged(mTop3, mBtnFollow3, touid, isAttention)) {
            return;
        }
        if (mAdapter != null) {
            mAdapter.updateItem(touid, isAttention);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_day) {
            toggleDataType(DAY);
        } else if (i == R.id.btn_week) {
            toggleDataType(WEEK);
        } else if (i == R.id.btn_month) {
            toggleDataType(MONTH);
        } else if (i == R.id.btn_total) {
            toggleDataType(TOTAL);
        } else if (i == R.id.item_1) {
            forwardHome(mTop1);
        } else if (i == R.id.item_2) {
            forwardHome(mTop2);
        } else if (i == R.id.item_3) {
            forwardHome(mTop3);
        } else if (i == R.id.btn_follow_1) {
            follow(mTop1);
        } else if (i == R.id.btn_follow_2) {
            follow(mTop2);
        } else if (i == R.id.btn_follow_3) {
            follow(mTop3);
        }
    }

    private void toggleDataType(String dataType) {
        if (!TextUtils.isEmpty(dataType) && !dataType.equals(mDataType)) {
            mDataType = dataType;
            refreshData();
        }
    }

    private void forwardHome(ListBean bean) {
        if (bean != null) {
            if (canClick()) {
                RouteUtil.forwardUserHome(mContext, bean.getUid());
            }
        }
    }

    private void follow(ListBean bean) {
        if (bean != null) {
            if (canClick()) {
                CommonHttpUtil.setAttention(bean.getUid(), null);
            }
        }
    }


}
