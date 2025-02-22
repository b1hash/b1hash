package com.yunbao.common.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.R;
import com.yunbao.common.dialog.NotLoginDialogFragment;
import com.yunbao.common.interfaces.LifeCycleListener;
import com.yunbao.common.interfaces.PermissionCallback;
import com.yunbao.common.utils.ClickUtil;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.LanguageUtil;
import com.yunbao.common.utils.LocationUtil;
import com.yunbao.common.utils.PermissionUtil;
import com.yunbao.common.utils.ScreenDimenUtil;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.common.utils.WordUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2017/8/3.
 */

public abstract class AbsActivity extends AppCompatActivity {

    protected String mTag;
    protected Context mContext;
    protected List<LifeCycleListener> mLifeCycleListeners;
    private boolean mStatusBarColorWhite;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageUtil.attachBaseContext(newBase));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = this.getClass().getSimpleName();
        getIntentParams();
        setStatusBar(isStatusBarWhite());
        int layoutId = getLayoutId();
        if (layoutId > 0) {
            setContentView(layoutId);
        }
        setStatusHeight();
        mContext = this;
        mLifeCycleListeners = new ArrayList<>();
        main(savedInstanceState);
        if (mLifeCycleListeners != null) {
            for (LifeCycleListener listener : mLifeCycleListeners) {
                listener.onCreate();
            }
        }
    }

    protected void getIntentParams() {

    }

    protected abstract int getLayoutId();

    protected void main(Bundle savedInstanceState) {
        main();
    }

    protected void main() {

    }

    protected boolean isStatusBarWhite() {
        return false;
    }


    protected void setTitle(String title) {
        TextView titleView = (TextView) findViewById(R.id.titleView);
        if (titleView != null) {
            titleView.setText(title);
        }
    }


    public void backClick(View v) {
        if (v.getId() == R.id.btn_back) {
            onBackPressed();
        }
    }

    protected boolean canClick() {
        return ClickUtil.canClick();
    }

    public void changeStatusBarWhite(boolean isWhite) {
        if (mStatusBarColorWhite != isWhite) {
            setStatusBar(isWhite);
        }
    }

    /**
     * 设置透明状态栏
     */
    private void setStatusBar(boolean isWhite) {
        mStatusBarColorWhite = isWhite;
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (isWhite) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(0);
    }


    @Override
    protected void onDestroy() {
        if (mLifeCycleListeners != null) {
            for (LifeCycleListener listener : mLifeCycleListeners) {
                listener.onDestroy();
            }
            mLifeCycleListeners.clear();
            mLifeCycleListeners = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mLifeCycleListeners != null) {
            for (LifeCycleListener listener : mLifeCycleListeners) {
                listener.onStart();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mLifeCycleListeners != null) {
            for (LifeCycleListener listener : mLifeCycleListeners) {
                listener.onReStart();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLifeCycleListeners != null) {
            for (LifeCycleListener listener : mLifeCycleListeners) {
                listener.onResume();
            }
        }
        updateBrightness();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLifeCycleListeners != null) {
            for (LifeCycleListener listener : mLifeCycleListeners) {
                listener.onPause();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLifeCycleListeners != null) {
            for (LifeCycleListener listener : mLifeCycleListeners) {
                listener.onStop();
            }
        }
    }

    public void addLifeCycleListener(LifeCycleListener listener) {
        if (mLifeCycleListeners != null && listener != null) {
            mLifeCycleListeners.add(listener);
        }
    }

    public void addAllLifeCycleListener(List<LifeCycleListener> listeners) {
        if (mLifeCycleListeners != null && listeners != null) {
            mLifeCycleListeners.addAll(listeners);
        }
    }

    public void removeLifeCycleListener(LifeCycleListener listener) {
        if (mLifeCycleListeners != null) {
            mLifeCycleListeners.remove(listener);
        }
    }


    public void startActivity(Class<? extends Activity> cs) {
        Intent intent = new Intent(this, cs);
        startActivity(intent);
    }

    /**
     * 设置当前应用亮度
     */
    public void updateBrightness() {
        float brightness = SpUtil.getInstance().getBrightness();
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        if (brightness != lp.screenBrightness) {
            lp.screenBrightness = brightness;
            window.setAttributes(lp);
        }
    }

    /**
     * 根据不同手机的状态栏设置高度
     */
    private void setStatusHeight() {
        View flTop = findViewById(R.id.fl_top);
        if (flTop != null) {
            flTop.setPadding(0, ScreenDimenUtil.getInstance().getStatusBarHeight(), 0, 0);
        }
    }

    /**
     * 未登录的弹窗
     */
    public void showNotLoginDialog() {
        NotLoginDialogFragment fragment = new NotLoginDialogFragment();
        fragment.show(getSupportFragmentManager(), "NotLoginDialogFragment");
    }


    public boolean checkLogin() {
        if (!CommonAppConfig.getInstance().isLogin()) {
            showNotLoginDialog();
            return false;
        }
        return true;
    }


    public void checkLocationPermission(final Runnable runnable) {
        if (hasLocationPermission()) {
            if (runnable != null) {
                runnable.run();
            }
        } else {
            new DialogUitl.Builder(mContext)
                    .setTitle(WordUtil.getString(R.string.开启定位服务))
                    .setContent(WordUtil.getString(R.string.开启定位提示))
                    .setCancelable(true)
                    .setBackgroundDimEnabled(true)
                    .setConfrimString(WordUtil.getString(R.string.设置))
                    .setClickCallback(new DialogUitl.SimpleCallback() {
                        @Override
                        public void onConfirmClick(Dialog dialog, String content) {
                            PermissionUtil.request((AbsActivity) mContext, new PermissionCallback() {
                                        @Override
                                        public void onAllGranted() {
                                            if (runnable != null) {
                                                runnable.run();
                                            }
                                        }
                                    },
                                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
                            );
                        }
                    })
                    .build()
                    .show();

        }
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }
}
