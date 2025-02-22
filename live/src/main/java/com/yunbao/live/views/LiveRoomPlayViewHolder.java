package com.yunbao.live.views;

import android.content.Context;
import android.view.ViewGroup;

import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.live.interfaces.ILiveLinkMicViewHolder;

/**
 * Created by cxf on 2018/10/25.
 */

public abstract class LiveRoomPlayViewHolder extends AbsViewHolder implements ILiveLinkMicViewHolder {

    public LiveRoomPlayViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public abstract void play(String url);

    public abstract void stopPlay();

    public abstract void resumePlay();

    public abstract void pausePlay();

    public abstract void hideCover();

    public abstract void release();

    public abstract void setCover(String coverUrl);

    public void playAgora(String url,boolean isVideo,String agoraToken, String channelId,int liveUid) {

    }


    /**
     * 声网sdk--> 开始/关闭 观众连麦推流
     *
     * @param isPush true开始推流 ，false结束推流
     */
    public void toggleLinkMicPushAgora(boolean isPush) {

    }

    /**
     * 设置主播连麦模式
     *
     * @param anchorLinkMic
     */
    public void setAnchorLinkMic(boolean anchorLinkMic, int delayTime) {

    }

    public void changeSize(boolean landscape) {

    }
}
