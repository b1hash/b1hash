package com.yunbao.live.views;

import android.content.Context;
import android.view.TextureView;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.yunbao.common.bean.UserBean;
import com.yunbao.common.views.AbsViewHolder;
import com.yunbao.live.bean.LiveVoiceGiftBean;
import com.yunbao.live.bean.LiveVoiceLinkMicBean;
import com.yunbao.live.bean.LiveVoiceMixUserBean;
import com.yunbao.live.interfaces.LivePushListener;

import java.util.List;

/**
 * Created by http://www.yunbaokj.com on 2024/2/20.
 */
public abstract class AbsLiveChatRoomLinkMicViewHolder extends AbsViewHolder {
    public AbsLiveChatRoomLinkMicViewHolder(Context context, ViewGroup parentView) {
        super(context, parentView);
    }

    public AbsLiveChatRoomLinkMicViewHolder(Context context, ViewGroup parentView, Object... args) {
        super(context, parentView, args);
    }

    public abstract TextureView getFirstPreview();
    /**
     * 用户上麦
     *
     * @param toUid    上麦人员的uid
     * @param toName   上麦人员的name
     * @param toAvatar 上麦人员的头像
     * @param position 上麦人员的位置
     */
    public abstract void onUserUpMic(String toUid, String toName, String toAvatar, int position);

    /**
     * 语音聊天室--主播控制麦位 闭麦开麦禁麦等
     *
     * @param position 麦位
     * @param status   麦位的状态 -1 关麦；  0无人； 1开麦 ； 2 禁麦；
     */
    public abstract void onControlMicPosition(int position, int status);

    /**
     * 语音聊天室--收到上麦观众发送表情的消息
     *
     * @param uid       上麦观众的uid
     * @param faceIndex 表情标识
     */
    public abstract void onVoiceRoomFace(String uid, int faceIndex);

    public abstract List<LiveVoiceGiftBean> getVoiceGiftUserList();

    /**
     * 获取用户
     */
    public abstract LiveVoiceLinkMicBean getUserBean(int position);

    /**
     * 获取用户
     */
    public abstract LiveVoiceLinkMicBean getUserBean(String toUid);

    /**
     * 获取用户在麦上的位置
     */
    public abstract int getUserPosition(String uid);

    public abstract void setChatRoomType(int chatRoomType, UserBean anchorInfo);


    /**
     * 用户下麦
     *
     * @param uid 下麦人员的uid
     */
    public abstract void onUserDownMic(String uid);

    /**
     * 用户下麦
     *
     * @param position 下麦人员的position
     */
    public abstract void onUserDownMic(int position);

    /**
     * 停止播放
     */
    public abstract void stopPlay(String uid);

    /**
     * 停止播放
     */
    public abstract void stopPlay(int position);

    /**
     * 语音聊天室--播放上麦观众的低延时流
     *
     * @param uid        上麦观众的uid
     * @param pull       上麦观众的低延时流地址
     * @param userStream 上麦观众的流名，主播混流用
     */
    public abstract void playAccStream(String uid, String pull, String userStream);

    /**
     * 主播混流时候获取上麦用户的Stream
     */
    public abstract List<LiveVoiceMixUserBean> getUserStreamForMix();


    /**
     * 显示房间用户数据
     */
    public abstract void showUserList(JSONArray arr);

    /**
     * 开始推流
     */
    public abstract void startPush(String pushUrl, LivePushListener pushListener);

    /**
     * 停止推流
     */
    public void stopPush() {

    }

    /**
     * 停止所有播放
     */
    public void stopAllPlay() {

    }

    /**
     * 设置静音
     */
    public void setPushMute(boolean pushMute) {

    }
}
