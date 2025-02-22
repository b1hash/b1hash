package com.yunbao.common.http;

/**
 * Created by http://www.yunbaokj.com on 2023/3/15.
 */
public interface IHttpClient {
    IHttpRequest get(String serviceName, String tag);

    IHttpRequest post(String serviceName, String tag);

    IHttpRequest getString(String url, String tag);

    IHttpRequest getFile(String url, String tag);

    void cancel(String tag);
}
