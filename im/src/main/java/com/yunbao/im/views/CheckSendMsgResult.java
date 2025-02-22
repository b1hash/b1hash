package com.yunbao.im.views;

/**
 * Created by http://www.yunbaokj.com on 2022/12/8.
 */
public class CheckSendMsgResult {
    private boolean mCanSend;

    public CheckSendMsgResult(boolean canSend) {
        mCanSend = canSend;
    }

    public boolean isCanSend() {
        return mCanSend;
    }
}
