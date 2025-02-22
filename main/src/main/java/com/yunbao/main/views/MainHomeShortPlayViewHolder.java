package com.yunbao.main.views;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.utils.LanguageUtil;
import com.yunbao.main.R;

public class MainHomeShortPlayViewHolder extends AbsMainViewHolder {
    private WebView webView;

    public MainHomeShortPlayViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.main_home_short_play;
    }

    @Override
    public void init() {
        Log.i("Sunday", "MainHomeShortPlayViewHolder init=======");
        setStatusHeight();
        webView = (WebView) findViewById(R.id.web_view);
        if (webView != null) {
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true); // 如果需要启用 JavaScript
            webView.setWebViewClient(new WebViewClient()); // 在当前 WebView 中打开链接
            String url = CommonAppConfig.getInstance().getShortPlayUrl() + "?" + "uid=" + CommonAppConfig.getInstance().getUid() + "&token=" + CommonAppConfig.getInstance().getToken() + "&" + CommonHttpConsts.LANGUAGE + "=" + LanguageUtil.getInstance().getLanguage();
            Log.i("Sunday", "MainHomeShortPlayViewHolder init=======url:" + url);
            webView.loadUrl(url); // 替换为你要加载的网页 URL
        }
    }
}
