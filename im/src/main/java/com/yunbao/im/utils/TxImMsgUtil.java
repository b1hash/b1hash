package com.yunbao.im.utils;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMConversationListener;
import com.tencent.imsdk.v2.V2TIMConversationResult;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMDownloadCallback;
import com.tencent.imsdk.v2.V2TIMElem;
import com.tencent.imsdk.v2.V2TIMImageElem;
import com.tencent.imsdk.v2.V2TIMLocationElem;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMMessageExtension;
import com.tencent.imsdk.v2.V2TIMMessageReceipt;
import com.tencent.imsdk.v2.V2TIMSDKConfig;
import com.tencent.imsdk.v2.V2TIMSDKListener;
import com.tencent.imsdk.v2.V2TIMSendCallback;
import com.tencent.imsdk.v2.V2TIMSoundElem;
import com.tencent.imsdk.v2.V2TIMTextElem;
import com.tencent.imsdk.v2.V2TIMUserFullInfo;
import com.tencent.imsdk.v2.V2TIMUserStatus;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.CommonAppContext;
import com.yunbao.common.Constants;
import com.yunbao.common.event.LoginInvalidEvent;
import com.yunbao.common.interfaces.CommonCallback;
import com.yunbao.common.utils.FaceTextUtil;
import com.yunbao.common.utils.L;
import com.yunbao.common.utils.LanguageUtil;
import com.yunbao.common.utils.RouteUtil;
import com.yunbao.common.utils.SpUtil;
import com.yunbao.common.utils.ToastUtil;
import com.yunbao.common.utils.WordUtil;
import com.yunbao.im.R;
import com.yunbao.im.bean.ImConversationBean;
import com.yunbao.im.bean.ImMessageBean;
import com.yunbao.im.event.ImConversationEvent;
import com.yunbao.im.event.ImMessageRevokeEvent;
import com.yunbao.im.event.ImUnReadCountEvent;
import com.yunbao.im.interfaces.ImClient;
import com.yunbao.im.tpns.TpnsUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * Created by cxf on 2017/8/10.
 * 腾讯IM消息实现类
 */

public class TxImMsgUtil implements ImClient {

    private static final String TAG = "腾讯IM";


    private SimpleDateFormat mSimpleDateFormat;
    private String mImageString;
    private String mVoiceString;
    private String mLocationString;
    private String mGoodsString;
    private String mEmojiString;
    private int mGoodsMsgIndex;
    private SoundPool mSoundPool;
    private int mSoundId = -1;
    private boolean mOpenChatActivity;//是否打开了聊天activity
    private boolean mInited;//是否初始化了
    private final ImUnReadCount mImUnReadCount;
    private List<String> mUnReadConIdList;//未读消息数单独计算这几个会话id
    private V2TIMValueCallback<List<V2TIMConversation>> mUnReadConIdCallback;

    public TxImMsgUtil() {
        mInited = false;
        mImUnReadCount = new ImUnReadCount();
        mUnReadConIdList = Arrays.asList(
                "c2c_" + Constants.MALL_GOODS_ORDER,
                "c2c_" + Constants.IM_MSG_CONCAT,
                "c2c_" + Constants.IM_MSG_LIKE,
                "c2c_" + Constants.IM_MSG_AT,
                "c2c_" + Constants.IM_MSG_COMMENT
        );
        changeString();
    }

    /**
     * 改变消息提示文字
     */
    @Override
    public void changeString() {
        mImageString = WordUtil.getString(R.string.im_type_image);
        mVoiceString = WordUtil.getString(R.string.im_type_voide);
        mLocationString = WordUtil.getString(R.string.im_type_location);
        mGoodsString = WordUtil.getString(R.string.im_type_goods);
        mEmojiString = WordUtil.getString(R.string.im_type_emoji);
    }

    public void init() {
        if (mInited) {
            return;
        }
        mInited = true;
        V2TIMSDKConfig config = new V2TIMSDKConfig();
        config.setLogLevel(V2TIMSDKConfig.V2TIM_LOG_INFO);
//        config.setLogListener(new V2TIMLogListener() {
//            @Override
//            public void onLog(int logLevel, String logContent) {
//                L.e(TAG, "onLog---->" + logContent);
//            }
//        });
        V2TIMManager.getInstance().addIMSDKListener(new V2TIMSDKListener() {
            @Override
            public void onConnecting() {
                L.e(TAG, "onConnecting---->连接中");
            }

            @Override
            public void onConnectSuccess() {
                L.e(TAG, "onConnectSuccess---->连接成功");
            }

            @Override
            public void onConnectFailed(int code, String error) {
                L.e(TAG, "onConnectFailed---连接失败---code---> " + code + " ---error---> " + error);
            }

            @Override
            public void onKickedOffline() {
                L.e(TAG, "onKickedOffline---->被其他终端顶掉了");
                EventBus.getDefault().post(new LoginInvalidEvent());
                RouteUtil.forwardLoginInvalid(WordUtil.getString(R.string.token已过期));
            }

            @Override
            public void onUserSigExpired() {
                L.e(TAG, "onUserSigExpired----用户签名过期了，需要重新登录");
            }

            @Override
            public void onSelfInfoUpdated(V2TIMUserFullInfo info) {
                L.e(TAG, "onSelfInfoUpdated---个人资料更新-->");
            }

            @Override
            public void onUserStatusChanged(List<V2TIMUserStatus> userStatusList) {
                L.e(TAG, "onUserStatusChanged--登录状态发生变化-->");
                if (userStatusList != null) {
                    for (V2TIMUserStatus status : userStatusList) {
                        L.e(TAG, "onUserStatusChanged---userId-->" + status.getUserID() + "---statusType---->" + status.getStatusType());
                    }
                }

            }
        });
        V2TIMManager.getInstance().initSDK(CommonAppContext.getInstance(),
                CommonAppConfig.TX_IM_APP_ID, config);
        V2TIMManager.getMessageManager().addAdvancedMsgListener(new V2TIMAdvancedMsgListener() {
            @Override
            public void onRecvNewMessage(V2TIMMessage msg) {
                L.e(TAG, "收到新消息---->来自：" + msg.getUserID());
                ImMessageBean bean = convertImMsgBean(msg);
                if (bean != null) {
                    EventBus.getDefault().post(bean);
                    playRing();
                }
            }

            @Override
            public void onRecvMessageReadReceipts(List<V2TIMMessageReceipt> receiptList) {
                super.onRecvMessageReadReceipts(receiptList);
            }

            @Override
            public void onRecvC2CReadReceipt(List<V2TIMMessageReceipt> receiptList) {
                super.onRecvC2CReadReceipt(receiptList);
            }

            @Override
            public void onRecvMessageRevoked(String msgID) {
                V2TIMManager.getMessageManager().findMessages(Collections.singletonList(msgID),
                        new V2TIMValueCallback<List<V2TIMMessage>>() {
                            @Override
                            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                                if (v2TIMMessages == null || v2TIMMessages.size() == 0) {
                                    return;
                                }
                                V2TIMMessage v2Msg = v2TIMMessages.get(0);
                                EventBus.getDefault().post(new ImMessageRevokeEvent(v2Msg.getSender(), v2Msg.getMsgID(), false));
                            }

                            @Override
                            public void onError(int code, String desc) {
                            }
                        });


            }

            @Override
            public void onRecvMessageModified(V2TIMMessage msg) {
                super.onRecvMessageModified(msg);
            }

            @Override
            public void onRecvMessageExtensionsChanged(String msgID, List<V2TIMMessageExtension> extensions) {
                super.onRecvMessageExtensionsChanged(msgID, extensions);
            }

            @Override
            public void onRecvMessageExtensionsDeleted(String msgID, List<String> extensionKeys) {
                super.onRecvMessageExtensionsDeleted(msgID, extensionKeys);
            }
        });


        V2TIMManager.getConversationManager().addConversationListener(new V2TIMConversationListener() {
            @Override
            public void onSyncServerStart() {
                //同步服务器会话开始	SDK 会在登录成功、用户上线、
                //断网重连后自动同步服务器会话，您可以监听这个事件做一些 UI 进度展示操作。
                L.e(TAG, "同步会话----->开始");
            }

            @Override
            public void onSyncServerFinish() {
                //同步服务器会话完成
                //如果会话有变更，会通过 onNewConversation/onConversationChanged 回调告知。
                L.e(TAG, "同步会话----->完成");
            }

            @Override
            public void onSyncServerFailed() {
                L.e(TAG, "同步会话----->失败");
            }

            @Override
            public void onNewConversation(List<V2TIMConversation> conversationList) {
                L.e(TAG, "会话新增----->");
            }

            @Override
            public void onConversationChanged(List<V2TIMConversation> conversationList) {
                L.e(TAG, "会话发生变化----->" + conversationList.size());
                for (V2TIMConversation con : conversationList) {
                    if (con == null) {
                        continue;
                    }
                    String userId = con.getUserID();
                    if (Constants.IM_MSG_CONCAT.equals(userId)
                            || Constants.IM_MSG_LIKE.equals(userId)
                            || Constants.IM_MSG_AT.equals(userId)
                            || Constants.IM_MSG_COMMENT.equals(userId)) {
                        continue;
                    }
                    ImConversationBean bean = new ImConversationBean();
                    bean.setConversationID(con.getConversationID());
                    bean.setUserID(userId);
                    bean.setGroupID(con.getGroupID());
                    bean.setGroup(con.getType() == V2TIMConversation.V2TIM_GROUP);
                    bean.setUnReadCount(con.getUnreadCount());
                    V2TIMMessage v2msg = con.getLastMessage();
                    if (v2msg != null) {
                        bean.setLastMsgId(v2msg.getMsgID());
                        bean.setLastMsgString(getMessageString(v2msg));
                        bean.setLastMsgTime(v2msg.getTimestamp());
                        bean.setLastMsgTimeString(getMessageTimeString(v2msg.getTimestamp()));
                    }
                    EventBus.getDefault().post(new ImConversationEvent(bean));
                }

            }

            @Override
            public void onTotalUnreadMessageCountChanged(long totalUnreadCount) {
                unReadCountChanged(totalUnreadCount);
            }

            @Override
            public void onConversationGroupCreated(String groupName, List<V2TIMConversation> conversationList) {
                super.onConversationGroupCreated(groupName, conversationList);
            }

            @Override
            public void onConversationGroupDeleted(String groupName) {
                super.onConversationGroupDeleted(groupName);
            }

            @Override
            public void onConversationGroupNameChanged(String oldName, String newName) {
                super.onConversationGroupNameChanged(oldName, newName);
            }

            @Override
            public void onConversationsAddedToGroup(String groupName, List<V2TIMConversation> conversationList) {
                super.onConversationsAddedToGroup(groupName, conversationList);
            }

            @Override
            public void onConversationsDeletedFromGroup(String groupName, List<V2TIMConversation> conversationList) {
                super.onConversationsDeletedFromGroup(groupName, conversationList);
            }
        });

    }


    /**
     * 登录腾讯IM
     */
    public void loginImClient(String uid) {
        //如果用户已经处于已登录和登录中状态，请勿再频繁调用登录接口登录
        int loginStatus = V2TIMManager.getInstance().getLoginStatus();
        if (loginStatus == V2TIMManager.V2TIM_STATUS_LOGINED) {
            L.e(TAG, "已经登录了");
            getAllUnreadCount();
            return;
        }
        //登录TPNS推送
        TpnsUtil.bindPushAccount();
        if (loginStatus == V2TIMManager.V2TIM_STATUS_LOGINING) {
            L.e(TAG, "正在登录中");
            return;
        }
        String userSig = SpUtil.getInstance().getStringValue(SpUtil.TX_IM_USER_SIGN);
        if (TextUtils.isEmpty(userSig)) {
            L.e(TAG, "userSig不存在，无法登录");
            return;
        }
        L.e(TAG, "开始登录--userSig-->" + userSig);
        V2TIMManager.getInstance().login(CommonAppConfig.getInstance().getUid(),
                userSig, new V2TIMCallback() {
                    @Override
                    public void onSuccess() {
                        L.e(TAG, "登录成功----->");
                        getAllUnreadCount();
                    }

                    @Override
                    public void onError(int code, String desc) {
                        // 如果返回以下错误码，表示使用 UserSig 已过期，请您使用新签发的 UserSig 进行再次登录。
                        // 1. ERR_USER_SIG_EXPIRED（6206）
                        // 2. ERR_SVR_ACCOUNT_USERSIG_EXPIRED（70001）
                        // 注意：其他的错误码，请不要在这里调用登录接口，避免 IM SDK 登录进入死循环。
                        L.e(TAG, "登录失败---code-->" + code + "---desc-->" + desc);
                    }
                });
    }

    /**
     * 登出腾讯IM
     */
    public void logoutImClient() {
        if (mImUnReadCount != null) {
            mImUnReadCount.clear();
        }
        EventBus.getDefault().post(new ImUnReadCountEvent());
        V2TIMManager.getInstance().logout(new V2TIMCallback() {
            @Override
            public void onSuccess() {
                L.e(TAG, "登出成功----->");
            }

            @Override
            public void onError(int code, String desc) {
                L.e(TAG, "登出失败---code-->" + code + "---desc-->" + desc);
            }
        });
    }

    /**
     * 主动获取未读消息总数
     */
    private void getAllUnreadCount() {
        V2TIMManager.getConversationManager().getTotalUnreadMessageCount(new V2TIMValueCallback<Long>() {
            @Override
            public void onSuccess(Long totalUnreadCount) {
                L.e(TAG, "主动获取未读消息数成功----->" + totalUnreadCount);
                unReadCountChanged(totalUnreadCount);
            }

            @Override
            public void onError(int code, String desc) {
                L.e(TAG, "主动获取未读消息数失败---code-->" + code + "---desc-->" + desc);
            }
        });
    }

    /**
     * 显示未读消息总数
     */
    private void unReadCountChanged(long totalUnreadCount) {
        L.e("腾讯IM", "未读消息总数----->" + totalUnreadCount);
        if (mImUnReadCount != null) {
            mImUnReadCount.setTotalCount((int) totalUnreadCount);
        }
        if (mUnReadConIdCallback == null) {
            mUnReadConIdCallback = new V2TIMValueCallback<List<V2TIMConversation>>() {
                @Override
                public void onSuccess(List<V2TIMConversation> conversationList) {
                    if (mImUnReadCount != null) {
                        if (conversationList != null) {
                            for (V2TIMConversation con : conversationList) {
                                if (con != null) {
                                    String userId = con.getUserID();
                                    if (Constants.MALL_GOODS_ORDER.equals(userId)) {
                                        mImUnReadCount.setGoodsOrderCount(con.getUnreadCount());
                                    } else if (Constants.IM_MSG_CONCAT.equals(userId)) {
                                        mImUnReadCount.setConcatCount(con.getUnreadCount());
                                    } else if (Constants.IM_MSG_LIKE.equals(userId)) {
                                        mImUnReadCount.setLikeCount(con.getUnreadCount());
                                    } else if (Constants.IM_MSG_AT.equals(userId)) {
                                        mImUnReadCount.setAtCount(con.getUnreadCount());
                                    } else if (Constants.IM_MSG_COMMENT.equals(userId)) {
                                        mImUnReadCount.setCommentCount(con.getUnreadCount());
                                    }
                                }
                            }
                        }
                        EventBus.getDefault().post(new ImUnReadCountEvent());
                    }
                }

                @Override
                public void onError(int code, String desc) {
                    if (mImUnReadCount != null) {
                        EventBus.getDefault().post(new ImUnReadCountEvent());
                    }
                }
            };
        }
        V2TIMManager.getConversationManager().getConversationList(mUnReadConIdList, mUnReadConIdCallback);
    }

    /**
     * 获取会话列表
     */
    @Override
    public void getConversationList(final CommonCallback<List<ImConversationBean>> callback) {
        final List<ImConversationBean> conList = new ArrayList<>();
        V2TIMManager.getConversationManager().getConversationList(0, Integer.MAX_VALUE, new V2TIMValueCallback<V2TIMConversationResult>() {
            @Override
            public void onSuccess(V2TIMConversationResult v2TIMConversationResult) {
                List<V2TIMConversation> v2TIMConversationList = v2TIMConversationResult.getConversationList();
                if (v2TIMConversationList != null) {
                    L.e(TAG, "获取会话列表成功---->" + v2TIMConversationList.size());
                    for (V2TIMConversation con : v2TIMConversationList) {
                        if (con == null) {
                            continue;
                        }
                        String userId = con.getUserID();
                        if (Constants.IM_MSG_CONCAT.equals(userId)
                                || Constants.IM_MSG_LIKE.equals(userId)
                                || Constants.IM_MSG_AT.equals(userId)
                                || Constants.IM_MSG_COMMENT.equals(userId)) {
                            continue;
                        }
                        ImConversationBean bean = new ImConversationBean();
                        bean.setConversationID(con.getConversationID());
                        bean.setUserID(userId);
                        bean.setGroupID(con.getGroupID());
                        bean.setGroup(con.getType() == V2TIMConversation.V2TIM_GROUP);
                        bean.setUnReadCount(con.getUnreadCount());
                        V2TIMMessage v2msg = con.getLastMessage();
                        if (v2msg != null) {
                            bean.setLastMsgId(v2msg.getMsgID());
                            bean.setLastMsgString(getMessageString(v2msg));
                            bean.setLastMsgTime(v2msg.getTimestamp());
                            bean.setLastMsgTimeString(getMessageTimeString(v2msg.getTimestamp()));
                        }
                        conList.add(bean);
                    }
                }
                if (callback != null) {
                    callback.callback(conList);
                }
            }

            @Override
            public void onError(int code, String desc) {
                L.e(TAG, "获取会话列表失败--code-->" + code + "---desc-->" + desc);
                if (callback != null) {
                    callback.callback(conList);
                }
            }
        });
    }

    /**
     * 发送单聊文本消息
     *
     * @param text  消息内容
     * @param toUid 对方的uid
     */
    @Override
    public void sendC2CTextMessage(String text, String toUid, final ImMessageUtil.SendMsgCallback callback) {
        V2TIMMessage v2msg = V2TIMManager.getMessageManager().createTextMessage(text);
        ImMessageBean.TextMsgBean entryBean = new ImMessageBean.TextMsgBean(
                v2msg.getMsgID(),
                v2msg.getSender(),
                v2msg.getGroupID(),
                v2msg.getTimestamp(),
                v2msg.isSelf());
        entryBean.setText(text);
        final ImMessageBean imMessageBean = new ImMessageBean(entryBean, ImMessageBean.TYPE_TEXT, ImMessageBean.STATUS_SEND_ING);
        if (callback != null) {
            callback.onStart(imMessageBean);
        }
        V2TIMManager.getMessageManager().sendMessage(v2msg, toUid, null, V2TIMMessage.V2TIM_PRIORITY_NORMAL, false, null, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int progress) {
                // 文本消息不会回调进度
                if (callback != null) {
                    callback.onProgress(imMessageBean, progress);
                }
            }

            @Override
            public void onSuccess(V2TIMMessage message) {
                // 文本消息发送成功
                imMessageBean.setMsgId(message.getMsgID());
                if (callback != null) {
                    callback.onEnd(imMessageBean, true);
                }
            }

            @Override
            public void onError(int code, String desc) {
                // 文本消息发送失败
                if (callback != null) {
                    callback.onEnd(imMessageBean, false);
                }
            }
        });
    }

    /**
     * 发送单聊图片消息
     *
     * @param filePath 图片文件本地路径
     * @param toUid    对方的uid
     */
    @Override
    public void sendC2CImageMessage(String filePath, String toUid, final ImMessageUtil.SendMsgCallback callback) {
        V2TIMMessage v2TIMMessage = V2TIMManager.getMessageManager().createImageMessage(filePath);
        ImMessageBean.ImageMsgBean entryBean = new ImMessageBean.ImageMsgBean(
                v2TIMMessage.getMsgID(),
                v2TIMMessage.getSender(),
                v2TIMMessage.getGroupID(),
                v2TIMMessage.getTimestamp(),
                v2TIMMessage.isSelf());
        entryBean.setLocalPath(filePath);
        final ImMessageBean imMessageBean = new ImMessageBean(entryBean, ImMessageBean.TYPE_IMAGE, ImMessageBean.STATUS_SEND_ING);
        if (callback != null) {
            callback.onStart(imMessageBean);
        }
        V2TIMManager.getMessageManager().sendMessage(v2TIMMessage, toUid, null, V2TIMMessage.V2TIM_PRIORITY_NORMAL, false, null, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int progress) {
                // 图片上传进度，progress 取值 [0, 100]
                if (callback != null) {
                    callback.onProgress(imMessageBean, progress);
                }
            }

            @Override
            public void onSuccess(V2TIMMessage message) {
                // 图片消息发送成功
                imMessageBean.setMsgId(message.getMsgID());
                if (callback != null) {
                    callback.onEnd(imMessageBean, true);
                }
            }

            @Override
            public void onError(int code, String desc) {
                // 图片消息发送失败
                if (callback != null) {
                    callback.onEnd(imMessageBean, false);
                }
            }
        });
    }

    /**
     * 发送单聊语音消息
     *
     * @param filePath 语音文件本地路径
     * @param toUid    对方的uid
     * @param duration 语音时长，单位 s
     */
    @Override
    public void sendC2CSoundMessage(String filePath, int duration, String toUid, final ImMessageUtil.SendMsgCallback callback) {
        V2TIMMessage v2msg = V2TIMManager.getMessageManager().createSoundMessage(filePath, duration);
        ImMessageBean.SoundMsgBean entryBean = new ImMessageBean.SoundMsgBean(
                v2msg.getMsgID(),
                v2msg.getSender(),
                v2msg.getGroupID(),
                v2msg.getTimestamp(),
                v2msg.isSelf());
        V2TIMSoundElem soundElem = v2msg.getSoundElem();
        if (soundElem != null) {
            entryBean.setDuration(soundElem.getDuration());
        }
        final ImMessageBean imMessageBean = new ImMessageBean(entryBean, ImMessageBean.TYPE_SOUND, ImMessageBean.STATUS_SEND_ING);
        if (callback != null) {
            callback.onStart(imMessageBean);
        }
        V2TIMManager.getMessageManager().sendMessage(v2msg, toUid, null, V2TIMMessage.V2TIM_PRIORITY_NORMAL, false, null, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int progress) {
                // 语音上传进度，progress 取值 [0, 100]
                if (callback != null) {
                    callback.onProgress(imMessageBean, progress);
                }
            }

            @Override
            public void onSuccess(V2TIMMessage message) {
                // 语音消息发送成功
                imMessageBean.setMsgId(message.getMsgID());
                if (callback != null) {
                    callback.onEnd(imMessageBean, true);
                }
            }

            @Override
            public void onError(int code, String desc) {
                // 语音消息发送失败
                if (callback != null) {
                    callback.onEnd(imMessageBean, false);
                }
            }
        });
    }

    /**
     * 发送单聊位置消息
     *
     * @param address 详细地址
     * @param lng     经度
     * @param lat     纬度
     * @param toUid   对方的uid
     */
    @Override
    public void sendC2CLocationMessage(String address, double lng, double lat, String toUid, final ImMessageUtil.SendMsgCallback callback) {
        final V2TIMMessage v2TIMMessage = V2TIMManager.getMessageManager().createLocationMessage(address, lng, lat);
        ImMessageBean.LocationMsgBean entryBean = new ImMessageBean.LocationMsgBean(
                v2TIMMessage.getMsgID(),
                v2TIMMessage.getSender(),
                v2TIMMessage.getGroupID(),
                v2TIMMessage.getTimestamp(),
                v2TIMMessage.isSelf());
        V2TIMLocationElem locationElem = v2TIMMessage.getLocationElem();
        if (locationElem != null) {
            entryBean.setLng(locationElem.getLongitude());
            entryBean.setLat(locationElem.getLatitude());
            entryBean.setAddress(locationElem.getDesc());
        }
        final ImMessageBean imMessageBean = new ImMessageBean(entryBean, ImMessageBean.TYPE_LOCATION, ImMessageBean.STATUS_SEND_ING);
        if (callback != null) {
            callback.onStart(imMessageBean);
        }
        V2TIMManager.getMessageManager().sendMessage(v2TIMMessage, toUid, null, V2TIMMessage.V2TIM_PRIORITY_NORMAL, false, null, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int progress) {
                // 定位消息不回调进度
                if (callback != null) {
                    callback.onProgress(imMessageBean, progress);
                }
            }

            @Override
            public void onSuccess(V2TIMMessage message) {
                // 定位消息发送成功
                imMessageBean.setMsgId(message.getMsgID());
                if (callback != null) {
                    callback.onEnd(imMessageBean, true);
                }
            }

            @Override
            public void onError(int code, String desc) {
                // 定位消息发送失败
                if (callback != null) {
                    callback.onEnd(imMessageBean, false);
                }
            }
        });
    }


    /**
     * 拉取单聊历史消息
     */
    @Override
    public void getMessageList(String toUid, final CommonCallback<List<ImMessageBean>> callback) {
        // 拉取单聊历史消息，首次拉取，lastMsg 设置为 null
        // 再次拉取时，lastMsg 可以使用返回的消息列表中的最后一条消息
        V2TIMManager.getMessageManager().getC2CHistoryMessageList(toUid, 20, null, new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onError(int code, String desc) {
                L.e(TAG, "拉取单聊历史消息失败---code-->" + code + "---desc--->" + desc);
            }

            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                L.e(TAG, "拉取单聊历史消息成功------>");
                List<ImMessageBean> list = new ArrayList<>();
                for (int i = v2TIMMessages.size() - 1; i >= 0; i--) {
                    ImMessageBean msgBean = convertImMsgBean(v2TIMMessages.get(i));
                    if (msgBean != null) {
                        list.add(msgBean);
                    }
                }
                if (callback != null) {
                    callback.callback(list);
                }
            }
        });
    }


    private ImMessageBean convertImMsgBean(V2TIMMessage v2msg) {
        if (v2msg == null) {
            return null;
        }
        int msgStatus;
        switch (v2msg.getStatus()) {
            case V2TIMMessage.V2TIM_MSG_STATUS_SENDING:
                msgStatus = ImMessageBean.STATUS_SEND_ING;
                break;
            case V2TIMMessage.V2TIM_MSG_STATUS_SEND_FAIL:
                msgStatus = ImMessageBean.STATUS_SEND_FAIL;
                break;
            default:
                msgStatus = ImMessageBean.STATUS_SEND_SUCC;
        }
        ImMessageBean msgBean = null;
        int msgType = v2msg.getElemType();
        if (msgType == V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
            V2TIMCustomElem customElem = v2msg.getCustomElem();
            String data = new String(customElem.getData());
            if (!TextUtils.isEmpty(data)) {
                try {
                    JSONObject obj = JSON.parseObject(data);
                    String method = obj.getString("method");
                    if (Constants.IM_CUSTOM_METHOD_GOODS.equals(method)) {
                        ImMessageBean.GoodsMsgBean entryBean = new ImMessageBean.GoodsMsgBean(
                                v2msg.getMsgID(),
                                v2msg.getSender(),
                                v2msg.getGroupID(),
                                v2msg.getTimestamp(),
                                v2msg.isSelf());
                        entryBean.setGoodsId(obj.getString("goodsid"));
                        msgBean = new ImMessageBean(entryBean, ImMessageBean.TYPE_GOODS, msgStatus);
                    } else if (Constants.IM_CUSTOM_METHOD_ORDER.equals(method)) {
                        ImMessageBean.TextMsgBean entryBean = new ImMessageBean.TextMsgBean(
                                v2msg.getMsgID(),
                                v2msg.getSender(),
                                v2msg.getGroupID(),
                                v2msg.getTimestamp(),
                                v2msg.isSelf());
                        String msgText = "";
                        if (LanguageUtil.isZh()) {
                            msgText = obj.getString(Constants.LANG_ZH);
                        } else {
                            msgText = obj.getString(Constants.LANG_EN);
                        }
                        entryBean.setText(msgText);
                        msgBean = new ImMessageBean(entryBean, ImMessageBean.TYPE_TEXT, msgStatus);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (msgType == V2TIMMessage.V2TIM_ELEM_TYPE_TEXT) {
            ImMessageBean.TextMsgBean entryBean = new ImMessageBean.TextMsgBean(
                    v2msg.getMsgID(),
                    v2msg.getSender(),
                    v2msg.getGroupID(),
                    v2msg.getTimestamp(),
                    v2msg.isSelf());
            entryBean.setText(v2msg.getTextElem().getText());
            msgBean = new ImMessageBean(entryBean, ImMessageBean.TYPE_TEXT, msgStatus);
        } else if (msgType == V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE) {
            ImMessageBean.ImageMsgBean entryBean = new ImMessageBean.ImageMsgBean(
                    v2msg.getMsgID(),
                    v2msg.getSender(),
                    v2msg.getGroupID(),
                    v2msg.getTimestamp(),
                    v2msg.isSelf());
            V2TIMImageElem imageElem = v2msg.getImageElem();
            if (imageElem != null) {
                List<V2TIMImageElem.V2TIMImage> v2ImageList = imageElem.getImageList();
                if (v2ImageList != null) {
                    for (V2TIMImageElem.V2TIMImage v2TIMImage : v2ImageList) {
                        if (v2TIMImage.getType() == V2TIMImageElem.V2TIM_IMAGE_TYPE_ORIGIN) {
                            entryBean.setOriginImageUrl(v2TIMImage.getUrl());
                            entryBean.setWidth(v2TIMImage.getWidth());
                            entryBean.setHeight(v2TIMImage.getHeight());
                        } else if (v2TIMImage.getType() == V2TIMImageElem.V2TIM_IMAGE_TYPE_THUMB) {
                            entryBean.setThumbImageUrl(v2TIMImage.getUrl());
                            entryBean.setWidth(v2TIMImage.getWidth());
                            entryBean.setHeight(v2TIMImage.getHeight());
                        }
                    }
                }
            }
            msgBean = new ImMessageBean(entryBean, ImMessageBean.TYPE_IMAGE, msgStatus);
        } else if (msgType == V2TIMMessage.V2TIM_ELEM_TYPE_SOUND) {
            ImMessageBean.SoundMsgBean entryBean = new ImMessageBean.SoundMsgBean(
                    v2msg.getMsgID(),
                    v2msg.getSender(),
                    v2msg.getGroupID(),
                    v2msg.getTimestamp(),
                    v2msg.isSelf());
            V2TIMSoundElem soundElem = v2msg.getSoundElem();
            if (soundElem != null) {
                entryBean.setDuration(soundElem.getDuration());
            }
            msgBean = new ImMessageBean(entryBean, ImMessageBean.TYPE_SOUND, msgStatus);
        } else if (msgType == V2TIMMessage.V2TIM_ELEM_TYPE_LOCATION) {
            ImMessageBean.LocationMsgBean entryBean = new ImMessageBean.LocationMsgBean(
                    v2msg.getMsgID(),
                    v2msg.getSender(),
                    v2msg.getGroupID(),
                    v2msg.getTimestamp(),
                    v2msg.isSelf());
            V2TIMLocationElem locationElem = v2msg.getLocationElem();
            if (locationElem != null) {
                entryBean.setLng(locationElem.getLongitude());
                entryBean.setLat(locationElem.getLatitude());
                entryBean.setAddress(locationElem.getDesc());
            }
            msgBean = new ImMessageBean(entryBean, ImMessageBean.TYPE_LOCATION, msgStatus);
        }
        if (msgBean != null) {
            msgBean.setRevoked(v2msg.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_LOCAL_REVOKED);
        }
        return msgBean;
    }


    /**
     * 获取图片文件
     *
     * @param thumbnail 是否是缩略图
     */
    @Override
    public void getImageFile(ImMessageBean bean, final boolean thumbnail, final CommonCallback<File> commonCallback) {
        ImMessageBean.ImageMsgBean imageMsgBean = bean.getImageBean();
        if (imageMsgBean == null) {
            return;
        }
        if (imageMsgBean.isSelf()) {
            String localPath = imageMsgBean.getLocalPath();
            if (!TextUtils.isEmpty(localPath)) {
                File localFile = new File(localPath);
                if (localFile.exists()) {
                    if (commonCallback != null) {
                        commonCallback.callback(localFile);
                    }
                    return;
                }
            }
        }
        V2TIMManager.getMessageManager().findMessages(Collections.singletonList(bean.getMsgId()),
                new V2TIMValueCallback<List<V2TIMMessage>>() {
                    @Override
                    public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                        if (v2TIMMessages == null || v2TIMMessages.size() == 0) {
                            return;
                        }
                        V2TIMMessage v2Msg = v2TIMMessages.get(0);
                        V2TIMImageElem imageElem = v2Msg.getImageElem();
                        if (imageElem == null) {
                            return;
                        }
                        List<V2TIMImageElem.V2TIMImage> list = imageElem.getImageList();
                        V2TIMImageElem.V2TIMImage target = null;
                        for (V2TIMImageElem.V2TIMImage v2TIMImage : list) {
                            if (thumbnail) {
                                if (v2TIMImage.getType() == V2TIMImageElem.V2TIM_IMAGE_TYPE_THUMB) {
                                    target = v2TIMImage;
                                    break;
                                }
                            } else {
                                if (v2TIMImage.getType() == V2TIMImageElem.V2TIM_IMAGE_TYPE_ORIGIN) {
                                    target = v2TIMImage;
                                    break;
                                }
                            }
                        }
                        if (target == null) {
                            return;
                        }
                        File dir = new File(CommonAppConfig.IM_IMAGE);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        final File targetFile = new File(dir, target.getUUID());
                        if (targetFile.exists()) {
                            if (commonCallback != null) {
                                commonCallback.callback(targetFile);
                            }
                            return;
                        }
                        target.downloadImage(targetFile.getAbsolutePath(), new V2TIMDownloadCallback() {
                            @Override
                            public void onProgress(V2TIMElem.V2ProgressInfo v2ProgressInfo) {

                            }

                            @Override
                            public void onSuccess() {
                                if (commonCallback != null) {
                                    commonCallback.callback(targetFile);
                                }
                            }

                            @Override
                            public void onError(int i, String s) {

                            }
                        });
                    }

                    @Override
                    public void onError(int code, String desc) {
                    }
                });
    }

    /**
     * 获取语音文件
     */
    @Override
    public void getSoundFile(ImMessageBean bean, final CommonCallback<File> commonCallback) {
        ImMessageBean.SoundMsgBean soundMsgBean = bean.getSoundBean();
        if (soundMsgBean == null) {
            return;
        }
        final File soundFile = soundMsgBean.getSoundFile();
        if (soundFile.exists()) {
            if (commonCallback != null) {
                commonCallback.callback(soundFile);
            }
            return;
        }
        File dir = soundFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        V2TIMManager.getMessageManager().findMessages(Collections.singletonList(bean.getMsgId()),
                new V2TIMValueCallback<List<V2TIMMessage>>() {
                    @Override
                    public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                        if (v2TIMMessages == null || v2TIMMessages.size() == 0) {
                            return;
                        }
                        V2TIMMessage v2Msg = v2TIMMessages.get(0);
                        V2TIMSoundElem soundElem = v2Msg.getSoundElem();
                        soundElem.downloadSound(soundFile.getAbsolutePath(), new V2TIMDownloadCallback() {
                            @Override
                            public void onProgress(V2TIMElem.V2ProgressInfo v2ProgressInfo) {

                            }

                            @Override
                            public void onSuccess() {
                                if (commonCallback != null) {
                                    commonCallback.callback(soundFile);
                                }
                            }

                            @Override
                            public void onError(int code, String desc) {
                                L.e(TAG, "下载声音文件失败-----code--->" + code + "--desc---->" + desc);
                            }
                        });
                    }

                    @Override
                    public void onError(int code, String desc) {
                    }
                });
    }

    /**
     * 返回消息的字符串描述
     */
    private String getMessageString(V2TIMMessage v2msg) {
        if (v2msg == null) {
            return "";
        }
        String result = "";
        if (v2msg.getStatus() == V2TIMMessage.V2TIM_MSG_STATUS_LOCAL_REVOKED) {
            if (v2msg.isSelf()) {
                result = WordUtil.getString(R.string.chat_msg_prompt_0);
            } else {
                result = WordUtil.getString(R.string.chat_msg_prompt_1);
            }
            return result;
        }
        int msgType = v2msg.getElemType();
        if (msgType == V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
            V2TIMCustomElem customElem = v2msg.getCustomElem();
            String data = new String(customElem.getData());
            if (!TextUtils.isEmpty(data)) {
                try {
                    JSONObject obj = JSON.parseObject(data);
                    String method = obj.getString("method");
                    if (Constants.IM_CUSTOM_METHOD_GOODS.equals(method)) {
                        result = mGoodsString;
                    } else if (Constants.IM_CUSTOM_METHOD_ORDER.equals(method)) {
                        if (LanguageUtil.isZh()) {
                            result = obj.getString(Constants.LANG_ZH);
                        } else {
                            result = obj.getString(Constants.LANG_EN);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (msgType == V2TIMMessage.V2TIM_ELEM_TYPE_TEXT) {
            V2TIMTextElem textElem = v2msg.getTextElem();
            if (textElem != null) {
                result = textElem.getText();
                if (!LanguageUtil.isZh()) {
                    result = FaceTextUtil.replaceChatMessageText(result, mEmojiString);
                }
            }
        } else if (msgType == V2TIMMessage.V2TIM_ELEM_TYPE_IMAGE) {
            result = mImageString;
        } else if (msgType == V2TIMMessage.V2TIM_ELEM_TYPE_SOUND) {
            result = mVoiceString;
        } else if (msgType == V2TIMMessage.V2TIM_ELEM_TYPE_LOCATION) {
            result = mLocationString;
        }
        return result;
    }

    private String getMessageTimeString(long timestamp) {
        if (mSimpleDateFormat == null) {
            mSimpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
        }
        return mSimpleDateFormat.format(new Date(timestamp * 1000));
    }

    /**
     * 获取未读消息数
     */
    @Override
    public ImUnReadCount getUnReadMsgCount() {
        return mImUnReadCount;
    }


    /**
     * 设置某个会话的消息为已读
     *
     * @param toUid 对方uid
     */
    @Override
    public void markConversationAsRead(final String toUid) {
        if (!TextUtils.isEmpty(toUid)) {
            V2TIMManager.getMessageManager().markC2CMessageAsRead(toUid, new V2TIMCallback() {
                @Override
                public void onSuccess() {
                    L.e(TAG, toUid + "_会话标记已读成功----->");
                }

                @Override
                public void onError(int code, String desc) {
                    L.e(TAG, toUid + "_会话标记已读失败---code-->" + code + "----desc--->" + desc);
                }
            });
        }
    }

    /**
     * 标记所有会话为已读  即忽略所有未读
     */
    public void markAllConversationAsRead() {
        V2TIMManager.getMessageManager().markAllMessageAsRead(new V2TIMCallback() {
            @Override
            public void onSuccess() {
                L.e(TAG, "全部会话标记已读成功----->");
            }

            @Override
            public void onError(int code, String desc) {
                L.e(TAG, "全部会话标记已读失败---code-->" + code + "----desc--->" + desc);
            }
        });
    }

    /**
     * 发送商品消息
     */
    public void sendGoodsMessage(List<String> toUids, String goodsJson, Runnable allComplete) {
        if (toUids == null || toUids.size() == 0 || TextUtils.isEmpty(goodsJson)) {
            return;
        }
        // 创建自定义消息
        V2TIMMessage goodsMsg = V2TIMManager.getMessageManager().createCustomMessage(goodsJson.getBytes());
        mGoodsMsgIndex = 0;
        nextGoodsMessage(toUids, goodsMsg, allComplete);
    }

    /**
     * 发送商品消息
     */
    private void nextGoodsMessage(final List<String> toUids, final V2TIMMessage goodsMsg, final Runnable allComplete) {
        final String toUid = toUids.get(mGoodsMsgIndex);
        L.e(TAG, "sendCustomMessage----开始----> " + toUid);
        V2TIMManager.getMessageManager().sendMessage(goodsMsg, toUid, null, V2TIMMessage.V2TIM_PRIORITY_NORMAL, false, null, new V2TIMSendCallback<V2TIMMessage>() {
            @Override
            public void onProgress(int progress) {
                // 自定义消息不会回调进度
            }

            @Override
            public void onSuccess(V2TIMMessage message) {
                // 发送单聊自定义消息成功
                L.e(TAG, "sendCustomMessage----结束ok----> " + toUid);
                mGoodsMsgIndex++;
                if (mGoodsMsgIndex < toUids.size()) {
                    nextGoodsMessage(toUids, goodsMsg, allComplete);
                } else {
                    L.e(TAG, "sendCustomMessage----全部完成----> ");
                    if (allComplete != null) {
                        allComplete.run();
                    }
                }
            }

            @Override
            public void onError(int code, String desc) {
                // 发送单聊自定义消息失败
                L.e(TAG, "sendCustomMessage----结束error----> " + toUid + "----code---->" + code + "----desc-->" + desc);
                mGoodsMsgIndex++;
                if (mGoodsMsgIndex < toUids.size()) {
                    nextGoodsMessage(toUids, goodsMsg, allComplete);
                } else {
                    L.e(TAG, "sendCustomMessage----全部完成----> ");
                    if (allComplete != null) {
                        allComplete.run();
                    }
                }
            }
        });
    }


    /**
     * 删除会话记录
     */
    public void removeConversation(String touid) {
        V2TIMManager.getConversationManager().deleteConversation("c2c_" + touid, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                L.e(TAG, "删除会话成功-----> ");
            }

            @Override
            public void onError(int code, String desc) {
                L.e(TAG, "删除会话失败---code-->" + code + "----desc-->" + desc);
            }
        });
    }

    /**
     * 撤回消息
     */
    @Override
    public void revokeMessage(final String toUid, final String v2MsgId) {
        if (TextUtils.isEmpty(toUid) || TextUtils.isEmpty(v2MsgId)) {
            return;
        }
        V2TIMManager.getMessageManager().findMessages(Collections.singletonList(v2MsgId), new V2TIMValueCallback<List<V2TIMMessage>>() {
            @Override
            public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                if (v2TIMMessages == null || v2TIMMessages.size() == 0) {
                    return;
                }
                V2TIMMessage v2Msg = v2TIMMessages.get(0);
                V2TIMManager.getMessageManager().revokeMessage(v2Msg, new V2TIMCallback() {
                    @Override
                    public void onError(int code, String desc) {
                        // 撤回消息失败
                        L.e(TAG, "撤回消息失败----code--> " + code + " ----desc--> " + desc);
                        //10031 是撤回群消息 ，20016是撤回单聊消息
                        if (code == 20016 || code == 10031) {
                            ToastUtil.show(R.string.chat_msg_prompt_2);
                        } else {
                            ToastUtil.show(R.string.chat_msg_prompt_3);
                        }
                    }

                    @Override
                    public void onSuccess() {
                        // 撤回消息成功
                        L.e(TAG, "撤回消息成功----->");
                        EventBus.getDefault().post(new ImMessageRevokeEvent(toUid, v2MsgId, true));
                    }
                });
            }

            @Override
            public void onError(int code, String desc) {
            }
        });
    }


    /**
     * 播放消息提示音
     */
    @Override
    public void playRing() {
        if (mOpenChatActivity || !SpUtil.getInstance().isImMsgRingOpen()) {
            return;
        }
        if (mSoundPool == null) {
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(1);
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            builder.setAudioAttributes(attrBuilder.build());
            mSoundPool = builder.build();
            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    if (status == 0 && mSoundId != -1) {
                        soundPool.play(mSoundId, 1, 1, 1, 0, 1);
                    }
                }
            });
        }
        if (mSoundId == -1) {
            mSoundId = mSoundPool.load(CommonAppContext.getInstance(), R.raw.msg_ring, 1);
        } else {
            mSoundPool.play(mSoundId, 1, 1, 1, 0, 1);
        }
    }

    @Override
    public void setOpenChatActivity(boolean openChatActivity) {
        mOpenChatActivity = openChatActivity;
    }

}
