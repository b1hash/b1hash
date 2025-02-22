package com.yunbao.live.dialog;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.yunbao.common.Constants;
import com.yunbao.common.dialog.AbsDialogFragment;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.live.R;

/**
 * Created by 云豹科技 on 2022/3/4.
 * 超管关播提示
 */
public class SuperAdminTipDialogFragment extends AbsDialogFragment {


    @Override
    protected int getLayoutId() {
        return R.layout.layout_super_admin_tip;
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
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = DpUtil.dp2px(280);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        Bundle bundle=getArguments();
        if(bundle!=null){
            CharSequence tip=bundle.getCharSequence(Constants.TIP);
            TextView tvTip=findViewById(R.id.tip);
            tvTip.setText(tip);
        }

    }
}
