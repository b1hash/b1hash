package com.yunbao.live.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.dialog.AbsDialogFragment;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.live.R;
import com.yunbao.live.bean.SuperForbidTimeBean;
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;
import com.zyyoona7.wheel.WheelView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by 云豹科技 on 2022/3/4.
 * 超管选择关播时间
 */
public class SuperForbidTimeDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    private WheelView<SuperForbidTimeBean> mWheelView;
    private ActionListener mActionListener;

    @Override
    protected int getLayoutId() {
        return R.layout.layout_super_forbid_time;
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
        params.width = DpUtil.dp2px(280);
        params.height = DpUtil.dp2px(240);
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        mWheelView = (WheelView<SuperForbidTimeBean>) findViewById(R.id.wheelview);

        LiveHttpUtil.getSuperForbidTime(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    List<SuperForbidTimeBean> list = JSON.parseArray(Arrays.toString(info), SuperForbidTimeBean.class);
                    if(mWheelView!=null){
                        mWheelView.setData(list);
                    }
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_cancel) {
            dismiss();
        } else if (i == R.id.btn_confirm) {
            if (mActionListener != null && mWheelView != null) {
                mActionListener.onConfirmClick(mWheelView.getSelectedItemData());
            }
            dismiss();
        }
    }


    @Override
    public void onDestroy() {
        LiveHttpUtil.cancel(LiveHttpConsts.GET_SUPER_FORBID_TIME);
        if (mWheelView != null) {
            mWheelView.forceFinishScroll();
        }
        mWheelView = null;
        super.onDestroy();
    }


    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public interface ActionListener {
        void onConfirmClick(SuperForbidTimeBean data);
    }
}
