package com.yunbao.common.utils;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import com.yunbao.common.CommonAppContext;

import java.util.Locale;

/**
 * Created by cxf on 2017/10/10.
 * 获取string.xml中的字
 */

public class WordUtil {

    private static Resources sResources;

    static {
        sResources = CommonAppContext.getInstance().getResources();
    }

    public static String getString(int res) {
        if (res == 0) {
            return "";
        }
        Locale appLocale = LanguageUtil.getLanguageLocale();
        Configuration configuration = sResources.getConfiguration();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            LocaleList localeList = configuration.getLocales();
            if (localeList.indexOf(appLocale) < 0) {
                configuration.setLocale(appLocale);
                DisplayMetrics dm = sResources.getDisplayMetrics();
                sResources.updateConfiguration(configuration, dm);
            }
        } else {
            Locale locale = configuration.locale;
            if (locale == null || !locale.equals(appLocale)) {
                configuration.setLocale(appLocale);
                DisplayMetrics dm = sResources.getDisplayMetrics();
                sResources.updateConfiguration(configuration, dm);
            }
        }
        return sResources.getString(res);
    }

}
