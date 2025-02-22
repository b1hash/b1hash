package com.yunbao.common.event;

/**
 * Created by cxf on 2018/7/18.
 */

public class LocationCityEvent {
    private final String city;

    public LocationCityEvent(String city) {
        this.city = city;
    }


    public String getCity() {
        return city;
    }
}
