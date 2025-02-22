package com.yunbao.im.utils;

import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.WordFilterUtil;
import com.yunbao.im.bean.ImConversationBean;
import com.yunbao.im.bean.ImMessageBean;
import com.yunbao.im.interfaces.ImClient;
import com.yunbao.im.tpns.TpnsUtil;

import java.io.File;
import java.util.List;

/**
 * Created by cxf on 2017/8/10.
 * IM各种功能
 */

public class ImMessageUtil {

    private static ImMessageUtil sInstance;
    private ImClient mImClient;

    private ImMessageUtil() {
        mImClient = new TxImMsgUtil();
    }

    public static ImMessageUtil getInstance() {
        if (sInstance == null) {
            synchronized (ImMessageUtil.class) {
                if (sInstance == null) {
                    sInstance = new ImMessageUtil();
                }
            }
        }
        return sInstance;
    }


    public void init() {
        if (mImClient != null) {
            mImClient.init();
        }
    }

    /**
     * 登录IM
     */
    public void loginImClient(String uid) {
        if (mImClient != null) {
            mImClient.loginImClient(uid);
        }
    }


    /**
     * 登出IM
     */
    public void logoutImClient() {
        if (mImClient != null) {
            mImClient.logoutImClient();
        }
        TpnsUtil.clearPushAccount();
    }

    /**
     * 获取会话列表
     */
    public void getConversationList(CommonCallback<List<ImConversationBean>> callback) {
        if (mImClient != null) {
            mImClient.getConversationList(callback);
        }
    }

    /**
     * 获取消息列表
     */
    public void getMessageList(String toUid, CommonCallback<List<ImMessageBean>> callback) {
        if (mImClient != null) {
            mImClient.getMessageList(toUid, callback);
        }
    }

    /**
     * 获取未读消息数
     */
    public ImUnReadCount getUnReadMsgCount() {
        if (mImClient != null) {
            return mImClient.getUnReadMsgCount();
        }
        return null;
    }

    /**
     * 设置某个会话的消息为已读
     *
     * @param toUid 对方uid
     */
    public void markConversationAsRead(String toUid) {
        if (mImClient != null) {
            mImClient.markConversationAsRead(toUid);
        }
    }

    /**
     * 标记所有会话为已读  即忽略所有未读
     */
    public void markAllConversationAsRead() {
        if (mImClient != null) {
            mImClient.markAllConversationAsRead();
        }
    }


    /**
     * 获取图片文件
     *
     * @param thumbnail 是否是缩略图
     */
    public void getImageFile(ImMessageBean bean, boolean thumbnail, CommonCallback<File> commonCallback) {
        if (mImClient != null) {
            mImClient.getImageFile(bean, thumbnail, commonCallback);
        }
    }

    /**
     * 获取语音文件
     */
    public void getSoundFile(ImMessageBean bean, CommonCallback<File> commonCallback) {
        if (mImClient != null) {
            mImClient.getSoundFile(bean, commonCallback);
        }
    }

    /**
     * 发送单聊文本消息
     *
     * @param text  消息内容
     * @param toUid 对方的uid
     */
    public void sendC2CTextMessage(String text, String toUid, ImMessageUtil.SendMsgCallback callback) {
        text = WordFilterUtil.getInstance().filter(text);
        if (mImClient != null) {
            mImClient.sendC2CTextMessage(text, toUid, callback);
        }
    }

    /**
     * 发送单聊图片消息
     *
     * @param filePath 图片文件本地路径
     * @param toUid    对方的uid
     */
    public void sendC2CImageMessage(String filePath, String toUid, final ImMessageUtil.SendMsgCallback callback) {
        if (mImClient != null) {
            mImClient.sendC2CImageMessage(filePath, toUid, callback);
        }
    }

    /**
     * 发送单聊语音消息
     *
     * @param filePath 语音文件本地路径
     * @param toUid    对方的uid
     * @param duration 语音时长，单位 s
     */
    public void sendC2CSoundMessage(String filePath, int duration, String toUid, ImMessageUtil.SendMsgCallback callback) {
        if (mImClient != null) {
            mImClient.sendC2CSoundMessage(filePath, duration, toUid, callback);
        }
    }

    /**
     * 发送单聊位置消息
     *
     * @param address 详细地址
     * @param lng     经度
     * @param lat     纬度
     * @param toUid   对方的uid
     */
    public void sendC2CLocationMessage(String address, double lng, double lat, String toUid, SendMsgCallback callback) {
        if (mImClient != null) {
            mImClient.sendC2CLocationMessage(address, lng, lat, toUid, callback);
        }
    }

    /**
     * 删除会话记录
     */
    public void removeConversation(String uid) {
        if (mImClient != null) {
            mImClient.removeConversation(uid);
        }
    }

    /**
     * 撤回消息
     */
    public void revokeMessage(String toUid, String v2MsgId) {
        if (mImClient != null) {
            mImClient.revokeMessage(toUid, v2MsgId);
        }
    }


    /**
     * 发送商品自定义消息
     */
    public void sendGoodsMessage(List<String> toUids, String goodsJson, Runnable allComplete) {
        if (mImClient != null) {
            mImClient.sendGoodsMessage(toUids, goodsJson, allComplete);
        }
    }

    /**
     * 是否打开聊天Activity
     */
    public void setOpenChatActivity(boolean openChatActivity) {
        if (mImClient != null) {
            mImClient.setOpenChatActivity(openChatActivity);
        }
    }


    public void playRing() {
        if (mImClient != null) {
            mImClient.playRing();
        }
    }

    public void changeString(){
        if (mImClient != null) {
            mImClient.changeString();
        }
    }

    public interface SendMsgCallback {
        void onStart(ImMessageBean imMsgBean);

        void onProgress(ImMessageBean imMsgBean, int progress);

        void onEnd(ImMessageBean imMsgBean, boolean success);
    }

}
