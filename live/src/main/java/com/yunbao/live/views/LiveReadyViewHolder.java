package com.yunbao.live.views;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.Constants;
import com.yunbao.common.activity.AbsActivity;
import com.yunbao.common.event.LocationCityEvent;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.interfaces.ActivityResultCallback;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.interfaces.ImageResultCallback;
import com.yunbao.common.mob.MobCallback;
import com.yunbao.common.upload.UploadBean;
import com.yunbao.common.upload.UploadCallback;
import com.yunbao.common.upload.UploadStrategy;
import com.yunbao.common.upload.UploadUtil;
import com.yunbao.common.utils.ActivityResultUtil;
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.LocationUtil;
import com.yunbao.common.utils.MediaUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.live.R;
import com.yunbao.live.activity.LiveActivity;
import com.yunbao.live.activity.LiveAnchorActivity;
import com.yunbao.live.activity.LiveChooseClassActivity;
import com.yunbao.live.bean.LiveRoomTypeBean;
import com.yunbao.live.dialog.LiveRoomTypeDialogFragment;
import com.yunbao.live.dialog.LiveShareDialogFragment;
import com.yunbao.live.dialog.LiveTimeDialogFragment;
import com.yunbao.live.http.LiveHttpConsts;
import com.yunbao.live.http.LiveHttpUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cxf on 2018/10/7.
 * 开播前准备
 */

public class LiveReadyViewHolder extends AbsViewHolder implements View.OnClickListener {

    private ImageView mAvatar;
    private TextView mCoverText;
    private EditText mEditTitle;
    private File mAvatarFile;
    private TextView mCity;
    private ImageView mLocationImg;
    private TextView mLiveClass;
    private TextView mLiveTypeTextView;//房间类型TextView
    private int mLiveClassID;//直播频道id
    private int mLiveType;//房间类型
    private int mLiveTypeVal;//门票收费金额
    private String mLivePwd = "";//房间密码，
    private int mLiveTimeCoin;//计时收费金额
    private boolean mOpenLocation = true;
    private int mLiveSdk;
    private int haveStore;
    private boolean mShopChecked;
    private ImageView mShopImage;
    private TextView mShopText;
    private String mForbidLiveTip;//直播间封禁提示
    private boolean mIsCreateRoom;
    private Dialog mLoading;
    private boolean mIsScreenRecord;


    public LiveReadyViewHolder(Context context, ViewGroup parentView, int liveSdk, int haveStore, String forbidLiveTip, boolean isScreenRecord) {
        super(context, parentView, liveSdk, haveStore, forbidLiveTip, isScreenRecord);
    }

    @Override
    protected void processArguments(Object... args) {
        if (args.length > 0) {
            mLiveSdk = (int) args[0];
        }
        if (args.length > 1) {
            haveStore = (int) args[1];
        }
        if (args.length > 2) {
            mForbidLiveTip = (String) args[2];
        }
        if (args.length > 3) {
            mIsScreenRecord = (boolean) args[3];
        }
    }

    @Override
    protected int getLayoutId() {
//        if (((LiveActivity) mContext).isVoiceChatRoom()) {
//            return R.layout.view_live_ready_voice;
//        }
        return R.layout.view_live_ready;
    }

    @Override
    public void init() {
        setStatusHeight();
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mCoverText = (TextView) findViewById(R.id.cover_text);
        mEditTitle = (EditText) findViewById(R.id.edit_title);
        mCity = (TextView) findViewById(R.id.city);
        mCity.setText(CommonAppConfig.getInstance().getCity());
        mLocationImg = (ImageView) findViewById(R.id.location_img);
        findViewById(R.id.btn_locaiton).setOnClickListener(this);
        mOpenLocation = true;

        findViewById(R.id.avatar_group).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_start_live).setOnClickListener(this);
        findViewById(R.id.btn_share).setOnClickListener(this);
        TextView tvForbidTip = findViewById(R.id.forbid_tip);
        if (tvForbidTip != null) {
            tvForbidTip.setText(mForbidLiveTip);
        }
        if (!((LiveActivity) mContext).isChatRoom()) {
            mLiveTypeTextView = findViewById(R.id.tv_room_type);
            findViewById(R.id.btn_room_type).setOnClickListener(this);
            mLiveClass = findViewById(R.id.live_class);
            findViewById(R.id.btn_live_class).setOnClickListener(this);
            if (mIsScreenRecord) {
                findViewById(R.id.btn_camera).setVisibility(View.GONE);
                findViewById(R.id.btn_beauty).setVisibility(View.GONE);
            } else {
                findViewById(R.id.btn_camera).setOnClickListener(this);
                findViewById(R.id.btn_beauty).setOnClickListener(this);
            }
            View btnShop = findViewById(R.id.btn_shop);
            btnShop.setOnClickListener(this);
            mShopImage = findViewById(R.id.shop_img);
            mShopText = findViewById(R.id.shop_text);
            if (haveStore != 1) {
                btnShop.setVisibility(View.GONE);
            }
        }
        if (CommonAppConfig.getInstance().getLat() == 0 || CommonAppConfig.getInstance().getLng() == 0) {
            EventBus.getDefault().register(this);
            if (((AbsActivity) mContext).hasLocationPermission()) {
                LocationUtil.getInstance().startLocation();
            } else {
                ((AbsActivity) mContext).checkLocationPermission(new Runnable() {
                    @Override
                    public void run() {
                        LocationUtil.getInstance().startLocation();
                    }
                });
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (!canClick()) {
            return;
        }
        int i = v.getId();
        if (i == R.id.avatar_group) {
            setAvatar();

        } else if (i == R.id.btn_camera) {
            toggleCamera();

        } else if (i == R.id.btn_close) {
            close();

        } else if (i == R.id.btn_live_class) {
            chooseLiveClass();

        } else if (i == R.id.btn_beauty) {
            beauty();

        } else if (i == R.id.btn_room_type) {
            chooseLiveType();

        } else if (i == R.id.btn_start_live) {
            startLive();
        } else if (i == R.id.btn_locaiton) {
            switchLocation();
        } else if (i == R.id.btn_shop) {
            setShopSwitch();
        } else if (i == R.id.btn_share) {
            shareLive();
        }
    }

    private void setShopSwitch() {
        mShopChecked = !mShopChecked;
        if (mShopImage != null) {
            mShopImage.setImageResource(mShopChecked ? R.mipmap.icon_live_ready_shop_1 : R.mipmap.icon_live_ready_shop_0);
        }
        if (mShopText != null) {
            mShopText.setTextColor(ContextCompat.getColor(mContext, mShopChecked ? R.color.global : R.color.white));
        }
    }

    private void shareLive() {
        LiveShareDialogFragment fragment = new LiveShareDialogFragment();
        fragment.setActionListener(new LiveShareDialogFragment.ActionListener() {
            @Override
            public void onItemClick(String type) {
                if (Constants.LINK.equals(type)) {
                    ((LiveActivity) mContext).copyLink();
                } else {
                    ((LiveActivity) mContext).shareLive(type, mEditTitle.getText().toString().trim(), new MobCallback() {
                        @Override
                        public void onSuccess(Object data) {
                            LiveHttpUtil.dailyTaskShareLive();
                        }

                        @Override
                        public void onError() {

                        }

                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onFinish() {
                        }
                    });
                }
            }
        });
        fragment.show(((LiveActivity) mContext).getSupportFragmentManager(), "LiveShareDialogFragment");
    }

    /**
     * 打开 关闭位置
     */
    private void switchLocation() {
        if (mOpenLocation) {
            new DialogUitl.Builder(mContext)
                    .setContent(WordUtil.getString(R.string.live_location_close_3))
                    .setCancelable(true)
                    .setConfrimString(WordUtil.getString(R.string.live_location_close_2))
                    .setClickCallback(new DialogUitl.SimpleCallback() {

                        @Override
                        public void onConfirmClick(Dialog dialog, String content) {
                            toggleLocation();
                        }
                    })
                    .build()
                    .show();
        } else {
            toggleLocation();
        }
    }

    private void toggleLocation() {
        mOpenLocation = !mOpenLocation;
        if (mLocationImg != null) {
            mLocationImg.setImageResource(mOpenLocation ? R.mipmap.icon_live_ready_location_1 : R.mipmap.icon_live_ready_location_0);
        }
        if (mCity != null) {
            mCity.setText(mOpenLocation ? CommonAppConfig.getInstance().getCity() : WordUtil.getString(R.string.live_location_close));
        }
    }

    /**
     * 设置头像
     */
    private void setAvatar() {
        final ImageResultCallback imageResultCallback = new ImageResultCallback() {

            @Override
            public void beforeCamera() {
                ((LiveAnchorActivity) mContext).beforeCamera();
            }

            @Override
            public void onSuccess(File file) {
                if (file != null) {
                    ImgLoader.display(mContext, file, mAvatar);
                    if (mAvatarFile == null) {
                        mCoverText.setText(WordUtil.getString(R.string.live_cover_2));
                        mCoverText.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_live_cover));
                    }
                    mAvatarFile = file;
                }
            }

            @Override
            public void onFailure() {
            }
        };
        if (mLiveSdk == Constants.LIVE_SDK_TX) {
            MediaUtil.getImageByAlumb((AbsActivity) mContext, imageResultCallback);
        } else {
            DialogUitl.showStringArrayDialog(mContext, new Integer[]{
                    R.string.alumb, R.string.camera}, new DialogUitl.StringArrayDialogCallback() {
                @Override
                public void onItemClick(String text, int tag) {
                    if (tag == R.string.camera) {
                        MediaUtil.getImageByCamera((AbsActivity) mContext, imageResultCallback);
                    } else {
                        MediaUtil.getImageByAlumb((AbsActivity) mContext, imageResultCallback);
                    }
                }
            });
        }
    }

    /**
     * 切换镜头
     */
    private void toggleCamera() {
        ((LiveAnchorActivity) mContext).toggleCamera();
    }

    /**
     * 关闭
     */
    private void close() {
        ((LiveAnchorActivity) mContext).onBackPressed();
    }

    /**
     * 选择直播频道
     */
    private void chooseLiveClass() {
        Intent intent = new Intent(mContext, LiveChooseClassActivity.class);
        intent.putExtra(Constants.CLASS_ID, mLiveClassID);
        ActivityResultUtil.startActivityForResult(((AbsActivity) mContext), intent, new ActivityResultCallback() {
            @Override
            public void onSuccess(Intent intent) {
                mLiveClassID = intent.getIntExtra(Constants.CLASS_ID, 0);
                mLiveClass.setText(intent.getStringExtra(Constants.CLASS_NAME));
            }
        });
    }

    /**
     * 设置美颜
     */
    private void beauty() {
        ((LiveAnchorActivity) mContext).beauty();
    }

    /**
     * 选择直播类型
     */
    private void chooseLiveType() {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.CHECKED_ID, mLiveType);
        LiveRoomTypeDialogFragment fragment = new LiveRoomTypeDialogFragment();
        fragment.setArguments(bundle);
        fragment.setCallback(new CommonCallback<LiveRoomTypeBean>() {
            @Override
            public void callback(LiveRoomTypeBean bean) {
                switch (bean.getId()) {
                    case Constants.LIVE_TYPE_NORMAL:
                        onLiveTypeNormal(bean);
                        break;
                    case Constants.LIVE_TYPE_PWD:
                        onLiveTypePwd(bean);
                        break;
                    case Constants.LIVE_TYPE_PAY:
                        onLiveTypePay(bean);
                        break;
                    case Constants.LIVE_TYPE_TIME:
                        onLiveTypeTime(bean);
                        break;
                }
            }
        });
        fragment.show(((LiveAnchorActivity) mContext).getSupportFragmentManager(), "LiveRoomTypeDialogFragment");
    }

    /**
     * 普通房间
     */
    private void onLiveTypeNormal(LiveRoomTypeBean bean) {
        mLiveType = bean.getId();
        mLiveTypeTextView.setText(bean.getName());
        mLiveTypeVal = 0;
        mLiveTimeCoin = 0;
        mLivePwd = "";
    }

    /**
     * 密码房间
     */
    private void onLiveTypePwd(final LiveRoomTypeBean bean) {
        new DialogUitl.Builder(mContext)
                .setTitle(WordUtil.getString(R.string.live_set_pwd))
                .setCancelable(true)
                .setInput(true)
                .setHint(WordUtil.getString(R.string.login_input_pwd))
                .setInputType(DialogUitl.INPUT_TYPE_NUMBER_PASSWORD)
                .setClickCallback(new DialogUitl.SimpleCallback() {
                    @Override
                    public void onConfirmClick(Dialog dialog, String content) {
                        if (TextUtils.isEmpty(content)) {
                            ToastUtil.show(R.string.live_set_pwd_empty);
                        } else {
                            mLiveType = bean.getId();
                            mLiveTypeTextView.setText(bean.getName());
                            mLiveTypeVal = 0;
                            mLivePwd = content;
                            mLiveTimeCoin = 0;
                            dialog.dismiss();
                        }
                    }
                })
                .build()
                .show();
    }

    /**
     * 付费房间
     */
    private void onLiveTypePay(final LiveRoomTypeBean bean) {

        new DialogUitl.Builder(mContext)
                .setTitle(WordUtil.getString(R.string.live_set_fee))
                .setCancelable(true)
                .setInput(true)
                .setHint(WordUtil.getString(R.string.mall_339))
                .setInputType(DialogUitl.INPUT_TYPE_NUMBER)
                .setLength(8)
                .setClickCallback(new DialogUitl.SimpleCallback() {
                    @Override
                    public void onConfirmClick(Dialog dialog, String content) {
                        if (TextUtils.isEmpty(content)) {
                            ToastUtil.show(R.string.live_set_fee_empty);
                        } else {
                            mLiveType = bean.getId();
                            mLiveTypeTextView.setText(bean.getName());
                            if (StringUtil.isInt(content)) {
                                mLiveTypeVal = Integer.parseInt(content);
                            }
                            mLivePwd = "";
                            mLiveTimeCoin = 0;
                            dialog.dismiss();
                        }
                    }
                })
                .build()
                .show();


    }

    /**
     * 计时房间
     */
    private void onLiveTypeTime(final LiveRoomTypeBean bean) {
        LiveTimeDialogFragment fragment = new LiveTimeDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.CHECKED_COIN, mLiveTimeCoin);
        fragment.setArguments(bundle);
        fragment.setCommonCallback(new CommonCallback<Integer>() {
            @Override
            public void callback(Integer coin) {
                mLiveType = bean.getId();
                mLiveTypeTextView.setText(bean.getName());
                mLiveTypeVal = coin;
                mLivePwd = "";
                mLiveTimeCoin = coin;
            }
        });
        fragment.show(((LiveAnchorActivity) mContext).getSupportFragmentManager(), "LiveTimeDialogFragment");
    }

    public void hide() {
        if (mContentView != null && mContentView.getVisibility() == View.VISIBLE) {
            mContentView.setVisibility(View.INVISIBLE);
        }
    }


    public void show() {
        if (mContentView != null && mContentView.getVisibility() != View.VISIBLE) {
            mContentView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 点击开始直播按钮
     */
    private void startLive() {
        if (!((LiveActivity) mContext).isChatRoom()) {
            boolean startPreview = ((LiveAnchorActivity) mContext).isStartPreview();
            if (!startPreview) {
                ToastUtil.show(R.string.please_wait);
                return;
            }
            if (mLiveClassID == 0) {
                ToastUtil.show(R.string.live_choose_live_class);
                return;
            }
        }
        createRoom();
    }


    private void showLoading() {
        if (mLoading == null) {
            mLoading = DialogUitl.loadingDialog(mContext);
            mLoading.show();
        }
    }

    private void hideLoading() {
        if (mLoading != null) {
            mLoading.dismiss();
            mLoading = null;
        }
    }

    /**
     * 请求创建直播间接口，开始直播
     */
    private void createRoom() {
        if (!((LiveActivity) mContext).isChatRoom()) {
            if (mLiveClassID == 0) {
                ToastUtil.show(R.string.live_choose_live_class);
                return;
            }
        }
        final String title = mEditTitle.getText().toString().trim();
        int isShop = 0;
        if (!((LiveActivity) mContext).isChatRoom() && mShopChecked) {
            isShop = 1;
        }
        final int finalShop = isShop;
        final String typeVal = mLiveType == Constants.LIVE_TYPE_PWD ? mLivePwd : String.valueOf(mLiveTypeVal);
        if (mAvatarFile != null && mAvatarFile.exists()) {
            showLoading();
            UploadUtil.startUpload(new CommonCallback<UploadStrategy>() {
                @Override
                public void callback(UploadStrategy strategy) {
                    List<UploadBean> list = new ArrayList<>();
                    list.add(new UploadBean(mAvatarFile, UploadBean.IMG));
                    strategy.upload(list, false, new UploadCallback() {
                        @Override
                        public void onFinish(List<UploadBean> list, boolean success) {
                            if (success) {
                                if (!mIsCreateRoom) {
                                    mIsCreateRoom = true;
                                    LiveHttpUtil.createRoom(title, mLiveClassID, mLiveType, typeVal, finalShop, list.get(0).getRemoteFileName(), mOpenLocation, 0,0, new HttpCallback() {
                                        @Override
                                        public void onSuccess(int code, String msg, String[] info) {
                                            if (code == 0 && info.length > 0) {
                                                L.e("开播", "createRoom------->" + info[0]);

                                                ((LiveAnchorActivity) mContext).startLiveSuccess(info[0], mLiveType, mLiveTypeVal, title);
                                                ((LiveAnchorActivity) mContext).openShop(finalShop == 1);
                                            } else {
                                                ToastUtil.show(msg);
                                            }
                                        }

                                        @Override
                                        public void onFinish() {
                                            hideLoading();
                                        }
                                    });
                                }
                            } else {
                                hideLoading();
                            }
                        }
                    });
                }
            });
        } else {
            if (!mIsCreateRoom) {
                mIsCreateRoom = true;
                LiveHttpUtil.createRoom(title, mLiveClassID, mLiveType, typeVal, finalShop, null, mOpenLocation, 0,0, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            L.e("开播", "createRoom------->" + info[0]);

                            ((LiveAnchorActivity) mContext).startLiveSuccess(info[0], mLiveType, mLiveTypeVal, title);
                            ((LiveAnchorActivity) mContext).openShop(finalShop == 1);
                        } else {
                            ToastUtil.show(msg);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void release() {
        super.release();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        UploadUtil.cancelUpload();
        LiveHttpUtil.cancel(LiveHttpConsts.CREATE_ROOM);
        hideLoading();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationCityEvent(LocationCityEvent e) {
        if (mOpenLocation) {
            if (mCity != null) {
                mCity.setText(e.getCity());
            }
        }
    }
}
