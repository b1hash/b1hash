package com.yunbao.common.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.event.LocationCityEvent;
import com.yunbao.common.event.LocationEvent;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;

import org.greenrobot.eventbus.EventBus;

public class LocationUtil {
    private static final String TAG = "定位";
    private static LocationUtil sInstance;
    private TencentLocationManager mLocationManager;

    private LocationUtil() {
        mLocationManager = TencentLocationManager.getInstance(CommonAppContext.getInstance());
    }

    public static LocationUtil getInstance() {
        if (sInstance == null) {
            synchronized (LocationUtil.class) {
                if (sInstance == null) {
                    sInstance = new LocationUtil();
                }
            }
        }
        return sInstance;
    }


    private TencentLocationListener mLocationListener = new TencentLocationListener() {
        @Override
        public void onLocationChanged(TencentLocation location, int code, String reason) {
            L.e(TAG, "onLocationChanged-----code-----> " + code + "-----reason---> " + reason);
            if (code == TencentLocation.ERROR_OK) {
                double lng = location.getLongitude();//经度
                double lat = location.getLatitude();//纬度
                L.e(TAG, "获取经纬度成功------>经度：" + lng + "，纬度：" + lat);
                CommonAppConfig.getInstance().setLngLat(lng, lat);
                EventBus.getDefault().post(new LocationEvent(lng, lat));
                CommonHttpUtil.getAddressInfoByTxLocaitonSdk(lng, lat, 0, 1, CommonHttpConsts.GET_LOCAITON, mCallback);
            }
        }

        @Override
        public void onStatusUpdate(String s, int i, String s1) {

        }
    };

    private HttpCallback mCallback = new HttpCallback() {
        @Override
        public void onSuccess(int code, String msg, String[] info) {
            if (code == 0 && info.length > 0) {
                JSONObject obj = JSON.parseObject(info[0]);
                if (obj != null) {
                    L.e(TAG, "获取位置信息成功---当前地址--->" + obj.getString("address"));
                    JSONObject location = obj.getJSONObject("location");
                    JSONObject addressComponent = obj.getJSONObject("address_component");
                    String city = addressComponent.getString("city");
                    CommonAppConfig.getInstance().setLocationInfo(
                            location.getDoubleValue("lng"),
                            location.getDoubleValue("lat"),
                            addressComponent.getString("province"),
                            city,
                            addressComponent.getString("district"));
                    EventBus.getDefault().post(new LocationCityEvent(city));
                    CommonHttpUtil.updateCity(city);
                }
            }
        }
    };


    //启动定位
    public void startLocation() {
        if (mLocationManager != null) {
//            mLocationStarted = true;
            L.e(TAG, "开启定位");
            TencentLocationRequest request = TencentLocationRequest
                    .create()
                    .setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_GEO)
                    .setAllowGPS(true)
                    .setInterval(60 * 60 * 1000);//1小时定一次位
            mLocationManager.requestLocationUpdates(request, mLocationListener);
        }
    }

    //停止定位
    public void stopLocation() {
        CommonHttpUtil.cancel(CommonHttpConsts.GET_LOCAITON);
        if (mLocationManager != null) {
            L.e(TAG, "关闭定位");
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

}
