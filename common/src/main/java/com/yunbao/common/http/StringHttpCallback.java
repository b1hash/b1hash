package com.yunbao.common.http;

/**
 * Created by http://www.yunbaokj.com on 2023/3/17.
 */
public abstract class StringHttpCallback {
    public abstract void onSuccess(String responseStr);

    public void onError(Throwable e) {

    }

    public void onFinish() {

    }
}
