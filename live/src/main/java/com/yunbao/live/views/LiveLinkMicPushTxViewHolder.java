package com.yunbao.live.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.TextureView;
import android.view.ViewGroup;

import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePusher;
import com.tencent.live2.V2TXLivePusherObserver;
import com.tencent.live2.impl.V2TXLivePusherImpl;
import com.yunbao.common.utils.L;
import com.yunbao.live.R;

/**
 * Created by cxf on 2018/10/26.
 * 连麦推流小窗口  腾讯sdk
 */

public class LiveLinkMicPushTxViewHolder extends AbsLiveLinkMicPushViewHolder  {

    private static final String TAG = "LiveLinkMicPushTxViewHolder";
    private V2TXLivePusher mLivePusher;
    private boolean mPushSucceed;//是否推流成功

    public LiveLinkMicPushTxViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_link_mic_push_tx;
    }

    @Override
    public void init() {
        mLivePusher = new V2TXLivePusherImpl(mContext, V2TXLiveDef.V2TXLiveMode.TXLiveMode_RTC);
        V2TXLiveDef.V2TXLiveVideoEncoderParam videoParam = new V2TXLiveDef.V2TXLiveVideoEncoderParam(V2TXLiveDef.V2TXLiveVideoResolution.V2TXLiveVideoResolution480x360);
        videoParam.videoResolutionMode = V2TXLiveDef.V2TXLiveVideoResolutionMode.V2TXLiveVideoResolutionModePortrait;
        mLivePusher.setVideoQuality(videoParam);
        mLivePusher.setEncoderMirror(true);
        mLivePusher.setObserver(new V2TXLivePusherObserver() {

            @Override
            public void onError(int code, String msg, Bundle extraInfo) {
                L.e(TAG, "V2TXLivePusherObserver--onError: code=" + code + ", msg= " + msg);
            }

            @Override
            public void onWarning(int code, String msg, Bundle extraInfo) {
                L.e(TAG, "V2TXLivePusherObserver--onWarning: code=" + code + ", msg= " + msg);
            }

            @Override
            public void onCaptureFirstAudioFrame() {
                L.e(TAG, "V2TXLivePusherObserver--onCaptureFirstAudioFrame: 获得首帧音频");
            }

            @Override
            public void onCaptureFirstVideoFrame() {
                L.e(TAG, "V2TXLivePusherObserver--onCaptureFirstVideoFrame: 获得首帧视频");
                if (mLivePushListener != null) {
                    mLivePushListener.onPreviewStart();
                }
            }

            @Override
            public void onPushStatusUpdate(V2TXLiveDef.V2TXLivePushStatus status, String msg, Bundle extraInfo) {
                L.e(TAG, "V2TXLivePusherObserver--onPushStatusUpdate: msg=" + msg);
                if (status == V2TXLiveDef.V2TXLivePushStatus.V2TXLivePushStatusConnectSuccess) {
                    if (!mPushSucceed) {
                        mPushSucceed = true;
                        if (mLivePushListener != null) {
                            mLivePushListener.onPushStart();
                        }
                        L.e(TAG, "onPushStatusUpdate--->推流成功");
                    }
                }else if(status == V2TXLiveDef.V2TXLivePushStatus.V2TXLivePushStatusDisconnected){
                    L.e(TAG, "onPushStatusUpdate--->网络断开，推流失败");
                    if (mLivePushListener != null) {
                        mLivePushListener.onPushFailed();
                    }
                }
            }
        });
        mLivePusher.setRenderView((TextureView) findViewById(R.id.camera_preview));
        mLivePusher.startCamera(true);
        mLivePusher.startMicrophone();
    }

    /**
     * 开始推流
     *
     * @param pushUrl 推流地址
     */
    @Override
    public void startPush(String pushUrl) {
        if (mLivePusher != null) {
            mLivePusher.startPush(pushUrl);
        }
    }

    @Override
    public void release() {
        mLivePushListener = null;
        if (mLivePusher != null) {
            mLivePusher.stopPush();
            mLivePusher.stopMicrophone();
            mLivePusher.stopCamera();
            mLivePusher.setObserver(null);
            mLivePusher.release();
        }
        mLivePusher = null;
    }

    @Override
    public void pause() {
//        mPaused = true;
//        if (mStartPush && mLivePusher != null) {
//            mLivePusher.pausePusher();
//        }
    }

    @Override
    public void resume() {
//        if (mPaused && mStartPush && mLivePusher != null) {
//            mLivePusher.resumePusher();
//        }
//        mPaused = false;
    }


    private Bitmap decodeResource(Resources resources, int id) {
        TypedValue value = new TypedValue();
        resources.openRawResource(id, value);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inTargetDensity = value.density;
        return BitmapFactory.decodeResource(resources, id, opts);
    }

}
