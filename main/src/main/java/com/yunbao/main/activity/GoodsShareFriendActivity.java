package com.yunbao.main.activity;

import android.app.Dialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.custom.CommonRefreshView;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.im.utils.ImMessageUtil;
import com.yunbao.main.R;
import com.yunbao.main.adapter.GoodsShareAdapter;
import com.yunbao.main.bean.GoodsShareUserBean;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GoodsShareFriendActivity extends AbsActivity implements View.OnClickListener {

    private CommonRefreshView mRefreshView;
    private GoodsShareAdapter mAdapter;
    private String mGoodsId;
    private Dialog mLoading;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_goods_share_friend;
    }

    @Override
    protected void main() {
        mGoodsId = getIntent().getStringExtra(Constants.MALL_GOODS_ID);
        findViewById(R.id.btn_share).setOnClickListener(this);
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_follow);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<GoodsShareUserBean>() {
            @Override
            public RefreshAdapter<GoodsShareUserBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new GoodsShareAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                MainHttpUtil.getFollowList(CommonAppConfig.getInstance().getUid(), p, callback);
            }

            @Override
            public List<GoodsShareUserBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), GoodsShareUserBean.class);
            }

            @Override
            public void onRefreshSuccess(List<GoodsShareUserBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<GoodsShareUserBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
        mRefreshView.initData();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_share) {
            share();
        }
    }

    private void share() {
        if (TextUtils.isEmpty(mGoodsId) || mAdapter == null) {
            return;
        }
        List<GoodsShareUserBean> list = mAdapter.getList();
        if (list == null || list.size() == 0) {
            return;
        }
        List<String> toUids = null;
        for (GoodsShareUserBean bean : list) {
            if (bean.isChecked()) {
                if (toUids == null) {
                    toUids = new ArrayList<>();
                }
                toUids.add(bean.getId());
            }
        }
        if (toUids != null && toUids.size() > 0) {
            if (mLoading == null) {
                mLoading = DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.a_074));
            }
            mLoading.show();
            JSONObject obj=new JSONObject();
            obj.put("method", Constants.IM_CUSTOM_METHOD_GOODS);
            obj.put("goodsid", mGoodsId);
            ImMessageUtil.getInstance().sendGoodsMessage(toUids, obj.toJSONString(), new Runnable() {
                @Override
                public void run() {
                    if (mLoading != null && mLoading.isShowing()) {
                        mLoading.dismiss();
                        mLoading = null;
                    }
                    finish();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (mLoading != null && mLoading.isShowing()) {
            mLoading.dismiss();
            mLoading = null;
        }
        MainHttpUtil.cancel(MainHttpConsts.GET_FOLLOW_LIST);
        super.onDestroy();
    }
}
