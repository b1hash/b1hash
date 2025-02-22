package com.yunbao.common.dialog;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.R;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.adapter.FirstChargeAdapter;
import com.yunbao.common.bean.FirstChargeBean;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.ToastUtil;

import java.util.Arrays;
import java.util.List;

public class FirstChargeDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private FirstChargeAdapter mAdapter;
    private RadioButton mBtn0;
    private RadioButton mBtn1;
    private RadioButton mBtn2;
    private List<FirstChargeBean> mFirstChargeList;
    private int mCheckedIndex;
    private boolean mHasUsed;//是否首冲过了

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_first_charge;
    }

    @Override
    protected int getDialogStyle() {
        return R.style.dialog;
    }

    @Override
    protected boolean canCancel() {
        return true;
    }

    @Override
    protected void setWindowAttributes(Window window) {
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewById(R.id.btn_close).setOnClickListener(this);
        mBtn0 = findViewById(R.id.btn_0);
        mBtn1 = findViewById(R.id.btn_1);
        mBtn2 = findViewById(R.id.btn_2);
        mBtn0.setOnClickListener(this);
        mBtn1.setOnClickListener(this);
        mBtn2.setOnClickListener(this);
        findViewById(R.id.btn_charge).setOnClickListener(this);
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new FirstChargeAdapter(mContext);
        mRecyclerView.setAdapter(mAdapter);
        CommonHttpUtil.getFirstChargeRules(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0 && info.length > 0) {
                    JSONObject obj = JSON.parseObject(info[0]);
                    mHasUsed = obj.getIntValue("has_used") == 1;
                    List<FirstChargeBean> list = JSON.parseArray(obj.getString("list"), FirstChargeBean.class);
                    mFirstChargeList = list;
                    mBtn0.setText(list.get(0).getTitle());
                    mBtn1.setText(list.get(1).getTitle());
                    mBtn2.setText(list.get(2).getTitle());
                    mCheckedIndex = 0;
                    showData();
                }
            }
        });
    }


    private void showData() {
        if (mAdapter == null) {
            return;
        }
        if (mFirstChargeList != null && mFirstChargeList.size() > mCheckedIndex) {
            FirstChargeBean bean = mFirstChargeList.get(mCheckedIndex);
            mAdapter.refreshData(bean.getList());
        }
    }

    private void check(int index) {
        if (mCheckedIndex == index) {
            return;
        }
        mCheckedIndex = index;
        showData();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_close) {
            dismiss();
        } else if (i == R.id.btn_0) {
            check(0);
        } else if (i == R.id.btn_1) {
            check(1);
        } else if (i == R.id.btn_2) {
            check(2);
        } else if (i == R.id.btn_charge) {
            charge();
        }
    }

    private void charge() {
        if(mHasUsed){
            ToastUtil.show(R.string.a_100);
            return;
        }
        if (mFirstChargeList != null && mFirstChargeList.size() > mCheckedIndex) {
            FirstChargeBean bean = mFirstChargeList.get(mCheckedIndex);
            FirstChargePayDialogFragment fragment = new FirstChargePayDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString("id", bean.getId());
            bundle.putString("name", bean.getTitle());
            bundle.putString("money", bean.getMoney());
            bundle.putString("coin", bean.getCoin());
            fragment.setArguments(bundle);
            fragment.show(((AbsActivity) mContext).getSupportFragmentManager(), "FirstChargePayDialogFragment");
            dismiss();//增加这一行
        }
    }


    @Override
    public void onDestroy() {
        CommonHttpUtil.cancel(CommonHttpConsts.GET_FIRST_CHARGE_RULES);
        super.onDestroy();
    }
}
