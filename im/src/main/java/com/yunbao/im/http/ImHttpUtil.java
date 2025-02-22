package com.yunbao.im.http;

import com.yunbao.common.CommonAppConfig;
import com.yunbao.common.http.HttpCallback;
import com.yunbao.common.http.HttpClient;

/**
 * Created by cxf on 2019/2/26.
 */

public class ImHttpUtil {

    /**
     * 取消网络请求
     */
    public static void cancel(String tag) {
        HttpClient.getInstance().cancel(tag);
    }

    /**
     * 私信聊天页面用于获取用户信息
     */
    public static void getImUserInfo(String uids, HttpCallback callback) {
        getImUserInfo(uids, ImHttpConsts.GET_IM_USER_INFO, callback);
    }

    /**
     * 私信聊天页面用于获取用户信息
     */
    public static void getImUserInfo(String uids, String requsetTag, HttpCallback callback) {
        HttpClient.getInstance().get("User.GetUidsInfo", requsetTag)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("uids", uids)
                .execute(callback);
    }

    /**
     * 获取系统消息列表
     */
    public static void getSystemMessageList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Message.GetList", ImHttpConsts.GET_SYSTEM_MESSAGE_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }

    /**
     * 判断自己有没有被对方拉黑，聊天的时候用到
     */
    public static void checkBlack(String touid, HttpCallback callback) {
        HttpClient.getInstance().get("User.checkBlack", ImHttpConsts.CHECK_BLACK)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("touid", touid)
                .execute(callback);
    }


    /**
     * 获取用户被@ 的信息列表
     */
    public static void getImAtList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Message.atLists", ImHttpConsts.GET_IM_AT_LISTS)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }


    /**
     * 获取赞的消息列表
     */
    public static void getImLikeList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Message.praiseLists", ImHttpConsts.GET_IM_LIKE_LISTS)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }


    /**
     * 获取评论信息列表
     */
    public static void getImCommentList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Message.commentLists", ImHttpConsts.GET_IM_COMMENT_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }


    /**
     * 获取粉丝关注信息列表
     */
    public static void getImConcatList(int p, HttpCallback callback) {
        HttpClient.getInstance().get("Message.fansLists", ImHttpConsts.GET_IM_CONCAT_LIST)
                .params("uid", CommonAppConfig.getInstance().getUid())
                .params("token", CommonAppConfig.getInstance().getToken())
                .params("p", p)
                .execute(callback);
    }


}
