package com.yunbao.main.views;

import android.content.Context;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.bean.LiveClassBean;
import com.yunbao.common.custom.CommonRefreshView;
import com.yunbao.common.custom.ItemDecoration;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.live.bean.LiveBean;
import com.yunbao.live.utils.LiveStorge;
import com.yunbao.main.R;
import com.yunbao.main.adapter.MainHomeFollowAdapter;
import com.yunbao.main.adapter.MainLiveClassAdapter;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/9/22.
 * 首页 关注
 */

public class MainHomeFollowViewHolder extends AbsMainHomeChildViewHolder implements OnItemClickListener<LiveBean> {

    private CommonRefreshView mRefreshView;
    private MainHomeFollowAdapter mAdapter;
    private int mLiveClassId;
    private List<LiveBean> mEmptyList;
    private View mNoDataView;
    private View mGroupHasLogin;//已登录
    private View mGroupNotLogin;//未登录


    public MainHomeFollowViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_main_home_follow;
    }

    @Override
    public void init() {
        RecyclerView classRecyclerView = findViewById(R.id.recyclerView_class);
        classRecyclerView.setHasFixedSize(true);
        classRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        MainLiveClassAdapter classAdapter = new MainLiveClassAdapter(mContext);
        classAdapter.setOnItemClickListener(new OnItemClickListener<LiveClassBean>() {
            @Override
            public void onItemClick(LiveClassBean bean, int position) {
                mLiveClassId = bean.getId();
                if (mRefreshView != null) {
                    mRefreshView.initData();
                }
            }
        });
        classRecyclerView.setAdapter(classAdapter);
        mRefreshView = (CommonRefreshView) findViewById(R.id.refreshView);
        mNoDataView = LayoutInflater.from(mContext).inflate(R.layout.view_no_data_live_follow, mRefreshView.getmEmptyLayout(), false);
        mGroupHasLogin = mNoDataView.findViewById(R.id.group_has_login);
        mGroupNotLogin = mNoDataView.findViewById(R.id.group_not_login);
        mGroupNotLogin.findViewById(R.id.btn_to_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouteUtil.forwardLogin(mContext);
            }
        });
        mRefreshView.setEmptyLayoutView(mNoDataView);
        mRefreshView.setLayoutManager(new GridLayoutManager(mContext, 2, GridLayoutManager.VERTICAL, false));
        ItemDecoration decoration = new ItemDecoration(mContext, 0x00000000, 5, 0);
        decoration.setOnlySetItemOffsetsButNoDraw(true);
        mRefreshView.setItemDecoration(decoration);
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<LiveBean>() {
            @Override
            public RefreshAdapter<LiveBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new MainHomeFollowAdapter(mContext);
                    mAdapter.setOnItemClickListener(MainHomeFollowViewHolder.this);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (CommonAppConfig.getInstance().isLogin()) {
                    if (mGroupHasLogin != null && mGroupHasLogin.getVisibility() != View.VISIBLE) {
                        mGroupHasLogin.setVisibility(View.VISIBLE);
                    }
                    if (mGroupNotLogin != null && mGroupNotLogin.getVisibility() == View.VISIBLE) {
                        mGroupNotLogin.setVisibility(View.INVISIBLE);
                    }
                    MainHttpUtil.getFollow(p, mLiveClassId, callback);
                } else {
                    if (mGroupNotLogin != null && mGroupNotLogin.getVisibility() != View.VISIBLE) {
                        mGroupNotLogin.setVisibility(View.VISIBLE);
                    }
                    if (mGroupHasLogin != null && mGroupHasLogin.getVisibility() == View.VISIBLE) {
                        mGroupHasLogin.setVisibility(View.INVISIBLE);
                    }
                    callback.onSuccess(Constants.ZERO, Constants.EMPTY_STRING, Constants.EMPTY_STRING_ARR);
                    callback.onFinish();
                }
            }

            @Override
            public List<LiveBean> processData(String[] info) {
                if (CommonAppConfig.getInstance().isLogin()) {
                    if (info.length > 0) {
                        JSONObject obj = JSON.parseObject(info[0]);
                        return JSON.parseArray(obj.getString("list"), LiveBean.class);
                    }
                }
                if (mEmptyList == null) {
                    mEmptyList = new ArrayList<>();
                }
                return mEmptyList;
            }

            @Override
            public void onRefreshSuccess(List<LiveBean> adapterItemList, int allItemCount) {
                if (CommonAppConfig.LIVE_ROOM_SCROLL) {
                    LiveStorge.getInstance().put(Constants.LIVE_FOLLOW, adapterItemList);
                }
            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<LiveBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
    }

    @Override
    public void loadData() {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }

    @Override
    public void onItemClick(LiveBean bean, int position) {
        if(CommonAppConfig.getInstance().isBaseFunctionMode()){
            return;
        }
        watchLive(bean, Constants.LIVE_FOLLOW, position);
    }


    @Override
    public void release() {
        MainHttpUtil.cancel(MainHttpConsts.GET_FOLLOW);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }
}
