package com.yunbao.im.interfaces;

import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.im.bean.ImConversationBean;
import com.yunbao.im.bean.ImMessageBean;
import com.yunbao.im.utils.ImMessageUtil;
import com.yunbao.im.utils.ImUnReadCount;

import java.io.File;
import java.util.List;

/**
 * Created by cxf on 2019/4/1.
 */

public interface ImClient {

    /**
     * 初始化
     */
    void init();

    /**
     * 登录IM
     */
    void loginImClient(String uid);

    /**
     * 登出IM
     */
    void logoutImClient();

    /**
     * 获取会话列表
     */
    void getConversationList(CommonCallback<List<ImConversationBean>> callback);

    /**
     * 获取消息列表
     */
    void getMessageList(String toUid, CommonCallback<List<ImMessageBean>> callback);

    /**
     * 获取未读消息数
     */
    ImUnReadCount getUnReadMsgCount();

    /**
     * 设置某个会话的消息为已读
     *
     * @param toUid 对方uid
     */
    void markConversationAsRead(String toUid);

    /**
     * 标记所有会话为已读  即忽略所有未读
     */
    void markAllConversationAsRead();

    /**
     * 获取图片文件
     * @param thumbnail 是否是缩略图
     */
    void getImageFile(ImMessageBean bean, boolean thumbnail, CommonCallback<File> commonCallback);


    /**
     * 获取语音文件
     */
    void getSoundFile(ImMessageBean bean, CommonCallback<File> commonCallback);

    /**
     * 发送单聊文本消息
     *
     * @param text  消息内容
     * @param toUid 对方的uid
     */
    void sendC2CTextMessage(String text, String toUid, ImMessageUtil.SendMsgCallback callback);

    /**
     * 发送单聊图片消息
     *
     * @param filePath 图片文件本地路径
     * @param toUid    对方的uid
     */
    void sendC2CImageMessage(String filePath, String toUid, ImMessageUtil.SendMsgCallback callback);

    /**
     * 发送单聊语音消息
     *
     * @param filePath 语音文件本地路径
     * @param toUid    对方的uid
     * @param duration 语音时长，单位 s
     */
    void sendC2CSoundMessage(String filePath, int duration, String toUid, ImMessageUtil.SendMsgCallback callback);

    /**
     * 发送单聊位置消息
     *
     * @param address 详细地址
     * @param lng     经度
     * @param lat     纬度
     * @param toUid   对方的uid
     */
    void sendC2CLocationMessage(String address, double lng, double lat, String toUid, ImMessageUtil.SendMsgCallback callback);

    /**
     * 删除会话记录
     */
    void removeConversation(String uid);

    /**
     * 撤回消息
     */
    void revokeMessage(String toUid, String v2MsgId);

    /**
     * 发送商品消息
     */
    void sendGoodsMessage(List<String> toUids, String goodsJson, Runnable allComplete);

    /**
     * 是否打开聊天Activity
     */
    void setOpenChatActivity(boolean openChatActivity);

    void playRing();

    void changeString();
}
