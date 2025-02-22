package com.yunbao.mall.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.yunbao.common.dialog.AbsDialogFragment;
import com.yunbao.mall.R;
import com.yunbao.mall.activity.SellerRejectRefundActivity;
import com.yunbao.mall.bean.RejectRefundBean;
import com.zyyoona7.wheel.WheelView;

import java.util.List;


/**
 * 卖家 选择拒绝原因
 */
public class SellerRejectReasonDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private WheelView<RejectRefundBean> mWheelView;
    private List<RejectRefundBean> mList;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_seller_refund_reason;
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
        if (mList == null) {
            return;
        }
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_done).setOnClickListener(this);
        mWheelView = (WheelView) findViewById(R.id.wheelview);
        mWheelView.setData(mList);
    }

    public void setList(List<RejectRefundBean> list) {
        mList = list;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_cancel) {
            dismiss();
        } else if (id == R.id.btn_done) {
            if (mContext != null && mList != null && mWheelView != null) {
                ((SellerRejectRefundActivity) mContext).setRejectReason(mWheelView.getSelectedItemData());
                dismiss();
            }
        }
    }


    @Override
    public void onDestroy() {
        if (mWheelView != null) {
            mWheelView.forceFinishScroll();
        }
        mWheelView = null;
        mContext = null;
        super.onDestroy();
    }
}
