package com.yunbao.common.http.fuel;

import android.text.TextUtils;

import com.lzy.okgo.https.HttpsUtils;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.http.IHttpClient;
import com.yunbao.common.http.IHttpRequest;
import com.yunbao.common.utils.L;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Created by cxf on 2018/9/17.
 */

public class FuelHttpClient implements IHttpClient {

    private final String mUrl;
//    private final HashMap<String, FuelRequest> mMap;


    public FuelHttpClient() {
        mUrl = CommonAppConfig.HOST + "/appapi/";
//        mMap = new HashMap<>();
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession sslsession) {
                /*L.e("FuelHttpClient","hostnameVerifier----->" + hostname);
                if ("yinni.yyyybbbb.com".equals(hostname)
                        || "maps.googleapis.com".equals(hostname)
                        || "aip.baidubce.com".equals(hostname)
                        || "cdn-dev.foximediainternational.com".equals(hostname)
                        || "txplay.dev-foxizhibolian.xyz".equals(hostname)
                        || "mlvbdc.live.qcloud.com".equals(hostname)
                        || "license.vod2.myqcloud.com".equals(hostname)
                        || "inland-sdklog.trtc.tencent-cloud.com".equals(hostname)
                        || "events.my-imcloud.com".equals(hostname)
                        || "errnewlog.umeng.com".equals(hostname)
                        || "platform-lookaside.fbsbx.com".equals(hostname)
                        || "lh3.googleusercontent.com".equals(hostname)
                        || "ulogs.umengcloud.com".equals(hostname)
                        || "ulogs.umeng.com".equals(hostname)
                        || "sdkdc.live.qcloud.com".equals(hostname)
                        || "videoapi-sgp.im.qcloud.com".equals(hostname)
                ) {
                    return true;
                }
                return false;*/
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        HttpsURLConnection.setDefaultSSLSocketFactory(sslParams.sSLSocketFactory);
    }

    public IHttpRequest get(String serviceName, String tag) {
        FuelRequest req = new FuelRequest(mUrl, FuelRequest.Method.GET);
        req.setServiceName(serviceName);
//        if (!TextUtils.isEmpty(tag)) {
//            mMap.put(tag, req);
//        }
        return req;
    }


    public IHttpRequest post(String serviceName, String tag) {
        FuelRequest req = new FuelRequest(mUrl, FuelRequest.Method.POST);
        req.setServiceName(serviceName);
//        if (!TextUtils.isEmpty(tag)) {
//            mMap.put(tag, req);
//        }
        return req;
    }

    @Override
    public IHttpRequest getString(String url, String tag) {
        FuelRequest req = new FuelRequest(url, FuelRequest.Method.GET);
//        if (!TextUtils.isEmpty(tag)) {
//            mMap.put(tag, req);
//        }
        return req;
    }

    @Override
    public IHttpRequest getFile(String url, String tag) {
        FuelRequest req = new FuelRequest(url, FuelRequest.Method.GET);
//        if (!TextUtils.isEmpty(tag)) {
//            mMap.put(tag, req);
//        }
        return req;
    }

    public void cancel(String tag) {
//        if (!TextUtils.isEmpty(tag)) {
//            FuelRequest req = mMap.get(tag);
//            if (req != null) {
//                req.cancel();
//                mMap.remove(tag);
//            }
//        }
    }

}
