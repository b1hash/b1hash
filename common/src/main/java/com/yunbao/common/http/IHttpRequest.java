package com.yunbao.common.http;

import android.net.Uri;

import java.io.File;

/**
 * Created by http://www.yunbaokj.com on 2023/3/15.
 */
public interface IHttpRequest {

    void execute(HttpCallback callback);

    void execute(StringHttpCallback callback);

    void execute(File dir, String fileName, FileDownloadCallback callback);

    void execute(Uri uri, UriDownloadCallback callback);

    IHttpRequest params(String key, Object value);

    IHttpRequest headers(String key, String value);

}
