package com.yunbao.live.views;

import android.content.Context;
import android.view.TextureView;
import android.view.ViewGroup;

/**
 * Created by http://www.yunbaokj.com on 2024/2/20.
 */
public abstract class AbsLiveChatRoomPlayViewHolder extends LiveRoomPlayViewHolder{


    public AbsLiveChatRoomPlayViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }


    public abstract ViewGroup getContainer() ;



    /**
     * 播放主播低延时流地址
     *
     * @param accPullUrl 低延时流地址，为空切回到普通流
     */
    public  void changeAccStream(String accPullUrl) {

    }
}
