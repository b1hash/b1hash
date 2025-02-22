package com.yunbao.common;

/**
 * Created by cxf on 2018/10/15.
 */

public class HtmlConfig {

    //登录即代表同意服务和隐私条款
    public static final String LOGIN_PRIVCAY = CommonAppConfig.HOST + "/portal/page/index?id=4";
    //直播间贡献榜
    public static final String LIVE_LIST = CommonAppConfig.HOST + "/appapi/contribute/index?uid=";
    //个人主页分享链接
    public static final String SHARE_HOME_PAGE = CommonAppConfig.HOST + "/appapi/home/index?touid=";
    //提现记录
    public static final String CASH_RECORD = CommonAppConfig.HOST + "/appapi/cash/index";
    //支付宝充值回调地址
    public static final String ALI_PAY_COIN_URL = CommonAppConfig.HOST + "/appapi/Pay/notify_ali";
    //支付宝购物下单支付回调地址
    public static final String ALI_PAY_MALL_ORDER = CommonAppConfig.HOST + "/appapi/Shoppay/notify_ali";
    //支付宝购买付费内容 下单支付回调地址
    public static final String ALI_PAY_MALL_PAY_CONTENT = CommonAppConfig.HOST + "/appapi/Paidprogrampay/notify_ali";

    //视频分享地址
    public static final String SHARE_VIDEO = CommonAppConfig.HOST + "/appapi/video/index?videoid=";
    //直播间幸运礼物说明
    public static final String LUCK_GIFT_TIP = CommonAppConfig.HOST + "/portal/page/index?id=26";
    //直播间道具礼物说明
    public static final String DAO_GIFT_TIP = CommonAppConfig.HOST + "/portal/page/index?id=39";
    //在线商城
    public static final String SHOP = CommonAppConfig.HOST + ":8001/vip";
    //认证
    public static final String AUTH = CommonAppConfig.HOST + "/appapi/Auth/index";
    //我的明细
    public static final String DETAIL = CommonAppConfig.HOST + "/appapi/Detail/index";
    //充值协议
    public static final String CHARGE_PRIVCAY = CommonAppConfig.HOST + "/portal/page/index?id=6";
    //充值明细
    public static final String CHARGE_DETAIL = CommonAppConfig.HOST + "/appapi/charge/index";
    //直播间游戏规则
    public static final String GAME_RULE = CommonAppConfig.HOST + "/portal/page/index?id=35";
    //买家查看物流信息
    public static final String MALL_BUYER_WULIU = CommonAppConfig.HOST + "/appapi/Express/index?";
    //退款协商历史
    public static final String MALL_REFUND_HISTORY = CommonAppConfig.HOST + "/appapi/Goodsorderrefund/index?";
    //商城提现记录
    public static final String MALL_CASH_RECORD = CommonAppConfig.HOST + "/appapi/shopcash/index?";
    //付费内容实名认证
    public static final String MALL_PAY_AUTH = CommonAppConfig.HOST + "/appapi/Auth/index?";
    //付费内容管理规范说明
    public static final String MALL_PAY_APPLY_TIP = CommonAppConfig.HOST + "/portal/page/index?id=40";
    //什么是基本功能模式
    public static final String BASE_FUNCTION = CommonAppConfig.HOST + "/portal/page/index?id=45";
    //星球探宝规则说明
    public static final String GAME_RULE_XQTB = CommonAppConfig.HOST + "/appapi/game/xqtb.html";
    //幸运大转盘规则说明
    public static final String GAME_RULE_LUCKPAN = CommonAppConfig.HOST + "/appapi/game/xydzp.html";
}
