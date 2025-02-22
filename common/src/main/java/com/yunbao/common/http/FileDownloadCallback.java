package com.yunbao.common.http;

import java.io.File;

/**
 * Created by http://www.yunbaokj.com on 2023/3/17.
 */
public abstract class FileDownloadCallback {
    public abstract void onSuccess(File file);

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
