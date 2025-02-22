package com.yunbao.common.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.Constants;
import com.yunbao.common.R;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.http.HttpCallback;

/**
 * Created by cxf on 2019/2/25.
 */

public class RouteUtil {
    /**
     * 启动页
     */
    public static void forwardLauncher() {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.phonelive.activity.LauncherActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        CommonAppContext.getInstance().startActivity(intent);
    }

    public static void forwardLauncher(int type, String data) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.phonelive.activity.LauncherActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(Constants.PUSH_TYPE, type);
        intent.putExtra(Constants.PUSH_DATA, data);
        CommonAppContext.getInstance().startActivity(intent);
    }

    /**
     * 登录
     */
    public static void forwardLogin(Context context, String tip) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.LoginActivity");
        intent.putExtra(Constants.TIP, tip);
        context.startActivity(intent);
    }

    /**
     * 登录
     */
    public static void forwardLogin(Context context) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.LoginActivity");
        context.startActivity(intent);
    }

    /**
     * 登录过期
     */
    public static void forwardLoginInvalid(String tip) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.LoginInvalidActivity");
        intent.putExtra(Constants.TIP, tip);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        CommonAppContext.getInstance().startActivity(intent);
    }

    /**
     * 青少年模式提示
     */
    public static void teenagerTip(String tip) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.TeenagerTipActivity");
        intent.putExtra(Constants.TIP, tip);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        CommonAppContext.getInstance().startActivity(intent);
    }


    /**
     * 跳转到个人主页
     */
    public static void forwardUserHome(Context context, String toUid) {
        forwardUserHome(context, toUid, false, null);
    }

    /**
     * 跳转到个人主页
     */
    public static void forwardUserHome(Context context, String toUid, boolean fromLiveRoom, String fromLiveUid) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.UserHomeActivity");
        intent.putExtra(Constants.TO_UID, toUid);
        intent.putExtra(Constants.FROM_LIVE_ROOM, fromLiveRoom);
        intent.putExtra(Constants.LIVE_UID, fromLiveUid);
        context.startActivity(intent);
    }

    /**
     * 跳转到充值页面
     */
    public static void forwardMyCoin(Context context) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.MyCoinActivity");
        context.startActivity(intent);
    }

    /**
     * 商品详情页面
     */
    public static void forwardGoodsDetail(Context context, String goodsId, boolean fromShop, String liveUid, String shareUid) {
        forwardGoodsDetail(context, goodsId, fromShop, liveUid, shareUid, null);
    }

    /**
     * 商品详情页面
     */
    public static void forwardGoodsDetail(final Context context, final String goodsId, final boolean fromShop, final String liveUid, final String shareUid, final Parcelable liveBean) {
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            ToastUtil.show(R.string.a_137);
            return;
        }
        CommonHttpUtil.checkGoodsExist(goodsId, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    Intent intent = new Intent();
                    intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.mall.activity.GoodsDetailActivity");
                    intent.putExtra(Constants.MALL_GOODS_ID, goodsId);
                    intent.putExtra(Constants.LIVE_UID, liveUid);
                    intent.putExtra(Constants.SHARE_UID, shareUid);
                    intent.putExtra(Constants.MALL_GOODS_FROM_SHOP, fromShop);
                    if (liveBean!=null) {
                        intent.putExtra(Constants.LIVE_BEAN, liveBean);
                    }
                    context.startActivity(intent);
                } else {
                    ToastUtil.show(msg);
                }
            }
        });

    }

    public static void forwardGoodsDetailOutSide(final Context context, final String goodsId, final boolean fromShop) {
        forwardGoodsDetailOutSide(context, goodsId, fromShop, null);
    }

    /**
     * 站外商品详情页面
     */
    public static void forwardGoodsDetailOutSide(final Context context, final String goodsId, final boolean fromShop, final Parcelable liveBean) {
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            ToastUtil.show(R.string.a_137);
            return;
        }
        CommonHttpUtil.checkGoodsExist(goodsId, new HttpCallback() {
            @Override
            public void onSuccess(int code, String msg, String[] info) {
                if (code == 0) {
                    Intent intent = new Intent();
                    intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.mall.activity.GoodsOutSideDetailActivity");
                    intent.putExtra(Constants.MALL_GOODS_ID, goodsId);
                    intent.putExtra(Constants.LIVE_UID, "0");
                    intent.putExtra(Constants.MALL_GOODS_FROM_SHOP, fromShop);
                    if (liveBean!=null) {
                        intent.putExtra(Constants.LIVE_BEAN, liveBean);
                    }
                    context.startActivity(intent);
                } else {
                    ToastUtil.show(msg);
                }
            }
        });

    }

    /**
     * 付费内容详情页面
     */
    public static void forwardPayContentDetail(Context context, String goodsId) {
        if (CommonAppConfig.getInstance().isTeenagerType()) {
            ToastUtil.show(R.string.a_137);
            return;
        }
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.mall.activity.PayContentDetailActivity");
        intent.putExtra(Constants.MALL_GOODS_ID, goodsId);
        context.startActivity(intent);
    }


    /**
     * 跳转到视频播放
     */
    public static void forwardVideoPlay(Context context, String videoUrl, String coverImgUrl) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.ActiveVideoPlayActivity");
        intent.putExtra(Constants.VIDEO_PATH, videoUrl);
        intent.putExtra(Constants.URL, coverImgUrl);
        context.startActivity(intent);
    }

    /**
     * 跳转到认证页面
     */
    public static void forwardAuth(AbsActivity activity, int requestCode) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.AuthActivity");
        activity.startActivityForResult(intent, requestCode);
    }


    /**
     * 跳转到app内部的指定类名的activity
     *
     * @param context   出发的Activity
     * @param className 目标Activity的全类名
     */
    public static void forward(Context context, String className) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, className);
        context.startActivity(intent);
    }


    /**
     * 分享商品到动态
     */
    public static void forwardActivePub(Context context, String goodsId) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.ActivePubActivity");
        intent.putExtra(Constants.MALL_GOODS_ID, goodsId);
        context.startActivity(intent);
    }


    /**
     * 分享商品到好友
     */
    public static void forwardShareGoods(Context context, String goodsId) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.GoodsShareFriendActivity");
        intent.putExtra(Constants.MALL_GOODS_ID, goodsId);
        context.startActivity(intent);
    }


    public static void forwardTeenager(Context context) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.main.activity.TeenagerActivity");
        context.startActivity(intent);
    }

    public static void forwardLiveAudienceActivity(Context context) {
        Intent intent = new Intent();
        intent.setClassName(CommonAppConfig.PACKAGE_NAME, "com.yunbao.live.activity.LiveAudienceActivity");
        context.startActivity(intent);
    }



}
