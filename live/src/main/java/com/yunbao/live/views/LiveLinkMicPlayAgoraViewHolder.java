package com.yunbao.live.views;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.live2.V2TXLiveDef;
import com.tencent.live2.V2TXLivePlayer;
import com.tencent.live2.V2TXLivePlayerObserver;
import com.tencent.live2.impl.V2TXLivePlayerImpl;
import com.yunbao.common.utils.L;
import com.yunbao.live.R;

/**
 * Created by cxf on 2018/10/25.
 * 连麦播放小窗口  使用声网sdk
 */

public class LiveLinkMicPlayAgoraViewHolder extends AbsLiveLinkMicPlayViewHolder {

//    private static final String TAG = "LiveLinkMicPlayAgoraViewHolder";

    public LiveLinkMicPlayAgoraViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_link_mic_play_agora;
    }

    @Override
    public void init() {
        mBtnClose = findViewById(R.id.btn_close_link_mic);
    }

    @Override
    public void setOnCloseListener(View.OnClickListener onClickListener) {
        if (onClickListener != null) {
            mBtnClose.setVisibility(View.VISIBLE);
            mBtnClose.setOnClickListener(onClickListener);
        }
    }

    /**
     * 开始播放
     *
     * @param url 流地址
     */
    @Override
    public void play(final String url) {

    }

    @Override
    public void release() {

    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }
}
