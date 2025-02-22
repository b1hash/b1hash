package com.yunbao.common.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.R;
import com.yunbao.common.adapter.FirstChargePayAdapter;
import com.yunbao.common.bean.CoinPayBean;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.OnItemClickListener;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.WordUtil;

import java.util.Arrays;
import java.util.List;

public class FirstChargePayDialogFragment extends AbsDialogFragment implements OnItemClickListener<CoinPayBean> {

    private RecyclerView mRecyclerView;
    private FirstChargePayAdapter mAdapter;
//    private PayPresenter mPayPresenter;
    private String mChargeId;
    private String mChargeName;
    private String mChargeMoney;
    private String mChargeCoin;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_first_charge_pay;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog2;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        window.setWindowAnimations(R.style.bottomToTopAnim);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mChargeId = bundle.getString("id");
        mChargeName = bundle.getString("name");
        mChargeMoney = bundle.getString("money");
        mChargeCoin = bundle.getString("coin");
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mAdapter = new FirstChargePayAdapter(mContext);
        mAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(mAdapter);
//        mPayPresenter = new PayPresenter((AbsActivity) mContext);
//        mPayPresenter.setServiceNameAli(Constants.PAY_BUY_COIN_ALI);
//        mPayPresenter.setServiceNameWx(Constants.PAY_BUY_COIN_WX);
//        mPayPresenter.setServiceNamePaypal(Constants.PAY_BUY_COIN_PAYPAL);
//        mPayPresenter.setAliCallbackUrl(HtmlConfig.ALI_PAY_COIN_URL);
//        mPayPresenter.setPayCallback(new PayCallback() {
//            @Override
//            public void onSuccess() {
//                EventBus.getDefault().post(new FirstChargeEvent());
//                ToastUtil.show(R.string.pay_succ);
//                dismiss();
//            }
//
//            @Override
//            public void onFailed() {
//                ToastUtil.show(R.string.pay_fail);
//                dismiss();
//            }
//        });
//        CommonHttpUtil.getBalance(new HttpCallback() {
//            @Override
//            public void onSuccess(int code, String msg, String[] info) {
//                if (code == 0 && info.length > 0) {
//                    JSONObject obj = JSON.parseObject(info[0]);
//                    List<CoinPayBean> payList = JSON.parseArray(obj.getString("paylist"), CoinPayBean.class);
//                    if (mPayPresenter != null) {
//                        mPayPresenter.setAliPartner(obj.getString("aliapp_partner"));
//                        mPayPresenter.setAliSellerId(obj.getString("aliapp_seller_id"));
//                        mPayPresenter.setAliPrivateKey(obj.getString("aliapp_key_android"));
//                        mPayPresenter.setWxAppID(obj.getString("wx_appid"));
//                    }
//                    mAdapter.refreshData(payList);
//                }
//            }
//        });
        CommonHttpUtil.getPayType(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    List<CoinPayBean> payList = JSON.parseArray(Arrays.toString(info), CoinPayBean.class);
                    if (mAdapter != null) {
                        mAdapter.refreshData(payList);
                    }
                }
            }
        });
    }

    @Override
    public void onItemClick(CoinPayBean coinPayBean, int position) {
//        String href = coinPayBean.getHref();
//        if (TextUtils.isEmpty(href)) {
//            Map<String, String> orderParams = new HashMap<>();
//            orderParams.put("uid", CommonAppConfig.getInstance().getUid());
//            orderParams.put("token", CommonAppConfig.getInstance().getToken());
//            orderParams.put("money", mChargeMoney);
//            orderParams.put("changeid", mChargeId);
//            orderParams.put("coin", mChargeCoin);
//            mPayPresenter.pay(coinPayBean.getId(), mChargeMoney, mChargeName, orderParams);
//        } else {
//            Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_VIEW);
//            intent.setData(Uri.parse(href));
//            mContext.startActivity(intent);
//        }
        CommonHttpUtil.getPayUrl(mChargeMoney, mChargeId, mChargeCoin, coinPayBean.getPaytype(), new HttpCallback() {

            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    String url = obj.getString("url");
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    mContext.startActivity(intent);
                }
            }

            @Override
            public boolean showLoadingDialog() {
                return true;
            }

            @Override
            public Dialog createLoadingDialog() {
                return DialogUitl.loadingDialog(mContext, WordUtil.getString(R.string.loading));
            }
        });
    }
}
