package com.yunbao.mall.views;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.adapter.RefreshAdapter;
import com.yunbao.common.custom.CommonRefreshView;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.views.AbsCommonViewHolder;
import com.yunbao.mall.R;
import com.yunbao.mall.activity.PayContentActivity2;
import com.yunbao.mall.activity.PayContentPubActivity;
import com.yunbao.mall.adapter.PayPubAdapter;
import com.yunbao.mall.bean.PayContentBean;
import com.yunbao.mall.http.MallHttpConsts;
import com.yunbao.mall.http.MallHttpUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 我上传的
 */
public class PayPubViewHolder extends AbsCommonViewHolder implements View.OnClickListener {

    private CommonRefreshView mRefreshView;
    private PayPubAdapter mAdapter;
    private String mToUid;
    private boolean mShowBtnPub;

    public PayPubViewHolder(Context context, ViewGroup parentView, String toUid, boolean showBtnPub) {
        super(context, parentView, toUid, showBtnPub);
    }

    @Override
    protected void processArguments(Object... args) {
        if (args.length > 0) {
            mToUid = (String) args[0];
        }
        if (args.length > 1) {
            mShowBtnPub = (boolean) args[1];
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_pay_pub;
    }

    @Override
    public void init() {
        if (mShowBtnPub) {
            View btnPub = findViewById(R.id.btn_pub);
            btnPub.setVisibility(View.VISIBLE);
            btnPub.setOnClickListener(this);
        }
        mRefreshView = findViewById(R.id.refreshView);
        mRefreshView.setEmptyLayoutId(R.layout.view_no_data_pay_pub);
        mRefreshView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRefreshView.setDataHelper(new CommonRefreshView.DataHelper<PayContentBean>() {
            @Override
            public RefreshAdapter<PayContentBean> getAdapter() {
                if (mAdapter == null) {
                    mAdapter = new PayPubAdapter(mContext);
                }
                return mAdapter;
            }

            @Override
            public void loadData(int p, HttpCallback callback) {
                if (!TextUtils.isEmpty(mToUid)) {
                    if (mToUid.equals(CommonAppConfig.getInstance().getUid())) {
                        if (mContext instanceof PayContentActivity2) {
                            MallHttpUtil.getMyPayContentList(p, callback);
                        } else {
                            MallHttpUtil.getToPayContent(mToUid, p, callback);
                        }
                    } else {
                        MallHttpUtil.getToPayContent(mToUid, p, callback);
                    }
                }

            }

            @Override
            public List<PayContentBean> processData(String[] info) {
                return JSON.parseArray(Arrays.toString(info), PayContentBean.class);
            }

            @Override
            public void onRefreshSuccess(List<PayContentBean> list, int listCount) {

            }

            @Override
            public void onRefreshFailure() {

            }

            @Override
            public void onLoadMoreSuccess(List<PayContentBean> loadItemList, int loadItemCount) {

            }

            @Override
            public void onLoadMoreFailure() {

            }
        });
    }

    @Override
    public void onClick(View v) {
        PayContentPubActivity.forward(mContext);
    }


    @Override
    public void loadData() {
        if (mRefreshView != null) {
            mRefreshView.initData();
        }
    }


    @Override
    public void onDestroy() {
        MallHttpUtil.cancel(MallHttpConsts.GET_MY_PAY_CONTENT_LIST);
        MallHttpUtil.cancel(MallHttpConsts.GET_TO_PAY_CONTENT);
        super.onDestroy();
    }
}
