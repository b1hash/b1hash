package com.yunbao.live.views;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.yunbao.common.utils.DialogUitl;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.LocationUtil;
import com.yunbao.common.utils.MediaUtil;
import com.yunbao.common.utils.ScreenDimenUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.live.R;
import com.yunbao.live.activity.LiveActivity;
import com.yunbao.live.activity.LiveAnchorActivity;
import com.yunbao.live.adapter.LiveReadyShareAdapter;
import com.yunbao.live.bean.LiveRoomTypeBean;
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

public class LiveReadyChatRoomViewHolder extends LiveReadyViewHolder implements View.OnClickListener {

    private ImageView mAvatar;
    private TextView mCoverText;
    private EditText mEditTitle;
    private RecyclerView mLiveShareRecyclerView;
    private LiveReadyShareAdapter mLiveShareAdapter;
    private File mAvatarFile;
    private TextView mCity;
    private ImageView mLocationImg;
    private int mLiveClassID;//直播频道id
    private int mLiveType;//房间类型
    private int mLiveTypeVal;//门票收费金额
    private String mLivePwd = "";//房间密码，
    private int mLiveTimeCoin;//计时收费金额
    private ActivityResultCallback mActivityResultCallback;
    private CommonCallback<LiveRoomTypeBean> mLiveTypeCallback;
    private boolean mOpenLocation = true;
    private int mLiveSdk;
    private int haveStore;
    private String mForbidLiveTip;//直播间封禁提示
    private boolean mIsCreateRoom;
    private View mBtnVoiceType;
    private View mBtnVideoType;
    private ImageView mIconVoiceType;
    private ImageView mIconVideoType;
    private int mChatRoomType = Constants.CHAT_ROOM_TYPE_VOICE;


    public LiveReadyChatRoomViewHolder(Context context, ViewGroup parentView, int liveSdk, int haveStore, String forbidLiveTip) {
        super(context, parentView, liveSdk, haveStore, forbidLiveTip, false);
        View group_1 = findViewById(R.id.group_1);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) group_1.getLayoutParams();
        params.topMargin = ScreenDimenUtil.getInstance().getStatusBarHeight() + DpUtil.dp2px(5);
        group_1.requestLayout();
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

    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_live_ready_voice_0;
    }

    @Override
    public void init() {
        mBtnVoiceType = findViewById(R.id.btn_room_type_voice);
        mBtnVideoType = findViewById(R.id.btn_room_type_video);
        mIconVoiceType = findViewById(R.id.icon_room_type_voice);
        mIconVideoType = findViewById(R.id.icon_room_type_video);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.btn_room_type_voice) {
                    setChatRoomType(Constants.CHAT_ROOM_TYPE_VOICE);
                } else if (i == R.id.btn_room_type_video) {
                    setChatRoomType(Constants.CHAT_ROOM_TYPE_VIDEO);
                }
            }
        };
        mBtnVoiceType.setOnClickListener(onClickListener);
        mBtnVideoType.setOnClickListener(onClickListener);
        mAvatar = (ImageView) findViewById(R.id.avatar);
        mCoverText = (TextView) findViewById(R.id.cover_text);
        mEditTitle = (EditText) findViewById(R.id.edit_title);
        mCity = (TextView) findViewById(R.id.city);
        mCity.setText(CommonAppConfig.getInstance().getCity());
        mLocationImg = (ImageView) findViewById(R.id.location_img);
        findViewById(R.id.btn_locaiton).setOnClickListener(this);
        mOpenLocation = true;

        mLiveShareRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLiveShareRecyclerView.setHasFixedSize(true);
        mLiveShareRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mLiveShareAdapter = new LiveReadyShareAdapter(mContext);
        mLiveShareRecyclerView.setAdapter(mLiveShareAdapter);
        findViewById(R.id.avatar_group).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.btn_start_live).setOnClickListener(this);

        TextView tvForbidTip = findViewById(R.id.forbid_tip);
        if (tvForbidTip != null) {
            tvForbidTip.setText(mForbidLiveTip);
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

        } else if (i == R.id.btn_close) {
            close();

        } else if (i == R.id.btn_start_live) {
            startLive();

        } else if (i == R.id.btn_locaiton) {
            switchLocation();
        }
    }

    private void setChatRoomType(int chatRoomType) {
        if (mChatRoomType != chatRoomType) {
            mChatRoomType = chatRoomType;
            if (mChatRoomType == Constants.CHAT_ROOM_TYPE_VOICE) {
                if (mBtnVoiceType != null) {
                    mBtnVoiceType.setBackgroundResource(R.drawable.bg_live_room_type_1);
                }
                if (mBtnVideoType != null) {
                    mBtnVideoType.setBackgroundResource(R.drawable.bg_live_room_type_0);
                }
                if (mIconVoiceType != null) {
                    mIconVoiceType.setImageResource(R.mipmap.icon_live_room_voice_1);
                }
                if (mIconVideoType != null) {
                    mIconVideoType.setImageResource(R.mipmap.icon_live_room_video_0);
                }
            } else {
                if (mBtnVoiceType != null) {
                    mBtnVoiceType.setBackgroundResource(R.drawable.bg_live_room_type_0);
                }
                if (mBtnVideoType != null) {
                    mBtnVideoType.setBackgroundResource(R.drawable.bg_live_room_type_1);
                }
                if (mIconVoiceType != null) {
                    mIconVoiceType.setImageResource(R.mipmap.icon_live_room_voice_0);
                }
                if (mIconVideoType != null) {
                    mIconVideoType.setImageResource(R.mipmap.icon_live_room_video_1);
                }
            }
        }
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
     * 关闭
     */
    private void close() {
        ((LiveAnchorActivity) mContext).onBackPressed();
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

        if (mLiveShareAdapter != null) {
            String type = mLiveShareAdapter.getShareType();
            if (!TextUtils.isEmpty(type)) {
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
                        createRoom();
                    }
                });
            } else {
                createRoom();
            }
        } else {
            createRoom();
        }
    }

    /**
     * 请求创建直播间接口，开始直播
     */
    private void createRoom() {
        final String title = mEditTitle.getText().toString().trim();
        final int finalShop = 0;
        final String typeVal = mLiveType == Constants.LIVE_TYPE_PWD ? mLivePwd : String.valueOf(mLiveTypeVal);
        if (mAvatarFile != null && mAvatarFile.exists()) {
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
                                    LiveHttpUtil.createRoom(title, mLiveClassID, mLiveType, typeVal, finalShop, list.get(0).getRemoteFileName(), mOpenLocation, 1,mChatRoomType, new HttpCallback() {
                                        @Override
                                        public void onSuccess(int code, String msg, String[] info) {
                                            if (code == 0 && info.length > 0) {
                                                L.e("开播", "createRoom------->" + info[0]);
                                                ((LiveAnchorActivity) mContext).setChatRoomType(mChatRoomType);
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
                    });
                }
            });
        } else {
            if (!mIsCreateRoom) {
                mIsCreateRoom = true;
                LiveHttpUtil.createRoom(title, mLiveClassID, mLiveType, typeVal, finalShop, null, mOpenLocation, 1,mChatRoomType, new HttpCallback() {
                    @Override
                    public void onSuccess(int code, String msg, String[] info) {
                        if (code == 0 && info.length > 0) {
                            L.e("开播", "createRoom------->" + info[0]);
                            ((LiveAnchorActivity) mContext).setChatRoomType(mChatRoomType);
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

    public void release() {
        mActivityResultCallback = null;
        mLiveTypeCallback = null;
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
