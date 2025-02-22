package com.yunbao.main.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.dialog.AbsDialogFragment;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.main.R;
import com.yunbao.main.activity.TeenagerActivity;

/**
 * Created by 云豹科技 on 2022/6/9.
 */
public class TeenagerDialogFragment extends AbsDialogFragment implements View.OnClickListener {

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_teenager;
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
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewById(R.id.btn_open).setOnClickListener(this);
        findViewById(R.id.btn_dismiss).setOnClickListener(this);
        TextView tip = findViewById(R.id.tip);
        ConfigBean configBean = CommonAppConfig.getInstance().getConfig();
        if (configBean != null) {
            tip.setText(configBean.getTeenager_des());
        }

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_open) {
            TeenagerActivity.forward(mContext);
            dismiss();
        } else if (i == R.id.btn_dismiss) {
            dismiss();
        }
    }
}
