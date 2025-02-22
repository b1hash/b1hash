package com.yunbao.common.http;

import android.app.Dialog;

/**
 * Created by cxf on 2017/8/7.
 */

public abstract class HttpCallback  {

    private Dialog mLoadingDialog;

    public abstract void onSuccess(int code, String msg, String[] info);

    public void onError() {

    }

    /**
     * 登录过期
     */
    public void onLoginInvalid() {

    }

    public void onStart() {
        try {
            if (showLoadingDialog()) {
                if (mLoadingDialog == null) {
                    mLoadingDialog = createLoadingDialog();
                }
                mLoadingDialog.show();
            }
        } catch (Exception e) {

        }
    }

    public void onFinish() {
        if (showLoadingDialog() && mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }

    public Dialog createLoadingDialog() {
        return null;
    }

    public boolean showLoadingDialog() {
        return false;
    }

    public boolean isUseLoginInvalid() {
        return false;
    }

}
