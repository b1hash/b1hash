package com.yunbao.common.utils;

import android.net.Uri;
import android.text.TextUtils;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.http.FileDownloadCallback;
import com.yunbao.common.http.HttpClient;
import com.yunbao.common.http.UriDownloadCallback;

import java.io.File;

/**
 * Created by cxf on 2017/9/4.
 */

public class DownloadUtil {

    private final String mTag;

    public DownloadUtil() {
        this("Download_" + DateFormatUtil.getVideoCurTimeString());
    }

    public DownloadUtil(String tag) {
        mTag = tag;
    }

    public void download(String dirPath, String fileName, String url, FileDownloadCallback downloadCallback) {
        download(new File(dirPath), fileName, url, downloadCallback);
    }

    public void download(File dir, String fileName, String url, FileDownloadCallback downloadCallback) {
        if (dir == null || TextUtils.isEmpty(fileName) || TextUtils.isEmpty(url)) {
            return;
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        HttpClient.getInstance().getFile(url, mTag)
                .headers("referer", CommonAppConfig.HOST)
                .execute(dir, fileName, downloadCallback);
    }


    public void download(Uri uri, String url, UriDownloadCallback downloadCallback) {
        if (uri == null || TextUtils.isEmpty(url)) {
            return;
        }
        HttpClient.getInstance().getFile(url, mTag)
                .headers("referer", CommonAppConfig.HOST)
                .execute(uri, downloadCallback);
    }


    public void cancel() {
        HttpClient.getInstance().cancel(mTag);
    }

}
