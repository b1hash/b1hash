package com.yunbao.common.http;

import com.yunbao.common.http.fuel.FuelHttpClient;
import com.yunbao.common.utils.LanguageUtil;

import java.util.Map;

/**
 * Created by http://www.yunbaokj.com on 2023/3/15.
 */
public class HttpClient {

    private static HttpClient sInstance;
    private final IHttpClient mHttpClientImpl;

    private HttpClient() {
        mHttpClientImpl = new FuelHttpClient();
    }

    public static HttpClient getInstance() {
        if (sInstance == null) {
            synchronized (HttpClient.class) {
                if (sInstance == null) {
                    sInstance = new HttpClient();
                }
            }
        }
        return sInstance;
    }

    public IHttpRequest get(String serviceName, String tag) {
        return mHttpClientImpl.get(serviceName, tag).params(CommonHttpConsts.LANGUAGE, LanguageUtil.getInstance().getLanguage());
    }

    public IHttpRequest get(String serviceName, String tag, Map<String, String> params) {
        IHttpRequest req = this.get(serviceName, tag);
        if (params != null && params.size() > 0) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                req.params(entry.getKey(), entry.getValue());
            }
        }
        req.params(CommonHttpConsts.LANGUAGE, LanguageUtil.getInstance().getLanguage());
        return req;
    }

    public IHttpRequest post(String serviceName, String tag) {
        return mHttpClientImpl.post(serviceName, tag).params(CommonHttpConsts.LANGUAGE, LanguageUtil.getInstance().getLanguage());
    }

    public IHttpRequest post(String serviceName, String tag, Map<String, String> paramMap) {
        IHttpRequest req = this.post(serviceName, tag);
        if (paramMap != null && paramMap.size() > 0) {
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                req.params(entry.getKey(), entry.getValue());
            }
        }
        req.params(CommonHttpConsts.LANGUAGE, LanguageUtil.getInstance().getLanguage());
        return req;
    }

    public IHttpRequest getString(String url, String tag) {
        return mHttpClientImpl.getString(url, tag);
    }

    public IHttpRequest getFile(String url, String tag) {
        return mHttpClientImpl.getFile(url, tag);
    }

    public void cancel(String tag) {
        mHttpClientImpl.cancel(tag);
    }

}
