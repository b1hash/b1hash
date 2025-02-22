package com.yunbao.common.http;

/**
 * Created by http://www.yunbaokj.com on 2023/3/17.
 */
public abstract class UriDownloadCallback {
    public abstract void onSuccess();

    public void onError(Throwable e) {

    }

    public void onFinish() {

    }

    /**
     * @param progress 下载进度 0~1的小数
     */
    public void onProgress(float progress){

    }
}
