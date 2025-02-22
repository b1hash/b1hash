package com.yunbao.live.floatwindow;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yunbao.common.CommonAppContext;
import com.yunbao.common.Constants;
import com.yunbao.common.glide.ImgLoader;
import com.yunbao.common.utils.DpUtil;
import com.yunbao.common.utils.FloatWindowHelper;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.ScreenDimenUtil;
import com.yunbao.common.utils.StringUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.live.R;
import com.yunbao.live.bean.LiveAudienceFloatWindowData;
import com.yunbao.live.bean.LiveBean;
import com.yunbao.live.event.LiveFloatWindowEvent;
import com.yunbao.live.views.LiveChatRoomPlayUtil;
import com.yunbao.live.views.LiveFloatAgoraViewHolder;
import com.yunbao.live.views.LiveFloatViewHolder;

import org.greenrobot.eventbus.EventBus;


public class FloatWindowUtil implements FloatWindowHelper.ActionListener {

    private static final String TAG = "float";
    private static FloatWindowUtil sInstance;

    private FloatWindow mFloatWindow;
    private LiveBean mLiveBean;
    private LiveAudienceFloatWindowData mLiveAudienceFloatWindowData;
    private int mType = Constants.FLOAT_TYPE_DEFAULT;
    private LiveFloatViewHolder mLiveFloatViewHolder;
    private Handler mHandler;

    private FloatWindowUtil() {
        FloatWindowHelper.setActionListener(this);
    }

    public static FloatWindowUtil getInstance() {
        if (sInstance == null) {
            synchronized (FloatWindowUtil.class) {
                if (sInstance == null) {
                    sInstance = new FloatWindowUtil();
                }
            }
        }
        return sInstance;
    }

    public FloatWindowUtil setType(int type) {
        mType = type;
        return this;
    }

    public FloatWindowUtil setLiveBean(LiveBean liveBean) {
        mLiveBean = liveBean;
        return this;
    }

    public FloatWindowUtil setLiveAudienceFloatWindowData(LiveAudienceFloatWindowData liveAudienceFloatWindowData) {
        mLiveAudienceFloatWindowData = liveAudienceFloatWindowData;
        return this;
    }

    public void requestPermission() {
        FloatWindowPermission.getInstance().requestPermission();
    }

    /**
     * 显示悬浮窗
     */
    public void show() {
        if (mLiveBean == null) {
            L.e(TAG, "----show---->mLiveBean==null");
            return;
        }
        if (mLiveBean.isVoiceRoom()) {
            L.e(TAG, "----show---->聊天室");
            View v = LayoutInflater.from(CommonAppContext.getInstance()).inflate(R.layout.view_float_window_voice, null);
            ImageView avatar = v.findViewById(R.id.avatar);
            ImgLoader.displayAvatar(CommonAppContext.getInstance(), mLiveBean.getThumb(), avatar);
            TextView name = v.findViewById(R.id.name);
            name.setText(mLiveBean.getUserNiceName());
            TextView idVal = v.findViewById(R.id.id_val);
            idVal.setText(StringUtil.contact("ID:", mLiveBean.getUid()));
//            v.findViewById(R.id.btn_open).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (mLiveBean != null) {
//                        EventBus.getDefault().post(new LiveFloatWindowEvent(mLiveBean, mType));
//                    }
//                    dismiss();
//                }
//            });
            v.measure(0, 0);
            ScreenDimenUtil screenDimenUtil = ScreenDimenUtil.getInstance();
            int screenW = screenDimenUtil.getScreenWidth();
            int screenH = screenDimenUtil.getScreenHeight();
            int w = v.getMeasuredWidth();
            int h = DpUtil.dp2px(40);
            int x = screenW - w;
            int y = screenH - h - DpUtil.dp2px(150);
            FloatWindow window = new FloatWindow();
            window.setWidth(w);
            window.setHeight(h);
            window.setX(x);
            window.setY(y);

            window.setView(v);
            window.setClickWindowCallback(new Runnable() {
                @Override
                public void run() {
                    if (mLiveBean != null) {
                        EventBus.getDefault().post(new LiveFloatWindowEvent(mLiveBean, mType));
                    }
                    dismiss();
                }
            });
            boolean res = window.show();
            if (res) {
                mFloatWindow = window;
            }
        } else {
            L.e(TAG, "----show---->直播间");
            if (mLiveAudienceFloatWindowData == null) {
                return;
            }
            ViewGroup parent = (ViewGroup) LayoutInflater.from(CommonAppContext.getInstance()).inflate(R.layout.view_float_window_live, null);
//            parent.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (mLiveBean != null) {
//                        EventBus.getDefault().post(new LiveFloatWindowEvent(mLiveBean, mType));
//                    }
//                    dismiss();
//                }
//            });
            final FloatWindow window = new FloatWindow();
            window.setWidth(0);
            window.setHeight(0);
            window.setX(0);
            window.setY(0);
            window.setView(parent);
            window.setClickWindowCallback(new Runnable() {
                @Override
                public void run() {
                    if (mLiveBean != null) {
                        EventBus.getDefault().post(new LiveFloatWindowEvent(mLiveBean, mType));
                    }
                    dismiss();
                }
            });
            boolean useAgora = true;
            if (mLiveAudienceFloatWindowData.isTxSDK()) {
                useAgora = false;
            } else {
                String pull = mLiveBean.getPull();
                if (!TextUtils.isEmpty(pull)) {
                    if (pull.startsWith("http://")
                            || pull.startsWith("https://")
                            || pull.contains(".mp4")
                            || pull.contains(".MP4")
                    ) {
                        useAgora = false;
                    }
                }
            }
            if (useAgora) {
                if (mHandler == null) {
                    mHandler = new Handler();
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        LiveFloatAgoraViewHolder vh = new LiveFloatAgoraViewHolder(CommonAppContext.getInstance(), parent);
                        vh.setMActionListener(new LiveFloatViewHolder.ActionListener() {
                            @Override
                            public void onVideoSizeChanged(int w, int h) {
                                if (mFloatWindow == null) {
                                    boolean res = window.show();
                                    if (res) {
                                        mFloatWindow = window;
                                    }
                                }
                                window.updateWindowSize(w, h);
                            }

                            @Override
                            public void onCloseClick() {
                                dismiss();
                            }
                        });
                        vh.addToParent();
                        vh.startPlayAgora(mLiveAudienceFloatWindowData);
                        mLiveFloatViewHolder = vh;
                    }
                });
            } else {
                LiveFloatViewHolder vh = new LiveFloatViewHolder(CommonAppContext.getInstance(), parent);
                vh.setMActionListener(new LiveFloatViewHolder.ActionListener() {
                    @Override
                    public void onVideoSizeChanged(int w, int h) {
                        if (mFloatWindow == null) {
                            boolean res = window.show();
                            if (res) {
                                mFloatWindow = window;
                            }
                        }
                        window.updateWindowSize(w, h);
                    }

                    @Override
                    public void onCloseClick() {
                        dismiss();
                    }
                });
                vh.addToParent();
                vh.startPlay(mLiveBean.getPull());
                mLiveFloatViewHolder = vh;
            }
        }
    }


    /**
     * 是否可以播放声音
     */
    @Override
    public boolean checkVoice(boolean enterLive) {
        if (enterLive) {
            LiveChatRoomPlayUtil.getInstance().setKeepAlive(false);
            LiveChatRoomPlayUtil.getInstance().release();
            dismiss();
            return true;
        } else {
            if (mFloatWindow != null && mLiveBean != null) {
                ToastUtil.show(R.string.a_059);
                return false;
            }
        }
        return true;
    }

    /**
     * 设置隐藏和显示
     */
    @Override
    public void setFloatWindowVisible(boolean visible) {
        if (mLiveFloatViewHolder != null) {
            mLiveFloatViewHolder.setMute(!visible);
        }
        if (mFloatWindow != null) {
            mFloatWindow.setVisible(visible);
        }
    }

    /**
     * 隐藏悬浮窗
     */
    public void dismiss() {
        L.e(TAG, "----dismiss---->");
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mLiveFloatViewHolder != null) {
            mLiveFloatViewHolder.stopPlay();
            mLiveFloatViewHolder = null;
        }
        if (mFloatWindow != null) {
            mFloatWindow.dismiss();
        }
        mFloatWindow = null;
        mLiveBean = null;
    }


    public void release() {
        LiveChatRoomPlayUtil.getInstance().setKeepAlive(false);
        LiveChatRoomPlayUtil.getInstance().release();
        dismiss();
    }
}
