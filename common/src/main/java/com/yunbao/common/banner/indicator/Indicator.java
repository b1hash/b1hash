package com.yunbao.common.banner.indicator;

import android.view.View;

import androidx.annotation.NonNull;

import com.yunbao.common.banner.config.IndicatorConfig;
import com.yunbao.common.banner.listener.OnPageChangeListener;

public interface Indicator extends OnPageChangeListener {
    @NonNull
    View getIndicatorView();

    IndicatorConfig getIndicatorConfig();

    void onPageChanged(int count, int currentPosition);

}
