package com.yunbao.common;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yunbao.common.bean.ConfigBean;
import com.yunbao.common.bean.LevelBean;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.event.CloseFloatWindowEvent;
import com.yunbao.common.http.CommonHttpUtil;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.DeviceUtils;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.LanguageUtil;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.WordUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cxf on 2017/8/4 .
 */

public class CommonAppConfig {
    public static final String PACKAGE_NAME = "com.yunbao.phonelive";
    //Http请求头 Header
    public static final Map<String, String> HEADER = new HashMap<>();
    //域名
    public static final String HOST = getHost();

    public static final String EXTERNAL_PATH = getExternalPath();
    public static final String DOWNLOAD_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
    public static final String VIDEO_PATH = EXTERNAL_PATH + "/video/";
    public static final String VIDEO_RECORD_PATH = VIDEO_PATH + "/record/";
    public static final String VIDEO_RECORD_PARTS_PATH = VIDEO_RECORD_PATH + "/parts/";
    //下载视频保存路径
    public static final String VIDEO_DOWNLOAD_PATH = DOWNLOAD_PATH + "/video/";
    //下载音乐保存路径
    public static final String MUSIC_PATH = EXTERNAL_PATH + "/music/";

    public static final String IMAGE_PATH = EXTERNAL_PATH + "/image/";
    //下载图片保存路径
    public static final String IMAGE_DOWNLOAD_PATH = DOWNLOAD_PATH + "/image/";
    //log保存路径
    public static final String LOG_PATH = EXTERNAL_PATH + "/log/";

    public static final String GIF_PATH = EXTERNAL_PATH + "/gif/";
    public static final String WATER_MARK_PATH = EXTERNAL_PATH + "/water/";
    public static final String IM_SOUND = EXTERNAL_PATH + "/im_sound/";
    public static final String IM_IMAGE = EXTERNAL_PATH + "/im_image/";
    //腾讯IM appId
    public static final int TX_IM_APP_ID = getMetaDataInt("TxIMAppId");
    //QQ登录是否与PC端互通
    public static final boolean QQ_LOGIN_WITH_PC = false;
    //是否使用游戏
    public static final boolean GAME_ENABLE = true;
    //是否上下滑动切换直播间
    public static final boolean LIVE_ROOM_SCROLL = true;

    private static String getExternalPath() {
        String outPath = null;
        try {
            File externalFilesDir = CommonAppContext.getInstance().getExternalFilesDir("yunbao");
            if (externalFilesDir != null) {
                if (!externalFilesDir.exists()) {
                    externalFilesDir.mkdirs();
                }
                outPath = externalFilesDir.getAbsolutePath();
            }
        } catch (Exception e) {
            outPath = null;
        }
        if (TextUtils.isEmpty(outPath)) {
            outPath = CommonAppContext.getInstance().getFilesDir().getAbsolutePath();
        }
        return outPath;
    }

    private static CommonAppConfig sInstance;

    private CommonAppConfig() {

    }

    public static CommonAppConfig getInstance() {
        if (sInstance == null) {
            synchronized (CommonAppConfig.class) {
                if (sInstance == null) {
                    sInstance = new CommonAppConfig();
                }
            }
        }
        return sInstance;
    }

    private String mUid;
    private String mToken;
    private ConfigBean mConfig;
    private double mLng;
    private double mLat;
    private String mProvince;//省
    private String mCity;//市
    private String mDistrict;//区
    private UserBean mUserBean;
    private UserBean mEmptyUserBean;//未登录游客
    private String mVersion;
    private boolean mLaunched;//App是否启动了
    private SparseArray<LevelBean> mLevelMap;
    private SparseArray<LevelBean> mAnchorLevelMap;
    private String mGiftListJson;
    private String mGiftDaoListJson;
    private String mTxMapAppKey;//腾讯定位，地图的AppKey
    private String mTxMapAppSecret;//腾讯地图的AppSecret
    private boolean mFrontGround;
    private int mAppIconRes;
    private String mAppName;
    private Boolean mMhBeautyEnable;//是否使用美狐 true使用美狐 false 使用基础美颜
    private String mDeviceId;
    private Boolean mTeenagerType;//是否是青少年模式
    private int mTopActivityType;//最上面的Activity的类型 1直播间 2消息
    private boolean mShowLiveFloatWindow;//退出直播后是否显示直播悬浮窗
    private String mShortPlayUrl = "http://154.82.73.4/appapi/playletvip/index";

    public String getShortPlayUrl() {
        return mShortPlayUrl;
    }

    public void setShortPlayUrl(String mShortPlayUrl) {
        this.mShortPlayUrl = mShortPlayUrl;
        Log.d("Sunday","mShortPlayUrl:"+mShortPlayUrl);
    }

    public String getUid() {
        if (TextUtils.isEmpty(mUid)) {
            String[] uidAndToken = SpUtil.getInstance()
                    .getMultiStringValue(new String[]{SpUtil.UID, SpUtil.TOKEN});
            if (uidAndToken != null) {
                if (!TextUtils.isEmpty(uidAndToken[0]) && !TextUtils.isEmpty(uidAndToken[1])) {
                    mUid = uidAndToken[0];
                    mToken = uidAndToken[1];
                } else {
                    mUid = Constants.NOT_LOGIN_UID;
                    mToken = Constants.NOT_LOGIN_TOKEN;
                }
            } else {
                mUid = Constants.NOT_LOGIN_UID;
                mToken = Constants.NOT_LOGIN_TOKEN;
            }
        }
        return mUid;
    }

    public String getToken() {
        return mToken;
    }

    public boolean isLogin() {
        return !Constants.NOT_LOGIN_UID.equals(getUid());
    }

    public String getCoinName() {
        ConfigBean configBean = getConfig();
        if (configBean != null) {
            return configBean.getCoinName();
        }
        return Constants.DIAMONDS;
    }

    public String getVotesName() {
        ConfigBean configBean = getConfig();
        if (configBean != null) {
            return configBean.getVotesName();
        }
        return Constants.VOTES;
    }


    public String getScoreName() {
        ConfigBean configBean = getConfig();
        if (configBean != null) {
            return configBean.getScoreName();
        }
        return Constants.SCORE;
    }

    public ConfigBean getConfig() {
        if (mConfig == null) {
            String configString = SpUtil.getInstance().getStringValue(SpUtil.CONFIG);
            if (!TextUtils.isEmpty(configString)) {
                mConfig = JSON.parseObject(configString, ConfigBean.class);
            }
        }
        return mConfig;
    }

    public void getConfig(CommonCallback<ConfigBean> callback) {
        if (callback == null) {
            return;
        }
        ConfigBean configBean = getConfig();
        if (configBean != null) {
            callback.callback(configBean);
        } else {
            CommonHttpUtil.getConfig(callback);
        }
    }

    public void setConfig(ConfigBean config) {
        mConfig = config;
    }


    /**
     * 经度
     */
    public double getLng() {
        if (mLng == 0) {
            String lng = SpUtil.getInstance().getStringValue(SpUtil.LOCATION_LNG);
            if (!TextUtils.isEmpty(lng)) {
                try {
                    mLng = Double.parseDouble(lng);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return mLng;
    }

    /**
     * 纬度
     */
    public double getLat() {
        if (mLat == 0) {
            String lat = SpUtil.getInstance().getStringValue(SpUtil.LOCATION_LAT);
            if (!TextUtils.isEmpty(lat)) {
                try {
                    mLat = Double.parseDouble(lat);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return mLat;
    }

    /**
     * 省
     */
    public String getProvince() {
        if (TextUtils.isEmpty(mProvince)) {
            mProvince = SpUtil.getInstance().getStringValue(SpUtil.LOCATION_PROVINCE);
        }
        return mProvince == null ? "" : mProvince;
    }

    /**
     * 市
     */
    public String getCity() {
        if (TextUtils.isEmpty(mCity)) {
            mCity = SpUtil.getInstance().getStringValue(SpUtil.LOCATION_CITY);
        }
        return mCity == null ? "" : mCity;
    }

    /**
     * 区
     */
    public String getDistrict() {
        if (TextUtils.isEmpty(mDistrict)) {
            mDistrict = SpUtil.getInstance().getStringValue(SpUtil.LOCATION_DISTRICT);
        }
        return mDistrict == null ? "" : mDistrict;
    }

    public void setUserBean(UserBean bean) {
        mUserBean = bean;
    }

    public UserBean getUserBean() {
        if (mUserBean == null) {
            String userBeanJson = SpUtil.getInstance().getStringValue(SpUtil.USER_INFO);
            if (!TextUtils.isEmpty(userBeanJson)) {
                mUserBean = JSON.parseObject(userBeanJson, UserBean.class);
            }
        }
        if (mUserBean == null) {
            mUserBean = getEmptyUserBean();
        }
        return mUserBean;
    }

    /**
     * 设置美狐是否可用
     */
    public void setMhBeautyEnable(boolean mhBeautyEnable) {
        mMhBeautyEnable = mhBeautyEnable;
        SpUtil.getInstance().setBooleanValue(SpUtil.MH_BEAUTY_ENABLE, mhBeautyEnable);
    }

    /**
     * 美狐是否可用
     */
    public boolean isMhBeautyEnable() {
        if (mMhBeautyEnable == null) {
            mMhBeautyEnable = SpUtil.getInstance().getBooleanValue(SpUtil.MH_BEAUTY_ENABLE);
        }
        return mMhBeautyEnable;
    }


    public void setTeenagerType(boolean teenagerType) {
        mTeenagerType = teenagerType;
        SpUtil.getInstance().setBooleanValue(SpUtil.TEENAGER, teenagerType);
    }

    public boolean isTeenagerType() {
        if (mTeenagerType == null) {
            mTeenagerType = SpUtil.getInstance().getBooleanValue(SpUtil.TEENAGER);
        }
        return mTeenagerType;
    }

    public void setShowLiveFloatWindow(boolean showLiveFloatWindow) {
        mShowLiveFloatWindow = showLiveFloatWindow;
    }

    public boolean isShowLiveFloatWindow() {
        return mShowLiveFloatWindow;
    }


    /**
     * 设置登录信息
     */
    public void setLoginInfo(String uid, String token, boolean save) {
        L.e("登录成功", "uid------>" + uid);
        L.e("登录成功", "token------>" + token);
        mUid = uid;
        mToken = token;
        if (save) {
            SpUtil.getInstance().setBooleanValue(SpUtil.TEENAGER_SHOW, false);
        }
        Map<String, String> map = new HashMap<>();
        map.put(SpUtil.UID, uid);
        map.put(SpUtil.TOKEN, token);
        SpUtil.getInstance().setMultiStringValue(map);

    }

    /**
     * 清除登录信息
     */
    public void clearLoginInfo() {
        mUid = null;
        mToken = null;
        SpUtil.getInstance().removeValue(
                SpUtil.UID, SpUtil.TOKEN, SpUtil.USER_INFO, SpUtil.TX_IM_USER_SIGN,
                Constants.CASH_ACCOUNT_ID, Constants.CASH_ACCOUNT, Constants.CASH_ACCOUNT_TYPE,
                SpUtil.TEENAGER, SpUtil.TEENAGER_SHOW
        );
        CommonAppConfig.getInstance().setUserBean(getEmptyUserBean());
        mShowLiveFloatWindow = false;
        EventBus.getDefault().post(new CloseFloatWindowEvent());
    }

    /**
     * 未登录，游客
     */
    private UserBean getEmptyUserBean() {
        if (mEmptyUserBean == null) {
            UserBean bean = new UserBean();
            bean.setId(Constants.NOT_LOGIN_UID);
            bean.setUserNiceName("游客");
            bean.setLevel(1);
            bean.setLevelAnchor(1);
            String defaultAvatar = CommonAppConfig.HOST + "/default.jpg";
            bean.setAvatar(defaultAvatar);
            bean.setAvatarThumb(defaultAvatar);
            bean.setSignature(Constants.EMPTY_STRING);
            bean.setCoin(Constants.EMPTY_STRING);
            bean.setVotes(Constants.EMPTY_STRING);
            bean.setConsumption(Constants.EMPTY_STRING);
            bean.setVotestotal(Constants.EMPTY_STRING);
            bean.setProvince(Constants.EMPTY_STRING);
            bean.setCity(Constants.EMPTY_STRING);
            bean.setLocation(Constants.EMPTY_STRING);
            bean.setBirthday(Constants.EMPTY_STRING);
            bean.setVip(new UserBean.Vip());
            bean.setLiang(new UserBean.Liang());
            bean.setCar(new UserBean.Car());
            mEmptyUserBean = bean;
        }
        return mEmptyUserBean;
    }

    /**
     * 设置位置信息
     *
     * @param lng 经度
     * @param lat 纬度
     */
    public void setLngLat(double lng, double lat) {
        mLng = lng;
        mLat = lat;
        Map<String, String> map = new HashMap<>();
        map.put(SpUtil.LOCATION_LNG, String.valueOf(lng));
        map.put(SpUtil.LOCATION_LAT, String.valueOf(lat));
        SpUtil.getInstance().setMultiStringValue(map);
    }

    /**
     * 设置位置信息
     *
     * @param lng      经度
     * @param lat      纬度
     * @param province 省
     * @param city     市
     */
    public void setLocationInfo(double lng, double lat, String province, String city, String district) {
        mLng = lng;
        mLat = lat;
        mProvince = province;
        mCity = city;
        mDistrict = district;
        Map<String, String> map = new HashMap<>();
        map.put(SpUtil.LOCATION_LNG, String.valueOf(lng));
        map.put(SpUtil.LOCATION_LAT, String.valueOf(lat));
        map.put(SpUtil.LOCATION_PROVINCE, province);
        map.put(SpUtil.LOCATION_CITY, city);
        map.put(SpUtil.LOCATION_DISTRICT, district);
        SpUtil.getInstance().setMultiStringValue(map);
    }

    /**
     * 清除定位信息
     */
    public void clearLocationInfo() {
        mLng = 0;
        mLat = 0;
        mProvince = null;
        mCity = null;
        mDistrict = null;
        SpUtil.getInstance().removeValue(
                SpUtil.LOCATION_LNG,
                SpUtil.LOCATION_LAT,
                SpUtil.LOCATION_PROVINCE,
                SpUtil.LOCATION_CITY,
                SpUtil.LOCATION_DISTRICT);

    }

    /**
     * 获取版本号
     */
    public String getVersion() {
        if (TextUtils.isEmpty(mVersion)) {
            try {
                PackageManager manager = CommonAppContext.getInstance().getPackageManager();
                PackageInfo info = manager.getPackageInfo(PACKAGE_NAME, 0);
                mVersion = info.versionName;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mVersion;
    }

    public static boolean isYunBaoApp() {
        if (!TextUtils.isEmpty(PACKAGE_NAME)) {
            return PACKAGE_NAME.contains("com.yunbao.phoneliv");
        }
        return false;
    }

    /**
     * 获取App名称
     */
    public String getAppName() {
        if (TextUtils.isEmpty(mAppName)) {
            int res = CommonAppContext.getInstance().getResources().getIdentifier("app_name", "string", "com.yunbao.phonelive");
            mAppName = WordUtil.getString(res);
        }
        return mAppName;
    }


    /**
     * 获取App图标的资源id
     */
    public int getAppIconRes() {
        if (mAppIconRes == 0) {
            mAppIconRes = CommonAppContext.getInstance().getResources().getIdentifier("icon_app", "mipmap", "com.yunbao.phonelive");
        }
        return mAppIconRes;
    }


    /**
     * 获取MetaData中的腾讯定位，地图的AppKey
     *
     * @return
     */
    public String getTxMapAppKey() {
        if (mTxMapAppKey == null) {
            mTxMapAppKey = getMetaDataString("TencentMapSDK");
        }
        return mTxMapAppKey;
    }


    /**
     * 获取MetaData中的腾讯定位，地图的AppSecret
     *
     * @return
     */
    public String getTxMapAppSecret() {
        if (mTxMapAppSecret == null) {
            mTxMapAppSecret = getMetaDataString("TencentMapAppSecret");
        }
        return mTxMapAppSecret;
    }


    public static String getMetaDataString(String key) {
        String res = null;
        try {
            ApplicationInfo appInfo = CommonAppContext.getInstance().getPackageManager().getApplicationInfo(PACKAGE_NAME, PackageManager.GET_META_DATA);
            res = appInfo.metaData.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static boolean getMetaDataBoolean(String key) {
        boolean res = false;
        try {
            ApplicationInfo appInfo = CommonAppContext.getInstance().getPackageManager().getApplicationInfo(PACKAGE_NAME, PackageManager.GET_META_DATA);
            res = appInfo.metaData.getBoolean(key);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static int getMetaDataInt(String key) {
        int res = 0;
        try {
            ApplicationInfo appInfo = CommonAppContext.getInstance().getPackageManager().getApplicationInfo(PACKAGE_NAME, PackageManager.GET_META_DATA);
            res = appInfo.metaData.getInt(key, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }

    private static String getHost() {
        String host = getMetaDataString("SERVER_HOST");
        HEADER.put("referer", host);
        return host;
    }


    /**
     * 保存用户等级信息
     */
    public void setLevel(String levelJson) {
        if (TextUtils.isEmpty(levelJson)) {
            return;
        }
        List<LevelBean> list = JSON.parseArray(levelJson, LevelBean.class);
        if (list == null || list.size() == 0) {
            return;
        }
        if (mLevelMap == null) {
            mLevelMap = new SparseArray<>();
        }
        mLevelMap.clear();
        for (LevelBean bean : list) {
            mLevelMap.put(bean.getLevel(), bean);
        }
    }

    /**
     * 保存主播等级信息
     */
    public void setAnchorLevel(String anchorLevelJson) {
        if (TextUtils.isEmpty(anchorLevelJson)) {
            return;
        }
        List<LevelBean> list = JSON.parseArray(anchorLevelJson, LevelBean.class);
        if (list == null || list.size() == 0) {
            return;
        }
        if (mAnchorLevelMap == null) {
            mAnchorLevelMap = new SparseArray<>();
        }
        mAnchorLevelMap.clear();
        for (LevelBean bean : list) {
            mAnchorLevelMap.put(bean.getLevel(), bean);
        }
    }

    /**
     * 获取用户等级
     */
    public LevelBean getLevel(int level) {
        if (mLevelMap == null) {
            String configString = SpUtil.getInstance().getStringValue(SpUtil.CONFIG);
            if (!TextUtils.isEmpty(configString)) {
                JSONObject obj = JSON.parseObject(configString);
                setLevel(obj.getString("level"));
            }
        }
        if (mLevelMap == null || mLevelMap.size() == 0) {
            return null;
        }
        return mLevelMap.get(level);
    }

    /**
     * 获取主播等级
     */
    public LevelBean getAnchorLevel(int level) {
        if (mAnchorLevelMap == null) {
            String configString = SpUtil.getInstance().getStringValue(SpUtil.CONFIG);
            if (!TextUtils.isEmpty(configString)) {
                JSONObject obj = JSON.parseObject(configString);
                setAnchorLevel(obj.getString("levelanchor"));
            }
        }
        if (mAnchorLevelMap == null || mAnchorLevelMap.size() == 0) {
            return null;
        }
        return mAnchorLevelMap.get(level);
    }

    public String getGiftListJson() {
        return mGiftListJson;
    }

    public void setGiftListJson(String getGiftListJson) {
        mGiftListJson = getGiftListJson;
    }


    public String getGiftDaoListJson() {
        return mGiftDaoListJson;
    }

    public void setGiftDaoListJson(String getGiftDaoListJson) {
        mGiftDaoListJson = getGiftDaoListJson;
    }


    /**
     * 判断某APP是否安装
     */
    public static boolean isAppExist(String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            PackageManager manager = CommonAppContext.getInstance().getPackageManager();
            List<PackageInfo> list = manager.getInstalledPackages(0);
            for (PackageInfo info : list) {
                if (packageName.equalsIgnoreCase(info.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }


    public boolean isLaunched() {
        return mLaunched;
    }

    public void setLaunched(boolean launched) {
        mLaunched = launched;
    }

    //app是否在前台
    public boolean isFrontGround() {
        return mFrontGround;
    }

    //app是否在前台
    public void setFrontGround(boolean frontGround) {
        mFrontGround = frontGround;
    }

    public String getDeviceId() {
        if (TextUtils.isEmpty(mDeviceId)) {
            String deviceId = SpUtil.getInstance().getStringValue(SpUtil.DEVICE_ID);
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = DeviceUtils.getDeviceId();
                SpUtil.getInstance().setStringValue(SpUtil.DEVICE_ID, deviceId);
            }
            mDeviceId = deviceId;
        }
        L.e("getDeviceId---mDeviceId-----> " + mDeviceId);
        return mDeviceId;
    }

    public int getTopActivityType() {
        return mTopActivityType;
    }

    public void setTopActivityType(int topActivityType) {
        mTopActivityType = topActivityType;
    }

    /**
     * 是否是基本功能模式
     */
    public boolean isBaseFunctionMode() {
        return SpUtil.getInstance().getBooleanValue(SpUtil.BASE_FUNCTION_MODE, true);
    }

    /**
     * 设置基本功能模式
     */
    public void setBaseFunctionMode(boolean baseFunctionMode) {
        SpUtil.getInstance().setBooleanValue(SpUtil.BASE_FUNCTION_MODE, baseFunctionMode);
    }

    public static String getHtmlUrl(String url) {
        if (!TextUtils.isEmpty(url) && url.startsWith(CommonAppConfig.HOST)) {
            if (!url.contains("?")) {
                url = StringUtil.contact(url, "?");
            }
            url = StringUtil.contact(url,
                    "&uid=", CommonAppConfig.getInstance().getUid(),
                    "&token=", CommonAppConfig.getInstance().getToken(),
                    "&", Constants.LANGUAGE, "=", LanguageUtil.getInstance().getLanguage()
            );
        }
        return url;
    }

    public boolean isPrivateMsgSwitchOpen() {
        ConfigBean configBean = getConfig();
        if (configBean != null) {
            return configBean.getPriMsgSwitch() == 1;
        }
        return false;
    }
}
