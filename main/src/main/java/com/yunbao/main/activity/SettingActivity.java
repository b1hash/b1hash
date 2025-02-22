package com.yunbao.main.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.activity.WebViewActivity;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.event.LoginChangeEvent;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.CommonHttpConsts;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.LanguageUtil;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.UmengUtil;
import com.yunbao.common.utils.VersionUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.im.tpns.TpnsUtil;
import com.yunbao.im.utils.ImMessageUtil;
import com.yunbao.main.R;
import com.yunbao.main.adapter.SettingAdapter;
import com.yunbao.main.bean.SettingBean;
import com.yunbao.main.http.MainHttpConsts;
import com.yunbao.main.http.MainHttpUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by cxf on 2018/9/30.
 */

public class SettingActivity extends AbsActivity implements SettingAdapter.ActionListener {

    private RecyclerView mRecyclerView;
    private Handler mHandler;
    private SettingAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void main() {
        setTitle(WordUtil.getString(R.string.setting));
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        MainHttpUtil.getSettingList(new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                List<SettingBean> list0 = JSON.parseArray(Arrays.toString(info), SettingBean.class);
                List<SettingBean> list = new ArrayList<>();
                SettingBean bean = new SettingBean();
                bean.setId(-1);
                bean.setName(WordUtil.getString(R.string.setting_brightness));
                bean.setChecked(SpUtil.getInstance().getBrightness() == 0.05f);
                list.add(bean);

                bean = new SettingBean();
                bean.setId(-3);
                bean.setName(WordUtil.getString(R.string.setting_msg_window));
                bean.setChecked(CommonAppConfig.getInstance().isShowLiveFloatWindow());
                list.add(bean);

                bean = new SettingBean();
                bean.setId(-2);
                bean.setName(WordUtil.getString(R.string.setting_msg_ring));
                bean.setChecked(SpUtil.getInstance().isImMsgRingOpen());
                list.add(bean);

                bean = new SettingBean();
                bean.setId(-4);
                bean.setName(WordUtil.getString(R.string.setting_msg_language));
                list.add(bean);

                list.addAll(list0);
                bean = new SettingBean();
                bean.setId(-5);
                bean.setName(WordUtil.getString(R.string.setting_exit));
                list.add(bean);
                mAdapter = new SettingAdapter(mContext, list, VersionUtil.getVersion(), getCacheSize());
                mAdapter.setActionListener(SettingActivity.this);
                mRecyclerView.setAdapter(mAdapter);
            }
        });
    }


    @Override
    public void onItemClick(SettingBean bean, int position) {
        String href = bean.getHref();
        if (TextUtils.isEmpty(href)) {
            if (bean.getId() == -5) {//退出登录
                new DialogUitl.Builder(mContext)
                        .setContent(WordUtil.getString(R.string.logout_confirm))
                        .setConfrimString(WordUtil.getString(R.string.logout_confirm_2))
                        .setCancelable(true)
                        .setIsHideTitle(true)
                        .setBackgroundDimEnabled(true)
                        .setClickCallback(new DialogUitl.SimpleCallback() {
                            @Override
                            public void onConfirmClick(Dialog dialog, String content) {
                                logout();
                            }
                        })
                        .build()
                        .show();

            } else if (bean.getId() == Constants.SETTING_MODIFY_PWD) {//修改密码
                forwardModifyPwd();
            } else if (bean.getId() == Constants.SETTING_UPDATE_ID) {//检查更新
                checkVersion();
            } else if (bean.getId() == Constants.SETTING_CLEAR_CACHE) {//清除缓存
                clearCache(position);
            }
        } else {
            if (bean.getId() == 19) {//注销账号
                CancelConditionActivity.forward(mContext, href);
                return;
            }
            if (bean.getId() == 17) {//意见反馈要在url上加版本号和设备号
                if (!href.contains("?")) {
                    href = StringUtil.contact(href, "?");
                }
                href = StringUtil.contact(href, "&version=", android.os.Build.VERSION.RELEASE, "&model=", android.os.Build.MODEL);
            }
            WebViewActivity.forward(mContext, href);
        }
    }

    @Override
    public void onCheckChanged(SettingBean bean) {
        int id = bean.getId();
        if (id == -1) {
            SpUtil.getInstance().setBrightness(bean.isChecked() ? 0.05f : -1f);
            updateBrightness();
        } else if (id == -2) {
            SpUtil.getInstance().setImMsgRingOpen(bean.isChecked());
        }
    }

    @Override
    public void onLanguageClick() {
        //简体中文，英语，俄语，阿拉伯语，印度语
        DialogUitl.showStringArrayDialog(mContext, new String[]{
                Constants.CHINESE, Constants.ENGLISH, Constants.RUSSIAN, Constants.ARABIC, Constants.INDIA
        }, new DialogUitl.StringArrayDialogCallback() {
            @Override
            public void onItemClick(String text, int tag) {
                String lang = null;
                if (Constants.CHINESE.equals(text)) {
                    lang = Constants.LANG_ZH;
                } else if (Constants.RUSSIAN.equals(text)) {
                    lang = Constants.LANG_RU;
                } else if (Constants.ARABIC.equals(text)) {
                    lang = Constants.LANG_AR;
                } else if (Constants.INDIA.equals(text)) {
                    lang = Constants.LANG_HI;
                } else {
                    lang = Constants.LANG_EN;
                }
                changeLanguage(lang);
            }
        });
    }

    /**
     * 切换语言
     */
    private void changeLanguage(String lang) {
        if (!TextUtils.isEmpty(lang) && !LanguageUtil.getInstance().getLanguage().equals(lang)) {
            LanguageUtil.getInstance().updateLanguage(lang);
            ImMessageUtil.getInstance().changeString();
            TpnsUtil.changeTag();
            CommonHttpUtil.getConfig(new CommonCallback<ConfigBean>() {
                @Override
                public void callback(ConfigBean bean) {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.startActivity(intent);
                }
            });
        }
    }

    /**
     * 检查更新
     */
    private void checkVersion() {
        CommonAppConfig.getInstance().getConfig(new CommonCallback<ConfigBean>() {
            @Override
            public void callback(ConfigBean configBean) {
                if (configBean != null) {
                    if (VersionUtil.isLatest(configBean.getVersion())) {
                        ToastUtil.show(R.string.version_latest);
                    } else {
                        VersionUtil.showDialog(mContext, configBean, configBean.getDownloadApkUrl());
                    }
                }
            }
        });

    }

    /**
     * 退出登录
     */
    private void logout() {
        CommonAppConfig.getInstance().clearLoginInfo();
        //退出IM
        ImMessageUtil.getInstance().logoutImClient();
        //友盟统计登出
        UmengUtil.userLogout();
        EventBus.getDefault().post(new LoginChangeEvent(false, false));
        finish();
    }

    /**
     * 修改密码
     */
    private void forwardModifyPwd() {
        startActivity(new Intent(mContext, ModifyPwdActivity.class));
    }

    /**
     * 获取缓存
     */
    private String getCacheSize() {
        return ImgLoader.getCacheSize();
    }

    /**
     * 清除缓存
     */
    private void clearCache(final int position) {
        final Dialog dialog = DialogUitl.loadingDialog(mContext, getString(R.string.setting_clear_cache_ing));
        dialog.show();
        ImgLoader.clearImageCache();
        File gifGiftDir = new File(CommonAppConfig.GIF_PATH);
        if (gifGiftDir.exists() && gifGiftDir.length() > 0) {
            gifGiftDir.delete();
        }
        if (mHandler == null) {
            mHandler = new Handler();
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) {
                    dialog.dismiss();
                }
                if (mAdapter != null) {
                    mAdapter.setCacheString(getCacheSize());
                    mAdapter.notifyItemChanged(position);
                }
                ToastUtil.show(R.string.setting_clear_cache);
            }
        }, 2000);
    }


    @Override
    protected void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        MainHttpUtil.cancel(MainHttpConsts.GET_SETTING_LIST);
        CommonHttpUtil.cancel(CommonHttpConsts.GET_CONFIG);
        MainHttpUtil.cancel(MainHttpConsts.SET_LIVE_WINDOW);
        super.onDestroy();
    }


}
